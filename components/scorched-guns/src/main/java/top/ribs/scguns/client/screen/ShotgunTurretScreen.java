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
import top.ribs.scguns.blockentity.AutoTurretBlockEntity;
import top.ribs.scguns.blockentity.ShotgunTurretBlockEntity;

import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ShotgunTurretScreen extends AbstractContainerScreen<ShotgunTurretMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("scguns", "textures/gui/basic_turret.png");
    public ShotgunTurretScreen(ShotgunTurretMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 168);
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }


    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        ShotgunTurretBlockEntity turret = menu.getBlockEntity();
        graphics.text(this.font, "Owner: " + turret.getOwnerName(), 90, 6, 0x404040, false);
    }
}
