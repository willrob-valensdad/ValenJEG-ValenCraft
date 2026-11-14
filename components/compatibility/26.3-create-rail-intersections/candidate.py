#!/usr/bin/env python3
"""Compile a single-method Create candidate; preserve every unrelated ZIP payload."""
import argparse,os,hashlib,json,pathlib,runpy,subprocess,zipfile,shutil,sys
HERE=pathlib.Path(__file__).resolve().parent
ROOT=pathlib.Path("local-work")
JAVA=pathlib.Path("/usr/lib/jvm/java-25-openjdk-amd64/bin")
ENTRY="com/zurrtum/create/content/trains/graph/TrackGraph.class"
HELPER="com/zurrtum/create/content/trains/graph/ValenRailIntersections"
DESC="(Lnet/minecraft/world/level/LevelAccessor;Lcom/zurrtum/create/content/trains/graph/TrackNodeLocation$DiscoveredLocation;Lcom/zurrtum/create/content/trains/graph/TrackNodeLocation$DiscoveredLocation;Lcom/zurrtum/create/content/trains/track/BezierConnection;)V"
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def run(cmd,log,**kw):
 with log.open("w") as f:r=subprocess.run(["nice","-n","19","taskset","-c","24,25",*map(str,cmd)],stdout=f,stderr=subprocess.STDOUT,timeout=120,**kw)
 if r.returncode:raise RuntimeError("Inspect "+str(log))
