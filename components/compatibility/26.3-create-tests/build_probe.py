#!/usr/bin/env python3
"""Build a disposable Fabric content-contract fixture; never install it in a live world."""
import argparse, hashlib, json, subprocess, zipfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument("--classpath-file",type=Path,required=True)
p.add_argument("--output-dir",type=Path,required=True)
a=p.parse_args()
source=Path(__file__).resolve().parent
a.output_dir.mkdir(parents=True,exist_ok=False)
classes=a.output_dir/"classes";classes.mkdir()
cp=a.classpath_file.read_text().strip()
sources=sorted(source.glob("*.java"))
command=["javac","--release","25","-g","-proc:none","-implicit:none","-classpath",cp,"-d",str(classes),*[str(s) for s in sources]]
result=subprocess.run(command,text=True,capture_output=True)
(a.output_dir/"compile.log").write_text(result.stdout+result.stderr)
(a.output_dir/"javac-command.json").write_text(json.dumps(command,indent=2)+"\n")
if result.returncode: raise SystemExit(result.returncode)
jar=a.output_dir/"valen-create-probe.jar"
with zipfile.ZipFile(jar,"w",zipfile.ZIP_DEFLATED) as z:
 z.write(source/"fabric.mod.json","fabric.mod.json")
 for file in sorted(classes.rglob("*.class")):z.write(file,str(file.relative_to(classes)))
 for file in sorted((source/"data").rglob("*")):
  if file.is_file():z.write(file,str(file.relative_to(source)))
sha=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
receipt={"compile_pass":True,"runtime_tested":False,"fixture_jar":str(jar),"sha256":sha(jar),"source_hashes":{s.name:sha(s) for s in sources},"resource_hashes":{str(p.relative_to(source)):sha(p) for p in [source/"fabric.mod.json",*sorted((source/"data").rglob("*.json"))]}}
(a.output_dir/"build-receipt.json").write_text(json.dumps(receipt,indent=2)+"\n")
print(json.dumps(receipt))
