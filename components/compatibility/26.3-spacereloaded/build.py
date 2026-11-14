#!/usr/bin/env python3
"""Replay the pinned SpaceReloaded source port; build only, never install or launch."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import subprocess
import tarfile
import urllib.request

HERE = Path(__file__).resolve().parent
DEFAULT_ROOT = Path("local-space-work")
DEFAULT_CACHE = Path("local-inputs/gradle-cache")
JAVA_HOME = "/usr/lib/jvm/java-25-openjdk-amd64"

def sha(data):
    return hashlib.sha256(data).hexdigest()

def source_files(source, archive):
    with tarfile.open(archive) as package:
        names = {member.name.split("/", 1)[1] for member in package if member.isfile()}
    for part in ("core/src", "mod/src"):
        names.update(str(path.relative_to(source)) for path in (source / part).rglob("*") if path.is_file())
    return {name: sha((source / name).read_bytes()) for name in sorted(names) if (source / name).is_file()}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--work-root", type=Path, default=DEFAULT_ROOT)
    parser.add_argument("--gradle-cache", type=Path, default=DEFAULT_CACHE)
    parser.add_argument("--prepare-only", action="store_true")
    args = parser.parse_args()
    checkpoint = json.loads((HERE / "source-checkpoint.json").read_text())
    upstream = checkpoint["upstream"]
    root = args.work_root.resolve()
    root.mkdir(parents=True, exist_ok=True)
    archive = root / "author-source.tar.gz"
    if not archive.exists():
        with urllib.request.urlopen(upstream["archive_url"], timeout=60) as response:
            archive.write_bytes(response.read())
    if sha(archive.read_bytes()) != upstream["archive_sha256"]:
        raise SystemExit("Author archive hash mismatch.")
    patch = HERE / "26.3-port.patch"
    if sha(patch.read_bytes()) != checkpoint["patch_sha256"]:
        raise SystemExit("Patch differs from the source checkpoint.")
    source = root / "source"
    if not source.exists():
        source.mkdir()
        with tarfile.open(archive) as package:
            for member in package:
                parts = Path(member.name).parts[1:]
                if not parts:
                    continue
                if ".." in parts or member.issym() or member.islnk():
                    raise SystemExit("Unexpected author archive path or link.")
                target = source.joinpath(*parts)
                if member.isdir():
                    target.mkdir(parents=True, exist_ok=True)
                elif member.isfile():
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(package.extractfile(member).read())
                    target.chmod(member.mode & 0o777)
        with patch.open("rb") as changes, (root / "patch-apply.log").open("wb") as log:
            subprocess.run(["patch", "--batch", "--forward", "-p1"], cwd=source,
                           stdin=changes, stdout=log, stderr=subprocess.STDOUT, check=True)
    files = source_files(source, archive)
    digest = sha(json.dumps(files, sort_keys=True, separators=(",", ":")).encode())
    if digest != checkpoint["source_tree_sha256"]:
        raise SystemExit("Working source differs from the checkpoint; preserve it and inspect before building.")
    if args.prepare_only:
        print(json.dumps({"source": str(source), "source_tree_sha256": digest}))
        return
    env = dict(os.environ, JAVA_HOME=JAVA_HOME, JAVA_TOOL_OPTIONS="-XX:ActiveProcessorCount=4")
    command = ["nice", "-n", "15", "sh", "./gradlew", "--no-daemon", "--max-workers=4",
               "--gradle-user-home", str(args.gradle_cache), "-Dorg.gradle.parallel=false",
               "-Dorg.gradle.jvmargs=-Xmx1536M -XX:ActiveProcessorCount=4",
               ":core:test", ":mod:compileJava", ":mod:compileClientJava", ":mod:jar", ":mod:sourcesJar"]
    with (root / "checkpoint-build.log").open("wb") as log:
        result = subprocess.run(command, cwd=source, env=env, stdout=log, stderr=subprocess.STDOUT)
    artifacts = [{"path": str(path), "sha256": sha(path.read_bytes())}
                 for path in sorted((source / "mod/build/libs").glob("*.jar"))] if result.returncode == 0 else []
    receipt = {"status": "build only; runtime acceptance pending", "exit_code": result.returncode,
               "source_tree_sha256": digest, "patch_sha256": checkpoint["patch_sha256"],
               "command": command, "artifacts": artifacts}
    (root / "checkpoint-build-receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(json.dumps(receipt))
    raise SystemExit(result.returncode)

if __name__ == "__main__":
    main()
