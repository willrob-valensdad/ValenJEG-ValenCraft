package top.ribs.scguns.item;


import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class EnemyLogItem extends TeamLogItem {

    public EnemyLogItem(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = getCustomData(stack);
        if (!tag.isEmpty()) {
            // Show individual whitelisted entities
            if (tag.contains("Whitelist")) {
                ListTag listTag = tag.getListOrEmpty("Whitelist");
                if (!listTag.isEmpty()) {
                    tooltip.accept(Component.literal("Whitelisted Entities:"));
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompoundOrEmpty(i);
                        String entityName = entityTag.getStringOr("Name", "");
                        String entityType = entityTag.getStringOr("EntityType", "");
                        tooltip.accept(Component.literal("- " + entityName + " (" + entityType + ")"));
                    }
                }
            }

            // Show whitelisted entity types
            if (tag.contains("WhitelistEntityTypes")) {
                ListTag whitelistTag = tag.getListOrEmpty("WhitelistEntityTypes");
                if (!whitelistTag.isEmpty()) {
                    tooltip.accept(Component.literal("Whitelisted Entity Types:"));
                    for (int i = 0; i < whitelistTag.size(); i++) {
                        String entityType = whitelistTag.getStringOr(i, "");
                        tooltip.accept(Component.literal("- " + entityType));
                    }
                }
            }
        } else {
            tooltip.accept(Component.literal("No entities or types whitelisted"));
        }
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            EntityHitResult hitResult = rayTraceEntities(level, player);
            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity targetEntity) {
                if (player.isShiftKeyDown()) {
                    // Shift-right-click: add entity type to whitelist
                    boolean added = addEntityTypeToWhitelist(itemStack, targetEntity);
                    if (added) {
                        player.sendOverlayMessage(Component.literal("Added all " + targetEntity.getType().getDescription().getString() + " to whitelist"));
                    } else {
                        player.sendOverlayMessage(Component.literal(targetEntity.getType().getDescription().getString() + " is already whitelisted"));
                    }
                } else {
                    // Right-click: add specific entity to whitelist
                    boolean added = addEntityToWhitelist(itemStack, targetEntity);
                    if (added) {
                        player.sendOverlayMessage(Component.literal("Added " + targetEntity.getName().getString() + " to Whitelist"));
                    } else {
                        player.sendOverlayMessage(Component.literal(targetEntity.getName().getString() + " is already in Whitelist"));
                    }
                }
                logCurrentWhitelist(itemStack, player);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    boolean addEntityToWhitelist(ItemStack stack, LivingEntity targetEntity) {
        CompoundTag tag = getCustomData(stack);
        ListTag listTag = tag.getListOrEmpty("Whitelist");
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag existingTag = listTag.getCompoundOrEmpty(i);
            if (existingTag.read("UUID", UUIDUtil.CODEC).orElseThrow().equals(targetEntity.getUUID())) {
                return false; // Already in whitelist
            }
        }
        CompoundTag entityTag = new CompoundTag();
        entityTag.store("UUID", UUIDUtil.CODEC, targetEntity.getUUID());
        entityTag.putString("Name", targetEntity.getName().getString());
        entityTag.putString("EntityType", EntityType.getKey(targetEntity.getType()).toString());
        listTag.add(entityTag);
        tag.put("Whitelist", listTag);
        setCustomData(stack, tag);
        return true;
    }

    boolean addEntityTypeToWhitelist(ItemStack stack, LivingEntity targetEntity) {
        CompoundTag tag = getCustomData(stack);
        ListTag whitelistTag = tag.getListOrEmpty("WhitelistEntityTypes");
        String entityTypeKey = EntityType.getKey(targetEntity.getType()).toString();
        for (int i = 0; i < whitelistTag.size(); i++) {
            if (whitelistTag.getStringOr(i, "").equals(entityTypeKey)) {
                return false; // Entity type already whitelisted
            }
        }
        whitelistTag.add(StringTag.valueOf(entityTypeKey));
        tag.put("WhitelistEntityTypes", whitelistTag);
        setCustomData(stack, tag);
        return true;
    }

    void logCurrentWhitelist(ItemStack stack, Player player) {
        CompoundTag tag = getCustomData(stack);
        if (!tag.isEmpty()) {
            if (tag.contains("Whitelist")) {
                ListTag listTag = tag.getListOrEmpty("Whitelist");
                if (!listTag.isEmpty()) {
                    player.sendOverlayMessage(Component.literal("Entities in Whitelist:"));
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompoundOrEmpty(i);
                        String entityName = entityTag.getStringOr("Name", "");
                        String entityType = entityTag.getStringOr("EntityType", "");
                        player.sendOverlayMessage(Component.literal("- " + entityName + " (" + entityType + ")"));
                    }
                } else {
                    player.sendOverlayMessage(Component.literal("No specific entities whitelisted."));
                }
            }

            if (tag.contains("WhitelistEntityTypes")) {
                ListTag whitelistTag = tag.getListOrEmpty("WhitelistEntityTypes");
                if (!whitelistTag.isEmpty()) {
                    player.sendOverlayMessage(Component.literal("Whitelisted Entity Types:"));
                    for (int i = 0; i < whitelistTag.size(); i++) {
                        String entityType = whitelistTag.getStringOr(i, "");
                        player.sendOverlayMessage(Component.literal("- " + entityType));
                    }
                } else {
                    player.sendOverlayMessage(Component.literal("No entity types whitelisted."));
                }
            }
        } else {
            player.sendOverlayMessage(Component.literal("No entities or types logged in the item."));
        }
    }
}
