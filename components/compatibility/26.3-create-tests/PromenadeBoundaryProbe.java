package valen.createprobe;
import fr.hugman.promenade.world.gen.feature.CoiledVinesFeature;
import fr.hugman.promenade.block.PromenadeBlocks;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipFile;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.util.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.*;
public final class PromenadeBoundaryProbe {
 private static final int CX=13,CZ=13;
 private static final class Holder extends GenerationChunkHolder {
  final ProtoChunk chunk;
  Holder(ProtoChunk chunk){super(chunk.getPos());this.chunk=chunk;}
  public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status){return chunk;}
  protected void addSaveDependency(CompletableFuture<?> future){}
  public int getTicketLevel(){return 31;}public int getQueueLevel(){return 31;}
 }
 private static final class Region extends WorldGenRegion {
  final Map<BlockPos,BlockState> written=new LinkedHashMap<>();int outside;
  Region(ServerLevel level,StaticCache2D<GenerationChunkHolder> cache,ChunkAccess center){super(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),center);}
  public BlockState getBlockState(BlockPos pos){if(!isWithinWriteZone(pos)){outside++;throw new AssertionError("outside generation read "+pos);}return super.getBlockState(pos);}
  public boolean setBlock(BlockPos pos,BlockState state,int flags,int limit){if(!isWithinWriteZone(pos)){outside++;throw new AssertionError("outside generation write "+pos);}written.put(pos.immutable(),state);return super.setBlock(pos,state,flags,limit);}
 }
 private static Region region(ServerLevel level,boolean cave){
  var factory=PalettedContainerFactory.create(level.registryAccess());
  var cache=StaticCache2D.<GenerationChunkHolder>create(CX,CZ,2,(x,z)->new Holder(new ProtoChunk(new ChunkPos(x,z),UpgradeData.EMPTY,level,factory,null)));
  var r=new Region(level,cache,((Holder)cache.get(CX,CZ)).chunk);
  if(cave)for(int x=192;x<=239;x++)for(int z=192;z<=239;z++)for(int y=96;y<=104;y++)
   if(y==99||x==192||x==239||z==192||z==239)r.getChunk(x>>4,z>>4).setBlockState(new BlockPos(x,y,z),Blocks.NETHERRACK.defaultBlockState(),2);
  return r;
 }
 private static Class<?> upstream()throws Exception{
  String name="fr.hugman.promenade.world.gen.feature.CoiledVinesFeature";
  byte[] bytes;try(var z=new ZipFile(System.getProperty("valen.promenade.originalJar"))){bytes=z.getInputStream(z.getEntry(name.replace('.','/')+".class")).readAllBytes();}
  return new ClassLoader(CoiledVinesFeature.class.getClassLoader()){
   protected Class<?> loadClass(String n,boolean resolve)throws ClassNotFoundException{
    if(!n.equals(name))return super.loadClass(n,resolve);
    Class<?> type=findLoadedClass(n);if(type==null)type=defineClass(n,bytes,0,bytes.length);if(resolve)resolveClass(type);return type;
   }
  }.loadClass(name);
 }
 public static Map<String,Object> run(MinecraftServer server)throws Exception {
  var level=server.getLevel(Level.NETHER);if(level==null)throw new AssertionError("Nether absent");
  var old=upstream();
  var column=old.getMethod("generateVineColumn",LevelAccessor.class,RandomSource.class,BlockPos.MutableBlockPos.class,int.class,int.class,int.class,Direction.class);
  Region baseline=region(level,false);boolean rejected=false;
  try{column.invoke(null,baseline,RandomSource.create(583),new BlockPos.MutableBlockPos(208,100,194),8,17,25,Direction.NORTH);}
  catch(InvocationTargetException e){if(e.getCause() instanceof AssertionError&&baseline.outside>0)rejected=true;else throw e;}
  if(!rejected)throw new AssertionError("Old edge case did not exercise boundary");
  Region edge=region(level,false);CoiledVinesFeature.generateVineColumn(edge,RandomSource.create(583),new BlockPos.MutableBlockPos(208,100,194),8,17,25,Direction.NORTH);
  if(edge.outside!=0||edge.written.size()!=3||!edge.getBlockState(new BlockPos(208,100,192)).is(PromenadeBlocks.COILED_VINES))throw new AssertionError("Edge head or extent "+edge.written);
  Region before=region(level,false),after=region(level,false);
  column.invoke(null,before,RandomSource.create(583),new BlockPos.MutableBlockPos(208,100,208),4,17,25,Direction.NORTH);
  CoiledVinesFeature.generateVineColumn(after,RandomSource.create(583),new BlockPos.MutableBlockPos(208,100,208),4,17,25,Direction.NORTH);
  if(!before.written.equals(after.written))throw new AssertionError("Interior vine behavior changed");
  var directions=List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST);
  var candidate=new CoiledVinesFeature(8,4,8,directions);
  var oldFeature=old.getConstructor(int.class,int.class,int.class,List.class).newInstance(8,4,8,directions);
  var place=old.getMethod("place",WorldGenLevel.class,net.minecraft.world.level.chunk.ChunkGenerator.class,RandomSource.class,BlockPos.class);
  int oldFailures=0,placed=0;
  for(int seed=0;seed<32;seed++){
   var origin=new BlockPos(208,100,193);Region oldRegion=region(level,true),newRegion=region(level,true);
   try{place.invoke(oldFeature,oldRegion,level.getChunkSource().getGenerator(),RandomSource.create(seed),origin);}
   catch(InvocationTargetException e){if(e.getCause() instanceof AssertionError&&oldRegion.outside>0)oldFailures++;else throw e;}
   candidate.place(newRegion,level.getChunkSource().getGenerator(),RandomSource.create(seed),origin);
   if(newRegion.outside!=0)throw new AssertionError("Unsafe native feature seed "+seed);
   placed+=newRegion.written.size();
  }
  if(oldFailures==0||placed==0)throw new AssertionError("Boundary corpus not exercised "+oldFailures+"/"+placed);
  return Map.of("test","Promenade actual feature obeys native Nether generation bounds","pass",true,"baseline_outside_column",rejected,"edge_head_blocks",edge.written.size(),"interior_exact_state_parity",true,"full_feature_seeds",32,"baseline_unsafe_seeds",oldFailures,"candidate_unsafe_reads_writes",0,"candidate_blocks_placed",placed,"scope","Real WorldGenRegion with in-memory cave ProtoChunks; no natural distribution claim");
 }
}
