package top.ribs.scguns.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.compat.RecipeIngredientCompat;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageSetBlueprintRecipe;
import top.ribs.scguns.util.BlueprintRecipeData;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BlueprintScreen extends Screen {
    public static final Identifier BLUEPRINT_TEXTURE = Identifier.fromNamespaceAndPath("scguns", "textures/gui/blueprint.png");

    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private static final int SLOT_SIZE = 16;
    private final List<RecipeSlot> recipeSlots = new ArrayList<>();
    private final InteractionHand hand;
    private final ItemStack[] recipeItems = new ItemStack[12];

    private int currentPage = 0;
    private int maxPages = 0;
    private BlueprintPageButton nextPageButton;
    private BlueprintPageButton prevPageButton;

    private final ItemStack blueprintStack;
    private final List<RecipeHolder<GunBenchRecipe>> availableRecipes = new ArrayList<>();

    private static final List<String> GUN_ORDER = Arrays.asList(
            // ANTIQUE
            "flintlock_pistol", "handcannon", "musket", "blunderbuss", "fencer_carabine", "fencer_thumper", "doublet", "repeating_musket", "longarm",
            "laser_musket", "teslock_rifle", "plasmabuss",

            //FRONTIER
            "pax", "winnie", "winnie_millend", "callwell", "callwell_conversion", "callwell_terminal", "saketini",
            "red_raydar", "kiln_gun", "big_bore",

            // COPPER
            "scrapper", "rusty_gnat", "umax_pistol", "makeshift_rifle", "boomstick", "bruiser",
            "llr_director", "birdfeeder", "whistler", "blooper", "arc_worker",

            // IRON
            "defender_pistol", "trenchur", "greaser_smg", "m3_carabine", "m3_marksman","combat_shotgun", "venturi",
            "iron_javelin", "iron_spear", "auvtomag", "pulsar", "gyrojet_pistol", "brawler",
            "crusader", "mk43_rifle", "triquetra", "rocket_rifle", "ultra_knight_hawk",

            // OCEAN
            "floundergat", "marlin", "bomb_lance", "hullbreaker", "sequoia", "spirulida", "hyperbaria",

            // WRECKER
            "mokova", "mak_mkii", "railworker", "whizzbanger", "turnpike", "killer_23", "homemaker", "kalaskah", "basker", "tl_runner", "stigg",

            // DIAMOND STEEL
            "krauser", "soul_drummer", "uppercut", "micina", "valora", "prush_gun", "drill", "drill_conversion",
            "lockewood", "rg_jigsaw", "nailer", "inertial", "minksy", "mas_peddler", "mas_55", "inquisitor",
            "plasgun", "cyclone", "zilk_45", "truant", "shard_culler",

            // TREATED BRASS
            "m22_waltz", "waltz_conversion", "osgood_50", "grandle_og", "grandle", "cogloader", "gale", "jr_wristbreaker", "jackhammer",
            "hammer_gl", "howler", "howler_conversion", "gauss_rifle", "libertas", "niami", "spitfire", "gattaler",
            "thunderhead", "scratches", "cr4k_mining_laser", "dozier_rl",

            // PIGLIN
            "empty_blasphemy", "pyroclastic_flow", "freyr", "mangalitsa", "vulcanic_repeater", "super_shotgun",

            //DEEPDARK
            "whispers", "echoes_2", "sculk_resonator", "forlorn_hope",

            // END
            "carapice", "shellurker", "weevil", "dark_matter", "lone_wonder", "raygun",

            // SCORCHED
                "prima_materia", "rat_king_and_queen", "locust", "newborn_cyst", "earths_corpse",
            "sterilizer", "flayed_god", "nervepinch", "terra_incognita", "astella",

            //EXOSUIT
            "exo_suit_helmet", "exo_suit_chestplate", "exo_suit_leggings", "exo_suit_boots"
    );

    private record RecipeSlot(int x, int y, int index) {
    }

    public static class BlueprintPageButton extends Button.Plain {
        public BlueprintPageButton(int x, int y, Component text, OnPress onPress) {
            super(x, y, 20, 20, text, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            Minecraft.getInstance().player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        }
    }

    public BlueprintScreen(ItemStack blueprintStack, Player player, InteractionHand hand, List<RecipeHolder<GunBenchRecipe>> syncedRecipes) {
        super(Component.translatable("screen.scguns.blueprint.title"));

        this.blueprintStack = blueprintStack;
        this.hand = hand;
        Arrays.fill(recipeItems, ItemStack.EMPTY);
        loadAvailableRecipes(syncedRecipes);
        loadActiveRecipeAsCurrentPage();
    }

    /**
     * If there's an active recipe set for this blueprint, find it in the available recipes
     * and set the current page to that recipe.
     */
    private void loadActiveRecipeAsCurrentPage() {
        Identifier activeRecipeId = getActiveRecipe(blueprintStack);
        if (activeRecipeId != null) {
            for (int i = 0; i < availableRecipes.size(); i++) {
                if (availableRecipes.get(i).id().identifier().equals(activeRecipeId)) {
                    currentPage = i;
                    loadCurrentPageRecipe();
                    return;
                }
            }
        }
        loadCurrentPageRecipe();
    }

    private void loadCurrentPageRecipe() {
        if (currentPage < availableRecipes.size()) {
            RecipeHolder<GunBenchRecipe> recipe = availableRecipes.get(currentPage);
            loadRecipeIntoSlots(recipe);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.createMenuControls();
        this.setupRecipeSlots();
    }

    private void setupRecipeSlots() {
        recipeSlots.clear();

        int centerX = (this.width - IMAGE_WIDTH) / 2;
        int centerY = 2;

        recipeSlots.add(new RecipeSlot(centerX + 26, centerY + 17, 0)); // SLOT_TOP_INTERNAL_1
        recipeSlots.add(new RecipeSlot(centerX + 44, centerY + 17, 1)); // SLOT_TOP_INTERNAL_2
        recipeSlots.add(new RecipeSlot(centerX + 62, centerY + 17, 2)); // SLOT_TOP_BARREL_1
        recipeSlots.add(new RecipeSlot(centerX + 80, centerY + 17, 3)); // SLOT_TOP_BARREL_2
        recipeSlots.add(new RecipeSlot(centerX + 26, centerY + 35, 4)); // SLOT_INTERNAL_1
        recipeSlots.add(new RecipeSlot(centerX + 44, centerY + 35, 5)); // SLOT_INTERNAL_2
        recipeSlots.add(new RecipeSlot(centerX + 62, centerY + 35, 6)); // SLOT_BARREL_1
        recipeSlots.add(new RecipeSlot(centerX + 80, centerY + 35, 7)); // SLOT_BARREL_2
        recipeSlots.add(new RecipeSlot(centerX + 26, centerY + 53, 8)); // SLOT_GRIP
        recipeSlots.add(new RecipeSlot(centerX + 62, centerY + 53, 9)); // SLOT_MAGAZINE
        recipeSlots.add(new RecipeSlot(centerX + 116, centerY + 17, 11)); // SLOT_BLUEPRINT
        recipeSlots.add(new RecipeSlot(centerX + 140, centerY + 44, 10)); // SLOT_OUTPUT
    }

    protected void createMenuControls() {
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose())
                        .bounds(this.width / 2 - 100, 196, 200, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.scguns.blueprint.button.set_recipe"), (button) -> this.setActiveRecipe())
                        .bounds(this.width / 2 - 100, 220, 200, 20)
                        .build()
        );

        createPageControls();
    }

    private void setActiveRecipe() {
        if (currentPage < availableRecipes.size()) {
            RecipeHolder<GunBenchRecipe> currentRecipe = availableRecipes.get(currentPage);

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageSetBlueprintRecipe(hand, currentRecipe.id().identifier().toString()));

            saveActiveRecipe(blueprintStack, currentRecipe.id().identifier());
        }
        this.onClose();
    }

    private void createPageControls() {
        int centerX = (this.width - IMAGE_WIDTH) / 2;
        int centerY = 2;

        int buttonY = centerY + IMAGE_HEIGHT - 40;
        int rightSide = centerX + IMAGE_WIDTH - 35;

        this.prevPageButton = this.addRenderableWidget(
                new BlueprintPageButton(rightSide - 40, buttonY, Component.literal("<"),
                        (button) -> this.previousPage())
        );

        this.nextPageButton = this.addRenderableWidget(
                new BlueprintPageButton(rightSide - 15, buttonY, Component.literal(">"),
                        (button) -> this.nextPage())
        );

        updatePageButtonStates();
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCurrentPageRecipe();
            updatePageButtonStates();
        }
    }

    private void nextPage() {
        if (currentPage < maxPages - 1) {
            currentPage++;
            loadCurrentPageRecipe();
            updatePageButtonStates();
        }
    }

    private void updatePageButtonStates() {
        if (prevPageButton != null) {
            prevPageButton.active = currentPage > 0;
            prevPageButton.visible = true;
        }
        if (nextPageButton != null) {
            nextPageButton.active = currentPage < maxPages - 1;
            nextPageButton.visible = true;
        }
    }

    private void loadAvailableRecipes(List<RecipeHolder<GunBenchRecipe>> syncedRecipes) {
        availableRecipes.clear();
        if (syncedRecipes != null) {
            availableRecipes.addAll(syncedRecipes);
        }
        sortRecipesByProgression();

        maxPages = Math.max(1, availableRecipes.size());
        currentPage = 0;
    }

    /**
     * Sorts available recipes based on the hardcoded GUN_ORDER list.
     * Items not in the list will be placed at the end in their original order.
     */
    private void sortRecipesByProgression() {
        availableRecipes.sort((recipe1, recipe2) -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return 0;

            String item1Name = getItemNameFromRecipe(recipe1, level);
            String item2Name = getItemNameFromRecipe(recipe2, level);

            int index1 = getOrderIndex(item1Name);
            int index2 = getOrderIndex(item2Name);

            return Integer.compare(index1, index2);
        });
    }

    /**
     * Extracts the item name from a recipe's result item
     */
    private String getItemNameFromRecipe(RecipeHolder<GunBenchRecipe> recipe, Level level) {
        ItemStack resultItem = recipe.value().getResultItem(level.registryAccess());
        if (!resultItem.isEmpty()) {
            Identifier itemLocation = BuiltInRegistries.ITEM.getKey(resultItem.getItem());
            if (itemLocation != null) {
                return itemLocation.getPath();
            }
        }
        return "";
    }

    /**
     * Gets the order index for an item name. Items not in the hardcoded list
     * get a high index (placed at the end).
     */
    private int getOrderIndex(String itemName) {
        int index = GUN_ORDER.indexOf(itemName);
        return index == -1 ? Integer.MAX_VALUE : index;
    }

    private void loadRecipeIntoSlots(RecipeHolder<GunBenchRecipe> recipeHolder) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        clearRecipeItems();

        GunBenchRecipe recipe = recipeHolder.value();
        for (int i = 0; i < recipe.getIngredients().size() && i < 10; i++) {
            Ingredient ingredient = recipe.getIngredients().get(i);
            if (!RecipeIngredientCompat.isEmpty(ingredient)) {
                ItemStack displayStack = ingredient.items()
                        .findFirst()
                        .map(ItemStack::new)
                        .orElse(ItemStack.EMPTY);
                if (!displayStack.isEmpty()) {
                    recipeItems[i] = displayStack;
                }
            }
        }

        if (!RecipeIngredientCompat.isEmpty(recipe.getBlueprint())) {
            ItemStack blueprintDisplayStack = recipe.getBlueprint().items()
                    .findFirst()
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            if (!blueprintDisplayStack.isEmpty()) {
                recipeItems[11] = blueprintDisplayStack;
            }
        }

        recipeItems[10] = recipe.getResultItem(level.registryAccess()).copy();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = (this.width - IMAGE_WIDTH) / 2;
        int centerY = 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BLUEPRINT_TEXTURE, centerX, centerY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

        renderRecipeSlots(guiGraphics, mouseX, mouseY);
        renderGunInfo(guiGraphics, centerX, centerY);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        renderItemTooltips(guiGraphics, mouseX, mouseY);
    }


    private void renderGunInfo(GuiGraphicsExtractor guiGraphics, int centerX, int centerY) {
        if (currentPage < availableRecipes.size()) {
            GunBenchRecipe recipe = availableRecipes.get(currentPage).value();
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            ItemStack resultItem = recipe.getResultItem(level.registryAccess());
            if (!resultItem.isEmpty()) {
                String gunName = resultItem.getItemName().getString();
                int titleY = centerY + 77;

                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().scale(1.1f);

                int scaledX = (int) ((centerX + 30) / 1.1f);
                int scaledY = (int) (titleY / 1.1f);

                guiGraphics.text(this.font, gunName, scaledX, scaledY, 0xFF212057, false);
                guiGraphics.pose().popMatrix();

                int titleHeight = (int) (this.font.lineHeight * 1.1f);
                int descriptionY = titleY + titleHeight + 6;
                Identifier gunRegistryId = BuiltInRegistries.ITEM.getKey(resultItem.getItem());
                String descriptionKey = "scguns.desc." + gunRegistryId.getPath();
                String description = Component.translatable(descriptionKey).getString();
                if (description.equals(descriptionKey)) {
                    description = Component.translatable("scguns.desc.unknown").getString();
                }

                int maxWidth = IMAGE_WIDTH - 30;
                List<String> wrappedLines = wrapText(description, maxWidth);

                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().scale(0.9f);

                for (int i = 0; i < wrappedLines.size(); i++) {
                    int scaledDescX = (int) ((centerX + 22) / 0.9f);
                    int scaledDescY = (int) ((descriptionY + (i * this.font.lineHeight)) / 0.9f);

                    guiGraphics.text(this.font, wrappedLines.get(i),
                            scaledDescX, scaledDescY, 0xFF4A496B, false);
                }

                guiGraphics.pose().popMatrix();
            }
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (this.font.width(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void renderRecipeSlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        for (RecipeSlot slot : recipeSlots) {
            boolean isHovered = isMouseOverSlot(slot, mouseX, mouseY);

            if (isHovered) {
                guiGraphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, 0x80FFFFFF);
            }

            ItemStack itemStack = recipeItems[slot.index];
            if (!itemStack.isEmpty()) {
                guiGraphics.item(itemStack, slot.x, slot.y);
                guiGraphics.itemDecorations(this.font, itemStack, slot.x, slot.y);
            }
        }
    }

    private void renderItemTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        for (RecipeSlot slot : recipeSlots) {
            if (isMouseOverSlot(slot, mouseX, mouseY)) {
                ItemStack itemStack = recipeItems[slot.index];
                if (!itemStack.isEmpty()) {
                    guiGraphics.setTooltipForNextFrame(this.font, itemStack, mouseX, mouseY);
                }
                break;
            }
        }
    }

    private boolean isMouseOverSlot(RecipeSlot slot, int mouseX, int mouseY) {
        return mouseX >= slot.x && mouseX < slot.x + SLOT_SIZE &&
                mouseY >= slot.y && mouseY < slot.y + SLOT_SIZE;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }

        return switch (event.key()) {
            case 266 -> {
                if (this.prevPageButton.active) {
                    this.prevPageButton.onPress(event);
                }
                yield true;
            }
            case 267 -> {
                if (this.nextPageButton.active) {
                    this.nextPageButton.onPress(event);
                }
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void clearRecipeItems() {
        Arrays.fill(recipeItems, ItemStack.EMPTY);
    }

    private void saveActiveRecipe(ItemStack blueprint, Identifier recipeId) {
        BlueprintRecipeData.saveActiveRecipe(blueprint, recipeId);
    }

    public static Identifier getActiveRecipe(ItemStack blueprint) {
        return BlueprintRecipeData.getActiveRecipe(blueprint);
    }

    public static String getActiveRecipeName(ItemStack blueprint) {
        Level level = Minecraft.getInstance().level;
        return BlueprintRecipeData.getActiveRecipeName(blueprint, level);
    }
}
