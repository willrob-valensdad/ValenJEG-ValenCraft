package ttv.migami.jeg.legacy;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModItems;

public final class AmmoBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int SIZE = 15;
    private static final int[] SLOTS = IntStream.range(0, SIZE).toArray();
    private static final TagKey<net.minecraft.world.item.Item> AMMO = TagKey.create(Registries.ITEM, Reference.id("ammo"));
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int openCount;
    private final NonNullList<ItemStack> legacyOverflow = NonNullList.withSize(12, ItemStack.EMPTY);
    public AmmoBoxBlockEntity(BlockPos pos, BlockState state) { super(LegacyContent.AMMO_BOX_ENTITY, pos, state); }
    public static boolean isAmmo(ItemStack stack) { return !stack.isEmpty() && (stack.is(AMMO) || java.util.Set.of("minecraft:arrow","minecraft:tipped_arrow","minecraft:spectral_arrow","minecraft:echo_shard","minecraft:sculk_catalyst","minecraft:water_bucket","minecraft:lava_bucket","minecraft:bucket","jeg:rocket_launcher","jeg:explosive_charge","jeg:pocket_bubble").contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) || ModItems.AMMO.values().stream().anyMatch(item -> stack.is(item.get()))); }
    @Override public boolean isEmpty() { return super.isEmpty() && legacyOverflow.stream().allMatch(ItemStack::isEmpty); }
    @Override public int getContainerSize() { return SIZE; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("container.jeg.ammo_box"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new AmmoBoxMenu(id, inventory, this); }
    @Override public int[] getSlotsForFace(Direction side) { return SLOTS.clone(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return isAmmo(stack); }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) { NonNullList<ItemStack> saved=NonNullList.withSize(27, ItemStack.EMPTY); ContainerHelper.loadAllItems(input,saved); restoreLegacyItems(saved); }
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, savedItems());
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        NonNullList<ItemStack> saved=NonNullList.withSize(27, ItemStack.EMPTY);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(saved);
        restoreLegacyItems(saved);
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(savedItems()));
    }
    @Override public void removeComponentsFromTag(ValueOutput output) { super.removeComponentsFromTag(output); output.discard("Items"); }
    private void restoreLegacyItems(NonNullList<ItemStack> saved) {
        for(int i=0;i<SIZE;i++) items.set(i,saved.get(i));
        for(int i=0;i<12;i++) legacyOverflow.set(i,saved.get(SIZE+i));
    }
    private NonNullList<ItemStack> savedItems() {
        NonNullList<ItemStack> saved=NonNullList.withSize(27,ItemStack.EMPTY);
        for(int i=0;i<SIZE;i++) saved.set(i,items.get(i));
        for(int i=0;i<12;i++) saved.set(SIZE+i,legacyOverflow.get(i));
        return saved;
    }
    /** Original hidden slots15-26 survive NBT/portable components until first server opening. */
    public void releaseLegacyOverflow() {
        if(level==null || level.isClientSide()) return;
        for(int i=0;i<legacyOverflow.size();i++) {
            ItemStack stack=legacyOverflow.get(i);
            if(!stack.isEmpty()) { net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,stack); legacyOverflow.set(i,ItemStack.EMPTY); }
        }
        setChanged();
    }
    @Override public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!(user instanceof Player player) || isRemoved() || player.isSpectator()) return;
        if (openCount++ == 0) { level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition); level.playSound(null, worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, .5F, .9F + level.getRandom().nextFloat() * .1F); }
    }
    @Override public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!(user instanceof Player player) || isRemoved() || player.isSpectator()) return;
        if (openCount > 0 && --openCount == 0) { level.gameEvent(player, GameEvent.CONTAINER_CLOSE, worldPosition); level.playSound(null, worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, .5F, .9F + level.getRandom().nextFloat() * .1F); }
    }
}
