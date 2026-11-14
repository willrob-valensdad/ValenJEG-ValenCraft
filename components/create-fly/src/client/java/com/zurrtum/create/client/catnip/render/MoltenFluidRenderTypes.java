package com.zurrtum.create.client.catnip.render;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;

/** Exact curated molten fluids; geometry, sprites, tint and native pipeline are unchanged. */
public final class MoltenFluidRenderTypes {
    private static final Set<Fluid> SELECTED = selected();
    private MoltenFluidRenderTypes() {}
    private static Set<Fluid> selected() {
        Set<Fluid> fluids = Collections.newSetFromMap(new IdentityHashMap<>());
        for (String name : new String[]{"beryllium","calcium","lithium","potassium","sodium","wolframium"})
            BuiltInRegistries.FLUID.getOptional(Identifier.fromNamespaceAndPath("techreborn", name)).ifPresent(fluids::add);
        return fluids;
    }
    public static RenderType select(RenderType base, Fluid fluid, boolean flowing) {
        Fluid source = fluid instanceof FlowingFluid f ? f.getSource() : fluid;
        if (!SELECTED.contains(source) || !FabricLoader.getInstance().isModLoaded("iris")) return base;
        return IrisLayers.select(base, source, flowing);
    }
    public static boolean isFlowLayer(RenderType layer) {
        return FabricLoader.getInstance().isModLoaded("iris") && IrisLayers.isFlow(layer);
    }
    public static RenderType stillLayer(RenderType layer) {
        return IrisLayers.still(layer);
    }
    private static final class IrisLayers {
        private static final RenderType[] BASE = {net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock(),net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(),net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()};
        private static final RenderType[][] LAYERS = layers();
        private static RenderType[][] layers() {
            RenderType[][] result = new RenderType[BASE.length][2];
            for (int i=0; i<BASE.length; i++) for (int flow=0; flow<2; flow++)
                result[i][flow] = net.irisshaders.iris.layer.OuterWrappedRenderType.wrapExactlyOnce(
                    "create:molten_"+i+"_"+flow, BASE[i], new Marker(10124+flow));
            return result;
        }
        private static RenderType select(RenderType base, Fluid fluid, boolean flowing) {
            var ids = net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings.INSTANCE.getBlockStateIds();
            var state = fluid.defaultFluidState().createLegacyBlock();
            if (flowing && state.hasProperty(LiquidBlock.LEVEL)) state = state.setValue(LiquidBlock.LEVEL, 1);
            // Other shader packs keep their existing native draw path.
            if (ids == null || ids.getInt(state) != (flowing ? 10125 : 10124)) return base;
            for (int i=0; i<BASE.length; i++) if (base == BASE[i]) return LAYERS[i][flowing?1:0];
            return base;
        }
        private static boolean isFlow(RenderType layer) {
            for (RenderType[] pair : LAYERS) if (layer == pair[1]) return true;
            return false;
        }
        private static RenderType still(RenderType layer) {
            for (RenderType[] pair : LAYERS) if (layer == pair[1]) return pair[0];
            return layer;
        }
    }
    private static final class Marker implements net.irisshaders.iris.layer.RenderingWrapper {
        private final int marker;
        private int previous;
        Marker(int marker) { this.marker = marker; }
        public void setup() {
            var state = net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE;
            previous = state.getCurrentRenderedBlockEntity();
            state.setCurrentBlockEntity(marker);
            try {
                net.irisshaders.iris.layer.GbufferPrograms.runFallbackEntityListener();
            } catch (RuntimeException | Error failure) {
                state.setCurrentBlockEntity(previous);
                throw failure;
            }
        }
        public void clear() {
            net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE.setCurrentBlockEntity(previous);
            net.irisshaders.iris.layer.GbufferPrograms.runFallbackEntityListener();
        }
    }
}
