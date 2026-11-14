package net.neoforged.neoforge.fluids;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.Event;
public class RegisterCauldronFluidContentEvent extends Event {
    public static final List<Content> CONTENTS = new ArrayList<>();
    public void register(Block block, Fluid fluid, int amount, Object properties) {
        CONTENTS.add(new Content(block, fluid, amount));
    }
    public record Content(Block block, Fluid fluid, int amount) {}
    public static class Pre extends RegisterCauldronFluidContentEvent {}
    public static class Post extends RegisterCauldronFluidContentEvent {}
}
