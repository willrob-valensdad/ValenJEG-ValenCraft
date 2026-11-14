#!/usr/bin/env python3
"""Add an exact raw original-method alias to a TEST-ONLY candidate JAR."""
import argparse,os,hashlib,json,pathlib,runpy,struct,subprocess,zipfile
HERE=pathlib.Path(__file__).resolve().parent
ROOT=pathlib.Path("local-work")
JAVA=pathlib.Path("/usr/lib/jvm/java-25-openjdk-amd64/bin")
ENTRY="com/zurrtum/create/content/trains/graph/TrackGraph.class"
ALIAS="valen$connectNodesOriginal"
DESC="(Lnet/minecraft/world/level/LevelAccessor;Lcom/zurrtum/create/content/trains/graph/TrackNodeLocation$DiscoveredLocation;Lcom/zurrtum/create/content/trains/graph/TrackNodeLocation$DiscoveredLocation;Lcom/zurrtum/create/content/trains/track/BezierConnection;)V"
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def digest(data):return hashlib.sha256(data).hexdigest()
def u2(value):return struct.pack(">H",value)
def main():
 os.umask(0o077)
 p=argparse.ArgumentParser(description=__doc__);p.add_argument("--build-receipt",type=pathlib.Path,required=True);p.add_argument("--candidate-receipt",type=pathlib.Path,required=True);p.add_argument("--output",type=pathlib.Path,required=True)
 a=p.parse_args();out=a.output.resolve()
 if out.exists() or not out.is_relative_to(ROOT):p.error("new protected analysis output required")
 b=json.loads(a.build_receipt.read_text());c=json.loads(a.candidate_receipt.read_text())
 assert b["create"]==c["candidate"] and b["create_sha256"]==c["candidate_sha256"] and c["inputs_unchanged"]
 parts_path=HERE.parent/"26.3-client-light-queue/build.py"
 tracked={str(a.build_receipt.resolve()):sha(a.build_receipt),str(a.candidate_receipt.resolve()):sha(a.candidate_receipt),
  **b["source_sha256"],**c["source_sha256"],**b["libraries_sha256"],b["create"]:b["create_sha256"],
  b["fixture"]:b["fixture_sha256"],b["baseline_create"]:b["baseline_create_sha256"],str(parts_path):sha(parts_path)}
 for n in ("paired.py","RailPairedFixture.java"):tracked[str(HERE/n)]=sha(HERE/n)
 for f,pin in tracked.items():
  if sha(f)!=pin:p.error("input drift "+f)
 out.mkdir(parents=True,mode=0o700)
 parts=runpy.run_path(str(parts_path))["class_parts"]
 with zipfile.ZipFile(b["baseline_create"]) as z:original=z.read(ENTRY)
 with zipfile.ZipFile(b["create"]) as z:current=z.read(ENTRY)
 old,base=parts(original),parts(current);assert base["pool"].startswith(old["pool"])
 method=old["methods"][("connectNodes",DESC)];count=struct.unpack_from(">H",current,8)[0]
 assert count<65535 and (ALIAS,DESC) not in base["methods"]
 alias=method[:2]+u2(count)+method[4:]
 encoded=ALIAS.encode("utf-8");pool=base["pool"]+b"\x01"+u2(len(encoded))+encoded
 result=(current[:8]+u2(count+1)+pool+base["header"]+u2(len(base["interfaces"])//2)+base["interfaces"]
  +u2(len(base["fields"]))+b"".join(base["fields"].values())
  +u2(len(base["methods"])+1)+b"".join(base["methods"].values())+alias+current[base["attributes_start"]:])
 new=parts(result)
 assert all(new["methods"][key]==value for key,value in base["methods"].items())
 assert new["methods"][(ALIAS,DESC)][:2]+method[2:4]+new["methods"][(ALIAS,DESC)][4:]==method
 assert all(base[key]==new[key] for key in ("header","interfaces","fields","attributes"))
 testjar=out/"create-paired-fixture-only.jar"
 with zipfile.ZipFile(b["create"]) as src,zipfile.ZipFile(testjar,"w") as dst:
  for info in src.infolist():dst.writestr(info,result if info.filename==ENTRY else src.read(info.filename))
 with zipfile.ZipFile(b["create"]) as src,zipfile.ZipFile(testjar) as dst:
  assert src.namelist()==dst.namelist()
  assert [n for n in src.namelist() if src.read(n)!=dst.read(n)]==[ENTRY]
 classes=out/"classes";classes.mkdir()
 libs=[testjar,pathlib.Path(b["fixture"]),*map(pathlib.Path,b["classpath"])]
 libs += [x for x in sorted((pathlib.Path(b["pack"])/"server/mods").glob("*.jar")) if x.name!=pathlib.Path(b["baseline_create"]).name]
 libs += sorted((a.build_receipt.parent/"compile-deps").glob("*.jar"))
 for f in [*libs,JAVA/"javac",JAVA.parent/"lib/modules"]:tracked[str(f)]=sha(f)
 (out/"inputs-before.json").write_text(json.dumps(tracked,indent=2)+"\n")
 cmd=["nice","-n","19","taskset","-c","24,25",str(JAVA/"javac"),"-J-Xmx512m","-J-XX:ActiveProcessorCount=2","--release","25","-g","-proc:none",
  "-implicit:none","-cp",":".join(map(str,libs)),"-d",str(classes),str(HERE/"RailPairedFixture.java")]
 with (out/"compile.log").open("w") as log:r=subprocess.run(cmd,stdout=log,stderr=subprocess.STDOUT,timeout=120)
 if r.returncode:raise RuntimeError("compile failed: "+str(out/"compile.log"))
 fixture=out/"valen-rail-paired-fixture.jar"
 with zipfile.ZipFile(b["fixture"]) as src,zipfile.ZipFile(fixture,"w") as dst:
  for info in src.infolist():
   data=src.read(info.filename)
   if info.filename=="fabric.mod.json":
    meta=json.loads(data);meta["entrypoints"]["main"]=["com.zurrtum.create.content.trains.graph.RailPairedFixture"]
    meta["name"]="Valen rail paired TEST-ONLY fixture";data=(json.dumps(meta,indent=2)+"\n").encode()
   dst.writestr(info,data)
  for f in sorted(classes.rglob("*.class")):dst.write(f,str(f.relative_to(classes)))
 assert all(sha(f)==pin for f,pin in tracked.items())
 (out/"inputs-after.json").write_text(json.dumps({f:sha(f) for f in tracked},indent=2)+"\n")
 b.update(create=str(testjar),create_sha256=sha(testjar),fixture=str(fixture),fixture_sha256=sha(fixture),source_sha256=tracked,
  test_only_original_alias=True,compile_dependency_dir=str(a.build_receipt.parent/"compile-deps"),production_candidate=c["candidate"],production_candidate_sha256=c["candidate_sha256"],
  original_raw_method_sha256=digest(method),alias_raw_method_sha256=digest(alias),only_alias_name_index_changed=True,
  all_existing_candidate_methods_identical=True,original_method_alias=ALIAS,global_full_operation_warmup_per_runner=40)
 (out/"build-receipt.json").write_text(json.dumps(b,indent=2)+"\n")
 print(json.dumps(dict(compiled=True,test_only=True,receipt=str(out/"build-receipt.json"))))
if __name__=="__main__":main()
