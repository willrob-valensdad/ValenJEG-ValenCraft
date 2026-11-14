package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;

/** Fixture-only read-only observer of starts that earlier native tests generated.
 * No forced starts, FULL requests, placement, block edits, players or broad search.
 */
public final class StructureDetailsProbe {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Path INDEX=Path.of(System.getProperty("valenjeg.evidence.e7a1c2b93321", "VALENJEG_EVIDENCE_NOT_CONFIGURED/combined-native-structure-index.json"));
    private static final Path OUT=Path.of("valen-structure-details.json");
    private static final long SEED=-4056840922791553232L;
    private static void require(boolean b,String s){if(!b)throw new IllegalStateException(s);}
    private static JsonArray xyz(BlockPos p){JsonArray a=new JsonArray();a.add(p.getX());a.add(p.getY());a.add(p.getZ());return a;}
    private static JsonArray box(BoundingBox b){JsonArray a=new JsonArray();for(int v:new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()})a.add(v);return a;}
    private static BoundingBox intersection(BoundingBox a,BoundingBox b){int x=Math.max(a.minX(),b.minX()),y=Math.max(a.minY(),b.minY()),z=Math.max(a.minZ(),b.minZ()),xx=Math.min(a.maxX(),b.maxX()),yy=Math.min(a.maxY(),b.maxY()),zz=Math.min(a.maxZ(),b.maxZ());return x<=xx&&y<=yy&&z<=zz?new BoundingBox(x,y,z,xx,yy,zz):null;}
    public static void run(ServerLevel level){
        if(!Boolean.getBoolean("valen.structure.details"))return;
        require(Files.exists(Path.of("../preparation.json"))&&level.getServer().getPort()==25589&&level.getSeed()==SEED,"Wrong fixture preparation/port/seed");
        try{var job=new Job(level);ServerTickEvents.END_SERVER_TICK.register(server->{if(!job.done)job.step();});}
        catch(Exception e){throw new IllegalStateException("Structure details observer startup",e);}
    }
    private record Ref(Structure structure,ChunkPos chunk){}
    private static final class Job {
        final ServerLevel level;final JsonObject report=new JsonObject();final JsonArray sites=new JsonArray(),overlaps=new JsonArray(),reads=new JsonArray();
        final Map<Structure,String> ids=new HashMap<>();final Map<String,Structure> byId=new HashMap<>();final List<JsonObject> prior=new ArrayList<>();
        final ArrayDeque<ChunkPos> siloChunks=new ArrayDeque<>();final ArrayDeque<Ref> references=new ArrayDeque<>();final Set<Ref> seenRefs=new HashSet<>();
        final Map<ChunkPos,ChunkAccess> surfaceChunks=new HashMap<>();StructureStart silo;int index,ownerReads,surfaceReads,ticks;boolean done;
        Job(ServerLevel level)throws Exception{
            this.level=level;for(var h:level.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().toList()){String id=h.key().identifier().toString();ids.put(h.value(),id);byId.put(id,h.value());}
            for(var row:JsonParser.parseString(Files.readString(INDEX)).getAsJsonObject().getAsJsonArray("sites"))prior.add(row.getAsJsonObject());require(prior.size()==8,"Expected exact8 prior natural starts");
            report.addProperty("status","running read-only incrementally");report.addProperty("world_seed",SEED);report.addProperty("source_index",INDEX.toString());report.add("sites",sites);report.add("silo_other_starts",overlaps);report.add("native_STA_reads",reads);
            report.addProperty("scope","8 recorded start chunks; at most16 prior Silo FULL footprint chunks and32 referenced start owners; entrance actual surfaces at at most8 known prior FULL chunks");
            report.addProperty("placement_or_block_writes",false);report.addProperty("FULL_generation_requested",false);report.addProperty("overlap_proof_limit","Native start/piece bounding-box intersections only, not proof of block replacement/destructive overlap");save();
        }
        void save(){try{Files.writeString(OUT,GSON.toJson(report));}catch(Exception e){throw new RuntimeException(e);}}
        ChunkAccess read(ChunkPos cp,String reason){
            // Only explicit prior starts/footprints or their existing native references.
            var c=level.getChunkSource().getChunk(cp.x(),cp.z(),ChunkStatus.STRUCTURE_STARTS,true);require(c!=null,"Missing recorded native chunk");
            JsonObject r=new JsonObject();r.addProperty("chunk_x",cp.x());r.addProperty("chunk_z",cp.z());r.addProperty("reason",reason);r.addProperty("persisted_status",c.getPersistedStatus().toString());reads.add(r);return c;
        }
        int terrain(int x,int z){var s=level.getChunkSource();return s.getGenerator().getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,level,s.randomState());}
        JsonObject piece(StructurePiece p,int n){
            JsonObject r=new JsonObject();BoundingBox b=p.getBoundingBox();r.addProperty("index",n);r.addProperty("native_piece_class",p.getClass().getName());r.add("bounds",box(b));r.addProperty("rotation",p.getRotation().toString());r.addProperty("generation_depth",p.getGenDepth());
            int cx=(b.minX()+b.maxX())/2,cz=(b.minZ()+b.maxZ())/2;r.addProperty("unadapted_center_surface_y",terrain(cx,cz));
            if(p instanceof PoolElementStructurePiece pool){r.add("origin",xyz(pool.getPosition()));r.addProperty("ground_level_delta",pool.getGroundLevelDelta());r.addProperty("pool_element_class",pool.getElement().getClass().getName());
                if(pool.getElement() instanceof SinglePoolElement single){Identifier id=single.getTemplateLocation();r.addProperty("template_id",id==null?"runtime template":id.toString());}
            }return r;
        }
        void entranceSurface(JsonObject row,BoundingBox b){
            JsonArray samples=new JsonArray();row.add("native_surface_columns",samples);int cx=(b.minX()+b.maxX())/2,cz=(b.minZ()+b.maxZ())/2;
            for(int[] xz:new int[][]{{cx,cz},{b.minX(),cz},{b.maxX(),cz},{cx,b.minZ()},{cx,b.maxZ()},{b.minX(),b.minZ()},{b.maxX(),b.minZ()},{b.minX(),b.maxZ()},{b.maxX(),b.maxZ()}}){
                int x=xz[0],z=xz[1];ChunkPos cp=new ChunkPos(x>>4,z>>4);ChunkAccess c=surfaceChunks.get(cp);
                if(c==null){require(++surfaceReads<=8,"Entrance native surface read cap");c=read(cp,"recorded Moon entrance footprint surface (prior FULL)");surfaceChunks.put(cp,c);}
                JsonObject s=new JsonObject();s.addProperty("x",x);s.addProperty("z",z);s.addProperty("unadapted_surface_y",terrain(x,z));samples.add(s);
                if(c.getPersistedStatus()!=ChunkStatus.FULL){s.addProperty("status","not persisted FULL; actual surface/block observation skipped, no generation");continue;}
                int y=c.getHeight(Heightmap.Types.WORLD_SURFACE,x&15,z&15);s.addProperty("native_WORLD_SURFACE_height",y);s.addProperty("piece_top_y",b.maxY());s.addProperty("surface_minus_piece_top",y-b.maxY());JsonArray column=new JsonArray();s.add("block_column_near_piece_top_to_surface",column);
                int hi=Math.min(level.getMaxY()-1,Math.max(y+2,b.maxY()+2)),lo=Math.max(level.getMinY(),b.maxY()-2);require(hi-lo<=256,"Entrance column observation cap");
                String previous="";JsonObject run=null;
                for(int yy=lo;yy<=hi;yy++){var bs=c.getBlockState(new BlockPos(x,yy,z));String id=BuiltInRegistries.BLOCK.getKey(bs.getBlock()).toString();if(!id.equals(previous)){run=new JsonObject();run.addProperty("min_y",yy);run.addProperty("max_y",yy);run.addProperty("block",id);run.addProperty("air",bs.isAir());column.add(run);previous=id;}else run.addProperty("max_y",yy);}
            }
        }
        void priorStart(JsonObject prior){
            String id=prior.get("id").getAsString();Structure structure=byId.get(id);require(structure!=null,"Missing prior structure "+id);ChunkPos cp=new ChunkPos(prior.get("start_chunk_x").getAsInt(),prior.get("start_chunk_z").getAsInt());var start=read(cp,"recorded prior start "+id).getStartForStructure(structure);require(start!=null&&start.isValid(),"Recorded native start missing "+id);
            JsonObject row=new JsonObject();row.addProperty("id",id);row.addProperty("start_chunk_x",cp.x());row.addProperty("start_chunk_z",cp.z());row.add("bounds",box(start.getBoundingBox()));row.addProperty("piece_count",start.getPieces().size());JsonArray parts=new JsonArray();row.add("pieces",parts);sites.add(row);
            int i=0;for(var p:start.getPieces()){JsonObject part=piece(p,i++);parts.add(part);if(id.equals("jeg:valen_ad_astra/moon_dungeon")&&part.has("template_id")&&part.get("template_id").getAsString().endsWith("/entrance"))entranceSurface(part,p.getBoundingBox());}
            if(id.equals("jeg:valen/missile_silo")){silo=start;BoundingBox b=start.getBoundingBox();for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)siloChunks.add(new ChunkPos(x,z));require(siloChunks.size()<=16,"Silo footprint read cap");}
        }
        void enqueue(Structure s,ChunkPos cp){Ref ref=new Ref(s,cp);if(s==silo.getStructure()||!seenRefs.add(ref))return;if(seenRefs.size()>32){report.addProperty("silo_reference_cap_reached",true);return;}references.add(ref);}
        void footprint(ChunkPos cp){var c=read(cp,"recorded Silo FULL footprint starts/references");require(c.getPersistedStatus()==ChunkStatus.FULL,"Silo footprint not persisted FULL; no FULL generation permitted");for(var e:c.getAllStarts().entrySet())if(e.getValue().isValid())enqueue(e.getKey(),e.getValue().getChunkPos());for(var e:c.getAllReferences().entrySet())for(long packed:e.getValue())enqueue(e.getKey(),new ChunkPos(ChunkPos.getX(packed),ChunkPos.getZ(packed)));}
        void referenced(Ref ref){require(++ownerReads<=32,"Silo referenced owner cap");var start=read(ref.chunk(),"existing native Silo reference owner").getStartForStructure(ref.structure());if(start==null||!start.isValid())return;JsonObject row=new JsonObject();row.addProperty("id",ids.get(ref.structure()));row.addProperty("start_chunk_x",ref.chunk().x());row.addProperty("start_chunk_z",ref.chunk().z());row.add("bounds",box(start.getBoundingBox()));row.addProperty("piece_count",start.getPieces().size());row.addProperty("adjusted_start_bounds_intersect_silo",intersection(start.getBoundingBox(),silo.getBoundingBox())!=null);JsonArray physical=new JsonArray();row.add("piece_box_intersections",physical);overlaps.add(row);int i=0;
            for(var other:start.getPieces()){int j=0;for(var own:silo.getPieces()){BoundingBox hit=intersection(other.getBoundingBox(),own.getBoundingBox());if(hit!=null){JsonObject p=piece(other,i);p.addProperty("silo_piece_index",j);p.add("intersection_bounds",box(hit));physical.add(p);}j++;}i++;}
        }
        void step(){try{require(++ticks<=512,"Structure detail observer tick cap");if(index<prior.size())priorStart(prior.get(index++));else if(!siloChunks.isEmpty())footprint(siloChunks.removeFirst());else if(!references.isEmpty())referenced(references.removeFirst());else{report.addProperty("status","read-only native details complete");report.addProperty("referenced_owner_reads",ownerReads);report.addProperty("entrance_surface_chunk_reads",surfaceReads);done=true;}save();}catch(Throwable e){done=true;report.addProperty("status","observer failure; partial evidence retained");report.addProperty("error",e.toString());save();e.printStackTrace();}}
    }
}
