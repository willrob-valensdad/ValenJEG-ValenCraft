package top.ribs.scguns.client.handler;

import top.ribs.scguns.compat.transfer.LegacyEnergyTransferBridge;


import top.ribs.scguns.util.ScgunsColorCompat;
import top.ribs.scguns.util.ScgunsValueIOCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.util.GunModifierHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitPowerManager;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.world.entity.EquipmentSlot;
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class HUDRenderHandler {
    private static final Identifier CHARGE_BAR = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/charging_bar.png");
    private static final Identifier FILL_BAR = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/fill_bar.png");
    private static final Identifier MELEE_ATTACK_INDICATOR_PROGRESS = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/melee_attack_indicator_progress.png");
    private static final Identifier MELEE_ATTACK_INDICATOR_BACKGROUND = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/melee_attack_indicator_background.png");
    public static final Identifier HOT_BARREL_TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/hot_barrel.png");

    static int meleeCooldown = 0;
    static int maxMeleeCooldown = 0;
    static boolean isMeleeCooldownActive = false;

    private static boolean playingHitMarker = false;
    private static int hitMarkerTime;
    private static int prevHitMarkerTime;
    private static final int hitMarkerMaxTime = 2;
    private static boolean hitMarkerCrit = false;

    private static int reserveAmmo = 0;
    private static int ammoAutoUpdateTimer = 0;
    private static final int ammoAutoUpdateRate = 20;

    private static ItemStack cachedGunStack = ItemStack.EMPTY;
    private static Gun cachedGun = null;
    private static CompoundTag cachedTag = null;
    private static boolean cachedHasCreativeBox = false;
    private static int cacheValidityTimer = 0;
    private static final int CACHE_DURATION = 10;

    private static int cachedScreenWidth = -1;
    private static int cachedScreenHeight = -1;
    private static int screenCacheTimer = 0;
    private static final int SCREEN_CACHE_DURATION = 60;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null) {
            return;
        }

        updateScreenCache(mc);

        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();
        renderExoSuitStatusHUD(guiGraphics, player);

        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof GunItem)) {
            return;
        }

        updateCache(heldItem, player);

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        renderGunInfoHUD(heldItem, guiGraphics, player);
        renderChargeBarHUD(heldItem, partialTick, guiGraphics, player);
        renderMeleeCooldownHUD(partialTick, guiGraphics);
        renderHitMarker(partialTick, guiGraphics);
        renderHotBarrelOverlay(heldItem, guiGraphics);
    }

    private static void updateCache(ItemStack heldItem, Player player) {
        if (cacheValidityTimer <= 0 || !ItemStack.matches(cachedGunStack, heldItem)) {
            cachedGunStack = heldItem.copy();
            cachedGun = ((GunItem) heldItem.getItem()).getGun();
            cachedTag = getCustomData(heldItem);

            updateCreativeBoxCache(player);
            cacheValidityTimer = CACHE_DURATION;
        } else {
            cacheValidityTimer--;
        }
    }

    private static void updateCreativeBoxCache(Player player) {
        cachedHasCreativeBox = player.getInventory().getNonEquipmentItems().stream()
                .anyMatch(itemStack -> itemStack.getItem() instanceof CreativeAmmoBoxItem);

        if (!cachedHasCreativeBox) {
            AtomicBoolean hasBox = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                IItemHandlerModifiable curios = handler.getEquippedCurios();
                for (int i = 0; i < curios.getSlots() && !hasBox.get(); i++) {
                    ItemStack stack = curios.getStackInSlot(i);
                    if (stack.getItem() instanceof CreativeAmmoBoxItem) {
                        hasBox.set(true);
                    }
                }
            });
            cachedHasCreativeBox = hasBox.get();
        }

        if (!cachedHasCreativeBox) {
            cachedHasCreativeBox = hasCreativeAmmoBoxInExoSuit(player);
        }
    }

    private static boolean hasCreativeAmmoBoxInExoSuit(Player player) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return false;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return false;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return false;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CreativeAmmoBoxItem) {
                return true;
            }
        }

        return false;
    }

    private static ItemStack getEquippedChestplate(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == top.ribs.scguns.compat.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findPouchUpgrade(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static String getPouchId(ItemStack pouchUpgrade) {
        return pouchUpgrade.getItem().toString();
    }

    private static ItemStackHandler getPouchInventory(ItemStack chestplate, String pouchId, int size) {
        CompoundTag stackTag = getCustomData(chestplate);
        CompoundTag pouchData = stackTag != null ? stackTag.getCompoundOrEmpty("PouchData") : new CompoundTag();

        ItemStackHandler handler = new ItemStackHandler(size);
        if (pouchData.contains(pouchId)) {
            ScgunsValueIOCompat.read(handler, registryAccess(), pouchData.getCompoundOrEmpty(pouchId));
        }

        return handler;
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : null;
    }

    private static HolderLookup.Provider registryAccess() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null ? minecraft.level.registryAccess() : Gun.builtInRegistryProvider();
    }

    private static void updateScreenCache(Minecraft mc) {
        if (screenCacheTimer <= 0) {
            cachedScreenWidth = mc.getWindow().getGuiScaledWidth();
            cachedScreenHeight = mc.getWindow().getGuiScaledHeight();
            screenCacheTimer = SCREEN_CACHE_DURATION;
        } else {
            screenCacheTimer--;
        }
    }

    public static void renderHotBarrelOverlay(ItemStack heldItem, GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.asHolder(), heldItem) <= 0) {
            return;
        }

        float chargeRatio = HotBarrelCache.getSmoothHotBarrelPercentage(player, heldItem);
        if (chargeRatio <= 0.0f) {
            return;
        }

        int barWidth = 128;
        int barHeight = 8;
        float scale = 1.25f;
        int barX = (int) (((float) cachedScreenWidth / 2) - ((barWidth * scale) / 2));
        int barY = (int) (cachedScreenHeight - 45 * scale);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        int scaledBarX = (int) (barX / scale);
        int scaledBarY = (int) (barY / scale);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, HOT_BARREL_TEXTURE, scaledBarX, scaledBarY, 0, 0, barWidth, 5, barWidth, barHeight);

        int fillWidth = (int) (barWidth * chargeRatio);
        if (fillWidth > 0) {
            int fillY = scaledBarY + 1;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, HOT_BARREL_TEXTURE, scaledBarX, fillY, 0, 5, fillWidth, 3, barWidth, barHeight);
        }

        guiGraphics.pose().popMatrix();
    }

    private static void renderMeleeCooldownHUD(float partialTick, GuiGraphicsExtractor guiGraphics) {
        if (!isMeleeCooldownActive) {
            return;
        }

        int barWidth = 16;
        int barHeight = 16;
        int barX = (int) (cachedScreenWidth * 0.87);
        int barY = (int) (cachedScreenHeight * 0.73);

        float cooldownRatio = (meleeCooldown > 0) ? (float) meleeCooldown / maxMeleeCooldown : 0;


        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MELEE_ATTACK_INDICATOR_BACKGROUND, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (cooldownRatio > 0) {
            int fillHeight = (int) (barHeight * cooldownRatio);
            int fillY = barY + (barHeight - fillHeight);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MELEE_ATTACK_INDICATOR_PROGRESS, barX, fillY, 0, barHeight - fillHeight, barWidth, fillHeight, barWidth, barHeight);
        }

    }

    private static void renderGunInfoHUD(ItemStack heldItem, GuiGraphicsExtractor guiGraphics, Player player) {
        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return;
        }

        if (cachedTag == null) {
            return;
        }
        if (Config.CLIENT.display.immersiveGunInfo.get()) {
            if (heldItem.getItem() instanceof AnimatedGunItem animatedGun) {
                long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> controller = animatedGun.getAnimatableInstanceCache()
                        .getManagerForId(id)
                        .getAnimationControllers()
                        .get("controller");

                if (controller == null ||
                        (!animatedGun.isAnimationPlaying(controller, "inspect") &&
                                !animatedGun.isAnimationPlaying(controller, "carbine_inspect"))) {
                    return;
                }
            }
        }

        int currentAmmo = cachedTag.getIntOr("AmmoCount", 0);
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(heldItem, cachedGun);

        boolean isCreative = player.isCreative();
        MutableComponent ammoCountValue;
        MutableComponent reserveAmmoValue;

        if (isCreative) {
            ammoCountValue = Component.literal("∞ / ∞").withStyle(ChatFormatting.BOLD);
            reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
        } else {
            ammoCountValue = Component.literal(currentAmmo + " / " + maxAmmo).withStyle(ChatFormatting.BOLD);
            if (cachedHasCreativeBox) {
                reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
            } else {
                reserveAmmoValue = Component.literal(String.valueOf(reserveAmmo)).withStyle(ChatFormatting.BOLD);
            }
        }

        int ammoPosX = (int) (cachedScreenWidth * 0.88);
        int ammoPosY = (int) (cachedScreenHeight * 0.8);


        Minecraft mc = Minecraft.getInstance();

        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            if (player.isAlive()) {
                guiGraphics.text(mc.font, "Reloading...", ammoPosX, ammoPosY - 10, 0xFFFFFF55);
            }
        }

        guiGraphics.text(mc.font, ammoCountValue, ammoPosX, ammoPosY, (currentAmmo > 0 || isCreative ? 0xFFFFFFFF : 0xFFFF5555));
        int reserveAmmoPosY = ammoPosY + 10;
        guiGraphics.text(mc.font, reserveAmmoValue, ammoPosX, reserveAmmoPosY, (reserveAmmo <= 0 && !Gun.hasUnlimitedReloads(heldItem) ? 0xFF555555 : 0xFFAAAAAA));

        ItemStack ammoItemStack = new ItemStack(Objects.requireNonNull(Gun.getDisplayProjectile(heldItem, cachedGun).getItem()));
        renderAmmoTypeTexture(ammoItemStack, ammoPosX - 20, ammoPosY, guiGraphics);

    }

    private static void renderAmmoTypeTexture(ItemStack ammoItemStack, int x, int y, GuiGraphicsExtractor guiGraphics) {
        guiGraphics.item(ammoItemStack, x, y);
    }

    private static void renderChargeBarHUD(ItemStack heldItem, float partialTick, GuiGraphicsExtractor guiGraphics, LocalPlayer player) {
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        if (gun.getGeneral().getFireMode() != FireMode.PULSE) {
            return;
        }

        int maxChargeTime = gun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime(player.getUUID());
        if (chargeTime <= 0) {
            return;
        }

        float chargeRatio = (float) chargeTime / maxChargeTime;

        int barWidth = 16;
        int barHeight = 16;
        int barX = cachedScreenWidth / 2 - 15;
        int barY = cachedScreenHeight / 2 - barHeight / 2;


        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CHARGE_BAR, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (chargeRatio > 0) {
            int fillBarWidth = 16;
            int fillBarHeight = 16;
            int fillHeight = (int) (fillBarHeight * chargeRatio);
            int fillY = barY + (fillBarHeight - fillHeight);

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILL_BAR, barX, fillY, 0, fillBarHeight - fillHeight, fillBarWidth, fillHeight, fillBarWidth, fillBarHeight);
        }

    }

    private static void renderHitMarker(float partialTick, GuiGraphicsExtractor guiGraphics) {
        if (!playingHitMarker) {
            return;
        }

        float progress = (prevHitMarkerTime + (hitMarkerTime - prevHitMarkerTime) * partialTick) / (float) hitMarkerMaxTime;
        if (progress >= 1.0F) {
            playingHitMarker = false;
            hitMarkerTime = 0;
        }
    }

    public static void playHitMarker(boolean crit) {
        playingHitMarker = true;
        hitMarkerCrit = crit;
        hitMarkerTime = 1;
        prevHitMarkerTime = 0;
    }

    private static void fetchReserveAmmo(Player player, Gun gun) {
        ItemStack stack = player.getMainHandItem();
        reserveAmmo = Gun.getReserveAmmoCount(player, stack, gun);
        ammoAutoUpdateTimer = 0;
    }

    public static void stageReserveAmmoUpdate() {
        ammoAutoUpdateTimer = ammoAutoUpdateRate;
    }

    public static void updateReserveAmmo(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            fetchReserveAmmo(player, modifiedGun);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        updateHitMarker();

        if (++ammoAutoUpdateTimer >= ammoAutoUpdateRate) {
            ammoAutoUpdateTimer = 0;
        }

        if (isMeleeCooldownActive) {
            meleeCooldown--;
            if (meleeCooldown <= 0) {
                isMeleeCooldownActive = false;
                meleeCooldown = 0;
            }
        }
    }

    private static void updateHitMarker() {
        prevHitMarkerTime = hitMarkerTime;

        if (playingHitMarker) {
            hitMarkerTime++;
            if (hitMarkerTime > hitMarkerMaxTime) {
                playingHitMarker = false;
                hitMarkerTime = 0;
            }
        } else {
            hitMarkerTime = 0;
        }
    }

    public static boolean isRenderingHitMarker() {
        return playingHitMarker;
    }

    public static float getHitMarkerProgress(float partialTicks) {
        return (prevHitMarkerTime + (hitMarkerTime - prevHitMarkerTime) * partialTicks) / (float) hitMarkerMaxTime;
    }

    public static boolean getHitMarkerCrit() {
        return hitMarkerCrit;
    }
    private static void renderExoSuitStatusHUD(GuiGraphicsExtractor guiGraphics, Player player) {
        if (!hasAnyExoSuitPiece(player)) {
            return;
        }

        int hudX = 3;
        int hudY = cachedScreenHeight - 60;
        int lineHeight = 10;
        int currentY = hudY;

        Minecraft mc = Minecraft.getInstance();

        // Power Core Display
        ItemStack chestplate = getEquippedExoSuitChestplate(player);
        if (!chestplate.isEmpty()) {
            ItemStack powerCore = findPowerCoreInChestplate(chestplate);
            if (!powerCore.isEmpty()) {
                IEnergyStorage energyStorage = LegacyEnergyTransferBridge.legacy(powerCore);
                int energyStored = energyStorage != null ? energyStorage.getEnergyStored() : 0;
                int maxEnergy = energyStorage != null ? energyStorage.getMaxEnergyStored() : 0;

                if (maxEnergy > 0) {
                    int energyPercent = (energyStored * 100) / maxEnergy;
                    ChatFormatting energyColor = getEnergyColorForHUD(energyPercent);
                    Component powerText = Component.translatable("tooltip.scguns.exosuit.energy_level", energyPercent)
                            .withStyle(energyColor);
                    guiGraphics.text(mc.font, powerText, hudX, currentY,
                            ARGB.opaque(ScgunsColorCompat.legacyColor(energyColor, 0xFFFFFF)));
                    currentY += lineHeight;
                }
            } else {
                Component noPowerText = Component.translatable("hud.scguns.exosuit.no_power_core")
                        .withStyle(ChatFormatting.RED);
                guiGraphics.text(mc.font, noPowerText, hudX, currentY, 0xFFFF5555);
                currentY += lineHeight;
            }
        }

        ItemStack helmet = getEquippedExoSuitHelmet(player);
        if (!helmet.isEmpty() && hasHudModule(helmet)) {
            boolean hudEnabled = ExoSuitPowerManager.isPowerEnabled(player, "hud");
            boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "hud");

            String hudModuleKey = getHudModuleTranslationKey(helmet);
            String statusText = getModuleStatusText(hudEnabled, canFunction);
            ChatFormatting statusColor = getModuleStatusColor(hudEnabled, canFunction);

            Component helmetText = Component.translatable(hudModuleKey)
                    .append(": " + statusText)
                    .withStyle(statusColor);
            guiGraphics.text(mc.font, helmetText, hudX, currentY,
                    ARGB.opaque(ScgunsColorCompat.legacyColor(statusColor, 0xFFFFFF)));
            currentY += lineHeight;
        }

        if (!chestplate.isEmpty() && hasUtilityModule(chestplate)) {
            boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");
            boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");

            String utilityModuleKey = getUtilityModuleTranslationKey(chestplate);
            String statusText = getModuleStatusText(utilityEnabled, canFunction);
            ChatFormatting statusColor = getModuleStatusColor(utilityEnabled, canFunction);

            Component chestText = Component.translatable(utilityModuleKey)
                    .append(": " + statusText)
                    .withStyle(statusColor);
            guiGraphics.text(mc.font, chestText, hudX, currentY,
                    ARGB.opaque(ScgunsColorCompat.legacyColor(statusColor, 0xFFFFFF)));
            currentY += lineHeight;
        }

        // Boots Mobility Module Display
        ItemStack boots = getEquippedExoSuitBoots(player);
        if (!boots.isEmpty() && hasMobilityModule(boots)) {
            boolean mobilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "mobility");
            boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "mobility");

            String mobilityModuleKey = getMobilityModuleTranslationKey(boots);
            String statusText = getModuleStatusText(mobilityEnabled, canFunction);
            ChatFormatting statusColor = getModuleStatusColor(mobilityEnabled, canFunction);

            Component bootsText = Component.translatable(mobilityModuleKey)
                    .append(": " + statusText)
                    .withStyle(statusColor);
            guiGraphics.text(mc.font, bootsText, hudX, currentY,
                    ARGB.opaque(ScgunsColorCompat.legacyColor(statusColor, 0xFFFFFF)));
        }
    }
    private static boolean hasUtilityModule(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getUtilityModuleTranslationKey(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility")) {
                    if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.JetpackModuleItem) {
                        return "exosuit.upgrade.jetpack";
                    }
                    else if (upgrade.getEffects().hasFlight()) {
                        return "exosuit.upgrade.jetpack";
                    }
                    else {
                        return "exosuit.upgrade.utility";
                    }
                }
            }
        }
        return "exosuit.upgrade.utility";
    }
    private static boolean hasAnyExoSuitPiece(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof ExoSuitItem) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack getEquippedExoSuitHelmet(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == top.ribs.scguns.compat.ArmorItem.Type.HELMET) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getEquippedExoSuitChestplate(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == top.ribs.scguns.compat.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getEquippedExoSuitBoots(Player player) {
        for (EquipmentSlot scguns$armorSlot : EquipmentSlot.values()) {
            if (!scguns$armorSlot.isArmor()) continue;
            ItemStack armorStack = player.getItemBySlot(scguns$armorSlot);
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == top.ribs.scguns.compat.ArmorItem.Type.BOOTS) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findPowerCoreInChestplate(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("power_core")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasHudModule(ItemStack helmet) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(helmet, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("hud")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasMobilityModule(ItemStack boots) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(boots, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getHudModuleTranslationKey(ItemStack helmet) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(helmet, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("hud")) {
                    if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.NightVisionModuleItem) {
                        return "exosuit.upgrade.night_vision";
                    } else if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.TargetTrackerModuleItem) {
                        return "exosuit.upgrade.target_tracker";
                    } else {
                        return "exosuit.upgrade.hud";
                    }
                }
            }
        }
        return "exosuit.upgrade.hud";
    }

    private static String getMobilityModuleTranslationKey(ItemStack boots) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(boots, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return "exosuit.upgrade.mobility";
                }
            }
        }
        return "exosuit.upgrade.mobility";
    }

    // Simple status text method
    private static String getModuleStatusText(boolean enabled, boolean canFunction) {
        if (!enabled) {
            return "Disabled";
        } else if (!canFunction) {
            return "No Power";
        } else {
            return "Active";
        }
    }

    private static ChatFormatting getEnergyColorForHUD(int energyPercent) {
        if (energyPercent > 75) {
            return ChatFormatting.GREEN;
        } else if (energyPercent > 50) {
            return ChatFormatting.YELLOW;
        } else if (energyPercent > 25) {
            return ChatFormatting.GOLD;
        } else if (energyPercent > 0) {
            return ChatFormatting.RED;
        } else {
            return ChatFormatting.DARK_RED;
        }
    }

    private static ChatFormatting getModuleStatusColor(boolean enabled, boolean canFunction) {
        if (!enabled) {
            return ChatFormatting.GRAY;
        } else if (!canFunction) {
            return ChatFormatting.RED;
        } else {
            return ChatFormatting.GREEN;
        }
    }
}
