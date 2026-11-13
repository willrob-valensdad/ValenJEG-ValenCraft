#!/usr/bin/env python3
"""Stage pack labels without changing mod IDs, versions, code or assets."""
import argparse
import copy
import hashlib
import io
import json
from pathlib import Path
import zipfile


def parse_metadata(raw):
    # Fabric accepts comments and literal newlines in metadata strings.
    text = raw.decode("utf-8-sig")
    result, index, quoted, escaped = [], 0, False, False
    while index < len(text):
        char = text[index]
        if quoted:
            result.append(char)
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                quoted = False
            index += 1
        elif char == '"':
            quoted = True
            result.append(char)
            index += 1
        elif text.startswith("//", index):
            end = text.find("\n", index)
            index = len(text) if end < 0 else end
        elif text.startswith("/*", index):
            end = text.find("*/", index + 2)
            if end < 0:
                raise ValueError("Unclosed metadata comment")
            result.append(" ")
            index = end + 2
        else:
            result.append(char)
            index += 1
    return json.loads("".join(result), strict=False)


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def catalog(raw, location):
    with zipfile.ZipFile(io.BytesIO(raw)) as archive:
        if "fabric.mod.json" not in archive.namelist():
            return []
        metadata = parse_metadata(archive.read("fabric.mod.json"))
        rows = [{"path": location, **{key: metadata.get(key, default) for key, default in (
            ("id", None), ("name", metadata["id"]), ("version", None),
            ("depends", {}), ("provides", []), ("authors", []),
        )}}]
        for child in metadata.get("jars", []):
            name = child["file"]
            rows.extend(catalog(archive.read(name), location + "!/" + name))
        return rows


def signature(name):
    return name.upper().startswith("META-INF/") and name.upper().endswith((".SF", ".RSA", ".DSA", ".EC"))


def archived_signature(name):
    return "valenjeg/upstream-signatures/" + name


def relabel(raw, selected, private_variant=False):
    changes, replacements = [], {}
    with zipfile.ZipFile(io.BytesIO(raw)) as source:
        names = source.namelist()
        if "fabric.mod.json" not in names:
            return raw, changes
        original = parse_metadata(source.read("fabric.mod.json"))
        metadata = dict(original)
        if metadata["id"] in selected and "ValenJEG" not in metadata.get("name", ""):
            previous = metadata.get("name", metadata["id"])
            metadata["name"] = previous + " [ValenJEG Pack]"
            replacements["fabric.mod.json"] = (json.dumps(metadata, ensure_ascii=False, indent=2) + "\n").encode()
            changes.append({"id": metadata["id"], "version": metadata.get("version"),
                            "original_name": previous, "name": metadata["name"]})
        for child in metadata.get("jars", []):
            name = child["file"]
            data, nested = relabel(source.read(name), selected, private_variant)
            if nested:
                replacements[name] = data
                changes.extend({"nested": name, **row} for row in nested)
        if not replacements:
            return raw, changes
        signatures = [name for name in names if signature(name)]
        if signatures and not private_variant:
            raise ValueError(f"Preserve signed artifact; cannot relabel: {signatures}")
        if any(archived_signature(name) in names for name in signatures):
            raise ValueError("Upstream signature archive path already exists")
        if signatures:
            changes.append({"upstream_signatures_preserved_as_resources": signatures,
                            "candidate_signing": "unsigned private pack variant; original signed input retained unchanged"})
        output = io.BytesIO()
        with zipfile.ZipFile(output, "w") as target:
            target.comment = source.comment
            for entry in source.infolist():
                output_entry = copy.copy(entry)
                if signature(entry.filename):
                    output_entry.filename = archived_signature(entry.filename)
                target.writestr(output_entry, replacements.get(entry.filename, source.read(entry.filename)))
        result = output.getvalue()
    verify(raw, result)
    return result, changes


def verify(before, after):
    with zipfile.ZipFile(io.BytesIO(before)) as first, zipfile.ZipFile(io.BytesIO(after)) as second:
        moved = {name: archived_signature(name) for name in first.namelist()
                 if name not in second.namelist() and signature(name)}
        expected = [moved.get(name, name) for name in first.namelist()]
        if expected != second.namelist() or second.testzip() is not None:
            raise AssertionError("Archive entries changed or ZIP CRC failed")
        old = parse_metadata(first.read("fabric.mod.json")) if "fabric.mod.json" in first.namelist() else {}
        nested = {child["file"] for child in old.get("jars", [])}
        for name in first.namelist():
            original, candidate = first.read(name), second.read(moved.get(name, name))
            if original == candidate:
                continue
            if name == "fabric.mod.json":
                left, right = parse_metadata(original), parse_metadata(candidate)
                left.pop("name", None)
                right.pop("name", None)
                if left != right:
                    raise AssertionError("Metadata changed beyond the display name")
            elif name in nested:
                verify(original, candidate)
            else:
                raise AssertionError(f"Code, asset or other metadata bytes changed: {name}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, action="append", required=True)
    parser.add_argument("--scope", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--unsigned-private-variant", action="store_true",
                        help="Archive upstream signature material in derived copies; preserve original signed inputs")
    args = parser.parse_args()
    selected = set(json.loads(args.scope.read_text())["selected_ids"])
    if args.output.exists():
        raise FileExistsError("Use a new candidate directory; preserve previous artifacts")
    forbidden = [path.resolve() for path in args.input]
    if any(args.output.resolve() == path or args.output.resolve().is_relative_to(path) for path in forbidden):
        raise ValueError("Candidate output must be separate from installed input directories")
    args.output.mkdir(parents=True)
    report = {"scope_sha256": sha(args.scope.read_bytes()), "artifacts": [],
              "proof": "Only display names and private variant signature-resource locations changed; IDs, versions, dependencies, credits, licenses, code and assets preserved",
              "native_compatibility_proof": False, "installed": False}
    for directory in args.input:
        for path in sorted(directory.glob("*.jar")):
            raw = path.read_bytes()
            candidate, changes = relabel(raw, selected, args.unsigned_private_variant)
            if not changes:
                continue
            name = path.stem + "-valenjeg-label.jar"
            destination = args.output / directory.parent.name / name
            destination.parent.mkdir(exist_ok=True)
            if destination.exists():
                raise FileExistsError(destination)
            destination.write_bytes(candidate)
            report["artifacts"].append({"input": str(path), "input_sha256": sha(raw),
                                        "candidate": str(destination), "candidate_sha256": sha(candidate),
                                        "changes": changes, "allowed_changes_recursive_verification": "pass"})
    (args.output / "LABEL-ARTIFACTS.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    print(json.dumps({"candidate_jars": len(report["artifacts"]), "installed": False}))


if __name__ == "__main__":
    main()
