#!/usr/bin/env python3
"""Build two private, data-only industrial locations for the 26.3 friends pack."""
import argparse
import gzip
import hashlib
import json
import struct
import zipfile
from pathlib import Path

NS = "valen_facilities"
DATA_VERSION = 5023


def nbt(value):
    if isinstance(value, str):
        raw = value.encode("utf-8")
        return 8, struct.pack(">H", len(raw)) + raw
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, list):
        items = [nbt(item) for item in value]
        kind = items[0][0] if items else 10
        assert all(item[0] == kind for item in items)
        return 9, bytes([kind]) + struct.pack(">i", len(items)) + b"".join(item[1] for item in items)
    if isinstance(value, dict):
        payload = bytearray()
        for key, item in value.items():
            kind, encoded = nbt(item)
            payload += bytes([kind]) + nbt(key)[1] + encoded
        return 10, bytes(payload) + b"\0"
    raise TypeError(type(value))


class Site:
    def __init__(self, size):
        self.size = size
        self.blocks = {}

    def put(self, x, y, z, block, props=None, data=None):
        assert all(0 <= p < length for p, length in zip((x, y, z), self.size))
        state = {"id": block if ":" in block else "minecraft:" + block}
        if props:
            state["properties"] = {k: str(v).lower() for k, v in props.items()}
        self.blocks[(x, y, z)] = (state, data)

    def box(self, x1, y1, z1, x2, y2, z2, block, props=None):
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                for z in range(z1, z2 + 1):
                    self.put(x, y, z, block, props)

    def shell(self, x1, y1, z1, x2, y2, z2, block):
        self.box(x1, y1, z1, x2, y2, z2, block)
        self.box(x1 + 1, y1 + 1, z1 + 1, x2 - 1, y2 - 1, z2 - 1, "air")

    def crate(self, x, y, z, table):
        self.put(x, y, z, "barrel", {"facing": "up"}, {"id": "minecraft:barrel", "LootTable": NS + ":" + table})

    def stairs(self, x, y, z, face, block="polished_andesite_stairs"):
        self.put(x, y, z, block, {"facing": face, "half": "bottom", "shape": "straight", "waterlogged": False})

    def ladder(self, x, y1, y2, z, face):
        self.box(x, y1, z, x, y2, z, "ladder", {"facing": face, "waterlogged": False})

    def compressed(self):
        palette, lookup, blocks = [], {}, []
        for pos, (state, data) in sorted(self.blocks.items(), key=lambda entry: (entry[0][1], entry[0][0], entry[0][2])):
            key = json.dumps(state, sort_keys=True)
            if key not in lookup:
                lookup[key] = len(palette)
                palette.append(state)
            entry = {"pos": list(pos), "state": lookup[key]}
            if data:
                entry["nbt"] = data
            blocks.append(entry)
        root = {"DataVersion": DATA_VERSION, "size": list(self.size), "palette": palette, "blocks": blocks, "entities": []}
        return gzip.compress(b"\x0a\x00\x00" + nbt(root)[1], mtime=0), len(palette)


