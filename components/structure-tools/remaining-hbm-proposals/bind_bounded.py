"""Bind every original HBM prefab cell. Writes only this output directory."""
import gzip, hashlib, importlib.util, json, math, re
from pathlib import Path

OUT = Path(__file__).resolve().parent
PREV_PATH = OUT.parent / "structure-r61-hbm-remaining-prefabs" / "bind_remaining.py"
IND_PATH = Path("../valenjeg-core/structures/industrial/convert_industrial.py")
JEG = Path("../valenjeg-core")
HBM = Path("../hbm/fabric")
LAYOUTS = HBM / "src/main/resources/hbm/multiblock_layouts.nbt"
MODBLOCKS = HBM / "src/main/java/com/hbm/blocks/ModBlocks.java"
JEG_STRUCT = JEG / "src/main/resources/data/jeg/structure/valen"
UP = JEG / "structures/upstream/hbm"

spec = importlib.util.spec_from_file_location("prev", PREV_PATH)
prev = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prev)
spec = importlib.util.spec_from_file_location("ind", IND_PATH)
ind = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ind)

FORGE = ["down", "up", "north", "south", "west", "east"]
OFF = [(0, -1, 0), (0, 1, 0), (0, 0, -1), (0, 0, 1), (-1, 0, 0), (1, 0, 0)]
F2D = {"south": 0, "west": 1, "north": 2, "east": 3}
# Direction.VALUES horizontal order, which is how MultiblockCellShapes.install interns.
INTERN_FACINGS = (2, 0, 1, 3)
FURNACE = {2: "north", 5: "east", 3: "south", 4: "west"}
CRT = ["deco_crt_clean", "deco_crt_broken", "deco_crt_blinking", "deco_crt_bsod"]
WOOD = ["wood_structure_roof", "wood_structure_scaffold", "wood_structure_ceiling"]
CONCRETE_EXT = ["machine", "machine_stripe", "indigo", "purple", "pink", "hazard", "sand", "bronze"]
SLAB = ["concrete_smooth", "concrete", "concrete_asbestos", "ducrete_smooth", "ducrete", "asphalt"]
DUD = ["balefire", "conventional", "nuke", "salted"]
# CellBuckets.GEOMETRY, partition + plain suffix + radix + analog.
BUCKETS = [
    ("door", "h5_r30_m6", 6, False),
    ("door", "h10_r60_m6", 3, False),
    ("door", "h10_r450_m6", 3, False),
    ("door", "h10_r600_m6", 5, False),
    ("door_sealed", "h10_r600_m6", 7, False),
    ("door_sealed", "h20_r1200_m6", 4, False),
    ("static", "h3_r18", 2, False),
    ("static", "h5_r6_m6", 4, False),
    ("static", "h5_r36_m6", 2, False),
    ("static", "h5_r6", 12, False),
    ("static", "h5_r12", 5, True),
    ("static", "h5_r60", 13, False),
    ("static", "h5_r360_m6", 2, False),
    ("static", "h50_r36", 4, False),
    ("rail", "h5_r6", 3, False),
]
BUCKET = { (part, suffix): (radix, analog) for part, suffix, radix, analog in BUCKETS }
CW = {"north": "east", "east": "south", "south": "west", "west": "north"}
CCW = {v: k for k, v in CW.items()}

def load(path):
    return ind.Reader(gzip.decompress(path.read_bytes())).root()

def state_of(tag):
    name = tag.value["Name"].value if "Name" in tag.value else tag.value["id"].value
    props_tag = tag.value.get("Properties") or tag.value.get("properties")
    st = {"Name": name}
    if props_tag is not None:
        st["Properties"] = {k: v.value for k, v in props_tag.value.items()}
    return st

def positions(doc):
    out = {}
    palette = doc.value["palette"].value[1]
    for block in doc.value["blocks"].value[1]:
        pos = tuple(part.value for part in block.value["pos"].value[1])
        out[pos] = (state_of(palette[block.value["state"].value]), block.value.get("nbt"))
    return out

def signed(raw):
    return [(b - 256) if b > 127 else b for b in raw]

