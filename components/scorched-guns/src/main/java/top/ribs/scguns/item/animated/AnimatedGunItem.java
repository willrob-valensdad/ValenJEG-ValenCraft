package top.ribs.scguns.item.animated;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.ClientUtil;
import com.geckolib.renderer.GeoItemRenderer;
import top.ribs.scguns.Config;
import top.ribs.scguns.animations.GunAnimations;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.handler.DualWieldShotTracker;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.client.render.gun.animated.AnimatedGunRenderer;
import top.ribs.scguns.client.util.GunRotationHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.common.network.ServerPlayHandler;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageEjectCasing;
import top.ribs.scguns.network.message.C2SMessageGunLoaded;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
import java.util.function.Consumer;

import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
public class AnimatedGunItem extends GunItem implements GeoAnimatable, GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final String gunID;
    private final SoundEvent reloadSoundMagOut;
    private final SoundEvent reloadSoundMagIn;
    private final SoundEvent reloadSoundEnd;
    private final SoundEvent boltPullSound;
    private final SoundEvent boltReleaseSound;
    private int drawTick = 0;
    private static final String RELOAD_START_TIME = "reloadStartTime";
    public static final String RELOAD_STATE = "scguns:ReloadState";
    private final GunRotationHandler rotationHandler = new GunRotationHandler();

    public GunRotationHandler getRotationHandler() {
        return rotationHandler;
    }

    private enum ReloadState {
        NONE,
        STARTING,
        LOADING,
        STOPPING
    }
    public AnimatedGunItem(Item.Properties properties, String path, SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent boltPullSound, SoundEvent boltReleaseSound) {
        this(properties, 13, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, boltPullSound, boltReleaseSound);
    }

    protected AnimatedGunItem(Item.Properties properties, int enchantability, String path, SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent boltPullSound, SoundEvent boltReleaseSound) {
        super(properties, enchantability);
        this.gunID = path;
        this.reloadSoundMagOut = reloadSoundMagOut;
        this.reloadSoundMagIn = reloadSoundMagIn;
        this.reloadSoundEnd = reloadSoundEnd;
        this.boltPullSound = boltPullSound;
        this.boltReleaseSound = boltReleaseSound;
    }

    private static CompoundTag getOrCreateStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : new CompoundTag();
    }

    private static void setStackTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public boolean isInCarbineMode(ItemStack stack) {

        if (stack.getItem() instanceof GunItem gunItem) {
            return gunItem.isOneHandedCarbineCandidate(stack) &&
                    (Gun.hasExtendedBarrel(stack) || Gun.hasStock(stack));
        }
        return false;
    }
    @OnlyIn(Dist.CLIENT)
    private void updateCarbineState(ItemStack stack, AnimationController<GeoAnimatable> controller) {
        boolean isCarbine = isInCarbineMode(stack);
        String currentAnim = controller.getCurrentAnimationPoint() != null ?
                controller.getCurrentAnimationPoint().animation().name() : "";
        boolean isCurrentCarbine = currentAnim.startsWith("carbine_");
        if (isCarbine != isCurrentCarbine) {
            controller.reset();
            tryTriggerAnimationCompat(controller, isCarbine ? "carbine_idle" : "idle");
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel world, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        /*
         * GeckoLib 4 stored GeoItem's stable animatable id in the stack NBT under
         * "GeckoLibID". GeckoLib 5 no longer reads that NBT key: GeoItem#getId
         * reads GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT instead and falls
         * back to ItemStack#hashCode when the component is absent.
         *
         * The initial 26.2 port preserved the old NBT assignment, so every gun was
         * effectively UNASSIGNED from GeckoLib 5's point of view. Scorched Guns
         * mutates CUSTOM_DATA continuously (IsAiming, DrawnTick, reload flags, ...),
         * and ItemStacks are also copied/rebuilt by the first-person item pipeline.
         * That makes the fallback identity unsuitable for the animation-manager key:
         * controller/bone state can jump to a fresh manager during ADS, draw/equip
         * and hotbar transitions, producing the vertical bind-pose arm flash.
         *
         * Use GeckoLib 5's canonical server-side id allocator. The data component is
         * part of the ItemStack and is synchronized to the client, so GeoItem#getId
         * remains stable while the gun's CUSTOM_DATA changes. Assign it BEFORE any
         * id comparisons in prepareInventoryStack.
         */
        GeoItem.getOrAssignId(stack, world);
        prepareInventoryStack(stack, entity);
    }

    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        CompoundTag tag = getOrCreateStackTag(stack);
        cleanupReloadState(tag);
        tag.remove("IsShooting");
        tag.remove("IsInspecting");
        tag.remove("IsAiming");
        tag.remove("IsRunning");
        tag.remove("IsDrawn");
        tag.remove("IsDrawing");
        tag.remove("DrawnTick");
        tag.remove("loaded");
        tag.remove("shouldStopOnLoopEnd");
        tag.remove("shouldTransitionToStop");
        tag.remove("MagazinePosition");
        tag.remove("MagazineOverride");
        tag.remove("scguns:MagazineTracking");
        tag.remove("_InitializedThisSession");
        tag.putBoolean("IsDroppedItem", true);

        if (entity.level().isClientSide()) {
            boolean isReloading = tag.getBooleanOr("scguns:IsReloading", false);
            tag.putBoolean("WasReloadingLastTick", isReloading);
        }
        setStackTag(stack, tag);

        if (entity.level().isClientSide()) {
            clientInventoryTick(stack, entity, -1, false);
        }
        return false;
    }

    private void prepareInventoryStack(ItemStack stack, Entity entity) {
        CompoundTag tag = getOrCreateStackTag(stack);
        tag.remove("IsDroppedItem");
        if (entity instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand != stack && GeoItem.getId(mainHand) != GeoItem.getId(stack)) {
                tag.remove("_InitializedThisSession");
            }
        }
        setStackTag(stack, tag);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientInventoryTick26(ItemStack stack, Entity entity, int slot, boolean selected) {
        prepareInventoryStack(stack, entity);
        clientInventoryTick(stack, entity, slot, selected);
    }



    @OnlyIn(Dist.CLIENT)
    private void clientInventoryTick(ItemStack stack, Entity entity, int slot, boolean selected) {
        CompoundTag nbtCompound = getOrCreateStackTag(stack);
        if (entity instanceof Player player) {
            if (!selected) {
                handleInactiveInventoryStack(stack, nbtCompound);
                setStackTag(stack, nbtCompound);
                return;
            }
            handleReloadStateSynchronization(stack, entity, nbtCompound);
            handleAnimationControllerUpdates(stack, entity, nbtCompound);
            handlePlayerSpecificLogic(stack, player, nbtCompound);
        } else {
            handleReloadStateSynchronization(stack, entity, nbtCompound);
            handleAnimationControllerUpdates(stack, entity, nbtCompound);
        }
        setStackTag(stack, nbtCompound);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleInactiveInventoryStack(ItemStack stack, CompoundTag nbtCompound) {
        /*
         * Minecraft 26.2 changes the selected hotbar slot before the first-person
         * hand-swap animation has finished rendering the outgoing ItemStack. The
         * client inventory tick therefore sees the old gun as inactive while
         * ItemInHandRenderer is still lowering that exact stack for a few frames.
         *
         * Resetting GeckoLib here used to be harmless in 1.20.1, but in GeckoLib 5
         * it immediately rebuilds the outgoing gun's controller/bone state. Because
         * right_arm/left_arm are anchors for the real player arms, that reset leaks
         * the bind/default arm transform into the still-visible unequip transition,
         * producing the single vertical-arm flash that remained after HF85.6.
         *
         * Clear gameplay/transient flags immediately, but PARK the animation manager
         * in its last valid pose until the outgoing stack is no longer rendered. On
         * the next equip, prepareInventoryStack removes _InitializedThisSession and
         * the normal draw initialization replaces this parked pose with the correct
         * draw/idle animation. This preserves the hand-lowering transform without
         * touching ADS, equip, reload, shoot, or the stable GeckoLib 5 stack id.
         */
        cleanupAllReloadTags(nbtCompound);
        nbtCompound.remove("IsDrawn");
        nbtCompound.remove("IsDrawing");
        nbtCompound.remove("DrawnTick");
        nbtCompound.remove("IsShooting");
        nbtCompound.remove("IsInspecting");
        nbtCompound.remove("IsAiming");
        nbtCompound.remove("IsRunning");
        nbtCompound.remove("loaded");

        // Deliberately do NOT reset/retarget the GeckoLib controller here.
        // The outgoing stack can still be rendered by the first-person hand swap.
    }
    @OnlyIn(Dist.CLIENT)
    private void handleReloadStateSynchronization(ItemStack stack, Entity entity, CompoundTag nbtCompound) {
        boolean currentReloading = nbtCompound.getBoolean("scguns:IsReloading").orElse(false);
        boolean serverReloading = entity instanceof Player ?
                ModSyncedDataKeys.RELOADING.getValue((Player)entity) : false;
        String currentReloadState = nbtCompound.getString(RELOAD_STATE).orElse("");
        boolean inCriticalPhase = nbtCompound.getBoolean("InCriticalReloadPhase").orElse(false);

        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        boolean isManualReload = modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL;

        long currentTime = System.currentTimeMillis();
        if (currentReloading && inCriticalPhase && !isManualReload) {
            long reloadStartTime = nbtCompound.getLong("ReloadStartTime").orElse(0L);
            if (reloadStartTime == 0) {
                nbtCompound.putLong("ReloadStartTime", currentTime);
                reloadStartTime = currentTime;
            }
            if (currentTime - reloadStartTime > 10000) {
                cleanupReloadState(nbtCompound);
                nbtCompound.remove("InCriticalReloadPhase");
                nbtCompound.remove("ReloadStartTime");
                nbtCompound.remove("ReloadAnimationStarted");
                nbtCompound.remove("ReloadAnimationRestarted");
                nbtCompound.remove("ReloadCompleted");
                if (entity instanceof Player) {
                    ModSyncedDataKeys.RELOADING.setValue((Player)entity, false);
                }
                return;
            }
        } else {
            nbtCompound.remove("ReloadStartTime");
        }

        if (!serverReloading && inCriticalPhase && !isManualReload) {
            cleanupReloadState(nbtCompound);
            nbtCompound.remove("InCriticalReloadPhase");
            nbtCompound.remove("ReloadStartTime");
            nbtCompound.remove("ReloadAnimationStarted");
            nbtCompound.remove("ReloadAnimationRestarted");
            nbtCompound.remove("ReloadCompleted");
            return;
        }

        // For non-manual reloads, prevent state corruption
        if (!isManualReload) {
            // Clear any manual reload states that shouldn't exist
            if (currentReloadState.equals("STOPPING") || currentReloadState.equals("LOADING") || currentReloadState.equals("STARTING")) {
                nbtCompound.remove(RELOAD_STATE);
            }

            if (serverReloading && !currentReloading) {
                nbtCompound.putBoolean("scguns:IsReloading", true);
                nbtCompound.remove(RELOAD_STATE);
                nbtCompound.putLong("ReloadStartTime", currentTime);
            } else if (!serverReloading && currentReloading) {
                nbtCompound.remove("scguns:IsReloading");
                cleanupReloadState(nbtCompound);
                nbtCompound.remove("InCriticalReloadPhase");
                nbtCompound.remove("ReloadStartTime");
                nbtCompound.remove("ReloadAnimationStarted");
                nbtCompound.remove("ReloadAnimationRestarted");
                nbtCompound.remove("ReloadCompleted");
            }
            return;
        }
        boolean isInTransitionState = currentReloadState.equals("STOPPING") ||
                nbtCompound.getBoolean("scguns:IsPlayingReloadStop").orElse(false);

        boolean playerTryingToAim = false;
        if (entity instanceof Player) {
            playerTryingToAim = KeyBinds.getAimMapping().isDown();
        }

        if (serverReloading && !currentReloading && !isInTransitionState) {
            nbtCompound.putBoolean("scguns:IsReloading", true);
            if (!nbtCompound.contains(RELOAD_STATE)) {
                nbtCompound.putString(RELOAD_STATE, ReloadState.NONE.name());
            }
        } else if (!serverReloading && currentReloading && !isInTransitionState) {
            nbtCompound.putString(RELOAD_STATE, ReloadState.STOPPING.name());
            nbtCompound.putBoolean("scguns:IsPlayingReloadStop", true);
        } else if (!serverReloading && isInTransitionState && !playerTryingToAim) {
            long lastCleanup = nbtCompound.getLong("LastCleanupTime").orElse(0L);

            if (currentTime - lastCleanup > 50) {
                nbtCompound.putLong("LastCleanupTime", currentTime);
                finishReloadTransition(nbtCompound);
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleAnimationControllerUpdates(ItemStack stack, Entity entity, CompoundTag nbtCompound) {
        long id = GeoItem.getId(stack);
        AnimationController<GeoAnimatable> animationController = this.getAnimatableInstanceCache()
                .getManagerForId(id)
                .getAnimationControllers()
                .get("controller");

        rotationHandler.updateRotations(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        if (nbtCompound.getBoolean("AttachmentChanged").orElse(false)) {
            if (animationController != null) {
                updateCarbineState(stack, animationController);
            }
            nbtCompound.remove("AttachmentChanged");
        }

        boolean inCriticalPhase = nbtCompound.getBoolean("InCriticalReloadPhase").orElse(false);
        boolean isReloading = nbtCompound.getBoolean("scguns:IsReloading").orElse(false);

        if (animationController != null && !isReloading && !inCriticalPhase
                && !isAnimationPlaying(animationController, "draw")
                && !isAnimationPlaying(animationController, "carbine_draw")
                && !isAnimationPlaying(animationController, "shoot")
                && !isAnimationPlaying(animationController, "shoot1")
                && !isAnimationPlaying(animationController, "carbine_shoot")
                && !isAnimationPlaying(animationController, "inspect")
                && !isAnimationPlaying(animationController, "carbine_inspect")
                && !isAnimationPlaying(animationController, "reload")
                && !isAnimationPlaying(animationController, "carbine_reload")
                && !isAnimationPlaying(animationController, "reload_start")
                && !isAnimationPlaying(animationController, "carbine_reload_start")
                && !isAnimationPlaying(animationController, "reload_loop")
                && !isAnimationPlaying(animationController, "carbine_reload_loop")
                && !isAnimationPlaying(animationController, "reload_stop")
                && !isAnimationPlaying(animationController, "carbine_reload_stop")) {
            updateCarbineState(stack, animationController);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handlePlayerSpecificLogic(ItemStack stack, Player player, CompoundTag nbtCompound) {
        long id = GeoItem.getId(stack);
        AnimationController<GeoAnimatable> animationController = this.getAnimatableInstanceCache()
                .getManagerForId(id)
                .getAnimationControllers()
                .get("controller");

        if (GeoItem.getId(player.getMainHandItem()) != id) {
            handleItemNotHeld(nbtCompound, animationController, stack, player);
            return;
        }

        handleDrawingState(nbtCompound);

        handleActionStates(nbtCompound, animationController, stack, player);

        handlePlayerStateUpdates(nbtCompound, player, stack);

        handleInitializationAndAnimations(nbtCompound, animationController, stack, player);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleItemNotHeld(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                   ItemStack stack, Player player) {
        if (nbtCompound.getBoolean("IsDrawn").orElse(false)) {
            nbtCompound.remove("IsDrawn");
            nbtCompound.remove("DrawnTick");
            this.drawTick = 0;

            cleanupAllReloadTags(nbtCompound);
            ModSyncedDataKeys.RELOADING.setValue(player, false);

            nbtCompound.remove("IsShooting");
            nbtCompound.remove("IsInspecting");
            nbtCompound.remove("IsAiming");
            nbtCompound.remove("IsRunning");
            nbtCompound.remove("loaded");
            nbtCompound.remove("DrawnTick");
        }

        animationController.reset();
        if (isInCarbineMode(stack)) {
            tryTriggerAnimationCompat(animationController, "carbine_idle");
        } else {
            tryTriggerAnimationCompat(animationController, "idle");
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleDrawingState(CompoundTag nbtCompound) {
        this.updateBooleanTag(nbtCompound, "IsDrawing", nbtCompound.getBoolean("IsDrawn").orElse(false));
        if (nbtCompound.getBoolean("IsDrawing").orElse(false) && nbtCompound.getInt("DrawnTick").orElse(0) < 15) {
            this.drawTick++;
            nbtCompound.putInt("DrawnTick", this.drawTick);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleActionStates(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                    ItemStack stack, Player player) {
        if (nbtCompound.getBoolean("IsShooting").orElse(false)) {
            handleShootState(nbtCompound, animationController, stack);
        }

        if (nbtCompound.getBoolean("IsInspecting").orElse(false)) {
            handleInspectState(animationController, stack);
        }

        if (MeleeAttackHandler.isBanzaiActive() &&
                (isAnimationPlaying(animationController, "inspect") ||
                        isAnimationPlaying(animationController, "carbine_inspect"))) {
            if (isInCarbineMode(stack)) {
                tryTriggerAnimationCompat(animationController, "carbine_idle");
            } else {
                tryTriggerAnimationCompat(animationController, "idle");
            }
        }

        if (player.isSprinting() &&
                (isAnimationPlaying(animationController, "inspect") ||
                        isAnimationPlaying(animationController, "carbine_inspect"))) {
            if (isInCarbineMode(stack)) {
                tryTriggerAnimationCompat(animationController, "carbine_idle");
            } else {
                tryTriggerAnimationCompat(animationController, "idle");
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handlePlayerStateUpdates(CompoundTag nbtCompound, Player player, ItemStack stack) {
        if (!nbtCompound.getBoolean("IsDrawn").orElse(false)) {
            nbtCompound.putBoolean("IsDrawn", true);
        }

        boolean isSprinting = player.isSprinting();
        boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
        boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean clientReloading = nbtCompound.getBoolean("scguns:IsReloading").orElse(false);

        if (serverReloading || clientReloading) {
            if (isAiming) {
                ModSyncedDataKeys.AIMING.setValue(player, false);
            }
            this.updateBooleanTag(nbtCompound, "IsAiming", false);
            this.updateBooleanTag(nbtCompound, "IsRunning", isSprinting);
            return;
        }

        if (isAiming && nbtCompound.getBoolean("IsDrawing").orElse(false) && nbtCompound.getInt("DrawnTick").orElse(0) < 15) {
            ModSyncedDataKeys.AIMING.setValue(player, false);
            isAiming = false;
        }

        this.updateBooleanTag(nbtCompound, "IsAiming", isAiming);
        this.updateBooleanTag(nbtCompound, "IsRunning", isSprinting);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleInitializationAndAnimations(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                                   ItemStack stack, Player player) {

        boolean wasDrawn = nbtCompound.getBoolean("IsDrawn").orElse(false);
        boolean isFirstTick = !wasDrawn && !nbtCompound.getBoolean("_InitializedThisSession").orElse(false);
        if (isFirstTick) {
            handleFirstTickInitialization(nbtCompound, animationController, stack, player);
        }
        handleAnimationStateFixes(nbtCompound, animationController, stack, player);
        handleMainAnimationLogic(nbtCompound, animationController, stack, player);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleFirstTickInitialization(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                               ItemStack stack, Player player) {
        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

        nbtCompound.putBoolean("_InitializedThisSession", true);
        nbtCompound.putBoolean("IsDrawn", true);
        nbtCompound.putInt("DrawnTick", 0);
        this.drawTick = 0;

        if (animationController != null) {
            boolean hasNoAnimation = animationController.getCurrentAnimationPoint() == null;
            boolean isStopped = !animationController.isAnimatingBones();
            boolean isCurrentlyReloading = nbtCompound.getBoolean("scguns:IsReloading").orElse(false) ||
                    ModSyncedDataKeys.RELOADING.getValue(player);

            if ((hasNoAnimation || isStopped) &&
                    !isCurrentlyReloading &&
                    nbtCompound.getInt("DrawnTick").orElse(0) >= 15) {

                updateCarbineState(stack, animationController);
                if (isInCarbineMode(stack)) {
                    tryTriggerAnimationCompat(animationController, "carbine_idle");
                } else {
                    tryTriggerAnimationCompat(animationController, "idle");
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleAnimationStateFixes(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                           ItemStack stack, Player player) {
        if (animationController != null) {
            boolean hasNoAnimation = animationController.getCurrentAnimationPoint() == null;
            boolean isStopped = !animationController.isAnimatingBones();
            boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);

            if ((hasNoAnimation || isStopped) &&
                    !nbtCompound.getBoolean("scguns:IsReloading").orElse(false) &&
                    nbtCompound.getInt("DrawnTick").orElse(0) >= 15) {
                updateCarbineState(stack, animationController);
                if (isInCarbineMode(stack)) {
                    tryTriggerAnimationCompat(animationController, "carbine_idle");
                } else {
                    tryTriggerAnimationCompat(animationController, "idle");
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleMainAnimationLogic(CompoundTag nbtCompound, AnimationController<GeoAnimatable> animationController,
                                          ItemStack stack, Player player) {
        if (nbtCompound.getBoolean("IsDrawing").orElse(false) && nbtCompound.getInt("DrawnTick").orElse(0) < 15
                && Config.COMMON.gameplay.drawAnimation.get()) {
            this.handleDrawingState(nbtCompound, animationController, stack);
            return;
        }

        if (nbtCompound.getInt("DrawnTick").orElse(0) >= 15) {
            assert animationController != null;

            String currentAnim = animationController.getCurrentAnimationPoint() != null ?
                    animationController.getCurrentAnimationPoint().animation().name() : "none";

            boolean isReloading = nbtCompound.getBoolean("scguns:IsReloading").orElse(false) ||
                    ModSyncedDataKeys.RELOADING.getValue(player);
            boolean inCriticalPhase = nbtCompound.getBoolean("InCriticalReloadPhase").orElse(false);

            if (isReloading) {
                this.handleReloadingState(nbtCompound, animationController, stack);
                return;
            }

            if (inCriticalPhase) {
                return;
            }

            if (!isPlayingCriticalAnimations(animationController)) {
                if (nbtCompound.contains(RELOAD_STATE) &&
                        nbtCompound.getString(RELOAD_STATE).orElse("").equals(ReloadState.STOPPING.name())) {
                    this.handleReloadingState(nbtCompound, animationController, stack);
                } else if (nbtCompound.getBoolean("IsAiming").orElse(false)) {
                    this.handleAimingState(nbtCompound, animationController);
                } else if (nbtCompound.getBoolean("IsRunning").orElse(false) &&
                        isPlayingInspectOrReloadAnimations(animationController)) {
                    this.handleRunningState(animationController);
                } else if (isPlayingInspectOrReloadAnimations(animationController)) {
                    if (isInCarbineMode(stack)) {
                        tryTriggerAnimationCompat(animationController, "carbine_idle");
                    } else {
                        tryTriggerAnimationCompat(animationController, "idle");
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isPlayingCriticalAnimations(AnimationController<GeoAnimatable> animationController) {
        return isAnimationPlaying(animationController, "draw") ||
                isAnimationPlaying(animationController, "carbine_draw") ||
                isAnimationPlaying(animationController, "jam") ||
                isAnimationPlaying(animationController, "melee") ||
                isAnimationPlaying(animationController, "bayonet") ||
                isAnimationPlaying(animationController, "shoot") ||
                isAnimationPlaying(animationController, "shoot1") ||
                isAnimationPlaying(animationController, "carbine_shoot") ||
                isAnimationPlaying(animationController, "aim_shoot") ||
                isAnimationPlaying(animationController, "aim_shoot1") ||
                isAnimationPlaying(animationController, "carbine_aim_shoot") ||
                isAnimationPlaying(animationController, "inspect") ||
                isAnimationPlaying(animationController, "carbine_inspect") ||
                isAnimationPlaying(animationController, "reload_stop") ||
                isAnimationPlaying(animationController, "carbine_reload_stop");
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isPlayingInspectOrReloadAnimations(AnimationController<GeoAnimatable> animationController) {
        return !isAnimationPlaying(animationController, "inspect") &&
                !isAnimationPlaying(animationController, "carbine_inspect") &&
                !isAnimationPlaying(animationController, "reload") &&
                !isAnimationPlaying(animationController, "carbine_reload") &&
                !isAnimationPlaying(animationController, "reload_alt") &&
                !isAnimationPlaying(animationController, "carbine_reload_loop") &&
                !isAnimationPlaying(animationController, "reload_loop");
    }

    @OnlyIn(Dist.CLIENT)
    private void finishReloadTransition(CompoundTag nbt) {
        nbt.remove("scguns:IsReloading");
        nbt.remove("scguns:IsPlayingReloadStop");
        nbt.remove("scguns:ReloadComplete");

        String currentState = nbt.getStringOr(RELOAD_STATE, "");
        if (currentState.equals("STOPPING")) {
            cleanupReloadState(nbt);
        }
    }
    private void cleanupAllReloadTags(CompoundTag nbt) {
        cleanupReloadState(nbt);
        nbt.remove("IsMagReload");
        nbt.remove("IsManualReload");
        nbt.remove("shouldStopOnLoopEnd");
        nbt.remove("shouldTransitionToStop");
    }
    /**
     * GeckoLib 4's tryTriggerAnimation did not restart an already-running
     * triggered animation. GeckoLib 5's triggerAnimation resets the animation
     * point/time on every call, so the direct 1:1 rename used by the initial
     * port can briefly snap animated arm bones back to the first keyframe.
     * Preserve the old behavior by only triggering when the same trigger is
     * not already active.
     */
    @OnlyIn(Dist.CLIENT)
    private boolean tryTriggerAnimationCompat(AnimationController<GeoAnimatable> animationController, String animationName) {
        /*
         * Most Scorched Guns idle clips are zero-duration loop animations: they
         * contain only static arm/gun keyframes at t=0 and omit animation_length.
         * GeckoLib 4 tolerated repeatedly requesting those through
         * tryTriggerAnimation(), but GeckoLib 5's triggerAnimation() explicitly
         * clears animationPoint before rebuilding the timeline. During ADS,
         * handleAimingState() requests idle every client tick, so a zero-duration
         * loop can periodically fall through the trigger guards and expose the
         * model's bind-pose arm bones for one render frame.
         *
         * Idle is a steady base pose, not a one-shot event. Use setAnimation() for
         * the two looping idle tracks. GeckoLib 5 makes setAnimation() a no-op when
         * the requested RawAnimation is already current, which keeps the arm bone
         * snapshots continuous while ADS is held. Keep triggerAnimation() only for
         * actual one-shot/triggered actions such as shoot, reload, draw and inspect.
         */
        if ("idle".equals(animationName)) {
            animationController.setAnimation(GunAnimations.IDLE);
            return true;
        }
        if ("carbine_idle".equals(animationName)) {
            animationController.setAnimation(GunAnimations.CARBINE_IDLE);
            return true;
        }

        /*
         * GeckoLib 5 reports isPlayingTriggeredAnimation() == false during the
         * short interval after triggerAnimation() clears animationPoint and before
         * the new timeline has begun animating bones. Calling triggerAnimation()
         * again in that interval clears animationPoint again and exposes the raw
         * arm-bone pose for a frame. GeckoLib 4's tryTriggerAnimation() did not
         * restart an already-selected trigger while the controller was running.
         *
         * Treat the same trigger as active while it is playing, transitioning, or
         * simply not finished yet. Once a one-shot has genuinely finished, a later
         * request is still allowed to trigger it again.
         */
        if (animationController.isTriggeredAnimation(animationName)
                && (animationController.isPlayingTriggeredAnimation()
                    || animationController.isTransitioning()
                    || !animationController.hasAnimationFinished())) {
            return true;
        }
        return animationController.triggerAnimation(animationName);
    }

    public boolean isAnimationPlaying(AnimationController<GeoAnimatable> animationController, String animationName) {
        return animationController.getCurrentAnimationPoint() != null &&
                animationController.getCurrentAnimationPoint().animation().name().equals(animationName);
    }

    private void updateBooleanTag(CompoundTag nbt, String key, boolean value) {
        if (value) {
            nbt.putBoolean(key, true);
        } else {
            nbt.remove(key);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleDrawingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        double drawSpeedMultiplier = 1.0;

        int quickHandsLevel = GunEnchantmentHelper.getQuickHands(stack);
        if (quickHandsLevel > 0) {
            drawSpeedMultiplier += 0.12 * quickHandsLevel;
        }
        int lightweightLevel = GunEnchantmentHelper.getLightweight(stack);
        if (lightweightLevel > 0) {
            drawSpeedMultiplier += 0.05 * lightweightLevel;
        }
        drawSpeedMultiplier = GunModifierHelper.getModifiedDrawSpeed(stack, drawSpeedMultiplier);

        animationController.setAnimationSpeed(drawSpeedMultiplier);

        if (nbt.getIntOr("DrawnTick", 0) < 15 && !nbt.getBooleanOr("scguns:IsReloading", false)) {
            if (isInCarbineMode(stack)) {
                tryTriggerAnimationCompat(animationController, "carbine_draw");
            } else {
                tryTriggerAnimationCompat(animationController, "draw");
            }
        }
        nbt.remove("IsShooting");
        nbt.remove("IsInspecting");
    }
    @OnlyIn(Dist.CLIENT)
    private void handleNormalReload(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        String currentAnim = animationController.getCurrentAnimationPoint() != null ?
                animationController.getCurrentAnimationPoint().animation().name() : "none";
        boolean animationStopped = !animationController.isAnimatingBones();

        if (!serverReloading) {
            cleanupReloadState(nbt);
            nbt.remove("InCriticalReloadPhase");
            nbt.remove("ReloadAnimationStarted");
            nbt.remove("ReloadAnimationRestarted");
            nbt.remove("ReloadCompleted");
            return;
        }

        double reloadSpeedMultiplier = 1.0;
        AttributeInstance reloadSpeedAttribute = player.getAttribute(SCAttributes.RELOAD_SPEED.asHolder());
        if (reloadSpeedAttribute != null) {
            reloadSpeedMultiplier = reloadSpeedAttribute.getValue();
        }

        int actualReloadTime = (int) Math.ceil(GunEnchantmentHelper.getRealReloadSpeed(stack) / reloadSpeedMultiplier);
        float speedMultiplier = (float) modifiedGun.getReloads().getReloadTimer() / actualReloadTime;

        animationController.setAnimationSpeed(speedMultiplier);

        boolean isCarbine = isInCarbineMode(stack);
        String reloadAnim = isCarbine ? "carbine_reload" : "reload";

        if (nbt.getBooleanOr("ReloadCompleted", false)) {
            return;
        }

        boolean hasStartedReload = nbt.getBooleanOr("ReloadAnimationStarted", false);
        if (!isAnimationPlaying(animationController, reloadAnim) && !hasStartedReload) {
            if (!isAnimationPlaying(animationController, "draw") &&
                    !isAnimationPlaying(animationController, "carbine_draw")) {
                animationController.reset();
                tryTriggerAnimationCompat(animationController, reloadAnim);
                nbt.putBoolean("ReloadAnimationStarted", true);
                return;
            }
        }

        if (!isAnimationPlaying(animationController, reloadAnim) && hasStartedReload &&
                animationStopped &&
                !nbt.getBooleanOr("ReloadAnimationRestarted", false)) {

            animationController.reset();
            tryTriggerAnimationCompat(animationController, reloadAnim);
            nbt.putBoolean("ReloadAnimationRestarted", true);
            return;
        }

        if (!animationController.isAnimatingBones() &&
                hasStartedReload &&
                !isAnimationPlaying(animationController, reloadAnim) &&
                !isAnimationPlaying(animationController, "draw") &&
                !isAnimationPlaying(animationController, "carbine_draw")) {


            if (modifiedGun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
            } else if (modifiedGun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleReloadingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        String currentState = nbt.getStringOr(RELOAD_STATE, "");

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean clientReloading = nbt.getBooleanOr("scguns:IsReloading", false);

        // For non-manual reloads, use simple logic
        if (modifiedGun.getReloads().getReloadType() != ReloadType.MANUAL) {
            handleNormalReload(nbt, animationController, stack);
            return;
        }

        // Manual reload handling with state machine
        if (!serverReloading && clientReloading && !currentState.equals("STOPPING")) {
            nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
            nbt.putBoolean("scguns:IsPlayingReloadStop", true);
            nbt.remove("scguns:IsReloading");
            animationController.reset();
            animationController.setAnimationSpeed(1.0);
            tryTriggerAnimationCompat(animationController, isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastStateChange = nbt.getLongOr("LastReloadStateChange", 0L);
        if (currentTime - lastStateChange < 0) {
            return;
        }

        double reloadSpeedMultiplier = 1.0;
        if (Minecraft.getInstance().player != null) {
            AttributeInstance reloadSpeedAttribute = Minecraft.getInstance().player.getAttribute(SCAttributes.RELOAD_SPEED.asHolder());
            if (reloadSpeedAttribute != null) {
                reloadSpeedMultiplier = reloadSpeedAttribute.getValue();
            }
        }
        int actualReloadTime = (int) Math.ceil(GunEnchantmentHelper.getRealReloadSpeed(stack) / reloadSpeedMultiplier);
        float speedMultiplier = (float) modifiedGun.getReloads().getReloadTimer() / actualReloadTime;

        int currentAmmo = nbt.getIntOr("AmmoCount", 0);
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun);
        boolean ammoFull = currentAmmo >= maxAmmo;
        boolean hasNoAmmo = Gun.findReloadAmmo(player, stack, modifiedGun).stack().isEmpty();

        if (currentState.isEmpty() || currentState.equals("NONE")) {
            currentState = ReloadState.NONE.name();
            nbt.putString(RELOAD_STATE, currentState);
        }

        ReloadState state;
        try {
            state = ReloadState.valueOf(currentState);
        } catch (IllegalArgumentException e) {
            state = ReloadState.NONE;
            nbt.putString(RELOAD_STATE, ReloadState.NONE.name());
        }
        switch (state) {
            case NONE:
                nbt.putString(RELOAD_STATE, ReloadState.STARTING.name());
                nbt.putLong("LastReloadStateChange", currentTime);
                nbt.putBoolean("ManualReloadInitialized", true);
                animationController.setAnimationSpeed(speedMultiplier);
                tryTriggerAnimationCompat(animationController, isInCarbineMode(stack) ? "carbine_reload_start" : "reload_start");
                break;

            case STARTING:
                boolean isStartAnimPlaying = isAnimationPlaying(animationController, "reload_start") ||
                        isAnimationPlaying(animationController, "carbine_reload_start");

                if (!isStartAnimPlaying && !animationController.isAnimatingBones()) {
                    if (ammoFull || hasNoAmmo || !ModSyncedDataKeys.RELOADING.getValue(player)) {
                        nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
                        nbt.putBoolean("scguns:IsPlayingReloadStop", true);
                        animationController.setAnimationSpeed(1.0);
                        tryTriggerAnimationCompat(animationController, isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
                    } else {
                        nbt.putString(RELOAD_STATE, ReloadState.LOADING.name());
                        nbt.putLong("LastReloadStateChange", currentTime);
                        nbt.putBoolean("InReloadLoop", true);
                        animationController.setAnimationSpeed(speedMultiplier);
                        tryTriggerAnimationCompat(animationController, isInCarbineMode(stack) ? "carbine_reload_loop" : "reload_loop");
                    }
                }
                break;

            case LOADING:
                if (ammoFull || hasNoAmmo || !ModSyncedDataKeys.RELOADING.getValue(player)) {
                    nbt.putString(RELOAD_STATE, ReloadState.STOPPING.name());
                    nbt.putBoolean("scguns:IsPlayingReloadStop", true);
                    nbt.remove("InReloadLoop");
                    animationController.reset();
                    animationController.setAnimationSpeed(1.0);
                    tryTriggerAnimationCompat(animationController, isInCarbineMode(stack) ? "carbine_reload_stop" : "reload_stop");
                } else {
                    boolean isLoopPlaying = isAnimationPlaying(animationController, "reload_loop") ||
                            isAnimationPlaying(animationController, "carbine_reload_loop");

                    if (!isLoopPlaying && !animationController.isAnimatingBones()) {
                        animationController.setAnimationSpeed(speedMultiplier);
                        tryTriggerAnimationCompat(animationController, isInCarbineMode(stack) ? "carbine_reload_loop" : "reload_loop");
                    }
                }
                break;

            case STOPPING:
                if (!isAnimationPlaying(animationController, "reload_stop") &&
                        !isAnimationPlaying(animationController, "carbine_reload_stop")) {

                    if (!animationController.isAnimatingBones()) {
                        cleanupReloadState(nbt);
                        nbt.remove("LastReloadStateChange");
                        nbt.remove("ManualReloadInitialized");
                        nbt.remove("InReloadLoop");

                        if (isInCarbineMode(stack)) {
                            tryTriggerAnimationCompat(animationController, "carbine_idle");
                        } else {
                            tryTriggerAnimationCompat(animationController, "idle");
                        }
                    }
                }
                break;
        }
    }
    public void cleanupReloadState(CompoundTag nbt) {
        nbt.remove(RELOAD_STATE);
        nbt.remove(RELOAD_START_TIME);
        nbt.remove("scguns:ReloadComplete");
        nbt.remove("scguns:IsPlayingReloadStop");
        nbt.remove("scguns:IsMagReload");
        nbt.remove("scguns:IsManualReload");
        nbt.remove("loaded");
        nbt.remove("IsReloading");
        nbt.remove("scguns:IsReloading");
        nbt.remove("scguns:PausedDuringReload");
        nbt.remove("LastReloadStateChange");
        nbt.remove("InCriticalReloadPhase");
        nbt.remove("ReloadAnimStartTime");
        nbt.remove("IsMagReload");
        nbt.remove("IsManualReload");
        // CRITICAL FIX: Remove animation tracking flags
        nbt.remove("ReloadAnimationStarted");
        nbt.remove("ReloadAnimationRestarted");
        nbt.remove("ReloadCompleted");
    }

    @OnlyIn(Dist.CLIENT)
    private void handleAimingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController) {
        if (nbt.getBooleanOr("IsDrawing", false) && nbt.getIntOr("DrawnTick", 0) < 15) {
            return;
        }

        animationController.setAnimationSpeed(1.0);
        nbt.remove("IsInspecting");
        assert Minecraft.getInstance().player != null;
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        isInCarbineMode(stack);
        if (stack.getItem() instanceof AnimatedGunItem && isInCarbineMode(stack)) {
            tryTriggerAnimationCompat(animationController, "carbine_idle");
        } else {
            tryTriggerAnimationCompat(animationController, "idle");
        }
    }
    private void handleRunningState(AnimationController<GeoAnimatable> animationController) {
        animationController.setAnimationSpeed(1.0);
        assert Minecraft.getInstance().player != null;
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        boolean isCarbine = stack.getItem() instanceof AnimatedGunItem && isInCarbineMode(stack);

        if (isAnimationPlaying(animationController, isCarbine ? "carbine_inspect" : "inspect")) {
            tryTriggerAnimationCompat(animationController, isCarbine ? "carbine_idle" : "idle");
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void handleInspectState(AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        boolean isCarbine = isInCarbineMode(stack);
        String animToPlay = isCarbine ? "carbine_inspect" : "inspect";

        if (!isAnimationPlaying(animationController, animToPlay)) {
            animationController.setAnimationSpeed(1.0);
            animationController.reset();
            tryTriggerAnimationCompat(animationController, animToPlay);
        }
    }
    private void handleShootState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        boolean isCarbine = isInCarbineMode(stack);

        if (stack.getItem() instanceof AnimatedDualWieldGunItem) {
       } else {
            if (nbt.getBooleanOr("IsAiming", false)) {
                tryTriggerAnimationCompat(animationController, isCarbine ? "carbine_aim_shoot" : "aim_shoot");
            } else {
                tryTriggerAnimationCompat(animationController, isCarbine ? "carbine_shoot" : "shoot");
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void soundListener(KeyFrameEvent<AnimatedGunItem, SoundKeyframeData> gunItemSoundKeyframeEvent) {
        Player player = ClientUtil.getClientPlayer();
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() == this && !getOrCreateStackTag(mainHand).getBoolean("IsDroppedItem").orElse(false);
        boolean isOffHand = offHand.getItem() == this && !getOrCreateStackTag(offHand).getBoolean("IsDroppedItem").orElse(false);

        if (!isMainHand && !isOffHand) {
            return;
        }

        ItemStack heldStack = isMainHand ? mainHand : offHand;
        CompoundTag heldTag = getOrCreateStackTag(heldStack);

        if (!heldTag.getBooleanOr("IsDrawn", false)) {
            return;
        }

        switch (gunItemSoundKeyframeEvent.keyframeData().getSound()) {
            case "gun_rustle":
                player.playSound(ModSounds.GUN_RUSTLE.get(), 1.0F, 1.0F);
                break;
            case "metal":
                player.playSound(ModSounds.METAL.get(), 1.0F, 1.0F);
                break;
            case "jam":
                player.playSound(ModSounds.COPPER_GUN_JAM.get(), 1.0F, 1.0F);
                break;
            case "lever":
                player.playSound(ModSounds.LEVER.get(), 1.0F, 1.0F);
                break;
            case "slap":
                player.playSound(ModSounds.SLAP.get(), 1.0F, 1.0F);
                break;
            case "rack":
                player.playSound(ModSounds.RACK.get(), 1.0F, 1.0F);
                break;
            case "reload_mag_out":
                player.playSound(this.reloadSoundMagOut, 1.0F, 1.0F);
                break;
            case "reload_mag_in":
                player.playSound(this.reloadSoundMagIn, 1.0F, 1.0F);
                break;
            case "reload_end":
                player.playSound(this.reloadSoundEnd, 1.0F, 1.0F);
                break;
            case "bolt_pull":
                player.playSound(this.boltPullSound, 1.0F, 1.0F);
                break;
            case "bolt_release":
                player.playSound(this.boltReleaseSound, 1.0F, 1.0F);
                break;
            case "bolt":
                player.playSound(ModSounds.BOLT.get(), 1.0F, 1.0F);
                break;
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void particleListener(KeyFrameEvent<AnimatedGunItem, ParticleKeyframeData> gunItemParticleKeyframeEvent) {
        Player player = ClientUtil.getClientPlayer();
        if (player == null) return;
        ItemStack currentItem = player.getMainHandItem();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() == this && !getOrCreateStackTag(mainHand).getBoolean("IsDroppedItem").orElse(false);
        boolean isOffHand = offHand.getItem() == this && !getOrCreateStackTag(offHand).getBoolean("IsDroppedItem").orElse(false);

        if (!isMainHand && !isOffHand) {
            return;
        }

        ItemStack heldStack = isMainHand ? mainHand : offHand;
        CompoundTag heldTag = getOrCreateStackTag(heldStack);

        if (!heldTag.getBooleanOr("IsDrawn", false)) {
            return;
        }

        GunItem gunItem = (GunItem) heldStack.getItem();
        Gun gun = gunItem.getModifiedGun(heldStack);
        CompoundTag tag = getOrCreateStackTag(heldStack);

        String effect = gunItemParticleKeyframeEvent.keyframeData().getEffect();

        switch(effect) {
            case "reset_mag_position":
                tag.putFloat("MagazinePosition", 0.0f);
                tag.putBoolean("MagazineOverride", true);
                break;

            case "loaded":
                ModSyncedDataKeys.RELOADING.getValue(player);
                tag.putBoolean("loaded", true);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
                break;

            case "eject_casing":
                if (gun.getProjectile().ejectsCasing() && gun.getProjectile().ejectDuringReload()) {
                    if (Config.COMMON.gameplay.spawnCasings.get()) {
                        Level level = player.level();
                        GunEventBus.ejectCasing(level, player, false);
                        if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild) {
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageEjectCasing());
                        }
                    }
                }
                break;
        }
        setStackTag(heldStack, tag);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<?> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedGunRenderer(Identifier.fromNamespaceAndPath("scguns", gunID));
                }
                return this.renderer;
            }
        });
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }
    @OnlyIn(Dist.CLIENT)

    private PlayState predicate(AnimationTest<AnimatedGunItem> event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return PlayState.STOP;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() == this && !getOrCreateStackTag(mainHand).getBoolean("IsDroppedItem").orElse(false);
        boolean isOffHand = offHand.getItem() == this && !getOrCreateStackTag(offHand).getBoolean("IsDroppedItem").orElse(false);

        if (!isMainHand && !isOffHand) {
            return PlayState.STOP;
        }

        ItemStack heldStack = isMainHand ? mainHand : offHand;
        CompoundTag heldTag = getOrCreateStackTag(heldStack);

        if (!heldTag.getBooleanOr("IsDrawn", false)) {
            return PlayState.STOP;
        }

        return PlayState.CONTINUE;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            registerClientControllers(controllers);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerClientControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<AnimatedGunItem> controller = new AnimationController<>("controller", 0, this::predicate)
                .setSoundKeyframeHandler(this::soundListener)
                .setParticleKeyframeHandler(this::particleListener)
                .triggerableAnim("idle", GunAnimations.IDLE)
                .triggerableAnim("carbine_idle", GunAnimations.CARBINE_IDLE)
                .triggerableAnim("shoot", GunAnimations.SHOOT)
                .triggerableAnim("shoot1", GunAnimations.SHOOT1)
                .triggerableAnim("carbine_shoot", GunAnimations.CARBINE_SHOOT)
                .triggerableAnim("aim_shoot", GunAnimations.AIM_SHOOT)
                .triggerableAnim("aim_shoot1", GunAnimations.AIM_SHOOT1)
                .triggerableAnim("carbine_aim_shoot", GunAnimations.CARBINE_AIM_SHOOT)
                .triggerableAnim("reload", GunAnimations.RELOAD)
                .triggerableAnim("carbine_reload", GunAnimations.CARBINE_RELOAD)
                .triggerableAnim("reload_alt", GunAnimations.RELOAD_ALT)
                .triggerableAnim("reload_start", GunAnimations.RELOAD_START)
                .triggerableAnim("carbine_reload_start", GunAnimations.CARBINE_RELOAD_START)
                .triggerableAnim("reload_loop", GunAnimations.RELOAD_LOOP)
                .triggerableAnim("carbine_reload_loop", GunAnimations.CARBINE_RELOAD_LOOP)
                .triggerableAnim("reload_stop", GunAnimations.RELOAD_STOP)
                .triggerableAnim("carbine_reload_stop", GunAnimations.CARBINE_RELOAD_STOP)
                .triggerableAnim("draw", GunAnimations.DRAW)
                .triggerableAnim("carbine_draw", GunAnimations.CARBINE_DRAW)
                .triggerableAnim("inspect", GunAnimations.INSPECT)
                .triggerableAnim("carbine_inspect", GunAnimations.CARBINE_INSPECT)
                .triggerableAnim("jam", GunAnimations.JAM);
        controllers.add(controller);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
