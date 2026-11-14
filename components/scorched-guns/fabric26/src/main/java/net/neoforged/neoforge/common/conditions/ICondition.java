package net.neoforged.neoforge.common.conditions;
import com.mojang.serialization.MapCodec;
/** Recipe condition the Scorched compat classes already implement. */
public interface ICondition {
    boolean test(IContext context);
    MapCodec<? extends ICondition> codec();
    interface IContext {}
}
