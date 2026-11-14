package top.ribs.scguns.client.handler;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.item.GunItem;

/**
 * Modern 26.2 GUI extraction bridge for overlays that were previously drawn
 * directly from client/render ticks or from GameRenderer mixins.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class ClientOverlayRenderHandler {
    private static final Identifier COOLDOWN_BACKGROUND_SPRITE =
            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    private static final Identifier COOLDOWN_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private static final float COOLDOWN_SCALE = 3.0F;

    private ClientOverlayRenderHandler() {
    }

    /**
     * Draw the blindness wash before vanilla HUD layers so the HUD remains
     * readable, matching the old GameRenderer injection ordering.
     */
    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        MobEffectInstance effect = player.getEffect(ModEffects.BLINDED.asHolder());
        if (effect == null) {
            return;
        }

        float threshold = Math.max(1.0F, Config.SERVER.alphaFadeThreshold.get().floatValue());
        float percent = Math.min(effect.getDuration() / threshold, 1.0F);
        int alpha = Mth.clamp((int) (percent * Config.SERVER.alphaOverlay.get() + 0.5F), 0, 255);

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0x00FFFFFF);
    }

    /**
     * Submit the semi-auto cooldown indicator through the 26.2 GUI render-state
     * extractor instead of constructing GuiGraphicsExtractor manually.
     */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isWindowActive() || minecraft.player == null || minecraft.gui.screen() != null) {
            return;
        }
        if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }
        if (!Config.CLIENT.display.cooldownIndicator.get()) {
            return;
        }

        Player player = minecraft.player;
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Gun gun = gunItem.getGun();
        if (gun.getGeneral().isAuto()) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float cooldown = player.getCooldowns().getCooldownPercent(heldItem, partialTick);
        if (cooldown <= 0.0F) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int y = (int) ((graphics.guiHeight() / 2.0F - 7.0F - 60.0F) / COOLDOWN_SCALE);
        int x = (int) Math.ceil((graphics.guiWidth() / 2.0F - 8.0F * COOLDOWN_SCALE) / COOLDOWN_SCALE);
        int progress = Mth.clamp((int) Math.ceil((cooldown + 0.05F) * 17.0F) - 1, 0, 16);

        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(COOLDOWN_SCALE);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, COOLDOWN_BACKGROUND_SPRITE, x, y, 16, 4);
            if (progress > 0) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        COOLDOWN_PROGRESS_SPRITE,
                        16, 4,
                        0, 0,
                        x, y,
                        progress, 4
                );
            }
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
