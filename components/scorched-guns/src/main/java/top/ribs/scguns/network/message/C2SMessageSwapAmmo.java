package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.PlayMessageContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;

public class C2SMessageSwapAmmo {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageSwapAmmo> STREAM_CODEC = StreamCodec.of(
            C2SMessageSwapAmmo::encode,
            C2SMessageSwapAmmo::decode
    );
    private final Identifier selectedAmmo;

    public C2SMessageSwapAmmo(Identifier selectedAmmo) {
        this.selectedAmmo = selectedAmmo;
    }

    private static CompoundTag getStackTag(ItemStack stack) {
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

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageSwapAmmo message) {
        buffer.writeIdentifier(message.selectedAmmo);
    }

    private static C2SMessageSwapAmmo decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageSwapAmmo(buffer.readIdentifier());
    }

    public static void handle(C2SMessageSwapAmmo message, PlayMessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player == null || player.isSpectator()) {
                return;
            }

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem gunItem)) {
                return;
            }

            Gun gun = gunItem.getModifiedGun(stack);
            if (!gun.getGeneral().allowsAmmoChange()) {
                player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.not_supported").withStyle(ChatFormatting.RED));
                return;
            }
            if (gun.getAcceptedProjectiles().size() <= 1) {
                player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.no_types").withStyle(ChatFormatting.RED));
                return;
            }

            Item selected = BuiltInRegistries.ITEM.getOptional(message.selectedAmmo).orElse(null);
            if (selected == null) {
                player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.no_types").withStyle(ChatFormatting.RED));
                return;
            }
            if (!gun.acceptsProjectileItem(selected)) {
                player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.not_supported").withStyle(ChatFormatting.RED));
                return;
            }
            if (!player.isCreative() && Gun.findAmmo(player, selected).stack().isEmpty()) {
                player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.no_available").withStyle(ChatFormatting.RED));
                return;
            }

            Gun.setSelectedProjectileItem(stack, gun, selected);
            CompoundTag tag = getStackTag(stack);
            int loadedAmmo = tag.getIntOr("AmmoCount", 0);
            if (loadedAmmo > 0) {
                if (!player.isCreative()) {
                    Item loadedItem = Gun.getLoadedProjectileItem(stack, gun);
                    if (loadedItem != null) {
                        ItemStack returnedAmmo = new ItemStack(loadedItem, loadedAmmo);
                        if (!player.getInventory().add(returnedAmmo)) {
                            player.drop(returnedAmmo, false, net.minecraft.util.Prediction.SERVER_ONLY);
                        }
                    }
                }
                tag.putInt("AmmoCount", 0);
                setStackTag(stack, tag);
                Gun.clearLoadedProjectileItem(stack);
                PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateAmmo(0));
            }

            player.sendOverlayMessage(Component.translatable("message.scguns.ammo_swap.changed")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new ItemStack(selected).getHoverName().copy().withStyle(ChatFormatting.YELLOW)));
        });
        context.setHandled(true);
    }
}
