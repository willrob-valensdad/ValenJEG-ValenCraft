package top.ribs.scguns.item;


import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class TeamLogItem extends Item {

    public TeamLogItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            EntityHitResult hitResult = rayTraceEntities(level, player);
            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity targetEntity) {
                if (player.isShiftKeyDown()) {
                    boolean added = addEntityTypeToBlacklist(itemStack, targetEntity);
                    if (added) {
                        player.sendOverlayMessage(Component.literal("Added all " + targetEntity.getType().getDescription().getString() + " to blacklist"));
                    } else {
                        player.sendOverlayMessage(Component.literal(targetEntity.getType().getDescription().getString() + " is already blacklisted"));
                    }
                } else {
                    boolean added = addEntityToTeamLog(itemStack, targetEntity);
                    if (added) {
                        player.sendOverlayMessage(Component.literal("Added " + targetEntity.getName().getString() + " to Team Log"));
                    } else {
                        player.sendOverlayMessage(Component.literal(targetEntity.getName().getString() + " is already in Team Log"));
                    }
                }
                logCurrentEntities(itemStack, player);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    EntityHitResult rayTraceEntities(Level level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        Vec3 endPos = eyePosition.add(lookVector.scale(5.0));
        AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(5.0)).inflate(1.0D, 1.0D, 1.0D);
        List<Entity> entities = level.getEntities(player, searchBox, entity -> entity instanceof LivingEntity && entity != player);
        EntityHitResult closestHitResult = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            AABB boundingBox = entity.getBoundingBox();
            Vec3 intercept = boundingBox.clip(eyePosition, endPos).orElse(null);
            if (intercept != null) {
                double distance = eyePosition.distanceTo(intercept);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestHitResult = new EntityHitResult(entity, intercept);
                }
            }
        }
        return closestHitResult;
    }
    boolean addEntityToTeamLog(ItemStack stack, LivingEntity targetEntity) {
        CompoundTag tag = getCustomData(stack);
        ListTag listTag = tag.getListOrEmpty("Entities");
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag existingTag = listTag.getCompoundOrEmpty(i);
            if (existingTag.read("UUID", UUIDUtil.CODEC).orElseThrow().equals(targetEntity.getUUID())) {
                return false;
            }
        }
        CompoundTag entityTag = new CompoundTag();
        entityTag.store("UUID", UUIDUtil.CODEC, targetEntity.getUUID());
        entityTag.putString("Name", targetEntity.getName().getString());
        entityTag.putString("EntityType", EntityType.getKey(targetEntity.getType()).toString());
        listTag.add(entityTag);
        tag.put("Entities", listTag);
        setCustomData(stack, tag);
        return true;
    }

    boolean addEntityTypeToBlacklist(ItemStack stack, LivingEntity targetEntity) {
        CompoundTag tag = getCustomData(stack);
        ListTag blacklistTag = tag.getListOrEmpty("Blacklist");
        String entityTypeKey = EntityType.getKey(targetEntity.getType()).toString();
        for (int i = 0; i < blacklistTag.size(); i++) {
            if (blacklistTag.getStringOr(i, "").equals(entityTypeKey)) {
                return false;
            }
        }
        blacklistTag.add(StringTag.valueOf(entityTypeKey));
        tag.put("Blacklist", blacklistTag);
        setCustomData(stack, tag);
        return true;
    }

    void logCurrentEntities(ItemStack stack, Player player) {
        CompoundTag tag = getCustomData(stack);
        if (!tag.isEmpty()) {
            if (tag.contains("Entities")) {
                ListTag listTag = tag.getListOrEmpty("Entities");
                if (!listTag.isEmpty()) {
                    player.sendOverlayMessage(Component.literal("Entities in Team Log:"));
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompoundOrEmpty(i);
                        String entityName = entityTag.getStringOr("Name", "");
                        String entityType = entityTag.getStringOr("EntityType", "");
                        player.sendOverlayMessage(Component.literal("- " + entityName + " (" + entityType + ")"));
                    }
                } else {
                    player.sendOverlayMessage(Component.literal("No specific entities logged."));
                }
            }

            if (tag.contains("Blacklist")) {
                ListTag blacklistTag = tag.getListOrEmpty("Blacklist");
                if (!blacklistTag.isEmpty()) {
                    player.sendOverlayMessage(Component.literal("Blacklisted Entity Types:"));
                    for (int i = 0; i < blacklistTag.size(); i++) {
                        String entityType = blacklistTag.getStringOr(i, "");
                        player.sendOverlayMessage(Component.literal("- " + entityType));
                    }
                } else {
                    player.sendOverlayMessage(Component.literal("No entity types blacklisted."));
                }
            }
        } else {
            player.sendOverlayMessage(Component.literal("No entities or types logged in the item."));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = getCustomData(stack);
        if (!tag.isEmpty()) {
            if (tag.contains("Entities")) {
                ListTag listTag = tag.getListOrEmpty("Entities");
                if (!listTag.isEmpty()) {
                    tooltip.accept(Component.literal("Logged Entities:"));
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompoundOrEmpty(i);
                        String entityName = entityTag.getStringOr("Name", "");
                        String entityType = entityTag.getStringOr("EntityType", "");
                        tooltip.accept(Component.literal("- " + entityName + " (" + entityType + ")"));
                    }
                }
            }

            if (tag.contains("Blacklist")) {
                ListTag blacklistTag = tag.getListOrEmpty("Blacklist");
                if (!blacklistTag.isEmpty()) {
                    tooltip.accept(Component.literal("Blacklisted Entity Types:"));
                    for (int i = 0; i < blacklistTag.size(); i++) {
                        String entityType = blacklistTag.getStringOr(i, "");
                        tooltip.accept(Component.literal("- " + entityType));
                    }
                }
            }
        } else {
            tooltip.accept(Component.literal("No entities or types logged"));
        }
    }
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        CompoundTag tag = getCustomData(stack);
        if (!tag.isEmpty()) {
            // Check for any type of logged data
            if (tag.contains("Entities")) {
                ListTag listTag = tag.getListOrEmpty("Entities");
                if (!listTag.isEmpty()) {
                    return true;
                }
            }
            if (tag.contains("Blacklist")) {
                ListTag blacklistTag = tag.getListOrEmpty("Blacklist");
                if (!blacklistTag.isEmpty()) {
                    return true;
                }
            }
            // For EnemyLogItem whitelist checks
            if (tag.contains("Whitelist")) {
                ListTag whitelistTag = tag.getListOrEmpty("Whitelist");
                if (!whitelistTag.isEmpty()) {
                    return true;
                }
            }
            if (tag.contains("WhitelistEntityTypes")) {
                ListTag whitelistTypeTag = tag.getListOrEmpty("WhitelistEntityTypes");
                if (!whitelistTypeTag.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    protected static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
