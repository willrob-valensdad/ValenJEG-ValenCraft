package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.util.ProblemReporter;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.legacy.*;

/** Opt-in fixture mod only. Uses real Fabric registries/world/entities; never packaged in production. */
public final class LegacyWorldProbe {
    private static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
    private static List<ItemEntity> drops(ServerLevel level,BlockPos pos) { return level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(4)); }
    private static void clearDrops(ServerLevel level,BlockPos pos) { for(var entity:drops(level,pos))entity.discard(); }
    private static void emptyCooldown(ServerPlayer player) { for(int i=0;i<50;i++)player.getCooldowns().tick(); }
    private static ItemStack item(String id) { return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id))); }
    private static LivingEntity target(ServerLevel level,Vec3 pos,List<Entity> owned) {
        var entity=EntityTypes.IRON_GOLEM.create(level,EntitySpawnReason.TRIGGERED);
        check(entity!=null,"Native iron golem factory failed");entity.setPos(pos);entity.setNoAi(true);check(level.addFreshEntity(entity),"Native target spawn failed");owned.add(entity);return entity;
    }
    public static void run(ServerLevel level,ServerPlayer player,JsonObject receipt) throws Exception {
        if(!Boolean.getBoolean("valen.legacy.probe")) { receipt.addProperty("legacy_probe","disabled");return; }
        JsonArray passed=new JsonArray();receipt.add("legacy_passed",passed);
        BlockPos pos=new BlockPos(16,240,16);Map<BlockPos,BlockState> old=new HashMap<>();
        List<Entity> owned=new ArrayList<>();Vec3 previous=player.position();var previousConnection=player.connection;
        // Real native disconnected listener accepts queued animation/menu packets without opening a socket.
        if(player.connection==null)player.connection=new ServerGamePacketListenerImpl(level.getServer(),new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(player.getGameProfile(),false));
        try {
            for(BlockPos at:List.of(pos,pos.below(),pos.below(2),pos.offset(8,0,0),pos.offset(12,0,0),pos.offset(12,1,0),pos.offset(12,2,0)))old.put(at,level.getBlockState(at));
            player.setPos(Vec3.atCenterOf(pos).add(2,0,0));player.getInventory().clearContent();
            level.setBlock(pos,LegacyContent.AMMO_BOX.defaultBlockState(),3);
            AmmoBoxBlockEntity box=(AmmoBoxBlockEntity)level.getBlockEntity(pos);
            check(box!=null,"Ammo box BE missing in native world");
            var rejected=HopperBlockEntity.addItem(null,box,new ItemStack(Items.DIAMOND,2),Direction.UP);
            check(rejected.getCount()==2&&box.isEmpty(),"Real hopper insertion bypassed filter");
            check(HopperBlockEntity.addItem(null,box,new ItemStack(Items.ARROW,9),Direction.UP).isEmpty(),"Real hopper rejected arrow");
            level.setBlock(pos.below(),Blocks.HOPPER.defaultBlockState(),3);
            var hopper=(HopperBlockEntity)level.getBlockEntity(pos.below());
            check(HopperBlockEntity.suckInItems(level,hopper)&&hopper.getItem(0).is(Items.ARROW)&&box.getItem(0).getCount()==8,"Actual hopper extraction lost/doubled ammo");
            check(box.getBlockState().getAnalogOutputSignal(level,pos,Direction.NORTH)>0,"Native comparator empty with ammo");
            AmmoBoxMenu menu=new AmmoBoxMenu(27,player.getInventory(),box);
            check(menu.slots.size()==51&&menu.stillValid(player),"Ammo UI slot count/authority mismatch");
            player.getInventory().setItem(9,new ItemStack(Items.DIAMOND,3));
            check(menu.quickMoveStack(player,15).isEmpty()&&player.getInventory().getItem(9).getCount()==3,"Shift transfer bypassed ammo filter");
            check(menu.quickMoveStack(player,0).is(Items.ARROW)&&box.isEmpty(),"Ammo shift extraction uses hidden27slot bounds");
            menu.removed(player);box.setItem(0,new ItemStack(Items.ARROW,9));
            ItemStack named=new ItemStack(LegacyContent.AMMO_BOX_ITEM);named.set(DataComponents.CUSTOM_NAME,Component.literal("Fixture ammo"));box.applyComponentsFromItemStack(named);box.setItem(0,new ItemStack(Items.ARROW,9));
            clearDrops(level,pos);check(level.destroyBlock(pos,true,player),"Survival break failed");
            var packed=drops(level,pos).stream().filter(e->e.getItem().is(LegacyContent.AMMO_BOX_ITEM)).toList();
            check(packed.size()==1&&packed.get(0).getItem().getCount()==1,"Survival ammo box drop duplicated/missing");
            ItemStack carried=packed.get(0).getItem().copy();clearDrops(level,pos);
            level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);player.getInventory().clearContent();player.setItemInHand(InteractionHand.MAIN_HAND,carried);
            var use=new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0,.5,0),Direction.UP,pos.below(),false));
            carried.useOn(use);
            check(level.getBlockEntity(pos) instanceof AmmoBoxBlockEntity,"Native BlockItem placement failed");box=(AmmoBoxBlockEntity)level.getBlockEntity(pos);
            check(box.getItem(0).getCount()==9&&Component.literal("Fixture ammo").equals(box.getCustomName()),"Native break/place lost contents/name");
            passed.add("Actual hopper insert/reject/extract;51-slot menu/shift filter; comparator; survival break->native BlockItem placement retains9arrows/name exactly once");
            var legacy=NonNullList.withSize(27,ItemStack.EMPTY);legacy.set(15,new ItemStack(Items.DIAMOND,7));legacy.set(26,new ItemStack(Items.SPECTRAL_ARROW,11));
            var stack=new ItemStack(LegacyContent.AMMO_BOX_ITEM);stack.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(legacy));box.applyComponentsFromItemStack(stack);
            clearDrops(level,pos);box.releaseLegacyOverflow();int first=drops(level,pos).stream().mapToInt(e->e.getItem().getCount()).sum();box.releaseLegacyOverflow();int second=drops(level,pos).stream().mapToInt(e->e.getItem().getCount()).sum();
            check(first==18&&second==18,"Legacy overflow first-open release lost/duplicated items");clearDrops(level,pos);
            // Player's gameMode() drives actual creative path without changing server player-list state.
            ServerPlayer creative=new ServerPlayer(level.getServer(),level,new GameProfile(UUID.randomUUID(),"ValenLegacyCreative"),ClientInformation.createDefault()) { @Override public GameType gameMode(){return GameType.CREATIVE;} @Override public void sendSystemMessage(Component c){} };
            creative.connection=player.connection;box.setItem(0,new ItemStack(Items.ARROW,5));
            LegacyContent.AMMO_BOX.playerWillDestroy(level,pos,box.getBlockState(),creative);
            check(drops(level,pos).stream().filter(e->e.getItem().is(LegacyContent.AMMO_BOX_ITEM)).count()==1,"Creative break lost/duplicated packed ammo");clearDrops(level,pos);creative.discard();
            passed.add("Legacy hidden15/26 overflow18items released exactly once; native creative destruction preserves packed nonempty box");

            player.getInventory().clearContent();player.setPos(Vec3.atCenterOf(pos).add(0,1,0));
            ItemStack bowStack=item("jeg:compound_bow");player.setItemInHand(InteractionHand.MAIN_HAND,bowStack);player.getInventory().setItem(1,new ItemStack(Items.ARROW,3));emptyCooldown(player);
            CompoundBowItem bow=(CompoundBowItem)bowStack.getItem();
            var enchantments=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            var flame=enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME);
            var power=enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.POWER);
            check(bowStack.isEnchantable() && bowStack.get(DataComponents.ENCHANTABLE).value()==5,"Original5 enchantability absent");
            check(flame.value().isSupportedItem(bowStack)&&bow.canBeEnchantedWith(bowStack,flame,net.fabricmc.fabric.api.item.v1.EnchantingContext.PRIMARY),"Native Flame table support absent");
            check(!bow.canBeEnchantedWith(bowStack,power,net.fabricmc.fabric.api.item.v1.EnchantingContext.PRIMARY),"Native bow tag accidentally enabled unsupported Power");
            int arrows=player.getInventory().getItem(1).getCount();
            check(!bow.tryShoot(level,player,InteractionHand.MAIN_HAND),"Compound bow fired generic gun bullet on press");
            check(!bow.releaseUsing(bowStack,level,player,72000)&&player.getInventory().getItem(1).getCount()==arrows,"Zero-charge bow consumed ammunition");
            check(bow.releaseUsing(bowStack,level,player,71980)&&player.getInventory().getItem(1).getCount()==arrows-1,"Full charge did not consume one arrow");
            var arrowsOut=level.getEntitiesOfClass(LegacyArrowEntity.class,new AABB(pos).inflate(10));check(arrowsOut.size()==1&&Math.abs(arrowsOut.get(0).getDeltaMovement().length()-8)<.001,"Original arrow spawn/speed changed");for(var e:arrowsOut)e.discard();
            check(!bow.releaseUsing(bowStack,level,player,71980),"Bow bypassed native10tick cooldown");emptyCooldown(player);
            // Actual collision flight, not direct invocation of a mocked hit callback.
            LivingEntity body=target(level,player.position().add(0,0,4),owned);
            var half=new LegacyArrowEntity(level,player,bowStack,.5F);half.setDeltaMovement(body.position().add(0,.5,0).subtract(half.position()).normalize().scale(8));check(level.addFreshEntity(half),"Half-charge arrow spawn failed");owned.add(half);float hp=body.getHealth();half.tick();
            check(!half.isAlive()&&Math.abs(body.getHealth()-(hp-5))<.01,"Original half-charge/body collision damage not5");body.discard();
            LivingEntity head=target(level,player.position().add(0,0,4),owned);var full=new LegacyArrowEntity(level,player,bowStack,1F);full.setDeltaMovement(head.getEyePosition().subtract(full.position()).normalize().scale(8));level.addFreshEntity(full);owned.add(full);hp=head.getHealth();full.tick();
            check(!full.isAlive()&&Math.abs(head.getHealth()-(hp-25))<.01,"Original charged headshot collision damage not25");head.discard();
            ItemStack flameBow=bowStack.copy();flameBow.enchant(flame,1);
            LivingEntity burning=target(level,player.position().add(0,0,4),owned);var fireArrow=new LegacyArrowEntity(level,player,flameBow,1F);fireArrow.setDeltaMovement(burning.position().add(0,.5,0).subtract(fireArrow.position()).normalize().scale(8));level.addFreshEntity(fireArrow);owned.add(fireArrow);fireArrow.tick();check(burning.isOnFire(),"Flame arrow collision didnotignitefor5seconds");burning.discard();
            passed.add("Original5 enchantability, actual native Flame supported/Power denied, native enchanted arrow collision ignites target");
            // Original ProjectileEntity moves first, adds-.04 gravity afterward, has no snowball air/water inertia.
            var trajectory=new LegacyArrowEntity(level,player,bowStack,1F);trajectory.setPos(Vec3.atCenterOf(pos).add(0,16,0));trajectory.setDeltaMovement(8,0,0);owned.add(trajectory);level.addFreshEntity(trajectory);Vec3 origin=trajectory.position();
            trajectory.tick();check(trajectory.position().distanceTo(origin.add(8,0,0))<.00001&&trajectory.getDeltaMovement().distanceTo(new Vec3(8,-.04,0))<.00001,"Arrow original firsttick/order/drag differs");
            trajectory.tick();check(trajectory.position().distanceTo(origin.add(16,-.04,0))<.00001&&trajectory.getDeltaMovement().distanceTo(new Vec3(8,-.08,0))<.00001,"Arrow original secondtick/order/drag differs");trajectory.discard();
            BlockPos water=pos.offset(12,1,0);level.setBlock(water,Blocks.WATER.defaultBlockState(),3);level.setBlock(water.above(),Blocks.WATER.defaultBlockState(),3);level.setBlock(water.below(),Blocks.WATER.defaultBlockState(),3);
            var underwaterArrow=new LegacyArrowEntity(level,player,bowStack,1F);underwaterArrow.setPos(Vec3.atCenterOf(water));underwaterArrow.setDeltaMovement(0,0,8);owned.add(underwaterArrow);level.addFreshEntity(underwaterArrow);origin=underwaterArrow.position();underwaterArrow.tick();
            check(underwaterArrow.position().distanceTo(origin.add(0,0,8))<.00001&&underwaterArrow.getDeltaMovement().distanceTo(new Vec3(0,-.04,8))<.00001,"Arrow inherited native0.8waterdrag");underwaterArrow.discard();
            var waterBeam=new WhirlpoolProjectile(LegacyContent.WHIRLPOOL_ENTITY,level);waterBeam.setPos(Vec3.atCenterOf(water));waterBeam.setDeltaMovement(0,0,3);owned.add(waterBeam);level.addFreshEntity(waterBeam);origin=waterBeam.position();waterBeam.tick();
            check(waterBeam.position().distanceTo(origin.add(0,0,3))<.00001&&waterBeam.getDeltaMovement().distanceTo(new Vec3(0,0,3))<.00001,"Water beam inherited nativegravity/waterdrag");waterBeam.discard();
            passed.add("Original arrow move-before-.04gravity,8speed/noairdrag and no0.8waterdrag; waterbeam3speed/nogravity/nodrag under actual water blocks");
            passed.add("Native bow release requirescharge, consumes1arrow, spawns8speed arrow, honors10tick cooldown; actual half/body5 and full/head25 collision damage");

            BlockPos chargePos=pos.offset(8,0,0);level.setBlock(chargePos,Blocks.DIRT.defaultBlockState(),3);
            var flying=new ExplosiveChargeEntity(LegacyContent.CHARGE_ENTITY,level);flying.setOwner(player);flying.setPos(Vec3.atCenterOf(chargePos).add(0,8,0));level.addFreshEntity(flying);owned.add(flying);for(int i=0;i<5;i++)flying.tick();check(!flying.landed()&&flying.getFuse()==200,"Charge cooked before landing");flying.discard();
            var charge=new ExplosiveChargeEntity(LegacyContent.CHARGE_ENTITY,level);charge.setOwner(player);charge.setPos(Vec3.atCenterOf(chargePos).add(0,1,0));charge.setDeltaMovement(0,-.2,0);level.addFreshEntity(charge);owned.add(charge);
            for(int i=0;i<30&&!charge.landed();i++)charge.tick();check(charge.landed()&&charge.getFuse()>190,"Sticky charge never landed/started200tickfuse");
            double stickyY=charge.getY();level.setBlock(chargePos,Blocks.AIR.defaultBlockState(),3);for(int i=0;i<3;i++)charge.tick();check(charge.getY()<stickyY&&charge.getDeltaMovement().y<0,"Original landed charge didnotfallafterallsupportremoved");level.setBlock(chargePos,Blocks.DIRT.defaultBlockState(),3);
            for(int i=0;i<50;i++)charge.tick();int remaining=charge.getFuse();
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());charge.save(out);var restored=new ExplosiveChargeEntity(LegacyContent.CHARGE_ENTITY,level);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));restored.setUUID(UUID.randomUUID());charge.discard();level.addFreshEntity(restored);owned.add(restored);
            check(restored.landed()&&restored.getFuse()==remaining,"Charge native entitysave/load reset timer/stickiness");for(int i=0;i<remaining-1;i++)restored.tick();check(restored.isAlive()&&restored.getFuse()==1,"Charge exploded before landed200ticks");restored.tick();check(!restored.isAlive(),"Charge didnotdetonate atzero");
            passed.add("Original sticky charge resumesgravityafterallsupportremoved");
            passed.add("Actual thrown charge holds200fuse inair, sticksblock, native entitysave/load retainslandedfuse, explodesat200landedticks");

            var wetCharge=new ExplosiveChargeEntity(LegacyContent.CHARGE_ENTITY,level);wetCharge.setPos(Vec3.atCenterOf(chargePos).add(0,1,0));level.addFreshEntity(wetCharge);owned.add(wetCharge);
            var splash=new LandWhirlpool(LegacyContent.SPLASH_ENTITY,level);splash.setOwner(player);splash.setPos(wetCharge.position());level.addFreshEntity(splash);owned.add(splash);clearDrops(level,chargePos);splash.tick();
            check(!wetCharge.isAlive()&&drops(level,chargePos).stream().filter(e->e.getItem().is(LegacyContent.EXPLOSIVE_CHARGE)).mapToInt(e->e.getItem().getCount()).sum()==1,"Whirlpool faileddefuse/refundcharge");for(int i=0;i<70;i++)splash.tick();check(!splash.isAlive(),"Original70tickSplash lifetimechanged");clearDrops(level,chargePos);
            var area=new LandWhirlpool(LegacyContent.SPLASH_ENTITY,level);area.setOwner(player);area.setPos(Vec3.atCenterOf(chargePos).add(0,1,0));level.addFreshEntity(area);owned.add(area);
            LivingEntity heavy=target(level,area.position().add(.5,0,0),owned);var blaze=EntityTypes.BLAZE.create(level,EntitySpawnReason.TRIGGERED);check(blaze!=null,"Native Blaze create failed");blaze.setPos(area.position().add(-.5,0,0));blaze.setNoAi(true);level.addFreshEntity(blaze);owned.add(blaze);float heavyHp=heavy.getHealth(),fireHp=blaze.getHealth();for(int i=0;i<9;i++)area.tick();
            check(Math.abs(heavy.getHealth()-(heavyHp-2.5F))<.01&&Math.abs(blaze.getHealth()-(fireHp-5F))<.01,"Native waterarea original2.5/5firetagdamage changed");
            check(heavy.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)&&heavy.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS).getAmplifier()==3,"Native waterarea missing originalSlowIV");area.discard();heavy.discard();blaze.discard();
            passed.add("Actual native Splash frame3 damage2.5Golem/5taggedBlaze andSlowIV40ticks");
            var spearStack=item("jeg:atlantean_spear");var spear=(AtlanteanSpearItem)spearStack.getItem();player.setItemInHand(InteractionHand.MAIN_HAND,spearStack);spearStack.set(ModDataComponents.GUN_AMMO.get(),6);emptyCooldown(player);
            check(spear.tryShoot(level,player,InteractionHand.MAIN_HAND)&&spear.getMagazineAmmo(spearStack)==5,"Native spear shot didnotconsume1of6waterbombs");
            var projectiles=level.getEntitiesOfClass(WhirlpoolProjectile.class,new AABB(pos).inflate(12));check(projectiles.size()==1&&Math.abs(projectiles.get(0).getDeltaMovement().length()-3)<.001,"Spear didnotspawnoriginal3speedcustomwaterprojectile");for(var e:projectiles){owned.add(e);e.discard();}
            passed.add("LandWhirlpool actualcollision defuses/refunds1charge andexpires70ticks; spear nativeshoot consumes1of6waterbombs andspawns3speedcustomprojectile");
            var fireTag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,Identifier.parse("jeg:fire"));
            check(EntityTypes.BLAZE.builtInRegistryHolder().is(fireTag)&&EntityTypes.ENDERMAN.builtInRegistryHolder().is(fireTag)&&!EntityTypes.IRON_GOLEM.builtInRegistryHolder().is(fireTag),"Original fire entity membership missing/expanded");
            passed.add("Native fire tag preserves original Blaze/Enderman membership with Golem negativecontrol");
            SpearReloadNativeChecks.run(level,player,receipt);
            receipt.addProperty("legacy_status","pass");
        } catch(Throwable failure) { receipt.addProperty("legacy_status","fail");receipt.addProperty("legacy_error",failure.toString());if(failure instanceof Exception exception)throw exception;throw new AssertionError(failure); }
        finally { for(var e:owned)if(e.isAlive())e.discard();for(var at:old.keySet())clearDrops(level,at);for(var entry:old.entrySet()){if(level.getBlockEntity(entry.getKey()) instanceof net.minecraft.world.Container container)container.clearContent();level.setBlock(entry.getKey(),entry.getValue(),3);}player.getInventory().clearContent();player.setPos(previous);player.connection=previousConnection; }
    }
}
