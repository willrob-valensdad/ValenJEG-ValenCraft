package top.ribs.scguns.event;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.*;

@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ArmorRenderHandler {

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre event) {
        if (event.getRenderer().getModel() instanceof PlayerModel playerModel
                && event.getRenderState() instanceof HumanoidRenderState humanoidState) {
            hideSecondLayerForCustomArmor(humanoidState, playerModel);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post event) {
        if (event.getRenderer().getModel() instanceof PlayerModel playerModel) {
            restoreSecondLayer(playerModel);
        }
    }

    private static void hideSecondLayerForCustomArmor(HumanoidRenderState renderState, PlayerModel playerModel) {
        if (isCustomArmor(renderState.headEquipment)) {
            hideSecondLayerForSlot(playerModel, EquipmentSlot.HEAD);
        }
        if (isCustomArmor(renderState.chestEquipment)) {
            hideSecondLayerForSlot(playerModel, EquipmentSlot.CHEST);
        }
        if (isCustomArmor(renderState.legsEquipment)) {
            hideSecondLayerForSlot(playerModel, EquipmentSlot.LEGS);
        }
    }

    private static boolean isCustomArmor(ItemStack armorStack) {
        return armorStack.getItem() instanceof AdrienArmorItem
                || armorStack.getItem() instanceof AnthraliteArmorItem
                || armorStack.getItem() instanceof AnthraliteGasMaskArmorItem
                || armorStack.getItem() instanceof NetheriteGasMaskArmorItem
                || armorStack.getItem() instanceof ExoSuitItem
                || armorStack.getItem() instanceof DiamondSteelArmorItem;
    }

    private static void hideSecondLayerForSlot(PlayerModel playerModel, EquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                playerModel.hat.visible = false;
                break;
            case CHEST:
                playerModel.jacket.visible = false;
                playerModel.leftSleeve.visible = false;
                playerModel.rightSleeve.visible = false;
                break;
            case LEGS:
                playerModel.leftPants.visible = false;
                playerModel.rightPants.visible = false;
                break;
        }
    }

    private static void restoreSecondLayer(PlayerModel playerModel) {
        playerModel.hat.visible = true;
        playerModel.jacket.visible = true;
        playerModel.leftSleeve.visible = true;
        playerModel.rightSleeve.visible = true;
        playerModel.leftPants.visible = true;
        playerModel.rightPants.visible = true;
    }
}
