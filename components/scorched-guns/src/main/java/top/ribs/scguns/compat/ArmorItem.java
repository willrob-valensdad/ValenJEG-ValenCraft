package top.ribs.scguns.compat;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Source-compatibility facade for Scorched Guns' 1.21-era armor subclasses.
 * Minecraft 26.2 models armor as Item data components rather than ArmorItem.
 */
public class ArmorItem extends Item {
    private final Holder<ArmorMaterial> material;
    private final Type type;

    public ArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(properties.humanoidArmor(material.value(), type.modernType()));
        this.material = material;
        this.type = type;
    }

    public Holder<ArmorMaterial> getMaterial() { return this.material; }
    public Type getType() { return this.type; }
    public EquipmentSlot getEquipmentSlot() { return this.type.modernType().getSlot(); }

    public enum Type {
        HELMET(ArmorType.HELMET),
        CHESTPLATE(ArmorType.CHESTPLATE),
        LEGGINGS(ArmorType.LEGGINGS),
        BOOTS(ArmorType.BOOTS),
        BODY(ArmorType.BODY);

        private final ArmorType modernType;
        Type(ArmorType modernType) { this.modernType = modernType; }
        public ArmorType modernType() { return this.modernType; }
        public EquipmentSlot getSlot() { return this.modernType.getSlot(); }
        public int getDurability(int multiplier) { return this.modernType.getDurability(multiplier); }
    }
}
