package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class EmptyCasingPouchItem extends AmmoBoxItem {
    private static final int CASING_MAX_ITEM_COUNT = 2048;
    private static final int CASING_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.4F, 0.4F, 0.7F);

    public EmptyCasingPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Identifier getAmmoTag() {
        return Identifier.fromNamespaceAndPath("scguns", "empty_casing");
    }


    @Override
    public int getBarColor(ItemStack stack) {
        return CASING_BAR_COLOR;
    }

    @Override
    protected int getBaseMaxItemCount() {
        return CASING_MAX_ITEM_COUNT;
    }

}
