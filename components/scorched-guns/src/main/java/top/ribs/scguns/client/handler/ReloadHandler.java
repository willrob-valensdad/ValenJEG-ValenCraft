package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mojang.blaze3d.platform.InputConstants;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.event.GunReloadEvent;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public class ReloadHandler {
    private static ReloadHandler instance;

    public static ReloadHandler get() {
        if (instance == null) {
            instance = new ReloadHandler();
        }
        return instance;
    }

    private int startReloadTick;
    private int reloadTimer;
    private int prevReloadTimer;
    private int reloadingSlot;

    private ReloadHandler() {
    }

    @Nullable
    private static CompoundTag getStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : null;
    }

    private static CompoundTag getOrCreateStackTag(ItemStack stack) {
        CompoundTag tag = getStackTag(stack);
        return tag != null ? tag : new CompoundTag();
    }

    private static void setStackTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void prepareAmmoSwapReload(ItemStack stack) {
        CompoundTag tag = getOrCreateStackTag(stack);
        if (tag.getIntOr("AmmoCount", 0) > 0) {
            tag.putInt("AmmoCount", 0);
            setStackTag(stack, tag);
            Gun.clearLoadedProjectileItem(stack);
        }
    }

    public static void loaded(Player player) {
        Item item = player.getMainHandItem().getItem();
        if (item instanceof GunItem gunItem) {
            ItemStack stack = player.getMainHandItem();
            CompoundTag tag = getOrCreateStackTag(stack);
            Gun gun = gunItem.getModifiedGun(stack);
            if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                if (tag.getBooleanOr("scguns:ReloadComplete", false)) {
                    if (!tag.getBooleanOr("scguns:IsPlayingReloadStop", false)) {
                        tag.putBoolean("scguns:IsPlayingReloadStop", true);
                        tag.remove("InCriticalReloadPhase");
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        if (item instanceof AnimatedGunItem animatedGun) {
                            long id = GeoItem.getId(stack);
                            AnimationController<GeoAnimatable> animationController = animatedGun
                                    .getAnimatableInstanceCache()
                                    .getManagerForId(id)
                                    .getAnimationControllers()
                                    .get("controller");

                            if (animationController != null) {
                                if (animatedGun.isInCarbineMode(stack)) {
                                    animationController.triggerAnimation("carbine_reload_stop");
                                } else {
                                    animationController.triggerAnimation("reload_stop");
                                }
                            }
                        }
                    }
                }
            } else {
                if (tag.getBooleanOr("scguns:ReloadComplete", false)) {
                    tag.putBoolean("scguns:IsPlayingReloadStop", true);
                    tag.remove("InCriticalReloadPhase");
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    if (item instanceof AnimatedGunItem) {
                        long id = GeoItem.getId(stack);
                        AnimationController<GeoAnimatable> animationController = ((AnimatedGunItem) item)
                                .getAnimatableInstanceCache()
                                .getManagerForId(id)
                                .getAnimationControllers()
                                .get("controller");

                        if (animationController != null) {
                            animationController.triggerAnimation("reload_stop");
                        }
                    }
                }
            }
            setStackTag(stack, tag);
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.prevReloadTimer = this.reloadTimer;
            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                ItemStack stack = player.getMainHandItem();
                if (Minecraft.getInstance().isPaused() && stack.getItem() instanceof GunItem) {
                    Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                    if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                        setReloading(false);
                        return;
                    }
                }
            }
            if (ModSyncedDataKeys.RELOADING.getValue(player) && Minecraft.getInstance().isPaused()) {
                setReloading(false);
            }
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageLeftOverAmmo());

            if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().getSelectedSlot()) {
                    setReloading(false);
                }
            }

            this.updateReloadTimer(player);
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem && !(stack.getItem() instanceof AnimatedGunItem)) {
                Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                if (gun.getReloads().getReloadType() == ReloadType.MAG_FED &&
                        this.reloadTimer <= 0 &&
                        ModSyncedDataKeys.RELOADING.getValue(player)) {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
                    setReloading(false);
                }
            }

            HUDRenderHandler.updateReserveAmmo(player);
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundTag tag = getOrCreateStackTag(stack);
            Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            tag.getBooleanOr("InCriticalReloadPhase", false);

            if (KeyBinds.KEY_SWAP_AMMO.consumeClick() && event.getAction() == InputConstants.PRESS) {
                if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                    return;
                }
                if (!gun.getGeneral().allowsAmmoChange() || gun.getAcceptedProjectiles().size() <= 1) {
                    Item current = Gun.getSelectedProjectileItem(stack, gun);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageSwapAmmo(BuiltInRegistries.ITEM.getKey(current)));
                    return;
                }
                Item current = Gun.getSelectedProjectileItem(stack, gun);
                Item selected = Gun.cycleSelectedAvailableProjectileItem(player, stack, gun);
                if (selected == null) {
                    player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.no_available").withStyle(ChatFormatting.RED));
                    return;
                }
                if (selected == current) {
                    return;
                }
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageSwapAmmo(BuiltInRegistries.ITEM.getKey(selected)));
                prepareAmmoSwapReload(stack);
                setReloading(true);
                HUDRenderHandler.stageReserveAmmoUpdate();
                return;
            }

            if (KeyBinds.KEY_RELOAD.isDown() && event.getAction() == InputConstants.PRESS) {
                boolean currentlyReloading = ModSyncedDataKeys.RELOADING.getValue(player);
                if (currentlyReloading) {
                    return;
                }
                setReloading(true);
                HUDRenderHandler.updateReserveAmmo(player);
            }

            if (KeyBinds.KEY_UNLOAD.consumeClick() && event.getAction() == InputConstants.PRESS) {
                setReloading(false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
                HUDRenderHandler.stageReserveAmmoUpdate();
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseButton.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundTag tag = getOrCreateStackTag(stack);
            Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            boolean inCriticalPhase = tag.getBooleanOr("InCriticalReloadPhase", false);
            boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

            if (!isReloading && inCriticalPhase) {
                tag.remove("InCriticalReloadPhase");
                setStackTag(stack, tag);
                inCriticalPhase = false;
            }

            if (KeyBinds.getAimMapping().isDown() && event.getAction() == InputConstants.PRESS &&
                    inCriticalPhase && gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                return;
            }
            KeyBinds.getAimMapping().isDown();
        }
    }

    public void setReloading(boolean reloading) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        CompoundTag tag = getOrCreateStackTag(stack);

        boolean inCriticalPhase = tag.getBooleanOr("InCriticalReloadPhase", false);

        if (reloading) {
            if (stack.getItem() instanceof GunItem gunItem) {
                if (inCriticalPhase) {
                    return;
                }

                if (!tag.contains("IgnoreAmmo")) {
                    if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                        return;
                    }

                    if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                        Gun gun = gunItem.getModifiedGun(stack);

                        // Force stop aiming for any reload
                        if (ModSyncedDataKeys.AIMING.getValue(player)) {
                            ModSyncedDataKeys.AIMING.setValue(player, false);
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
                        }

                        if (!tag.getBooleanOr("IsDrawn", false)) {
                            tag.putBoolean("IsDrawn", true);
                            tag.putInt("DrawnTick", 15);
                            setStackTag(stack, tag);
                        }

                        boolean hasReloadTags = tag.getBooleanOr("IsReloading", false) ||
                                tag.getBooleanOr("scguns:IsReloading", false) ||
                                tag.contains("scguns:ReloadState");

                        if (hasReloadTags && !ModSyncedDataKeys.RELOADING.getValue(player)) {
                            animatedGun.cleanupReloadState(tag);
                            setStackTag(stack, tag);
                        }

                        if (tag.getBooleanOr("IsReloading", false) && ModSyncedDataKeys.RELOADING.getValue(player)) {
                            return;
                        }
                    }

                    Gun gun = gunItem.getModifiedGun(stack);

                    if (tag.getIntOr("AmmoCount", 0) >= GunModifierHelper.getModifiedAmmoCapacity(stack, gun)) {
                        return;
                    }
                    if (Gun.findReloadAmmo(player, stack, gun).stack().isEmpty()) {
                        return;
                    }

                    Identifier preReloadSound = gun.getSounds().getPreReload();
                    if (preReloadSound != null) {
                        Config.SERVER.reloadMaxDistance.get();
                        player.playSound(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.getValue(preReloadSound)),
                                0.7F, 1.0F);
                    }

                    if (NeoForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack)).isCanceled()) {
                        return;
                    }

                    ModSyncedDataKeys.RELOADING.setValue(player, true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true));
                    this.reloadingSlot = player.getInventory().getSelectedSlot();

                    if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                        tag.putBoolean("IsReloading", true);
                        tag.putBoolean("scguns:IsReloading", true);
                        tag.remove("ReloadComplete");
                        tag.remove("scguns:ReloadComplete");
                        setStackTag(stack, tag);

                        if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                            tag.putBoolean("InCriticalReloadPhase", true);
                            setStackTag(stack, tag);
                        }

                        long id = GeoItem.getId(stack);
                        AnimationController<GeoAnimatable> controller = animatedGun
                                .getAnimatableInstanceCache()
                                .getManagerForId(id)
                                .getAnimationControllers()
                                .get("controller");

                        if (controller != null) {
                            ReloadType reloadType = gun.getReloads().getReloadType();
                            if (reloadType == ReloadType.MAG_FED || reloadType == ReloadType.SINGLE_ITEM) {
                                tag.putBoolean("IsMagReload", true);
                                setStackTag(stack, tag);
                                if (animatedGun.isInCarbineMode(stack)) {
                                    controller.triggerAnimation("carbine_reload");
                                } else {
                                    controller.triggerAnimation("reload");
                                }
                            }
                            if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                                tag.putBoolean("IsManualReload", true);
                                tag.putString("scguns:ReloadState", "NONE");
                                setStackTag(stack, tag);
                                if (animatedGun.isInCarbineMode(stack)) {
                                    controller.triggerAnimation("carbine_reload_start");
                                } else {
                                    controller.triggerAnimation("reload_start");
                                }
                            }
                        }
                    }

                    NeoForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                }
            }
        } else {
            if (inCriticalPhase) {
                Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                if (gun.getReloads().getReloadType() != ReloadType.MANUAL) {
                    return;
                }
            }

            if (stack.getItem() instanceof AnimatedGunItem animatedGun) {
                Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);

                if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    if (tag.getBooleanOr("scguns:IsReloading", false) && !tag.getBooleanOr("scguns:IsPlayingReloadStop", false)) {
                        tag.putString("scguns:ReloadState", "STOPPING");
                        tag.putBoolean("scguns:IsPlayingReloadStop", true);
                        setStackTag(stack, tag);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    } else {
                        animatedGun.cleanupReloadState(tag);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("InCriticalReloadPhase");
                        setStackTag(stack, tag);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    }
                } else {
                    if (!inCriticalPhase) {
                        animatedGun.cleanupReloadState(tag);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("InCriticalReloadPhase");
                        setStackTag(stack, tag);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    }
                }
            } else {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                tag.remove("InCriticalReloadPhase");
                setStackTag(stack, tag);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;
            }
        }

        if (stack.getItem() instanceof GunItem) {
            HUDRenderHandler.updateReserveAmmo(player);
        }
    }

    private void updateReloadTimer(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            if (stack.getItem() instanceof AnimatedGunItem) {
                return;
            }

            if (this.startReloadTick == -1) {
                this.startReloadTick = player.tickCount + 5;
            }
            if (this.reloadTimer < 5) {
                this.reloadTimer++;
            }
        } else {
            if (this.startReloadTick != -1) {
                this.startReloadTick = -1;
            }
            if (this.reloadTimer > 0) {
                this.reloadTimer--;
            }
        }
    }

    public int getStartReloadTick() {
        return this.startReloadTick;
    }

    public int getReloadTimer() {
        return this.reloadTimer;
    }

    public float getReloadProgress(float partialTicks) {
        return (this.prevReloadTimer + (this.reloadTimer - this.prevReloadTimer) * partialTicks) / 5F;
    }
}
