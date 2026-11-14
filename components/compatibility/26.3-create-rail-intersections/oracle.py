#!/usr/bin/env python3
"""Build distinct intrusive rail oracle artifacts; never use these for timing."""
import argparse,os,hashlib,json,pathlib,subprocess,zipfile
HERE=pathlib.Path(__file__).resolve().parent
ROOT=pathlib.Path("local-work")
JAVA=pathlib.Path("/usr/lib/jvm/java-25-openjdk-amd64/bin")
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def run(cmd,path):
 with path.open("w") as f:r=subprocess.run(["nice","-n","19","taskset","-c","24,25",*map(str,cmd)],stdout=f,stderr=subprocess.STDOUT,timeout=120)
 if r.returncode:raise RuntimeError("Inspect "+str(path))
def main():
 os.umask(0o077)
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument("--build-receipt",type=pathlib.Path,required=True)
 p.add_argument("--output",type=pathlib.Path,required=True)
 a=p.parse_args();out=a.output.resolve()
 if not out.is_relative_to(ROOT) or out.exists():p.error("new analysis directory required")
 b=json.loads(a.build_receipt.read_text())
 if not b.get("test_only_original_alias"):p.error("paired fixture with exact original alias required")
 for name,pin in {**b["source_sha256"],**b["libraries_sha256"],b["create"]:b["create_sha256"]}.items():
  if sha(name)!=pin:p.error("input drift "+name)
 tracked={str(a.build_receipt.resolve()):sha(a.build_receipt),b["create"]:b["create_sha256"],**b["source_sha256"]}
 for name in ("oracle.py","RailOracle.java","RailObserve.java","RailObserveHooks.java"):tracked[str(HERE/name)]=sha(HERE/name)
 out.mkdir(parents=True,mode=0o700);classes=out/"classes";classes.mkdir();tools=out/"tools";tools.mkdir()
 asm=pathlib.Path("local-inputs/asm")
 cp=":".join(str(asm/name/"9.10.1"/f"{name}-9.10.1.jar") for name in ("asm","asm-tree"))
 run([JAVA/"javac","-J-XX:ActiveProcessorCount=2","-cp",cp,"-d",tools,HERE/"RailObserve.java"],out/"observe-compile.log")
 observed=out/"create-observed.jar"
 run([JAVA/"java","-XX:ActiveProcessorCount=2","-cp",str(tools)+":"+cp,"RailObserve",b["create"],observed],out/"observe.log")
 pack=pathlib.Path(b["pack"]);deps=pathlib.Path(b.get("compile_dependency_dir",str(a.build_receipt.parent/"compile-deps")))
 libs=[pathlib.Path(b["engine"]),observed,*map(pathlib.Path,b["classpath"])]
 libs += [x for x in sorted((pack/"server/mods").glob("*.jar")) if x.name!=pathlib.Path(b["baseline_create"]).name]
 libs += sorted(deps.glob("*.jar"))
 empty=out/"empty";empty.mkdir()
 run([JAVA/"javac","-J-Xmx512m","-J-XX:ActiveProcessorCount=2","--release","25","-g","-proc:none","-implicit:none","-sourcepath",empty,
  "-cp",":".join(map(str,libs)),"-d",classes,*[HERE/n for n in ("RailFixture.java","RailOracle.java","RailObserveHooks.java")]],out/"compile.log")
 fixture=out/"valen-rail-oracle.jar"
 metadata=json.loads((HERE/"fabric.mod.json").read_text());metadata["entrypoints"]["main"]=["com.zurrtum.create.content.trains.graph.RailOracle"]
 metadata["name"]="Valen rail correctness oracle"
 with zipfile.ZipFile(fixture,"w",zipfile.ZIP_DEFLATED) as z:
  z.writestr("fabric.mod.json",json.dumps(metadata,indent=2)+"\n")
  for f in sorted(classes.rglob("*.class")):z.write(f,str(f.relative_to(classes)))
 for name,pin in tracked.items():
  if sha(name)!=pin:raise RuntimeError("input changed "+name)
 b.update(compiled=True,intrusive_correctness_only=True,fixture=str(fixture),fixture_sha256=sha(fixture),
  create=str(observed),create_sha256=sha(observed),source_sha256=tracked,unobserved_create=b["create"],unobserved_create_sha256=b["create_sha256"])
 receipt=out/"build-receipt.json";receipt.write_text(json.dumps(b,indent=2)+"\n")
 print(json.dumps(dict(compiled=True,receipt=str(receipt))))
if __name__=="__main__":main()
