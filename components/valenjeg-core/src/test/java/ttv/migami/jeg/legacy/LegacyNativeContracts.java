package ttv.migami.jeg.legacy;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.*;
import ttv.migami.jeg.gun.GunDefinitions;
import ttv.migami.jeg.item.GunItem;
/** Native registry/item/codec/portable migration contracts. No client/world fixture is launched. */
public final class LegacyNativeContracts {
    private static void require(boolean value,String message) { if(!value)throw new AssertionError(message); }
    public static void run(HolderLookup.Provider lookup) throws Exception {
        for(String id:List.of("ammo_box","compound_bow","atlantean_spear","explosive_charge"))require(BuiltInRegistries.ITEM.containsKey(Identifier.parse("jeg:"+id)),"Missing original item "+id);
        Item bow=BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:compound_bow"));
        Item spear=BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:atlantean_spear"));
        require(bow instanceof CompoundBowItem && spear instanceof AtlanteanSpearItem,"Original weapon factory was bypassed");
        require(bow.getDefaultInstance().getMaxDamage()==128 && spear.getDefaultInstance().getMaxDamage()==128,"Original weapon durability changed");
        require(LegacyContent.EXPLOSIVE_CHARGE.getDefaultInstance().getMaxStackSize()==2 && LegacyContent.EXPLOSIVE_CHARGE.getDefaultInstance().getRarity()==Rarity.EPIC,"Original charge properties changed");
        for(String name:List.of("compound_bow","atlantean_spear")) {
            JsonObject original=JsonParser.parseString(Files.readString(Path.of("provenance/legacy-items/"+name+".original.json"))).getAsJsonObject();
            var stats=GunDefinitions.ALL.get(Identifier.parse("jeg:"+name));
            var projectile=original.getAsJsonObject("projectile");var reloads=original.getAsJsonObject("reloads");
            require(stats.damage()==projectile.get("damage").getAsFloat()&&stats.projectileSpeed()==projectile.get("speed").getAsFloat()&&stats.projectileLife()==projectile.get("life").getAsInt(),"Original projectile stats diverged: "+name);
            require(stats.magazineSize()==reloads.get("maxAmmo").getAsInt()&&stats.reloadTime()==reloads.get("reloadTimer").getAsInt()&&stats.additionalReloadTime()==reloads.get("additionalReloadTimer").getAsInt(),"Original reload stats diverged: "+name);
        }
        require(CompoundBowItem.charge(-1)==0F && CompoundBowItem.charge(0)==0F && CompoundBowItem.charge(10)==.5F && CompoundBowItem.charge(20)==1F && CompoundBowItem.charge(72000)==1F,"Native draw timing boundaries changed");
        var state=LegacyContent.AMMO_BOX.defaultBlockState();
        var box=new AmmoBoxBlockEntity(BlockPos.ZERO,state);
        require(box.getContainerSize()==15,"Ammo UI/storage mismatch");
        for(Direction side:Direction.values()) {
            require(Arrays.equals(box.getSlotsForFace(side),java.util.stream.IntStream.range(0,15).toArray()),"Sided slots changed");
            require(box.canPlaceItemThroughFace(0,new ItemStack(Items.ARROW),side),"Arrow rejected through hopper");
            require(box.canPlaceItemThroughFace(0,new ItemStack(LegacyContent.EXPLOSIVE_CHARGE),side),"Charge rejected through hopper");
            require(!box.canPlaceItemThroughFace(0,new ItemStack(Items.DIAMOND),side),"Automation bypasses ammo filter");
            require(!box.canPlaceItemThroughFace(0,new ItemStack(LegacyContent.AMMO_BOX_ITEM),side),"Recursive ammo-box storage");
            require(box.canTakeItemThroughFace(0,new ItemStack(Items.ARROW),side),"Extraction changed");
        }
        for(int i=0;i<15;i++)box.setItem(i,new ItemStack(Items.ARROW,i+1));
        ItemStack named=new ItemStack(LegacyContent.AMMO_BOX_ITEM);named.set(DataComponents.CUSTOM_NAME,Component.literal("Preserved ammo"));box.applyComponentsFromItemStack(named);
        // applyComponentsFromItemStack also applies container; refill after the intentionally empty named item.
        for(int i=0;i<15;i++)box.setItem(i,new ItemStack(Items.ARROW,i+1));
        var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);box.saveCustomOnly(output);
        var loaded=new AmmoBoxBlockEntity(BlockPos.ZERO,state);loaded.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING,lookup,output.buildResult()));
        for(int i=0;i<15;i++)require(loaded.getItem(i).getCount()==i+1,"Ammo save/load changed slot "+i);
        require(Component.literal("Preserved ammo").equals(loaded.getCustomName()),"Ammo name lost");
        ItemStack portable=new ItemStack(LegacyContent.AMMO_BOX_ITEM);portable.applyComponents(box.collectComponents());
        var replaced=new AmmoBoxBlockEntity(BlockPos.ZERO,state);replaced.applyComponentsFromItemStack(portable);
        for(int i=0;i<15;i++)require(replaced.getItem(i).getCount()==i+1,"Portable item lost slot "+i);
        require(Component.literal("Preserved ammo").equals(replaced.getCustomName()),"Portable name lost");
        var all=NonNullList.withSize(27,ItemStack.EMPTY);all.set(0,new ItemStack(Items.ARROW,3));all.set(15,new ItemStack(Items.DIAMOND,7));all.set(26,new ItemStack(Items.SPECTRAL_ARROW,11));
        var legacy=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);ContainerHelper.saveAllItems(legacy,all);
        var migrated=new AmmoBoxBlockEntity(BlockPos.ZERO,state);migrated.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING,lookup,legacy.buildResult()));
        var savedAgain=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);migrated.saveCustomOnly(savedAgain);
        var readBack=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING,lookup,savedAgain.buildResult()),readBack);
        require(readBack.get(15).getCount()==7 && readBack.get(26).getCount()==11,"Legacy hidden slots were truncated on disk");
        ItemStack packed=new ItemStack(LegacyContent.AMMO_BOX_ITEM);packed.applyComponents(migrated.collectComponents());
        var componentItems=NonNullList.withSize(27,ItemStack.EMPTY);packed.get(DataComponents.CONTAINER).copyInto(componentItems);
        require(componentItems.get(15).getCount()==7 && componentItems.get(26).getCount()==11,"Legacy hidden slots were truncated in portable item");
        var onlyOverflow=new AmmoBoxBlockEntity(BlockPos.ZERO,state);ItemStack overflowStack=new ItemStack(LegacyContent.AMMO_BOX_ITEM);overflowStack.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(all));onlyOverflow.applyComponentsFromItemStack(overflowStack);onlyOverflow.setItem(0,ItemStack.EMPTY);
        require(!onlyOverflow.isEmpty(),"Creative break would lose overflow-only box");
        int signal=net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(box);require(signal>0,"Ammo comparator always empty");
        var ops=RegistryOps.create(JsonOps.INSTANCE,lookup);
        JsonElement encoded=ItemStack.CODEC.encodeStart(ops,packed).getOrThrow();ItemStack decoded=ItemStack.CODEC.parse(ops,encoded).getOrThrow();
        require(ItemStack.matches(packed,decoded),"Native portable stack codec lost contents");
        System.out.println("JEG_LEGACY_NATIVE_CONTRACTS_PASS: 4 original IDs, source stats, charge boundaries, 6 automation faces,15slots, names, portable components, legacy overflow, comparator, native item codec");
    }
}
