package top.ribs.scguns.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import top.ribs.scguns.Reference;

public class ShellCatcherModuleScreen extends AbstractContainerScreen<ShellCatcherModuleMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/shell_catcher_module.png");

    public ShellCatcherModuleScreen(ShellCatcherModuleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 168);
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
