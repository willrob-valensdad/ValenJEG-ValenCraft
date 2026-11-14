package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.interfaces.IGunModifier;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;

public class BayonetItem extends UnderBarrelItem {
    private final float attackDamage;
    private final float attackSpeed;
    private final IGunModifier modifier;

    public BayonetItem(UnderBarrel underBarrel, Properties properties, float attackDamage, float attackSpeed) {
        super(underBarrel, bayonetAttributes(properties, attackDamage, attackSpeed));
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.modifier = underBarrel.getModifier();
    }

    public BayonetItem(UnderBarrel underBarrel, Properties properties, boolean colored, float attackDamage, float attackSpeed) {
        super(underBarrel, bayonetAttributes(properties, attackDamage, attackSpeed), colored);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.modifier = underBarrel.getModifier();
    }

    public float getAdditionalDamage() {
        return this.modifier.additionalDamage();
    }

        public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(ModEnchantments.BANZAI.asHolder())
                || enchantment.is(Enchantments.SHARPNESS)
                || enchantment.is(Enchantments.SMITE)
                || enchantment.is(Enchantments.BANE_OF_ARTHROPODS)
                || enchantment.is(Enchantments.KNOCKBACK)
                || enchantment.is(Enchantments.FIRE_ASPECT)
                || enchantment.value().isSupportedItem(stack);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
}

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, entityLiving, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity user) {
        return !(user instanceof Player player) || !player.isCreative();
    }

    private static Properties bayonetAttributes(Properties properties, float attackDamage, float attackSpeed) {
        return properties.enchantable(10).attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
    }

    public float getDamage() {
        return this.attackDamage;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(Blocks.COBWEB) ? 15.0F : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState block) {
        return block.is(Blocks.COBWEB);
    }
}
