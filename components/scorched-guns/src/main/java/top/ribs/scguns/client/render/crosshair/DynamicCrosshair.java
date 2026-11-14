package top.ribs.scguns.client.render.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.SpreadTracker;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunCompositeStatHelper;

//Thanks to Miga!
public class DynamicCrosshair extends Crosshair {
    private static final Identifier DYNAMIC_CROSSHAIR_H = Identifier.fromNamespaceAndPath("scguns", "textures/crosshair/dynamic_horizontal.png");
    private static final Identifier DYNAMIC_CROSSHAIR_V = Identifier.fromNamespaceAndPath("scguns", "textures/crosshair/dynamic_vertical.png");
    private static final Identifier DOT_CROSSHAIR = Identifier.fromNamespaceAndPath("scguns", "textures/crosshair/dot.png");
    private float scale;
    private float prevScale;
    private float fireBloom;
    private float prevFireBloom;

    public DynamicCrosshair() {
        super(Identifier.fromNamespaceAndPath("scguns", "dynamic"));
    }

    public void tick() {
        this.prevScale = this.scale;
        this.scale *= 0.7F;
        this.prevFireBloom = this.fireBloom;
        if (this.fireBloom > 0.0F) {
            float i = (float) Config.COMMON.projectileSpread.spreadThreshold.get() / 50.0F;
            this.fireBloom -= Math.min(2.0F / Math.max(i, 1.0F), this.fireBloom);
        }
    }

    public void onGunFired() {
        this.prevScale = 0.0F;
        this.scale = 1.2F;
        this.fireBloom = 5.0F;
    }

    private float getCurrentChargeProgress(Player player, ItemStack weapon, Gun modifiedGun) {
        int maxChargeTime = modifiedGun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime(player.getUUID());
        if (maxChargeTime > 0 && chargeTime > 0) {
            return Math.min(1.0F, (float) chargeTime / maxChargeTime);
        }
        return 0.0F;
    }

