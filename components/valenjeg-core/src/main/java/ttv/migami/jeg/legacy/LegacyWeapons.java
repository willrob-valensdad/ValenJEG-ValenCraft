package ttv.migami.jeg.legacy;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.gun.GunStats;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
/** Exact gameplay values from original generated gun definitions, not modern gun aliases. */
public final class LegacyWeapons {
    private LegacyWeapons() {}
    public static void addDefinitions(Map<Identifier,GunStats> map) {
        map.put(Reference.id("compound_bow"),new GunStats(Reference.id("compound_bow"),Identifier.parse("minecraft:arrow"),"jeg:inventory_fed",1,16,0,10,10F,8F,1000,true,false,0F,1,Reference.id("item.compound_bow.fire"),null,null,Reference.id("item.combat_rifle.reload_magazine_out"),Reference.id("item.compound_bow.place_arrow"),Reference.id("item.combat_rifle.reload_ejector"),Reference.id("item.compound_bow.pull"),Reference.id("item.compound_bow.charge"),.05F,-1,2F));
        map.put(Reference.id("atlantean_spear"),new GunStats(Reference.id("atlantean_spear"),Reference.id("water_bomb"),"jeg:manual",6,16,10,32,10F,3F,40,false,false,0F,1,Reference.id("item.typhoonee.fire"),null,Reference.id("item.typhoonee.fire"),Reference.id("item.pistol.reload"),Reference.id("item.pistol.cock"),null,null,null,.05F,56575,0F));
    }
    public static float meleeDamage(net.minecraft.world.item.ItemStack stack,float fallback) { return stack.getItem() instanceof AtlanteanSpearItem ? 8F/1.5F : fallback; }
    public static GunItem create(Identifier id,Item.Properties properties,GunStats stats) {
        return switch(id.getPath()) {
            case "compound_bow" -> new CompoundBowItem(properties.durability(128).rarity(Rarity.EPIC).enchantable(5),stats);
            case "atlantean_spear" -> new AtlanteanSpearItem(properties.durability(128).rarity(Rarity.EPIC),stats);
            default -> new AnimatedGunItem(properties,stats);
        };
    }
}
