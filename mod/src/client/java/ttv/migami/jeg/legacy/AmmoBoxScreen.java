package ttv.migami.jeg.legacy;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ttv.migami.jeg.Reference;
public final class AmmoBoxScreen extends AbstractContainerScreen<AmmoBoxMenu> {
    public AmmoBoxScreen(AmmoBoxMenu menu,Inventory inventory,Component title) { super(menu,inventory,title,176,172); inventoryLabelY=80; }
    @Override public void extractContents(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED,Reference.id("textures/gui/container/ammo_box.png"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);
        super.extractContents(graphics,mouseX,mouseY,partialTick);
    }
}
