package top.ribs.scguns.client.render.armor;

import com.geckolib.animatable.GeoItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.ribs.scguns.client.compat.FreshAnimationsArmorCompat;

import java.util.UUID;

/**
 * Shared renderer base for Scorched Guns GeckoLib armor.
 *
 * <p>When EMF is absent this follows GeckoLib's normal armor path unchanged.
 * When EMF/Fresh Animations is active, GeckoLib first builds its normal armor
 * pose and this renderer then replaces only the final armor bone transforms
 * with the pose captured from EMF.</p>
 */
public class FreshAnimationsGeoArmorRenderer<T extends Item & GeoItem>
        extends GeoArmorRenderer<T, HumanoidRenderState> {
    private static final DataTicket<UUID> WEARER_UUID =
            DataTickets.create("scguns:fresh_animations_armor_wearer", UUID.class);

    public FreshAnimationsGeoArmorRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    public void captureDefaultRenderState(T animatable,
                                          GeoArmorRenderer.RenderData renderData,
                                          HumanoidRenderState renderState,
                                          float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);

        if (renderData.entity() instanceof Player player) {
            ((GeoRenderState) (Object) renderState).addGeckolibData(WEARER_UUID, player.getUUID());
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void adjustRenderPose(RenderPassInfo renderInfo) {
        // GeckoLib 5 exposes raw RenderPassInfo here in this 26.2 port because
        // HumanoidRenderState gains GeoRenderState through interface injection.
        HumanoidRenderState renderState = humanoidState(renderInfo);

        // IMPORTANT: the EMF root is a vanilla ModelPart transform. Apply it in
        // vanilla/entity model space BEFORE GeoArmorRenderer changes basis with
        // translate(0, 1.5, 0) + scale(-1, -1, 1). Applying the raw ModelPart
        // transform after that basis change mirrors root translations and changes
        // the effective rotation axes, which is why strafing/turning/flying could
        // pull the player out of the armor even though limb animation matched.
        UUID uuid = wearerUuid(renderState);
        if (uuid != null) {
            FreshAnimationsArmorCompat.applyRoot(uuid, renderInfo.poseStack());
        }

        super.adjustRenderPose(renderInfo);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void adjustModelBonesForRender(RenderPassInfo renderInfo,
                                          BoneSnapshots snapshots) {
        // Let GeckoLib do its normal vanilla-derived setup first, including slot
        // selection and all of its standard armor-bone mapping.
        super.adjustModelBonesForRender(renderInfo, snapshots);

        // Then replace those final transforms with the already-finished EMF pose.
        // This is the important ordering: GeckoLib's baseModel.setupAnim(...) can
        // no longer overwrite Fresh Animations after this point.
        HumanoidRenderState renderState = humanoidState(renderInfo);
        UUID uuid = wearerUuid(renderState);
        if (uuid != null) {
            FreshAnimationsArmorCompat.applyArmorPose(uuid, renderState, snapshots, this);
        }
    }

    @SuppressWarnings("rawtypes")
    private static HumanoidRenderState humanoidState(RenderPassInfo renderInfo) {
        return (HumanoidRenderState) (Object) renderInfo.renderState();
    }

    private static UUID wearerUuid(HumanoidRenderState renderState) {
        return ((GeoRenderState) (Object) renderState)
                .getOrDefaultGeckolibData(WEARER_UUID, (UUID) null);
    }
}
