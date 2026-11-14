package top.ribs.scguns.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

public class MiniButton extends Button
{
    private final int u, v;
    private final Identifier texture;

    public MiniButton(int x, int y, int u, int v, Identifier texture, OnPress onPress)
    {
        super(x, y, 10, 10, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.u = u;
        this.v = v;
        this.texture = texture;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(),
                (float) this.u, (float) this.v, this.width, this.height, 256, 256);
        if (this.isHovered())
        {
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + 10, this.getY() + 10, -2130706433, -2130706433);
        }
    }
}
