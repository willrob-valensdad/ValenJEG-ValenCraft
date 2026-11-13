package ttv.migami.jeg.tech.astra.oxygen.ui;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import team.reborn.energy.api.EnergyStorageUtil;
/** Actual installed native item providers; no substitute suit reserve or gas-item API. */
public final class OxygenItemRouting {
 private OxygenItemRouting(){}
 public static int inputSlot(ItemStack stack){
  if(stack.isEmpty())return -1;
  if(EnergyStorageUtil.isEnergyStorage(stack))return 0;
  var fluids=ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
  if(fluids!=null)for(var view:fluids)if(view.getAmount()>0&&view.getResource().equals(FluidVariant.of(Fluids.WATER)))return 1;
  return -1;
 }
}