    private float calculateChargeSpreadMultiplier(float chargeProgress, Gun modifiedGun) {
        chargeProgress = Mth.clamp(chargeProgress, 0.0F, 1.0F);
        float weaponSpreadPenalty = 3.0F;
        float minSpreadMultiplier = 1.0F + weaponSpreadPenalty;
        float maxSpreadMultiplier = 0.15F;
        float curveValue = chargeProgress * chargeProgress * chargeProgress;
        return minSpreadMultiplier - (minSpreadMultiplier - maxSpreadMultiplier) * curveValue;
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor graphics, int windowWidth, int windowHeight, float partialTicks) {
        float alpha = 1.0F;
        float size1 = 7.0F;
        float size2 = 1.0F;
        float spread = 0.0F;
        float scaleMultiplier = 2.0F;
        boolean renderDot = false;
        float finalSpreadTranslate;

        if (mc.player != null) {
            ItemStack heldItem = mc.player.getMainHandItem();
            Item item = heldItem.getItem();
            if (item instanceof GunItem gun) {
                Gun modifiedGun = gun.getModifiedGun(heldItem);
                if (Gun.hasLaserSight(heldItem)) {
                    return;
                }

                finalSpreadTranslate = (float) AimingHandler.get().getNormalisedAdsProgress();
                float sprintTransition = GunRenderingHandler.get().getSprintTransition(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
                float spreadCount = SpreadTracker.get(mc.player).getNextSpread(gun, finalSpreadTranslate);
                float spreadModifier = (spreadCount + 1.0F / Math.max((float) Config.COMMON.projectileSpread.maxCount.get(), 1.0F))
                        * Math.min(Mth.lerp(partialTicks, this.prevFireBloom, this.fireBloom), 1.0F);
                spreadModifier = (float) Mth.lerp((double) sprintTransition * 0.5, spreadModifier, 1.0);

                float baseSpread = GunCompositeStatHelper.getCompositeSpread(heldItem, modifiedGun);
                float minSpread = modifiedGun.getGeneral().isAlwaysSpread() ? baseSpread : 0.0F;
                if (modifiedGun.getGeneral().getFireMode() == FireMode.PULSE) {
                    float chargeProgress = getCurrentChargeProgress(mc.player, heldItem, modifiedGun);
                    float chargeSpreadMultiplier = calculateChargeSpreadMultiplier(chargeProgress, modifiedGun);
                    baseSpread *= chargeSpreadMultiplier;
                    minSpread *= chargeSpreadMultiplier;
                }

                float aimingSpreadMultiplier = Mth.lerp(finalSpreadTranslate, 1.0F, 0.5F);
                spread = Math.max(Mth.lerp(spreadModifier, minSpread, baseSpread) * aimingSpreadMultiplier, 0.0F);

                DotRenderMode dotRenderMode = Config.CLIENT.display.dynamicCrosshairDotMode.get();
                renderDot = dotRenderMode == DotRenderMode.ALWAYS
                        || dotRenderMode == DotRenderMode.AT_MIN_SPREAD
                        && SpreadTracker.get(mc.player).getNextSpread(gun, finalSpreadTranslate) * spreadModifier <= 0.0F
                        && (double) spread <= (Double) Config.CLIENT.display.dynamicCrosshairDotThreshold.get()
                        || dotRenderMode == DotRenderMode.THRESHOLD
                        && (double) spread <= Config.CLIENT.display.dynamicCrosshairDotThreshold.get()
                        && (!(Boolean) Config.CLIENT.display.onlyRenderDotWhileAiming.get() || finalSpreadTranslate > 0.9F);
            }
        }

        float baseScale = 1.0F + Mth.lerp(partialTicks, this.prevScale, this.scale) * scaleMultiplier;
        float adjustedSpread = spread > 1.0F ? (float) (1.0F + Math.log(spread) / 1.5) : spread;
        float fireBloomScale = Mth.lerp(partialTicks, this.prevScale, this.scale) * scaleMultiplier;
        float spreadScale = (float) ((double) adjustedSpread * 2.0 * Config.CLIENT.display.dynamicCrosshairSpreadMultiplier.get());
        float scale = baseScale + spreadScale + fireBloomScale;
        scale = Math.min(scale, (float) (double) Config.CLIENT.display.dynamicCrosshairMaxScale.get() + fireBloomScale);
        float scaleSize = scale / 6.0F + 1.15F;
        float crosshairBaseTightness = (float) (0.8 - Config.CLIENT.display.dynamicCrosshairBaseSpread.get() / 2.0);
        finalSpreadTranslate = (float) (Mth.lerp(0.95, scaleSize - 1.0F, Math.log(scaleSize)) * 2.799999952316284);
        float centerX = Math.round(windowWidth / 2.0F) - 0.5F;
        float centerY = Math.round(windowHeight / 2.0F) - 0.5F;
        int color = ARGB.white(alpha);

        // Left arm: top 1px strip of the 9x9 horizontal texture.
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(scaleSize, 1.0F);
        graphics.pose().translate(-size1 / 2.0F - finalSpreadTranslate + crosshairBaseTightness, -size2 / 2.0F);
        graphics.blit(RenderPipelines.CROSSHAIR, DYNAMIC_CROSSHAIR_H, 0, 0, 0.0F, 0.0F,
                7, 1, 9, 1, 9, 9, color);
        graphics.pose().popMatrix();

        // Right arm: bottom 1px strip.
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(scaleSize, 1.0F);
        graphics.pose().translate(-size1 / 2.0F + finalSpreadTranslate - crosshairBaseTightness, -size2 / 2.0F);
        graphics.blit(RenderPipelines.CROSSHAIR, DYNAMIC_CROSSHAIR_H, 0, 0, 0.0F, 8.0F,
                7, 1, 9, 1, 9, 9, color);
        graphics.pose().popMatrix();

        // Top arm: left 1px strip of the 9x9 vertical texture.
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(1.0F, scaleSize);
        graphics.pose().translate(-size2 / 2.0F, -size1 / 2.0F - finalSpreadTranslate + crosshairBaseTightness);
        graphics.blit(RenderPipelines.CROSSHAIR, DYNAMIC_CROSSHAIR_V, 0, 0, 0.0F, 0.0F,
                1, 7, 1, 9, 9, 9, color);
        graphics.pose().popMatrix();

        // Bottom arm: right 1px strip.
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(1.0F, scaleSize);
        graphics.pose().translate(-size2 / 2.0F, -size1 / 2.0F + finalSpreadTranslate - crosshairBaseTightness);
        graphics.blit(RenderPipelines.CROSSHAIR, DYNAMIC_CROSSHAIR_V, 0, 0, 8.0F, 0.0F,
                1, 7, 1, 9, 9, 9, color);
        graphics.pose().popMatrix();

        if (renderDot) {
            int dotSize = 9;
            graphics.pose().pushMatrix();
            graphics.pose().translate(centerX - dotSize / 2.0F, centerY - dotSize / 2.0F);
            graphics.blit(RenderPipelines.CROSSHAIR, DOT_CROSSHAIR, 0, 0, 0.0F, 0.0F,
                    dotSize, dotSize, 16, 16, 16, 16, color);
            graphics.pose().popMatrix();
        }
    }
}
