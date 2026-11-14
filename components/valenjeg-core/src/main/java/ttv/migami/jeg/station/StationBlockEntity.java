package ttv.migami.jeg.station;
import javax.annotation.Nullable;
import com.google.gson.*;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.item.GunItem;

/** Native persistence and sided inventory reused from MagazineLoaderBlockEntity.
 * Recycler state machine and station slots follow original JEG's AbstractRecyclerBlockEntity. */
public final class StationBlockEntity extends BlockEntity implements WorldlyContainer, ExtendedMenuProvider<String> {
    private static final int[] INPUT_SLOTS = {0,1}, OUTPUT_SLOTS = {2};
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int progress, duration, burnTime, burnDuration;
    private String activeRecipe = "";
    private float experience;
    private final ContainerData data = new ContainerData() {
        public int get(int i) { return switch(i) {case 0 -> progress; case 1 -> duration; case 2 -> burnTime; case 3 -> burnDuration; default -> 0;}; }
        public void set(int i,int v) {switch(i) {case 0 -> progress=v; case 1 -> duration=v; case 2 -> burnTime=v; case 3 -> burnDuration=v;}}
        public int getCount() {return 4;}
    };
    public StationBlockEntity(BlockPos p, BlockState s) { super(ModBlockEntities.STATION.get(),p,s); }
    public String station() { return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath(); }
    public ContainerData data() { return data; }
    public static boolean isBlueprint(ItemStack s, boolean advanced) {
        Identifier id=BuiltInRegistries.ITEM.getKey(s.getItem());
        return !s.isEmpty() && (id.equals(Identifier.parse("jeg:firearm_blueprint")) || advanced && id.equals(Identifier.parse("jeg:advanced_firearm_blueprint")));
    }
    public static String blueprintTarget(ItemStack stack) {return blueprintTarget(stack,StationRecipes.forStation("blueprint_workbench"));}
    public static String blueprintTarget(ItemStack stack, java.util.List<StationRecipe> definitions) {
        if (stack.getItem() instanceof GunItem) {
            String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return definitions.stream().anyMatch(r -> r.result().toString().equals(id) && (!JsonParser.parseString(r.json()).getAsJsonObject().has("copyable") || JsonParser.parseString(r.json()).getAsJsonObject().get("copyable").getAsBoolean())) ? id : null;
        }
        if (!isBlueprint(stack,false)) return null;
        String id=stack.get(ModDataComponents.BLUEPRINT_GUN.get());
        return id != null && definitions.stream().anyMatch(r -> r.result().toString().equals(id) && (!JsonParser.parseString(r.json()).getAsJsonObject().has("copyable") || JsonParser.parseString(r.json()).getAsJsonObject().get("copyable").getAsBoolean())) ? id : null;
    }
    public static ItemStack copyBlueprint(ItemStack source, ItemStack blank) {
        String target=blueprintTarget(source);
        if (target==null || !isBlueprint(blank,false)) return ItemStack.EMPTY;
        ItemStack output=blank.copyWithCount(1);
        output.set(ModDataComponents.BLUEPRINT_GUN.get(),target);
        return output;
    }
    public static boolean isFuel(ItemStack stack) {return stack.has(DataComponents.COOKING_FUEL);}
    public int fuelTicks(ItemStack stack) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server))return 0;
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(server)
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,getBlockState())
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY,this)
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.CONTAINER,this)
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CONTAINER_PROCESS);
        var context=new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(java.util.Optional.empty());
        return net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.getFromItem(stack,DataComponents.COOKING_FUEL,net.minecraft.world.item.component.CookingFuel::burnTime,context,0);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, StationBlockEntity station) {
        if (!station.station().equals("recycler")) return;
        boolean dirty=false;
        if (station.burnTime>0) {station.burnTime--;dirty=true;}
        StationRecipe recipe=StationRecipes.forStation("recycler").stream()
            .filter(r -> r.materials().get(0).matches(station.items.get(0)))
            .sorted(java.util.Comparator.comparing(r -> r.materials().get(0).tag())).findFirst().orElse(null);
        boolean valid=recipe!=null && station.canOutput(recipe.output());
        String next=recipe==null ? "" : recipe.id().toString();
        if (!station.activeRecipe.equals(next)) {station.progress=0; station.activeRecipe=next; dirty=true;}
        if (valid) {
            station.duration=recipe.ticks();
            if (station.burnTime==0 && station.fuelTicks(station.items.get(1))>0) {
                ItemStack fuel=station.items.get(1);
                station.burnTime=station.burnDuration=station.fuelTicks(fuel);
                var template=fuel.getItem().getCraftingRemainder();
                ItemStack remainder=template==null ? ItemStack.EMPTY : template.create();
                fuel.shrink(1);
                if (fuel.isEmpty()) station.items.set(1,remainder);
                dirty=true;
            }
            if (station.burnTime>0 && ++station.progress>=station.duration) {
                ItemStack output=recipe.output();
                if (station.items.get(2).isEmpty()) station.items.set(2,output); else station.items.get(2).grow(output.getCount());
                station.items.get(0).shrink(1); station.progress=0;
                station.experience+=recipe.experience();
                dirty=true;
            }
        } else if (station.progress>0) {station.progress=Math.max(0,station.progress-2);dirty=true;}
        if (dirty) station.setChanged();
    }
    private boolean canOutput(ItemStack result) {
        ItemStack output=items.get(2);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output,result) && output.getCount()+result.getCount()<=output.getMaxStackSize();
    }
    public float takeExperience() {float xp=experience;experience=0;setChanged();return xp;}
    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot != 2 && this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && slot == 2;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize(stack)) {
            stack.setCount(this.getMaxStackSize(stack));
        }
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (station().equals("recycler")) return slot == 0 ? StationRecipes.forStation("recycler").stream().anyMatch(r -> r.materials().get(0).matches(stack)) : slot == 1 && isFuel(stack);
        if (station().equals("schematic_station")) return false; // Original schematic station has no persistent/automated inventory.
        return slot == 0 && (station().equals("blueprint_workbench") ? isBlueprint(stack, true) : stack.getItem() instanceof net.minecraft.world.item.DyeItem);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.clearItems();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null && !this.level.isClientSide()) {
            if(this.level instanceof net.minecraft.server.level.ServerLevel server) {
                float xp=takeExperience();int amount=(int)xp+(server.getRandom().nextFloat()<xp%1 ? 1 : 0);
                if(amount>0)net.minecraft.world.entity.ExperienceOrb.award(server,net.minecraft.world.phys.Vec3.atCenterOf(pos),amount);
            }
            Containers.dropContents(this.level, pos, this);
            this.clearItems();
        }
        super.preRemoveSideEffects(pos, state);
    }


    public Component getDisplayName() {return Component.translatable("block."+(station().equals("mo_workbench") ? "mteg" : "jeg")+"."+station());}
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new StationMenu(id,inv,station().equals("schematic_station") ? new SimpleContainer(3) : this,data,ContainerLevelAccess.create(level,worldPosition),station(),StationRecipes.forStation(station().equals("schematic_station") ? "blueprint_workbench" : station()),this);
    }
    public String getScreenOpeningData(ServerPlayer player) {
        JsonObject json=new JsonObject();
        json.addProperty("station",station());
        JsonArray recipes=new JsonArray();
        for (StationRecipe r:StationRecipes.forStation(station().equals("schematic_station") ? "blueprint_workbench" : station())) {JsonObject o=JsonParser.parseString(r.json()).getAsJsonObject();o.addProperty("id",r.id().toString());recipes.add(o);}
        json.add("recipes",recipes);
        return json.toString();
    }
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);clearItems();
        in.child("Items").ifPresent(child -> ContainerHelper.loadAllItems(child,items));
        progress=in.getIntOr("Progress",0);duration=in.getIntOr("Duration",0);
        burnTime=in.getIntOr("BurnTime",0);burnDuration=in.getIntOr("BurnDuration",0);
        activeRecipe=in.getStringOr("ActiveRecipe","");experience=in.getFloatOr("Experience",0);
    }
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);ContainerHelper.saveAllItems(out.child("Items"),items);
        out.putInt("Progress",progress);out.putInt("Duration",duration);out.putInt("BurnTime",burnTime);
        out.putInt("BurnDuration",burnDuration);out.putString("ActiveRecipe",activeRecipe);out.putFloat("Experience",experience);
    }
    private void clearItems() {for(int i=0;i<items.size();i++)items.set(i,ItemStack.EMPTY);}
}
