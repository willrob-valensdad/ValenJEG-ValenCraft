package top.ribs.scguns.client.render.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import top.ribs.scguns.client.handler.AimingHandler;

/**
 * Author: MrCrayfish
 */
public class TexturedCrosshair extends Crosshair
{
    private final Identifier texture;
    private final boolean blend;

    public TexturedCrosshair(Identifier id)
    {
        this(id, true);
    }

    public TexturedCrosshair(Identifier id, boolean blend)
    {
        super(id);
        this.texture = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/crosshair/" + id.getPath() + ".png");
        this.blend = blend;
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor graphics, int windowWidth, int windowHeight, float partialTicks)
    {
        float alpha = 1.0F - (float) AimingHandler.get().getNormalisedAdsProgress();
        int size = 8;
        int x = Math.round((windowWidth - size) / 2.0F);
        int y = Math.round((windowHeight - size) / 2.0F);

        graphics.blit(
                this.blend ? RenderPipelines.CROSSHAIR : RenderPipelines.GUI_TEXTURED,
                this.texture,
                x, y,
                0.0F, 0.0F,
                size, size,
                16, 16,
                16, 16,
                ARGB.white(alpha));
    }
}