def foundry():
    s = Site((39, 24, 33))
    s.box(0, 0, 0, 38, 3, 32, "stone_bricks")
    s.box(0, 4, 0, 38, 22, 32, "air")
    s.box(1, 4, 1, 37, 4, 31, "polished_andesite")
    s.shell(3, 4, 4, 27, 14, 27, "bricks")
    # Reinforced frame, pitched roof and continuous clerestory.
    for x in (3, 9, 15, 21, 27):
        s.box(x, 5, 4, x, 14, 4, "polished_deepslate")
        s.box(x, 5, 27, x, 14, 27, "polished_deepslate")
    for z in (4, 10, 16, 22, 27):
        s.box(3, 5, z, 3, 14, z, "polished_deepslate")
        s.box(27, 5, z, 27, 14, z, "polished_deepslate")
    for x in range(2, 29):
        rise = min(x - 2, 28 - x) // 3
        s.box(x, 14 + rise, 3, x, 14 + rise, 28, "cut_copper")
    s.box(11, 17, 8, 19, 17, 23, "glass")
    for x in (6, 12, 18, 24):
        s.box(x, 9, 4, x + 1, 12, 4, "cyan_stained_glass")
        s.box(x, 9, 27, x + 1, 12, 27, "cyan_stained_glass")
    # Open loading bay faces the apron; no inaccessible sealed building.
    s.box(12, 5, 4, 18, 10, 4, "air")
    s.box(11, 11, 3, 19, 11, 3, "iron_block")
    for x in range(11, 20):
        s.put(x, 4, 2, "yellow_concrete" if x % 2 else "black_concrete")
    # Furnaces and workbenches are usable vanilla machines, initially idle.
    for z in (8, 11, 14, 17):
        s.put(5, 5, z, "blast_furnace", {"facing": "east", "lit": False})
        s.box(4, 6, z, 4, 12, z, "iron_block")
        s.put(5, 6, z, "polished_blackstone_pressure_plate")
    for x in range(9, 23, 3):
        s.put(x, 5, 25, "smithing_table" if x % 2 else "crafting_table")
        s.put(x, 6, 25, "lantern")
    # Gantry crane, suspended hook and small cargo stacks.
    for x in (9, 22):
        s.box(x, 5, 9, x, 11, 9, "yellow_concrete")
        s.box(x, 5, 22, x, 11, 22, "yellow_concrete")
        s.box(x, 12, 9, x, 12, 22, "iron_block")
    s.box(9, 12, 15, 22, 12, 15, "yellow_concrete")
    s.box(16, 8, 15, 16, 11, 15, "iron_chain", {"axis": "y", "waterlogged": False})
    s.put(16, 7, 15, "anvil", {"facing": "north"})
    for x, z in ((10, 21), (12, 21), (20, 20), (22, 20), (23, 23)):
        s.crate(x, 5, z, "industrial_salvage")
        if x % 2 == 0:
            s.box(x, 6, z, x, 6, z, "stripped_spruce_log", {"axis": "y"})
    # External service tower with stairs, tank and a solid, cold chimney.
    s.shell(29, 4, 17, 36, 12, 26, "light_gray_concrete")
    s.box(29, 8, 18, 29, 10, 25, "cyan_stained_glass")
    s.box(29, 5, 20, 29, 6, 22, "air")
    s.ladder(35, 5, 12, 25, "north")
    s.box(34, 12, 24, 35, 12, 25, "air")
    for y in range(13, 20):
        s.put(34, y, 23, "iron_bars")
    s.put(34, 20, 23, "lightning_rod", {"facing": "up", "waterlogged": False, "powered": False})
    s.shell(30, 4, 6, 35, 11, 11, "oxidized_cut_copper")
    s.box(27, 6, 8, 29, 6, 8, "cut_copper")
    s.box(24, 12, 22, 26, 22, 24, "bricks")
    s.box(25, 16, 23, 25, 22, 23, "air")
    for x in range(2, 37, 6):
        s.put(x, 5, 30, "sea_lantern")
    # Perimeter rails leave an open 7-block truck entrance.
    s.box(0, 5, 0, 11, 6, 0, "iron_bars", {"east": True, "west": True})
    s.box(19, 5, 0, 38, 6, 0, "iron_bars", {"east": True, "west": True})
    for z in (0, 32):
        for x in (0, 38):
            s.box(x, 5, z, x, 8, z, "polished_deepslate")
            s.put(x, 9, z, "sea_lantern")
    return s


def bunker():
    s = Site((35, 19, 35))
    s.box(0, 0, 0, 34, 4, 34, "deepslate_bricks")
    s.box(0, 5, 0, 34, 18, 34, "air")
    s.box(1, 5, 1, 33, 5, 33, "polished_andesite")
    # Thick low compound, central open courtyard and two control towers.
    s.shell(3, 5, 5, 31, 12, 29, "gray_concrete")
    s.box(11, 6, 12, 23, 18, 23, "air")
    s.box(12, 5, 13, 22, 5, 22, "black_concrete")
    s.box(14, 5, 15, 20, 5, 20, "yellow_concrete")
    s.box(15, 5, 16, 19, 5, 19, "black_concrete")
    # Front gate and traversable interior corridors.
    s.box(15, 6, 5, 19, 9, 12, "air")
    s.box(10, 6, 13, 11, 8, 16, "air")
    s.box(23, 6, 19, 24, 8, 22, "air")
    s.box(12, 6, 23, 15, 8, 24, "air")
    for x in (3, 31):
        s.box(x, 8, 9, x, 9, 11, "iron_bars", {"north": True, "south": True})
        s.box(x, 8, 23, x, 9, 25, "iron_bars", {"north": True, "south": True})
    for z in (5, 29):
        for x in (7, 12, 23, 27):
            s.box(x, 8, z, x + 1, 9, z, "black_stained_glass")
    for x1 in (3, 26):
        s.shell(x1, 12, 5, x1 + 5, 16, 10, "light_gray_concrete")
        s.box(x1 + 1, 14, 5, x1 + 4, 15, 5, "cyan_stained_glass")
        s.box(x1 + 1, 14, 10, x1 + 4, 15, 10, "cyan_stained_glass")
        s.box(x1 + 2, 12, 7, x1 + 3, 12, 8, "air")
        s.box(x1 + 3, 6, 9, x1 + 3, 13, 9, "gray_concrete")
        s.ladder(x1 + 3, 6, 14, 8, "north")
        s.put(x1 + 2, 17, 7, "sea_lantern")
    # Laboratory, armoury, communications room and supply lockers.
    for z in (14, 17, 20, 23, 26):
        s.put(5, 6, z, "smooth_quartz")
        s.put(5, 7, z, "brewing_stand" if z % 2 else "tinted_glass")
        s.crate(29, 6, z, "armory")
    for x in range(8, 28, 4):
        s.put(x, 6, 27, "crafting_table" if x % 3 else "smithing_table")
        s.put(x, 7, 28, "black_glazed_terracotta", {"facing": "north"})
        s.put(x, 8, 28, "stone_button", {"face": "wall", "facing": "north", "powered": False})
    for x, z in ((6, 8), (6, 12), (28, 10), (28, 26), (17, 26)):
        s.put(x, 10, z, "sea_lantern")
    s.box(17, 13, 27, 17, 17, 27, "iron_bars")
    s.box(15, 17, 27, 19, 17, 27, "iron_bars", {"east": True, "west": True})
    # Fortified outer parapet and roof access; front entrance remains open.
    for x in range(35):
        for z in (0, 34):
            if z == 0 and 14 <= x <= 20:
                continue
            s.box(x, 5, z, x, 7 + (x % 3 == 0), z, "deepslate_bricks")
    for z in range(35):
        for x in (0, 34):
            s.box(x, 5, z, x, 7 + (z % 3 == 0), z, "deepslate_bricks")
    for x in range(14, 21):
        s.put(x, 5, 3, "yellow_concrete" if x % 2 else "black_concrete")
    return s


