package ttv.migami.jeg.tech.astra.oxygen.ui;
import java.util.Arrays;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
/** Six native channels adapt original item/power/input/output-fluid controls.
 * Original extraction-input slots3/4 are unavailable: no invented item-gas storage. */
public final class OxygenSideConfiguration {
 public static final int ITEM_INPUT=0,ITEM_OUTPUT=1,ENERGY=2,WATER=3,OXYGEN=4,CHANNELS=6;
 private final int[] masks={62,1,63,63,63,0};
 public int mask(int channel){check(channel);return masks[channel];}
 public boolean allows(int channel,Direction side){return side==null?mask(channel)!=0:(mask(channel)&(1<<side.ordinal()))!=0;}
 public void toggle(int channel,Direction side){check(channel);masks[channel]^=1<<side.ordinal();if(channel==5&&(masks[5]&(1<<side.ordinal()))!=0)masks[4]&=~(1<<side.ordinal());if(channel==4&&(masks[4]&(1<<side.ordinal()))!=0)masks[5]&=~(1<<side.ordinal());}
 public void save(ValueOutput out){for(int i=0;i<CHANNELS;i++)out.putInt("Side"+i,masks[i]);}
 public void load(ValueInput in){for(int i=0;i<CHANNELS;i++)masks[i]=in.getIntOr("Side"+i,i==ITEM_INPUT?62:i==ITEM_OUTPUT?1:i==5?0:63)&63;masks[4]&=~masks[5];}
 public static int button(int channel,Direction side){check(channel);return 100+channel*6+side.ordinal();}
 public static boolean validButton(int id){return id>=100&&id<100+CHANNELS*6;}
 private static void check(int channel){if(channel<0||channel>=CHANNELS)throw new IllegalArgumentException("Unknown oxygen channel "+channel);}
}
