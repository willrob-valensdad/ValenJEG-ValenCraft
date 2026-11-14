#!/usr/bin/env python3
"""Launch only an explicitly requested, bounded disposable rail fixture."""
import argparse,hashlib,json,os,pathlib,re,shutil,signal,socket,subprocess,time,zipfile
ROOT=pathlib.Path("local-work")
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def main():
 os.umask(0o077)
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument("--build-receipt",type=pathlib.Path,required=True)
 p.add_argument("--runtime-receipt",type=pathlib.Path,required=True)
 p.add_argument("--output",type=pathlib.Path,required=True)
 p.add_argument("--parallelism",type=int,choices=(1,2,4,8),required=True)
 p.add_argument("--cpu-list",default="28-35")
 p.add_argument("--port",type=int,default=25616)
 p.add_argument("--timeout",type=int,default=300)
 p.add_argument("--warmup",type=int,default=3);p.add_argument("--samples",type=int,default=7)
 p.add_argument("--reverse",action="store_true")
 p.add_argument("--execute",action="store_true")
 a=p.parse_args();out=a.output.resolve()
 if not out.is_relative_to(ROOT) or out.exists():p.error("new protected analysis output required")
 if not a.execute:p.error("--execute required")
 if not 1<=a.warmup<=8 or not 3<=a.samples<=15 or not 60<=a.timeout<=600:p.error("bounds")
 if not re.fullmatch(r"[0-9]+(?:-[0-9]+)?(?:,[0-9]+(?:-[0-9]+)?)*",a.cpu_list):p.error("CPU list")
 cpus=set()
 for part in a.cpu_list.split(","):
  ends=list(map(int,part.split("-")))
  if ends[-1]<ends[0] or ends[-1]-ends[0]>7:p.error("CPU range")
  cpus.update(range(ends[0],ends[-1]+1))
 if not a.parallelism<=len(cpus)<=8 or not cpus.issubset(os.sched_getaffinity(0)):p.error("CPU affinity bound")
 b=json.loads(a.build_receipt.read_text());rt=json.loads(a.runtime_receipt.read_text())
 image=pathlib.Path(rt["image"]);pack=pathlib.Path(b["pack"]);engine=pathlib.Path(b["engine"])
 tracked={str(a.build_receipt.resolve()):sha(a.build_receipt),str(a.runtime_receipt.resolve()):sha(a.runtime_receipt),
  b["fixture"]:b["fixture_sha256"],b["create"]:b["create_sha256"],b["engine"]:b["engine_sha256"],
  str(pack/"input-receipt.json"):b["pack_receipt_sha256"],**b["source_sha256"],**b["libraries_sha256"],
  **{str(image/name):pin["sha256"] for name,pin in rt["artifacts"].items()}}
 for name,pin in tracked.items():
  if sha(name)!=pin:p.error("input drift "+name)
 inputs=json.loads((pack/"input-receipt.json").read_text())
 original_create=pathlib.Path(b["baseline_create"]).name
 for name,pin in inputs["mods"]["server"].items():
  source=pack/"server/mods"/name
  if sha(source)!=pin:p.error("pack mod drift")
  tracked[str(source)]=pin
  with zipfile.ZipFile(source) as z:
   metadata=json.loads(z.read("fabric.mod.json"),strict=False)
   if metadata["id"] in {"valen_path_probe","valen_engine_prelaunch","valen_engine_world_probe"}:p.error("observer mod in timed pack")
 eula=ROOT/"runtime-baseline/eula.txt"
 if not re.search(r"^eula=true$",eula.read_text(),re.M):p.error("accepted EULA missing")
 out.mkdir(parents=True,mode=0o700);game=out/"game";(game/"mods").mkdir(parents=True)
 for name,pin in inputs["mods"]["server"].items():
  source=pathlib.Path(b["create"]) if name==original_create else pack/"server/mods"/name
  dest=game/"mods"/name;shutil.copyfile(source,dest);tracked[str(dest)]=sha(source)
  if sha(dest)!=tracked[str(dest)]:raise RuntimeError("mod copy mismatch")
 for folder in ("config","defaultconfigs"):
  src=pack/"server"/folder
  if src.exists():shutil.copytree(src,game/folder)
 fixture=pathlib.Path(b["fixture"]);dest=game/"mods"/fixture.name;shutil.copyfile(fixture,dest)
 tracked[str(dest)]=b["fixture_sha256"]
 (game/"eula.txt").write_bytes(eula.read_bytes())
 (game/".valen-disposable-benchmark").write_text("Disposable actual Create rail graph fixture. No production world.\n")
 settings=dict(biome="minecraft:plains",layers=[dict(block="minecraft:bedrock",height=1),dict(block="minecraft:grass_block",height=1)],lakes=False,features=False)
 props={"server-ip":"127.0.0.1","server-port":str(a.port),"online-mode":"false","level-type":"minecraft:flat","level-seed":"626",
  "generator-settings":json.dumps(settings,separators=(",",":")),"view-distance":"2","simulation-distance":"2","enable-rcon":"false",
  "enable-query":"false","spawn-protection":"0","max-tick-time":"-1","spawn-animals":"false","spawn-monsters":"false",
  "generate-structures":"false","level-name":"disposable-rail"}
 (game/"server.properties").write_text("".join(f"{k}={v}\n" for k,v in props.items()))
 cmd=["nice","-n","15","taskset","-c",a.cpu_list,str(image/"bin/java"),"-Xms512m","-Xmx4G","-XX:+UseG1GC",
  "-XX:ActiveProcessorCount="+str(a.parallelism),"-Dvalenmultithreaderd.parallelism="+str(a.parallelism),
  "-Dvalen.rail.fixture=true","-Dvalen.rail.reverse="+str(a.reverse).lower(),"-Dvalen.rail.warmup="+str(a.warmup),"-Dvalen.rail.samples="+str(a.samples),
  "-Dfabric.gameJarPath="+str(engine),"-cp",":".join(b["classpath"]),"net.fabricmc.loader.impl.launch.knot.KnotServer","nogui"]
 record=dict(passed=False,production_installed=False,command=cmd,inputs_sha256=tracked,parallelism=a.parallelism,
  build_receipt=str(a.build_receipt),runtime_receipt=str(a.runtime_receipt),runtime_receipt_sha256=sha(a.runtime_receipt),
  create_sha256=b["create_sha256"],cpu_affinity=a.cpu_list)
 receipt=out/"run-receipt.json"
 def save():receipt.write_text(json.dumps(record,indent=2)+"\n")
 save()
 with socket.socket() as s:s.bind(("127.0.0.1",a.port))
 env=os.environ.copy()
 for k in ("DISPLAY","WAYLAND_DISPLAY","JAVA_TOOL_OPTIONS","JDK_JAVA_OPTIONS","_JAVA_OPTIONS","LD_PRELOAD","LD_LIBRARY_PATH"):env.pop(k,None)
 start=time.monotonic()
 with (out/"server.log").open("w") as log:
  proc=subprocess.Popen(cmd,cwd=game,env=env,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
  record["pid"]=proc.pid;save()
  try:
   try:code=proc.wait(timeout=a.timeout)
   except subprocess.TimeoutExpired:
    record["timed_out"]=True;os.killpg(proc.pid,signal.SIGTERM)
    try:code=proc.wait(timeout=10)
    except subprocess.TimeoutExpired:os.killpg(proc.pid,signal.SIGKILL);code=proc.wait(timeout=10)
  finally:
   if proc.poll() is None:
    os.killpg(proc.pid,signal.SIGTERM)
    try:proc.wait(timeout=10)
    except subprocess.TimeoutExpired:os.killpg(proc.pid,signal.SIGKILL);proc.wait(timeout=10)
 record.update(exit_code=code,process_absent=proc.poll() is not None,wall_seconds=time.monotonic()-start,log_sha256=sha(out/"server.log"))
 result=game/"rail-result.json"
 if result.exists():record.update(result=json.loads(result.read_text()),result_sha256=sha(result))
 changed={}
 for name,pin in tracked.items():
  try:now=sha(name)
  except OSError:now=None
  if now!=pin:changed[name]={"before":pin,"after":now}
 record.update(inputs_unchanged=not changed,changed_inputs=changed)
 record["passed"]=code==0 and not record.get("timed_out") and not changed and record.get("result",{}).get("passed") is True
 save();print(json.dumps(dict(passed=record["passed"],receipt=str(receipt))))
 if not record["passed"]:raise SystemExit(1)
if __name__=="__main__":main()
