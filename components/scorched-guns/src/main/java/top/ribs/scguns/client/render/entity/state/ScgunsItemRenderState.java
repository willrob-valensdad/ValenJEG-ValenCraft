package top.ribs.scguns.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

/**
 * Shared extracted render state for Scorched Guns item-backed projectiles.
 * Minecraft 26.2 resolves item geometry during extraction and submits it later.
 */
public final class ScgunsItemRenderState extends EntityRenderState {
    public final ItemStackRenderState item = new ItemStackRenderState();
    public ItemStack stack = ItemStack.EMPTY;
    public float yRot;
    public float xRot;
    public boolean renderItem;
}
