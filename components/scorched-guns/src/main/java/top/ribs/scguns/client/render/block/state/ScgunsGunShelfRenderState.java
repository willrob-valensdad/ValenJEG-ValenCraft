package top.ribs.scguns.client.render.block.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

/**
 * Extracted 26.2 render data for the gun displayed on a Gun Shelf.
 */
public class ScgunsGunShelfRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean hasItem;
    public final ItemStackRenderState item = new ItemStackRenderState();
}
