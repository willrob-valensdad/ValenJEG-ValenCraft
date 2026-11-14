package top.ribs.scguns.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class CogMaceItem extends Item {
    public CogMaceItem(ToolMaterial material, int attackDamage, float attackSpeed, Properties properties) {
        super(properties.sword(material, attackDamage, attackSpeed));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.resetFallDistance();
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }
}
