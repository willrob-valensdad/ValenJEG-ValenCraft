package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;

public class AnthraliteHammerItem extends Item {
    public AnthraliteHammerItem(ToolMaterial material, float attackSpeed, Properties properties) {
        super(properties.pickaxe(material, 1.0F, attackSpeed));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        boolean result = super.mineBlock(stack, level, state, pos, entity);

        try {
            Class<?> hammerProcedureClass = Class.forName("create_ironworks.procedures.HammerProcedure");
            Method executeMethod = hammerProcedureClass.getMethod("execute", LevelAccessor.class, double.class, double.class, double.class, Entity.class);
            executeMethod.invoke(null, level, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), entity);
        } catch (ReflectiveOperationException ignored) {
        }

        return result;
    }
}
