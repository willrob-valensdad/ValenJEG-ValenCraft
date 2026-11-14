package ttv.migami.jeg.mteg;
import com.google.gson.*;
import net.minecraft.resources.Identifier;
import ttv.migami.jeg.gun.GunStats;
/** Exact original MTEG gun source, with dependency5322334 reload semantics. No registry aliases. */
public final class MtegDefinition {
    private final Identifier id;private final JsonObject source;private final int durability;
    public MtegDefinition(String path,JsonObject source,int durability) {
        this.id=Identifier.fromNamespaceAndPath("mteg",path);this.source=source.deepCopy();this.durability=durability;
        if(durability<1||magazineSize()<1||fireDelay()<1||projectileLife()<1||projectileSpeed()<=0)throw new IllegalArgumentException("Invalid original definition "+id);
    }
    private JsonObject section(String name){return source.has(name)?source.getAsJsonObject(name):new JsonObject();}
    private int integer(String section,String name,int fallback){var o=section(section);return o.has(name)?o.get(name).getAsInt():fallback;}
    private float number(String section,String name,float fallback){var o=section(section);return o.has(name)?o.get(name).getAsFloat():fallback;}
    private boolean flag(String section,String name,boolean fallback){var o=section(section);return o.has(name)?o.get(name).getAsBoolean():fallback;}
    private String text(String section,String name,String fallback){var o=section(section);return o.has(name)?o.get(name).getAsString():fallback;}
    public Identifier id(){return id;}public JsonObject source(){return source.deepCopy();}public int durability(){return durability;}
    public String fireMode(){return text("general","fireMode","jeg:semi_automatic");}
    public boolean automatic(){return fireMode().equals("jeg:automatic");}public boolean burst(){return fireMode().equals("jeg:burst");}
    public int burstAmount(){return integer("general","burstAmount",0);}public int fireDelay(){return integer("general","rate",1);}
    public int pellets(){return integer("general","projectileAmount",1);}public float spread(){return number("general","spread",0);}
    public boolean alwaysSpread(){return flag("general","alwaysSpread",false);}
    public boolean supports(String slot){var modules=section("modules");return modules.has("attachments")&&modules.getAsJsonObject("attachments").has(slot);}
    public float zoomFov(){var modules=section("modules");return modules.has("zoom")&&modules.getAsJsonObject("zoom").has("fovModifier")?modules.getAsJsonObject("zoom").get("fovModifier").getAsFloat():1F;}
    public String gripType(){return text("general","gripType","jeg:two_handed");}
    public float moduleValue(String group,String field,float fallback){
        var modules=section("modules");if(!modules.has(group))return fallback;
        var object=modules.getAsJsonObject(group);return object.has(field)?object.get(field).getAsFloat():fallback;
    }
    public float attachmentValue(String slot,String field,float fallback){
        var modules=section("modules");if(!modules.has("attachments")||!modules.getAsJsonObject("attachments").has(slot))return fallback;
        var object=modules.getAsJsonObject("attachments").getAsJsonObject(slot);return object.has(field)?object.get(field).getAsFloat():fallback;
    }
    public float recoilAngle(){return number("general","recoilAngle",0);}public float recoilKick(){return number("general","recoilKick",0);}
    public Identifier ammo(){return Identifier.parse(text("projectile","item","minecraft:air"));}
    public float damage(){return number("projectile","damage",0);}public float projectileSpeed(){return number("projectile","speed",0);}
    public int projectileLife(){return integer("projectile","life",0);}public float projectileSize(){return number("projectile","size",.05F);}
    public boolean gravity(){return flag("projectile","gravity",false);}public String advantage(){return text("projectile","advantage","jeg:none");}
    public String reloadType(){return text("reloads","reloadType","jeg:manual");}public boolean singleItem(){return reloadType().equals("jeg:single_item");}
    public boolean manual(){return reloadType().equals("jeg:manual");}public int magazineSize(){return integer("reloads","maxAmmo",30);}
    public int reloadTimer(){return integer("reloads","reloadTimer",20);}public int emptyMagTimer(){return integer("reloads","emptyMagTimer",5);}
    public int reloadAmount(){return integer("reloads","reloadAmount",1);}
    public Identifier reloadAmmo(){return singleItem()?Identifier.parse(text("reloads","reloadItem","jeg:scrap")):ammo();}
    /** Original manual interval10, otherwise original strict elapsed>timer boundary. */
    public int nextReloadTicks(int currentAmmo){return manual()?10:reloadTimer()+(currentAmmo==0?emptyMagTimer():0)+1;}
    public int roundsToLoad(int currentAmmo,int available) {
        int missing=Math.max(0,magazineSize()-currentAmmo);if(available<=0)return 0;
        return singleItem()?missing:Math.min(missing,Math.min(available,manual()?reloadAmount():missing));
    }
    public Identifier sound(String name){String value=text("sounds",name,null);return value==null?null:Identifier.parse(value);}
    public GunStats stats(){return new GunStats(id,ammo(),reloadType(),magazineSize(),reloadTimer(),0,fireDelay(),damage(),projectileSpeed(),projectileLife(),gravity(),false,spread(),pellets(),sound("fire"),sound("silencedFire"),sound("enchantedFire"),sound("reload"),sound("reload"),sound("cock"),sound("cock"),null,projectileSize(),integer("projectile","trailColor",-256),number("projectile","trailLengthMultiplier",2));}
}