def main():
 os.umask(0o077)
 p=argparse.ArgumentParser(description=__doc__);p.add_argument("--output",type=pathlib.Path,required=True)
 a=p.parse_args();out=a.output.resolve()
 if not out.is_relative_to(ROOT) or out.exists():p.error("new protected analysis output required")
 pins=json.loads((HERE/"source-inputs.json").read_text());baseline=pathlib.Path(pins["baseline_jar"]);source=pathlib.Path(pins["track_graph_source"])
 if sha(baseline)!=pins["baseline_sha256"] or sha(source)!=pins["track_graph_source_sha256"]:p.error("baseline drift")
 files=[HERE/n for n in ("candidate.py","TrackGraph.patch","ValenRailIntersections.java","source-inputs.json")]
 hashes={str(x):sha(x) for x in files}
 asm=pathlib.Path("local-inputs/asm")
 asm_jars=[asm/name/"9.10.1"/f"{name}-9.10.1.jar" for name in ("asm","asm-tree")]
 overlay=HERE.parent/"26.3-server-light-layers/ServerLayerOverlay.java"
 parts_source=HERE.parent/"26.3-client-light-queue/build.py"
 compiler_files=[JAVA/"java",JAVA/"javac",JAVA.parent/"lib/modules",JAVA.parent/"lib/libjava.so",
  JAVA.parent/"lib/server/libjvm.so",JAVA.parent/"lib/libjli.so"]
 compiler_files=[x for x in compiler_files if x.is_file()]
 immutable={str(x):sha(x) for x in [baseline,source,*files,overlay,parts_source,*asm_jars,*compiler_files,
  pathlib.Path(sys.executable).resolve(),pathlib.Path(shutil.which("patch")).resolve()]}
 out.mkdir(parents=True,mode=0o700)
 generated=out/"sources"/ENTRY.replace(".class",".java");generated.parent.mkdir(parents=True);generated.write_bytes(source.read_bytes())
 run(["patch","--batch","--fuzz=0","-p1","-i",HERE/"TrackGraph.patch"],out/"patch.log",cwd=out/"sources")
 pack=ROOT/"valenmultithreaderd-r1/pack-inputs-r1"
 for pin_file in (pack/"input-receipt.json",pack/"engine-receipt.json",ROOT/"server-rebuild-proof/build-receipt.json"):
  immutable[str(pin_file)]=sha(pin_file)
 inputs=json.loads((pack/"input-receipt.json").read_text())
 engine_pin=json.loads((pack/"engine-receipt.json").read_text())["artifacts"]["server"]
 engine=pathlib.Path(engine_pin["jar"])
 if sha(engine)!=engine_pin["sha256"]:p.error("engine drift")
 immutable[str(engine)]=engine_pin["sha256"]
 if {f.name for f in (pack/"server/mods").glob("*.jar")}!=set(inputs["mods"]["server"]):p.error("pack membership drift")
 libs=[engine,baseline]+[pathlib.Path(x) for x in json.loads((ROOT/"server-rebuild-proof/build-receipt.json").read_text())["runtime_libraries"]]
 libs += [ROOT/"neoform"/name for name in ("annotations-26.0.2.jar","jsr305-3.0.2.jar")]
 deps=out/"compile-deps";deps.mkdir()
 for mod in sorted((pack/"server/mods").glob("*.jar")):
  if sha(mod)!=inputs["mods"]["server"][mod.name]:p.error("pack mod drift")
  immutable[str(mod)]=inputs["mods"]["server"][mod.name]
  if mod!=baseline:libs.append(mod)
  with zipfile.ZipFile(mod) as z:
   for name in z.namelist():
    if name.startswith("META-INF/jars/") and name.endswith(".jar"):
     child=deps/(mod.stem+"-"+pathlib.Path(name).name);child.write_bytes(z.read(name));libs.append(child)
 for item in libs:
  pin=sha(item)
  if str(item) in immutable and immutable[str(item)]!=pin:p.error("input drift before compile "+str(item))
  immutable[str(item)]=pin
 before=out/"inputs-before.json";before.write_text(json.dumps(immutable,indent=2)+"\n")
 classes=out/"classes";classes.mkdir();empty=out/"empty";empty.mkdir()
 run([JAVA/"javac","-J-Xmx1G","-J-XX:ActiveProcessorCount=2","--release","25","-g","-proc:none","-implicit:none","-sourcepath",empty,
  "-cp",":".join(map(str,libs)),"-d",classes,generated,HERE/"ValenRailIntersections.java"],out/"compile.log")
 cp=":".join(map(str,asm_jars))
 tools=out/"tools";tools.mkdir()
 run([JAVA/"javac","-cp",cp,"-d",tools,overlay],out/"tool.log")
 with zipfile.ZipFile(baseline) as z:original=z.read(ENTRY)
 raw=out/"original.class";raw.write_bytes(original);patched=out/"TrackGraph.class"
 run([JAVA/"java","-cp",str(tools)+":"+cp,"ServerLayerOverlay",raw,classes/ENTRY,patched,"connectNodes"+DESC,""],out/"overlay.log")
 parts=runpy.run_path(str(parts_source))["class_parts"]
 old,new=parts(original),parts(patched.read_bytes())
 assert new["pool"].startswith(old["pool"])
 assert all(old[k]==new[k] for k in ("header","interfaces","fields"))
 assert set(old["methods"])==set(new["methods"])
 changed=("connectNodes",DESC)
 assert all(new["methods"][k]==v for k,v in old["methods"].items() if k!=changed)
 for key,value in dict(old["attributes"]).items():
  other=dict(new["attributes"])[key]
  if key in ("BootstrapMethods","InnerClasses"):assert other[8:].startswith(value[8:]),key
  else:assert other==value,key
 additions={str(x.relative_to(classes)):x.read_bytes() for x in classes.rglob("ValenRailIntersections*.class")}
 assert additions and all(n.startswith(HELPER) for n in additions)
 jar=out/"create-server-rail-intersections-candidate.jar"
 with zipfile.ZipFile(baseline) as src,zipfile.ZipFile(jar,"w") as dst:
  assert not set(additions)&set(src.namelist())
  for info in src.infolist():dst.writestr(info,patched.read_bytes() if info.filename==ENTRY else src.read(info.filename))
  for name,data in sorted(additions.items()):dst.writestr(name,data)
 with zipfile.ZipFile(baseline) as src,zipfile.ZipFile(jar) as dst:
  assert [n for n in src.namelist() if src.read(n)!=dst.read(n)]==[ENTRY]
  assert set(dst.namelist())==set(src.namelist())|set(additions)
 assert sha(baseline)==pins["baseline_sha256"] and sha(source)==pins["track_graph_source_sha256"]
 assert all(sha(k)==v for k,v in hashes.items())
 after={k:sha(k) for k in immutable}
 (out/"inputs-after.json").write_text(json.dumps(after,indent=2)+"\n")
 if after!=immutable:raise RuntimeError("Build input changed; candidate is unaccepted")
 receipt=dict(compiled=True,installed=False,candidate=str(jar),candidate_sha256=sha(jar),baseline=pins,
  source_sha256=hashes,changed_methods=["connectNodes"+DESC],untouched_raw_methods=len(old["methods"])-1,
  added_classes=sorted(additions),other_payloads_identical=True,libraries_sha256={str(x):immutable[str(x)] for x in libs},
  inputs_sha256_before=immutable,inputs_sha256_after=after,inputs_unchanged=True,
  engine_expected_sha256=engine_pin["sha256"],compiler_tools_before_after_pinned=True)
 (out/"candidate-receipt.json").write_text(json.dumps(receipt,indent=2)+"\n")
 print(json.dumps(dict(compiled=True,receipt=str(out/"candidate-receipt.json"),candidate=str(jar))))
if __name__=="__main__":main()
