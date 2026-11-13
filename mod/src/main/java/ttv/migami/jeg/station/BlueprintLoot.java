package ttv.migami.jeg.station;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import ttv.migami.jeg.init.*;

/** Fabric adapter of original BlueprintOnWither/BlueprintOnWarden and fishing/barter loot modifiers.
 * Probabilities and original boss target sets retained, including actual restored explosive_charge. */
public final class BlueprintLoot {
    private BlueprintLoot() {}
    public static LootPool.Builder pool(boolean advanced,float chance,String... targets) {
        LootPool.Builder pool=LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1)));
        if(chance<1)pool.when(Holder.direct(LootItemRandomChanceCondition.randomChance(chance).build()));
        for(String target:targets) {
            if(target.equals("UNPORTED")) {pool.add(EmptyLootItem.emptyItem());continue;}
            pool.add(LootItem.lootTableItem(ModItems.STATION_MATERIALS.get(advanced ? "advanced_firearm_blueprint" : "firearm_blueprint").get())
                .apply(Holder.direct(SetComponentsFunction.setComponent(ModDataComponents.BLUEPRINT_GUN.get(),"jeg:"+target).build())));
        }
        return pool;
    }
    public static void init() {
        LootTableEvents.MODIFY.register((key,table,source,registries)->{
            String id=key.identifier().toString();
            switch(id) {
                case "minecraft:chests/shipwreck_treasure" -> table.withPool(LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1)))
                    .when(Holder.direct(LootItemRandomChanceCondition.randomChance(1F-.66F).build()))
                    .add(LootItem.lootTableItem(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("jeg:atlantean_spear")))));
                case "minecraft:entities/wither" -> table.withPool(pool(true,1,"explosive_charge","rocket_launcher","explosive_muzzle"));
                case "minecraft:entities/warden" -> table.withPool(pool(true,1,"subsonic_rifle","supersonic_shotgun","hypersonic_cannon"));
                case "minecraft:gameplay/piglin_bartering" -> {
                    table.withPool(pool(true,.05F,"hollenfire_mk2"));
                    table.withPool(pool(true,.03F,"soulhunter_mk2"));
                }
                case "minecraft:gameplay/fishing" -> {
                    table.withPool(pool(false,.03F,"repeating_shotgun"));
                    table.withPool(pool(false,.03F,"service_rifle"));
                    table.withPool(pool(false,.05F,"infantry_rifle"));
                }
                // Current Fabric raids use difficulty-tier reward barrels rather than old faction tables.
                case "jeg:chests/faction_raid_reward_2" -> table.withPool(pool(true,1,"compound_bow"));
                case "jeg:chests/faction_raid_reward_3" -> table.withPool(pool(true,1,"light_machine_gun"));
                case "jeg:chests/faction_raid_reward_4" -> table.withPool(pool(true,1,"flamethrower","blossom_rifle"));
                case "jeg:chests/faction_raid_reward_5" -> table.withPool(pool(true,1,"grenade_launcher"));
                default -> {}
            }
        });
    }
}
