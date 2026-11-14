package ttv.migami.jeg.enchantment;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;

/** Original master71bbf39 FireStarterOnBarteringModifier: append one real level-I
 * enchanted book iff nextFloat()>0.97. No replacement of existing piglin loot. */
public final class LegacyEnchantmentAcquisition {
    private LegacyEnchantmentAcquisition(){}
    private static boolean registered;
    public static boolean grants(float random){return random>.97F;}
    public enum BarterChance implements LootItemCondition {
        INSTANCE;
        public static final MapCodec<BarterChance> CODEC=MapCodec.unit(INSTANCE);
        @Override public MapCodec<? extends LootItemCondition> codec(){return CODEC;}
        @Override public boolean test(LootContext context){return grants(context.getRandom().nextFloat());}
    }
    public static void register(){
        if(registered)return;registered=true;
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE,Identifier.fromNamespaceAndPath("jeg","fire_starter_on_bartering"),BarterChance.CODEC);
        LootTableEvents.MODIFY.register((key,table,source,lookup)->{
            if(!key.identifier().toString().equals("minecraft:gameplay/piglin_bartering"))return;
            var enchant=lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LegacyGunEnchantments.FIRE_STARTER);
            var book=BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("enchanted_book"));
            var function=new SetEnchantmentsFunction.Builder().withEnchantment(enchant,Holder.direct(new ConstantValue(1))).build();
            table.withPool(LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1)))
                    .when(Holder.direct(BarterChance.INSTANCE)).add(LootItem.lootTableItem(book).apply(Holder.direct(function))));
        });
    }
}
