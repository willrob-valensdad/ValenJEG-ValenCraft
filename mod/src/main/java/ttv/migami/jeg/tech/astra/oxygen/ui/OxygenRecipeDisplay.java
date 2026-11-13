package ttv.migami.jeg.tech.astra.oxygen.ui;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.*;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.resources.Identifier;
/** Server-owned native input/output gas IDs and quantities. No fabricated fluid or filled-tank item. */
public final class OxygenRecipeDisplay extends BasicDisplay {
 public static final CategoryIdentifier<OxygenRecipeDisplay> CATEGORY=CategoryIdentifier.of("jeg","valen_oxygen_loading");
 public final int energy,ticks,inputMb,gasMb;public final String inputId,gasId,recipeId;
 private static final StreamCodec<RegistryFriendlyByteBuf,List<EntryIngredient>> ENTRIES=EntryIngredient.streamCodec().apply(ByteBufCodecs.list());
 public static final DisplaySerializer<OxygenRecipeDisplay> SERIALIZER=DisplaySerializer.of(
  RecordCodecBuilder.<OxygenRecipeDisplay>mapCodec(i->i.group(
   EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(OxygenRecipeDisplay::getInputEntries),
   Codec.INT.fieldOf("energy").forGetter(d->d.energy),Codec.INT.fieldOf("ticks").forGetter(d->d.ticks),
   Codec.INT.fieldOf("input_mb").forGetter(d->d.inputMb),Codec.INT.fieldOf("gas_mb").forGetter(d->d.gasMb),
   Codec.STRING.fieldOf("input_id").forGetter(d->d.inputId),Codec.STRING.fieldOf("gas_id").forGetter(d->d.gasId),
   Codec.STRING.fieldOf("recipe_id").forGetter(d->d.recipeId)).apply(i,OxygenRecipeDisplay::new)),
  new StreamCodec<RegistryFriendlyByteBuf,OxygenRecipeDisplay>(){
   public OxygenRecipeDisplay decode(RegistryFriendlyByteBuf buf){return new OxygenRecipeDisplay(ENTRIES.decode(buf),buf.readVarInt(),buf.readVarInt(),buf.readVarInt(),buf.readVarInt(),buf.readUtf(),buf.readUtf(),buf.readUtf());}
   public void encode(RegistryFriendlyByteBuf buf,OxygenRecipeDisplay d){ENTRIES.encode(buf,d.getInputEntries());buf.writeVarInt(d.energy);buf.writeVarInt(d.ticks);buf.writeVarInt(d.inputMb);buf.writeVarInt(d.gasMb);buf.writeUtf(d.inputId);buf.writeUtf(d.gasId);buf.writeUtf(d.recipeId);}
  });
 public OxygenRecipeDisplay(List<EntryIngredient> input,int energy,int ticks,int inputMb,int gasMb,String inputId,String gasId,String recipeId){
  super(input,List.of(),Optional.of(Identifier.parse(recipeId)));this.energy=energy;this.ticks=ticks;this.inputMb=inputMb;this.gasMb=gasMb;this.inputId=inputId;this.gasId=gasId;this.recipeId=recipeId;
 }
 public CategoryIdentifier<OxygenRecipeDisplay> getCategoryIdentifier(){return CATEGORY;}
 public DisplaySerializer<OxygenRecipeDisplay> getSerializer(){return SERIALIZER;}
}