def triples(raw):
    vals = signed(raw)
    return [(vals[i], vals[i + 1], vals[i + 2]) for i in range(0, len(vals), 3)]

def parse_layouts():
    root = load(LAYOUTS)
    layouts = {}
    for key, tag in root.value.items():
        body = tag.value
        geom = body["geometry"].value
        keys = [item.value for item in geom["keys"].value[1]]
        offsets, cells = {}, {}
        for facing in range(4):
            offsets[facing] = set(triples(body[f"offsets{facing}"].value))
            coords = triples(geom[f"cells{facing}"].value)
            tuples = geom[f"tuples{facing}"].value
            cells[facing] = {coord: tuples[i] for i, coord in enumerate(coords)}
        layouts[key.split(":", 1)[1]] = {"keys": keys, "offsets": offsets, "cells": cells}
    return layouts

def parse_registrations():
    text = MODBLOCKS.read_text()
    marks = []
    for match in re.finditer(r"Reg\.(?:<[^>]+>)?block(?:Item)?\(|\b(?:stirling|fusionMachine|mediumPylon|fusionComponent)\(", text):
        marks.append(match.start())
    rows = []
    for index, start in enumerate(marks):
        chunk = text[start:marks[index + 1]] if index + 1 < len(marks) else text[start:start + 800]
        name = re.search(r'"([a-z0-9_]+)"', chunk)
        if not name:
            continue
        hardness, resistance, color = None, None, None
        if "doorProperties" in chunk:
            hardness, resistance, color = 10.0, 60.0, 6
        strength = re.search(r"strength\(\s*([0-9.]+)F(?:\s*,\s*([0-9.]+)F)?", chunk)
        if strength:
            hardness = float(strength.group(1))
            resistance = float(strength.group(2) or strength.group(1))
        color_name = re.search(r"MapColor\.([A-Z0-9_]+)", chunk)
        if color_name and color_name.group(1) != "NONE":
            color = {"METAL": 6, "WOOD": 13, "STONE": 11, "COLOR_ORANGE": 15}.get(color_name.group(1), color)
        if "mapColor(MapColor.WOOD)" in chunk:
            color = 13
        light = 15 if "lightLevel" in chunk else 0
        tool = "requiresCorrectToolForDrops" in chunk or "doorProperties" in chunk
        if "BlockDoorGeneric" in chunk:
            part = "door_sealed" if re.search(r"BlockDoorGeneric\([^)]*,\s*true\s*\)", chunk) else "door"
        else:
            part = "static"
        suffix = None
        if hardness is not None:
            suffix = f"h{int(hardness)}_r{int(resistance)}" + ("" if color is None else f"_m{color}")
        rows.append({"id": name.group(1), "part": part, "suffix": suffix, "light": light, "tool": tool})
    return rows

def plain_cells(rows, layouts):
    info = {}
    for row in rows:
        if row["id"] in layouts and row["suffix"] and row["tool"] and not row["light"]:
            info[row["id"]] = row["suffix"]
    return info

def intern_shapes(layouts, rows):
    alphabets = {}
    owners = {}
    for row in rows:
        layout = layouts.get(row["id"])
        if layout is None or not layout["keys"] or row["suffix"] is None or row["light"] or not row["tool"]:
            continue
        spec = BUCKET.get((row["part"], row["suffix"]))
        if spec is None:
            continue
        radix, analog = spec
        alphabet = alphabets.setdefault((row["part"], row["suffix"]), {})
        seen = set()
        for facing in INTERN_FACINGS:
            for tuple_index in layout["cells"][facing].values():
                if tuple_index in seen:
                    continue
                seen.add(tuple_index)
                key = layout["keys"][tuple_index]
                if key not in alphabet:
                    if len(alphabet) >= radix * radix:
                        raise RuntimeError(f"shape alphabet overflow for {row['part']} {row['suffix']} at {row['id']}")
                    alphabet[key] = len(alphabet)
        owners[row["id"]] = (row["part"], row["suffix"], radix, analog, alphabet)
    return owners

