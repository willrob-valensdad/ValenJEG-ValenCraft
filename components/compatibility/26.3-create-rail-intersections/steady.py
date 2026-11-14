#!/usr/bin/env python3
"""Build a separate warmed timing entrypoint without changing candidate code."""
import argparse,hashlib,json,os,pathlib,subprocess,zipfile
HERE=pathlib.Path(__file__).resolve().parent
ROOT=pathlib.Path("local-work")
JAVA=pathlib.Path("/usr/lib/jvm/java-25-openjdk-amd64/bin")
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def main():
 os.umask(0o077)
 p=argparse.ArgumentParser(description=__doc__);p.add_argument("--build-receipt",type=pathlib.Path,required=True);p.add_argument("--output",type=pathlib.Path,required=True)
 a=p.parse_args();out=a.output.resolve()
 if out.exists() or not out.is_relative_to(ROOT):p.error("new protected analysis output required")
 b=json.loads(a.build_receipt.read_text());tracked={str(a.build_receipt.resolve()):sha(a.build_receipt),b["fixture"]:b["fixture_sha256"],**b["source_sha256"]}
 for n in ("RailSteadyFixture.java","steady.py"):tracked[str(HERE/n)]=sha(HERE/n)
 for f,pin in tracked.items():
  if sha(f)!=pin:p.error("input drift "+f)
 out.mkdir(parents=True,mode=0o700);classes=out/"classes";classes.mkdir()
 libs=[pathlib.Path(b["fixture"]),pathlib.Path(b["create"]),*map(pathlib.Path,b["classpath"])]
 libs += [x for x in sorted((pathlib.Path(b["pack"])/"server/mods").glob("*.jar")) if x.name!=pathlib.Path(b["baseline_create"]).name]
 libs += sorted((a.build_receipt.parent/"compile-deps").glob("*.jar"))
 cmd=["nice","-n","19","taskset","-c","24,25",str(JAVA/"javac"),"-J-Xmx512m","-J-XX:ActiveProcessorCount=2","--release","25","-g","-proc:none",
  "-implicit:none","-cp",":".join(map(str,libs)),"-d",str(classes),str(HERE/"RailSteadyFixture.java")]
 with (out/"compile.log").open("w") as log:r=subprocess.run(cmd,stdout=log,stderr=subprocess.STDOUT,timeout=120)
 if r.returncode:raise RuntimeError("compile failed")
 fixture=out/"valen-rail-steady-fixture.jar"
 with zipfile.ZipFile(b["fixture"]) as src,zipfile.ZipFile(fixture,"w") as dst:
  for info in src.infolist():
   data=src.read(info.filename)
   if info.filename=="fabric.mod.json":
    meta=json.loads(data);meta["entrypoints"]["main"]=["com.zurrtum.create.content.trains.graph.RailSteadyFixture"]
    meta["name"]="Valen rail steady-state fixture";data=(json.dumps(meta,indent=2)+"\n").encode()
   dst.writestr(info,data)
  for f in sorted(classes.rglob("*.class")):dst.write(f,str(f.relative_to(classes)))
 assert all(sha(f)==pin for f,pin in tracked.items())
 b.update(fixture=str(fixture),fixture_sha256=sha(fixture),source_sha256=tracked,global_full_operation_warmup=40,
  global_warmup_shape="fresh crossing32, cold geometry",startup_comparison_separate=True)
 (out/"build-receipt.json").write_text(json.dumps(b,indent=2)+"\n")
 print(json.dumps(dict(compiled=True,receipt=str(out/"build-receipt.json"))))
if __name__=="__main__":main()
