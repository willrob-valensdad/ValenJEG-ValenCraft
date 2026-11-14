#!/usr/bin/env python3
"""Prepare current-format structure resources offline with the native Minecraft data fixer."""
import argparse, hashlib, json, subprocess
from pathlib import Path

p = argparse.ArgumentParser()
p.add_argument("--classpath-file", type=Path, required=True)
p.add_argument("--output-dir", type=Path, required=True)
p.add_argument("--java-home", type=Path, default=Path("/usr/lib/jvm/java-25-openjdk-amd64"))
p.add_argument("inputs", nargs="+", type=Path)
args = p.parse_args()
args.output_dir.mkdir(parents=True, exist_ok=False)
classes = args.output_dir / "classes"
classes.mkdir()
source = Path(__file__).with_name("UpgradeTemplates.java")
cp = args.classpath_file.read_text().strip()
compile_cmd = [str(args.java_home / "bin/javac"), "--release", "25", "-cp", cp, "-d", str(classes), str(source)]
result = subprocess.run(compile_cmd, capture_output=True, text=True)
(args.output_dir / "compile.log").write_text(result.stdout + result.stderr)
if result.returncode:
    raise SystemExit(result.returncode)
command = ["nice", "-n", "15", str(args.java_home / "bin/java"), "-Xmx4G", "-XX:ActiveProcessorCount=2",
           "-cp", str(classes) + ":" + cp, "UpgradeTemplates", str(args.output_dir / "output"),
           *map(str, args.inputs)]
sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
(args.output_dir / "invocation.json").write_text(json.dumps({
    "command": command, "source_sha256": sha(source),
    "inputs": [{"path": str(x), "sha256": sha(x)} for x in args.inputs]
}, indent=2) + "\n")
with (args.output_dir / "conversion.log").open("w") as log:
    result = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT)
raise SystemExit(result.returncode)
