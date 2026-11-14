package top.ribs.scguns.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import top.ribs.scguns.Reference;


public class PoweredMaceratorScreen extends AbstractContainerScreen<PoweredMaceratorMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/powered_macerator_gui.png");
    private static final int BAR_WIDTH = 7;
    private static final int BAR_HEIGHT = 42;
    private static final int BAR_X = 14; // Adjust as needed
    private static final int BAR_Y = 21; // Adjust as needed
    private static final int TEXTURE_BAR_X = 176;
    private static final int TEXTURE_BAR_Y = 32; // Adjust as needed

    public PoweredMaceratorScreen(PoweredMaceratorMenu menu, Inventory inv, Component title) {
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
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            int progress = this.menu.getScaledProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 84, y + 25, 176, 14, progress + 1, 16, 256, 256);
        }
    }

    private void renderEnergyBar(GuiGraphicsExtractor guiGraphics, int x, int y) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        int energyHeight = (int) (energy * BAR_HEIGHT / (float) maxEnergy);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + BAR_X, y + BAR_Y + (BAR_HEIGHT - energyHeight), TEXTURE_BAR_X, TEXTURE_BAR_Y + (BAR_HEIGHT - energyHeight), BAR_WIDTH, energyHeight, 256, 256);
    }

    private boolean isMouseOverEnergyBar(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x + BAR_X && mouseX <= x + BAR_X + BAR_WIDTH && mouseY >= y + BAR_Y && mouseY <= y + BAR_Y + BAR_HEIGHT;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (isMouseOverEnergyBar(mouseX, mouseY, x, y)) {
            guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("tooltip.powered_macerator.energy", this.menu.getEnergy()), mouseX, mouseY);
        }
    }
}
