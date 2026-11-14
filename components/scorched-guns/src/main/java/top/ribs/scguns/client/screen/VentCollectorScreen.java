package top.ribs.scguns.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class VentCollectorScreen extends AbstractContainerScreen<VentCollectorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("scguns", "textures/gui/vent_collector.png");

    public VentCollectorScreen(VentCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        int filterCharge = menu.getFilterCharge();
        int maxFilterCharge = Math.max(1, menu.getMaxFilterCharge());
        int barWidth = Math.min(52, (int)(((float)filterCharge / maxFilterCharge) * 52));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 62, y + 37, 176, 0, barWidth, 8, 256, 256);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        super.extractTooltip(guiGraphics, x, y);

        int startX = (this.width - this.imageWidth) / 2 + 62;
        int startY = (this.height - this.imageHeight) / 2 + 37;
        if (x >= startX && x < startX + 52 && y >= startY && y < startY + 8) {
            int filterCharge = menu.getFilterCharge();
            int maxFilterCharge = Math.max(1, menu.getMaxFilterCharge());
            guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("gui.scguns.vent_collector.filter_charge", filterCharge, maxFilterCharge), x, y);
        }
    }
}
