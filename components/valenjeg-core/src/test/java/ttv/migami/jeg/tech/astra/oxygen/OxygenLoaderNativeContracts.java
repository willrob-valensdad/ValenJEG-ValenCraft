package ttv.migami.jeg.tech.astra.oxygen;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Pure quantities, real Fabric transactions and native value/recipe codecs; no mixin lookup. */
public final class OxygenLoaderNativeContracts {
    private static int checks;
    private static void check(boolean pass,String label){checks++;if(!pass)throw new AssertionError(label);}
    private static void seed(OxygenLoaderProcess process,long energy,long waterMb,long oxygen){
        CompoundTag tag=new CompoundTag();
        tag.putLong("Energy",energy);tag.putLong("WaterDroplets",waterMb*OxygenLoaderProcess.DROPLETS_PER_MB);tag.putLong("OxygenMb",oxygen);
        process.load(TagValueInput.create(ProblemReporter.DISCARDING,
            net.minecraft.data.registries.VanillaRegistries.createWorldLookup(),tag));
    }
    public static void main(String[] args)throws Exception{
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        var json=JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/jeg/valen_oxygen_loading/water.json")));
        OxygenLoadingRecipe recipe=OxygenLoadingRecipe.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();recipe.validateBinding();
        check(recipe.cookingTime()==1 && recipe.energy()==30 && recipe.input().amount()==100 && recipe.result().amount()==4,"Original pinned water recipe quantities");
        check(OxygenLoaderProcess.DROPLETS_PER_MB==81 && OxygenLoaderProcess.WATER_CAPACITY==243000,"Actual native Fabric mB units and steel tank capacity");
        AtomicInteger dirty=new AtomicInteger();
        var process=new OxygenLoaderProcess(dirty::incrementAndGet);
        seed(process,600,1000,0);
        var initial=process.state();
        try(var transaction=Transaction.openOuter()){
            check(process.step(recipe,transaction),"One source batch completes in one processing tick");
            check(process.energy()==570 && process.water()==900*81 && process.oxygen()==4,"30 E and 100 mB debit produce only 4 existing gas mB");
        }
        check(process.state().equals(initial),"Aborted production restores energy/water/gas/progress together");
        try(var outer=Transaction.openOuter()){
            process.step(recipe,outer);
            try(var nested=outer.openNested()){process.step(recipe,nested);nested.commit();}
        }
        check(process.state().equals(initial),"Nested committed batch plus parent abort restores entire physical state");
        try(var transaction=Transaction.openOuter()){process.step(recipe,transaction);transaction.commit();}
        check(process.energy()==570 && process.water()==72900 && process.oxygen()==4 && dirty.get()==1,"Committed production quantities and final owner dirty callback");
        seed(process,29,1000,0);initial=process.state();
        try(var t=Transaction.openOuter()){check(!process.step(recipe,t),"No batch below required energy");t.commit();}
        check(process.state().equals(initial),"Starved power preserves water and gas");
        seed(process,300,99,0);initial=process.state();
        try(var t=Transaction.openOuter()){check(!process.step(recipe,t),"No batch below source water quantity");t.commit();}
        check(process.state().equals(initial),"Starved water preserves power and gas");
        seed(process,300,1000,2999);initial=process.state();
        try(var t=Transaction.openOuter()){check(!process.step(recipe,t),"Insufficient output room pauses production");t.commit();}
        check(process.state().equals(initial),"Output backpressure consumes neither input");
        seed(process,300,1000,2996);
        try(var t=Transaction.openOuter()){check(process.step(recipe,t),"Exact output capacity admits one full batch");t.commit();}
        check(process.oxygen()==3000 && process.energy()==270 && process.water()==72900,"Output clamps by full batch rather than excess generation");
        initial=process.state();
        try(var t=Transaction.openOuter()){check(!process.step(recipe,t),"Full gas tank blocks another batch");t.commit();}
        check(process.state().equals(initial),"Full output retains inputs");
        seed(process,0,0,0);
        try(var t=Transaction.openOuter()){
            check(process.energyInput().insert(1000,t)==150,"Source steel energy admission 150 per transfer");
            check(process.energyInput().extract(30,t)==0,"Machine exposes no free external power extraction");
            check(process.insertWater(45,t)==45,"Fractional native mB droplets retained without rounding");
        }
        check(process.energy()==0 && process.water()==0,"Incoming input transaction abort restores both quantities");
        seed(process,100,200,8);
        try(var outer=Transaction.openOuter()){
            check(process.extractOxygen(3,outer)==3,"Finite output extraction quantity");
        }
        check(process.oxygen()==8,"Oxygen extraction abort retains reserve");
        try(var t=Transaction.openOuter()){process.insertWater(1,t);t.commit();}
        var output=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);process.save(output);
        var restored=new OxygenLoaderProcess(()->{});
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,net.minecraft.data.registries.VanillaRegistries.createWorldLookup(),output.buildResult()));
        check(process.state().equals(restored.state()),"Native value persistence retains exact fractional water units and finite gas/energy");
        var corrupt=output.buildResult();corrupt.putLong("Energy",Long.MAX_VALUE);corrupt.putLong("WaterDroplets",-1);corrupt.putLong("OxygenMb",Long.MAX_VALUE);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,net.minecraft.data.registries.VanillaRegistries.createWorldLookup(),corrupt));
        check(restored.energy()==20000 && restored.water()==0 && restored.oxygen()==3000,"Malformed persistence is bounded");
        boolean rejected=false;try(var t=Transaction.openOuter()){restored.energyInput().insert(-1,t);}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Negative native input rejected");
        System.out.println("Oxygen draft classpath contracts PASS "+checks+"; real item/fluid/gas/tank/suit behavior remains proper-Fabric-only and unexecuted");
    }
}