def local_of(dx, dy, dz, facing):
    if facing == "west":
        return dz, dy, -dx
    if facing == "north":
        return -dx, dy, -dz
    if facing == "east":
        return -dz, dy, dx
    return dx, dy, dz

def find_core(raw, pos, name):
    seen = set()
    while pos not in seen:
        seen.add(pos)
        block = raw.get(pos)
        if block is None or block[0]["Name"] != name:
            return None
        meta = int(block[0]["Properties"]["meta"])
        reduced = meta - 6 if meta >= 6 else meta
        if reduced >= 6:
            return pos
        ox, oy, oz = OFF[reduced]
        pos = (pos[0] - ox, pos[1] - oy, pos[2] - oz)
    return None

def member_state(block_id, raw, pos, owners, layouts, plains):
    name = raw[pos][0]["Name"]
    core = find_core(raw, pos, name)
    if core is None:
        return None, "core-walk failed"
    core_meta = int(raw[core][0]["Properties"]["meta"])
    direction = core_meta - 10
    if direction not in (2, 3, 4, 5):
        return None, f"core meta {core_meta} is not a horizontal ForgeDirection"
    facing = FORGE[direction]
    lx, ly, lz = local_of(pos[0] - core[0], pos[1] - core[1], pos[2] - core[2], facing)
    if pos == core:
        return ("core", facing, core), None
    layout = layouts[block_id.split(":", 1)[1]]
    slot = F2D[facing]
    if (lx, ly, lz) in layout["cells"][slot]:
        part, suffix, radix, analog, alphabet = owners[block_id.split(":", 1)[1]]
        key = layout["keys"][layout["cells"][slot][(lx, ly, lz)]]
        shape = alphabet[key]
        props = {"shape_hi": str(shape // radix), "shape_lo": str(shape % radix)}
        if part.startswith("door"):
            meta = int(raw[pos][0]["Properties"]["meta"])
            props["open"] = "true" if 6 <= meta < 12 else "false"
        if analog:
            props["comparator"] = "true" if block_id == "hbm:machine_fluidtank" else "false"
        block = f"hbm:multiblock_geometry_cell_{part}_{suffix}"
        return prev.state(block, **props), None
    if (lx, ly, lz) in layout["offsets"][slot]:
        suffix = plains.get(block_id.split(":", 1)[1])
        if suffix is None:
            return None, f"no plain-cell bucket for {block_id}"
        props = {"sound": "0", "sealed": "false"}
        if suffix in ("h5_r12", "h5_r6"):
            props["comparator"] = "true" if block_id == "hbm:machine_fluidtank" else "false"
        return prev.state(f"hbm:multiblock_cell_{suffix}", **props), None
    return None, f"local {lx},{ly},{lz} facing {facing} is outside the baked layout"

def door_nbt(nbt):
    state = nbt.value["state"].value if nbt is not None and "state" in nbt.value else 0
    ticks = nbt.value["openTicks"].value if nbt is not None and "openTicks" in nbt.value else 0
    redstone = nbt.value["redstoned"].value if nbt is not None and "redstoned" in nbt.value else 0
    skin = nbt.value["skin"].value if nbt is not None and "skin" in nbt.value else 0
    tag = ind.compound({
        "id": ind.string("hbm:door"),
        "state": ind.Tag(1, int(state)),
        "openTicks": ind.Tag(3, int(ticks)),
        "redstoned": ind.Tag(3, int(redstone)),
    })
    return tag, int(state), int(skin)

def exact(st, pos, raw, owners, layouts, plains, notes):
    name = st["Name"]
    if not name.startswith("hbm:tile."):
        return None
    short = name[9:]
    meta = int(st.get("Properties", {}).get("meta", 0))
    if short in ("machine_fluidtank", "cargo_door", "silo_hatch", "launch_pad_rusted", "vending_machine", "turret_howard_damaged"):
        block = {
            "machine_fluidtank": "hbm:machine_fluidtank",
            "cargo_door": "hbm:cargo_door",
            "silo_hatch": "hbm:silo_hatch",
            "launch_pad_rusted": "hbm:launch_pad_rusted",
            "vending_machine": "hbm:vending_machine",
            "turret_howard_damaged": "hbm:turret_howard_damaged",
        }[short]
        got, error = member_state(block, raw, pos, owners, layouts, plains)
        if error:
            notes.append({"pos": list(pos), "block": short, "meta": meta, "error": error})
            return None
        if isinstance(got, tuple) and got[0] == "core":
            facing = got[1]
            if short == "machine_fluidtank":
                return prev.state(block, facing=facing, damaged="false")
            if short in ("cargo_door", "silo_hatch"):
                tag, state, skin = door_nbt(raw[pos][1])
                if not 0 <= skin <= 6:
                    notes.append({"pos": list(pos), "block": short, "error": f"skin {skin} outside 0..6"})
                    return None
                return prev.state(block, facing=facing, skin=str(skin), open="true" if state else "false"), tag
            if short == "vending_machine":
                above = raw.get((pos[0], pos[1] + 1, pos[2]))
                snacks = above and above[0]["Name"] == name and int(above[0]["Properties"]["meta"]) >= 6
                return prev.state("hbm:vending_machine_snacks" if snacks else "hbm:vending_machine", facing=facing)
            return prev.state(block, facing=facing)
        return got
    if short == "steel_scaffold":
        orient = "ns_flat" if meta >= 12 else "ew_upright" if meta >= 8 else "ew_flat" if meta >= 4 else "ns_upright"
        notes.append({"pos": list(pos), "block": short, "meta": meta, "color_index": abs(meta) % 4, "bound_orient": orient, "port_has_color_property": False})
        return prev.state("hbm:steel_scaffold", orient=orient, waterlogged="false")
    if short == "stalactite" and meta in (2, 3):
        return prev.state("hbm:stalactite_ice" if meta == 2 else "hbm:stalactite_snow")
    if short == "concrete_colored_ext" and meta < len(CONCRETE_EXT):
        return prev.state("hbm:concrete_ext_" + CONCRETE_EXT[meta])
    if short == "wood_structure" and meta < len(WOOD):
        return prev.state("hbm:" + WOOD[meta], waterlogged="false")
    if short == "wood_barrier" and meta in (2, 3, 4, 5):
        return prev.state("hbm:wood_barrier", facing=FORGE[meta], front="false", left="false", right="false", waterlogged="false")
    if short == "spotlight_incandescent_off":
        return prev.state("hbm:spotlight_incandescent", facing=FORGE[meta >> 1], broken=str(bool(meta & 1)).lower(), lit="false", waterlogged="false")
    if short == "deco_crt":
        return prev.state("hbm:" + CRT[(abs(meta) % 16) // 4], rotation=str(meta % 4))
    if short == "concrete_slab":
        kind = SLAB[(meta & 7) % len(SLAB)]
        block = "hbm:concrete_smooth_slab" if kind == "concrete_smooth" else f"hbm:{kind}_slab"
        return prev.state(block, type="top" if meta & 8 else "bottom", waterlogged="false")
    if short == "steel_roof":
        if meta:
            notes.append({"pos": list(pos), "block": short, "meta": meta, "class_ignores_meta": True})
        return prev.state("hbm:steel_roof", waterlogged="false")
    if short == "machine_electric_furnace_off" and meta in FURNACE:
        return prev.state("hbm:machine_electric_furnace", facing=FURNACE[meta], lit="false")
    if short in ("radio_torch_sender", "radio_torch_receiver") and meta < 6:
        return prev.state("hbm:" + short, facing=FORGE[meta], lit="false")
    if short == "pole_satellite_receiver" and meta in FURNACE:
        return prev.state("hbm:pole_satellite_receiver", facing=FURNACE[meta])
    if short == "crashed_bomb" and meta < len(DUD):
        return prev.state("hbm:crashed_" + DUD[meta])
    if short == "bobblehead":
        return prev.state("hbm:bobblehead", rotation=str(meta & 15))
    if short == "cable_switch":
        return prev.state("hbm:cable_switch", open="true" if meta == 1 else "false")
    return None

def placed_facing(rot):
    # BlockWandLoot.replace sets a fake player's yaw to the stored float, then calls onBlockPlacedBy.
    # Vanilla chest / furnace placement stores floor(yaw * 4 / 360 + 0.5) & 3 as metadata 2, 5, 3, 4.
    return ["north", "east", "south", "west"][math.floor(float(rot) * 4.0 / 360.0 + 0.5) & 3]

def wand_block(nbt):
    if nbt is None or "block" not in nbt.value:
        return {"Name": "hbm:deco_loot"}, 0
    block = nbt.value["block"].value
    replace_meta = int(nbt.value["meta"].value) if "meta" in nbt.value else 0
    if block.startswith("hbm:tile."):
        block = "hbm:" + block[9:]
    # BlockEnums.DecoCabinetEnum is GREEN, STEEL. The old container keeps that metadata;
    # BlockDecoContainer does not apply the yaw, and the port's default facing is north.
    if block == "hbm:filing_cabinet":
        colors = ["hbm:filing_cabinet_green", "hbm:filing_cabinet_steel"]
        if replace_meta >= len(colors):
            return None, replace_meta
        return prev.state(colors[replace_meta], facing="north"), replace_meta
    if block == "minecraft:chest":
        rot = nbt.value["rot"].value if "rot" in nbt.value else 0.0
        return prev.state("minecraft:chest", facing=placed_facing(rot), type="single", waterlogged="false"), replace_meta
    return {"Name": block}, replace_meta

def bobble_types():
    port = Path("../hbm/fabric/src/main/java/com/hbm/blocks/generic/BlockBobble.java").read_text()
    old = (OUT / "reference/Hbm-s-Nuclear-Tech-GIT-ad3185b56ae2b020bf9a4a0ddcd3ebcb6e1002e6/src/main/java/com/hbm/blocks/generic/BlockBobble.java").read_text()
    def names(text):
        return [item.lower() for item in re.findall(r'\(\s*"([^"]+)"', text[text.index("enum BobbleType"):])]
    modern, pinned = names(port), names(old)
    if modern[:len(pinned)] != pinned and modern != pinned:
        raise RuntimeError("bobble type order differs between the pin and the port")
    return modern

def barrier_pass(assigned):
    for pos, (st, nbt, how) in list(assigned.items()):
        if st["Name"] != "hbm:wood_barrier":
            continue
        facing = st["Properties"]["facing"]
        sides = {"front": facing, "left": CCW[facing], "right": CW[facing]}
        props = dict(st["Properties"])
        for label, side in sides.items():
            step = prev.STEP[side]
            neighbor = assigned.get((pos[0] + step[0], pos[1], pos[2] + step[2]))
            props[label] = "true" if neighbor and prev.solid(neighbor[0]) else "false"
        assigned[pos] = ({"Name": st["Name"], "Properties": props}, nbt, how)

def pack(size, blocks):
    palette, index, out = [], {}, []
    for pos in sorted(blocks):
        st, nbt = blocks[pos]
        key = json.dumps(st, sort_keys=True)
        if key not in index:
            index[key] = len(palette)
            props = {k: ind.string(v) for k, v in st.get("Properties", {}).items()}
            entry = {"id": ind.string(st["Name"])}
            if props:
                entry["properties"] = ind.compound(props)
            palette.append(ind.compound(entry))
        block = {"pos": ind.Tag(9, (3, [ind.Tag(3, n) for n in pos])), "state": ind.Tag(3, index[key])}
        if nbt is not None:
            block["nbt"] = nbt
        out.append(ind.compound(block))
    return ind.compound({
        "DataVersion": ind.Tag(3, 5023),
        "size": ind.Tag(9, (3, [ind.Tag(3, n) for n in size])),
        "palette": ind.Tag(9, (10, palette)),
        "blocks": ind.Tag(9, (10, out)),
        "entities": ind.Tag(9, (10, [])),
    })

def jeg_air(name, original, size):
    path = JEG_STRUCT / f"{name}.nbt"
    if not path.exists():
        return {}
    extra = {}
    for pos, (st, nbt) in positions(load(path)).items():
        if pos in original or st["Name"] != "minecraft:air":
            continue
        if all(0 <= pos[i] < size[i] for i in range(3)):
            extra[pos] = ({"Name": "minecraft:air"}, None)
    return extra

def bind(name, source, donor_path, owners, layouts, plains, bobble):
    raw = positions(load(source))
    donor = positions(load(donor_path)) if donor_path else {}
    plain = {pos: (st, None) for pos, (st, nbt) in raw.items()}
    assigned, policy, missing, notes = {}, [], [], []
    for pos, (st, nbt) in raw.items():
        how, got, cell_nbt = None, None, None
        if st["Name"] == "hbm:tile.wand_logic":
            got, how = prev.state("minecraft:air"), "explicit_logic_policy"
            policy.append({"pos": list(pos), "marker": "wand_logic", "block": "minecraft:air", "npc": False})
        elif st["Name"] == "hbm:tile.wand_loot":
            got, replace_meta = wand_block(nbt)
            how = "wand_marker" if got else None
            if got:
                if pos in donor and donor[pos][0]["Name"] == got["Name"]:
                    cell_nbt = donor[pos][1]
                    if donor[pos][0] != got:
                        notes.append({"pos": list(pos), "marker": "wand_loot", "class_state": got, "donor_state": donor[pos][0]})
                policy.append({
                    "pos": list(pos), "marker": "wand_loot", "replace_block": got["Name"], "replace_meta": replace_meta,
                    "source_pool": nbt.value["pool"].value if nbt is not None and "pool" in nbt.value else None,
                    "min": nbt.value["min"].value if nbt is not None and "min" in nbt.value else None,
                    "max": nbt.value["max"].value if nbt is not None and "max" in nbt.value else None,
                    "rot": nbt.value["rot"].value if nbt is not None and "rot" in nbt.value else None,
                    "current_explicit_loot_policy": "jeg:chests/valen/field_supplies",
                })
        elif st["Name"] == "hbm:tile.ore_coal_oil":
            got, how = prev.state("hbm:ore_coal_oil"), "pinned_ore_coal_oil"
        elif pos in donor:
            got, cell_nbt, how = donor[pos][0], donor[pos][1], "donor_coordinate"
        else:
            short = st["Name"][9:] if st["Name"].startswith("hbm:tile.") else ""
            owned = short in {"machine_fluidtank", "cargo_door", "silo_hatch", "launch_pad_rusted", "vending_machine", "turret_howard_damaged"}
            special = exact(st, pos, raw, owners, layouts, plains, notes)
            if isinstance(special, tuple):
                got, cell_nbt = special
            else:
                got = special
            if got is None and not owned:
                got = prev.unique(st) or prev.decode(st, pos, plain)
            how = "bound" if got else None
        if got is None:
            missing.append({"pos": list(pos), "state": st})
            continue
        if st["Name"] == "hbm:tile.bobblehead":
            type_index = int(nbt.value["type"].value) if nbt is not None and "type" in nbt.value else 0
            cell_nbt = ind.compound({"id": ind.string("hbm:bobblehead"), "type": ind.string(bobble[type_index])})
        assigned[pos] = (got, cell_nbt, how)
    if missing:
        return None, missing, policy, notes, raw
    states = {pos: item[0] for pos, item in assigned.items()}
    for pos, (st, nbt, how) in list(assigned.items()):
        if how == "donor_coordinate":
            continue
        props = dict(st.get("Properties", {}))
        block = st["Name"]
        if "shape" in props and block.endswith("_stairs"):
            props["shape"] = prev.stair_shape(pos, st, states)
        if block in prev.CONNECT or block in prev.PANE_COLORS:
            for direction, (dx, dy, dz) in prev.STEP.items():
                neighbor = states.get((pos[0] + dx, pos[1] + dy, pos[2] + dz))
                props[direction] = "true" if prev.connects(block, neighbor) else "false"
        assigned[pos] = ({"Name": block, **({"Properties": props} if props else {})}, nbt, how)
    barrier_pass(assigned)
    return {pos: (st, nbt) for pos, (st, nbt, how) in assigned.items()}, [], policy, notes, raw

def kinds(tag, found):
    if tag is None:
        return
    found.add(tag.kind)
    if tag.kind == 10:
        for child in tag.value.values():
            kinds(child, found)
    elif tag.kind == 9:
        for child in tag.value[1]:
            kinds(child, found)

def main():
    layouts = parse_layouts()
    rows = parse_registrations()
    owners = intern_shapes(layouts, rows)
    plains = plain_cells(rows, layouts)
    if plains.get("vending_machine") != "h5_r6_m6":
        raise RuntimeError("vending machine plain cell parsed as " + str(plains.get("vending_machine")))
    bobble = bobble_types()
    targets = [
        ("military_base", None),
        ("abandoned_arcticstation", None),
        ("falloutshelter_house", None),
        ("submarine", None),
        ("beached_patrol", prev.DONOR / "beached_patrol.nbt"),
        ("oil_rig", prev.DONOR / "oil_rig.nbt"),
    ]
    report, military, raw_military, failed = {}, None, set(), False
    data = OUT / "data/jeg/structure/valen"
    (OUT / "missing").mkdir(exist_ok=True)
    (OUT / "policy").mkdir(exist_ok=True)
    for name, donor in targets:
        source = UP / f"{name}.nbt"
        blocks, missing, policy, notes, raw = bind(name, source, donor, owners, layouts, plains, bobble)
        (OUT / "policy" / f"{name}.json").write_text(json.dumps({"markers": policy, "class_notes": notes}, indent=2) + "\n")
        if missing:
            failed = True
            (OUT / "missing" / f"{name}.json").write_text(json.dumps(missing, indent=2) + "\n")
            print("INCOMPLETE", name, len(missing), missing[0])
            report[name] = {"complete": False, "missing": len(missing)}
            continue
        doc = load(source)
        size = [part.value for part in doc.value["size"].value[1]]
        air = jeg_air(name, blocks, size)
        blocks.update(air)
        if len(blocks) < len(raw) or any(pos not in blocks for pos in raw):
            raise RuntimeError(name + " dropped an original coordinate")
        if any("None" in st["Name"] for st, nbt in blocks.values()):
            raise RuntimeError(name + " produced an unresolved cell id")
        path = data / f"{name}.nbt"
        ind.write_nbt(path, pack(size, blocks))
        checked = positions(load(path))
        if set(checked) != set(blocks):
            raise RuntimeError(name + " round-trip positions differ")
        present = set()
        for st, nbt in checked.values():
            kinds(nbt, present)
        report[name] = {
            "complete": True, "size": size, "original_cells": len(raw), "cells": len(blocks),
            "jeg_air_added": len(air), "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
            "nbt_kinds_present": sorted(present),
        }
        print("complete", name, len(raw), "plus air", len(air), "kinds", sorted(present))
        if name == "military_base":
            military = blocks
            raw_military = set(raw)
    if military and not failed:
        crops = [
            ("watchtower", prev.watchtower, (20, 4, 34), [7, 14, 7]),
            ("armory", prev.armory, (8, 4, 53), [13, 9, 11]),
            ("listening_post", prev.listening, (31, 5, 43), [8, 12, 11]),
        ]
        for name, mask, origin, size in crops:
            local, original_cells = {}, 0
            for pos, cell in military.items():
                if mask(pos):
                    local[tuple(pos[i] - origin[i] for i in range(3))] = cell
                    if pos in raw_military:
                        original_cells += 1
            jeg = jeg_air(name, local, size)
            local.update(jeg)
            path = data / f"{name}.nbt"
            ind.write_nbt(path, pack(size, local))
            report[name] = {
                "complete": True, "size": size, "original_cells": original_cells, "cells": len(local),
                "jeg_air_retained": len(local) - original_cells, "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
            }
            print("crop", name, len(local))
    (OUT / "checkpoint.json").write_text(json.dumps({"label": "ValenJEG", "report": report, "failed": failed}, indent=2) + "\n")
    if failed:
        print("NOT COMPLETE")
        return
    (OUT / "binding-manifest.json").write_text(json.dumps(report, indent=2) + "\n")

if __name__ == "__main__":
    main()
