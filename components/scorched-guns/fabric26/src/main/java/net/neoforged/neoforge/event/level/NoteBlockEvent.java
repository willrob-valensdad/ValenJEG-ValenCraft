package net.neoforged.neoforge.event.level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.Event;
public class NoteBlockEvent extends Event {
    public static class Play extends NoteBlockEvent {
        private final Level level;
        private final BlockPos pos;
        private final NoteBlockInstrument instrument;
        public Play(Level level, BlockPos pos, NoteBlockInstrument instrument) {
            this.level = level;
            this.pos = pos;
            this.instrument = instrument;
        }
        public Level getLevel() { return level; }
        public BlockPos getPos() { return pos; }
        public NoteBlockInstrument getInstrument() { return instrument; }
    }
}
