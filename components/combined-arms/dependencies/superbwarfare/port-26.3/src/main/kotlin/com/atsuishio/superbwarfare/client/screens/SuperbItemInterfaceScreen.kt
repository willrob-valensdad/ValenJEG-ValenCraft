package com.atsuishio.superbwarfare.client.screens

import com.atsuishio.superbwarfare.inventory.menu.SuperbItemInterfaceMenu
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
class SuperbItemInterfaceScreen(menu: SuperbItemInterfaceMenu, playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<SuperbItemInterfaceMenu>(menu, playerInventory, title) {
    init {
        this.imageHeight = 133
        this.inventoryLabelY = this.imageHeight - 94
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick)
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun extractContents(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val i = (this.width - this.imageWidth) / 2
        val j = (this.height - this.imageHeight) / 2
        guiGraphics.blit(HOPPER_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight)
    }

    companion object {
        private val HOPPER_LOCATION = Identifier.withDefaultNamespace("textures/gui/container/hopper.png")
    }
}
