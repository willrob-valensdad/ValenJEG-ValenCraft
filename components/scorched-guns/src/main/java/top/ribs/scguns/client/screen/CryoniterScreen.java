package top.ribs.scguns.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.blockentity.BasicTurretBlockEntity;

import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class CryoniterScreen extends AbstractContainerScreen<CryoniterMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("scguns", "textures/gui/cryoniter_gui.png");

    public CryoniterScreen(CryoniterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
