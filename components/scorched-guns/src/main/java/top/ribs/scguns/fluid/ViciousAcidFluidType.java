package top.ribs.scguns.fluid;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidType;


public class ViciousAcidFluidType extends FluidType {
    private final Identifier stillTexture;
    private final Identifier flowingTexture;

    public ViciousAcidFluidType(Identifier stillTexture, Identifier flowingTexture) {
        super(Properties.create()
                .density(1100)
                .viscosity(1200)
                .temperature(300)
                .canSwim(true)
                .canDrown(true)
                .canPushEntity(true)
                .supportsBoating(false));
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    public Identifier getStillTextureId() {
        return this.stillTexture;
    }

    public Identifier getFlowingTextureId() {
        return this.flowingTexture;
    }
}
