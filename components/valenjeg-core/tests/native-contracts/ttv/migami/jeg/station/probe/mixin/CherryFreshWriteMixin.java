package ttv.migami.jeg.station.probe.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import ttv.migami.jeg.station.probe.CherryFreshTrace;
/** Fixture jar only. Passes the original result through and records one fresh site's registered cells. */
@Mixin(WorldGenRegion.class)
public abstract class CherryFreshWriteMixin {
 @WrapMethod(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
 private boolean valen$traceFreshCherry(BlockPos pos,BlockState state,int flags,int extra,Operation<Boolean> original){
  return CherryFreshTrace.around((WorldGenRegion)(Object)this,pos,state,flags,extra,original);
 }
}
