package top.ribs.scguns.init;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import top.ribs.scguns.Reference;
import top.ribs.scguns.compat.ArmorItem;

import java.util.EnumMap;
import java.util.Map;

/** Minecraft 26.2 armor material definitions. */
public final class ModArmorMaterials {
    public static final Holder<ArmorMaterial> ADRIEN = register("adrien", 22, new int[]{3, 6, 6, 4}, 8, SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.1f);
    public static final Holder<ArmorMaterial> ANTHRALITE = register("anthralite", 32, new int[]{2, 4, 3, 2}, 12, SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 0.05f);
    public static final Holder<ArmorMaterial> DIAMOND_STEEL = register("diamond_steel", 36, new int[]{3, 6, 5, 3}, 16, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0f, 0.05f);
    public static final Holder<ArmorMaterial> TREATED_BRASS = register("treated_brass", 30, new int[]{4, 6, 5, 4}, 10, SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.2f);
    public static final Holder<ArmorMaterial> ANCIENT_BRASS = register("ancient_brass", 16, new int[]{3, 5, 4, 3}, 10, SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.15f);
    public static final Holder<ArmorMaterial> EXO_SUIT = register("exo_suit", 200, new int[]{1, 1, 1, 1}, 6, SoundEvents.ARMOR_EQUIP_NETHERITE, 0.0f, 0.0f);
    public static final Holder<ArmorMaterial> REDCOAT = register("redcoat", 12, new int[]{2, 4, 3, 2}, 9, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f);
    public static final Holder<ArmorMaterial> SCRAP = register("scrap", 6, new int[]{5, 6, 6, 4}, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.05f);

    private ModArmorMaterials() {}

    // Durability is now installed by Item.Properties.humanoidArmor in the compatibility ArmorItem.
    public static Item.Properties withDurability(Item.Properties properties, Holder<ArmorMaterial> material, ArmorItem.Type type) {
        return properties;
    }

    private static Holder<ArmorMaterial> register(String name, int durability, int[] defense, int enchantmentValue,
                                                   Holder<SoundEvent> equipSound, float toughness, float knockbackResistance) {
        EnumMap<ArmorType, Integer> defenseMap = new EnumMap<>(ArmorType.class);
        defenseMap.put(ArmorType.BOOTS, defense[0]);
        defenseMap.put(ArmorType.LEGGINGS, defense[1]);
        defenseMap.put(ArmorType.CHESTPLATE, defense[2]);
        defenseMap.put(ArmorType.HELMET, defense[3]);
        defenseMap.put(ArmorType.BODY, defense[2]);
        TagKey<Item> repairTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "repairs_" + name + "_armor"));
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Reference.MOD_ID, name));
        return Holder.direct(new ArmorMaterial(durability, defenseMap, enchantmentValue, equipSound, toughness, knockbackResistance, repairTag, asset));
    }
}
