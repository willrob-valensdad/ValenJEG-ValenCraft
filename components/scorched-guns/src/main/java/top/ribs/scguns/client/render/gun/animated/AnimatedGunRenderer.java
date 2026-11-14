package top.ribs.scguns.client.render.gun.animated;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.GunRenderType;
import top.ribs.scguns.client.compat.OptionalClientCompat;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.client.handler.DualWieldShotTracker;
import top.ribs.scguns.client.handler.GunRecoilHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.ShootingHandler;
import top.ribs.scguns.client.render.gun.GunRenderContext;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.render.gun.ModelOverrides;
import top.ribs.scguns.client.render.gun.ScgunsLegacyItemTransforms;
import top.ribs.scguns.client.screen.AttachmentScreen;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.item.attachment.IBarrel;
import top.ribs.scguns.item.attachment.impl.Scope;
import top.ribs.scguns.util.GunModifierHelper;
import top.ribs.scguns.util.ScgunsNbtCompat;

/**
 * GeckoLib 5.5 / Minecraft 26.2 animated gun renderer.
 *
 * Rendering is state/submission based: weapon state is captured into GeoRenderState,
 * dynamic bone visibility is applied through BoneSnapshot, and secondary geometry
 * (arms, attachments, glow bones and muzzle flash) is submitted through
 * SubmitNodeCollector. No immediate buffer-source mutation remains here.
 */
public class AnimatedGunRenderer extends GeoItemRenderer<AnimatedGunItem> {
    private static final DataTicket<ItemStack> RENDER_STACK =
            DataTickets.create("scguns:animated_gun_render_stack", ItemStack.class);
    private static final DataTicket<CompoundTag> RENDER_STACK_TAG =
            DataTickets.create("scguns:animated_gun_render_tag", CompoundTag.class);
    private static final CompoundTag EMPTY_RENDER_TAG = new CompoundTag();
    private static final long PARTICLE_COOLDOWN_MS = 100L;
    private static final Set<String> GLOW_BONES = Set.of("glow", "glow_2", "glow_3");
    private static final Set<String> ARM_BONES = Set.of("left_arm", "right_arm", "fake_left_arm", "fake_right_arm");
    private static final int MAX_STABLE_ARM_POSES = 64;

