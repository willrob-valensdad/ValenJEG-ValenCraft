package org.alex_melan.spacereloaded.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Светящаяся геометрия для анимаций мультиблоков 004 (RenderTypes.lightning — позиция + цвет,
 * аддитивно, без текстуры и освещения): кубы-«ореолы» чуть больше блока и квады окон.
 */
final class GlowGeometry {
    // Keep native additive geometry without identifying every plume as a lightning bolt.
    static final RenderType RENDER_TYPE = createRenderType();

    private static RenderType createRenderType() {
        RenderType nativeType = RenderType.create("spacereloaded:glow",
                RenderSetup.builder(RenderPipelines.LIGHTNING)
                        .setOitPipelines(RenderPipelines.OIT_LIGHTNING).sortOnUpload().createRenderSetup());
        return FabricLoader.getInstance().isModLoaded("iris") ? IrisGlow.wrap(nativeType) : nativeType;
    }

    // Loaded only with Iris. Keep the producer identity through its final buffered draw.
    private static final class IrisGlow implements net.irisshaders.iris.layer.RenderingWrapper {
        private static final net.irisshaders.iris.shaderpack.materialmap.NamespacedId ROCKET =
                new net.irisshaders.iris.shaderpack.materialmap.NamespacedId("spacereloaded", "rocket");
        private int previousEntity;
        private boolean active;

        static RenderType wrap(RenderType nativeType) {
            return net.irisshaders.iris.layer.OuterWrappedRenderType.wrapExactlyOnce(
                    "spacereloaded:glow", nativeType, new IrisGlow());
        }

        @Override
        public void setup() {
            var ids = net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings.INSTANCE.getEntityIds();
            active = ids != null;
            if (active) {
                var state = net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE;
                previousEntity = state.getCurrentRenderedEntity();
                state.setCurrentEntity(ids.applyAsInt(ROCKET));
                net.irisshaders.iris.layer.GbufferPrograms.runFallbackEntityListener();
            }
        }

        @Override
        public void clear() {
            if (active) {
                net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE.setCurrentEntity(previousEntity);
                net.irisshaders.iris.layer.GbufferPrograms.runFallbackEntityListener();
                active = false;
            }
        }
    }


    private GlowGeometry() {
    }

    /** Куб [x0..x1]×[y0..y1]×[z0..z1] цвета ARGB — все 6 граней. */
    static void cube(PoseStack.Pose pose, VertexConsumer vc, float x0, float y0, float z0,
                     float x1, float y1, float z1, int argb) {
        // низ, верх
        quad(pose, vc, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, argb);
        quad(pose, vc, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, argb);
        // север, юг
        quad(pose, vc, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, argb);
        quad(pose, vc, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1, argb);
        // запад, восток
        quad(pose, vc, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, argb);
        quad(pose, vc, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, argb);
    }

    static void quad(PoseStack.Pose pose, VertexConsumer vc,
                     float ax, float ay, float az, float bx, float by, float bz,
                     float cx, float cy, float cz, float dx, float dy, float dz, int argb) {
        vc.addVertex(pose, ax, ay, az).setColor(argb);
        vc.addVertex(pose, bx, by, bz).setColor(argb);
        vc.addVertex(pose, cx, cy, cz).setColor(argb);
        vc.addVertex(pose, dx, dy, dz).setColor(argb);
    }

    /** ARGB с альфой 0..1. */
    static int argb(float alpha, int rgb) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
