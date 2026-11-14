package top.ribs.scguns.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Native 26.2 render-type facade for Scorched Guns custom geometry. */
public final class GunRenderType {
    private GunRenderType() {}

    public static RenderType getBulletTrail() {
        return RenderTypes.lightning();
    }

    public static RenderType getMuzzleFlash(Identifier flashTexture) {
        return RenderTypes.entityTranslucentEmissive(flashTexture);
    }
}
