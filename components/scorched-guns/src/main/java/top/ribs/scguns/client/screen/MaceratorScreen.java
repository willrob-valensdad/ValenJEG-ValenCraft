package top.ribs.scguns.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import top.ribs.scguns.Reference;


public class MaceratorScreen extends AbstractContainerScreen<MaceratorMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/macerator_gui.png");

    public MaceratorScreen(MaceratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 6;
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderProgressArrow(guiGraphics, x, y);
        renderFuelBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            int progress = this.menu.getScaledProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 84, y + 25, 176, 14, progress + 1, 16, 256, 256);
        }
    }

    private void renderFuelBar(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.hasFuel()) {
            int fuel = this.menu.getScaledFuelProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 86, y + 58 - fuel, 176, 12 - fuel, 14, fuel + 1, 256, 256);
        }
    }
}
