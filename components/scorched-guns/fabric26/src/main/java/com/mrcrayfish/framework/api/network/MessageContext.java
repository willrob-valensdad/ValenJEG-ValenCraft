package com.mrcrayfish.framework.api.network;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
public class MessageContext {
    private final Player player;
    public MessageContext(Player player) { this.player = player; }
    public void execute(Runnable work) { work.run(); }
    public Optional<Player> getPlayer() { return Optional.ofNullable(player); }
    public void setHandled(boolean handled) {}
}
