package net.neoforged.neoforge.event;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;
/** Datapack sync for one player. Recipe types are the Scorched machine recipes JEI needs. */
public class OnDatapackSyncEvent extends Event {
    private final ServerPlayer player;
    private final List<RecipeType<?>> recipes = new ArrayList<>();
    public OnDatapackSyncEvent(ServerPlayer player) { this.player = player; }
    public ServerPlayer getPlayer() { return player; }
    public void sendRecipes(RecipeType<?>... types) {
        for (RecipeType<?> type : types) recipes.add(type);
    }
    public List<RecipeType<?>> recipes() { return recipes; }
}
