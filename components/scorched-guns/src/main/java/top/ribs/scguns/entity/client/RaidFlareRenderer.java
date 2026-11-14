package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsItemRenderState;
import top.ribs.scguns.entity.projectile.RaidFlareEntity;

public class RaidFlareRenderer extends EntityRenderer<RaidFlareEntity, ScgunsItemRenderState> {
    private final ItemModelResolver itemModelResolver;

    public RaidFlareRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ScgunsItemRenderState createRenderState() {
        return new ScgunsItemRenderState();
    }

    @Override
    public void extractRenderState(RaidFlareEntity entity, ScgunsItemRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.stack = getFlareItemStack(entity.getRaidId());
        state.renderItem = entity.tickCount >= 2 && !state.stack.isEmpty();

        if (state.renderItem) {
            this.itemModelResolver.updateForTopItem(
                    state.item,
                    state.stack,
                    ItemDisplayContext.GROUND,
                    entity.level(),
                    entity,
                    entity.getId()
            );
        } else {
            state.item.clear();
        }
    }

    @Override
    public void submit(ScgunsItemRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.renderItem) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(state.yRot - 90.0F)));
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(state.xRot)));
            state.item.submit(
                    poseStack, collector, state.lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor
            );
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static ItemStack getFlareItemStack(String raidId) {
        if (raidId == null || raidId.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String itemId = "piglin".equals(raidId) ? "gold_flare" : raidId + "_flare";
        Item item = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(Reference.MOD_ID, itemId)
        );
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
