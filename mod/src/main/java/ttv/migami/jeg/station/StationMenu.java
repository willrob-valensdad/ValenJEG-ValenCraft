package ttv.migami.jeg.station;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.AttachmentType;
import ttv.migami.jeg.item.attachment.GunAttachments;

/** Ports original AbstractWorkbenchContainer / SchematicStationMenu / RecyclerMenu slots.
 * Server clicks reuse vanilla menu-button transport with a per-open immutable recipe snapshot. */
public final class StationMenu extends AbstractContainerMenu {
    public final String station;
    public final List<StationRecipe> recipes;
    public final Container inventory;
    public final ContainerData data;
    public final Inventory playerInventory;
    private final ContainerLevelAccess access;
    private final int stationSlots;
    private final StationBlockEntity owner;
    private boolean updating;
    private final ResultContainer schematicResult=new ResultContainer();
    public StationMenu(int id,Inventory inv,String opening) {
        this(id,inv,new SimpleContainer(3),new SimpleContainerData(4),ContainerLevelAccess.NULL,
            JsonParser.parseString(opening).getAsJsonObject().get("station").getAsString(),decode(opening));
    }
    private static List<StationRecipe> decode(String opening) {
        List<StationRecipe> result=new ArrayList<>();
        for(JsonElement e:JsonParser.parseString(opening).getAsJsonObject().getAsJsonArray("recipes")) {
            JsonObject o=e.getAsJsonObject();result.add(StationRecipe.parse(Identifier.parse(o.get("id").getAsString()),o));
        }
        return List.copyOf(result);
    }
    public StationMenu(int id,Inventory inv,Container container,ContainerData data,ContainerLevelAccess access,String station,List<StationRecipe> recipes) {
        this(id,inv,container,data,access,station,recipes,container instanceof StationBlockEntity be ? be : null);
    }
    public StationMenu(int id,Inventory inv,Container container,ContainerData data,ContainerLevelAccess access,String station,List<StationRecipe> recipes,StationBlockEntity owner) {
        super(ModMenuTypes.STATION.get(),id);
        this.station=station;this.recipes=List.copyOf(recipes);this.inventory=container;this.playerInventory=inv;this.data=data;this.access=access;this.owner=owner;
        boolean workbench=station.endsWith("workbench");
        stationSlots=workbench ? 1 : 3;
        if (workbench) {
            addSlot(new Slot(container,0,174,18) {
                public boolean mayPlace(ItemStack s) {return station.equals("blueprint_workbench") ? StationBlockEntity.isBlueprint(s,true) : s.getItem() instanceof DyeItem;}
                public int getMaxStackSize() {return 1;}
            });
        } else {
            boolean schematic=station.equals("schematic_station");
            addSlot(new Slot(container,0,schematic ? 15 : 56,schematic ? 52 : 17) {
                public boolean mayPlace(ItemStack s) {return schematic ? StationBlockEntity.blueprintTarget(s,recipes)!=null : recipes.stream().anyMatch(r -> r.materials().get(0).matches(s));}
            });
            addSlot(new Slot(container,1,schematic ? 15 : 56,schematic ? 15 : 53) {
                public boolean mayPlace(ItemStack s) {return schematic ? StationBlockEntity.isBlueprint(s,false) : StationBlockEntity.isFuel(s);}
            });
            addSlot(new Slot(schematic ? schematicResult : container,schematic ? 0 : 2,schematic ? 145 : 116,35) {
                public boolean mayPlace(ItemStack s) {return false;}
                public void onTake(Player player,ItemStack result) {
                    if (schematic) {StationMenu.this.inventory.removeItem(1,1);schematicResult.setItem(0,ItemStack.EMPTY);}
                    else if (container instanceof StationBlockEntity be && player.level() instanceof ServerLevel level) {
                        float xp=be.takeExperience();int n=(int)xp+(level.getRandom().nextFloat()<xp%1 ? 1 : 0);
                        if(n>0)ExperienceOrb.award(level,player.position(),n);
                    }
                    super.onTake(player,result);slotsChanged(container);
                }
            });
        }
        int y=workbench ? 102 : 84;
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,8+col*18,y+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,y+58));
        addDataSlots(data);
    }
    @Override public void broadcastChanges() {slotsChanged(inventory);super.broadcastChanges();}
    @Override public void slotsChanged(Container c) {
        if(!station.equals("schematic_station") || updating || playerInventory.player.level().isClientSide())return;
        updating=true;
        ItemStack result=StationBlockEntity.copyBlueprint(inventory.getItem(0),inventory.getItem(1));
        if(!ItemStack.matches(result,schematicResult.getItem(0)))schematicResult.setItem(0,result);
        updating=false;
    }
    public String blueprintTarget(ItemStack stack) {return StationBlockEntity.blueprintTarget(stack,recipes);}
    public boolean blueprintAllows(StationRecipe recipe) {
        return !station.equals("blueprint_workbench") || recipe.result().toString().equals(inventory.getItem(0).get(ModDataComponents.BLUEPRINT_GUN.get()));
    }
    @Override public boolean clickMenuButton(Player player,int index) {
        if (player.level().isClientSide() || player.containerMenu!=this || !stillValid(player) || !station.endsWith("workbench") || index<0 || index>=recipes.size())return false;
        StationRecipe snapshot=recipes.get(index), recipe=StationRecipes.get(snapshot.id());
        if(recipe==null || !recipe.json().equals(snapshot.json()) || !recipe.station().equals(station) || !blueprintAllows(recipe))return false;
        int[] plan=recipe.plan(playerInventory);
        if(plan==null)return false;
        ItemStack output=recipe.output();
        // Original dye slot is optional and reusable; carry it through the modern attachment component.
        if(output.getItem() instanceof GunItem && inventory.getItem(0).getItem() instanceof DyeItem) {
            // Persist the dye id and the stored stack together. A string-only write reloads as a fresh dye and drops the stack component.
            boolean colored=GunAttachments.setCosmetic(output, AttachmentType.DYE, inventory.getItem(0).copy());
            // Consume the optional dye only when the authored output has tinted geometry.
            if(colored && station.equals("mo_workbench")) inventory.setItem(0,ItemStack.EMPTY);
        }
        for(int i=0;i<plan.length;i++)if(plan[i]>0)playerInventory.removeItem(i,plan[i]);
        playerInventory.setChanged();
        access.execute((level,pos)->Containers.dropItemStack(level,pos.getX()+0.5,pos.getY()+1.125,pos.getZ()+0.5,output));
        // Reuse the guide's existing successful-action trigger; removed vanilla recipes must not remain registry holders.
        ttv.migami.jeg.advancement.GameplayActions.action(player,"station_craft",recipe.result());
        broadcastChanges();return true;
    }
    @Override public void removed(Player player) {
        super.removed(player);
        if(station.equals("schematic_station") && !player.level().isClientSide()) {
            schematicResult.clearContent();
            clearContainer(player,inventory);
        }
    }
    @Override public boolean stillValid(Player player) {
        return player.level().isClientSide() || access.evaluate((level,pos)->owner!=null && level.getBlockEntity(pos)==owner && BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).equals(Identifier.parse((station.equals("mo_workbench") ? "mteg:" : "jeg:")+station)) && player.distanceToSqr(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5)<=64,false);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0 || index>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index<stationSlots) {if(!moveItemStackTo(stack,stationSlots,stationSlots+36,true))return ItemStack.EMPTY;}
        else {
            boolean moved=false;
            for(int target=0;target<Math.min(2,stationSlots);target++)if(slots.get(target).mayPlace(stack) && moveItemStackTo(stack,target,target+1,false)) {moved=true;break;}
            if(!moved) {
                int split=stationSlots+27;
                if(index<split ? !moveItemStackTo(stack,split,stationSlots+36,false) : !moveItemStackTo(stack,stationSlots,split,false))return ItemStack.EMPTY;
            }
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        if(stack.getCount()==copy.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack);slotsChanged(inventory);return copy;
    }
}
