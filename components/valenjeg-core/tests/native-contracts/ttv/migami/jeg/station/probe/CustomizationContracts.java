package ttv.migami.jeg.station.probe;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.NbtOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import ttv.migami.jeg.item.attachment.*;
import ttv.migami.jeg.item.*;
import ttv.migami.jeg.gun.GunScopeSupport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.MagazineItem;
import ttv.migami.jeg.init.ModDataComponents;
import ttv.migami.jeg.mteg.MtegGunItem;

/** Actual loaded-registry compatibility audit, isolated from owner/player inventories. */
public final class CustomizationContracts {
 public static void register(){
  if(Boolean.getBoolean("valen.customization.audit"))ServerLifecycleEvents.SERVER_STARTED.register(CustomizationContracts::run);
 }
 private static void run(MinecraftServer server){
  JsonObject result=new JsonObject();JsonArray rows=new JsonArray(),failures=new JsonArray();result.add("guns",rows);result.add("failures",failures);
  try{
   Path root=Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
   if(!server.getServerDirectory().toRealPath().equals(root.toRealPath()))throw new IllegalStateException("Customization audit refuses owner server");
   boolean mode=ttv.migami.jeg.Config.magazineFeedEnabled();result.addProperty("magazine_feed",mode);
   for(var item:BuiltInRegistries.ITEM){
    if(!(item instanceof GunItem gun))continue;
    var stats=gun.getStats();MagazineItem.MagazineType family=null;
    if(stats.usesMagazine()&&!(gun instanceof MtegGunItem)&&gun.usesLoadedAmmo())for(var type:MagazineItem.MagazineType.values())if(type.baseType()==type&&type.capacity()==stats.magazineSize()&&stats.ammoItem()!=null&&type.supports(stats.ammoItem())){
     if(family!=null)throw new AssertionError("Ambiguous native magazine family "+stats.id());family=type;
    }
    boolean expected=mode&&family!=null;
    JsonObject row=new JsonObject();String id=BuiltInRegistries.ITEM.getKey(item).toString();row.addProperty("id",id);row.addProperty("reload",stats.reloadType());row.addProperty("capacity",stats.magazineSize());row.addProperty("ammo",String.valueOf(stats.ammoItem()));row.addProperty("expected_physical",expected);row.addProperty("actual_physical",gun.usesMagazineSwapReload());row.addProperty("expected_family",String.valueOf(family));rows.add(row);
    if(gun.usesMagazineSwapReload()!=expected)failures.add(id+" native reload classification expected "+expected+" actual "+gun.usesMagazineSwapReload());
    for(var magazineItem:BuiltInRegistries.ITEM){
     if(!(magazineItem instanceof MagazineItem mag))continue;
     ItemStack loaded=new ItemStack(magazineItem);loaded.set(ModDataComponents.MAGAZINE_AMMO_ITEM.get(),String.valueOf(stats.ammoItem()));loaded.set(ModDataComponents.MAGAZINE_AMMO_COUNT.get(),1);
     boolean match=expected&&mag.type().isVariantOf(family);boolean actual=gun.isLoadedMagazineCompatible(loaded);
     if(actual!=match)failures.add(id+" / "+BuiltInRegistries.ITEM.getKey(magazineItem)+" native loaded magazine expected "+match+" actual "+actual);
     loaded.set(ModDataComponents.MAGAZINE_AMMO_COUNT.get(),0);
     if(gun.isLoadedMagazineCompatible(loaded))failures.add(id+" accepted an empty magazine");
    }
   }
   JsonArray cosmeticRows=new JsonArray();result.add("cosmetics",cosmeticRows);
   JsonObject descriptors;
   try(var in=CustomizationContracts.class.getResourceAsStream("/assets/mteg/valen/item-model-descriptors.json")){
    descriptors=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
   }
   var ops=RegistryOps.create(NbtOps.INSTANCE,server.registryAccess());
   ItemStack combat=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("jeg:combat_scope")));
   int paintChecks=0,dyeChecks=0,scopeChecks=0,roundTrips=0;
   for(var item:BuiltInRegistries.ITEM){
    if(!(item instanceof GunItem gun))continue;
    var id=BuiltInRegistries.ITEM.getKey(item);
    for(var candidate:BuiltInRegistries.ITEM){
     if(!(candidate instanceof PaintJobCanItem can) && !(candidate instanceof net.minecraft.world.item.DyeItem))continue;
     boolean paint=candidate instanceof PaintJobCanItem;AttachmentType type=paint?AttachmentType.PAINT_JOB:AttachmentType.DYE;
     String skin=paint?((PaintJobCanItem)candidate).paintJob():null;
     boolean expected=paint?CustomizationContracts.class.getResource("/assets/"+id.getNamespace()+"/textures/animated/gun/paintjob/"+skin+"/"+id.getPath()+".png")!=null
        :id.getNamespace().equals("mteg")&&descriptors.has(id.getPath())&&descriptors.getAsJsonObject(id.getPath()).has("tinted")&&descriptors.getAsJsonObject(id.getPath()).get("tinted").getAsBoolean();
     ItemStack target=new ItemStack(item),attachment=new ItemStack(candidate);
     boolean accepted=GunAttachments.canAttachStack(target,type,attachment);
     boolean stored=GunAttachments.setCosmetic(target,type,attachment);
     if(accepted!=expected||stored!=expected)failures.add(id+" / "+BuiltInRegistries.ITEM.getKey(candidate)+" cosmetic support expected "+expected+" accept="+accepted+" write="+stored);
     if(stored){
      ItemStack decoded=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,target).getOrThrow()).getOrThrow();
      if(!GunAttachments.id(decoded,type).orElseThrow().equals(BuiltInRegistries.ITEM.getKey(candidate)))failures.add(id+" lost cosmetic id on native save/load");
      if(!net.minecraft.world.item.ItemStack.isSameItemSameComponents(attachment,GunAttachments.stack(decoded,type).orElseThrow()))failures.add(id+" lost cosmetic stack on native save/load");
      GunAttachments.clear(decoded,type);if(GunAttachments.has(decoded,type))failures.add(id+" clear left a cosmetic component");roundTrips++;
     }
     if(paint)paintChecks++;else dyeChecks++;
    }
    ItemStack target=new ItemStack(item);
    boolean support=GunAttachmentRules.canAttach(target,AttachmentType.SCOPE);boolean accepted=GunAttachments.set(target,AttachmentType.SCOPE,combat);
    if(support!=accepted || support&&!GunScopeSupport.hasMagnifiedOptic(target))failures.add(id+" Combat Scope install/magnified state differs from scope rail support");scopeChecks++;
    JsonObject row=new JsonObject();row.addProperty("gun",id.toString());row.addProperty("combat_scope_supported",support);row.addProperty("paint_slot",GunAttachments.canUseAttachmentSlot(new ItemStack(item),AttachmentType.PAINT_JOB));row.addProperty("dye_slot",GunAttachments.canUseAttachmentSlot(new ItemStack(item),AttachmentType.DYE));cosmeticRows.add(row);
   }
   result.addProperty("paint_pairs_checked",paintChecks);result.addProperty("dye_pairs_checked",dyeChecks);result.addProperty("scope_guns_checked",scopeChecks);result.addProperty("native_cosmetic_save_load_roundtrips",roundTrips);
   result.addProperty("registered_guns",rows.size());result.addProperty("world_or_inventory_mutated",false);result.addProperty("status",failures.isEmpty()?"pass":"fail");
   Files.writeString(root.resolve("valen-customization-native-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
  }catch(Throwable error){error.printStackTrace();throw new IllegalStateException(error);}
 }
}
