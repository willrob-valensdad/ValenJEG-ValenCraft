package ttv.migami.jeg.tech.astra.oxygen.ui;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerData;
import ttv.migami.jeg.tech.astra.oxygen.*;
import za.co.neroland.nerospace.platform.GasLookup;
/** Vanilla menu property packets carry signed16 values; preserve exact Fabric droplets with two words. */
public final class OxygenMenuData implements ContainerData {
 public static final int COUNT=14;
 private final OxygenLoaderBlockEntity owner;
 public OxygenMenuData(OxygenLoaderBlockEntity owner){this.owner=owner;}
 public int getCount(){return COUNT;}
 public int get(int i){
  if(i==0)return (int)owner.process().energy();
  if(i==1)return low(owner.process().water());
  if(i==2)return high(owner.process().water());
  if(i==3)return (int)owner.process().oxygen();
  if(i==4)return owner.process().progress();
  if(i==5)return OxygenLoadingRecipe.active()==null?0:OxygenLoadingRecipe.active().cookingTime();
  if(i>=6&&i<12)return owner.sideConfig().mask(i-6);
  if(i==12){int mask=0;var level=owner.getLevel();if(level!=null)for(Direction side:Direction.values()){
   var pos=owner.getBlockPos().relative(side);
   if(level.hasChunkAt(pos)&&GasLookup.INSTANCE.find(level,pos,side.getOpposite())!=null)mask|=1<<side.ordinal();
  }return mask;}
  if(i==13)return (int)owner.process().inputGas();
  return 0;
 }
 public void set(int i,int value){} // Server state is changed only by validated menu actions.
 public static int low(long value){return (int)value&65535;}
 public static int high(long value){return (int)(value>>>16)&65535;}
 public static long join(int low,int high){return ((long)(high&65535)<<16)|(low&65535L);}
}