    /**
     * GeckoLib 5 can temporarily clear a bone's frameSnapshot while a controller
     * is being reset/re-initialized. The gun model survives that one-frame gap,
     * but Scorched Guns' real player arms use these bones as anchors, so rendering
     * the raw bind pose produces the giant vertical-arm flash seen during ADS,
     * hotbar changes, equip and unequip transitions. Keep the last fully animated
     * first-person pose per stack/bone and reuse it only for those snapshot gaps.
     */
    private final Map<String, PoseStack.Pose> lastStableFirstPersonArmPoses =
            new LinkedHashMap<>(16, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PoseStack.Pose> eldest) {
                    return size() > MAX_STABLE_ARM_POSES;
                }
            };

    private final AttachmentRenderer attachmentRenderer = new AttachmentRenderer(this);

    // HF90: restore the original 0.5.5 bone-visibility cache that was lost while
    // moving the renderer from GeckoLib 4 recursion to GeckoLib 5 BoneSnapshots.
    private final Map<String, Boolean> boneVisibilityCache = new HashMap<>();
    private ItemStack lastBoneVisibilityStack = ItemStack.EMPTY;

    // 0.5.5 kept this set as a static renderer constant. Keep it lazy in 26.2 so
    // DeferredHolder values are not resolved before registries are ready.
    private static Set<Item> modeledInGunAttachments;

    private float sprintIntensity;
    private float fallSway;
    private float prevFallSway;
    private long lastParticleSpawnTime;

    public AnimatedGunRenderer(Identifier path) {
        super(new AnimatedGunModel(path));
    }

    private static CompoundTag getOrCreateStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : new CompoundTag();
    }

    private static ItemStack parseClientStack(CompoundTag tag) {
        ClientLevel level = Minecraft.getInstance().level;
        return ScgunsNbtCompat.readItemStack(
                level != null ? level.registryAccess() : Gun.builtInRegistryProvider(),
                tag
        );
    }

    private static CompoundTag renderStackTag(RenderPassInfo<GeoRenderState> renderInfo) {
        return renderInfo.getOrDefaultGeckolibData(RENDER_STACK_TAG, EMPTY_RENDER_TAG);
    }

    private static Set<Item> modeledInGunAttachments() {
        Set<Item> cached = modeledInGunAttachments;
        if (cached == null) {
            cached = Set.of(
                    ModItems.SILENCER.get(), ModItems.ADVANCED_SILENCER.get(), ModItems.MUZZLE_BRAKE.get(),
                    ModItems.VERTICAL_GRIP.get(), ModItems.LIGHT_GRIP.get(), ModItems.IRON_BAYONET.get(),
                    ModItems.ANTHRALITE_BAYONET.get(), ModItems.DIAMOND_BAYONET.get(), ModItems.NETHERITE_BAYONET.get(),
                    ModItems.EXTENDED_BARREL.get(), ModItems.WOODEN_STOCK.get(), ModItems.LIGHT_STOCK.get(),
                    ModItems.WEIGHTED_STOCK.get(), ModItems.BUMP_STOCK.get(), ModItems.EXTENDED_MAG.get(),
                    ModItems.SPEED_MAG.get(), ModItems.PLUS_P_MAG.get()
            );
            modeledInGunAttachments = cached;
        }
        return cached;
    }

    private static ItemDisplayContext effectiveDisplayContext(ItemStack stack, ItemDisplayContext display, Minecraft client) {
        if (stack.getItem() instanceof AnimatedGunItem
                && display == ItemDisplayContext.NONE
                && client.options.getCameraType() == CameraType.FIRST_PERSON) {
            return client.options.mainHand().get() == HumanoidArm.LEFT
                    ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                    : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        }
        return display;
    }

    private static boolean isFirstPersonHand(ItemDisplayContext display) {
        return display == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || display == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    private static boolean isRightHandDisplay(ItemDisplayContext display) {
        return display == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || display == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private static ItemStack stack(RenderPassInfo<GeoRenderState> renderInfo) {
        return renderInfo.getOrDefaultGeckolibData(RENDER_STACK, ItemStack.EMPTY);
    }

    private static ItemDisplayContext display(RenderPassInfo<GeoRenderState> renderInfo, ItemStack stack) {
        ItemDisplayContext display = renderInfo.getOrDefaultGeckolibData(
                DataTickets.ITEM_RENDER_PERSPECTIVE,
                ItemDisplayContext.NONE
        );
        return effectiveDisplayContext(stack, display, Minecraft.getInstance());
    }

    @Override
    public void captureDefaultRenderState(AnimatedGunItem animatable,
                                          GeoItemRenderer.RenderData renderData,
                                          GeoRenderState renderState,
                                          float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);
        ItemStack renderStack = renderData.itemStack();
        renderState.addGeckolibData(RENDER_STACK, renderStack);
        // HF90: one read-only snapshot for this submitted render. Before this fix,
        // sliding-magazine and attachment callbacks each copied CustomData again.
        renderState.addGeckolibData(RENDER_STACK_TAG, getOrCreateStackTag(renderStack));
    }

    @Override
    public void preRenderPass(RenderPassInfo<GeoRenderState> renderInfo, SubmitNodeCollector collector) {
        super.preRenderPass(renderInfo, collector);
        ItemStack stack = stack(renderInfo);
        if (stack.isEmpty() || !(stack.getItem() instanceof AnimatedGunItem)) {
            return;
        }

        ItemDisplayContext display = display(renderInfo, stack);
        float partialTick = renderInfo.renderState().getPartialTick();
        updatePackedLight(renderInfo.renderState(), stack, display, partialTick);
        registerPerBoneTasks(renderInfo);

        // The outer first-person pipeline (GunRenderingHandler) owns camera-space
        // transforms for animated guns as well. It already applies ADS, view bobbing,
        // sway, sprint and recoil before submitting this GeckoLib renderer with
        // ItemDisplayContext.NONE. Re-applying those transforms here caused the
        // weapon to drift off-screen while aiming and doubled movement bobbing.
    }

    private void updateMotionState() {
        this.prevFallSway = this.fallSway;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float deltaY = (float) Mth.clamp(mc.player.yo - mc.player.getY(), -1.0, 1.0);
        deltaY = (float) (deltaY * (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
        deltaY = (float) (deltaY * (1.0 - Mth.abs(mc.player.getXRot()) / 90.0F));
        this.fallSway = Mth.approach(
                this.fallSway,
                deltaY * 60.0F * Config.CLIENT.display.swaySensitivity.get().floatValue(),
                10.0F
        );
        float targetSprintIntensity = mc.player.isSprinting() ? 0.75F : 1.0F;
        this.sprintIntensity = Mth.approach(this.sprintIntensity, targetSprintIntensity, 0.1F);
    }

    private void updatePackedLight(GeoRenderState renderState,
                                   ItemStack stack,
                                   ItemDisplayContext display,
                                   float partialTick) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        int packedLight;
        if (player == null || display == ItemDisplayContext.GUI) {
            packedLight = LightCoordsUtil.pack(12, 12);
        } else {
            BlockPos eyePos = BlockPos.containing(player.getEyePosition(partialTick));
            int blockLight = player.isOnFire() ? 15 : player.level().getBrightness(LightLayer.BLOCK, eyePos);
            if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(stack)
                    && GunRenderingHandler.entityIdForMuzzleFlash.contains(player.getId())) {
                blockLight += 3;
            }
            blockLight = Math.min(blockLight, 15);
            packedLight = LightCoordsUtil.pack(
                    blockLight,
                    player.level().getBrightness(LightLayer.SKY, eyePos)
            );
        }
        renderState.addGeckolibData(DataTickets.PACKED_LIGHT, packedLight);
    }

    private void applyFirstPersonWeaponTransforms(PoseStack poseStack,
                                                   ItemStack stack,
                                                   ItemDisplayContext display,
                                                   float partialTick) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        ItemStack transformStack = stack;
        CompoundTag stackTag = getOrCreateStackTag(stack);
        if (stackTag.contains("Model")) {
            ItemStack override = parseClientStack(stackTag.getCompoundOrEmpty("Model"));
            if (!override.isEmpty()) {
                transformStack = override;
            }
        }

        ScgunsLegacyItemTransforms.Transform transform = ScgunsLegacyItemTransforms.get(transformStack, display);
        float scaleX = transform.scaleX();
        float scaleY = transform.scaleY();
        float scaleZ = transform.scaleZ();
        float translateX = transform.translationX();
        float translateY = transform.translationY();
        float translateZ = transform.translationZ();
        boolean right = isRightHandDisplay(display);
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);

        if (AimingHandler.get().getNormalisedAdsProgress() > 0.0 && modifiedGun.canAimDownSight()) {
            double xOffset = translateX - 0.5 * scaleX;
            double yOffset = translateY - 0.5 * scaleY;
            double zOffset = translateZ - 0.5 * scaleZ;
            Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
            xOffset += gunOrigin.x * 0.0625 * scaleX;
            yOffset += gunOrigin.y * 0.0625 * scaleY;
            zOffset += gunOrigin.z * 0.0625 * scaleZ;
            Scope scope = Gun.getScope(stack);
            Vec3 ironSightCamera;
            if (modifiedGun.canAttachType(IAttachment.Type.SCOPE) && scope != null) {
                ironSightCamera = PropertyHelper.getAttachmentPosition(stack, modifiedGun, IAttachment.Type.SCOPE).subtract(gunOrigin);
                xOffset += ironSightCamera.x * 0.0625 * scaleX;
                yOffset += ironSightCamera.y * 0.0625 * scaleY;
                zOffset += ironSightCamera.z * 0.0625 * scaleZ;
                ItemStack scopeStack = Gun.getScopeStack(stack);
                Vec3 scopeOrigin = PropertyHelper.getModelOrigin(scopeStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                Vec3 scopeCamera = PropertyHelper.getScopeCamera(scopeStack).subtract(scopeOrigin);
                Vec3 scopeScale = PropertyHelper.getAttachmentScale(stack, modifiedGun, IAttachment.Type.SCOPE);
                xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
                yOffset += (scopeCamera.y * 0.0625 * scaleY + 0.54) * scopeScale.y;
                zOffset += (scopeCamera.z * 0.0625 * scaleZ - 0.16) * scopeScale.z;
            } else {
                ironSightCamera = PropertyHelper.getIronSightCamera(stack, modifiedGun, gunOrigin).subtract(gunOrigin);
                xOffset += ironSightCamera.x * 0.0625 * scaleX;
                yOffset += ironSightCamera.y * 0.0625 * scaleY + 0.6059;
                zOffset += ironSightCamera.z * 0.0625 * scaleZ - 0.16;
                if (PropertyHelper.isLegacyIronSight(stack)) {
                    zOffset += 0.72;
                }
            }

            float side = right ? 1.0F : -1.0F;
            double time = AimingHandler.get().getNormalisedAdsProgress();
            double transition = PropertyHelper.getSightAnimations(stack, modifiedGun).getSightCurve().apply(time);
            poseStack.translate(-0.56 * side * transition, 0.52 * transition, 0.72 * transition);
            poseStack.translate(-xOffset * side * transition, -yOffset * transition, -zOffset * transition);
        }

        applyBobbingTransforms(poseStack, partialTick);
        int offset = right ? 1 : -1;
        applyRecoilTransforms(poseStack, stack, modifiedGun);
        applyAimingTransforms(poseStack, stack, modifiedGun, translateX, translateY, translateZ, offset);
        applySwayTransforms(poseStack, modifiedGun, stack, player, translateX, translateY, translateZ, partialTick);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderInfo, snapshots);
        ItemStack stack = stack(renderInfo);
        if (stack.isEmpty() || !(stack.getItem() instanceof AnimatedGunItem animatable)) {
            return;
        }

        CompoundTag renderTag = renderStackTag(renderInfo);
        updateBoneVisibilityCache(stack);

        snapshots.ifPresent("cogloader_magazine", snapshot -> snapshot.setRotY(
                snapshot.getRotY() + (float) Math.toRadians(animatable.getRotationHandler().getCurrentMagazineRotation())
        ));
        snapshots.ifPresent("cylinder_magazine", snapshot -> snapshot.setRotZ(
                snapshot.getRotZ() + (float) Math.toRadians(animatable.getRotationHandler().getCurrentCylinderRotation())
        ));
        snapshots.ifPresent("sliding_magazine", snapshot -> applySlidingMagazine(snapshot, stack, renderTag));

        for (GeoBone bone : renderInfo.model().boneLookup().get().values()) {
            BoneSnapshot snapshot = snapshots.get(bone);
            String name = bone.name();
            if (ARM_BONES.contains(name)) {
                snapshot.skipRender(true).skipChildrenRender(true);
                continue;
            }
            if ("seal".equals(name)) {
                boolean hidden = !Config.CLIENT.display.puritySeals.get();
                snapshot.skipRender(hidden).skipChildrenRender(hidden);
                continue;
            }
            if (GLOW_BONES.contains(name)) {
                // Render the cubes in a dedicated full-bright per-bone task.
                snapshot.skipRender(true);
                continue;
            }
            boolean hidden = this.boneVisibilityCache.computeIfAbsent(name, boneName -> shouldHideBone(boneName, stack));
            snapshot.skipRender(hidden);
        }
    }

    private void updateBoneVisibilityCache(ItemStack stack) {
        if (!ItemStack.isSameItemSameComponents(stack, this.lastBoneVisibilityStack)) {
            this.boneVisibilityCache.clear();
            this.lastBoneVisibilityStack = stack.copy();
        }
    }

    private static void applySlidingMagazine(BoneSnapshot snapshot, ItemStack stack, CompoundTag tag) {
        float slidePosition;
        if (tag.contains("MagazineOverride") && tag.getBooleanOr("MagazineOverride", false)) {
            slidePosition = tag.getFloatOr("MagazinePosition", 0.0F);
        } else {
            float maxAmmo = Gun.getMaxAmmo(stack);
            float currentAmmo = Gun.getAmmoCount(stack);
            slidePosition = maxAmmo <= 0.0F ? 0.0F : Math.min((maxAmmo - currentAmmo) / maxAmmo, 1.0F);
        }
        // BoneSnapshot translation is expressed in model pixels and X is inverted by GeckoLib.
        snapshot.setTranslateX(snapshot.getTranslateX() - slidePosition * 4.0F);
    }

    private boolean shouldHideBone(String name, ItemStack stack) {
        return switch (name) {
            case "sights" -> Gun.getScope(stack) != null;
            case "no_sights" -> Gun.getScope(stack) == null;
            case "wooden_stock", "light_stock", "weighted_stock" -> hideStock(name, stack);
            case "standard_barrel", "extended_barrel", "silencer", "advanced_silencer", "muzzle_brake",
                 "standard_barrel1", "extended_barrel1", "silencer1", "advanced_silencer1", "muzzle_brake1" -> hideBarrel(name, stack);
            case "light_grip", "vertical_grip", "iron_bayonet", "anthralite_bayonet", "diamond_bayonet", "netherite_bayonet" -> hideUnderBarrel(name, stack);
            case "standard_mag", "default_mag", "standard_mag_2", "default_mag_2",
                 "extended_mag", "extended_mag_2", "speed_mag", "speed_mag_2" -> hideMagazine(name, stack);
            case "standard_stock", "standard_grip" -> !Gun.getAttachment(IAttachment.Type.STOCK, stack).isEmpty();
            default -> false;
        };
    }

    private boolean hideStock(String name, ItemStack stack) {
        ItemStack attachment = Gun.getAttachment(IAttachment.Type.STOCK, stack);
        return switch (name) {
            case "wooden_stock" -> attachment.getItem() != ModItems.WOODEN_STOCK.get();
            case "light_stock" -> attachment.getItem() != ModItems.LIGHT_STOCK.get();
            case "weighted_stock" -> attachment.getItem() != ModItems.WEIGHTED_STOCK.get()
                    && attachment.getItem() != ModItems.BUMP_STOCK.get();
            default -> false;
        };
    }

    private boolean hideBarrel(String name, ItemStack stack) {
        ItemStack attachment = Gun.getAttachment(IAttachment.Type.BARREL, stack);
        return switch (name) {
            case "standard_barrel", "standard_barrel1" -> attachment.getItem() == ModItems.EXTENDED_BARREL.get();
            case "extended_barrel", "extended_barrel1" -> attachment.getItem() != ModItems.EXTENDED_BARREL.get();
            case "silencer", "silencer1" -> attachment.getItem() != ModItems.SILENCER.get();
            case "advanced_silencer", "advanced_silencer1" -> attachment.getItem() != ModItems.ADVANCED_SILENCER.get();
            case "muzzle_brake", "muzzle_brake1" -> attachment.getItem() != ModItems.MUZZLE_BRAKE.get();
            default -> false;
        };
    }

    private boolean hideUnderBarrel(String name, ItemStack stack) {
        Item attachment = Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem();
        return switch (name) {
            case "light_grip" -> attachment != ModItems.LIGHT_GRIP.get();
            case "vertical_grip" -> attachment != ModItems.VERTICAL_GRIP.get();
            case "iron_bayonet" -> attachment != ModItems.IRON_BAYONET.get();
            case "anthralite_bayonet" -> attachment != ModItems.ANTHRALITE_BAYONET.get();
            case "diamond_bayonet" -> attachment != ModItems.DIAMOND_BAYONET.get();
            case "netherite_bayonet" -> attachment != ModItems.NETHERITE_BAYONET.get();
            default -> false;
        };
    }

    private boolean hideMagazine(String name, ItemStack stack) {
        ItemStack attachment = Gun.getAttachment(IAttachment.Type.MAGAZINE, stack);
        boolean modeledMagazine = attachment.getItem() == ModItems.EXTENDED_MAG.get()
                || attachment.getItem() == ModItems.SPEED_MAG.get();
        return switch (name) {
            case "standard_mag", "default_mag", "standard_mag_2", "default_mag_2" -> modeledMagazine;
            case "extended_mag", "extended_mag_2" -> attachment.getItem() != ModItems.EXTENDED_MAG.get();
            case "speed_mag", "speed_mag_2" -> attachment.getItem() != ModItems.SPEED_MAG.get();
            default -> false;
        };
    }

    private void registerPerBoneTasks(RenderPassInfo<GeoRenderState> renderInfo) {
        for (String armName : ARM_BONES) {
            renderInfo.model().getBone(armName).ifPresent(bone ->
                    renderInfo.addPerBoneRender(bone, (info, currentBone, collector) ->
                            submitPlayerArm(info, currentBone, collector)));
        }
        renderInfo.model().getBone("attachment_bone").ifPresent(bone ->
                renderInfo.addPerBoneRender(bone, (info, currentBone, collector) ->
                        submitAttachments(info, currentBone, collector)));
        for (String glowName : GLOW_BONES) {
            renderInfo.model().getBone(glowName).ifPresent(bone ->
                    renderInfo.addPerBoneRender(bone, (info, currentBone, collector) ->
                            submitGlowBone(info, currentBone, collector)));
        }
    }

    private boolean shouldRenderArms(RenderPassInfo<GeoRenderState> renderInfo) {
        if (!Config.CLIENT.display.renderArms.get()) {
            return false;
        }
        ItemStack stack = stack(renderInfo);
        if (stack.isEmpty()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() instanceof AttachmentScreen) {
            return false;
        }
        ItemDisplayContext display = display(renderInfo, stack);
        if (!isFirstPersonHand(display) || mc.options.getCameraType() != CameraType.FIRST_PERSON) {
            return false;
        }
        return !mc.player.isSpectator() && mc.player.isAlive();
    }

    private void submitPlayerArm(RenderPassInfo<GeoRenderState> renderInfo,
                                 GeoBone bone,
                                 SubmitNodeCollector collector) {
        if (!shouldRenderArms(renderInfo)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        HumanoidArm arm = isRightArm(bone) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (arm == HumanoidArm.LEFT && stack(renderInfo).getItem() instanceof GunItem gunItem) {
            gunItem.getModifiedGun(stack(renderInfo)).determineGripType(stack(renderInfo));
        }
        PoseStack poseStack = renderInfo.poseStack();
        Identifier playerSkin = player.getSkin().body().texturePath();
        int packedLight = renderInfo.packedLight();
        int packedOverlay = renderInfo.packedOverlay();

        // A null frameSnapshot means GeckoLib is between controller states and
        // prepMatrixForBone/transformToBone used only the bone's bind pose for
        // this render. Never submit a real player arm from that transient pose.
        // Reuse the last fully animated matrix for this exact stack/bone instead.
        // If this is the first-ever frame for the stack and no stable pose exists
        // yet, suppress the arm for that single frame rather than flashing bind pose.
        if (!stabilizeFirstPersonArmPose(stack(renderInfo), bone, poseStack)) {
            return;
        }

        if (OptionalClientCompat.renderCleanAnimatedArm(
                player, arm, poseStack, collector, playerSkin, bone, packedLight, packedOverlay)) {
            return;
        }

        // GeckoLib 5's per-bone task enters the callback at the bone pivot
        // (RenderUtil.transformToBone ends with translateToPivotPoint). The 1.20.1
        // renderer instead called GeckoLib 4's prepMatrixForBone, which ended
        // translated AWAY from the pivot, then applied Scorched Guns' arm scale/
        // offset, and finally translated the vanilla ModelPart back to the pivot.
        // Restore that exact ordering before drawing the 26.2 arm geometry.
        bone.translateAwayFromPivotPoint(poseStack);
        boolean right = arm == HumanoidArm.RIGHT;
        poseStack.scale(0.66F, right ? 0.78F : 0.79F, 0.66F);
        poseStack.translate(right ? 0.25D : -0.25D, -0.1D, 0.1625D);
        RenderUtil.renderBoneLocalFirstPersonArm(
                player, arm, poseStack, collector, bone, packedLight, packedOverlay);
    }

    private boolean stabilizeFirstPersonArmPose(ItemStack stack, GeoBone bone, PoseStack poseStack) {
        String cacheKey = GeoItem.getId(stack) + ":" + bone.name();

        if (bone.frameSnapshot != null) {
            PoseStack.Pose stablePose = this.lastStableFirstPersonArmPoses.computeIfAbsent(
                    cacheKey, ignored -> new PoseStack.Pose());
            stablePose.set(poseStack.last());
            return true;
        }

        PoseStack.Pose stablePose = this.lastStableFirstPersonArmPoses.get(cacheKey);
        if (stablePose == null) {
            return false;
        }

        poseStack.last().set(stablePose);
        return true;
    }

    private static boolean isRightArm(GeoBone bone) {
        return "right_arm".equals(bone.name()) || "fake_right_arm".equals(bone.name());
    }

    private void submitGlowBone(RenderPassInfo<GeoRenderState> renderInfo,
                                GeoBone bone,
                                SubmitNodeCollector collector) {
        if (!(bone instanceof CuboidGeoBone cuboidBone)) {
            return;
        }

        ItemStack stack = stack(renderInfo);

        /*
         * These seven black-powder weapons use a child bone named "glow" under
         * their animated "flash" bone for the small lock/pan spark. In GeckoLib
         * 4 the inactive animation left that helper effectively hidden. GeckoLib
         * 5 restores unanimated bones to their bind pose, so the full-bright
         * per-bone task below was drawing the spark permanently in GUI and
         * third-person renders.
         *
         * Do not treat every glow bone this way: many other Scorched Guns models
         * use glow/glow_2/glow_3 as real emissive geometry. Restrict the fix to
         * the affected gun items and render their helper only while a firing
         * animation is actually active.
         */
        if ("glow".equals(bone.name()) && isBlackPowderFlashHelperGun(stack)) {
            ItemDisplayContext context = display(renderInfo, stack);
            if (!isHandDisplay(context) || !isBlackPowderShotAnimationActive(stack)) {
                return;
            }
        }

        RenderType renderType = getRenderType(renderInfo.renderState(), getTextureLocation(renderInfo.renderState()));
        if (renderType == null) {
            return;
        }
        collector.submitCustomGeometry(renderInfo.poseStack(), renderType, (pose, buffer) -> {
            PoseStack poseStack = renderInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            for (var cube : cuboidBone.cubes) {
                poseStack.pushPose();
                cube.render(
                        poseStack,
                        buffer,
                        LightCoordsUtil.FULL_BRIGHT,
                        renderInfo.packedOverlay(),
                        renderInfo.renderColor()
                );
                poseStack.popPose();
            }
            poseStack.popPose();
        });
    }

    private static boolean isHandDisplay(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    private static boolean isBlackPowderFlashHelperGun(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.FLINTLOCK_PISTOL.get()
                || item == ModItems.HANDCANNON.get()
                || item == ModItems.MUSKET.get()
                || item == ModItems.BLUNDERBUSS.get()
                || item == ModItems.FENCER_THUMPER.get()
                || item == ModItems.DOUBLET.get()
                || item == ModItems.REPEATING_MUSKET.get();
    }

    private static boolean isBlackPowderShotAnimationActive(ItemStack stack) {
        if (!(stack.getItem() instanceof AnimatedGunItem animatable)) {
            return false;
        }

        try {
            long id = GeoItem.getId(stack);
            var controller = animatable.getAnimatableInstanceCache()
                    .getManagerForId(id)
                    .getAnimationControllers()
                    .get("controller");
            if (controller == null || controller.getCurrentAnimationPoint() == null) {
                return false;
            }

            String animation = controller.getCurrentAnimationPoint().animation().name();
            return "shoot".equals(animation)
                    || "shoot1".equals(animation)
                    || "aim_shoot".equals(animation)
                    || "aim_shoot1".equals(animation)
                    || "carbine_shoot".equals(animation)
                    || "carbine_aim_shoot".equals(animation);
        } catch (Throwable ignored) {
            // A missing/transient animation manager must hide the helper, never
            // make it fall back to the bind-pose spark.
            return false;
        }
    }

    private void submitAttachments(RenderPassInfo<GeoRenderState> renderInfo,
                                   GeoBone bone,
                                   SubmitNodeCollector collector) {
        ItemStack stack = stack(renderInfo);
        if (stack.isEmpty() || !(stack.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Gun modifiedGun = gunItem.getModifiedGun(stack);
        CompoundTag attachments = renderStackTag(renderInfo).getCompoundOrEmpty("Attachments");
        Set<Item> modeledInGun = modeledInGunAttachments();

        PoseStack poseStack = renderInfo.poseStack();
        // HF89: GeckoLib 4 attachment-layer parity. PerBoneRender enters at the bone pivot;
        // the 1.20.1 renderForBone path applied attachment offsets after prepMatrixForBone(),
        // which had already translated away from that pivot. Restore that exact matrix state.
        bone.translateAwayFromPivotPoint(poseStack);
        for (String tagKey : attachments.keySet()) {
            IAttachment.Type type = IAttachment.Type.byTagKey(tagKey);
            if (type == null || !modifiedGun.canAttachType(type)) {
                continue;
            }
            ItemStack attachmentStack = parseClientStack(
                    attachments.getCompound(type.getTagKey()).orElseGet(CompoundTag::new));
            if (attachmentStack.isEmpty() || modeledInGun.contains(attachmentStack.getItem())) {
                continue;
            }

            poseStack.pushPose();
            Vec3 origin = PropertyHelper.getModelOrigin(attachmentStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
            Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
            Vec3 translation = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
            Vec3 scale = PropertyHelper.getAttachmentScale(stack, modifiedGun, type);
            Vec3 center = origin.subtract(8.0, 8.0, 8.0).scale(0.0625);
            poseStack.translate(-origin.x * 0.0625, -origin.y * 0.0625, -origin.z * 0.0625);
            poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625, gunOrigin.z * 0.0625);
            poseStack.translate(translation.x * 0.0625, translation.y * 0.0625, translation.z * 0.0625);
            poseStack.translate(center.x, center.y, center.z);
            poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
            poseStack.translate(-center.x, -center.y, -center.z);

            IOverrideModel overrideModel = ModelOverrides.getModel(attachmentStack);
            if (overrideModel != null) {
                overrideModel.render(
                        renderInfo.renderState().getPartialTick(),
                        ItemDisplayContext.NONE,
                        attachmentStack,
                        stack,
                        Minecraft.getInstance().player,
                        poseStack,
                        new GunRenderContext(collector, ItemDisplayContext.NONE),
                        renderInfo.packedLight(),
                        renderInfo.packedOverlay()
                );
            } else {
                this.attachmentRenderer.submit(
                        renderInfo,
                        attachmentStack,
                        collector,
                        renderInfo.packedLight(),
                        renderInfo.packedOverlay()
                );
            }
            poseStack.popPose();
        }
    }

    private void applyRecoilTransforms(PoseStack poseStack, ItemStack item, Gun gun) {
        double recoilNormal = GunRecoilHandler.get().getGunRecoilNormal();
        if (Gun.hasAttachmentEquipped(item, gun, IAttachment.Type.SCOPE)) {
            recoilNormal -= recoilNormal * 0.5 * AimingHandler.get().getNormalisedAdsProgress();
        }

        Minecraft mc = Minecraft.getInstance();
        float kickReduction, recoilReduction;

        if (mc.player != null) {
            kickReduction = 1.0F - GunModifierHelper.getKickReduction(mc.player, item);
            recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(mc.player, item);
        } else {
            kickReduction = 1.0F - GunModifierHelper.getKickReduction(item);
            recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(item);
        }

        double kick = (double) gun.getGeneral().getRecoilKick() * 0.0625 * recoilNormal * GunRecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilLift = (float) ((double) gun.getGeneral().getRecoilAngle() * recoilNormal) * (float) GunRecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilSwayAmount = (float) (2.0 + (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
        float recoilSway = (float) ((double) (GunRecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2.0F) * recoilNormal);

        poseStack.translate(0.0, 0.0, kick * (double) kickReduction);
        poseStack.translate(0.0, 0.0, 0.15);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(recoilSway * recoilReduction / 5.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(recoilSway * recoilReduction / 5.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(recoilLift * recoilReduction / 5.0F)));
        poseStack.translate(0.0, 0.0, -0.15);
    }

    private void applyBobbingTransforms(PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.bobView().get()) {
            Entity var5 = mc.getCameraEntity();
            if (var5 instanceof Player player) {
                float distanceWalked = -player.walkAnimation.position(partialTicks);
                float bobbing = player.walkAnimation.speed(partialTicks);
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * 3.1415927F - 0.2F) * bobbing) * 5.0F))));
                poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(-(Mth.sin(distanceWalked * 3.1415927F) * bobbing * 3.0F))));
                poseStack.translate(-(Mth.sin(distanceWalked * 3.1415927F) * bobbing * 0.5F), Math.abs(Mth.cos(distanceWalked * 3.1415927F) * bobbing), 0.0);
                bobbing = (float) ((double) bobbing * (player.isSprinting() ? 8.0 : 4.0));
                bobbing = (float) ((double) bobbing * Config.CLIENT.display.bobbingIntensity.get());
                double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * (double) this.sprintIntensity;
                if (!AimingHandler.get().isAiming()) {
                    poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(Math.abs(Mth.cos(distanceWalked * 3.1415927F - 0.2F) * bobbing) * -2.0F * (float) invertZoomProgress)));
                    poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(Mth.sin(distanceWalked * 3.1415927F) * bobbing * 3.0F * (float) invertZoomProgress)));
                }
            }
        }

    }

    private void applyAimingTransforms(PoseStack poseStack, ItemStack stack, Gun modifiedGun, float x, float y, float z, int offset) {
        poseStack.translate(x * (float) offset, y, z);
        poseStack.translate(0.0, -0.25, 0.25);
        float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180.0));
        aiming = PropertyHelper.getSightAnimations(stack, modifiedGun).getAimTransformCurve().apply(aiming / 2.0F);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(aiming * 10.0F * (float) offset)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(aiming * 8.0F)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(aiming * 8.0F * (float) offset)));
        poseStack.translate(0.0, 0.25, -0.25);
        poseStack.translate(-x * (float) offset, -y, -z);
    }

    private void applySwayTransforms(PoseStack poseStack, Gun modifiedGun, ItemStack stack, LocalPlayer player, float x, float y, float z, float partialTicks) {
        if (!Config.CLIENT.display.weaponSway.get() || player == null) {
            return;
        }

        poseStack.pushPose();
        {
            poseStack.translate(x, y, z);
            double zOffset = modifiedGun.determineGripType(stack).heldAnimation().getFallSwayZOffset();
            poseStack.translate(0, -0.25, zOffset);
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevFallSway, this.fallSway))));
            poseStack.translate(0, 0.25, -zOffset);
            float bobPitch = Mth.rotLerp(partialTicks, player.xBobO, player.xBob);
            float headPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
            float swayPitch = headPitch - bobPitch;
            float adsProgress = (float) AimingHandler.get().getNormalisedAdsProgress();
            swayPitch *= (float) (1.0 - 0.5 * adsProgress);
            float pitchSensitivity = Config.CLIENT.display.swaySensitivity.get().floatValue();
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Config.CLIENT.display.swayType.get().getPitchRotation()
                    .rotationDegrees(swayPitch * pitchSensitivity)));
            float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
            float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
            float swayYaw = headYaw - bobYaw;
            swayYaw *= (float) (1.0 - 0.5 * adsProgress);
            float yawSensitivity = Config.CLIENT.display.swaySensitivity.get().floatValue();
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Config.CLIENT.display.swayType.get().getYawRotation()
                    .rotationDegrees(swayYaw * yawSensitivity)));

            poseStack.translate(-x, -y, -z);
        }
        poseStack.popPose();
    }


    private void renderMuzzleFlash(@Nullable LivingEntity entity,
                                   PoseStack poseStack,
                                   SubmitNodeCollector collector,
                                   ItemStack weapon,
                                   ItemDisplayContext display,
                                   float partialTicks) {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        Gun.Display.Flash flash = modifiedGun.getDisplay().getFlash();
        if (flash == null || entity == null) {
            return;
        }
        if (display != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                && display != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                && display != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                && display != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            return;
        }

        boolean beamActive = BeamHandler.activeBeams.containsKey(entity.getUUID());
        if (!beamActive && !GunRenderingHandler.entityIdForMuzzleFlash.contains(entity.getId())) {
            return;
        }

        float randomValue = GunRenderingHandler.entityIdToRandomValue.getOrDefault(entity.getId(), 0.0F);
        boolean mirror = DualWieldShotTracker.get().shouldUseAlternateAnimation(entity.getId())
                && flash.hasAlternateMuzzleFlash();
        Identifier texture = Identifier.fromNamespaceAndPath(
                Reference.MOD_ID,
                "textures/effect/" + flash.getTextureLocation() + ".png"
        );
        drawMuzzleFlash(weapon, modifiedGun, randomValue, mirror, poseStack, collector, partialTicks, texture, entity);
        if (flash.shouldSpawnParticles() && flash.getParticleType() != null) {
            spawnParticles(flash, entity);
        }
    }

    private void drawMuzzleFlash(ItemStack weapon,
                                 Gun modifiedGun,
                                 float random,
                                 boolean mirror,
                                 PoseStack poseStack,
                                 SubmitNodeCollector collector,
                                 float partialTicks,
                                 Identifier flashTexture,
                                 LivingEntity entity) {
        if (!PropertyHelper.hasMuzzleFlash(weapon, modifiedGun)) {
            return;
        }
        Gun.Display.Flash flash = modifiedGun.getDisplay().getFlash();
        if (flash == null) {
            return;
        }

        poseStack.pushPose();
        int shotCount = DualWieldShotTracker.get().getShotCount(entity.getId());
        Vec3 weaponOrigin = PropertyHelper.getModelOrigin(weapon, PropertyHelper.GUN_DEFAULT_ORIGIN);
        Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(weapon, modifiedGun).subtract(weaponOrigin);
        if (shotCount % 2 == 1 && flash.hasAlternateMuzzleFlash()) {
            Vec3 alternatePos = flash.getAlternatePosition();
            flashPosition = flashPosition.add(
                    alternatePos.x * 0.0625,
                    alternatePos.y * 0.0625,
                    alternatePos.z * 0.0625
            );
        }

        poseStack.translate(weaponOrigin.x * 0.0625, weaponOrigin.y * 0.0625, weaponOrigin.z * 0.0625);
        poseStack.translate(
                flashPosition.x * 0.0625 + 0.575,
                flashPosition.y * 0.0625 + 1.08,
                flashPosition.z * 0.0625
        );
        if (AimingHandler.get().isAiming()) {
            poseStack.translate(-0.075, 0.0, 0.0);
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, weapon);
        if (!barrelStack.isEmpty() && barrelStack.getItem() instanceof IBarrel barrel
                && !PropertyHelper.isUsingBarrelMuzzleFlash(barrelStack)) {
            Vec3 scale = PropertyHelper.getAttachmentScale(weapon, modifiedGun, IAttachment.Type.BARREL);
            poseStack.translate(0.0, 0.0, -barrel.getProperties().getLength() * 0.0625 * scale.z);
        }

        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(360.0F * random)));
        if (mirror) {
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(180.0F)));
        }
        Vec3 flashScale = PropertyHelper.getMuzzleFlashScale(weapon, modifiedGun);
        float scaleX = ((float) flashScale.x / 2.0F) - ((float) flashScale.x / 2.0F) * (1.0F - partialTicks);
        float scaleY = ((float) flashScale.y / 2.0F) - ((float) flashScale.y / 2.0F) * (1.0F - partialTicks);
        poseStack.scale(scaleX * 2.0F, scaleY * 2.0F, 1.0F);
        float scaleModifier = (float) GunModifierHelper.getMuzzleFlashScale(weapon, 1.0);
        poseStack.scale(scaleModifier, scaleModifier, 1.0F);
        poseStack.translate(-0.5, -0.5, 0.0);
        submitMuzzleFlashQuad(collector, poseStack, weapon, flashTexture);
        poseStack.popPose();
    }

    private static void submitMuzzleFlashQuad(SubmitNodeCollector collector,
                                              PoseStack poseStack,
                                              ItemStack weapon,
                                              Identifier flashTexture) {
        float minU = weapon.isEnchanted() ? 0.5F : 0.0F;
        float maxU = weapon.isEnchanted() ? 1.0F : 0.5F;
        collector.submitCustomGeometry(poseStack, GunRenderType.getMuzzleFlash(flashTexture), (pose, builder) -> {
            Matrix4f matrix = pose.pose();
            emitFlashVertex(builder, pose, matrix, 0, 0, 0, maxU, 1.0F);
            emitFlashVertex(builder, pose, matrix, 1, 0, 0, minU, 1.0F);
            emitFlashVertex(builder, pose, matrix, 1, 1, 0, minU, 0.0F);
            emitFlashVertex(builder, pose, matrix, 0, 1, 0, maxU, 0.0F);
        });
    }

    private static void emitFlashVertex(VertexConsumer builder,
                                        PoseStack.Pose pose,
                                        Matrix4f matrix,
                                        float x, float y, float z,
                                        float u, float v) {
        builder.addVertex(matrix, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private void spawnParticles(Gun.Display.Flash flash, LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            return;
        }
        ClientLevel world = (ClientLevel) entity.level();
        RandomSource random = entity.getRandom();
        double posX = entity.getX();
        double posY = entity.getY() + entity.getEyeHeight();
        double posZ = entity.getZ();
        SimpleParticleType particleType = flash.getParticleType();
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastParticleSpawnTime < PARTICLE_COOLDOWN_MS) {
            return;
        }
        this.lastParticleSpawnTime = currentTime;

        double radius = flash.getParticleRingRadius();
        if (radius > 0) {
            for (int i = 0; i < flash.getParticleCount(); i++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double randomizedRadius = radius * (0.8 + random.nextDouble() * 0.4);
                double offsetX = Math.cos(angle) * randomizedRadius;
                double offsetZ = Math.sin(angle) * randomizedRadius;
                double spread = flash.getParticleSpread();
                double offsetY = (random.nextDouble() - 0.5) * spread;
                offsetX += (random.nextDouble() - 0.5) * spread * 0.5;
                offsetZ += (random.nextDouble() - 0.5) * spread * 0.5;
                double motionStrength = 0.03 + random.nextDouble() * 0.02;
                world.addParticle(
                        particleType,
                        posX + offsetX,
                        posY + offsetY,
                        posZ + offsetZ,
                        offsetX * motionStrength,
                        offsetY * motionStrength + 0.01,
                        offsetZ * motionStrength
                );
            }
        } else {
            for (int i = 0; i < flash.getParticleCount(); i++) {
                double spread = flash.getParticleSpread();
                double offsetX = (random.nextDouble() - 0.5) * spread;
                double offsetY = (random.nextDouble() - 0.5) * spread;
                double offsetZ = (random.nextDouble() - 0.5) * spread;
                world.addParticle(
                        particleType,
                        posX + offsetX,
                        posY + offsetY,
                        posZ + offsetZ,
                        offsetX * 0.1,
                        offsetY * 0.1,
                        offsetZ * 0.1
                );
            }
        }
    }
}
