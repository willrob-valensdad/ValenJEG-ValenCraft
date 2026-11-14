package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.config.RaidFlareConfig;
import top.ribs.scguns.entity.projectile.RaidFlareEntity;
import top.ribs.scguns.entity.raid.RaidManager;

import java.util.List;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public class FlarePistolItem extends Item {
    public FlarePistolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack pistolStack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.FAIL;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (!(offhandStack.getItem() instanceof RaidFlareItem flareItem)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("item.scguns.flare_pistol.no_flare").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (RaidManager.hasActiveRaidInDimension(serverLevel)) {
                player.sendOverlayMessage(Component.translatable("item.scguns.flare_pistol.raid_active").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            String raidId = flareItem.getRaidId();
            if (RaidFlareConfig.getFlareData(raidId) == null) {
                player.sendOverlayMessage(Component.translatable("item.scguns.flare_pistol.no_raid_config").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            RaidFlareEntity flare = new RaidFlareEntity(serverLevel, player, raidId);
            Vec3 eyePos = player.position().add(0.0D, player.getEyeHeight(), 0.0D);
            Vec3 look = player.getLookAngle();
            flare.snapTo(eyePos.x, eyePos.y, eyePos.z, player.getYRot(), player.getXRot());
            flare.setDeltaMovement(look.scale(2.5D));
            serverLevel.addFreshEntity(flare);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.9F);

            if (!player.getAbilities().instabuild) {
                offhandStack.shrink(1);
            }
            player.getCooldowns().addCooldown(pistolStack, 30);
        }

        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.scguns.flare_pistol.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.scguns.flare_pistol.usage").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
