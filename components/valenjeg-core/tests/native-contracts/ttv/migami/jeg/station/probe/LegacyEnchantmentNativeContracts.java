package ttv.migami.jeg.station.probe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.storage.*;
import ttv.migami.jeg.enchantment.*;
import ttv.migami.jeg.item.GunItem;

/** Real loaded native registry/component contracts, not hand-built fake enchantment holders. */
public final class LegacyEnchantmentNativeContracts {
    private LegacyEnchantmentNativeContracts(){}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static ItemStack item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.parse(id)).getDefaultInstance();}
    public static void run(ServerLevel level,JsonObject receipt){
        var lookup=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var puncturing=lookup.getOrThrow(LegacyGunEnchantments.PUNCTURING);
        var collateral=lookup.getOrThrow(LegacyGunEnchantments.COLLATERAL);
        var fire=lookup.getOrThrow(LegacyGunEnchantments.FIRE_STARTER);
        check(lookup.get(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT,Identifier.parse("jeg:critical"))).isEmpty(),"Invented critical enchantment ID");
        JsonArray checks=new JsonArray();receipt.add("legacy_enchantment_contracts",checks);
        check(puncturing.value().getMaxLevel()==4&&puncturing.value().getWeight()==2,"Puncturing level/rarity differs");
        for(int i=1;i<=4;i++){
            check(puncturing.value().getMinCost(i)==1+(i-1)*10&&puncturing.value().getMaxCost(i)==11+(i-1)*10,"Puncturing source costs differ");
            ItemStack gun=item("mteg:awp");gun.enchant(puncturing,i);
            var snapshot=LegacyGunEnchantments.capture(gun);
            check(Math.abs(snapshot.criticalChance()-i*.05F)<.000001,"Puncturing5%perlevel lost");
            var random=RandomSource.create(100+i);
            boolean critical=false,normal=false;
            for(int n=0;n<4096&&!(critical&&normal);n++){
                var damage=LegacyGunEnchantments.roll(snapshot,random,20);
                check(damage.amount()==20||damage.amount()==30,"Critical source1.5 multiplier changed");
                if(damage.critical())critical=true;else normal=true;
            }
            check(critical&&normal,"Native RNG did not exercise both source branches");
            gun.set(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY);
            check(snapshot.puncturing()==i&&LegacyGunEnchantments.capture(gun).puncturing()==0,"Shot snapshot depends on later held-stack mutation");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            LegacyGunEnchantments.write(snapshot,out);
            check(LegacyGunEnchantments.read(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult())).equals(snapshot),"Native snapshot roundtrip failed");
        }
        checks.add("Actual levelI-IV components ->5/10/15/20%shot snapshots, seeded native RNG20/30damage, stack mutation independence/native serialization");
        check(collateral.value().getMaxLevel()==1&&collateral.value().getMinCost(1)==10&&collateral.value().getMaxCost(1)==30,"Collateral source levels/costs differ");
        check(fire.value().getMaxLevel()==1&&fire.value().getMinCost(1)==15&&fire.value().getMaxCost(1)==41,"FireStarter inherited max-cost41 lost");
        check(!Enchantment.areCompatible(puncturing,collateral)&&!Enchantment.areCompatible(puncturing,fire)&&!Enchantment.areCompatible(collateral,fire),"Original PROJECTILE exclusivity lost");
        int guns=0;
        for(Item item:BuiltInRegistries.ITEM)if(item instanceof GunItem){
            ItemStack stack=item.getDefaultInstance();guns++;
            check(puncturing.value().isSupportedItem(stack)&&collateral.value().isSupportedItem(stack),"Registered native GunItem missing original applicability:"+BuiltInRegistries.ITEM.getKey(item));
            boolean bow=BuiltInRegistries.ITEM.getKey(item).getPath().equals("compound_bow")||BuiltInRegistries.ITEM.getKey(item).getPath().equals("primitive_bow");
            check(fire.value().isSupportedItem(stack)!=bow,"Original bow FireStarter exception lost:"+BuiltInRegistries.ITEM.getKey(item));
        }
        check(!puncturing.value().isSupportedItem(item("minecraft:stick")),"Fake generic non-gun applicability");
        check(item("mteg:awp").get(DataComponents.ENCHANTABLE).value()==5,"Original MTEG enchantability5 missing");
        check(item("jeg:assault_rifle").get(DataComponents.ENCHANTABLE).value()==10,"Unsolicited modern JEG enchantability change");
        receipt.addProperty("legacy_enchantment_guns",guns);checks.add("Actual native GunItem applicability/bow FireStarter exception/all3exclusivity/MTEG5 vs modernJEG10");
        check(puncturing.is(EnchantmentTags.IN_ENCHANTING_TABLE)&&collateral.is(EnchantmentTags.IN_ENCHANTING_TABLE)&&!fire.is(EnchantmentTags.IN_ENCHANTING_TABLE),"Treasure/table policy differs");
        check(puncturing.is(EnchantmentTags.TRADEABLE)&&collateral.is(EnchantmentTags.TRADEABLE)&&!fire.is(EnchantmentTags.TRADEABLE),"Original FireStarter nontradeable policy differs");
        check(fire.is(EnchantmentTags.TREASURE)&&fire.is(EnchantmentTags.ON_RANDOM_LOOT),"Original treasure/randomloot acquisition lost");
        var table=lookup.getOrThrow(EnchantmentTags.IN_ENCHANTING_TABLE);
        var available=EnchantmentHelper.getAvailableEnchantmentResults(20,item("mteg:awp"),table.stream());
        check(available.stream().anyMatch(e->e.enchantment().is(LegacyGunEnchantments.COLLATERAL))&&available.stream().anyMatch(e->e.enchantment().is(LegacyGunEnchantments.PUNCTURING)),"Native actual enchanting-table selector cannot offer original non-treasure enchants");
        check(available.stream().noneMatch(e->e.enchantment().is(LegacyGunEnchantments.FIRE_STARTER)),"Treasure FireStarter leaked into native table selector");
        check(!LegacyEnchantmentAcquisition.grants(.97F)&&LegacyEnchantmentAcquisition.grants(Math.nextUp(.97F))&&!LegacyEnchantmentAcquisition.grants(0)&&LegacyEnchantmentAcquisition.grants(1),"Exact original barter nextFloat>0.97 boundary changed");
        checks.add("Real registry table/trade/treasure/randomloot membership and actual vanilla table selection; exact barter threshold boundary");
        receipt.addProperty("legacy_enchantment_contract_status","pass");
    }
}
