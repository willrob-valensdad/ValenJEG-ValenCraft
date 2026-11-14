package top.ribs.scguns.client.render.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.CrosshairHandler;
import top.ribs.scguns.client.handler.HUDRenderHandler;

/**
 * Author: MrCrayfish
 */
public class SpecialHitMarker extends Crosshair {
    private static final Identifier NORMAL_HITMARKER = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/crosshair/special_hit_marker.png");
    private static final Identifier ALT_HITMARKER = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/crosshair/special_hit_marker2.png");

    public SpecialHitMarker() {
        super(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "special_hit_marker"));
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor graphics, int windowWidth, int windowHeight, float partialTicks) {
        int size = 9;
        float hitMarkerProgress = HUDRenderHandler.getHitMarkerProgress(partialTicks);
        boolean crit = HUDRenderHandler.getHitMarkerCrit();
        float scale = 1.5F + hitMarkerProgress;
        float alpha = 1.0F - hitMarkerProgress * 0.3F;

        boolean useAltHitMarker = CrosshairHandler.get().getCurrentCrosshair().getLocation()
                .equals(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "hit_marker"));
        Identifier texture = useAltHitMarker ? ALT_HITMARKER : NORMAL_HITMARKER;
        int textureSize = useAltHitMarker ? 32 : 16;
        int alphaByte = Mth.clamp(Math.round(alpha * 255.0F), 0, 255);
        int tint = ARGB.color(alphaByte, 255, crit ? 64 : 255, crit ? 64 : 255);

        graphics.pose().pushMatrix();
        graphics.pose().translate(Math.round(windowWidth / 2.0F) - 0.5F, Math.round(windowHeight / 2.0F) - 0.5F);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-size / 2.0F, -size / 2.0F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0F, 0.0F,
                size, size, textureSize, textureSize, textureSize, textureSize, tint);
        graphics.pose().popMatrix();
    }
}
