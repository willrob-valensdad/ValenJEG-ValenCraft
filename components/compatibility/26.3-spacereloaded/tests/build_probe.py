#!/usr/bin/env python3
"""Build only the disposable SpaceReloaded registry and optional REI contract."""
import argparse, hashlib, json, subprocess, zipfile
from pathlib import Path

p = argparse.ArgumentParser(description=__doc__)
p.add_argument("--rei-build", type=Path, required=True)
p.add_argument("--output", type=Path, required=True)
p.add_argument("--gravity-build", type=Path)
p.add_argument("--oxygen", action="store_true", help="Build native cross-mod oxygen fixture")
p.add_argument("--gameplay", action="store_true", help="Include native terrain/oxygen/rocket flight with transfer fixture")
p.add_argument("--transfers", action="store_true", help="Build the separate native energy/fluid fixture")
a = p.parse_args()
if a.oxygen and (a.gameplay or a.transfers):
    p.error("--oxygen is separate from --transfers/--gameplay")
if a.gameplay and not a.transfers:
    p.error("--gameplay requires --transfers")
a.output.mkdir(exist_ok=False)
classes = a.output / "classes"
classes.mkdir()
receipt = json.loads((a.rei_build / "build-receipt.json").read_text())
recipe_jar = Path(receipt["jar"])
assert hashlib.sha256(recipe_jar.read_bytes()).hexdigest() == receipt["sha256"]
cp = json.loads((a.rei_build / "classpath.json").read_text()) + [str(recipe_jar)]
source = Path(__file__).with_name("SpaceOxygenProbe.java" if a.oxygen else "SpaceTransferProbe.java" if a.transfers else "SpaceReloadedProbe.java")
sources = [source]
if a.gravity_build:
    gravity = json.loads((a.gravity_build / "build-receipt.json").read_text())
    gravity_jar = Path(gravity["jar"])
    assert hashlib.sha256(gravity_jar.read_bytes()).hexdigest() == gravity["sha256"]
    space_inputs = [(Path(name), digest) for name, digest in gravity["compile_inputs_sha256"].items()
                    if Path(name).name.startswith("spacereloaded-")]
    assert len(space_inputs) == 1
    space_jar, space_sha = space_inputs[0]
    assert hashlib.sha256(space_jar.read_bytes()).hexdigest() == space_sha
    cp = [str(space_jar)] + [name for name in cp if not Path(name).name.startswith("spacereloaded-")]
    cp.append(str(gravity_jar))
    if not a.transfers or a.gameplay:
        sources.append(source.with_name("SpaceGravityProbe.java"))
    if a.gameplay:
        sources.append(source.with_name("SpaceWorldProbe.java"))
else:
    raise SystemExit("--gravity-build is required by the current optional gravity contract")
command = ["nice", "-n", "15", "/usr/lib/jvm/java-25-openjdk-amd64/bin/javac",
           "-J-Xmx1024M", "-J-XX:ActiveProcessorCount=4", "-proc:none", "-implicit:none",
           "-cp", ":".join(cp), "-d", str(classes), *map(str, sources)]
result = subprocess.run(command, capture_output=True, text=True)
(a.output / "compile.log").write_text(result.stdout + result.stderr)
if result.returncode:
    raise SystemExit(result.stderr)
metadata = {"schemaVersion": 1, "id": "valen_spacereloaded_probe", "version": "0.1.0",
            "environment": "server", "entrypoints": {"main": ["valen.spacereloadedprobe." + ("SpaceOxygenProbe" if a.oxygen else "SpaceTransferProbe" if a.transfers else "SpaceReloadedProbe")]},
            "depends": {"fabricloader": ">=0.20.1", "fabric-api": "*", "valen_nerospace_rei": "*"}}
if a.gameplay:
    metadata["entrypoints"]["main"].append("valen.spacereloadedprobe.SpaceWorldProbe")
if a.transfers or a.oxygen:
    metadata["depends"]["spacereloaded"] = "*"
jar = a.output / "valen-spacereloaded-probe.jar"
with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as archive:
    archive.writestr("fabric.mod.json", json.dumps(metadata))
    for path in sorted(classes.rglob("*.class")):
        archive.write(path, str(path.relative_to(classes)))
(a.output / "build-receipt.json").write_text(json.dumps({
    "jar": str(jar), "sha256": hashlib.sha256(jar.read_bytes()).hexdigest(),
    "source_sha256": hashlib.sha256(source.read_bytes()).hexdigest(),
    "sources_sha256": {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in sources},
    "recipe_companion_sha256": receipt["sha256"], "runtime_acceptance": "pending"
}, indent=2) + "\n")
print(jar)
