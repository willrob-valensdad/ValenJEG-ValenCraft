import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import com.google.gson.*;
import com.geckolib.loading.definition.animation.ActorAnimations;
import com.geckolib.loading.math.MathParser;
import software.bernie.geckolib.core.molang.MolangParser;
public class AnimationBakeCheck {
 public static void main(String[] args)throws Exception {
  Map<String,Object> result=new LinkedHashMap<>();
  Map<String,Double> legacy=new LinkedHashMap<>();
  for(String name:List.of("easeInSine","easeOutSine","easeOutQuad","easeInOutSine")) {
   double v=MolangParser.parseExpression(name).get();
   if(v!=0)throw new AssertionError(name+" legacy="+v);
   legacy.put(name,v);
  }
  result.put("actual_gecko_4_4_4_values",legacy);
  List<Object> runs=new ArrayList<>();
  for(int which=0;which<2;which++) {
   List<String> failures=new ArrayList<>();int files=0,animations=0,success=0;
   try(ZipFile z=new ZipFile(args[which])) {
    for(var e:Collections.list(z.entries())) {
     if(!e.getName().startsWith("assets/jeg/geckolib/animations/")||!e.getName().endsWith(".json"))continue;
     files++;
     var obj=ActorAnimations.GSON.fromJson(new String(z.getInputStream(e).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8),ActorAnimations.class);
     for(var entry:obj.animations().entrySet()) {
      animations++;
      try {entry.getValue().bake(entry.getKey(),MathParser.createWithDeduplication());success++;}
      catch(Throwable ex){failures.add(e.getName()+"#"+entry.getKey()+": "+ex.toString());}
     }
    }
   }
   runs.add(Map.of("jar",args[which],"files",files,"animations",animations,"success",success,"failures",failures));
   if(which==0 && failures.size()!=5)throw new AssertionError("Expected five original failures: "+failures);
   if(which==1 && !failures.isEmpty())throw new AssertionError(failures);
  }
  result.put("native_gecko_5_5_7",runs);result.put("pass",true);
  String json=new GsonBuilder().setPrettyPrinting().create().toJson(result);
  Files.writeString(Path.of(args[2]),json+"\n");System.out.println(json);
 }
}
