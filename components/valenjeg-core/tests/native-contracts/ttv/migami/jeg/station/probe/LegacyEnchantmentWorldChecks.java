package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.enchantment.*;
import ttv.migami.jeg.entity.BulletEntity;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.mteg.*;

/** Fixture-only actual native registry/anvil/barter/factory/physics checks.
 * Root owns execution; does not claim connected-client/UI/GPU acceptance. */
public final class LegacyEnchantmentWorldChecks {
    private LegacyEnchantmentWorldChecks(){}
    private static void check(boolean value,String message){LegacyEnchantmentNativeContracts.check(value,message);}
    private static ItemStack item(String id){return LegacyEnchantmentNativeContracts.item(id);}
    private static Mob target(ServerLevel level,Vec3 pos,List<Entity> owned){
        var mob=(Mob)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:iron_golem")).create(level,EntitySpawnReason.TRIGGERED);
        check(mob!=null,"Native IronGolem create failed");mob.setNoAi(true);mob.setPos(pos);level.addFreshEntity(mob);owned.add(mob);return mob;
    }
    private static MtegProjectile fire(ServerLevel level,ServerPlayer player,ItemStack weapon,Vec3 origin,List<Entity> owned){
        Set<UUID> before=new HashSet<>();for(var p:level.getEntitiesOfClass(MtegProjectile.class,new AABB(origin,origin).inflate(32)))before.add(p.getUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND,weapon);
        ((MtegGunItem)weapon.getItem()).fireDirectionallyFrom(level,player,weapon,origin,new Vec3(1,0,0));
        var fresh=level.getEntitiesOfClass(MtegProjectile.class,new AABB(origin,origin).inflate(32),p->p.getOwner()==player&&!before.contains(p.getUUID()));
        check(fresh.size()==1,"Actual one-pellet AWP factory did not spawn one owned projectile:"+fresh.size());owned.addAll(fresh);return fresh.getFirst();
    }
    private static void cleanup(List<Entity> owned){for(var entity:owned)entity.discard();owned.clear();}
    private static void anvil(ServerLevel level,ServerPlayer player,ItemStack first,ItemStack second,boolean expected,String label){
        var menu=new AnvilMenu(997,player.getInventory(),ContainerLevelAccess.create(level,player.blockPosition()));
        try{menu.getSlot(0).set(first);menu.getSlot(1).set(second);menu.createResult();check(!menu.getSlot(2).getItem().isEmpty()==expected,label);}
        finally{menu.getSlot(0).set(ItemStack.EMPTY);menu.getSlot(1).set(ItemStack.EMPTY);menu.getSlot(2).set(ItemStack.EMPTY);}
    }
    public static void run(ServerLevel level,ServerPlayer player,JsonObject receipt){
        if(!Boolean.getBoolean("valen.enchantments.probe")){receipt.addProperty("legacy_enchantment_status","disabled");return;}
        LegacyEnchantmentNativeContracts.run(level,receipt);
        var registry=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var puncturing=registry.getOrThrow(LegacyGunEnchantments.PUNCTURING);
        var collateral=registry.getOrThrow(LegacyGunEnchantments.COLLATERAL);
        var fireStarter=registry.getOrThrow(LegacyGunEnchantments.FIRE_STARTER);
        List<Entity> owned=new ArrayList<>();Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        var position=player.position();float yaw=player.getYRot(),pitch=player.getXRot();boolean creative=player.getAbilities().instabuild;
        var held=player.getMainHandItem().copy();boolean allowFire=MtegConfig.SET_FIRE_TO_BLOCKS.get(),allowDestroy=Config.BULLET_BLOCK_DESTRUCTION_ENABLED.get();
        JsonArray checks=new JsonArray();receipt.add("legacy_enchantment_world_checks",checks);
        Vec3 origin=position.add(0,20,24);
        try{
            player.getAbilities().instabuild=false;player.setPos(origin.add(-1,-1.62,0));player.setYRot(-90);player.setXRot(0);player.setOldPosAndRot();
            anvil(level,player,item("mteg:awp"),EnchantmentHelper.createBook(new EnchantmentInstance(puncturing,4)),true,"Actual anvil refused PuncturingIV book on gun");
            var already=item("mteg:awp");already.enchant(puncturing,1);
            anvil(level,player,already,EnchantmentHelper.createBook(new EnchantmentInstance(fireStarter,1)),false,"Actual anvil combined incompatible PROJECTILE enchants");
            anvil(level,player,item("jeg:compound_bow"),EnchantmentHelper.createBook(new EnchantmentInstance(fireStarter,1)),false,"Actual anvil accepted FireStarter on original bow");
            checks.add("Actual AnvilMenu accepts original gun book and rejects PROJECTILE conflict/bowFireStarter, no item/XP consumption");
            int normal=0,critical=0;
            for(int n=0;n<256&&(normal==0||critical==0);n++){
                var mob=target(level,origin.add(5,-.3,0),owned);var gun=item("mteg:awp");gun.enchant(puncturing,4);
                var projectile=fire(level,player,gun,origin,owned);
                check(projectile.legacyEnchantments().puncturing()==4&&Math.abs(projectile.legacyEnchantments().criticalChance()-.2F)<.000001,"Factory lost source PuncturingIV snapshot");
                gun.set(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY);player.setItemInHand(InteractionHand.MAIN_HAND,item("minecraft:stick"));
                var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());projectile.save(output);
                var restored=new MtegProjectile(MtegContent.BULLET_ENTITY,level);owned.add(restored);
                restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),output.buildResult()));
                check(restored.legacyEnchantments().equals(projectile.legacyEnchantments()),"Actual projectile native save/load lost frozen enchantment levels/chance");
                float health=mob.getHealth();projectile.tick();float damage=health-mob.getHealth();
                check(Math.abs(damage-20)<.0001||Math.abs(damage-30)<.0001,"Actual Puncturing hit damage differs:"+damage);
                if(Math.abs(damage-30)<.0001)critical++;else normal++;cleanup(owned);
            }
            check(normal>0&&critical>0,"Native PuncturingIV hits failed to exercise20/30 damage within bounded256 shots");
            receipt.addProperty("legacy_actual_critical_hits",critical);receipt.addProperty("legacy_actual_normal_hits",normal);
            checks.add("Actual MTEG gun factory: native20/30damage at PuncturingIV; later held-stack mutation ignored; actual projectile native save/load retains shot snapshot");
            for(boolean collat:List.of(false,true)){
                var first=target(level,origin.add(4,-.3,0),owned);var second=target(level,origin.add(12,-.3,0),owned);var gun=item("mteg:awp");if(collat)gun.enchant(collateral,1);
                var projectile=fire(level,player,gun,origin,owned);float a=first.getHealth(),b=second.getHealth();projectile.tick();
                check(first.getHealth()<a&&(second.getHealth()<b)==collat,"MTEG exact same-segment Collateral/default closest-hit selection differs");
                check(!projectile.isAlive(),"MTEG5322334 Collateral silently changed to master continuation");cleanup(owned);
            }
            checks.add("Actual MTEG5322334 same-segment two-target Collateral and unenchanted single-target control; both retain original segment removal");
            var first=target(level,origin.add(3,-.3,0),owned);var second=target(level,origin.add(8,-.3,0),owned);
            ItemStack modern=item("jeg:assault_rifle");modern.enchant(collateral,1);player.setItemInHand(InteractionHand.MAIN_HAND,modern);
            var modernGun=(GunItem)modern.getItem();var bullet=GunItem.createBullet(level,player,modern,modernGun.getStats(),new Vec3(5,0,0));bullet.initialisePosition(origin);level.addFreshEntity(bullet);owned.add(bullet);
            float firstHp=first.getHealth(),secondHp=second.getHealth();bullet.tick();check(first.getHealth()<firstHp&&second.getHealth()==secondHp&&bullet.isAlive(),"Native JEG master first-tick Collateral continuation failed");
            bullet.tick();check(second.getHealth()<secondHp&&bullet.isAlive(),"Native JEG master later-tick Collateral continuation failed");cleanup(owned);
            checks.add("Actual native JEG bullet factory/vehicle-aware collision adapter: Collateral survives first hit and reaches next entity on a later tick");
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            ItemStack modernSnapshot=item("jeg:assault_rifle");modernSnapshot.enchant(puncturing,4);
            var savedBullet=GunItem.createBullet(level,player,modernSnapshot,((GunItem)modernSnapshot.getItem()).getStats(),new Vec3(5,0,0));owned.add(savedBullet);
            savedBullet.save(saved);var restoredBullet=new BulletEntity(ttv.migami.jeg.init.ModEntities.BULLET.get(),level);owned.add(restoredBullet);
            restoredBullet.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(savedBullet.legacyEnchantments().equals(restoredBullet.legacyEnchantments()),"Native JEG bullet snapshot serialization failed");cleanup(owned);
            ItemStack bow=item("jeg:compound_bow");bow.enchant(collateral,1);
            first=target(level,origin.add(3,-.3,0),owned);second=target(level,origin.add(6,-.3,0),owned);
            var arrow=new ttv.migami.jeg.legacy.LegacyArrowEntity(level,player,bow,1);arrow.setPos(origin);level.addFreshEntity(arrow);owned.add(arrow);
            firstHp=first.getHealth();secondHp=second.getHealth();arrow.tick();
            check(first.getHealth()<firstHp&&second.getHealth()<secondHp&&arrow.isAlive(),"Actual original bow Collateral did not reach two targets and survive");
            saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());arrow.save(saved);
            var restoredArrow=new ttv.migami.jeg.legacy.LegacyArrowEntity(ttv.migami.jeg.legacy.LegacyContent.ARROW_ENTITY,level);owned.add(restoredArrow);
            restoredArrow.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(arrow.legacyEnchantments().equals(restoredArrow.legacyEnchantments()),"Native original bow snapshot serialization failed");cleanup(owned);
            ItemStack spear=item("jeg:atlantean_spear");spear.enchant(fireStarter,1);player.setItemInHand(InteractionHand.MAIN_HAND,spear);
            first=target(level,origin.add(3,-.3,0),owned);
            ((GunItem)spear.getItem()).fireDirectionallyFrom(level,player,spear,origin,new Vec3(1,0,0));
            var water=level.getEntitiesOfClass(ttv.migami.jeg.legacy.WhirlpoolProjectile.class,new AABB(origin,origin).inflate(16),entity->entity.getOwner()==player);
            check(water.size()==1,"Actual original spear factory did not spawn one water projectile");var whirlpool=water.getFirst();owned.add(whirlpool);
            check(whirlpool.legacyEnchantments().fireStarter()==1,"Actual original spear factory lost snapshot");whirlpool.tick();
            check(first.getRemainingFireTicks()==40,"Original spear common FireStarter impact missing");
            owned.addAll(level.getEntitiesOfClass(ttv.migami.jeg.legacy.LandWhirlpool.class,new AABB(origin,origin).inflate(16),entity->entity.getOwner()==player));cleanup(owned);
            ItemStack guided=item("jeg:javelin");guided.enchant(fireStarter,1);
            var missile=new ttv.migami.jeg.vehicle.projectile.VehicleMissileEntity(level,player,null,null,origin,new Vec3(1,0,0),Identifier.parse("jeg:javelin"),false).withLegacyEnchantments(LegacyGunEnchantments.capture(guided));owned.add(missile);
            saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());missile.save(saved);
            @SuppressWarnings("unchecked") var missileType=(EntityType<ttv.migami.jeg.vehicle.projectile.VehicleMissileEntity>)missile.getType();
            var restoredMissile=new ttv.migami.jeg.vehicle.projectile.VehicleMissileEntity(missileType,level);owned.add(restoredMissile);
            restoredMissile.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(missile.legacyEnchantments().equals(restoredMissile.legacyEnchantments()),"Native guided missile snapshot serialization failed");cleanup(owned);
            checks.add("Actual native JEG bullet/bow snapshot serialization, bow two-target continuation, spear factory common FireStarter impact; existing typed missile snapshot serialization without changing blast/flight");

            // A solid wall clips the collected MTEG segment before the rear target.
            BlockPos wall=BlockPos.containing(origin.add(8,0,0));blocks.putIfAbsent(wall,level.getBlockState(wall));level.setBlock(wall,Blocks.STONE.defaultBlockState(),3);
            first=target(level,origin.add(4,-.3,0),owned);second=target(level,origin.add(12,-.3,0),owned);ItemStack gun=item("mteg:awp");gun.enchant(collateral,1);
            var projectile=fire(level,player,gun,origin,owned);firstHp=first.getHealth();secondHp=second.getHealth();projectile.tick();
            check(first.getHealth()<firstHp&&second.getHealth()==secondHp,"Collateral passed a solid wall");cleanup(owned);level.setBlock(wall,blocks.get(wall),3);
            checks.add("Actual solid-wall ray clipping prevents Collateral rear-target damage");
            wall=BlockPos.containing(origin.add(6,0,0));var firePos=wall.west();for(var p:List.of(wall,firePos,firePos.below()))blocks.putIfAbsent(p,level.getBlockState(p));
            level.setBlock(wall,Blocks.STONE.defaultBlockState(),3);level.setBlock(firePos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(firePos,Blocks.AIR.defaultBlockState(),3);
            Config.BULLET_BLOCK_DESTRUCTION_ENABLED.set(false);
            for(boolean enabled:List.of(true,false)){
                MtegConfig.SET_FIRE_TO_BLOCKS.set(enabled);gun=item("mteg:awp");gun.enchant(fireStarter,1);projectile=fire(level,player,gun,origin,owned);projectile.tick();
                check((level.getBlockState(firePos).getBlock() instanceof BaseFireBlock)==enabled,"FireStarter block gate coupled to block destruction or ignored:"+enabled);
                cleanup(owned);level.setBlock(firePos,Blocks.AIR.defaultBlockState(),3);
            }
            level.setBlock(wall,blocks.get(wall),3);
            first=target(level,origin.add(4,-.3,0),owned);gun=item("mteg:awp");gun.enchant(fireStarter,1);projectile=fire(level,player,gun,origin,owned);projectile.tick();
            check(first.getRemainingFireTicks()==40,"Entity FireStarter2seconds was disabled with independent block-fire setting");cleanup(owned);
            checks.add("Actual FireStarter block placement while native block destruction=false, independent disabled block-fire control, entity40fireticks remain active");
            var piglin=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:piglin")).create(level,EntitySpawnReason.TRIGGERED);check(piglin!=null,"Native Piglin create failed");owned.add(piglin);
            var params=new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY,piglin).create(LootContextParamSets.PIGLIN_BARTER);
            var loot=level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("minecraft:gameplay/piglin_bartering")));
            int books=0,other=0;
            for(int seed=1;seed<=2048;seed++)for(ItemStack result:loot.getRandomItems(params,seed)){
                int enchant=EnchantmentHelper.getEnchantmentsForCrafting(result).getLevel(fireStarter);
                if(enchant>0){check(enchant==1&&BuiltInRegistries.ITEM.getKey(result.getItem()).toString().equals("minecraft:enchanted_book"),"Barter granted a fake FireStarter item/level");books++;}else other++;
            }
            check(books>0&&other>=2048,"Actual native piglin table lost appended FireStarter books or replaced base loot");
            receipt.addProperty("legacy_actual_barter_books_2048",books);checks.add("Actual native piglin loot table2048 seeded contexts appends real stored-enchantment FireStarterI books and preserves vanilla rewards");
            receipt.addProperty("legacy_enchantment_status","pass");
        }finally{
            cleanup(owned);for(var entry:blocks.entrySet())level.setBlock(entry.getKey(),entry.getValue(),3);
            MtegConfig.SET_FIRE_TO_BLOCKS.set(allowFire);Config.BULLET_BLOCK_DESTRUCTION_ENABLED.set(allowDestroy);
            player.setItemInHand(InteractionHand.MAIN_HAND,held);player.getAbilities().instabuild=creative;player.setPos(position);player.setYRot(yaw);player.setXRot(pitch);player.setOldPosAndRot();
        }
    }
}
