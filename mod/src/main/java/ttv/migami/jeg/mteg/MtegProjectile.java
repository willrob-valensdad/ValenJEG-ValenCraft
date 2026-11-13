package ttv.migami.jeg.mteg;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.init.ModDamageTypes;
import ttv.migami.jeg.init.ModTags;

/**
 * Native adapter for exact JEG5322334 ProjectileEntity and the three original MTEG
 * subclasses (MTEG5d7fb0f, GPLv3). No modern-category range, armor, gravity or cone.
 * configure damage is the total modified shot damage, divided into source pellet count.
 */
public final class MtegProjectile extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<String> GUN=SynchedEntityData.defineId(MtegProjectile.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DAMAGE=SynchedEntityData.defineId(MtegProjectile.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIZE=SynchedEntityData.defineId(MtegProjectile.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE=SynchedEntityData.defineId(MtegProjectile.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> GRAVITY=SynchedEntityData.defineId(MtegProjectile.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> AGE=SynchedEntityData.defineId(MtegProjectile.class,EntityDataSerializers.INT);
    public static final TagKey<EntityType<?>> HEAVY=sourceTag("heavy"),VERY_HEAVY=sourceTag("very_heavy"),UNDEAD=sourceTag("undead"),GHOST=sourceTag("ghost");
    private static TagKey<EntityType<?>> sourceTag(String name){return TagKey.create(Registries.ENTITY_TYPE,Identifier.fromNamespaceAndPath("mteg",name));}
    private static final Map<Player,LinkedList<AABB>> PLAYER_BOXES=new WeakHashMap<>();
    private static boolean registered;
    private ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot legacyEnchantments=ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot.NONE;
    private boolean legacyTrailSent;
    public void setLegacyEnchantments(ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot snapshot){legacyEnchantments=snapshot;}
    public ttv.migami.jeg.enchantment.LegacyGunEnchantments.Snapshot legacyEnchantments(){return legacyEnchantments;}

        public MtegProjectile(EntityType<? extends MtegProjectile> type,Level level) {super(type,level);setNoGravity(true);}
    public static void register() {
        if(registered)return;registered=true;
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var player:server.getPlayerList().getPlayers()) {
                if(player.isSpectator()){PLAYER_BOXES.remove(player);continue;}
                var boxes=PLAYER_BOXES.computeIfAbsent(player,p->new LinkedList<>());
                boxes.addFirst(player.getBoundingBox());if(boxes.size()>20)boxes.removeLast();
            }
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->PLAYER_BOXES.remove(handler.getPlayer()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->PLAYER_BOXES.keySet().removeIf(p->p.level() instanceof ServerLevel l&&l.getServer()==server));
    }
    public void configure(LivingEntity owner,MtegDefinition definition,Vec3 direction,float totalDamage) {
        setOwner(owner);entityData.set(GUN,definition.id().toString());
        entityData.set(DAMAGE,Math.max(0,totalDamage));entityData.set(SIZE,definition.projectileSize());
        entityData.set(LIFE,definition.projectileLife());entityData.set(GRAVITY,definition.gravity()?-.04F:0F);
        setPos(owner.getX(),owner.getEyeY(),owner.getZ());
        setDeltaMovement(direction.normalize().scale(definition.projectileSpeed()));
        updateRotation();refreshDimensions();setOldPosAndRot();
    }
    public MtegDefinition definition(){return MtegContent.DEFINITIONS.get(Identifier.parse(entityData.get(GUN)));}
    public float totalDamage(){return entityData.get(DAMAGE);}
    public int age(){return entityData.get(AGE);}
    public int lifetime(){return entityData.get(LIFE);}
    public double originalGravity(){return entityData.get(GRAVITY);}
    public int fireSeconds(){
        String ammo=definition().ammo().toString();
        return ammo.equals("mteg:taki")?5:ammo.equals("minecraft:magma_cream")?10:ammo.equals("mteg:flare")?20:0;
    }
    @Override public ItemStack getItem(){return new ItemStack(BuiltInRegistries.ITEM.getValue(definition().ammo()));}
    @Override public EntityDimensions getDimensions(Pose pose){float size=entityData.get(SIZE);return EntityDimensions.scalable(size,size);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        builder.define(GUN,"mteg:scar_l");builder.define(DAMAGE,8F);builder.define(SIZE,.05F);builder.define(AGE,0);builder.define(LIFE,80);builder.define(GRAVITY,0F);
    }
    private BlockHitResult blocks(Vec3 start,Vec3 end,boolean ignoreLeaves){
        var context=new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this);
        return BlockGetter.traverseBlocks(start,end,context,(ray,pos)->{
            var state=level().getBlockState(pos);
            if(ignoreLeaves&&state.getBlock() instanceof LeavesBlock)return null;
            return level().clipWithInteractionOverride(start,end,pos,ray.getBlockShape(state,level(),pos),state);
        },ray->BlockHitResult.miss(end,Direction.getApproximateNearest(start.subtract(end)),BlockPos.containing(end)));
    }
    private record Target(Entity entity,Vec3 position,boolean headshot){}
    private Target target(Entity entity,Vec3 start,Vec3 end){
    if(entity==getOwner())return null;
    AABB body=entity.getBoundingBox();
    if(entity instanceof ServerPlayer player && getOwner() instanceof ServerPlayer shooter) {
        var history=PLAYER_BOXES.get(player);
        if(history!=null&&!history.isEmpty()){
            int lag=(int)Math.floor(shooter.connection.latency()/1000D*20+.5);
            body=history.get(Math.clamp(lag,0,history.size()-1));
        }
    }
    if(entity instanceof Player&&!entity.isCrouching())body=body.expandTowards(0,.0625,0);
    Vec3 direct=body.clip(start,end).orElse(null);
    Vec3 grown=body.inflate(.3,0,.3).clip(start,end).orElse(null);
    if(direct==null&&grown!=null&&blocks(start,grown,true).getType()!=HitResult.Type.BLOCK)direct=grown;
    boolean headshot=false;
    if(entity instanceof LivingEntity living){
        AABB head=headBox(living);
        if(head!=null){
            var center=body.getCenter();head=head.move(center.x,body.minY,center.z);
            Vec3 headHit=head.clip(start,end).orElse(null);
            if(headHit==null)headHit=head.inflate(.3,0,.3).clip(start,end).orElse(null);
            if(headHit!=null&&(direct==null||headHit.distanceTo(direct)<.5)){direct=headHit;headshot=true;}
        }
    }
        return direct==null?null:new Target(entity,direct,headshot);
    }
    private List<Target> targets(Vec3 start,Vec3 end){
        List<Target> result=new ArrayList<>();
        for(var entity:level().getEntities(this,getBoundingBox().expandTowards(getDeltaMovement()).inflate(1),e->e.isPickable()&&!e.isSpectator())){
            var hit=target(entity,start,end);if(hit!=null)result.add(hit);
        }
        return result;
    }
    private Target target(Vec3 start,Vec3 end){
        Target closest=null;double distance=Double.MAX_VALUE;
        for(var hit:targets(start,end)){double next=start.distanceToSqr(hit.position);if(next<distance){distance=next;closest=hit;}}
        return closest;
    }
    @Override public void tick(){
        super.tick();updateRotation();
        if(!level().isClientSide()&&!legacyTrailSent){
            ttv.migami.jeg.enchantment.LegacyEnchantmentTrailPayload.send(this,legacyEnchantments,Math.max(1,lifetime()-age()),originalGravity(),false);legacyTrailSent=true;
        }
        if(level().isClientSide())particles();
        else {
            var start=position();var end=start.add(getDeltaMovement());var block=blocks(start,end,true);
            var clipped=block.getType()==HitResult.Type.MISS?end:block.getLocation();
            if(legacyEnchantments.collateral()>0){
                // Exact5322334: collect the complete block-clipped segment, then process
                // every collected hit even after its normal per-impact removal.
                var hits=targets(start,clipped);
                for(var target:hits)if(!(target.entity instanceof Player player&&getOwner() instanceof Player shooter&&!shooter.canHarmPlayer(player)))hit(target);
                if(hits.isEmpty()&&block.getType()!=HitResult.Type.MISS)hitBlock(block);
            }else{
                var target=target(start,clipped);
                if(target!=null){
                    if(!(target.entity instanceof Player player&&getOwner() instanceof Player shooter&&!shooter.canHarmPlayer(player)))hit(target);
                }else if(block.getType()!=HitResult.Type.MISS)hitBlock(block);
            }
        }
        // The original always moves the complete velocity, then adds gravity, without drag.
        var motion=getDeltaMovement();setPos(position().add(motion));
        setDeltaMovement(motion.add(0,originalGravity(),0));
        entityData.set(AGE,age()+1);
        if(age()>=lifetime())discard();
        // Original subclasses trace again AFTER superclass movement, even after base impact.
        if(!level().isClientSide()&&fireSeconds()>0){
            var block=blocks(position(),position().add(getDeltaMovement()),fireSeconds()!=10);
            if(block.getType()!=HitResult.Type.MISS)igniteBlock(block);
        }
    }
    public static float perPelletDamage(float total,int pellets){return Math.max(0,total/Math.max(1,pellets));}
    public static float advantageMultiplier(String advantage,boolean heavy,boolean veryHeavy,boolean undead,boolean ghost){
        float value=1;
        if(!advantage.equals("jeg:none")){
            if(advantage.equals("jeg:undead")&&(undead||ghost))value=1.25F;
            if(heavy)value=advantage.equals("jeg:heavy")||advantage.equals("jeg:very_heavy")?1.25F:.5F;
            else if(veryHeavy)value=advantage.equals("jeg:heavy")?1F:.25F;
        }
        return value;
    }
    private boolean tagged(Entity target,TagKey<EntityType<?>> tag){return target.getType().builtInRegistryHolder().is(tag);}
    private void hit(Target hit){
        Entity entity=hit.entity;
        if(entity==getOwner()||(getOwner() instanceof Player player&&entity.isAlliedTo(player)))return;
        ttv.migami.jeg.enchantment.LegacyGunEnchantments.igniteEntity(legacyEnchantments,entity);
        var definition=definition();String advantage=definition.advantage();
        boolean ghost=tagged(entity,GHOST),undead=tagged(entity,UNDEAD);
        float damage=perPelletDamage(totalDamage(),definition.pellets());
        var source=definition.source().getAsJsonObject("projectile");
        if(source.has("damageReduceOverLife")&&source.get("damageReduceOverLife").getAsBoolean())damage*=Math.max(0,(lifetime()-age())/(float)lifetime());
        var critical=ttv.migami.jeg.enchantment.LegacyGunEnchantments.roll(legacyEnchantments,random,damage);damage=critical.amount();
        damage*=advantageMultiplier(advantage,tagged(entity,HEAVY),tagged(entity,VERY_HEAVY),undead,ghost);
        if(advantage.equals("jeg:undead")&&(undead||ghost))entity.igniteForSeconds(2);
        if(hit.headshot)damage*=1.25F;
        if(!ghost||advantage.equals("jeg:undead")){
            var damageSource=ModDamageTypes.causeBulletDamage(level().registryAccess(),this,getOwner());
            entity.hurtServer((ServerLevel)level(),damageSource,damage);
        }
        if(fireSeconds()>0)entity.igniteForSeconds(fireSeconds());
        entity.setInvulnerableTime(0);
        if(critical.critical()&&getOwner() instanceof ServerPlayer player)ttv.migami.jeg.network.NetworkHandler.sendHitMarker(player,true);
        // Exact5322334 exception; ordinary collateral still stops after this segment.
        if(!(ghost&&advantage.equals("jeg:undead")&&legacyEnchantments.collateral()>0))discard();
    }
    private void hitBlock(BlockHitResult hit){
        var state=level().getBlockState(hit.getBlockPos());float hardness=state.getDestroySpeed(level(),hit.getBlockPos());
        if(Config.bulletBlockDestructionEnabled()&&state.is(ModTags.Blocks.FRAGILE)&&hardness>=0&&random.nextFloat()<1/(hardness+1))
            level().destroyBlock(hit.getBlockPos(),true);
        if(!state.canBeReplaced())discard();
        if(level() instanceof ServerLevel server)ttv.migami.jeg.enchantment.LegacyGunEnchantments.igniteBlock(legacyEnchantments,server,hit,random);
        // Vanilla projectile hit callbacks retain buttons/targets etc. through the native state API.
        super.onHitBlock(hit);
    }
    private void igniteBlock(BlockHitResult hit){
        if(!MtegConfig.setFireToBlocks())return;
        var pos=hit.getBlockPos().relative(hit.getDirection());
        if(BaseFireBlock.canBePlacedAt(level(),pos,hit.getDirection())){
            level().setBlock(pos,BaseFireBlock.getState(level(),pos),11);
            var p=hit.getLocation();((ServerLevel)level()).sendParticles(ParticleTypes.LAVA,p.x-1+random.nextDouble()*2,p.y,p.z-1+random.nextDouble()*2,4,0,0,0,0);
        }
    }
    private void particles(){
        int fire=fireSeconds();if(fire==0)return;
        var velocity=getDeltaMovement();int count=fire==20?2:5;
        for(int i=0;i<count;i++){
            // The source i=0 divided by zero. Preserve the first trail sample at the actual
            // projectile instead of emitting NaN/Infinity positions into the native renderer.
            double divisor=i==0?1:i;var p=position().subtract(velocity.scale(i==0?0:1/divisor));
            if(fire==10)level().addParticle(MtegContent.FIREBALL_PARTICLE,p.x,p.y,p.z,0,0,0);
            else{
                if(fire==20)level().addParticle(MtegContent.FLARE_SMOKE_PARTICLE,p.x,p.y,p.z,0,0,0);
                for(int j=0;j<(fire==20?3:1);j++)level().addParticle(ParticleTypes.LAVA,p.x,p.y,p.z,0,0,0);
            }
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput output){
        super.addAdditionalSaveData(output);ttv.migami.jeg.enchantment.LegacyGunEnchantments.write(legacyEnchantments,output);output.putString("Gun",entityData.get(GUN));output.putFloat("Damage",totalDamage());
        output.putFloat("Size",entityData.get(SIZE));output.putInt("Life",lifetime());output.putInt("Age",age());output.putDouble("ModifiedGravity",originalGravity());
    }
    @Override protected void readAdditionalSaveData(ValueInput input){
        super.readAdditionalSaveData(input);legacyEnchantments=ttv.migami.jeg.enchantment.LegacyGunEnchantments.read(input);String gun=input.getStringOr("Gun","mteg:scar_l");
        if(!MtegContent.DEFINITIONS.containsKey(Identifier.parse(gun))){discard();return;}
        entityData.set(GUN,gun);entityData.set(DAMAGE,Math.max(0,input.getFloatOr("Damage",definition().damage())));
        entityData.set(SIZE,input.getFloatOr("Size",definition().projectileSize()));entityData.set(LIFE,Math.max(1,input.getIntOr("Life",definition().projectileLife())));
        entityData.set(AGE,Math.max(0,input.getIntOr("Age",0)));entityData.set(GRAVITY,(float)input.getDoubleOr("ModifiedGravity",definition().gravity()?-.04:0));refreshDimensions();
    }

    /* Exact5322334 BoundingBoxManager head geometry (the later master enlarged many8->10). */
    private record Head(double width,double height,double y,double z,boolean child,boolean noChild,boolean pitch){
        AABB box(LivingEntity entity){
            if(noChild&&entity.isBaby())return null;
            AABB box=new AABB(-width/32, y/16, -width/32,width/32,(y+height)/16,width/32);
            if(z!=0)box=box.move(Vec3.directionFromRotation(pitch?entity.getXRot():0,entity.yBodyRot).normalize().scale(z/16));
            if(child&&entity.isBaby())return new AABB(box.minX*.75,box.minY*.5,box.minZ*.75,box.maxX*.75,box.maxY*.565,box.maxZ*.75);
            return box;
        }
    }
    private static final Map<String,Head> HEADS=new HashMap<>();
    static {
        HEADS.put("minecraft:zombie",new Head(8.0,8.0,24.0,0,true,false,false));
        HEADS.put("minecraft:zombified_piglin",new Head(8.0,8.0,24.0,0,true,false,false));
        HEADS.put("minecraft:husk",new Head(8.0,8.0,24.0,0,true,false,false));
        HEADS.put("minecraft:skeleton",new Head(8.0,8.0,24.0,0,false,false,false));
        HEADS.put("minecraft:stray",new Head(8.0,8.0,24.0,0,false,false,false));
        HEADS.put("minecraft:creeper",new Head(8.0,8.0,18.0,0,false,false,false));
        HEADS.put("minecraft:spider",new Head(8.0,8.0,5.0,7.0,false,false,false));
        HEADS.put("minecraft:drowned",new Head(8.0,8.0,24.0,0,false,false,false));
        HEADS.put("minecraft:villager",new Head(8.0,9.0,23.0,0,false,true,false));
        HEADS.put("minecraft:zombie_villager",new Head(8.0,9.0,23.0,0,false,true,false));
        HEADS.put("minecraft:vindicator",new Head(8.0,9.0,23.0,0,false,true,false));
        HEADS.put("minecraft:evoker",new Head(8.0,9.0,23.0,0,false,false,false));
        HEADS.put("minecraft:pillager",new Head(8.0,9.0,23.0,0,false,false,false));
        HEADS.put("minecraft:illusioner",new Head(8.0,9.0,23.0,0,false,false,false));
        HEADS.put("minecraft:wandering_trader",new Head(8.0,9.0,23.0,0,false,false,false));
        HEADS.put("minecraft:witch",new Head(8.0,9.0,23.0,0,false,false,false));
        HEADS.put("minecraft:sheep",new Head(7.5,8.0,15.0,9.5,false,false,false));
        HEADS.put("minecraft:chicken",new Head(4.0,6.0,9.0,5.0,false,true,false));
        HEADS.put("minecraft:cow",new Head(7.5,8.0,16.0,10.5,false,true,false));
        HEADS.put("minecraft:mooshroom",new Head(7.5,8.0,16.0,10.5,false,true,false));
        HEADS.put("minecraft:pig",new Head(8.0,8.0,8.0,10.0,false,true,false));
        HEADS.put("minecraft:horse",new Head(10.0,10.0,26.0,16.0,false,false,false));
        HEADS.put("minecraft:skeleton_horse",new Head(10.0,10.0,26.0,16.0,false,false,false));
        HEADS.put("minecraft:donkey",new Head(7.5,8.0,20.0,13.0,false,false,false));
        HEADS.put("minecraft:mule",new Head(7.5,8.0,21.0,14.0,false,false,false));
        HEADS.put("minecraft:llama",new Head(8.0,8.0,26.0,10.0,false,false,false));
        HEADS.put("minecraft:trader_llama",new Head(8.0,8.0,26.0,10.0,false,false,false));
        HEADS.put("minecraft:polar_bear",new Head(9.0,9.0,12.0,20.0,false,false,false));
        HEADS.put("minecraft:snow_golem",new Head(10.0,10.0,20.5,0,false,false,false));
        HEADS.put("minecraft:turtle",new Head(6.0,5.0,1.0,10.0,false,false,false));
        HEADS.put("minecraft:iron_golem",new Head(8.0,10.0,33.0,3.5,false,false,false));
        HEADS.put("minecraft:phantom",new Head(6.0,3.0,1.5,6.5,false,false,true));
        HEADS.put("minecraft:hoglin",new Head(14.0,16.0,7.0,19.0,false,false,false));
        HEADS.put("minecraft:zoglin",new Head(14.0,16.0,7.0,19.0,false,false,false));
        HEADS.put("minecraft:piglin",new Head(8.0,8.0,24.0,0,true,false,false));
    }
    public static AABB headBox(LivingEntity entity){
        if(entity instanceof Player){
            AABB box=new AABB(-.25,0,-.25,.25,.5,.25);
            if(entity.isSwimming())box=box.move(0,3/16D,0).move(Vec3.directionFromRotation(entity.getXRot(),entity.yBodyRot).normalize().scale(.8));
            else box=box.move(0,(entity.isShiftKeyDown()?20:24)/16D,0);
            double scale=30/32D;
            return new AABB(box.minX*scale,box.minY*scale,box.minZ*scale,box.maxX*scale,box.maxY*scale,box.maxZ*scale);
        }
        var head=HEADS.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());return head==null?null:head.box(entity);
    }
}
