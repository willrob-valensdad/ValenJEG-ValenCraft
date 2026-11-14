package ttv.migami.jeg.legacy;

import java.lang.reflect.Proxy;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import ttv.migami.jeg.Reference;

/** Ports from MigaMiDev/Just-Enough-Guns master 71bbf39 (GPL-3.0). */
public final class LegacyContent {
    private LegacyContent() {}
    public static AmmoBoxBlock AMMO_BOX;
    public static BlockEntityType<AmmoBoxBlockEntity> AMMO_BOX_ENTITY;
    public static MenuType<AmmoBoxMenu> AMMO_BOX_MENU;
    public static BlockItem AMMO_BOX_ITEM;
    public static ExplosiveChargeItem EXPLOSIVE_CHARGE;
    public static net.minecraft.world.entity.EntityType<ExplosiveChargeEntity> CHARGE_ENTITY;
    public static net.minecraft.world.entity.EntityType<LegacyArrowEntity> ARROW_ENTITY;
    public static net.minecraft.world.entity.EntityType<WhirlpoolProjectile> WHIRLPOOL_ENTITY;
    public static net.minecraft.world.entity.EntityType<LandWhirlpool> SPLASH_ENTITY;
    public static void register() {
        CHARGE_ENTITY=entity("throwable_explosive_charge",ExplosiveChargeEntity::new,.25F,.25F);
        ARROW_ENTITY=entity("legacy_arrow",LegacyArrowEntity::new,.05F,.05F);
        WHIRLPOOL_ENTITY=entity("legacy_whirlpool",WhirlpoolProjectile::new,.05F,.05F);
        SPLASH_ENTITY=entity("legacy_splash",LandWhirlpool::new,5F,5F);
        EXPLOSIVE_CHARGE=Registry.register(BuiltInRegistries.ITEM,Reference.id("explosive_charge"),new ExplosiveChargeItem(properties("explosive_charge").stacksTo(2).rarity(net.minecraft.world.item.Rarity.EPIC)));

        AMMO_BOX = Registry.register(BuiltInRegistries.BLOCK, Reference.id("ammo_box"), new AmmoBoxBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Reference.id("ammo_box"))).strength(2.0F).sound(SoundType.METAL).noOcclusion()));
        AMMO_BOX_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.id("ammo_box"), createAmmoBoxType());
        AMMO_BOX_MENU = Registry.register(BuiltInRegistries.MENU, Reference.id("ammo_box"), new MenuType<>(AmmoBoxMenu::new, FeatureFlags.DEFAULT_FLAGS));
        AMMO_BOX_ITEM = Registry.register(BuiltInRegistries.ITEM, Reference.id("ammo_box"), new BlockItem(AMMO_BOX, properties("ammo_box").stacksTo(1)));
    }
    public static void registerNetworking() { BowTriggerPayload.register(); SpearReload.register(); }
    private static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> entity(String id, net.minecraft.world.entity.EntityType.EntityFactory<T> factory,float width,float height) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE,Reference.id(id),net.minecraft.world.entity.EntityType.Builder.of(factory,net.minecraft.world.entity.MobCategory.MISC).sized(width,height).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,Reference.id(id))));
    }
    public static Item.Properties properties(String id) { return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Reference.id(id))); }
    @SuppressWarnings("unchecked")
    private static BlockEntityType<AmmoBoxBlockEntity> createAmmoBoxType() {
        try {
            Class<?> type = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object factory = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> new AmmoBoxBlockEntity((net.minecraft.core.BlockPos) args[0], (net.minecraft.world.level.block.state.BlockState) args[1]));
            var constructor = BlockEntityType.class.getDeclaredConstructor(type, Set.class);
            constructor.setAccessible(true);
            return (BlockEntityType<AmmoBoxBlockEntity>) constructor.newInstance(factory, Set.of(AMMO_BOX));
        } catch (ReflectiveOperationException e) { throw new IllegalStateException("Cannot register original ammo box", e); }
    }
}
