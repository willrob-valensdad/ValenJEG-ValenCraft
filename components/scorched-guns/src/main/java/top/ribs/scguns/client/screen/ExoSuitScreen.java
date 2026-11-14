package top.ribs.scguns.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import top.ribs.scguns.compat.ArmorItem;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;

public class ExoSuitScreen extends AbstractContainerScreen<ExoSuitMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/exosuit_gui.png");

    public ExoSuitScreen(ExoSuitMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderUpgradeSlots(guiGraphics, x, y);

        renderUpgradeInfo(guiGraphics, x, y);
    }

    private void renderUpgradeSlots(GuiGraphicsExtractor guiGraphics, int x, int y) {
        ItemStack armorPiece = menu.getArmorPiece();

        int[][] slotPositions = {
                {97, 25},   // Upgrade slot 1 (top-left)
                {115, 25},  // Upgrade slot 2 (top-right)
                {97, 43},   // Upgrade slot 3 (bottom-left)
                {115, 43}   // Upgrade slot 4 (bottom-right)
        };

        for (int i = 0; i < 4; i++) {
            int slotX = x + slotPositions[i][0];
            int slotY = y + slotPositions[i][1];

            if (!menu.isUpgradeSlotEnabled(ExoSuitMenu.UPGRADE_SLOT_1 + i)) {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x80000000);
            } else {
                renderSlotTypeIndicator(guiGraphics, slotX, slotY, armorPiece, i);
            }
        }
    }

    private void renderSlotTypeIndicator(GuiGraphicsExtractor guiGraphics, int slotX, int slotY, ItemStack armorPiece, int slotIndex) {
        if (armorPiece.isEmpty() || !(armorPiece.getItem() instanceof ExoSuitItem exosuit)) {
            return;
        }

        int color = getSlotTypeColor(exosuit.getType(), slotIndex);
        if (color != 0) {
            guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, color);
        }
    }
    private int getSlotTypeColor(ArmorItem.Type armorType, int slotIndex) {
        return switch (armorType) {
            case HELMET -> switch (slotIndex) {
                case 0 -> 0x20FF0000; // Red tint for plating
                case 1 -> 0x2000FF00; // Green tint for HUD
                case 2 -> 0x200000FF; // Blue tint for breathing
                default -> 0;
            };
            case CHESTPLATE -> switch (slotIndex) {
                case 0 -> 0x20FF0000; // Red tint for plating
                case 1 -> 0x20FFFF00; // Yellow tint for pauldrons
                case 2 -> 0x20FF00FF; // Magenta tint for power core
                case 3 -> 0x2000FFFF; // Cyan tint for utility
                default -> throw new IllegalStateException("Unexpected value: " + slotIndex);
            };
            case LEGGINGS -> switch (slotIndex) {
                case 0 -> 0x20FF0000; // Red tint for plating
                case 1 -> 0x20808080; // Gray tint for knee guards
                case 2 -> 0x2000FFFF; // Cyan tint for utility
                default -> 0;
            };
            case BOOTS -> switch (slotIndex) {
                case 0 -> 0x20FF0000; // Red tint for plating
                case 1 -> 0x2000FF00; // Green tint for mobility
                default -> 0;
            };
            default -> 0;
        };
    }

    private void renderUpgradeInfo(GuiGraphicsExtractor guiGraphics, int x, int y) {
        ItemStack armorPiece = menu.getArmorPiece();

        if (!armorPiece.isEmpty() && armorPiece.getItem() instanceof ExoSuitItem exosuit) {
            int centerX = x + 26 + 9;
            int textY = y + 20;

            int maxSlots = exosuit.getMaxUpgradeSlots();
            int usedSlots = exosuit.getCurrentUpgradeCount(armorPiece);
            String slotInfo = usedSlots + "/" + maxSlots + "Slots";

            int textWidth = this.font.width(slotInfo);
            int textX = centerX - (textWidth / 2);

            guiGraphics.text(this.font, slotInfo, textX, textY, 0xFF404040, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        super.extractTooltip(guiGraphics, x, y);

        renderUpgradeSlotTooltips(guiGraphics, x, y);
    }
    private void renderUpgradeSlotTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        ItemStack armorPiece = menu.getArmorPiece();
        if (armorPiece.isEmpty()) return;

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int[][] slotPositions = {
                {97, 25}, {115, 25}, {97, 43}, {115, 43}
        };

        for (int i = 0; i < 4; i++) {
            int slotX = x + slotPositions[i][0];
            int slotY = y + slotPositions[i][1];

            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                Slot upgradeSlot = menu.getSlot(ExoSuitMenu.EXOSUIT_INVENTORY_FIRST_SLOT_INDEX + ExoSuitMenu.UPGRADE_SLOT_1 + i);

                if (!menu.isUpgradeSlotEnabled(ExoSuitMenu.UPGRADE_SLOT_1 + i)) {
                    guiGraphics.setTooltipForNextFrame(this.font, Component.literal("Slot not available"), mouseX, mouseY);
                } else if (upgradeSlot.getItem().isEmpty()) {
                    String slotType = getSlotTypeTooltip(((ExoSuitItem)armorPiece.getItem()).getType(), i);
                    guiGraphics.setTooltipForNextFrame(this.font, Component.literal(slotType), mouseX, mouseY);
                }
                break;
            }
        }
    }

    private String getSlotTypeTooltip(ArmorItem.Type armorType, int slotIndex) {
        return switch (armorType) {
            case HELMET -> switch (slotIndex) {
                case 0 -> "Plating Slot";
                case 1 -> "HUD Slot";
                case 2 -> "Breathing Apparatus Slot";
                default -> "Upgrade Slot";
            };
            case CHESTPLATE -> switch (slotIndex) {
                case 0 -> "Plating Slot";
                case 1 -> "Pauldron Slot";
                case 2 -> "Power Core Slot";
                case 3 -> "Utility Slot";
                default -> throw new IllegalStateException("Unexpected value: " + slotIndex);
            };
            case LEGGINGS -> switch (slotIndex) {
                case 0 -> "Plating Slot";
                case 1 -> "Knee Guard / Plating Slot";
                case 2 -> "Utility Slot";
                default -> "Upgrade Slot";
            };
            case BOOTS -> switch (slotIndex) {
                case 0 -> "Plating Slot";
                case 1 -> "Mobility Enhancement Slot";
                default -> "Upgrade Slot";
            };
            default -> "Upgrade Slot";
        };
    }
}
