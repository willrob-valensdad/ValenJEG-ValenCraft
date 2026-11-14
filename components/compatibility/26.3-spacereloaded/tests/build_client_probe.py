#!/usr/bin/env python3
"""Build the isolated SpaceReloaded client observer; no production payload edits."""
import argparse,hashlib,json,subprocess,zipfile
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument("--launch-args",type=Path,required=True)
p.add_argument("--mods",type=Path,required=True)
p.add_argument("--output",type=Path,required=True)
a=p.parse_args();a.output.mkdir(exist_ok=False);classes=a.output/"classes";classes.mkdir()
args=json.loads(a.launch_args.read_text());cp=args[args.index("-cp")+1].split(":")
nested=a.output/"nested";nested.mkdir();jars=sorted(a.mods.glob("*.jar"))
for jar in list(jars):
 with zipfile.ZipFile(jar) as z:
  m=json.loads(z.read("fabric.mod.json"),strict=False)
  for child in m.get("jars",[]):
   name=child["file"];data=z.read(name);out=nested/(hashlib.sha256(data).hexdigest()+".jar")
   if not out.exists():out.write_bytes(data)
jars+=sorted(nested.glob("*.jar"));cp+=list(map(str,jars))
source=Path(__file__).with_name("SpaceClientProbe.java")
result=subprocess.run(["nice","-n","15","/usr/lib/jvm/java-25-openjdk-amd64/bin/javac","-J-Xmx1G","-J-XX:ActiveProcessorCount=2","-proc:none","-implicit:none","-cp",":".join(cp),"-d",str(classes),str(source)],capture_output=True,text=True)
(a.output/"compile.log").write_text(result.stdout+result.stderr)
if result.returncode:raise SystemExit(result.stderr)
jar=a.output/"valen-space-client-probe.jar"
with zipfile.ZipFile(jar,"w",zipfile.ZIP_DEFLATED) as z:
 z.writestr("fabric.mod.json",json.dumps({"schemaVersion":1,"id":"valen_space_client_probe","version":"0.1.0","environment":"client","entrypoints":{"client":["valen.spacereloadedprobe.SpaceClientProbe"]},"depends":{"fabricloader":">=0.20.1","fabric-api":"*","spacereloaded":"*","valen_nerospace_rei":"*","iris":"*"}}))
 for f in sorted(classes.rglob("*.class")):z.write(f,str(f.relative_to(classes)))
(a.output/"build-receipt.json").write_text(json.dumps({"jar":str(jar),"sha256":hashlib.sha256(jar.read_bytes()).hexdigest(),"source_sha256":hashlib.sha256(source.read_bytes()).hexdigest(),"compile_inputs_sha256":{str(x):hashlib.sha256(x.read_bytes()).hexdigest() for x in jars},"runtime_acceptance":"pending"},indent=2)+"\n")
print(jar)
