package top.ribs.scguns.client.render.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import top.ribs.scguns.client.handler.CrosshairHandler;
import top.ribs.scguns.interfaces.IResourceLocation;

/**
 * Author: MrCrayfish
 */
public abstract class Crosshair implements IResourceLocation
{
    public static final Crosshair DEFAULT = new Crosshair(Identifier.withDefaultNamespace("default")) {};

    static
    {
        CrosshairHandler.get().register(DEFAULT);
    }

    private final Identifier id;

    protected Crosshair(Identifier id)
    {
        this.id = id;
    }

    /**
     * Extracts this crosshair into the 26.2 GUI render state.
     */
    public void render(Minecraft mc, GuiGraphicsExtractor graphics, int windowWidth, int windowHeight, float partialTicks) {}

    public void tick() {}

    public void onGunFired() {}

    @Override
    public final Identifier getLocation()
    {
        return this.id;
    }

    public final boolean isDefault()
    {
        return this == DEFAULT;
    }
}
