package top.ribs.scguns.client.render.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.AimingHandler;

/**
 * Author: MrCrayfish
 */
public class TechCrosshair extends Crosshair
{
    private static final Identifier TECH_CROSSHAIR = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/crosshair/tech.png");
    private static final Identifier DOT_CROSSHAIR = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/crosshair/dot.png");

    private float scale;
    private float prevScale;
    private float rotation;
    private float prevRotation;

    public TechCrosshair()
    {
        super(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "tech"));
    }

    @Override
    public void tick()
    {
        this.prevRotation = this.rotation;
        this.prevScale = this.scale;
        this.rotation += 4;
        this.scale *= 0.75F;
    }

    @Override
    public void onGunFired()
    {
        this.scale = 1.5F;
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor graphics, int windowWidth, int windowHeight, float partialTicks)
    {
        float alpha = 1.0F - (float) AimingHandler.get().getNormalisedAdsProgress();
        int size = 8;
        int color = ARGB.white(alpha);

        int dotX = Math.round((windowWidth - size) / 2.0F);
        int dotY = Math.round((windowHeight - size) / 2.0F);
        graphics.blit(RenderPipelines.CROSSHAIR, DOT_CROSSHAIR, dotX, dotY, 0.0F, 0.0F,
                size, size, 16, 16, 16, 16, color);

        graphics.pose().pushMatrix();
        graphics.pose().translate(windowWidth / 2.0F, windowHeight / 2.0F);
        float renderScale = 1.0F + Mth.lerp(partialTicks, this.prevScale, this.scale);
        graphics.pose().scale(renderScale, renderScale);
        graphics.pose().rotate((float) Math.toRadians(Mth.lerp(partialTicks, this.prevRotation, this.rotation)));
        graphics.pose().translate(-size / 2.0F, -size / 2.0F);
        graphics.blit(RenderPipelines.CROSSHAIR, TECH_CROSSHAIR, 0, 0, 0.0F, 0.0F,
                size, size, 16, 16, 16, 16, color);
        graphics.pose().popMatrix();
    }
}
