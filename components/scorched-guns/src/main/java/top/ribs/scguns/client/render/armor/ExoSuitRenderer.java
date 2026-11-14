package top.ribs.scguns.client.render.armor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public class ExoSuitRenderer extends FreshAnimationsGeoArmorRenderer<ExoSuitItem> {
    private static final DataTicket<ItemStack> RENDER_STACK =
            DataTickets.create("scguns:exosuit_render_stack", ItemStack.class);

    public ExoSuitRenderer() {
        super(new ExoSuitModel());
    }
    /**
     * Enum to define different armor component types and their rendering behavior
     */
    public enum ArmorComponentType {
        // Base components - the basic exosuit frame
        BASE(false),

        // Standard armor - medium coverage, replaces base
        STANDARD(true),

        // Heavy armor - full coverage, replaces base
        HEAVY(true),

        LIGHT(false),

        MEDIUM(true),

        // Utility components - attachments and accessories
        UTILITY(false),

        // Special/Future armor types
        STEALTH(false),
        TECH(false);

        private final boolean hidesBase;

        ArmorComponentType(boolean hidesBase) {
            this.hidesBase = hidesBase;
        }

        public boolean shouldHideBase() {
            return hidesBase;
        }
    }

    @Override
    public void captureDefaultRenderState(ExoSuitItem animatable,
                                          GeoArmorRenderer.RenderData renderData,
                                          HumanoidRenderState renderState,
                                          float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);

        // GeckoLib 5 injects GeoRenderState into Minecraft's EntityRenderState hierarchy
        // (META-INF/interface_injections.json). Javac does not expose that injected
        // interface on HumanoidRenderState here, so bridge through the runtime contract.
        ((GeoRenderState) (Object) renderState).addGeckolibData(RENDER_STACK, renderData.itemStack());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void adjustModelBonesForRender(RenderPassInfo renderInfo,
                                          BoneSnapshots snapshots) {
        // Raw RenderPassInfo is deliberate: its generic bound is GeoRenderState, while
        // HumanoidRenderState gains that interface through GeckoLib's interface injection.
        super.adjustModelBonesForRender(renderInfo, snapshots);
        ItemStack armorStack = (ItemStack) renderInfo.getOrDefaultGeckolibData(RENDER_STACK, ItemStack.EMPTY);

        for (GeoBone bone : renderInfo.model().boneLookup().get().values()) {
            handleComponentVisibility(snapshots.get(bone), bone.name(), armorStack);
        }
    }

    private static void setBoneHidden(BoneSnapshot snapshot, boolean hidden) {
        snapshot.skipRender(hidden).skipChildrenRender(hidden);
    }

    private void handleComponentVisibility(BoneSnapshot snapshot, String boneName, ItemStack armorStack) {
        if (armorStack == null || !(armorStack.getItem() instanceof ExoSuitItem)) {
            return;
        }

        boolean hasPlatingUpgrade = hasUpgradeInSlot(armorStack, 0);
        ArmorComponentType activeArmorType = determineActiveArmorType(armorStack);

        if (isBaseComponent(boneName)) {
            boolean shouldHideBase = activeArmorType.shouldHideBase() && hasPlatingUpgrade;
            setBoneHidden(snapshot, shouldHideBase);
            return;
        }

        if (isStandardArmorComponent(boneName)) {
            boolean showStandard = hasPlatingUpgrade && activeArmorType == ArmorComponentType.STANDARD;
            setBoneHidden(snapshot, !showStandard);
            return;
        }

        if (isHeavyArmorComponent(boneName)) {
            boolean showHeavy = hasPlatingUpgrade && activeArmorType == ArmorComponentType.HEAVY;
            setBoneHidden(snapshot, !showHeavy);
            return;
        }

        if (isLightArmorComponent(boneName)) {
            boolean showLight = hasPlatingUpgrade && activeArmorType == ArmorComponentType.LIGHT;
            setBoneHidden(snapshot, !showLight);
            return;
        }

        if (isNightVisionComponent(boneName)) {
            boolean showNightVision = hasSpecificHudUpgrade(armorStack, top.ribs.scguns.item.exosuit.NightVisionModuleItem.class);
            setBoneHidden(snapshot, !showNightVision);
            return;
        }

        if (isTargetTrackerComponent(boneName)) {
            boolean showTargetTracker = hasSpecificHudUpgrade(armorStack, top.ribs.scguns.item.exosuit.TargetTrackerModuleItem.class);
            setBoneHidden(snapshot, !showTargetTracker);
            return;
        }

        if (isBreathingComponent(boneName)) {
            boolean hasBreathingUpgrade = hasUpgradeOfType(armorStack, "breathing");

            if (!hasBreathingUpgrade) {
                setBoneHidden(snapshot, true);
                return;
            }
            ItemStack breathingUpgrade = findBreathingUpgrade(armorStack);
            if (!breathingUpgrade.isEmpty()) {
                boolean isRebreather = breathingUpgrade.getItem() instanceof top.ribs.scguns.item.exosuit.RebreatherModuleItem;
                boolean isGasMask = breathingUpgrade.getItem() instanceof top.ribs.scguns.item.exosuit.GasMaskModuleItem;

                if (boneName.equals("breathing_unit_1")) {
                    setBoneHidden(snapshot, !isRebreather);
                } else if (boneName.equals("breathing_unit_2")) {
                    setBoneHidden(snapshot, !isGasMask);
                }
            } else {
                setBoneHidden(snapshot, true);
            }
            return;
        }
        if (isJetpackComponent(boneName)) {
            boolean hasJetpackUpgrade = hasUpgradeOfType(armorStack, "jetpack") || hasJetpackUtilityUpgrade(armorStack);
            setBoneHidden(snapshot, !hasJetpackUpgrade);
            return;
        }
        if (isPauldronComponent(boneName)) {
            boolean hasPauldronUpgrade = hasUpgradeOfType(armorStack, "pauldron");

            if (!hasPauldronUpgrade) {
                setBoneHidden(snapshot, true);
                return;
            }

            ItemStack pauldronUpgrade = findPauldronUpgrade(armorStack);
            if (!pauldronUpgrade.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(pauldronUpgrade);
                if (upgrade != null) {
                    String pauldronModel = upgrade.getDisplay().getModel();

                    if (boneName.contains("heavy_")) {
                        boolean showHeavyPauldron = pauldronModel.contains("heavy");
                        setBoneHidden(snapshot, !showHeavyPauldron);
                    }
                    else if (boneName.contains("standard_")) {
                        boolean showStandardPauldron = pauldronModel.contains("standard") || !pauldronModel.contains("heavy");
                        setBoneHidden(snapshot, !showStandardPauldron);
                    }
                    else {
                        setBoneHidden(snapshot, false);
                    }
                } else {
                    setBoneHidden(snapshot, boneName.contains("heavy_"));
                }
            } else {
                setBoneHidden(snapshot, true);
            }
            return;
        }

        if (isPouchesComponent(boneName)) {
            boolean showPouches = hasUpgradeOfType(armorStack, "pouches");
            if (!showPouches) {
                setBoneHidden(snapshot, true);
                return;
            }
            ItemStack pouchesUpgrade = findPouchesUpgrade(armorStack);
            if (!pouchesUpgrade.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(pouchesUpgrade);
                if (upgrade != null) {
                    String pouchesModel = upgrade.getDisplay().getModel();

                    if (boneName.contains("heavy_")) {
                        boolean showHeavyPouches = pouchesModel.contains("heavy");
                        setBoneHidden(snapshot, !showHeavyPouches);
                    } else if (boneName.contains("standard_")) {
                        boolean showStandardPouches = pouchesModel.contains("standard") || !pouchesModel.contains("heavy");
                        setBoneHidden(snapshot, !showStandardPouches);
                    } else {
                        setBoneHidden(snapshot, false);
                    }
                } else {
                    setBoneHidden(snapshot, boneName.contains("heavy_"));
                }
            } else {
                setBoneHidden(snapshot, true);
            }
            return;
        }
        if (isBackpackComponent(boneName)) {
            boolean showBackpack = hasUpgradeOfType(armorStack, "backpack") || hasUpgradeOfType(armorStack, "utility");
            setBoneHidden(snapshot, !showBackpack);
            return;
        }

        if (isKneeGuardComponent(boneName)) {
            boolean hasKneeGuardUpgrade = hasUpgradeOfType(armorStack, "knee_guard");
            boolean hasPlatingInKneeGuardSlot = hasPlatingInKneeGuardSlot(armorStack);

            if (!hasKneeGuardUpgrade && !hasPlatingInKneeGuardSlot) {
                setBoneHidden(snapshot, true);
                return;
            }

            ItemStack kneeGuardUpgrade = findKneeGuardOrPlatingUpgrade(armorStack);
            if (!kneeGuardUpgrade.isEmpty()) {
                ExoSuitUpgrade upgrade;
                if (hasPlatingInKneeGuardSlot && !hasKneeGuardUpgrade) {
                    upgrade = getRenderUpgradeForSlot(kneeGuardUpgrade, "knee_guard");
                } else {
                    upgrade = getRenderUpgrade(kneeGuardUpgrade);
                }

                if (upgrade != null) {
                    String kneeGuardModel = upgrade.getDisplay().getModel();

                    if (boneName.contains("heavy_")) {
                        boolean showHeavyKneeGuard = kneeGuardModel.contains("heavy");
                        setBoneHidden(snapshot, !showHeavyKneeGuard);
                    } else if (boneName.contains("standard_")) {
                        boolean showStandardKneeGuard = kneeGuardModel.contains("standard") || !kneeGuardModel.contains("heavy");
                        setBoneHidden(snapshot, !showStandardKneeGuard);
                    } else {
                        setBoneHidden(snapshot, false);
                    }
                } else {
                    setBoneHidden(snapshot, boneName.contains("heavy_"));
                }
            } else {
                setBoneHidden(snapshot, true);
            }
            return;
        }
        if (isMobilityModuleComponent(boneName)) {
            boolean hasMobilityUpgrade = hasUpgradeOfType(armorStack, "mobility");

            if (!hasMobilityUpgrade) {
                setBoneHidden(snapshot, true);
                return;
            }

            ItemStack mobilityUpgrade = findMobilityUpgrade(armorStack);
            if (!mobilityUpgrade.isEmpty()) {
                boolean isRabbitModule = mobilityUpgrade.getItem() instanceof top.ribs.scguns.item.exosuit.RabbitModuleItem;
                boolean isShockAbsorberModule = mobilityUpgrade.getItem().toString().contains("shock_absorber");

                if (boneName.contains("rabbit_")) {
                    setBoneHidden(snapshot, !isRabbitModule);
                } else if (boneName.contains("shock_absorber_")) {
                    setBoneHidden(snapshot, !isShockAbsorberModule);
                } else {
                    setBoneHidden(snapshot, false);
                }
            } else {
                setBoneHidden(snapshot, true);
            }
            return;
        }

        if (isFutureUpgradeBone(boneName)) {
            setBoneHidden(snapshot, true);
        }
    }

    private ArmorComponentType determineActiveArmorType(ItemStack armorStack) {
        if (hasUpgradeInSlot(armorStack, 0)) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, 0);

            ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
            if (upgrade != null) {
                String modelType = upgrade.getDisplay().getModel();

                return switch (modelType) {
                    case "heavy_plating", "heavy" -> ArmorComponentType.HEAVY;
                    case "standard_plating", "standard" -> ArmorComponentType.STANDARD;
                    case "light_plating", "light" -> ArmorComponentType.LIGHT;
                    case "medium_plating", "medium" -> ArmorComponentType.MEDIUM;
                    default -> ArmorComponentType.BASE;
                };
            }
        }

        return ArmorComponentType.BASE;
    }

    private boolean hasUpgradeInSlot(ItemStack armorStack, int slot) {
        try {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            boolean hasUpgrade = !upgradeItem.isEmpty();

            if (hasUpgrade) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                return upgrade != null;
            }

            return false;
        } catch (Exception e) {
            System.err.println("ExoSuit: Error checking upgrade slot " + slot + ": " + e.getMessage());
            return false;
        }
    }

    private boolean hasUpgradeOfType(ItemStack armorStack, String upgradeType) {
        try {
            for (int slot = 0; slot < 4; slot++) {
                ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                if (!upgradeItem.isEmpty()) {
                    ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                    if (upgrade != null && upgrade.getType().equals(upgradeType)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean hasPlatingInKneeGuardSlot(ItemStack armorStack) {
        try {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, 1);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                return upgrade != null && upgrade.getType().equals("plating");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    private ItemStack findKneeGuardOrPlatingUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                if (upgrade != null) {
                    if (upgrade.getType().equals("knee_guard")) {
                        return upgradeItem;
                    }
                    if (slot == 1 && upgrade.getType().equals("plating")) {
                        return upgradeItem;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private boolean hasSpecificHudUpgrade(ItemStack armorStack, Class<?> upgradeClass) {
        try {
            for (int slot = 0; slot < 4; slot++) {
                ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                if (!upgradeItem.isEmpty()) {
                    ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                    if (upgrade != null && upgrade.getType().equals("hud") &&
                            upgradeClass.isInstance(upgradeItem.getItem())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack findBreathingUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("breathing")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findPauldronUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pauldron")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private ItemStack findPouchesUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findMobilityUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }



    private boolean isBaseComponent(String boneName) {
        return boneName.equals("base_helmet") ||
                boneName.equals("base_torso") ||
                boneName.equals("base_right_arm") ||
                boneName.equals("base_left_arm") ||
                boneName.equals("base_right_leg") ||
                boneName.equals("base_left_leg") ||
                boneName.equals("base_right_boot") ||
                boneName.equals("base_left_boot");
    }

    private boolean isStandardArmorComponent(String boneName) {
        return boneName.equals("standard_helmet") ||
                boneName.equals("standard_torso") ||
                boneName.equals("standard_right_arm") ||
                boneName.equals("standard_left_arm") ||
                boneName.equals("standard_right_leg") ||
                boneName.equals("standard_left_leg") ||
                boneName.equals("standard_right_boot") ||
                boneName.equals("standard_left_boot");
    }

    private boolean isHeavyArmorComponent(String boneName) {
        return boneName.equals("heavy_helmet") ||
                boneName.equals("heavy_torso") ||
                boneName.equals("heavy_right_arm") ||
                boneName.equals("heavy_left_arm") ||
                boneName.equals("heavy_right_leg") ||
                boneName.equals("heavy_left_leg") ||
                boneName.equals("heavy_right_boot") ||
                boneName.equals("heavy_left_boot");
    }

    private boolean isLightArmorComponent(String boneName) {
        return boneName.equals("light_helmet") ||
                boneName.equals("light_torso") ||
                boneName.equals("light_right_arm") ||
                boneName.equals("light_left_arm") ||
                boneName.equals("light_right_leg") ||
                boneName.equals("light_left_leg") ||
                boneName.equals("light_right_boot") ||
                boneName.equals("light_left_boot");
    }
    private boolean isJetpackComponent(String boneName) {
        return boneName.equals("jetpack");
    }

    /**
     * Checks if there's a jetpack-specific utility upgrade
     */
    private boolean hasJetpackUtilityUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = getRenderUpgrade(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility")) {
                    String model = upgrade.getDisplay().getModel();
                    return model != null && model.contains("jetpack");
                }
            }
        }
        return false;
    }
    private boolean isNightVisionComponent(String boneName) {
        return boneName.equals("night_vision");
    }

    private boolean isTargetTrackerComponent(String boneName) {
        return boneName.equals("target_tracker");
    }

    private boolean isBreathingComponent(String boneName) {
        return boneName.equals("breathing_unit_1") || boneName.equals("breathing_unit_2");
    }

    private boolean isPauldronComponent(String boneName) {
        return boneName.equals("heavy_right_pauldron") ||
                boneName.equals("heavy_left_pauldron") ||
                boneName.equals("standard_right_pauldron") ||
                boneName.equals("standard_left_pauldron");
    }

    private boolean isPouchesComponent(String boneName) {
        return boneName.equals("standard_pouches") ||
                boneName.equals("heavy_pouches");
    }

    private boolean isBackpackComponent(String boneName) {
        return boneName.equals("backpack");
    }

    // NEW: Check for knee guard components
    private boolean isKneeGuardComponent(String boneName) {
        return boneName.equals("standard_left_knee_guard") ||
                boneName.equals("standard_right_knee_guard") ||
                boneName.equals("heavy_left_knee_guard") ||
                boneName.equals("heavy_right_knee_guard");
    }

    // NEW: Check for mobility module components (rabbit and shock absorber)
    private boolean isMobilityModuleComponent(String boneName) {
        return boneName.equals("rabbit_left_module") ||
                boneName.equals("rabbit_right_module") ||
                boneName.equals("shock_absorber_left_module") ||
                boneName.equals("shock_absorber_right_module");
    }

    private ExoSuitUpgrade getRenderUpgrade(ItemStack upgradeStack) {
        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeStack);
        return upgrade != null ? upgrade : getBuiltinRenderUpgrade(upgradeStack);
    }

    private ExoSuitUpgrade getRenderUpgradeForSlot(ItemStack upgradeStack, String slotType) {
        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItemInSlot(upgradeStack, slotType);
        return upgrade != null ? upgrade : getBuiltinRenderUpgradeForSlot(upgradeStack, slotType);
    }

    private ExoSuitUpgrade getBuiltinRenderUpgradeForSlot(ItemStack upgradeStack, String slotType) {
        ExoSuitUpgrade upgrade = getBuiltinRenderUpgrade(upgradeStack);
        if (upgrade != null && "knee_guard".equals(slotType) && "plating".equals(upgrade.getType())) {
            return renderUpgrade("knee_guard", upgrade.getDisplay().getModel());
        }
        return upgrade;
    }

    private ExoSuitUpgrade getBuiltinRenderUpgrade(ItemStack upgradeStack) {
        if (upgradeStack == null || upgradeStack.isEmpty()) {
            return null;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(upgradeStack.getItem());
        if (itemId == null || !"scguns".equals(itemId.getNamespace())) {
            return null;
        }

        return switch (itemId.getPath()) {
            case "armor_plate" -> renderUpgrade("plating", "standard");
            case "heavy_armor_plate" -> renderUpgrade("plating", "heavy");
            case "pauldron" -> renderUpgrade("pauldron", "standard_pauldron");
            case "heavy_pauldron" -> renderUpgrade("pauldron", "heavy_pauldron");
            case "armor_pouches" -> renderUpgrade("pouches", "standard_pouches");
            case "heavy_armor_pouches" -> renderUpgrade("pouches", "heavy_pouches");
            case "exo_suit_core", "advanced_exo_suit_core" -> renderUpgrade("power_core", "power_core");
            case "night_vision_module" -> renderUpgrade("hud", "night_vision");
            case "target_tracker_module" -> renderUpgrade("hud", "target_tracker");
            case "gas_mask_module" -> renderUpgrade("breathing", "breathing_unit_2");
            case "rebreather_module" -> renderUpgrade("breathing", "breathing_unit_1");
            case "jetpack_module" -> renderUpgrade("utility", "jetpack");
            case "rabbit_module" -> renderUpgrade("mobility", "rabbit_module");
            case "shock_absorber" -> renderUpgrade("mobility", "shock_absorber_module");
            case "suit_grease", "tension_spring" -> renderUpgrade("utility", "");
            default -> null;
        };
    }

    private ExoSuitUpgrade renderUpgrade(String type, String model) {
        return ExoSuitUpgrade.Builder.create()
                .setType(type)
                .setModel(model)
                .build();
    }

    private boolean isFutureUpgradeBone(String boneName) {
        if (boneName.equals("night_vision") || boneName.equals("target_tracker") ||
                boneName.equals("breathing_unit_1") || boneName.equals("breathing_unit_2")) {
            return false;
        }

        return boneName.contains("plating_") ||
                boneName.contains("upgrade_") ||
                boneName.contains("power_") ||
                boneName.contains("enhancement_") ||
                boneName.contains("stealth_") ||
                boneName.contains("tech_") ||
                boneName.contains("utility_");
    }
}
