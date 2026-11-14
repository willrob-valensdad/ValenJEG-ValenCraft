package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class RocketAmmoBoxItem extends AmmoBoxItem {
    private static final int ROCKET_MAX_ITEM_COUNT = 256;
    private static final int ROCKET_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.4F, 0.4F, 0.7F);

    public RocketAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Identifier getAmmoTag() {
        return Identifier.fromNamespaceAndPath("scguns", "rocket_ammo");
    }


    @Override
    public int getBarColor(ItemStack stack) {
        return ROCKET_BAR_COLOR;
    }

    @Override
    protected int getBaseMaxItemCount() {
        return ROCKET_MAX_ITEM_COUNT;
    }

}
