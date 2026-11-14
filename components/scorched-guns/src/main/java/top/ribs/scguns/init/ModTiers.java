package top.ribs.scguns.init;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public final class ModTiers {
    private ModTiers() {}

    public static final ToolMaterial ANTHRALITE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            600,
            7.0F,
            2.5F,
            10,
            ModTags.Items.REPAIRS_ANTHRALITE_TOOLS
    );

    public static final ToolMaterial ANCIENT_BRASS = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            250,
            6.0F,
            3.0F,
            14,
            ModTags.Items.REPAIRS_ANCIENT_BRASS_TOOLS
    );
}
