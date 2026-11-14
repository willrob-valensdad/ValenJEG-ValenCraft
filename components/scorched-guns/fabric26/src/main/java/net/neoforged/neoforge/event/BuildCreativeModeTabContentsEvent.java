/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.neoforge.event;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class BuildCreativeModeTabContentsEvent {
    private final ResourceKey<CreativeModeTab> tabKey;
    private final CreativeModeTab.ItemDisplayParameters parameters;
    private final CreativeModeTab.Output output;

    public BuildCreativeModeTabContentsEvent(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        this.tabKey = tabKey;
        this.parameters = parameters;
        this.output = output;
    }

    public ResourceKey<CreativeModeTab> getTabKey() { return tabKey; }
    public CreativeModeTab.ItemDisplayParameters getParameters() { return parameters; }
    public Set<ItemStack> getParentEntries() { return Set.of(); }
    public Set<ItemStack> getSearchEntries() { return Set.of(); }

    public void accept(ItemLike itemLike) { accept(new ItemStack(itemLike), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS); }
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) { output.accept(stack, visibility); }
}
