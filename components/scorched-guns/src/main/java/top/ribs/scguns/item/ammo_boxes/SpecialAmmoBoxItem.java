package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class SpecialAmmoBoxItem extends AmmoBoxItem {
    private static final int SPECIAL_MAX_ITEM_COUNT = 512;
    private static final int SPECIAL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.4F, 0.4F, 0.7F);

    public SpecialAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Identifier getAmmoTag() {
        return Identifier.fromNamespaceAndPath("scguns", "special_ammo");
    }


    @Override
    public int getBarColor(ItemStack stack) {
        return SPECIAL_BAR_COLOR;
    }

    @Override
    protected int getBaseMaxItemCount() {
        return SPECIAL_MAX_ITEM_COUNT;
    }

}
