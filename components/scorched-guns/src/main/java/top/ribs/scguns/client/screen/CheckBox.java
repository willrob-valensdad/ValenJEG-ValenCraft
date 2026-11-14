package top.ribs.scguns.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class CheckBox extends AbstractWidget {
    private static final Identifier GUI = Identifier.parse("scguns:textures/gui/components.png");

    private boolean toggled = false;

    public CheckBox(int left, int top, Component title) {
        super(left, top, 8, 8, title);
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.getX(), this.getY(), 0.0F, 0.0F, 8, 8, 256, 256);
        if (this.toggled) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.getX(), this.getY() - 1, 8.0F, 0.0F, 9, 8, 256, 256);
        }
        graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + 12, this.getY(), 0xFFFFFFFF);
    }
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.toggled = !this.toggled;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}