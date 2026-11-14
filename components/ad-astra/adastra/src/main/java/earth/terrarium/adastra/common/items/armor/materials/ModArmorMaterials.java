package earth.terrarium.adastra.common.items.armor.materials;

import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.common.tags.ModItemTags;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public class ModArmorMaterials {
    public static final Holder<ArmorMaterial> SPACE_SUIT = register(
        "space_suit", 37,
        Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2, ArmorType.BODY, 5),
        14, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ModItemTags.STEEL_INGOTS
    );
    public static final Holder<ArmorMaterial> NETHERITE_SPACE_SUIT = register(
        "netherite_space_suit", 37,
        Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 7, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3, ArmorType.BODY, 11),
        15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, ItemTags.NETHERITE_TOOL_MATERIALS
    );
    public static final Holder<ArmorMaterial> JET_SUIT = register(
        "jet_suit", 42,
        Map.of(ArmorType.BOOTS, 4, ArmorType.LEGGINGS, 7, ArmorType.CHESTPLATE, 9, ArmorType.HELMET, 4, ArmorType.BODY, 11),
        15, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.1F, ModItemTags.CALORITE_INGOTS
    );

    private static Holder<ArmorMaterial> register(String name, int durability, Map<ArmorType, Integer> defense, int enchantment, Holder<SoundEvent> sound, float toughness, float knockback, TagKey<Item> repair) {
        ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, name)
        );
        return Holder.direct(new ArmorMaterial(durability, defense, enchantment, sound, toughness, knockback, repair, asset));
    }
}
