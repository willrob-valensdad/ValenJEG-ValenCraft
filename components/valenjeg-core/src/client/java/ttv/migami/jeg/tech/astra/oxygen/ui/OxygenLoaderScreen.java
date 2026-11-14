package ttv.migami.jeg.tech.astra.oxygen.ui;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
/** Native extraction screen; original source slot/inventory positions, no copied ARR GUI artwork. */
public final class OxygenLoaderScreen extends AbstractContainerScreen<OxygenLoaderMenu> {
 private final List<Button> faceButtons=new ArrayList<>();
 private static final String[] LABELS={"Item in","Item out","Power","Water","Gas out","Gas in"};
 public OxygenLoaderScreen(OxygenLoaderMenu menu,Inventory inv,Component title){super(menu,inv,title,316,208);inventoryLabelY=90;}
 @Override protected void init(){
  super.init();faceButtons.clear();
  for(int lane=0;lane<OxygenSideConfiguration.CHANNELS;lane++)for(Direction face:Direction.values()){
   int channel=lane;Direction side=face;
   faceButtons.add(addRenderableWidget(Button.builder(Component.literal(""),button->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,OxygenSideConfiguration.button(channel,side)))
    .bounds(leftPos+244+face.ordinal()*11,topPos+49+lane*22,11,18).build()));
  }
 }
 @Override protected void containerTick(){
  super.containerTick();for(int lane=0;lane<OxygenSideConfiguration.CHANNELS;lane++)for(Direction side:Direction.values()){
   boolean on=(menu.data.get(6+lane)&(1<<side.ordinal()))!=0;
   faceButtons.get(lane*6+side.ordinal()).setMessage(Component.literal(on?"+":"-"));
  }
 }
 @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
  int x=leftPos,y=topPos;g.fill(x,y,x+316,y+208,0xFFC6C6C6);g.fill(x+176,y+4,x+178,y+204,0xFF777777);
  for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;g.fill(sx-1,sy-1,sx+17,sy+17,0xFF777777);g.fill(sx,sy,sx+16,sy+16,0xFF8B8B8B);}
  bar(g,x+43,y+22,16,48,menu.data.get(13)>0?menu.data.get(13)*81L:menu.waterDroplets(),243000,0xFF357FD8);
  bar(g,x+100,y+22,16,48,menu.data.get(3),3000,0xFF9BDEDF);
  bar(g,x+150,y+52,16,18,menu.data.get(0),20000,0xFFFFCC42);
  if(mx>=x+43&&mx<x+59&&my>=y+22&&my<y+70)g.setTooltipForNextFrame(Component.literal((menu.data.get(13)>0?"Native oxygen input: "+menu.data.get(13)+" mB; water droplets: ":"Water: ")+menu.waterDroplets()+"/243000 droplets"),mx,my);
  if(mx>=x+100&&mx<x+116&&my>=y+22&&my<y+70)g.setTooltipForNextFrame(Component.literal("Native nerospace:oxygen: "+menu.data.get(3)+"/3000 mB"),mx,my);
  g.text(font,(menu.data.get(13)>0?menu.data.get(13):menu.waterDroplets()/81)+" mB",x+37,y+74,0xFF404040);g.text(font,menu.data.get(3)+" mB",x+94,y+74,0xFF404040);
  g.text(font,menu.data.get(0)+" E",x+120,y+83,0xFF404040);
  g.text(font,"World faces (+ / -)",x+183,y+9,0xFF404040);
  for(Direction side:Direction.values())g.text(font,side.name().substring(0,1),x+248+side.ordinal()*11,y+31,0xFF404040);
  for(int lane=0;lane<OxygenSideConfiguration.CHANNELS;lane++){
   g.text(font,LABELS[lane],x+183,y+54+lane*22,0xFF404040);
   for(Direction face:Direction.values())if(mx>=x+244+face.ordinal()*11&&mx<x+255+face.ordinal()*11&&my>=y+49+lane*22&&my<y+67+lane*22)
    g.setTooltipForNextFrame(Component.literal(LABELS[lane]+" · "+face.name()+" · world direction"),mx,my);
  }
  int providers=menu.data.get(12);String faces="";for(Direction side:Direction.values())if((providers&(1<<side.ordinal()))!=0)faces+=side.name().substring(0,1)+" ";
  g.text(font,"Tank: "+(faces.isEmpty()?"none":faces),x+183,y+186,0xFF404040);
  g.text(font,"Nero tank beside loader",x+183,y+198,0xFF404040);
  super.extractContents(g,mx,my,delta);
 }
 private void bar(GuiGraphicsExtractor g,int x,int y,int w,int h,long amount,long max,int color){
  g.fill(x-1,y-1,x+w+1,y+h+1,0xFF555555);g.fill(x,y,x+w,y+h,0xFF222222);
  int fill=(int)Math.clamp(amount*h/max,0,h);g.fill(x,y+h-fill,x+w,y+h,color);
 }
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  g.text(font,title,titleLabelX,titleLabelY,0xFF404040,false);g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xFF404040,false);
 }
}
