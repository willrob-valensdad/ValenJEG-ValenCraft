package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class BakedItemModelBufferer {
    private static final List<RenderType> LAYERS = List.of(
        Sheets.cutoutBlockItemSheet(), Sheets.cutoutItemSheet(),
        Sheets.translucentBlockItemSheet(), Sheets.translucentItemSheet());
    private static final Map<RenderType, Integer> CHUNK_LAYERS = new IdentityHashMap<>();
    static {
        for (int i = 0; i < LAYERS.size(); i++) CHUNK_LAYERS.put(LAYERS.get(i), i);
    }

    private static List<SubmitNode> collect(ItemStackRenderState state) {
        SubmitNodeStorage storage = new SubmitNodeStorage();
        state.submit(new PoseStack(), storage, 0, OverlayTexture.NO_OVERLAY, 0);
        List<SubmitNode> submits = new ArrayList<>();
        storage.drainPhases(phase -> phase.sortInto((submit, ordered) -> submits.add(submit)));
        return submits;
    }

    private static boolean supports(List<SubmitNode> submits) {
        if (submits.isEmpty()) return false;
        for (SubmitNode node : submits) {
            if (!(node instanceof ItemFeatureRenderer.Submit item) || item.foilType() != ItemStackRenderState.FoilType.NONE)
                return false;
            for (BakedQuad quad : item.quads())
                if (!CHUNK_LAYERS.containsKey(quad.materialInfo().itemRenderType())) return false;
        }
        return true;
    }

    public static boolean supports(ItemStackRenderState state) {
        return !state.isAnimated() && supports(collect(state));
    }

    public static void bufferItemStack(ItemStack stack, BlockAndTintGetter level,
                                      ItemDisplayContext displayContext, ResultConsumer resultConsumer,
                                      MeshResultConsumer meshResultConsumer) {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
            state, stack, displayContext, level instanceof ClientLevel client ? client : null, null, 0);
        List<SubmitNode> submits = collect(state);
        if (!supports(submits)) throw new IllegalArgumentException("Item requires native rendering: " + stack);
        QuadInstance instance = new QuadInstance();
        for (RenderType layer : LAYERS) {
            try (ByteBufferBuilder bytes = new ByteBufferBuilder(1024)) {
                BufferBuilder buffer = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.ENTITY);
                for (SubmitNode node : submits) {
                    ItemFeatureRenderer.Submit item = (ItemFeatureRenderer.Submit) node;
                    instance.setLightCoords(item.lightCoords());
                    instance.setOverlayCoords(item.overlayCoords());
                    for (BakedQuad quad : item.quads()) {
                        if (quad.materialInfo().itemRenderType() != layer) continue;
                        int tint = quad.materialInfo().tintIndex();
                        instance.setColor(tint >= 0 && tint < item.tintLayers().length ? item.tintLayers()[tint] : -1);
                        buffer.putBakedQuad(item.pose(), quad, instance);
                    }
                }
                try (MeshData mesh = buffer.build()) {
                    if (mesh != null) resultConsumer.accept(layer, true, mesh);
                }
            }
        }
    }

    public static Map<RenderType, Integer> getChunkLayers() {
        return CHUNK_LAYERS;
    }

    public interface ResultConsumer {
        void accept(RenderType renderType, boolean shaded, MeshData data);
    }

    public interface MeshResultConsumer {
        void accept(RenderType renderType, Material material, Mesh mesh, boolean translucent);
    }
}
