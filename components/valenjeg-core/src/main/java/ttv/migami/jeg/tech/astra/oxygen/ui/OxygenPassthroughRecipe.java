package ttv.migami.jeg.tech.astra.oxygen.ui;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import ttv.migami.jeg.tech.astra.oxygen.OxygenLoadingRecipe;
/** Same pinned source recipe:25native oxygen ->25native oxygen,30E,1tick. Not free unprocessed gas exposure. */
public final class OxygenPassthroughRecipe extends SimpleJsonResourceReloadListener<OxygenLoadingRecipe> implements IdentifiableResourceReloadListener {
 private static volatile OxygenLoadingRecipe active;
 public OxygenPassthroughRecipe(){super(OxygenLoadingRecipe.CODEC,FileToIdConverter.json("valen_oxygen_passthrough"));}
 public Identifier getFabricId(){return Identifier.parse("jeg:valen_oxygen_passthrough");}
 public static OxygenLoadingRecipe active(){return active;}
 protected void apply(Map<Identifier,OxygenLoadingRecipe> recipes,ResourceManager manager,ProfilerFiller profiler){
  var recipe=recipes.get(Identifier.parse("jeg:oxygen"));
  if(recipe!=null&&(!recipe.input().values().tag().equals(Identifier.parse("nerospace:oxygen"))||!recipe.result().id().equals(Identifier.parse("nerospace:oxygen"))))
   throw new IllegalArgumentException("Native passthrough supports only existing Nero oxygen");
  active=recipe;
 }
}
