package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.mojang.blaze3d.platform.InputConstants;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.screen.widget.MiniButton;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.common.container.AttachmentContainer;
import top.ribs.scguns.common.container.slot.AttachmentSlot;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.util.GunCompositeStatHelper;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer>
{
    private static final Identifier GUI_TEXTURES = Identifier.parse("scguns:textures/gui/attachments.png");
    private static final Component CONFIG_TOOLTIP = Component.translatable("scguns.button.config.tooltip");

    private final Inventory playerInventory;
    private final Container weaponInventory;

    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public AttachmentScreen(AttachmentContainer screenContainer, Inventory playerInventory, Component titleIn)
    {
        super(screenContainer, playerInventory, titleIn, 188, 192);
        this.playerInventory = playerInventory;
        this.weaponInventory = screenContainer.getWeaponInventory();
    }

    @Override
    protected void init()
    {
        super.init();

        this.gatherButtons().forEach(this::addRenderableWidget);
    }

    private List<MiniButton> gatherButtons()
    {
        List<MiniButton> buttons = new ArrayList<>();
        return buttons;
    }

    @Override
    public void containerTick()
    {
        super.containerTick();
        if(this.minecraft != null && this.minecraft.player != null)
        {
            if(!(this.minecraft.player.getMainHandItem().getItem() instanceof GunItem))
            {
                Minecraft.getInstance().setScreenAndShow(null);
            }
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        this.submitGunPreview(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractTooltip(graphics, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;
        int numSlots = IAttachment.Type.values().length;
        int centerX = 88 - (numSlots * 18) / 2 + 18 - 5;

        for(int i = 0; i < numSlots; i++)
        {
            int x = centerX + i * 18;
            int y = 89;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, startX + x, startY + y, 18, 18))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(!this.menu.getSlot(i).isActive())
                {
                    graphics.setComponentTooltipForNextFrame(this.font, List.of(
                            Component.translatable("slot.scguns.attachment." + type.getTranslationKey()),
                            Component.translatable("slot.scguns.attachment.not_applicable")
                    ), mouseX, mouseY);
                }
                else if(this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty() && !this.isCompatible(this.menu.getCarried(), slot))
                {
                    graphics.setComponentTooltipForNextFrame(this.font, List.of(
                            Component.translatable("slot.scguns.attachment.incompatible").withStyle(ChatFormatting.YELLOW)
                    ), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        this.renderGunStats(graphics);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - this.imageWidth) / 2 - 6;
        int top = (this.height - this.imageHeight) / 2 + 19;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int numSlots = IAttachment.Type.values().length;
        int centerX = 88 - (numSlots * 18) / 2 + 18 - 5;
        for (int i = 0; i < numSlots; i++)
        {
            int x = centerX + i * 18;
            int y = 89;
            if (!this.menu.getSlot(i).isActive())
            {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, left + x, top + y, 192, 0, 16, 16, 256, 256);
            }
            else if (this.weaponInventory.getItem(i).isEmpty())
            {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, left + x, top + y, 192, 16 + i * 16, 16, 16, 256, 256);
            }
        }
    }

    private void submitGunPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        if(this.minecraft == null || this.minecraft.player == null)
            return;

        ItemStack stack = this.minecraft.player.getMainHandItem();
        if(!(stack.getItem() instanceof GunItem))
            return;

        int left = (this.width - this.imageWidth) / 2 - 6;
        int top = (this.height - this.imageHeight) / 2 + 19;
        int x0 = left - 6;
        int y0 = top - 65;
        int x1 = left + 210;
        int y1 = top + 88;

        float panX = this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0);
        float panY = this.windowY + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0);
        float rotationX = this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0);
        float rotationY = this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0);

        graphics.submitPictureInPictureRenderState(new GunPreviewRenderState(
                x0, x1, y0, y1,
                this.minecraft.player, stack,
                panX, panY, rotationX, rotationY, this.windowZoom,
                graphics.peekScissorStack()
        ));
    }

    public record GunPreviewRenderState(
            int x0, int x1, int y0, int y1,
            LivingEntity entity, ItemStack stack,
            float panX, float panY, float rotationX, float rotationY, float zoom,
            ScreenRectangle scissorArea, ScreenRectangle bounds
    ) implements PictureInPictureRenderState
    {
        private GunPreviewRenderState(
                int x0, int x1, int y0, int y1,
                LivingEntity entity, ItemStack stack,
                float panX, float panY, float rotationX, float rotationY, float zoom,
                ScreenRectangle scissorArea)
        {
            this(x0, x1, y0, y1, entity, stack, panX, panY, rotationX, rotationY, zoom,
                    scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
        }

        @Override
        public float scale()
        {
            return 1.0F;
        }
    }

    public static final class GunPreviewRenderer extends PictureInPictureRenderer<GunPreviewRenderState>
    {
        @Override
        public Class<GunPreviewRenderState> getRenderStateClass()
        {
            return GunPreviewRenderState.class;
        }

        @Override
        protected String getTextureLabel()
        {
            return "scguns_attachment_gun_preview";
        }

        @Override
        protected float getTranslateY(int height, int guiScale)
        {
            return height / 2.0F;
        }

        @Override
        protected void renderToTexture(GunPreviewRenderState state, PoseStack poseStack, SubmitNodeCollector collector)
        {
            poseStack.pushPose();
            try
            {
                // The PiP base renderer centers X. The original preview origin was
                // six pixels left and 36.5 pixels below the preview rectangle center.
                poseStack.translate(-6.0F + state.panX(), 36.5F + state.panY(), 0.0F);
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(-30F)));
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(state.rotationY())));
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(state.rotationX())));
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(150F)));

                float zoom = state.zoom() / 10.0F;
                poseStack.scale(zoom, zoom, zoom);
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(90F)));
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(90.0F, 90.0F, 90.0F);

                GunRenderingHandler.get().renderWeapon(
                        state.entity(),
                        state.stack(),
                        ItemDisplayContext.GROUND,
                        poseStack,
                        collector,
                        LightCoordsUtil.FULL_BRIGHT,
                        0F
                );
            }
            finally
            {
                poseStack.popPose();
            }
        }
    }

    private void renderGunStats(GuiGraphicsExtractor graphics) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack gunStack = this.minecraft.player.getMainHandItem();
        if (!(gunStack.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Gun modifiedGun = gunItem.getModifiedGun(gunStack);
        Gun.Projectile projectile = Gun.getDisplayProjectile(gunStack, modifiedGun);
        Gun.General general = modifiedGun.getGeneral();
        Gun.Reloads reloads = modifiedGun.getReloads();

        int startX = -60;
        int startY = 0;
        int lineHeight = 7;
        float scale = 0.6F;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale);

        int scaledX = (int) (startX / scale);
        int currentY = (int) (startY / scale);

        String gunName = gunStack.getItemName().getString();
        graphics.text(this.font, gunName, scaledX, currentY, 0xFFFFFFFF, false);
        graphics.fill(scaledX, currentY + 8, scaledX + this.font.width(gunName), currentY + 9, 0xFFFFFFFF);
        currentY += (int) ((lineHeight + 3) / scale);

        Gun.WeaponType weaponType = general.getWeaponType();
        if (weaponType != null) {
            drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.weapon_type").getString(), Component.translatable("desc.scguns." + weaponType.id()).getString());
            currentY += (int) (lineHeight / scale);
        }
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.damage").getString(), formatOne(getDisplayDamage(gunStack, projectile)));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.critical_chance").getString(), formatOne(GunModifierHelper.getCriticalChance(gunStack) * 100.0F) + "%");
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.critical_multiplier").getString(), formatOne(projectile.getCritDamageMultiplier()) + "x");
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.armor_penetration").getString(), formatOne(GunEnchantmentHelper.getPuncturingArmorBypass(gunStack)));
        currentY += (int) (lineHeight / scale);

        FireMode fireMode = general.getFireMode();
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.fire_mode").getString(), Component.translatable("fire_mode." + fireMode.id()).getString());
        currentY += (int) (lineHeight / scale);

        int rate = Math.max(GunCompositeStatHelper.getCompositeRate(gunStack, modifiedGun), 1);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.fire_rate").getString(), String.format(Locale.ROOT, "%.0f RPM", (20.0F / rate) * 60.0F));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.max_ammo").getString(), Integer.toString(GunModifierHelper.getModifiedAmmoCapacity(gunStack, modifiedGun)));
        currentY += (int) (lineHeight / scale);

        ReloadType reloadType = reloads.getReloadType();
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.reload_type").getString(), Component.translatable("reload_type." + reloadType.id().toString().replace(":", ".")).getString());
        currentY += (int) (lineHeight / scale);

        GripType gripType = modifiedGun.determineGripType(gunStack);
        String gripKey = gripType == GripType.ONE_HANDED ? "info.scguns.grip_one_handed" : "info.scguns.grip_two_handed";
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.grip_type").getString(), Component.translatable(gripKey).getString());
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.spread").getString(), formatTwo(GunModifierHelper.getModifiedSpread(gunStack, general.getSpread())));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.recoil_angle").getString(), formatOne(general.getRecoilAngle()));
        currentY += (int) (lineHeight / scale);
        String falloffRange = projectile.hasDamageFalloff()
                ? formatOne(projectile.getDamageFalloffStart()) + " - " + formatOne(projectile.getDamageFalloffEnd())
                : Component.translatable("info.scguns.falloff_none").getString();
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.falloff_range").getString(), falloffRange);

        graphics.pose().popMatrix();
    }

    private static void drawStatLine(GuiGraphicsExtractor graphics, int x, int y, String label, String value) {
        graphics.text(Minecraft.getInstance().font, label + ": " + value, x, y, 0xFFFFFFFF, false);
    }

    private static float getDisplayDamage(ItemStack gunStack, Gun.Projectile projectile) {
        float damage = projectile.getDamage();
        damage = GunModifierHelper.getModifiedProjectileDamage(gunStack, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(gunStack, damage);
        damage = GunEnchantmentHelper.getHeavyShotDamage(gunStack, damage);
        damage = GunEnchantmentHelper.getPuncturingDamageReductionForTooltip(gunStack, damage);
        damage *= Config.COMMON.gameplay.globalDamageMultiplier.get().floatValue();

        CompoundTag tag = getStackTag(gunStack);
        if (tag != null && tag.contains("AdditionalDamage")) {
            damage += tag.getFloatOr("AdditionalDamage", 0.0F);
        }
        damage += GunModifierHelper.getAdditionalDamage(gunStack, false);
        return damage;
    }

    private static CompoundTag getStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : null;
    }

    private static String formatOne(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String formatTwo(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private boolean isCompatible(ItemStack stack, AttachmentSlot slot)
    {
        if(stack.isEmpty())
            return true;


        if(!(stack.getItem() instanceof IAttachment<?> attachment))
            return false;

        if(!attachment.getType().equals(slot.getType()))
            return true;

        if(this.minecraft == null || this.minecraft.player == null)
            return false;

        if(!attachment.canAttachTo(this.minecraft.player.getMainHandItem()))
            return false;

        return slot.isActive();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX - 6, startY - 65, 216, 153))
        {
            if(scrollY < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scrollY > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;

        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX - 6, startY - 65, 216, 153))
        {
            if(!this.mouseGrabbed && (button == InputConstants.MOUSE_BUTTON_LEFT || button == InputConstants.MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == InputConstants.MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event)
    {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == InputConstants.MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX += (int) (mouseX - this.mouseClickedX - 1);
                this.windowY += (int) (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == InputConstants.MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (float) (mouseX - this.mouseClickedX);
                this.windowRotationY -= (float) (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(event);
    }

    private void openConfigScreen()
    {
        ModList.get().getModContainerById(Reference.MOD_ID).ifPresent(container ->
        {
            Screen screen = container.getCustomExtension(IConfigScreenFactory.class).map(factory -> factory.createScreen(container, this)).orElse(null);
            if(screen != null)
            {
                this.minecraft.gui.setScreen(screen);
            }
            else if(this.minecraft != null && this.minecraft.player != null)
            {
                MutableComponent modName = Component.literal("Configured");
                modName.setStyle(modName.getStyle()
                        .withColor(ChatFormatting.YELLOW)
                        .withUnderlined(true)
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("scguns.chat.open_curseforge_page")))
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/configured"))));
                Component message = Component.translatable("scguns.chat.install_configured", modName);
                this.minecraft.player.sendSystemMessage(message);
            }
        });
    }
}
