package ttv.migami.jeg.mteg;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.resources.Identifier;
import ttv.migami.jeg.gun.GunStats;
/** Source-backed35 original MTEG definitions. Registration occurs explicitly in the native fork. */
public final class MtegDefinitions {
    private MtegDefinitions(){}
    public static Map<Identifier,MtegDefinition> decode(JsonObject bundle){
        Map<Identifier,MtegDefinition> result=new LinkedHashMap<>();
        for(var entry:bundle.getAsJsonObject("guns").entrySet()){
            var object=entry.getValue().getAsJsonObject();var definition=new MtegDefinition(entry.getKey(),object.getAsJsonObject("gun"),object.get("durability").getAsInt());
            if(result.put(definition.id(),definition)!=null)throw new IllegalArgumentException("Duplicate MTEG original ID");
        }
        if(result.size()!=35)throw new IllegalArgumentException("Expected35 original MTEG guns, got "+result.size());return Collections.unmodifiableMap(result);
    }
    public static Map<Identifier,MtegDefinition> load(){
        try(var input=MtegDefinitions.class.getResourceAsStream("/data/mteg/valen_content/definitions.json")){
            if(input==null)throw new IllegalStateException("Pinned MTEG definition bundle absent");
            return decode(JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject());
        }catch(IOException failure){throw new UncheckedIOException(failure);}
    }
    public static void addDefinitions(Map<Identifier,GunStats> target,Map<Identifier,MtegDefinition> definitions){
        for(var definition:definitions.values())if(target.putIfAbsent(definition.id(),definition.stats())!=null)throw new IllegalStateException("MTEG ID collision: "+definition.id());
    }
}
