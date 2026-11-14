package top.ribs.scguns.item.animated;

import top.ribs.scguns.compat.transfer.LegacyEnergyTransferBridge;

import top.ribs.scguns.util.ScgunsValueIOCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import top.ribs.scguns.compat.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.object.PlayState;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.render.armor.ExoSuitRenderer;
import top.ribs.scguns.client.screen.ExoSuitMenu;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.init.ModArmorMaterials;
import top.ribs.scguns.item.exosuit.DamageableUpgradeItem;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;
public class ExoSuitItem extends ArmorItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public ExoSuitItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, ModArmorMaterials.withDurability(pProperties, pMaterial, pType));
    }

    @Override
    public void createGeoRenderer(Consumer<com.geckolib.animatable.client.GeoRenderProvider> consumer) {
        top.ribs.scguns.client.render.armor.ScGunsGeoArmorRenderProvider.accept(consumer, ExoSuitRenderer::new);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                openExoSuitScreen((ServerPlayer) player, hand);
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player != null && player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide()) {
                openExoSuitScreen((ServerPlayer) player, context.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void openExoSuitScreen(ServerPlayer player, InteractionHand hand) {
top.ribs.scguns.fabric.MenuExtras.open(player, new ExoSuitMenuProvider(hand), buf -> {
            buf.writeEnum(hand);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        tooltip.accept(Component.translatable("tooltip.scguns.exosuit.frame"));

        if (ExoSuitData.hasUpgrades(stack)) {
            tooltip.accept(Component.translatable("tooltip.scguns.exosuit.has_upgrades"));
            int upgradeCount = getCurrentUpgradeCount(stack);
            int maxSlots = getMaxUpgradeSlots();
            tooltip.accept(Component.translatable("tooltip.scguns.exosuit.slots_used", upgradeCount, maxSlots)
                    .withStyle(ChatFormatting.GRAY));

            addPowerCoreInfo(stack, tooltip);

            addPouchInfo(stack, tooltip, context.registries());

            addUpgradeDurabilityInfo(stack, tooltip);
        } else {
            tooltip.accept(Component.translatable("tooltip.scguns.exosuit.no_upgrades"));
        }

        addSlotInformation(tooltip);
        tooltip.accept(Component.translatable("info.scguns.exosuit_help",
                        KeyBinds.KEY_ATTACHMENTS.getTranslatedKeyMessage().getString().toUpperCase(Locale.ENGLISH))
                .withStyle(ChatFormatting.YELLOW));
    }
    private void addPowerCoreInfo(ItemStack stack, Consumer<Component> tooltip) {
        if (this.getType() != ArmorItem.Type.CHESTPLATE) {
            return;
        }

        ItemStack powerCore = findPowerCore(stack);
        if (!powerCore.isEmpty()) {
            IEnergyStorage energyStorage = LegacyEnergyTransferBridge.legacy(powerCore);
            int energyStored = energyStorage != null ? energyStorage.getEnergyStored() : 0;
            int maxEnergy = energyStorage != null ? energyStorage.getMaxEnergyStored() : 0;

            if (maxEnergy > 0) {
                int energyPercent = (energyStored * 100) / maxEnergy;
                ChatFormatting energyColor = getEnergyColor(energyPercent);

                tooltip.accept(Component.literal(""));
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.power_core").withStyle(ChatFormatting.YELLOW));
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.energy_level", energyPercent)
                        .withStyle(energyColor));
            }
        }
    }
    private ItemStack findPowerCore(ItemStack stack) {
        for (int slot = 0; slot < getMaxUpgradeSlots(); slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(stack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("power_core")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private ChatFormatting getEnergyColor(int energyPercent) {
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

    private void addUpgradeDurabilityInfo(ItemStack stack, Consumer<Component> tooltip) {
        boolean hasAnyDamageableUpgrades = false;

        for (int slot = 0; slot < getMaxUpgradeSlots(); slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(stack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && isUpgradeDamageable(upgradeItem)) {
                    if (!hasAnyDamageableUpgrades) {
                        tooltip.accept(Component.literal(""));
                        tooltip.accept(Component.translatable("tooltip.scguns.exosuit.upgrade_condition").withStyle(ChatFormatting.YELLOW));
                        hasAnyDamageableUpgrades = true;
                    }

                    String upgradeName = getUpgradeDisplayName(upgrade, upgradeItem);
                    int durabilityPercent = getDurabilityPercentage(upgradeItem);
                    ChatFormatting color = getDurabilityColor(durabilityPercent);

                    tooltip.accept(Component.translatable("tooltip.scguns.exosuit.upgrade_durability", upgradeName, durabilityPercent)
                            .withStyle(color));
                }
            }
        }
    }

    private String getUpgradeDisplayName(ExoSuitUpgrade upgrade, ItemStack upgradeItem) {
        String upgradeType = upgrade.getType();

        if ("hud".equals(upgradeType)) {
            if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.NightVisionModuleItem) {
                return "Night Vision";
            } else if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.TargetTrackerModuleItem) {
                return "Target Tracker";
            } else if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.GasMaskModuleItem) {
                return "Gas Mask";
            } else if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.RebreatherModuleItem) {
                return "Rebreather";
            } else {
                return "HUD System";
            }
        }
        if ("breathing".equals(upgradeType)) {
            if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.GasMaskModuleItem) {
                return "Gas Mask";
            } else if (upgradeItem.getItem() instanceof top.ribs.scguns.item.exosuit.RebreatherModuleItem) {
                return "Rebreather";
            } else {
                return "Life Support";
            }
        }
        return switch (upgradeType) {
            case "plating" -> "Armor Plating";
            case "pauldron" -> "Pauldron";
            case "power_core" -> "Power Core";
            case "utility" -> "Utility Module";
            case "knee_guard" -> "Knee Guard";
            case "mobility" -> "Mobility System";
            default -> {
                String itemName = upgradeItem.getDisplayName().getString();
                if (itemName.startsWith("Heavy ")) {
                    itemName = itemName.substring(6);
                }
                yield itemName;
            }
        };
    }
    private int getDurabilityPercentage(ItemStack upgradeItem) {
        if (!isUpgradeDamageable(upgradeItem)) {
            return 100;
        }

        int maxDamage = upgradeItem.getMaxDamage();
        int currentDamage = upgradeItem.getDamageValue();

        if (maxDamage <= 0) {
            return 100;
        }

        int remainingDurability = maxDamage - currentDamage;
        return Math.max(0, (remainingDurability * 100) / maxDamage);
    }

    private ChatFormatting getDurabilityColor(int durabilityPercent) {
        if (durabilityPercent > 75) {
            return ChatFormatting.GREEN;
        } else if (durabilityPercent > 50) {
            return ChatFormatting.YELLOW;
        } else if (durabilityPercent > 25) {
            return ChatFormatting.GOLD;
        } else if (durabilityPercent > 0) {
            return ChatFormatting.RED;
        } else {
            return ChatFormatting.DARK_RED;
        }
    }

    private boolean isUpgradeDamageable(ItemStack upgradeItem) {
        if (upgradeItem.isEmpty()) {
            return false;
        }
        if (upgradeItem.getItem() instanceof DamageableUpgradeItem) {
            return true;
        }
        if (upgradeItem.isDamageableItem()) {
            return true;
        }
        return upgradeItem.getMaxDamage() > 0;
    }
    private void addPouchInfo(ItemStack stack, Consumer<Component> tooltip, HolderLookup.Provider registries) {
        if (this.getType() != ArmorItem.Type.CHESTPLATE) {
            return;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(stack);
        if (!pouchUpgrade.isEmpty()) {
            ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
            if (upgrade != null) {
                tooltip.accept(Component.literal(""));
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.pouches").withStyle(ChatFormatting.YELLOW));
                String pouchName = pouchUpgrade.getDisplayName().getString();
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.pouch_equipped", pouchName)
                        .withStyle(ChatFormatting.GREEN));
                if (isPouchEmpty(stack, pouchUpgrade, registries)) {
                    tooltip.accept(Component.translatable("tooltip.scguns.exosuit.pouch_empty")
                            .withStyle(ChatFormatting.DARK_GREEN));
                } else {
                    tooltip.accept(Component.translatable("tooltip.scguns.exosuit.pouch_has_items")
                            .withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }
    private ItemStack findPouchUpgrade(ItemStack stack) {
        for (int slot = 0; slot < getMaxUpgradeSlots(); slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(stack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private boolean isPouchEmpty(ItemStack chestplate, ItemStack pouchUpgrade, HolderLookup.Provider registries) {
        try {
            String pouchId = pouchUpgrade.getItem().toString();
            CustomData customData = chestplate.get(DataComponents.CUSTOM_DATA);
            CompoundTag pouchData = customData != null ? customData.copyTag().getCompoundOrEmpty("PouchData") : new CompoundTag();

            if (!pouchData.contains(pouchId)) {
                return true;
            }

            ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
            if (upgrade == null) {
                return true;
            }

            ItemStackHandler handler = new ItemStackHandler(upgrade.getDisplay().getStorageSize());
            ScgunsValueIOCompat.read(handler, registries, pouchData.getCompoundOrEmpty(pouchId));

            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
    private void addSlotInformation(Consumer<Component> tooltip) {
        switch (this.getType()) {
            case HELMET:
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.slots.helmet"));
                break;
            case CHESTPLATE:
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.slots.chest"));
                break;
            case LEGGINGS:
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.slots.legs"));
                break;
            case BOOTS:
                tooltip.accept(Component.translatable("tooltip.scguns.exosuit.slots.boots"));
                break;
        }
    }
    public int getMaxUpgradeSlots() {
        return switch (this.getType()) {
            case HELMET -> 3;      // Plating, HUD, Breathing
            case CHESTPLATE -> 4;  // Fixed to 4 instead of 5 to match the screen layout
            case LEGGINGS -> 3;    // Plating, Knee Guards, Utility
            case BOOTS -> 2;       // Plating, Mobility
            default -> 0;
        };
    }

    public int getCurrentUpgradeCount(ItemStack stack) {
        CompoundTag upgradeData = ExoSuitData.getUpgradeData(stack);

        if (upgradeData.contains("Upgrades")) {
            ListTag upgradeList = upgradeData.getList("Upgrades").orElseGet(ListTag::new); // 10 = CompoundTag type
            return upgradeList.size();
        }

        return 0;
    }

    private PlayState predicate(AnimationTest animationState) {
        animationState.controller().setAnimation(RawAnimation.begin().thenLoop("animation.exo_suit.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>( "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public record ExoSuitMenuProvider(InteractionHand hand) implements MenuProvider {

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("container.scguns.exosuit");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
            return new ExoSuitMenu(id, playerInventory, hand);
        }
    }
}
