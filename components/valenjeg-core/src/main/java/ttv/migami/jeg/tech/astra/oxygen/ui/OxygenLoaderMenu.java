package ttv.migami.jeg.tech.astra.oxygen.ui;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import ttv.migami.jeg.tech.astra.oxygen.OxygenLoaderBlockEntity;
/** Ad Astra's real battery/water/remainder slots and inventory offset; unsupported original gas-item pair omitted. */
public final class OxygenLoaderMenu extends AbstractContainerMenu {
 public final Container inventory;
 public final ContainerData data;
 private final OxygenLoaderBlockEntity owner;
 public OxygenLoaderMenu(int id,Inventory player,net.minecraft.core.BlockPos pos){
  this(id,player,new SimpleContainer(3),new SimpleContainerData(OxygenMenuData.COUNT),null);
 }
 public OxygenLoaderMenu(int id,Inventory player,OxygenLoaderBlockEntity owner){
  this(id,player,owner,new OxygenMenuData(owner),owner);
 }
 public OxygenLoaderMenu(int id,Inventory player,Container inventory,ContainerData data,OxygenLoaderBlockEntity owner){
  super(OxygenUi.MENU,id);checkContainerSize(inventory,3);checkContainerDataCount(data,OxygenMenuData.COUNT);
  this.inventory=inventory;this.data=data;this.owner=owner;
  addSlot(new Slot(inventory,0,150,22){public boolean mayPlace(ItemStack s){return OxygenItemRouting.inputSlot(s)==0;}});
  addSlot(new Slot(inventory,1,12,22){public boolean mayPlace(ItemStack s){return OxygenItemRouting.inputSlot(s)==1;}});
  addSlot(new Slot(inventory,2,12,52){public boolean mayPlace(ItemStack s){return false;}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(player,9+row*9+col,8+col*18,102+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(player,col,8+col*18,160));
  addDataSlots(data);
 }
 @Override public boolean stillValid(Player player){
  return player.level().isClientSide()||owner!=null&&owner.getLevel()==player.level()&&player.level().getBlockEntity(owner.getBlockPos())==owner&&owner.stillValid(player);
 }
 @Override public boolean clickMenuButton(Player player,int id){
  if(player.level().isClientSide()||player.containerMenu!=this||!stillValid(player)||!OxygenSideConfiguration.validButton(id))return false;
  int value=id-100;owner.sideConfig().toggle(value/6,Direction.values()[value%6]);owner.setChanged();broadcastChanges();return true;
 }
 @Override public ItemStack quickMoveStack(Player player,int index){
  if(index<0||index>=slots.size())return ItemStack.EMPTY;
  Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
  ItemStack stack=slot.getItem(),copy=stack.copy();
  if(index<3){if(!moveItemStackTo(stack,3,39,true))return ItemStack.EMPTY;}
  else{
   int target=OxygenItemRouting.inputSlot(stack);
   if(target>=0){if(!moveItemStackTo(stack,target,target+1,false))return ItemStack.EMPTY;}
   else if(index<30?!moveItemStackTo(stack,30,39,false):!moveItemStackTo(stack,3,30,false))return ItemStack.EMPTY;
  }
  if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
  if(stack.getCount()==copy.getCount())return ItemStack.EMPTY;
  slot.onTake(player,stack);return copy;
 }
 public long waterDroplets(){return OxygenMenuData.join(data.get(1),data.get(2));}
}
