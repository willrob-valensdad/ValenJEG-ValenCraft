#!/usr/bin/env python3
"""Build only a pinned disposable Create graph fixture/candidate; never deploy."""
import argparse,os,hashlib,json,pathlib,subprocess,zipfile
HERE=pathlib.Path(__file__).resolve().parent
ROOT=pathlib.Path("local-work")
RUNTIME=ROOT/"valenmultithreaderd-r1"
JAVA=pathlib.Path("/usr/lib/jvm/java-25-openjdk-amd64/bin")
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def main():
 os.umask(0o077)
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument("--output",type=pathlib.Path,required=True)
 p.add_argument("--create-jar",type=pathlib.Path)
 a=p.parse_args();out=a.output.resolve()
 if not out.is_relative_to(ROOT) or out.exists():p.error("new protected analysis output required")
 pins=json.loads((HERE/"source-inputs.json").read_text())
 baseline=pathlib.Path(pins["baseline_jar"]);create=(a.create_jar or baseline).resolve()
 if sha(baseline)!=pins["baseline_sha256"]:p.error("baseline Create drift")
 source=pathlib.Path(pins["track_graph_source"])
 if sha(source)!=pins["track_graph_source_sha256"]:p.error("author source drift")
 pack=RUNTIME/"pack-inputs-r1";inputs=json.loads((pack/"input-receipt.json").read_text())
 engine_pin=json.loads((pack/"engine-receipt.json").read_text())["artifacts"]["server"]
 engine=pathlib.Path(engine_pin["jar"])
 if sha(engine)!=engine_pin["sha256"]:p.error("engine drift")
 for name,pin in inputs["mods"]["server"].items():
  if sha(pack/"server/mods"/name)!=pin:p.error("mod drift "+name)
 out.mkdir(parents=True,mode=0o700);deps=out/"compile-deps";deps.mkdir()
 runtime=[pathlib.Path(x) for x in json.loads((ROOT/"server-rebuild-proof/build-receipt.json").read_text())["runtime_libraries"]]
 loader=[pathlib.Path(x) for x in (ROOT/"friends-pack-stage-r1/loader/fabric-classpath.txt").read_text().strip().split(":")]
 libraries=[engine,create,*runtime,*loader]
 for mod in sorted((pack/"server/mods").glob("*.jar")):
  if mod.name!=baseline.name:libraries.append(mod)
  with zipfile.ZipFile(mod) as z:
   for name in z.namelist():
    if name.startswith("META-INF/jars/") and name.endswith(".jar"):
     child=deps/(mod.stem+"-"+pathlib.Path(name).name)
     child.write_bytes(z.read(name));libraries.append(child)
 classes=out/"classes";classes.mkdir();empty=out/"empty";empty.mkdir()
 sources=[HERE/n for n in ("RailFixture.java","fabric.mod.json","build.py","run.py","README.md","source-inputs.json")]
 hashes={str(x):sha(x) for x in sources}
 args=["--release","25","-g","-proc:none","-implicit:none","-sourcepath",str(empty),"-classpath",":".join(map(str,libraries)),"-d",str(classes),str(HERE/"RailFixture.java")]
 argfile=out/"javac.args";argfile.write_text("\n".join('"'+x.replace("\\","\\\\").replace('"','\\"')+'"' for x in args)+"\n")
 cmd=["nice","-n","19","taskset","-c","24,25",str(JAVA/"javac"),"-J-Xmx512m","-J-XX:ActiveProcessorCount=2","@"+str(argfile)]
 with (out/"compile.log").open("w") as log:r=subprocess.run(cmd,stdout=log,stderr=subprocess.STDOUT,timeout=120)
 if r.returncode:raise RuntimeError("compile failed: "+str(out/"compile.log"))
 fixture=out/"valen-rail-fixture.jar"
 with zipfile.ZipFile(fixture,"w",zipfile.ZIP_DEFLATED) as z:
  z.write(HERE/"fabric.mod.json","fabric.mod.json")
  for f in sorted(classes.rglob("*.class")):z.write(f,str(f.relative_to(classes)))
 if any(sha(k)!=v for k,v in hashes.items()):raise RuntimeError("fixture source changed")
 receipt=dict(compiled=True,world_launched=False,production_installed=False,fixture=str(fixture),fixture_sha256=sha(fixture),
  create=str(create),create_sha256=sha(create),baseline_create=str(baseline),baseline_create_sha256=sha(baseline),
  engine=str(engine),engine_sha256=sha(engine),pack=str(pack),pack_receipt_sha256=sha(pack/"input-receipt.json"),
  source_sha256=hashes,classpath=[str(engine),*map(str,runtime),*map(str,loader)],
  libraries_sha256={str(x):sha(x) for x in [*runtime,*loader]},command=cmd,compile_log_sha256=sha(out/"compile.log"))
 (out/"build-receipt.json").write_text(json.dumps(receipt,indent=2)+"\n")
 print(json.dumps(dict(compiled=True,receipt=str(out/"build-receipt.json"))))
if __name__=="__main__":main()
