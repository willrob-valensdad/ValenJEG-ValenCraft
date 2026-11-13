package ttv.migami.jeg.station;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Original JEG station recipe format, with native 26.3 item/tag resolution. */
public record StationRecipe(Identifier id, String station, List<Material> materials,
        Identifier result, int count, int ticks, float experience, String json) {
    public record Material(Identifier id, boolean tag, int count) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && (tag ? stack.is(TagKey.create(Registries.ITEM, id))
                    : stack.is(BuiltInRegistries.ITEM.getValue(id)));
        }
    }
    public static StationRecipe parse(Identifier id, JsonObject o) {
        String rawType=o.get("type").getAsString();
        String type=rawType.equals("mteg:mo_workbench") ? "mo_workbench" : rawType.replace("jeg:","");
        List<Material> materials = new ArrayList<>();
        if (o.has("materials")) {
            for (JsonElement e : o.getAsJsonArray("materials")) materials.add(material(e.getAsJsonObject()));
        } else materials.add(material(o.getAsJsonObject("ingredient")));
        JsonElement output = o.get("result");
        JsonObject result = output.isJsonObject() ? output.getAsJsonObject() : new JsonObject();
        String item = output.isJsonPrimitive() ? output.getAsString() :
                result.get(result.has("id") ? "id" : "item").getAsString();
        int count = result.has("count") ? result.get("count").getAsInt() : 1;
        int ticks = o.has("recyclingtime") ? o.get("recyclingtime").getAsInt() : o.has("time") ? o.get("time").getAsInt() : 30;
        if (!Set.of("scrap_workbench","gunmetal_workbench","gunnite_workbench","blueprint_workbench","recycling","mo_workbench").contains(type)) throw new IllegalArgumentException("Unknown station "+type);
        if (type.equals("recycling") && (materials.size()!=1 || materials.get(0).count()!=1)) throw new IllegalArgumentException("Recycling requires one input");
        if (materials.isEmpty() || count < 1 || count > 64 || ticks < 1) throw new IllegalArgumentException("Invalid station recipe " + id);
        return new StationRecipe(id, type.equals("recycling") ? "recycler" : type, List.copyOf(materials),
                Identifier.parse(item), count, ticks, o.has("experience") ? o.get("experience").getAsFloat() : 0, o.toString());
    }
    private static Material material(JsonObject o) {
        boolean tag = o.has("tag");
        int n = o.has("count") ? o.get("count").getAsInt() : 1;
        if (n < 1 || n > 4096) throw new IllegalArgumentException("Invalid ingredient count");
        Identifier id=Identifier.parse(o.get(tag ? "tag" : "item").getAsString());
        if(tag && id.getNamespace().equals("forge"))id=Identifier.parse(switch(id.toString()) {
            case "forge:ingots/iron" -> "c:ingots/iron";
            case "forge:ingots/gold" -> "c:ingots/gold";
            case "forge:ingots/copper" -> "c:ingots/copper";
            case "forge:nuggets/iron" -> "c:nuggets/iron";
            case "forge:dyes/black" -> "c:dyes/black";
            case "forge:gunpowder" -> "c:gunpowders";
            case "forge:sandstone" -> "c:sandstone/blocks";
            default -> throw new IllegalArgumentException("Unmapped original Forge material tag "+id);
        });
        return new Material(id, tag, n);
    }
    public ItemStack output() {
        if (!BuiltInRegistries.ITEM.containsKey(result)) throw new IllegalStateException("Missing station result " + result);
        return new ItemStack(BuiltInRegistries.ITEM.getValue(result), count);
    }
    /** Only the displayed36 main/hotbar slots fund player crafting. Generic containers retain their declared size. */
    private static int budgetSize(Container inventory) {
        return inventory instanceof net.minecraft.world.entity.player.Inventory ? Math.min(36,inventory.getContainerSize()) : inventory.getContainerSize();
    }
    public int[] plan(Container inventory) {
        int[][] allocation=allocate(inventory);
        for(int missing:allocation[1])if(missing!=0)return null;
        return allocation[0];
    }
    /** Same allocation as the server transaction, so overlapping UI rows cannot all claim one shared stack. */
    public int[] deficits(Container inventory) {return allocate(inventory)[1];}
    private int[][] allocate(Container inventory) {
        int[] remaining=new int[budgetSize(inventory)],consumed=new int[remaining.length],missing=new int[materials.size()];
        for(int i=0;i<remaining.length;i++)remaining[i]=inventory.getItem(i).getCount();
        List<Integer> ordered=new ArrayList<>();
        for(int i=0;i<materials.size();i++)ordered.add(i);
        ordered.sort(Comparator.comparing(i->materials.get(i).tag()));
        for(int materialIndex:ordered) {
            Material ingredient=materials.get(materialIndex);int need=ingredient.count();
            for(int i=0;i<remaining.length&&need>0;i++) {
                if(!ingredient.matches(inventory.getItem(i)))continue;
                int take=Math.min(need,remaining[i]);remaining[i]-=take;consumed[i]+=take;need-=take;
            }
            missing[materialIndex]=need;
        }
        return new int[][]{consumed,missing};
    }
}
