// ValenJEG proposal. Registry id stays ore_coal_oil. Credits: HbmMods, LGPL-3.0,
// revision ad3185b56ae2b020bf9a4a0ddcd3ebcb6e1002e6.
// At that pin the block class is gone. MainRegistry ignoreMappings lists
// hbm:tile.ore_coal_oil and hbm:tile.ore_coal_oil_burning. NBTStructure substitutes
// hbm:tile.ore_coal_oil with coal_ore. Textures remain and are copied beside this file:
// textures/ore_coal_oil.png and textures/ore_coal_oil_burning.png.
// No drop, burn, hardness, or resistance class exists at the pin, so none of those are recovered.
// strength 5/6 and the stone sound below are only the port's ordinary simple-block properties,
// enough to register the id with the original texture. They are not a recovered ore_coal_oil class.
// The burning texture stays as source art until a real burning class is recovered.
// Do not keep the 21 factory cells as coal_ore after this block exists; their coordinates are
// factory-cells.json. Root edits the factory template.
Reg.block(
        "ore_coal_oil",
        Block::new,
        () ->
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(5.0F, 6.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE));
