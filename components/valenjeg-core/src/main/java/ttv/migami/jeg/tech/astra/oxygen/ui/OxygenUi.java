package ttv.migami.jeg.tech.astra.oxygen.ui;
import net.fabricmc.fabric.api.menu.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import ttv.migami.jeg.tech.astra.oxygen.*;
/** Native menu registration after the optional existing oxygen bootstrap; no custom input transport. */
public final class OxygenUi {
 public static MenuType<OxygenLoaderMenu> MENU;
 private OxygenUi(){}
 public static void register(){
  if(AstraOxygen.Content.BLOCK==null)return;
  if(MENU!=null)throw new IllegalStateException("Oxygen menu registered twice");
  MENU=Registry.register(BuiltInRegistries.MENU,Identifier.parse("jeg:valen_oxygen_loader"),
   new ExtendedMenuType<>((id,inv,pos)->new OxygenLoaderMenu(id,inv,pos),BlockPos.STREAM_CODEC));
 }
 public static void open(Player player,OxygenLoaderBlockEntity owner){
  if(!(player instanceof ServerPlayer server)||MENU==null)return;
  server.openMenu(new ExtendedMenuProvider<BlockPos>(){
   public BlockPos getScreenOpeningData(ServerPlayer ignored){return owner.getBlockPos();}
   public Component getDisplayName(){return Component.translatable("block.jeg.valen_oxygen_loader");}
   public AbstractContainerMenu createMenu(int id,Inventory inv,Player actor){return new OxygenLoaderMenu(id,inv,owner);}
  });
 }
}
