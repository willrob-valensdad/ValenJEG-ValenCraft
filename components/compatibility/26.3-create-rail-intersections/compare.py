#!/usr/bin/env python3
"""Consolidate completed full-operation rail receipts without launching work."""
import argparse,hashlib,json,pathlib,re,statistics
def sha(p):return hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument("--root",type=pathlib.Path,required=True);p.add_argument("--output",type=pathlib.Path,required=True)
 a=p.parse_args()
 if a.output.exists():p.error("new output required")
 runs={};reference={};receipts={}
 for f in sorted(a.root.glob("*/run-receipt.json")):
  name=f.parent.name
  if not re.fullmatch(r"(baseline|candidate)-p[1248]-r[23]",name):continue
  d=json.loads(f.read_text())
  assert d["passed"] and d["exit_code"]==0 and d["process_absent"] and d["inputs_unchanged"],name
  result=f.parent/"game/rail-result.json";assert sha(result)==d["result_sha256"]
  assert d["result"]["warmup"]==8 and d["result"]["samples"]==15,name
  rows={}
  for row in d["result"]["cases"]:
   case=row["case"];signature=tuple(row[k] for k in ("geometry_sha256","intersection_records","deferred_updates","adopted_edge_count"))
   if case in reference:assert reference[case]==signature,(name,case)
   else:reference[case]=signature
   values=row["samples_ns"];assert len(values)==15 and statistics.median(values)==row["median_ns"]
   values=sorted(v/1e6 for v in values)
   rows[case]=dict(median_ms=statistics.median(values),min_ms=values[0],p25_ms=values[3],p75_ms=values[11],max_ms=values[-1])
  assert len(rows)==7
  runs[name]=rows;receipts[name]=dict(path=str(f),sha256=sha(f),wall_seconds=d["wall_seconds"],pid=d["pid"],create_sha256=d["create_sha256"])
 pairs=[]
 for revision in (2,3):
  for count in (1,2,4,8):
   old=f"baseline-p{count}-r{revision}";new=f"candidate-p{count}-r{revision}"
   if old not in runs or new not in runs:continue
   for case in runs[old]:
    x=runs[old][case];y=runs[new][case]
    pairs.append(dict(parallelism=count,revision=revision,case=case,original=x,candidate=y,speedup=x["median_ms"]/y["median_ms"]))
 oracle=[]
 left=json.loads((a.root/"baseline-oracle-p4-r1/run-receipt.json").read_text())
 for directory in ("candidate-oracle-p4-r1","candidate-oracle-stock-r1"):
  path=a.root/directory/"run-receipt.json"
  if not path.exists():continue
  right=json.loads(path.read_text());assert left["passed"] and right["passed"]
  assert len(left["result"]["cases"])==len(right["result"]["cases"])==15
  for x,y in zip(left["result"]["cases"],right["result"]["cases"]):
   assert all(x[k]==y[k] for k in ("case","ordered_graph_sha256","uuid_calls","owner_trace_sha256","expected_failure_identity")),(directory,x["case"])
  oracle.append(dict(path=str(path),sha256=sha(path),exact_ordered_cases=15))
 candidate=json.loads((a.root/"candidate-build-r1/candidate-receipt.json").read_text())
 assert sha(candidate["candidate"])==candidate["candidate_sha256"]
 assert all(sha(k)==v for k,v in candidate["source_sha256"].items())
 pins=candidate["baseline"];assert sha(pins["baseline_jar"])==pins["baseline_sha256"]
 result=dict(equal_outputs=True,production_installed=False,scope="controlled actual Create rail construction, not a current gameplay bottleneck claim",
  method="whole connectNodes including prepare, geometry, UUID, dedup, publication; setup and output validation excluded",
  limitations=["One fresh JVM per runner/count/revision; distributions are within-process samples, not statistical confidence intervals.",
   "Participant count also sets ActiveProcessorCount; each candidate has a matching original control at the same count.",
   "P1 differences include JIT/layout/order effects and must not be attributed to native scaling."],
  candidate_sha256=candidate["candidate_sha256"],baseline_sha256=pins["baseline_sha256"],oracle=oracle,receipts=receipts,pairs=pairs)
 a.output.write_text(json.dumps(result,indent=2)+"\n");print(json.dumps(dict(equal_outputs=True,output=str(a.output),pairs=len(pairs))))
if __name__=="__main__":main()
