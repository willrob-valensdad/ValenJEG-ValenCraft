package ttv.migami.jeg.tech.astra.oxygen.ui;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import ttv.migami.jeg.tech.astra.oxygen.*;
import za.co.neroland.nerospace.gas.*;
import za.co.neroland.nerospace.storage.CoreTankBridge;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;
/** Actual native gas input, separate from processed output. Foreign tank debit never claims rollback. */
public final class OxygenGasIntake {
 private final OxygenLoaderProcess process;
 private final NerospaceGasStorage space=new NerospaceGasStorage(){
  public GasResource getGas(){return process.inputGas()==0?GasResource.EMPTY:GasResource.OXYGEN;}
  public long getAmount(){return process.inputGas();}
  public long getCapacity(){return OxygenLoaderProcess.TANK_MB;}
  public long drain(long maximum,boolean simulate){return 0;}
  public long fill(GasResource gas,long maximum,boolean simulate){
   if(!GasResource.OXYGEN.equals(gas)||maximum<=0)return 0;
   long amount=Math.min(maximum,process.water()>0?0:OxygenLoaderProcess.TANK_MB-process.inputGas());
   if(amount==0||simulate)return amount;
   try(var tx=Transaction.openNested(Transaction.getCurrentUnsafe())){long accepted=process.insertGas(amount,tx);tx.commit();return accepted;}
  }
 };
 private final NeroGasStorage core;
 public OxygenGasIntake(OxygenLoaderProcess process){this.process=process;this.core=CoreTankBridge.toCore(space);}
 public NerospaceGasStorage space(){return space;}
 public NeroGasStorage core(){return core;}
 public long pullFrom(NerospaceGasStorage source){
  if(source==null||source==space||!GasResource.OXYGEN.equals(source.getGas())||process.water()>0)return 0;
  if(Transaction.getCurrentUnsafe()!=null)throw new IllegalStateException("Foreign native gas debit cannot join Fabric parent transaction");
  long request=Math.min(200,OxygenLoaderProcess.TANK_MB-process.inputGas());
  if(request==0)return 0;
  long preview=source.drain(request,true);if(preview<=0)return 0;
  if(preview>request)throw new IllegalStateException("Foreign native gas preview exceeded request");
  long actual=source.drain(preview,false);if(actual<0||actual>preview)throw new IllegalStateException("Foreign native gas debit exceeded preview");
  try(var tx=Transaction.openOuter()){long inserted=process.insertGas(actual,tx);if(inserted!=actual)throw new IllegalStateException("Native gas debit/input credit mismatch");tx.commit();}
  return actual;
 }
}
