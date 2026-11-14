package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Attachments;
import top.ribs.scguns.common.GunModifiers;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.*;

import top.ribs.scguns.item.animated.*;
import top.ribs.scguns.item.attachment.impl.Barrel;
import top.ribs.scguns.item.attachment.impl.Magazine;
import top.ribs.scguns.item.attachment.impl.Stock;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;
import top.ribs.scguns.item.exosuit.*;

import java.lang.reflect.Constructor;

import top.ribs.scguns.compat.ArmorItem;
import top.ribs.scguns.compat.ArmorMaterials;
import net.minecraft.world.item.SpawnEggItem;
public class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Reference.MOD_ID);

    private static ResourceKey<JukeboxSong> jukeboxSong(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(Reference.MOD_ID, name));
    }

    public static DeferredItem<Item> ANTHRALITE_KNIFE;
    public static DeferredItem<GunItem> GALE;
    public static DeferredItem<GunItem> UMAX_PISTOL;
    public static DeferredItem<Item> VENTURI;
    public static DeferredItem<Item> SCRATCHES;

    public static void registerItems() {
        SCRATCHES = REGISTER.registerItem("scratches", props -> {
            Item.Properties properties = props.stacksTo(1).durability(1400);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "scratches",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "scratches",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });
        GALE = REGISTER.registerItem("gale", props -> {
            Item.Properties properties = props.stacksTo(1).durability(1400);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "gale",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "gale",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });
        UMAX_PISTOL = REGISTER.registerItem("umax_pistol", props -> {
            Item.Properties properties = props.stacksTo(1).durability(400);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "umax_pistol",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "umax_pistol",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });


        VENTURI = REGISTER.registerItem("venturi", props -> {
            Item.Properties properties = props.stacksTo(1).durability(800);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "venturi",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "venturi",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });


        if (ScorchedGuns.farmersDelightLoaded) {
            ANTHRALITE_KNIFE = REGISTER.registerItem("anthralite_knife", ModItems::createAnthraliteKnife);
        }
    }

    private static Item createAnthraliteKnife(Item.Properties properties) {
        Item.Properties knifeProperties = properties.sword(ModTiers.ANTHRALITE, 0.5F, -2.0F);
        try {
            Class<?> knifeItemClass = Class.forName("vectorwing.farmersdelight.common.item.KnifeItem");
            Constructor<?> currentConstructor = findConstructor(knifeItemClass, ToolMaterial.class, Item.Properties.class);
            if (currentConstructor != null) {
                return (Item) currentConstructor.newInstance(ModTiers.ANTHRALITE, knifeProperties);
            }

            Constructor<?> legacyConstructor = findConstructor(knifeItemClass, ToolMaterial.class, float.class, float.class, Item.Properties.class);
            if (legacyConstructor != null) {
                return (Item) legacyConstructor.newInstance(ModTiers.ANTHRALITE, 0.5F, -2.0F, knifeProperties);
            }

            ScorchedGuns.LOGGER.warn("Farmer's Delight KnifeItem constructor shape is unknown; using SwordItem fallback for Anthralite Knife.");
        } catch (ReflectiveOperationException | LinkageError e) {
            ScorchedGuns.LOGGER.warn("Could not create Farmer's Delight KnifeItem for Anthralite Knife; using SwordItem fallback.", e);
        }
        return new Item(knifeProperties);
    }

    private static Constructor<?> findConstructor(Class<?> itemClass, Class<?>... parameterTypes) {
        try {
            return itemClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


    public static final DeferredItem<AnimatedGunItem> M3_CARABINE = REGISTER.registerItem("m3_carabine",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "m3_carabine",                               // Model path
                    ModSounds.MAG_OUT.get(),        // Reload sound mag out
                    ModSounds.MAG_IN.get(),         // Reload sound mag in
                    ModSounds.RELOAD_END.get(),           // Reload sound end
                    ModSounds.COPPER_GUN_JAM.get(),      // Ejector sound pull
                    ModSounds.COPPER_GUN_JAM.get()    // Ejector sound release
            )
    );
    public static final DeferredItem<AnimatedGunItem> M3_MARKSMAN = REGISTER.registerItem("m3_marksman",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "m3_marksman",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MAKESHIFT_RIFLE = REGISTER.registerItem("makeshift_rifle",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "makeshift_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> DRILL = REGISTER.registerItem("drill",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1200),
                    "drill",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> DRILL_CONVERSION = REGISTER.registerItem("drill_conversion",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1200),
                    "drill_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> LOCKEWOOD = REGISTER.registerItem("lockewood",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "lockewood",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MICINA = REGISTER.registerItem("micina",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "micina",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> RG_JIGSAW = REGISTER.registerItem("rg_jigsaw",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "rg_jigsaw",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelAirGunItem> NAILER = REGISTER.registerItem("nailer",
            props -> new AnimatedDiamondSteelAirGunItem(
                    props.stacksTo(1).durability(1400),
                    "nailer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MINKSY = REGISTER.registerItem("minksy",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "minksy",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MAS_PEDDLER = REGISTER.registerItem("mas_peddler",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "mas_peddler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> INQUISITOR = REGISTER.registerItem("inquisitor",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "inquisitor",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GRANDLE = REGISTER.registerItem("grandle",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "grandle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GRANDLE_OG = REGISTER.registerItem("grandle_og",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "grandle_og",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> DEFENDER_PISTOL = REGISTER.registerItem("defender_pistol",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "defender_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TRENCHUR = REGISTER.registerItem("trenchur",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "trenchur",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> AUVTOMAG = REGISTER.registerItem("auvtomag",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "auvtomag",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GREASER_SMG = REGISTER.registerItem("greaser_smg",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "greaser_smg",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BOOMSTICK = REGISTER.registerItem("boomstick",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "boomstick",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> INERTIAL = REGISTER.registerItem("inertial",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "inertial",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> M22_WALTZ = REGISTER.registerItem("m22_waltz",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "m22_waltz",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> FLOUNDERGAT = REGISTER.registerItem("floundergat",
            props -> new AnimatedUnderWaterGunItem(
                    props.stacksTo(1).durability(256),
                    "floundergat",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> SPIRULIDA = REGISTER.registerItem("spirulida",
            props -> new AnimatedUnderWaterGunItem(
                    props.stacksTo(1).durability(800),
                    "spirulida",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> HYPERBARIA = REGISTER.registerItem("hyperbaria",
            props -> new AnimatedUnderWaterGunItem(
                    props.stacksTo(1).durability(800),
                    "hyperbaria",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> HULLBREAKER = REGISTER.registerItem("hullbreaker",
            props -> new AnimatedUnderWaterGunItem(
                    props.stacksTo(1).durability(256),
                    "hullbreaker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.CANNON_RELOAD.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> KRAUSER = REGISTER.registerItem("krauser",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(800),
                    "krauser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> UPPERCUT = REGISTER.registerItem("uppercut",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "uppercut",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> PRUSH_GUN = REGISTER.registerItem("prush_gun",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "prush_gun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> SOUL_DRUMMER = REGISTER.registerItem("soul_drummer",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(800),
                    "soul_drummer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> VALORA = REGISTER.registerItem("valora",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "valora",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> RUSTY_GNAT = REGISTER.registerItem("rusty_gnat",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "rusty_gnat",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CALLWELL = REGISTER.registerItem("callwell",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "callwell",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CALLWELL_TERMINAL = REGISTER.registerItem("callwell_terminal",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "callwell_terminal",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CALLWELL_CONVERSION = REGISTER.registerItem("callwell_conversion",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "callwell_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> COMBAT_SHOTGUN = REGISTER.registerItem("combat_shotgun",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "combat_shotgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> FLINTLOCK_PISTOL = REGISTER.registerItem("flintlock_pistol",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "flintlock_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> HANDCANNON = REGISTER.registerItem("handcannon",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "handcannon",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> MUSKET = REGISTER.registerItem("musket",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> REPEATING_MUSKET = REGISTER.registerItem("repeating_musket",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "repeating_musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> LONGARM = REGISTER.registerItem("longarm",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(200),
                    "longarm",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedAirGunItem> RED_RAYDAR = REGISTER.registerItem("red_raydar",
            props -> new AnimatedAirGunItem(
                    props.stacksTo(1).durability(200),
                    "red_raydar",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> BLUNDERBUSS = REGISTER.registerItem("blunderbuss",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "blunderbuss",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> FENCER_CARABINE = REGISTER.registerItem("fencer_carabine",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(180),
                    "fencer_carabine",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> FENCER_THUMPER = REGISTER.registerItem("fencer_thumper",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(180),
                    "fencer_thumper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> DOUBLET = REGISTER.registerItem("doublet",
            props -> new NonUnderwaterAnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "doublet",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MAS_55 = REGISTER.registerItem("mas_55",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "mas_55",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> COGLOADER = REGISTER.registerItem("cogloader",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "cogloader",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SAKETINI = REGISTER.registerItem("saketini",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "saketini",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SAKETINI_IRONPORT = REGISTER.registerItem("saketini_ironport",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "saketini_ironport",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    SoundEvents.LEVER_CLICK,
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SCRAPPER = REGISTER.registerItem("scrapper",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "scrapper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BRAWLER = REGISTER.registerItem("brawler",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "brawler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WINNIE = REGISTER.registerItem("winnie",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "winnie",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WINNIE_MILLEND = REGISTER.registerItem("winnie_millend",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "winnie_millend",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BRUISER = REGISTER.registerItem("bruiser",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "bruiser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> CYCLONE = REGISTER.registerItem("cyclone",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "cyclone",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> PLASGUN = REGISTER.registerItem("plasgun",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "plasgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> ROCKET_RIFLE = REGISTER.registerItem("rocket_rifle",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "rocket_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelUnderWaterGunItem> ZILK_45 = REGISTER.registerItem("zilk_45",
            props -> new AnimatedDiamondSteelUnderWaterGunItem(
                    props.stacksTo(1).durability(1350),
                    "zilk_45",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> TRUANT = REGISTER.registerItem("truant",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "truant",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TRIQUETRA = REGISTER.registerItem("triquetra",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "triquetra",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MARLIN = REGISTER.registerItem("marlin",
            props -> new AnimatedUnderWaterGunItem(
                    props.stacksTo(1).durability(800),
                    "marlin",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GAUSS_RIFLE = REGISTER.registerItem("gauss_rifle",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "gauss_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> NIAMI = REGISTER.registerItem("niami",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "niami",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> IRON_SPEAR = REGISTER.registerItem("iron_spear",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "iron_spear",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> IRON_JAVELIN = REGISTER.registerItem("iron_javelin",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "iron_javelin",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );

    public static final DeferredItem<AnimatedGunItem> LLR_DIRECTOR = REGISTER.registerItem("llr_director",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "llr_director",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BIRDFEEDER = REGISTER.registerItem("birdfeeder",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "birdfeeder",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TURNPIKE = REGISTER.registerItem("turnpike",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "turnpike",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> STILETTO = REGISTER.registerItem("stiletto",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "stiletto",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedAirGunItem> RAILWORKER = REGISTER.registerItem("railworker",
            props -> new AnimatedAirGunItem(
                    props.stacksTo(1).durability(540),
                    "railworker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WHIZZBANGER = REGISTER.registerItem("whizzbanger",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(15),
                    "whizzbanger",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BASKER = REGISTER.registerItem("basker",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "basker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> KALASKAH = REGISTER.registerItem("kalaskah",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "kalaskah",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MOKOVA = REGISTER.registerItem("mokova",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "mokova",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MAK_MKII = REGISTER.registerItem("mak_mkii",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "mak_mkii",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TL_RUNNER = REGISTER.registerItem("tl_runner",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "tl_runner",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> KILLER_23 = REGISTER.registerItem("killer_23",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "killer_23",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HOMEMAKER = REGISTER.registerItem("homemaker",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "homemaker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> RIBS_GLORY = REGISTER.registerItem("ribs_glory",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(2400),
                    "ribs_glory",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> STIGG = REGISTER.registerItem("stigg",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(540),
                    "stigg",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PAX = REGISTER.registerItem("pax",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "pax",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BIG_BORE = REGISTER.registerItem("big_bore",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(9),
                    "big_bore",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HOWLER = REGISTER.registerItem("howler",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "howler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HOWLER_CONVERSION = REGISTER.registerItem("howler_conversion",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "howler_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PULSAR = REGISTER.registerItem("pulsar",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "pulsar",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> ARC_WORKER = REGISTER.registerItem("arc_worker",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "arc_worker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BLOOPER = REGISTER.registerItem("blooper",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "blooper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WHISTLER = REGISTER.registerItem("whistler",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "whistler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LASER_MUSKET = REGISTER.registerItem("laser_musket",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(512),
                    "laser_musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HAMMER_GL = REGISTER.registerItem("hammer_gl",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "hammer_gl",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LIBERTAS = REGISTER.registerItem("libertas",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "libertas",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TESLOCK_RIFLE = REGISTER.registerItem("teslock_rifle",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(512),
                    "teslock_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> KILN_GUN = REGISTER.registerItem("kiln_gun",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(200),
                    "kiln_gun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PLASMABUSS = REGISTER.registerItem("plasmabuss",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(512),
                    "plasmabuss",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> JACKHAMMER = REGISTER.registerItem("jackhammer",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "jackhammer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> JR_WRISTBREAKER = REGISTER.registerItem("jr_wristbreaker",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "jr_wristbreaker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SEQUOIA = REGISTER.registerItem("sequoia",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "sequoia",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> ULTRA_KNIGHT_HAWK = REGISTER.registerItem("ultra_knight_hawk",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(15),
                    "ultra_knight_hawk",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SUPER_SHOTGUN = REGISTER.registerItem("super_shotgun",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "super_shotgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BOMB_LANCE = REGISTER.registerItem("bomb_lance",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "bomb_lance",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> DOZIER_RL = REGISTER.registerItem("dozier_rl",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(512),
                    "dozier_rl",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> DARK_MATTER = REGISTER.registerItem("dark_matter",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1600),
                    "dark_matter",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MK43_RIFLE = REGISTER.registerItem("mk43_rifle",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "mk43_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CRUSADER = REGISTER.registerItem("crusader",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(256),
                    "crusader",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GYROJET_PISTOL = REGISTER.registerItem("gyrojet_pistol",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(800),
                    "gyrojet_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> THUNDERHEAD = REGISTER.registerItem("thunderhead",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(600),
                    "thunderhead",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GATTALER = REGISTER.registerItem("gattaler",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1000),
                    "gattaler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CR4K_MINING_LASER = REGISTER.registerItem("cr4k_mining_laser",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "cr4k_mining_laser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SHARD_CULLER = REGISTER.registerItem("shard_culler",
            props -> new AnimatedDiamondSteelGunItem(
                    props.stacksTo(1).durability(1350),
                    "shard_culler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SPITFIRE = REGISTER.registerItem("spitfire",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "spitfire",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WALTZ_CONVERSION = REGISTER.registerItem("waltz_conversion",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "waltz_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> OSGOOD_50 = REGISTER.registerItem("osgood_50",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1400),
                    "osgood_50",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> FREYR = REGISTER.registerItem("freyr",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1050),
                    "freyr",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> VULCANIC_REPEATER = REGISTER.registerItem("vulcanic_repeater",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1050),
                    "vulcanic_repeater",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PYROCLASTIC_FLOW = REGISTER.registerItem("pyroclastic_flow",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1050),
                    "pyroclastic_flow",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MANGALITSA = REGISTER.registerItem("mangalitsa",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1050),
                    "mangalitsa",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TROTTERS = REGISTER.registerItem("trotters",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1050),
                    "trotters",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BLASPHEMY = REGISTER.registerItem("blasphemy",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1050),
                    "blasphemy",
                    SoundEvents.GENERIC_DRINK.value(),
                    SoundEvents.PLAYER_BURP,
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> WHISPERS = REGISTER.registerItem("whispers",
            props -> new AnimatedSculkGunItem(
                    props.stacksTo(1).durability(1100),
                    "whispers",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> SCULK_RESONATOR = REGISTER.registerItem("sculk_resonator",
            props -> new AnimatedSculkGunItem(
                    props.stacksTo(1).durability(1100),
                    "sculk_resonator",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> ECHOES_2 = REGISTER.registerItem("echoes_2",
            props -> new AnimatedSculkGunItem(
                    props.stacksTo(1).durability(1100),
                    "echoes_2",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> FORLORN_HOPE = REGISTER.registerItem("forlorn_hope",
            props -> new AnimatedSculkGunItem(
                    props.stacksTo(1).durability(1100),
                    "forlorn_hope",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> RAYGUN = REGISTER.registerItem("raygun",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1600),
                    "raygun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CARAPICE = REGISTER.registerItem("carapice",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1600),
                    "carapice",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SHELLURKER = REGISTER.registerItem("shellurker",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1600),
                    "shellurker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WEEVIL = REGISTER.registerItem("weevil",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1600),
                    "weevil",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LONE_WONDER = REGISTER.registerItem("lone_wonder",
            props -> new AnimatedGunItem(
                    props.stacksTo(1).durability(1600),
                    "lone_wonder",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDualWieldGunItem> RAT_KING_AND_QUEEN = REGISTER.registerItem("rat_king_and_queen",
            props -> new AnimatedDualWieldGunItem(
                    props.stacksTo(1).durability(2400),
                    "rat_king_and_queen",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LOCUST = REGISTER.registerItem("locust",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "locust",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> STERILIZER = REGISTER.registerItem("sterilizer",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "sterilizer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.HISS.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> NEWBORN_CYST = REGISTER.registerItem("newborn_cyst",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "newborn_cyst",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );

    public static final DeferredItem<AnimatedScorchedGunItem> ASTELLA = REGISTER.registerItem("astella",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "astella",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> TERRA_INCOGNITA = REGISTER.registerItem("terra_incognita",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "terra_incognita",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    SoundEvents.PISTON_EXTEND
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> PRIMA_MATERIA = REGISTER.registerItem("prima_materia",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "prima_materia",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> NERVEPINCH = REGISTER.registerItem("nervepinch",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "nervepinch",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> EARTHS_CORPSE = REGISTER.registerItem("earths_corpse",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "earths_corpse",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> FLAYED_GOD = REGISTER.registerItem("flayed_god",
            props -> new AnimatedScorchedGunItem(
                    props.stacksTo(1).durability(2400),
                    "flayed_god",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );


    public static final DeferredItem<Item> ANTHRALITE_PICKAXE = REGISTER.registerItem("anthralite_pickaxe", props -> new Item(props.pickaxe(ModTiers.ANTHRALITE, 1.0F, -2.8F)));
    public static final DeferredItem<Item> ANTHRALITE_SWORD = REGISTER.registerItem("anthralite_sword", props -> new Item(props.sword(ModTiers.ANTHRALITE, 3.0F, -2.4F)));
    public static final DeferredItem<Item> ANTHRALITE_AXE = REGISTER.registerItem("anthralite_axe", props -> new Item(props.axe(ModTiers.ANTHRALITE, 5.0F, -3.0F)));
    public static final DeferredItem<Item> ANTHRALITE_HAMMER = REGISTER.registerItem("anthralite_hammer", props -> new AnthraliteHammerItem(ModTiers.ANTHRALITE, -3.2F, props));
    public static final DeferredItem<Item> ANTHRALITE_PAXEL = REGISTER.registerItem("anthralite_paxel", props -> new AnthralitePaxelItem(ModTiers.ANTHRALITE, props));
    public static final DeferredItem<CogMaceItem> COG_MACE = REGISTER.registerItem("cog_mace", props -> new CogMaceItem(ModTiers.ANCIENT_BRASS, 2, -3.2F, props));
    public static final DeferredItem<Item> WARAXE = REGISTER.registerItem("war_axe", props -> new WaraxeItem(props));
    public static final DeferredItem<Item> ANTHRALITE_SHOVEL = REGISTER.registerItem("anthralite_shovel", props -> new Item(props.shovel(ModTiers.ANTHRALITE, 1.5F, -3.0F)));
    public static final DeferredItem<Item> ANTHRALITE_HOE = REGISTER.registerItem("anthralite_hoe", props -> new Item(props.hoe(ModTiers.ANTHRALITE, -3.0F, -3.0F)));
    public static final DeferredItem<Item> RANGE_FINDER = REGISTER.registerItem("range_finder", props -> new RangeFinderItem(props.stacksTo(1)));
    public static final DeferredItem<Item> METAL_DETECTOR = REGISTER.registerItem("metal_detector", props -> new MetalDetectorItem(props.stacksTo(1).durability(128).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> FLARE_PISTOL = REGISTER.registerItem("flare_pistol", props -> new FlarePistolItem(props.stacksTo(1).durability(128)));
    public static final DeferredItem<Item> WHITE_FLAG = REGISTER.registerItem("white_flag", props -> new WhiteFlagItem(props.stacksTo(16)));
    public static final DeferredItem<Item> ANTIQUE_FLARE = REGISTER.registerItem("antique_flare", props -> new RaidFlareItem(props.stacksTo(16), "antique"));
    public static final DeferredItem<Item> FRONTIER_FLARE = REGISTER.registerItem("frontier_flare", props -> new RaidFlareItem(props.stacksTo(16), "frontier"));
    public static final DeferredItem<Item> COPPER_FLARE = REGISTER.registerItem("copper_flare", props -> new RaidFlareItem(props.stacksTo(16), "copper"));
    public static final DeferredItem<Item> IRON_FLARE = REGISTER.registerItem("iron_flare", props -> new RaidFlareItem(props.stacksTo(16), "iron"));
    public static final DeferredItem<Item> WRECKER_FLARE = REGISTER.registerItem("wrecker_flare", props -> new RaidFlareItem(props.stacksTo(16), "wrecker"));
    public static final DeferredItem<Item> GOLD_FLARE = REGISTER.registerItem("gold_flare", props -> new RaidFlareItem(props.stacksTo(16), "piglin"));
    public static final DeferredItem<Item> DIAMOND_STEEL_FLARE = REGISTER.registerItem("diamond_steel_flare", props -> new RaidFlareItem(props.stacksTo(16), "diamond_steel"));
    public static final DeferredItem<Item> TREATED_BRASS_FLARE = REGISTER.registerItem("treated_brass_flare", props -> new RaidFlareItem(props.stacksTo(16), "treated_brass"));
    public static final DeferredItem<Item> OCEAN_FLARE = REGISTER.registerItem("ocean_flare", props -> new RaidFlareItem(props.stacksTo(16), "ocean"));
    public static final DeferredItem<Item> SCULK_FLARE = REGISTER.registerItem("sculk_flare", props -> new RaidFlareItem(props.stacksTo(16), "sculk"));
    public static final DeferredItem<Item> RUSTY_MEDAL = REGISTER.registerItem("rusty_medal", props -> new TooltipItem(props.rarity(Rarity.UNCOMMON), "item.scguns.rusty_medal.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> LABOR_TROPHY = REGISTER.registerItem("labor_trophy", props -> new TooltipItem(props.rarity(Rarity.UNCOMMON), "item.scguns.labor_trophy.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> SNAPPED_COGWHEEL = REGISTER.registerItem("snapped_cogwheel", props -> new TooltipItem(props.rarity(Rarity.UNCOMMON), "item.scguns.snapped_cogwheel.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> COG_HEART = REGISTER.registerItem("cog_heart", props -> new TooltipItem(props.rarity(Rarity.RARE), "item.scguns.cog_heart.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> GOLD_IDOL = REGISTER.registerItem("gold_idol", props -> new TooltipItem(props.rarity(Rarity.RARE), "item.scguns.gold_idol.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> LEVIATHAN_TOOTH = REGISTER.registerItem("leviathan_tooth", props -> new TooltipItem(props.rarity(Rarity.RARE), "item.scguns.leviathan_tooth.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> CEREMONIAL_COD = REGISTER.registerItem("ceremonial_cod", props -> new TooltipItem(props.rarity(Rarity.RARE), "item.scguns.ceremonial_cod.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> SCULK_TOME = REGISTER.registerItem("sculk_tome", props -> new TooltipItem(props.rarity(Rarity.RARE), "item.scguns.sculk_tome.tooltip", "item.scguns.found_in_raids"));
    public static final DeferredItem<Item> ANTHRALITE_HELMET = REGISTER.registerItem("anthralite_helmet", props -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> ANTHRALITE_CHESTPLATE = REGISTER.registerItem("anthralite_chestplate", props -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> ANTHRALITE_LEGGINGS = REGISTER.registerItem("anthralite_leggings", props -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> ANTHRALITE_BOOTS = REGISTER.registerItem("anthralite_boots", props -> new AnthraliteArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> RIDGETOP = REGISTER.registerItem("ridgetop", props -> new RidgetopArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> BRASS_MASK = REGISTER.registerItem("brass_mask", props -> new BrassMaskArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> IRON_MASK = REGISTER.registerItem("iron_mask", props -> new IronMaskArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> ADRIEN_HELM = REGISTER.registerItem("adrien_helm", props -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> ADRIEN_CHESTPLATE = REGISTER.registerItem("adrien_chestplate", props -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> ADRIEN_LEGGINGS = REGISTER.registerItem("adrien_leggings", props -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> ADRIEN_BOOTS = REGISTER.registerItem("adrien_boots", props -> new AdrienArmorItem(ModArmorMaterials.ADRIEN, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> COG_KNIGHT_HELMET = REGISTER.registerItem("cog_knight_helmet", props -> new CogKnightArmorItem(ModArmorMaterials.ANCIENT_BRASS, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> COG_KNIGHT_CHESTPLATE = REGISTER.registerItem("cog_knight_chestplate", props -> new CogKnightArmorItem(ModArmorMaterials.ANCIENT_BRASS, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> COG_KNIGHT_LEGGINGS = REGISTER.registerItem("cog_knight_leggings", props -> new CogKnightArmorItem(ModArmorMaterials.ANCIENT_BRASS, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> COG_KNIGHT_BOOTS = REGISTER.registerItem("cog_knight_boots", props -> new CogKnightArmorItem(ModArmorMaterials.ANCIENT_BRASS, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> EXO_SUIT_HELMET = REGISTER.registerItem("exo_suit_helmet", props -> new ExoSuitItem(ModArmorMaterials.EXO_SUIT, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> EXO_SUIT_CHESTPLATE = REGISTER.registerItem("exo_suit_chestplate", props -> new ExoSuitItem(ModArmorMaterials.EXO_SUIT, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> EXO_SUIT_LEGGINGS = REGISTER.registerItem("exo_suit_leggings", props -> new ExoSuitItem(ModArmorMaterials.EXO_SUIT, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> EXO_SUIT_BOOTS = REGISTER.registerItem("exo_suit_boots", props -> new ExoSuitItem(ModArmorMaterials.EXO_SUIT, ArmorItem.Type.BOOTS, props));

    public static final DeferredItem<Item> EXO_SUIT_CORE = REGISTER.registerItem("exo_suit_core",
            props -> new ExoSuitCoreItem(props.stacksTo(1), ExoSuitCoreItem.CoreTier.BASIC));
    public static final DeferredItem<Item> ADVANCED_EXO_SUIT_CORE = REGISTER.registerItem("advanced_exo_suit_core",
            props -> new ExoSuitCoreItem(props.stacksTo(1), ExoSuitCoreItem.CoreTier.ADVANCED));

    public static final DeferredItem<Item> HEAVY_ARMOR_PLATE = REGISTER.registerItem("heavy_armor_plate",
            props -> new DamageableUpgradeItem(props.durability(80)));
    public static final DeferredItem<Item> HEAVY_PAULDRON = REGISTER.registerItem("heavy_pauldron",
            props -> new DamageableUpgradeItem(props.durability(80)));
    public static final DeferredItem<Item> ARMOR_PLATE = REGISTER.registerItem("armor_plate",
            props -> new DamageableUpgradeItem(props.durability(64)));
    public static final DeferredItem<Item> PAULDRON = REGISTER.registerItem("pauldron",
            props -> new DamageableUpgradeItem(props.durability(64)));

    public static final DeferredItem<Item> NIGHT_VISION_MODULE = REGISTER.registerItem("night_vision_module",
            props -> new NightVisionModuleItem(props.stacksTo(1).durability(256)));

    public static final DeferredItem<Item> GAS_MASK_MODULE = REGISTER.registerItem("gas_mask_module",
            props -> new GasMaskModuleItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> REBREATHER_MODULE = REGISTER.registerItem("rebreather_module",
            props -> new RebreatherModuleItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> TARGET_TRACKER_MODULE = REGISTER.registerItem("target_tracker_module",
            props -> new TargetTrackerModuleItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> JETPACK_MODULE = REGISTER.registerItem("jetpack_module",
            props -> new JetpackModuleItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> ARMOR_POUCHES = REGISTER.registerItem("armor_pouches",
            props -> new UpgradeItem(props.stacksTo(1)));

    public static final DeferredItem<Item> HEAVY_ARMOR_POUCHES = REGISTER.registerItem("heavy_armor_pouches",
            props -> new UpgradeItem(props.stacksTo(1)));
    public static final DeferredItem<Item> RABBIT_MODULE = REGISTER.registerItem("rabbit_module",
            props -> new RabbitModuleItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> SUIT_GREASE = REGISTER.registerItem("suit_grease",
            props -> new DamageableUpgradeItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> TENSION_SPRING = REGISTER.registerItem("tension_spring",
            props -> new DamageableUpgradeItem(props.stacksTo(1).durability(256)));

    public static final DeferredItem<Item> SHOCK_ABSORBER = REGISTER.registerItem("shock_absorber",
            props -> new DamageableUpgradeItem(props.stacksTo(1).durability(256)));


    public static final DeferredItem<Item> ANTHRALITE_RESPIRATOR = REGISTER.registerItem("anthralite_respirator", props -> new AnthraliteGasMaskArmorItem(ModArmorMaterials.ANTHRALITE, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> NETHERITE_RESPIRATOR = REGISTER.registerItem("netherite_respirator", props -> new NetheriteGasMaskArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> DIAMOND_STEEL_HELMET = REGISTER.registerItem("diamond_steel_helmet", props -> new DiamondSteelArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> DIAMOND_STEEL_CHESTPLATE = REGISTER.registerItem("diamond_steel_chestplate", props -> new DiamondSteelArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> DIAMOND_STEEL_LEGGINGS = REGISTER.registerItem("diamond_steel_leggings", props -> new DiamondSteelArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> DIAMOND_STEEL_BOOTS = REGISTER.registerItem("diamond_steel_boots", props -> new DiamondSteelArmorItem(ModArmorMaterials.DIAMOND_STEEL, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> TREATED_BRASS_HELMET = REGISTER.registerItem("treated_brass_helmet", props -> new TreatedBrassArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> TREATED_BRASS_CHESTPLATE = REGISTER.registerItem("treated_brass_chestplate", props -> new TreatedBrassArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> TREATED_BRASS_LEGGINGS = REGISTER.registerItem("treated_brass_leggings", props -> new TreatedBrassArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> TREATED_BRASS_BOOTS = REGISTER.registerItem("treated_brass_boots", props -> new TreatedBrassArmorItem(ModArmorMaterials.TREATED_BRASS, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> REDCOAT_HAT = REGISTER.registerItem("redcoat_hat", props -> new RedcoatArmorItem(ModArmorMaterials.REDCOAT, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> REDCOAT_COAT = REGISTER.registerItem("redcoat_coat", props -> new RedcoatArmorItem(ModArmorMaterials.REDCOAT, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> REDCOAT_PANTS = REGISTER.registerItem("redcoat_pants", props -> new RedcoatArmorItem(ModArmorMaterials.REDCOAT, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> REDCOAT_BOOTS = REGISTER.registerItem("redcoat_boots", props -> new RedcoatArmorItem(ModArmorMaterials.REDCOAT, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> SCRAP_HELMET = REGISTER.registerItem("scrap_helmet", props -> new ScrapArmorItem(ModArmorMaterials.SCRAP, ArmorItem.Type.HELMET, props));
    public static final DeferredItem<Item> SCRAP_CHESTPLATE = REGISTER.registerItem("scrap_chestplate", props -> new ScrapArmorItem(ModArmorMaterials.SCRAP, ArmorItem.Type.CHESTPLATE, props));
    public static final DeferredItem<Item> SCRAP_LEGGINGS = REGISTER.registerItem("scrap_leggings", props -> new ScrapArmorItem(ModArmorMaterials.SCRAP, ArmorItem.Type.LEGGINGS, props));
    public static final DeferredItem<Item> SCRAP_BOOTS = REGISTER.registerItem("scrap_boots", props -> new ScrapArmorItem(ModArmorMaterials.SCRAP, ArmorItem.Type.BOOTS, props));
    public static final DeferredItem<Item> ANTIQUE_BLUEPRINT = REGISTER.registerItem("antique_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> FRONTIER_BLUEPRINT = REGISTER.registerItem("frontier_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> COPPER_BLUEPRINT = REGISTER.registerItem("copper_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> IRON_BLUEPRINT = REGISTER.registerItem("iron_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> WRECKER_BLUEPRINT = REGISTER.registerItem("wrecker_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> TREATED_BRASS_BLUEPRINT = REGISTER.registerItem("treated_brass_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> DIAMOND_STEEL_BLUEPRINT = REGISTER.registerItem("diamond_steel_blueprint", props -> new BlueprintItem(props.stacksTo(1)));
    public static final DeferredItem<Item> PIGLIN_BLUEPRINT = REGISTER.registerItem("piglin_blueprint", props -> new BlueprintItem(props.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> OCEAN_BLUEPRINT = REGISTER.registerItem("ocean_blueprint", props -> new BlueprintItem(props.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> DEEP_DARK_BLUEPRINT = REGISTER.registerItem("deep_dark_blueprint", props -> new BlueprintItem(props.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> END_BLUEPRINT = REGISTER.registerItem("end_blueprint", props -> new BlueprintItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> SCORCHED_BLUEPRINT = REGISTER.registerItem("scorched_blueprint", props -> new GlintedBlueprintItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> EXO_SUIT_BLUEPRINT = REGISTER.registerItem("exo_suit_blueprint", props -> new BlueprintItem(props.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BLUEPRINT_SCRAP = REGISTER.registerItem("blueprint_scrap", props -> new Item(props));
    public static final DeferredItem<Item> STANDARD_BULLET = REGISTER.registerItem("standard_bullet", props -> new Item(props));
    public static final DeferredItem<Item> ADVANCED_BULLET = REGISTER.registerItem("hardened_bullet", props -> new Item(props));
    public static final DeferredItem<Item> SYRINGE = REGISTER.registerItem("syringe", props -> new Item(props));
    public static final DeferredItem<Item> NITRO_POWDER = REGISTER.registerItem("nitro_powder", props -> new Item(props));
    public static final DeferredItem<Item> NITRO_POWDER_DUST = REGISTER.registerItem("nitro_powder_dust", props -> new Item(props));
    public static final DeferredItem<Item> NITER_DUST = REGISTER.registerItem("niter_dust", props -> new NiterDustItem(props));
    public static final DeferredItem<Item> BAT_GUANO = REGISTER.registerItem("bat_guano", props -> new BatGuanoItem(props));
    public static final DeferredItem<Item> SHEOL = REGISTER.registerItem("sheol", props -> new Item(props));
    public static final DeferredItem<Item> PEAL = REGISTER.registerItem("peal", props -> new Item(props));
    public static final DeferredItem<Item> PEAL_DUST = REGISTER.registerItem("peal_dust", props -> new Item(props));
    public static final DeferredItem<Item> VEHEMENT_COAL = REGISTER.registerItem("vehement_coal", props -> new FuelItem(props, 4800));
    public static final DeferredItem<Item> SHEOL_DUST = REGISTER.registerItem("sheol_dust", props -> new Item(props));
    public static final DeferredItem<Item> SULFUR_CHUNK = REGISTER.registerItem("sulfur_chunk", props -> new FuelItem(props, 800));
    public static final DeferredItem<Item> COMPOSITE_FILTER = REGISTER.registerItem("composite_filter", props -> new Item(props));
    public static final DeferredItem<Item> SULFUR_DUST = REGISTER.registerItem("sulfur_dust", props -> new SulfurDustItem(props));
    public static final DeferredItem<Item> PHOSPHOR_DUST = REGISTER.registerItem("phosphor_dust", props -> new PhosphorItem(props));
    public static final DeferredItem<Item> VICIOUS_ACID_BUCKET = REGISTER.registerItem("vicious_acid_bucket",
            props -> new BucketItem(ModFluids.VICIOUS_ACID_SOURCE.get(), props.craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<Item> BUCKSHOT = REGISTER.registerItem("buckshot", props -> new Item(props));
    public static final DeferredItem<Item> FLECHETTE = REGISTER.registerItem("flechette", props -> new Item(props));
    public static final DeferredItem<Item> NEEDLE = REGISTER.registerItem("needle", props -> new Item(props));
    public static final DeferredItem<Item> NITRO_BUCKSHOT = REGISTER.registerItem("nitro_buckshot", props -> new Item(props));
    public static final DeferredItem<Item> RAW_PHOSPHOR = REGISTER.registerItem("raw_phosphor", props -> new Item(props));
    public static final DeferredItem<Item> GUNPOWDER_DUST = REGISTER.registerItem("gunpowder_dust", props -> new Item(props));
    public static final DeferredItem<Item> RAW_ANTHRALITE = REGISTER.registerItem("raw_anthralite", props -> new Item(props));
    public static final DeferredItem<Item> CRUSHED_RAW_ANTHRALITE = REGISTER.registerItem("crushed_raw_anthralite", props -> new Item(props));
    public static final DeferredItem<Item> ANTHRALITE_DUST = REGISTER.registerItem("anthralite_dust", props -> new Item(props));
    public static final DeferredItem<Item> CLUMP_ANTHRALITE = REGISTER.registerItem("clump_anthralite", props -> new Item(props));
    public static final DeferredItem<Item> SHARD_ANTHRALITE = REGISTER.registerItem("shard_anthralite", props -> new Item(props));
    public static final DeferredItem<Item> DIRTY_DUST_ANTHRALITE = REGISTER.registerItem("dirty_dust_anthralite", props -> new Item(props));
    public static final DeferredItem<Item> MASS_PRODUCTION_MUSIC_DISC = REGISTER.registerItem("music_disc_mass_production",
            props -> new Item(props.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(jukeboxSong("mass_production"))));
    public static final DeferredItem<Item> MASS_DESTRUCTION_MUSIC_DISC = REGISTER.registerItem("music_disc_mass_destruction",
            props -> new Item(props.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(jukeboxSong("mass_destruction"))));
    public static final DeferredItem<Item> MASS_DESTRUCTION_EXTENDED_MUSIC_DISC = REGISTER.registerItem("music_disc_mass_destruction_extended",
            props -> new Item(props.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(jukeboxSong("mass_destruction_extended"))));

    public static final DeferredItem<Item> TEAM_LOG = REGISTER.registerItem("team_log", props -> new TeamLogItem(props.stacksTo(1)));

    public static final DeferredItem<Item> ENEMY_LOG = REGISTER.registerItem("enemy_log", props -> new EnemyLogItem(props.stacksTo(1)));
    public static final DeferredItem<Item> AUREOUS_SLAG = REGISTER.registerItem("aureous_slag", props -> new Item(props));
    public static final DeferredItem<Item> ANTHRALITE_INGOT = REGISTER.registerItem("anthralite_ingot", props -> new Item(props));
    public static final DeferredItem<Item> ANTHRALITE_NUGGET = REGISTER.registerItem("anthralite_nugget", props -> new Item(props));
    public static final DeferredItem<Item> ANCIENT_BRASS = REGISTER.registerItem("ancient_brass", props -> new Item(props));
    public static final DeferredItem<Item> TREATED_IRON_BLEND = REGISTER.registerItem("treated_iron_blend", props -> new Item(props));
    public static final DeferredItem<Item> TREATED_IRON_INGOT = REGISTER.registerItem("treated_iron_ingot", props -> new Item(props));
    public static final DeferredItem<Item> TREATED_IRON_NUGGET = REGISTER.registerItem("treated_iron_nugget", props -> new Item(props));

    public static final DeferredItem<Item> TREATED_BRASS_BLEND = REGISTER.registerItem("treated_brass_blend", props -> new Item(props));
    public static final DeferredItem<Item> TREATED_BRASS_INGOT = REGISTER.registerItem("treated_brass_ingot", props -> new Item(props));
    public static final DeferredItem<Item> DIAMOND_STEEL_BLEND = REGISTER.registerItem("diamond_steel_blend", props -> new Item(props));
    public static final DeferredItem<Item> DEPLETED_DIAMOND_STEEL_INGOT = REGISTER.registerItem("depleted_diamond_steel_ingot", props -> new DepletedDiamondSteelItem(props));
    public static final DeferredItem<Item> DIAMOND_STEEL_INGOT = REGISTER.registerItem("diamond_steel_ingot", props -> new Item(props));
    public static final DeferredItem<Item> SCORCHED_BLEND = REGISTER.registerItem("scorched_blend", props -> new ScorchedItem(props));
    public static final DeferredItem<Item> SCORCHED_INGOT = REGISTER.registerItem("scorched_ingot", props -> new ScorchedItem(props));
    public static final DeferredItem<Item> CHARGED_AMETHYST_SHARD = REGISTER.registerItem("charged_amethyst_shard", props -> new Item(props));
    public static final DeferredItem<Item> EMPTY_TANK = REGISTER.registerItem("empty_tank", props -> new Item(props));
    public static final DeferredItem<Item> AIR_CANISTER = REGISTER.registerItem("air_canister", props -> new AirCanisterItem(props.stacksTo(1), 1700));
    public static final DeferredItem<Item> REINFORCED_AIR_CANISTER = REGISTER.registerItem("reinforced_air_canister", props -> new AirCanisterItem(props.stacksTo(1), 3200));
    public static final DeferredItem<Item> CREATIVE_AIR_CANISTER = REGISTER.registerItem("creative_air_canister", props -> new CreativeAirCanisterItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> EMPTY_CORE = REGISTER.registerItem("empty_core", props -> new Item(props));
    public static final DeferredItem<Item> ENERGY_CORE = REGISTER.registerItem("energy_core", props -> new Item(props));
    public static final DeferredItem<Item> DEPLETED_ENERGY_CORE = REGISTER.registerItem("depleted_energy_core", props -> new Item(props));
    public static final DeferredItem<Item> PLASMA_CORE = REGISTER.registerItem("plasma_core", props -> new Item(props));
    public static final DeferredItem<Item> NETHER_STAR_FRAGMENT = REGISTER.registerItem("nether_star_fragment", props -> new NetherStarFragmentItem(props));
    public static final DeferredItem<Item> EMPTY_BLASPHEMY = REGISTER.registerItem("empty_blasphemy", props -> new EmptyBlasphemyItem(props.stacksTo(1)));
    public static final DeferredItem<Item> GUN_GRIP = REGISTER.registerItem("gun_grip", props -> new Item(props));
    public static final DeferredItem<Item> GUN_BARREL = REGISTER.registerItem("gun_barrel", props -> new Item(props));
    public static final DeferredItem<Item> HEAVY_GUN_BARREL = REGISTER.registerItem("heavy_gun_barrel", props -> new Item(props));
    public static final DeferredItem<Item> STONE_GUN_BARREL = REGISTER.registerItem("stone_gun_barrel", props -> new Item(props));
    public static final DeferredItem<Item> GUN_MAGAZINE = REGISTER.registerItem("gun_magazine", props -> new Item(props));
    public static final DeferredItem<Item> GUN_PARTS = REGISTER.registerItem("gun_parts", props -> new Item(props));
    public static final DeferredItem<Item> HEAVY_GUN_PARTS = REGISTER.registerItem("heavy_gun_parts", props -> new Item(props));
    public static final DeferredItem<Item> FIRING_UNIT = REGISTER.registerItem("firing_unit", props -> new Item(props));
    public static final DeferredItem<Item> RAPID_FIRING_UNIT = REGISTER.registerItem("rapid_firing_unit", props -> new Item(props));
    public static final DeferredItem<Item> COPPER_GUN_FRAME = REGISTER.registerItem("copper_gun_frame", props -> new Item(props));
    public static final DeferredItem<Item> SCORCHED_GUN_FRAME = REGISTER.registerItem("scorched_gun_frame", props -> new ScorchedItem(props));
    public static final DeferredItem<Item> TREATED_IRON_GUN_FRAME = REGISTER.registerItem("treated_iron_gun_frame", props -> new Item(props));
    public static final DeferredItem<Item> IRON_GUN_FRAME = REGISTER.registerItem("iron_gun_frame", props -> new Item(props));
    public static final DeferredItem<Item> TREATED_BRASS_GUN_FRAME = REGISTER.registerItem("treated_brass_gun_frame", props -> new Item(props));
    public static final DeferredItem<Item> DIAMOND_STEEL_GUN_FRAME = REGISTER.registerItem("diamond_steel_gun_frame", props -> new Item(props));
    public static final DeferredItem<Item> BLANK_MOLD = REGISTER.registerItem("blank_mold", props -> new Item(props));
    public static final DeferredItem<Item> SMALL_CASING_MOLD = REGISTER.registerItem("small_casing_mold", props -> new MoldItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> MEDIUM_CASING_MOLD = REGISTER.registerItem("medium_casing_mold", props -> new MoldItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> LARGE_CASING_MOLD = REGISTER.registerItem("large_casing_mold", props -> new MoldItem(props.stacksTo(1).durability(128)));
    public static final DeferredItem<Item> BULLET_MOLD = REGISTER.registerItem("bullet_mold", props -> new MoldItem(props.stacksTo(1).durability(256)));
    public static final DeferredItem<Item> DISC_MOLD = REGISTER.registerItem("disc_mold", props -> new MoldItem(props.stacksTo(1).durability(64)));
    public static final DeferredItem<Item> GUN_PARTS_MOLD = REGISTER.registerItem("gun_parts_mold", props -> new MoldItem(props.stacksTo(1).durability(32)));
    public static final DeferredItem<Item> COPPER_DISC = REGISTER.registerItem("copper_disc", props -> new Item(props));
    public static final DeferredItem<Item> SMALL_COPPER_CASING = REGISTER.registerItem("small_copper_casing", props -> new Item(props));
    public static final DeferredItem<Item> MEDIUM_COPPER_CASING = REGISTER.registerItem("medium_copper_casing", props -> new Item(props));
    public static final DeferredItem<Item> SMALL_IRON_CASING = REGISTER.registerItem("small_iron_casing", props -> new Item(props));
    public static final DeferredItem<Item> LARGE_IRON_CASING = REGISTER.registerItem("large_iron_casing", props -> new Item(props));
    public static final DeferredItem<Item> EMPTY_CELL = REGISTER.registerItem("empty_cell", props -> new Item(props));
    public static final DeferredItem<Item> SHULKER_CASING = REGISTER.registerItem("shulker_casing", props -> new Item(props));
    public static final DeferredItem<Item> SHULKER_CORE = REGISTER.registerItem("shulker_core", props -> new TooltipItem(props, "item.scguns.shulker_core.tooltip", "item.scguns.shulker_core.tooltip_2"));
    public static final DeferredItem<Item> SMALL_DIAMOND_STEEL_CASING = REGISTER.registerItem("small_diamond_steel_casing", props -> new Item(props));
    public static final DeferredItem<Item> MEDIUM_DIAMOND_STEEL_CASING = REGISTER.registerItem("medium_diamond_steel_casing", props -> new Item(props));
    public static final DeferredItem<Item> SMALL_BRASS_CASING = REGISTER.registerItem("small_brass_casing", props -> new Item(props));
    public static final DeferredItem<Item> MEDIUM_BRASS_CASING = REGISTER.registerItem("medium_brass_casing", props -> new Item(props));
    public static final DeferredItem<Item> LARGE_BRASS_CASING = REGISTER.registerItem("large_brass_casing", props -> new Item(props));
    public static final DeferredItem<Item> POWDER_AND_BALL = REGISTER.registerItem("powder_and_ball", props -> new AmmoItem(props));
    public static final DeferredItem<Item> GRAPESHOT = REGISTER.registerItem("grapeshot", props -> new AmmoItem(props));
    public static final DeferredItem<Item> COMPACT_COPPER_ROUND = REGISTER.registerItem("compact_copper_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> HOG_ROUND = REGISTER.registerItem("hog_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> STANDARD_COPPER_ROUND = REGISTER.registerItem("standard_copper_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> COMPACT_ADVANCED_ROUND = REGISTER.registerItem("compact_advanced_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> RAMROD_ROUND = REGISTER.registerItem("ramrod_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> ADVANCED_ROUND = REGISTER.registerItem("advanced_round",
            props -> new TooltipAmmo(props, 2));
    public static final DeferredItem<Item> SHATTER_ROUND = REGISTER.registerItem("shatter_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> KRAHG_ROUND = REGISTER.registerItem("krahg_round",
            props -> new TooltipAmmo(props, 4));
    public static final DeferredItem<Item> BEOWULF_ROUND = REGISTER.registerItem("beowulf_round",
            props -> new TooltipAmmo(props, 2));
    public static final DeferredItem<Item> GIBBS_ROUND = REGISTER.registerItem("gibbs_round",
            props -> new TooltipAmmo(props, 2));
    public static final DeferredItem<Item> SHOTGUN_SHELL = REGISTER.registerItem("shotgun_shell", props -> new AmmoItem(props));
    public static final DeferredItem<Item> BEARPACK_SHELL = REGISTER.registerItem("bearpack_shell", props -> new AmmoItem(props));
    public static final DeferredItem<Item> BLAZE_FUEL = REGISTER.registerItem("blaze_fuel",
            props -> new FuelAmmoItem(
                    props,
                    3200,
                    ModItems.EMPTY_TANK,
                    new MobEffectInstance(MobEffects.STRENGTH, 200, 0),
                    new MobEffectInstance(MobEffects.WEAKNESS, 100, 0)
            ));

    public static final DeferredItem<Item> SHOTBALL = REGISTER.registerItem("shotball", props -> new ThrowableShotballItem(props));
    public static final DeferredItem<Item> FROG_DART = REGISTER.registerItem("frog_dart", props -> new TooltipAmmo(props, "tooltip.scguns.water"));
    public static final DeferredItem<Item> ENERGY_CELL = REGISTER.registerItem("energy_cell", props -> new AmmoItem(props));
    public static final DeferredItem<Item> SCULK_CELL = REGISTER.registerItem("sculk_cell",
            props -> new TooltipAmmo(props, 6));
    public static final DeferredItem<Item> SHOCK_CELL = REGISTER.registerItem("shock_cell",
            props -> new TooltipAmmo(props, "tooltip.scguns.arcing"));
    public static final DeferredItem<Item> MICROJET = REGISTER.registerItem("microjet", props -> new AmmoItem(props));
    public static final DeferredItem<Item> SHULKSHOT = REGISTER.registerItem("shulkshot",
            props -> new TooltipAmmo(props, "tooltip.scguns.homing"));

    public static final DeferredItem<Item> UNFINISHED_COMPACT_COPPER_ROUND = REGISTER.registerItem("unfinished_compact_copper_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_HOG_ROUND = REGISTER.registerItem("unfinished_hog_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_STANDARD_COPPER_ROUND = REGISTER.registerItem("unfinished_standard_copper_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_COMPACT_ADVANCED_ROUND = REGISTER.registerItem("unfinished_compact_advanced_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_RAMROD_ROUND = REGISTER.registerItem("unfinished_ramrod_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_ADVANCED_ROUND = REGISTER.registerItem("unfinished_advanced_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_SHATTER_ROUND = REGISTER.registerItem("unfinished_shatter_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_KRAHG_ROUND = REGISTER.registerItem("unfinished_krahg_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_BEOWULF_ROUND = REGISTER.registerItem("unfinished_beowulf_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_GIBBS_ROUND = REGISTER.registerItem("unfinished_gibbs_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_SHOTGUN_SHELL = REGISTER.registerItem("unfinished_shotgun_shell", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_BEARPACK_SHELL = REGISTER.registerItem("unfinished_bearpack_shell", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_ENERGY_CELL = REGISTER.registerItem("unfinished_energy_cell", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_SCULK_CELL = REGISTER.registerItem("unfinished_sculk_cell", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_MICROJET = REGISTER.registerItem("unfinished_microjet", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_SHULKSHOT = REGISTER.registerItem("unfinished_shulkshot", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_ROCKET = REGISTER.registerItem("unfinished_rocket", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_GUN_PARTS = REGISTER.registerItem("unfinished_gun_parts", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_HEAVY_GUN_PARTS = REGISTER.registerItem("unfinished_heavy_gun_parts", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_PLASMA_CORE = REGISTER.registerItem("unfinished_plasma_core", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_OSBORNE_SLUG = REGISTER.registerItem("unfinished_osborne_slug", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_FROG_DART = REGISTER.registerItem("unfinished_frog_dart", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_HE_GRENADE_ROUND = REGISTER.registerItem("unfinished_he_grenade_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_GAS_GRENADE_ROUND = REGISTER.registerItem("unfinished_gas_grenade_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_BOUNCY_GRENADE_ROUND = REGISTER.registerItem("unfinished_bouncy_grenade_round", props -> new Item(props));
    public static final DeferredItem<Item> UNFINISHED_FIRE_GRENADE_ROUND = REGISTER.registerItem("unfinished_fire_grenade_round", props -> new Item(props));
    public static final DeferredItem<Item> ROCKET = REGISTER.registerItem("rocket", props -> new AmmoItem(props.stacksTo(32)));
    public static final DeferredItem<Item> OSBORNE_SLUG = REGISTER.registerItem("osborne_slug", props -> new AmmoItem(props.stacksTo(4)));
    public static final DeferredItem<Item> HE_GRENADE_ROUND = REGISTER.registerItem("he_grenade_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> GAS_GRENADE_ROUND = REGISTER.registerItem("gas_grenade_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> BOUNCY_GRENADE_ROUND = REGISTER.registerItem("bouncy_grenade_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> FIRE_GRENADE_ROUND = REGISTER.registerItem("fire_grenade_round", props -> new AmmoItem(props));
    public static final DeferredItem<Item> PEBBLES = REGISTER.registerItem("pebbles", props -> new Item(props));
    public static final DeferredItem<Item> WEIRD_FLESH = REGISTER.registerItem("weird_flesh", WeirdFleshItem::new);
    public static final DeferredItem<Item> NETHERITE_SCRAP_CHUNK = REGISTER.registerItem("netherite_scrap_chunk", props -> new ScorchedItem(props));

    public static final DeferredItem<Item> PLASMA = REGISTER.registerItem("plasma", props -> new FuelItem(props, 1800));
    public static final DeferredItem<Item> PLASMA_NUGGET = REGISTER.registerItem("plasma_nugget", props -> new FuelItem(props, 360));
    public static final DeferredItem<Item> PISTOL_AMMO_BOX = REGISTER.registerItem("pistol_ammo_box", props -> new PistolAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> RIFLE_AMMO_BOX = REGISTER.registerItem("rifle_ammo_box", props -> new RifleAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> SHOTGUN_AMMO_BOX = REGISTER.registerItem("shotgun_ammo_box", props -> new ShotgunAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> MAGNUM_AMMO_BOX = REGISTER.registerItem("magnum_ammo_box", props -> new MagnumAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> ENERGY_AMMO_BOX = REGISTER.registerItem("energy_ammo_box", props -> new EnergyAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> EMPTY_CASING_POUCH = REGISTER.registerItem("empty_casing_pouch", props -> new EmptyCasingPouchItem(props.stacksTo(1)));
    public static final DeferredItem<Item> ROCKET_AMMO_BOX = REGISTER.registerItem("rocket_ammo_box", props -> new RocketAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> SPECIAL_AMMO_BOX = REGISTER.registerItem("special_ammo_box", props -> new SpecialAmmoBoxItem(props.stacksTo(1)));
    public static final DeferredItem<Item> CREATIVE_AMMO_BOX = REGISTER.registerItem("creative_ammo_box", props -> new CreativeAmmoBoxItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> DISHES_POUCH = REGISTER.registerItem("dishes_pouch", props -> new DishesPouch(props.stacksTo(1)));
    public static final DeferredItem<Item> ROCK_POUCH = REGISTER.registerItem("rock_pouch", props -> new RockPouch(props.stacksTo(1)));


    // Projectiles And Throwables
    public static final DeferredItem<Item> GRENADE = REGISTER.registerItem("grenade", props -> new GrenadeItem(props.stacksTo(32), 20 * 3));
    public static final DeferredItem<Item> STUN_GRENADE = REGISTER.registerItem("stun_grenade", props -> new StunGrenadeItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> MOLOTOV_COCKTAIL = REGISTER.registerItem("molotov_cocktail", props -> new MolotovCocktailItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> CHOKE_BOMB = REGISTER.registerItem("choke_bomb", props -> new ChokeBombItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> SWARM_BOMB = REGISTER.registerItem("swarm_bomb", props -> new SwarmBombItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> NAIL_BOMB = REGISTER.registerItem("nail_bomb", props -> new NailBombItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> HELLFIRE_BOMB = REGISTER.registerItem("hellfire_bomb", props -> new HellfireBombItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> GAS_GRENADE = REGISTER.registerItem("gas_grenade", props -> new GasGrenadeItem(props.stacksTo(32), 72000));
    public static final DeferredItem<Item> BEACON_GRENADE = REGISTER.registerItem("beacon_grenade", props -> new BeaconGrenadeItem(props.stacksTo(32), 72000));

    // Medical Items
    public static final DeferredItem<Item> BASIC_POULTICE = REGISTER.registerItem("basic_poultice",
            props -> new HealingBandageItem(props.stacksTo(16), 4, (MobEffectInstance) null));

    public static final DeferredItem<Item> HONEY_SULFUR_POULTICE = REGISTER.registerItem("honey_sulfur_poultice",
            props -> new HealingBandageItem(props.stacksTo(16), 8, new MobEffectInstance(MobEffects.REGENERATION, 100, 0)));

    public static final DeferredItem<Item> ENCHANTED_BANDAGE = REGISTER.registerItem("enchanted_bandage", props -> new GlintedHealingBandageItem(props.stacksTo(16).rarity(Rarity.RARE), 12, new MobEffectInstance(MobEffects.REGENERATION, 100, 1), new MobEffectInstance(MobEffects.ABSORPTION, 400, 0)));

    public static final DeferredItem<Item> DRAGON_SALVE = REGISTER.registerItem("dragon_salve", props -> new GlintedHealingBandageItem(props.stacksTo(16).rarity(Rarity.RARE), 16, new MobEffectInstance(MobEffects.REGENERATION, 200, 1), new MobEffectInstance(MobEffects.ABSORPTION, 700, 0)));

    public static final DeferredItem<Item> COLD_PACK = REGISTER.registerItem("cold_pack", props -> new ColdPackItem(props.stacksTo(16)));

    // Scope Attachments
    public static final DeferredItem<Item> LASER_SIGHT = REGISTER.registerItem("laser_sight", props -> new LaserSightItem(Attachments.LASER_SIGHT, props.stacksTo(1).durability(1300)));
    public static final DeferredItem<Item> LONG_SCOPE = REGISTER.registerItem("long_scope", props -> new ScopeItem(Attachments.LONG_SCOPE, props.stacksTo(1).durability(1600)));
    public static final DeferredItem<Item> MEDIUM_SCOPE = REGISTER.registerItem("medium_scope", props -> new ScopeItem(Attachments.MEDIUM_SCOPE, props.stacksTo(1).durability(1400)));
    public static final DeferredItem<Item> REFLEX_SIGHT = REGISTER.registerItem("reflex_sight", props -> new ScopeItem(Attachments.REFLEX_SIGHT, props.stacksTo(1).durability(1300)));
    // Stock Attachments
    public static final DeferredItem<Item> LIGHT_STOCK = REGISTER.registerItem("light_stock", props -> new StockItem(Stock.create(GunModifiers.LIGHT_STOCK_MODIFIER), props.stacksTo(1).durability(1350), false));
    public static final DeferredItem<Item> WEIGHTED_STOCK = REGISTER.registerItem("weighted_stock", props -> new StockItem(Stock.create(GunModifiers.WEIGHTED_STOCK_MODIFIER), props.stacksTo(1).durability(1700)));
    public static final DeferredItem<Item> WOODEN_STOCK = REGISTER.registerItem("wooden_stock", props -> new StockItem(Stock.create(GunModifiers.WOODEN_STOCK_MODIFIER), props.stacksTo(1).durability(1550), false));
    public static final DeferredItem<Item> BUMP_STOCK = REGISTER.registerItem("bump_stock", props -> new StockItem(Stock.create(GunModifiers.BUMP_STOCK_MODIFIER), props.stacksTo(1).durability(1200), true));
    // Barrel Attachments
    public static final DeferredItem<Item> SILENCER = REGISTER.registerItem("silencer", props -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCER_MODIFIER, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), props.stacksTo(1).durability(550)));
    public static final DeferredItem<Item> ADVANCED_SILENCER = REGISTER.registerItem("advanced_silencer", props -> new BarrelItem(Barrel.create(0.0F, GunModifiers.ADVANCED_SILENCER_MODIFIER, GunModifiers.SILENCED), props.stacksTo(1).durability(1200)));
    public static final DeferredItem<Item> MUZZLE_BRAKE = REGISTER.registerItem("muzzle_brake", props -> new BarrelItem(Barrel.create(0.0F, GunModifiers.MUZZLE_BRAKE_MODIFIER), props.stacksTo(1).durability(1400)));
    public static final DeferredItem<Item> EXTENDED_BARREL = REGISTER.registerItem("extended_barrel", props -> new ExtendedBarrelItem(Barrel.create(0.0F, GunModifiers.EXTENDED_BARREL_MODIFIER), props.stacksTo(1).durability(700)));

    // Under Barrel Attachments
    public static final DeferredItem<Item> LIGHT_GRIP = REGISTER.registerItem("light_grip", props -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), props.stacksTo(1).durability(1400)));
    public static final DeferredItem<Item> VERTICAL_GRIP = REGISTER.registerItem("vertical_grip", props -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), props.stacksTo(1).durability(1600)));

    public static final DeferredItem<Item> IRON_BAYONET = REGISTER.registerItem("iron_bayonet", props -> new BayonetItem(UnderBarrel.create(GunModifiers.IRON_BAYONET_DAMAGE), props.stacksTo(1).durability(256), 1.5f, -3.0f));
    public static final DeferredItem<Item> ANTHRALITE_BAYONET = REGISTER.registerItem("anthralite_bayonet", props -> new BayonetItem(UnderBarrel.create(GunModifiers.ANTHRALITE_BAYONET_DAMAGE), props.stacksTo(1).durability(512), 2.0f, -3.0f));
    public static final DeferredItem<Item> DIAMOND_BAYONET = REGISTER.registerItem("diamond_bayonet", props -> new BayonetItem(UnderBarrel.create(GunModifiers.DIAMOND_BAYONET_DAMAGE), props.stacksTo(1).durability(1024), 3.0f, -3.0f));
    public static final DeferredItem<Item> NETHERITE_BAYONET = REGISTER.registerItem("netherite_bayonet", props -> new BayonetItem(UnderBarrel.create(GunModifiers.NETHERITE_BAYONET_DAMAGE), props.stacksTo(1).durability(1550), 4.0f, -3.0f));

    //Magazines
    public static final DeferredItem<Item> EXTENDED_MAG = REGISTER.registerItem("extended_mag", props -> new MagazineItem(Magazine.create(GunModifiers.EXTENDED_MAG_MODIFIER), props.stacksTo(1).durability(1700)));
    public static final DeferredItem<Item> SPEED_MAG = REGISTER.registerItem("speed_mag", props -> new MagazineItem(Magazine.create(GunModifiers.SPEED_MAG_MODIFIER), props.stacksTo(1).durability(1550)));
    public static final DeferredItem<Item> PLUS_P_MAG = REGISTER.registerItem("plus_p_mag", props -> new MagazineItem(Magazine.create(GunModifiers.INCREASED_DAMAGE, GunModifiers.PLUS_P_MAG), props.stacksTo(1).durability(1400)));
    //ITEMS
    public static final DeferredItem<Item> REPAIR_KIT = REGISTER.registerItem("repair_kit", props -> new Item(props));
    public static final DeferredItem<Item> TURRET_PLATFORM = REGISTER.registerItem("turret_platform", props -> new Item(props.stacksTo(16)));
    public static final DeferredItem<Item> SCAMP_PACKAGE = REGISTER.registerItem("scamp_package", props -> new ScampPackageItem(props.stacksTo(1)));
    public static final DeferredItem<Item> VIVENTRUM_PACKAGE = REGISTER.registerItem("viventrum_package", props -> new ViventrumPackageItem(props.stacksTo(1)));
    public static final DeferredItem<Item> COG_LOCATOR = REGISTER.registerItem("cog_locator", props -> new CogLocatorItem(props.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> THE_PACT = REGISTER.registerItem("the_pact", props -> new ThePactItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    // Mobs
    public static final DeferredItem<Item> COG_MINION_SPAWN_EGG = REGISTER.registerItem("cog_minion_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.COG_MINION.get())));
    public static final DeferredItem<Item> COG_KNIGHT_SPAWN_EGG = REGISTER.registerItem("cog_knight_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.COG_KNIGHT.get())));
    public static final DeferredItem<Item> TRAUMA_UNIT_SPAWN_EGG = REGISTER.registerItem("trauma_unit_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.TRAUMA_UNIT.get())));
    public static final DeferredItem<Item> SKY_CARRIER_SPAWN_EGG = REGISTER.registerItem("sky_carrier_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SKY_CARRIER.get())));
    public static final DeferredItem<Item> SUPPLY_SCAMP_SPAWN_EGG = REGISTER.registerItem("supply_scamp_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SUPPLY_SCAMP.get())));
    public static final DeferredItem<Item> REDCOAT_SPAWN_EGG = REGISTER.registerItem("redcoat_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.REDCOAT.get())));
    public static final DeferredItem<Item> DISSIDENT_SPAWN_EGG = REGISTER.registerItem("dissident_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.DISSIDENT.get())));
    public static final DeferredItem<Item> HIVE_SPAWN_EGG = REGISTER.registerItem("hive_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.HIVE.get())));
    public static final DeferredItem<Item> SWARM_SPAWN_EGG = REGISTER.registerItem("swarm_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SWARM.get())));
    public static final DeferredItem<Item> HORNLIN_SPAWN_EGG = REGISTER.registerItem("hornlin_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.HORNLIN.get())));
    public static final DeferredItem<Item> ZOMBIFIED_HORNLIN_SPAWN_EGG = REGISTER.registerItem("zombified_hornlin_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.ZOMBIFIED_HORNLIN.get())));
    public static final DeferredItem<Item> BLUNDERER_SPAWN_EGG = REGISTER.registerItem("blunderer_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.BLUNDERER.get())));
    public static final DeferredItem<Item> ADJUDICATOR_SPAWN_EGG = REGISTER.registerItem("adjudicator_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.ADJUDICATOR.get())));
    public static final DeferredItem<Item> SUBJUGATOR_SPAWN_EGG = REGISTER.registerItem("subjugator_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SUBJUGATOR.get())));
    public static final DeferredItem<Item> FINFORCER_SPAWN_EGG = REGISTER.registerItem("finforcer_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.FINFORCER.get())));
    public static final DeferredItem<Item> PRAETOR_SPAWN_EGG = REGISTER.registerItem("praetor_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.PRAETOR.get())));
    public static final DeferredItem<Item> MOTHER_GHAST_SPAWN_EGG = REGISTER.registerItem("mother_ghast_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.MOTHER_GHAST.get())));
    public static final DeferredItem<Item> VIVENTRUM_SPAWN_EGG = REGISTER.registerItem("viventrum_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.VIVENTRUM.get())));
    public static final DeferredItem<Item> SULFURHEAD_SPAWN_EGG = REGISTER.registerItem("sulfurhead_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SULFURHEAD.get())));
    public static final DeferredItem<Item> THE_MERCHANT_SPAWN_EGG = REGISTER.registerItem("the_merchant_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.THE_MERCHANT.get())));
    public static final DeferredItem<Item> SIGNAL_BEACON_SPAWN_EGG = REGISTER.registerItem("signal_beacon_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SIGNAL_BEACON.get())));
    public static final DeferredItem<Item> SCAMP_TANK_SPAWN_EGG = REGISTER.registerItem("scamp_tank_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SCAMP_TANK.get())));
    public static final DeferredItem<Item> SCAMPLER_SPAWN_EGG = REGISTER.registerItem("scampler_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.SCAMPLER.get())));
    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }


}





