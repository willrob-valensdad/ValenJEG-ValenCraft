package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AnthralitePaxelItem extends Item {
    private final ToolMaterial material;

    public AnthralitePaxelItem(ToolMaterial material, Properties properties) {
        super(properties.tool(material, BlockTags.MINEABLE_WITH_PICKAXE, 4.5F, -3.05F, 0.0F));
        this.material = material;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isMineableByPaxel(state) && !state.is(this.material.incorrectBlocksForDrops());
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        net.minecraft.core.Registry<net.minecraft.core.component.BlockTransformer> registry = context.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK_TRANSFORMER);
        for (net.minecraft.resources.ResourceKey<net.minecraft.core.component.BlockTransformer> key : java.util.List.of(
                net.minecraft.world.item.component.BlockTransformers.AXE,
                net.minecraft.world.item.component.BlockTransformers.HOE,
                net.minecraft.world.item.component.BlockTransformers.SHOVEL)) {
            java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.core.component.BlockTransformer>> found = registry.get(key.identifier());
            if (found.isEmpty()) continue;
            net.minecraft.world.InteractionResult result = found.get().value().transformBlock(context);
            if (result.consumesAction()) return result;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isMineableByPaxel(state) ? this.material.speed() : 1.0F;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
    }

    private static boolean isMineableByPaxel(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(BlockTags.MINEABLE_WITH_HOE)
                || state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }
}
