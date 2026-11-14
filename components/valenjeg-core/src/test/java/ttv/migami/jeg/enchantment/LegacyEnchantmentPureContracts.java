package ttv.migami.jeg.enchantment;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.*;

/** Actual native cost codecs, RNG and NBT; no server/client/world or fake registries. */
public final class LegacyEnchantmentPureContracts {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static JsonObject read(String path)throws IOException{
        try(var input=LegacyEnchantmentPureContracts.class.getResourceAsStream(path)){
            if(input==null)throw new AssertionError("Missing production resource "+path);
            return JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    public static void main(String[]args)throws Exception{
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        int[][] source={{4,1,10,11,10},{1,10,0,30,0},{1,15,0,41,0}};
        String[] names={"puncturing","collateral","fire_starter"};
        for(int i=0;i<names.length;i++){
            var json=read("/data/jeg/enchantment/"+names[i]+".json");
            var min=Enchantment.Cost.CODEC.parse(JsonOps.INSTANCE,json.get("min_cost")).getOrThrow();
            var max=Enchantment.Cost.CODEC.parse(JsonOps.INSTANCE,json.get("max_cost")).getOrThrow();
            check(json.get("max_level").getAsInt()==source[i][0],"Source level changed:"+names[i]);
            for(int level=1;level<=source[i][0];level++){
                check(min.calculate(level)==source[i][1]+(level-1)*source[i][2],"Native min cost:"+names[i]);
                check(max.calculate(level)==source[i][3]+(level-1)*source[i][4],"Native max cost:"+names[i]);
            }
            check(json.get("exclusive_set").getAsString().equals("#jeg:exclusive_set/projectile"),"Projectile family exclusivity lost");
        }
        for(long seed:List.of(0L,17L,5322334L)){
            var actual=RandomSource.create(seed);var expected=RandomSource.create(seed);
            var unchanged=LegacyGunEnchantments.roll(LegacyGunEnchantments.Snapshot.NONE,actual,9.5F);
            check(unchanged.amount()==9.5F&&!unchanged.critical(),"Unenchanted damage changed");
            check(actual.nextLong()==expected.nextLong(),"Unenchanted RNG sequence changed");
        }
        var lookup=VanillaRegistries.createWorldLookup();
        for(int level=1;level<=4;level++){
            var shot=new LegacyGunEnchantments.Snapshot(level,0,0,level*.05F);
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);
            LegacyGunEnchantments.write(shot,out);
            check(LegacyGunEnchantments.read(TagValueInput.create(ProblemReporter.DISCARDING,lookup,out.buildResult())).equals(shot),"Native snapshot NBT roundtrip changed");
            var random=RandomSource.create(71+level);int critical=0,normal=0;
            for(int n=0;n<512;n++){
                var hit=LegacyGunEnchantments.roll(shot,random,20F);
                check(hit.amount()==(hit.critical()?30F:20F),"Source1.5 critical multiplier changed");
                if(hit.critical())critical++;else normal++;
            }
            check(critical>0&&normal>0,"Native RNG did not exercise critical/noncritical paths");
        }
        var empty=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);
        LegacyGunEnchantments.write(LegacyGunEnchantments.Snapshot.NONE,empty);
        check(empty.buildResult().isEmpty(),"Unenchanted serialization gained fields");
        check(!LegacyEnchantmentAcquisition.grants(.97F)&&LegacyEnchantmentAcquisition.grants(Math.nextUp(.97F)),"Original exact barter threshold changed");
        var guns=read("/data/jeg/tags/item/enchantable/gun.json").getAsJsonArray("values");
        Set<String> actual=new HashSet<>();for(var gun:guns)check(actual.add(gun.getAsString()),"Duplicate gun applicability ID");
        check(actual.size()==71,"Source-derived native gun applicability count changed");
        var fire=read("/data/jeg/tags/item/enchantable/gun_fire_starter.json").getAsJsonArray("values");
        Set<String> fireIds=new HashSet<>();for(var id:fire)fireIds.add(id.getAsString());
        actual.remove("jeg:compound_bow");actual.remove("jeg:primitive_bow");
        check(actual.equals(fireIds),"Original bow FireStarter exception changed");
        System.out.println("JEG_LEGACY_ENCHANTMENT_PURE_CONTRACTS_PASS checks="+checks+"; actual loaded-registry/anvil/barter/world/client proof remains separate");
    }
}
