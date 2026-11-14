#!/usr/bin/env python3
"""Restore Gecko 4's proven zero-valued legacy symbols in five author animations."""
import argparse, hashlib, json, re, zipfile
from pathlib import Path
AUTHOR_SHA="b00ef96c95f692e9e88f7a5d72c1f656a295b6dbf4ecd5a17e2964727649ded7"
TEMPLATE_UPGRADED_SHA="86d508bf61888e0014781f9f181f8826caf23ea638725117bd62ce376a9fb2c8"
EXPECTED={"entity/phantom_gunner":6,"entity/terror_phantom":6,"item/primitive_bow":12,"item/rocket_launcher":2,"item/typhoonee":2}
NAMES={"easeInSine","easeOutSine","easeOutQuad","easeInOutSine"}
p=argparse.ArgumentParser(description=__doc__);p.add_argument("--input",type=Path,required=True);p.add_argument("--output",type=Path,required=True);a=p.parse_args()
sha=lambda b:hashlib.sha256(b).hexdigest()
input_sha=sha(a.input.read_bytes())
assert input_sha in {AUTHOR_SHA,TEMPLATE_UPGRADED_SHA},"Unexpected pinned input"
a.output.mkdir(exist_ok=False)
jar=a.output/"jegn-1.8.2-hotfix1020+fabric26.3-valen-animations.jar";changes=[]
with zipfile.ZipFile(a.input) as source,zipfile.ZipFile(jar,"w") as target:
 for entry in source.infolist():
  before=source.read(entry.filename);after=before
  key=entry.filename.removeprefix("assets/jeg/geckolib/animations/").removesuffix(".animation.json")
  if key in EXPECTED:
   text=before.decode();count=[0]
   def fix(match):
    values=json.loads(match.group(1))
    if len(values)==3 and all(isinstance(x,str) and x in NAMES for x in values):
     assert len(set(values))==1
     count[0]+=1
     return match.group(0).replace(match.group(1),"[0, 0, 0]")
    return match.group(0)
   text=re.sub(r'"vector"\s*:\s*(\[[^\[\]]*\])',fix,text)
   assert count[0]==EXPECTED[key],(key,count)
   after=text.encode()
   # Compare against a narrowly transformed tree, keeping all easing/timing/events.
   old=json.loads(before)
   def expected(o):
    if isinstance(o,dict):
     for k,v in o.items():
      if k=="vector" and len(v)==3 and all(isinstance(x,str) and x in NAMES for x in v):o[k]=[0,0,0]
      else:expected(v)
    elif isinstance(o,list):
     for v in o:expected(v)
   expected(old);assert old==json.loads(after)
   changes.append({"path":entry.filename,"vector_count":count[0],"before":sha(before),"after":sha(after)})
  target.writestr(entry,after)
assert len(changes)==5
with zipfile.ZipFile(a.input) as x,zipfile.ZipFile(jar) as y:
 changed={c["path"] for c in changes};assert x.namelist()==y.namelist()
 assert all(x.read(n)==y.read(n) for n in x.namelist() if n not in changed)
receipt={"input":str(a.input),"input_sha256":input_sha,"author_sha256":AUTHOR_SHA,"jar":str(jar),"sha256":sha(jar.read_bytes()),"changes":changes,"unchanged_payloads":len(x.namelist())-5,"scope":"28 malformed vectors / 84 components only; original Gecko 4.4.4 parser semantics. Every class, legitimate easing field, timestamp, event and remaining ZIP payload unchanged."}
(a.output/"receipt.json").write_text(json.dumps(receipt,indent=2)+"\n")
print(json.dumps(receipt,indent=2))
