package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class ShotgunAmmoBoxItem extends AmmoBoxItem {
    private static final int SHOTGUN_MAX_ITEM_COUNT = 768;
    private static final int SHOTGUN_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.4F, 0.4F, 0.7F);

    public ShotgunAmmoBoxItem(Properties properties) {
        super(properties);
    }
    @Override
    protected Identifier getAmmoTag() {
        return Identifier.fromNamespaceAndPath("scguns", "shotgun_ammo");
    }


    @Override
    public int getBarColor(ItemStack stack) {
        return SHOTGUN_BAR_COLOR;
    }

    @Override
    protected int getBaseMaxItemCount() {
        return SHOTGUN_MAX_ITEM_COUNT;
    }

}

