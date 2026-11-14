#!/usr/bin/env python3
"""Normalize exact original Forge material-tag aliases for the Fabric station recipe set."""
import argparse,json
from pathlib import Path
ALIASES={"forge:ingots/iron":"c:ingots/iron","forge:ingots/gold":"c:ingots/gold","forge:ingots/copper":"c:ingots/copper","forge:nuggets/iron":"c:nuggets/iron","forge:dyes/black":"c:dyes/black","forge:gunpowder":"c:gunpowders","forge:sandstone":"c:sandstone/blocks"}
parser=argparse.ArgumentParser();parser.add_argument("--check",action="store_true");args=parser.parse_args()
root=Path(__file__).resolve().parents[1]/"src/main/resources/data/jeg/valen_station"
changed=[];tags=set();uses=0
for path in sorted(root.glob("*.json")):
    data=json.loads(path.read_text());dirty=False
    for material in data.get("materials",[data.get("ingredient",{})]):
        if "tag" not in material:continue
        before=material["tag"];uses+=1
        if before.startswith("forge:"):
            if before not in ALIASES:raise SystemExit("Unmapped original material tag: "+before+" in "+path.name)
            changed.append({"recipe":path.name,"before":before,"after":ALIASES[before],"count":material.get("count",1)})
            if not args.check:material["tag"]=ALIASES[before];dirty=True
        tags.add(material["tag"])
    if dirty:path.write_text(json.dumps(data,indent=2)+chr(10))
if args.check and changed:raise SystemExit("Unnormalized original Forge material tags: "+json.dumps(changed))
print(json.dumps({"recipes":len(list(root.glob("*.json"))),"tagged_material_entries":uses,"unique_tags":sorted(tags),"changes":changed},indent=2))
