package ttv.migami.jeg.enchantment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import ttv.migami.jeg.mteg.MtegConfig;

/** Original JEG71bbf39/5322334 gun enchantment rules, GPLv3. Native enchantments are
 * data-driven; capture actual firing-stack levels, never consult a later held item. */
public final class LegacyGunEnchantments {
    private LegacyGunEnchantments(){}
    public static net.minecraft.core.particles.SimpleParticleType TRAIL;
    private static boolean registered;

    public static final ResourceKey<Enchantment> PUNCTURING=key("puncturing"),COLLATERAL=key("collateral"),FIRE_STARTER=key("fire_starter");
    private static ResourceKey<Enchantment> key(String name){return ResourceKey.create(Registries.ENCHANTMENT,Identifier.fromNamespaceAndPath("jeg",name));}
    public record Snapshot(int puncturing,int collateral,int fireStarter,float criticalChance) {
        public static final Snapshot NONE=new Snapshot(0,0,0,0);
        public Snapshot {
            puncturing=Math.max(0,puncturing);collateral=Math.max(0,collateral);fireStarter=Math.max(0,fireStarter);
            criticalChance=Math.clamp(criticalChance,0,1);
        }
        public boolean any(){return puncturing>0||collateral>0||fireStarter>0;}
        public int flags(){return (puncturing>0?1:0)|(fireStarter>0?2:0);}
    }
    public record Damage(float amount,boolean critical){}
    public static void register(){
        if(registered)return;registered=true;
        TRAIL=net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE,Identifier.fromNamespaceAndPath("jeg","legacy_enchanted_trail"),net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple());
        LegacyEnchantmentTrailPayload.register();LegacyEnchantmentAcquisition.register();
    }

    public static Snapshot capture(ItemStack stack){
        int puncturing=0,collateral=0,fire=0;
        for(var entry:stack.getOrDefault(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY).entrySet()){
            if(entry.getKey().is(PUNCTURING))puncturing=entry.getIntValue();
            else if(entry.getKey().is(COLLATERAL))collateral=entry.getIntValue();
            else if(entry.getKey().is(FIRE_STARTER))fire=entry.getIntValue();
        }
        // Exact original modifier API defaults to zero; no original attachment overrides it.
        return new Snapshot(puncturing,collateral,fire,Math.clamp(puncturing*.05F,0,1));
    }
    public static Damage roll(Snapshot snapshot,RandomSource random,float damage){
        // Preserve current unenchanted paths and their RNG sequence.
        boolean critical=snapshot.criticalChance()>0&&random.nextFloat()<snapshot.criticalChance();
        return new Damage(critical?damage*1.5F:damage,critical);
    }
    public static void igniteEntity(Snapshot snapshot,Entity entity){
        if(snapshot.fireStarter()>0)entity.igniteForSeconds(2);
    }
    public static boolean igniteBlock(Snapshot snapshot,ServerLevel level,BlockHitResult hit,RandomSource random){
        if(snapshot.fireStarter()==0||!MtegConfig.setFireToBlocks())return false;
        var pos=hit.getBlockPos().relative(hit.getDirection());
        if(!BaseFireBlock.canBePlacedAt(level,pos,hit.getDirection()))return false;
        boolean placed=level.setBlock(pos,BaseFireBlock.getState(level,pos),11);
        var p=hit.getLocation();
        level.sendParticles(ParticleTypes.LAVA,p.x-1+random.nextDouble()*2,p.y,p.z-1+random.nextDouble()*2,4,0,0,0,0);
        return placed;
    }
    public static void write(Snapshot snapshot,ValueOutput out){
        if(!snapshot.any())return;
        out.putInt("LegacyPuncturing",snapshot.puncturing());out.putInt("LegacyCollateral",snapshot.collateral());
        out.putInt("LegacyFireStarter",snapshot.fireStarter());out.putFloat("LegacyCriticalChance",snapshot.criticalChance());
    }
    public static Snapshot read(ValueInput in){
        int p=in.getIntOr("LegacyPuncturing",0);
        return new Snapshot(p,in.getIntOr("LegacyCollateral",0),in.getIntOr("LegacyFireStarter",0),in.getFloatOr("LegacyCriticalChance",p*.05F));
    }
}
