package top.ribs.scguns.fabric.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.fabric.ScorchedFabric;
@Mixin(NoteBlock.class)
public class ScorchedNoteBlockMixin {
    @Inject(method = "playNote", at = @At("HEAD"))
    private void scguns$play(Entity source, BlockState state, Level level, BlockPos pos, CallbackInfo callback) {
        ScorchedFabric.MOD_BUS.post(new NoteBlockEvent.Play(level, pos, state.getValue(NoteBlock.INSTRUMENT)));
    }
}
