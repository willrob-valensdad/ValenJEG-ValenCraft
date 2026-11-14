package earth.terrarium.adastra.common.items.armor.base;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class CustomDyeableArmorItem extends Item {

    public CustomDyeableArmorItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, int durabilityFactor) {
        super(properties
            .humanoidArmor(armorMaterial.value(), type)
            .durability(type.getDurability(durabilityFactor))
            .component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)));
    }
}
