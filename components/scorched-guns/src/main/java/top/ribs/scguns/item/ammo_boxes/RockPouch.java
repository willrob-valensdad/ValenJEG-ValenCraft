package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class RockPouch extends AmmoBoxItem {
    private static final int ROCKS_MAX_ITEM_COUNT = 6400;
    private static final int ROCKS_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.4F, 0.4F, 0.7F);

    public RockPouch(Properties properties) {
        super(properties);
    }

    @Override
    protected Identifier getAmmoTag() {
        return Identifier.fromNamespaceAndPath("scguns", "rocks");
    }


    @Override
    public int getBarColor(ItemStack stack) {
        return ROCKS_BAR_COLOR;
    }

    @Override
    protected int getBaseMaxItemCount() {
        return ROCKS_MAX_ITEM_COUNT;
    }

}
