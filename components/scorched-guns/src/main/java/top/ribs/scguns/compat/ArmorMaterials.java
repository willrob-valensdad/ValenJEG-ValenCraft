package top.ribs.scguns.compat;

import net.minecraft.core.Holder;
import net.minecraft.world.item.equipment.ArmorMaterial;

/** Holder-shaped aliases retained for old Scorched Guns constructors. */
public final class ArmorMaterials {
    public static final Holder<ArmorMaterial> LEATHER = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.LEATHER);
    public static final Holder<ArmorMaterial> COPPER = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.COPPER);
    public static final Holder<ArmorMaterial> CHAIN = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.CHAINMAIL);
    public static final Holder<ArmorMaterial> IRON = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.IRON);
    public static final Holder<ArmorMaterial> GOLD = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.GOLD);
    public static final Holder<ArmorMaterial> DIAMOND = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.DIAMOND);
    public static final Holder<ArmorMaterial> TURTLE = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.TURTLE_SCUTE);
    public static final Holder<ArmorMaterial> NETHERITE = Holder.direct(net.minecraft.world.item.equipment.ArmorMaterials.NETHERITE);
    private ArmorMaterials() {}
}
