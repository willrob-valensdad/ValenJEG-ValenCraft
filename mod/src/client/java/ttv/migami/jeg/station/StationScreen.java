package ttv.migami.jeg.station;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.AttachmentItem;
import ttv.migami.jeg.init.ModItems;

/** 26.3 extraction adapter for original JEG workbench, schematic and recycler screens.
 * Original artwork, slot positions, category order, recipe paging and material rows retained. */
public final class StationScreen extends AbstractContainerScreen<StationMenu> {
    private static boolean showRemaining;
    private final boolean workbench;
    private final boolean blueprint;
    private final List<String> tabs=new ArrayList<>();
    private int tab,index;
    private Button craft;
    private List<StationRecipe> visible=List.of();
    public StationScreen(StationMenu menu,Inventory inv,Component title) {
        super(menu,inv,title,menu.station.endsWith("workbench") ? 275 : 176,menu.station.endsWith("workbench") ? 184 : 166);
        workbench=menu.station.endsWith("workbench");blueprint=menu.station.equals("blueprint_workbench");
        inventoryLabelY=workbench ? 90 : 72;
        if(workbench && !blueprint) for(String category:List.of("weapons","attachments","ammo","stations","misc"))
            if(menu.recipes.stream().anyMatch(r->category(r).equals(category)))tabs.add(category);
    }
    private String category(StationRecipe r) {
        Item item=r.output().getItem();
        return item instanceof GunItem ? "weapons" : item instanceof AttachmentItem ? "attachments" :
            ModItems.AMMO.containsKey(r.result()) ? "ammo" : item instanceof BlockItem ? "stations" : "misc";
    }
    private void refresh() {clearWidgets();init();}
    @Override protected void init() {
        super.init();
        if(!workbench)return;
        if(!tabs.isEmpty())topPos+=14;
        visible=menu.recipes.stream().filter(r->blueprint ? menu.blueprintAllows(r) : tabs.isEmpty() || category(r).equals(tabs.get(tab))).toList();
        index=Math.min(index,Math.max(0,visible.size()-1));
        if(!blueprint) {
            addRenderableWidget(Button.builder(Component.literal("<"),b->{if(!visible.isEmpty()){index=(index+visible.size()-1)%visible.size();refresh();}}).bounds(leftPos+9,topPos+18,15,20).build());
            addRenderableWidget(Button.builder(Component.literal(">"),b->{if(!visible.isEmpty()){index=(index+1)%visible.size();refresh();}}).bounds(leftPos+153,topPos+18,15,20).build());
        }
        craft=addRenderableWidget(Button.builder(Component.translatable("gui.jeg.workbench.assemble"),b->{
            if(!visible.isEmpty() && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.recipes.indexOf(visible.get(index)));
        }).bounds(leftPos+195,topPos+16,74,20).build());
        addRenderableWidget(Button.builder(Component.literal(showRemaining ? "Remaining" : "Materials"),b->{showRemaining=!showRemaining;refresh();}).bounds(leftPos+174,topPos+44,95,15).build());
        for(int i=0;i<tabs.size();i++) {
            int n=i;addRenderableWidget(Button.builder(Component.literal(""),b->{tab=n;index=0;refresh();}).bounds(leftPos+i*28,topPos-28,28,28).build());
        }
    }
    @Override protected void containerTick() {
        super.containerTick();
        if(blueprint) {
            List<StationRecipe> next=menu.recipes.stream().filter(menu::blueprintAllows).toList();
            if(!next.equals(visible)){index=0;refresh();}
        }
        if(craft!=null)craft.active=!visible.isEmpty() && menu.blueprintAllows(visible.get(index)) && visible.get(index).plan(menu.playerInventory)!=null;
    }
    private void blit(GuiGraphicsExtractor g,Identifier texture,int x,int y,int u,int v,int w,int h) {
        g.blit(RenderPipelines.GUI_TEXTURED,texture,x,y,u,v,w,h,256,256);
    }
    private ItemStack materialStack(StationRecipe.Material m) {
        if(!m.tag())return new ItemStack(BuiltInRegistries.ITEM.getValue(m.id()));
        List<ItemStack> items=new ArrayList<>();
        for(var holder:BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM,m.id())))items.add(new ItemStack(holder.value()));
        return items.isEmpty() ? ItemStack.EMPTY : items.get((int)((System.currentTimeMillis()/1000)%items.size()));
    }
    private ItemStack tabIcon(String category) {
        return menu.recipes.stream().filter(r->category(r).equals(category)).map(StationRecipe::output).findFirst().orElse(ItemStack.EMPTY);
    }
    private void preview(GuiGraphicsExtractor g,ItemStack stack,int cx,int cy,float scale) {
        g.pose().pushMatrix();g.pose().translate(cx,cy);g.pose().scale(scale,scale);g.item(stack,-8,-8);g.pose().popMatrix();
    }
    @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta) {
        int x=leftPos,y=topPos;
        if(workbench) {
            Identifier texture=Identifier.parse("jeg:textures/gui/workbench.png");
            blit(g,texture,x,y,0,0,173,184);
            // Same stretched center and fixed end cap as original workbench.
            g.blit(RenderPipelines.GUI_TEXTURED,texture,x+173,y,173,0,78,184,1,184,256,256);
            blit(g,texture,x+251,y,174,0,24,184);
            if(blueprint) {
                blit(g,texture,x+171,y+15,218,0,22,22);
                if(menu.getSlot(0).getItem().isEmpty())blit(g,texture,x+174,y+18,180,199,16,16);
            }
            for(int i=0;i<tabs.size();i++) {
                blit(g,texture,x+i*28,y-28,i==tab ? (i==0 ? 80 :108) :80,i==tab ?214 :184,28,32);
            }
            if(!visible.isEmpty()) {
                StationRecipe recipe=visible.get(index);ItemStack output=recipe.output();
                String name=output.getHoverName().getString()+(output.getCount()>1 ? " x "+output.getCount() : "");
                g.text(font,name,x+88-font.width(name)/2,y+23,0xFFFFFFFF);
                preview(g,output,x+88,y+62,3.0F);
                int row=0,materialIndex=0;
                int[] deficits=recipe.deficits(menu.playerInventory);
                for(StationRecipe.Material material:recipe.materials()) {
                    int deficit=deficits[materialIndex++],need=showRemaining ? deficit : material.count();
                    if(showRemaining && need==0)continue;
                    int yy=y+63+row*19;
                    blit(g,texture,x+172,yy,0,deficit==0 ?184:222,80,19);
                    ItemStack example=materialStack(material);g.item(example,x+174,yy+1);
                    g.text(font,String.valueOf(need),x+196,yy+5,deficit==0 ?0xFFFFFFFF:0xFFFF7777);
                    if(mx>=x+172 && mx<x+252 && my>=yy && my<yy+19)g.setTooltipForNextFrame(font,example,mx,my);
                    row++;
                }
                if(mx>=x+8&&mx<x+168&&my>=y+38&&my<y+86)g.setTooltipForNextFrame(font,output,mx,my);
            }
        } else {
            Identifier texture=Identifier.parse("jeg:textures/gui/"+menu.station+".png");
            blit(g,texture,x,y,0,0,176,166);
            if(menu.station.equals("recycler")) {
                int burn=menu.data.get(3)==0 ?0:Math.min(13,menu.data.get(2)*13/menu.data.get(3));
                if(burn>0)blit(g,texture,x+56,y+48-burn,176,12-burn,14,burn+1);
                int progress=menu.data.get(1)==0 ?0:Math.min(24,menu.data.get(0)*24/menu.data.get(1));
                if(progress>0)blit(g,texture,x+79,y+34,176,14,progress+1,16);
            } else {
                String target=menu.blueprintTarget(menu.getSlot(0).getItem());
                if(target!=null)preview(g,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(target))),x+100,y+42,3);
            }
        }
        super.extractContents(g,mx,my,delta);
        // Icons are extracted after button backgrounds, just like the original category tabs.
        for(int i=0;i<tabs.size();i++) {
            g.item(tabIcon(tabs.get(i)),x+i*28+6,y-20);
            if(mx>=x+i*28&&mx<x+i*28+28&&my>=y-28&&my<y)g.setTooltipForNextFrame(Component.translatable("gui.jeg.workbench.tab."+tabs.get(i)),mx,my);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my) {
        g.text(font,title,titleLabelX,titleLabelY,0xFF404040,false);
        g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xFF404040,false);
    }
}