def loot(items):
    entries = [{"type": "minecraft:item", "name": name, "weight": weight,
                "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": minimum, "max": maximum}}]}
               for name, weight, minimum, maximum in items]
    return {"type": "minecraft:chest", "pools": [{"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4}, "entries": entries}]}


def build(destination):
    destination.mkdir(parents=True, exist_ok=False)
    payload = {}
    def add(name, value):
        payload[name] = json.dumps(value, indent=2).encode() + b"\n"
    add("fabric.mod.json", {"schemaVersion": 1, "id": NS, "version": "1.0.0", "name": "Valen Facilities",
        "description": "Private industrial foundry and fortified research compound. Native structure data only.",
        "environment": "*", "license": "All-Rights-Reserved",
        "depends": {"minecraft": "26.3", "fabric-resource-loader-v0": "*", "create": "*", "jeg": "*"}})
    add("pack.mcmeta", {"pack": {"description": "Industrial salvage and fortified research sites", "min_format": [121, 0], "max_format": [121, 0]}})
    add(f"data/{NS}/tags/worldgen/biome/sites.json", {"replace": False, "values": ["#minecraft:is_forest", "#minecraft:is_taiga", "minecraft:plains", "minecraft:sunflower_plains", "minecraft:snowy_plains", "minecraft:savanna", "minecraft:desert", "minecraft:meadow"]})
    manifest = []
    for name, site in (("foundry", foundry()), ("research_bunker", bunker())):
        data, states = site.compressed()
        payload[f"data/{NS}/structure/{name}.nbt"] = data
        add(f"data/{NS}/worldgen/template_pool/{name}.json", {"fallback": "minecraft:empty", "elements": [{
            "weight": 1, "element": {"element_type": "minecraft:single_pool_element", "projection": "rigid",
                "location": f"{NS}:{name}", "processors": "minecraft:empty"}}]})
        add(f"data/{NS}/worldgen/structure/{name}.json", {"type": "minecraft:jigsaw",
            "biomes": f"#{NS}:sites", "step": "surface_structures", "spawn_overrides": {},
            "terrain_adaptation": "beard_thin", "start_pool": f"{NS}:{name}", "size": 0,
            "start_height": {"absolute": -4}, "project_start_to_heightmap": "WORLD_SURFACE_WG",
            "max_distance_from_center": 64, "use_expansion_hack": False})
        manifest.append({"id": f"{NS}:{name}", "dimensions": site.size, "blocks": len(site.blocks),
                         "states": states, "sha256": hashlib.sha256(data).hexdigest()})
    add(f"data/{NS}/worldgen/structure_set/facilities.json", {"structures": [
        {"structure": f"{NS}:foundry", "weight": 1}, {"structure": f"{NS}:research_bunker", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 40, "separation": 18,
                      "salt": 58326031, "spread_type": "triangular"}})
    add(f"data/{NS}/loot_table/industrial_salvage.json", loot([
        ("minecraft:iron_ingot", 8, 2, 6), ("minecraft:copper_ingot", 8, 2, 8),
        ("minecraft:redstone", 6, 2, 6), ("create:andesite_alloy", 4, 1, 3),
        ("create:cogwheel", 3, 1, 2), ("create:brass_ingot", 2, 1, 3)]))
    add(f"data/{NS}/loot_table/armory.json", loot([
        ("minecraft:iron_ingot", 6, 2, 5), ("minecraft:gunpowder", 6, 2, 5),
        ("jeg:pistol_ammo", 4, 4, 12), ("minecraft:redstone", 4, 2, 5),
        ("minecraft:golden_carrot", 2, 1, 3), ("minecraft:spyglass", 1, 1, 1)]))
    jar = destination / "valen-facilities-1.0.0-26.3.jar"
    with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as archive:
        for name, data in sorted(payload.items()):
            entry = zipfile.ZipInfo(name, date_time=(2026, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(entry, data)
    receipt = {"jar": str(jar), "sha256": hashlib.sha256(jar.read_bytes()).hexdigest(),
               "structures": manifest, "files": len(payload), "runtime_classes": 0,
               "tick_functions": 0, "private_original_content": True, "native_acceptance": "pending"}
    (destination / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(json.dumps(receipt, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    build(parser.parse_args().output)
