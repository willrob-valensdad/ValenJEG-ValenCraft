package net.neoforged.neoforge.common.loot;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
/** Global loot modifier with the conditions and priority the Scorched modifiers encode. */
public abstract class LootModifier implements IGlobalLootModifier {
    private final LootItemCondition[] conditions;
    private final int priority;
    protected LootModifier(LootItemCondition[] conditions, int priority) {
        this.conditions = conditions;
        this.priority = priority;
    }
    public LootItemCondition[] conditions() { return conditions; }
    public int priority() { return priority; }
    public static <T extends LootModifier> Products.P2<RecordCodecBuilder.Mu<T>, LootItemCondition[], Integer> codecStart(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                LootItemCondition.DIRECT_CODEC.listOf().xmap(
                        list -> list.toArray(LootItemCondition[]::new),
                        Arrays::asList
                ).fieldOf("conditions").forGetter(LootModifier::conditions),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(LootModifier::priority)
        );
    }
    protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (LootItemCondition condition : conditions) {
            if (!condition.test(context)) return generatedLoot;
        }
        return doApply(generatedLoot, context);
    }
    @Override public abstract MapCodec<? extends IGlobalLootModifier> codec();
}
