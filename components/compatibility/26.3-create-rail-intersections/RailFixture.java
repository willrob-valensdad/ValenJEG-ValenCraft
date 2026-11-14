package com.zurrtum.create.content.trains.graph;

import com.google.gson.GsonBuilder;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.track.BezierConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;

/** Full author graph operation in a disposable actual Fabric world. */
public final class RailFixture implements ModInitializer {
    record Shape(String name,int crossing,int disjoint,boolean warm) {}
    record Setup(TrackGraph graph,DiscoveredLocation first,DiscoveredLocation second,BezierConnection turn,
                 Map<UUID,TrackGraph> previousNetworks,TrackGraphSync previousSync) {}
    record Trial(long nanos,String geometry,int intersections,int deferred,int edgeCount) {}
    static final Vec3 UP=new Vec3(0,1,0);
    static final Shape[] SHAPES={
        new Shape("empty",0,0,true),new Shape("small4",4,0,true),
        new Shape("crossing16",16,0,true),new Shape("crossing32",32,0,true),
        new Shape("mixed512",32,480,true),new Shape("disjoint512",0,512,true),
        new Shape("cold_crossing32",32,0,false)};
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if(Boolean.getBoolean("valen.rail.fixture"))server.execute(()->run(server));
        });
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static DiscoveredLocation location(Vec3 p) {
        return new DiscoveredLocation(Level.OVERWORLD,p).withNormal(UP).materials(AllTrackMaterials.ANDESITE,AllTrackMaterials.ANDESITE);
    }
    static BezierConnection curve(Vec3 a,Vec3 b,Vec3 axisA,Vec3 axisB,boolean primary) {
        return new BezierConnection(Couple.create(BlockPos.containing(a),BlockPos.containing(b)),
            Couple.create(a,b),Couple.create(axisA,axisB),Couple.create(UP,UP),primary,false,AllTrackMaterials.ANDESITE);
    }
    static Setup setup(Shape shape) {
        Map<UUID,TrackGraph> old=Create.RAILWAYS.trackNetworks;
        TrackGraphSync oldSync=Create.RAILWAYS.sync;
        Create.RAILWAYS.trackNetworks=new LinkedHashMap<>();
        Create.RAILWAYS.sync=new TrackGraphSync();
        TrackGraph graph=new TrackGraph(new UUID(0,1));
        graph.nodes=new LinkedHashMap<>();graph.connectionsByNode=new LinkedHashMap<>();
        Create.RAILWAYS.trackNetworks.put(graph.id,graph);
        Vec3 a=new Vec3(0,201,0),b=new Vec3(32,201,0);
        DiscoveredLocation first=location(a),second=location(b);
        graph.addNode(new TrackNode(first,1,UP));graph.addNode(new TrackNode(second,2,UP));
        for(int i=0;i<shape.crossing+shape.disjoint;i++) {
            boolean crossing=i<shape.crossing;
            double x=crossing?i+0.5:100+(i-shape.crossing)*2;
            Vec3 p=new Vec3(x,201,-16),q=new Vec3(x,201,16);
            DiscoveredLocation l1=location(p),l2=location(q);
            TrackNode n1=new TrackNode(l1,3+i*2,UP),n2=new TrackNode(l2,4+i*2,UP);
            graph.addNode(n1);graph.addNode(n2);
            BezierConnection turn=curve(p,q,new Vec3(0,0,1),new Vec3(0,0,-1),false);
            if(shape.warm)turn.getBounds();
            TrackEdge forward=new TrackEdge(n1,n2,turn,AllTrackMaterials.ANDESITE);
            TrackEdge reverse=new TrackEdge(n2,n1,turn.secondary(),AllTrackMaterials.ANDESITE);
            graph.connectionsByNode.put(n1,new LinkedHashMap<>(Map.of(n2,forward)));
            graph.connectionsByNode.put(n2,new LinkedHashMap<>(Map.of(n1,reverse)));
        }
        BezierConnection turn=curve(a,b,new Vec3(1,0,0),new Vec3(-1,0,0),true);
        return new Setup(graph,first,second,turn,old,oldSync);
    }
    static Trial trial(MinecraftServer server,Shape shape) throws Exception {
        Setup s=setup(shape);
        try {
            long start=System.nanoTime();
            s.graph.connectNodes(server.overworld(),s.first,s.second,s.turn);
            long elapsed=System.nanoTime()-start;
            StringBuilder text=new StringBuilder();int intersections=0,edges=0;
            Map<UUID,Integer> ids=new LinkedHashMap<>();
            IdentityHashMap<TrackEdge,String> names=new IdentityHashMap<>();
            for(TrackNode from:s.graph.nodes.values()) {
                Map<TrackNode,TrackEdge> connections=s.graph.connectionsByNode.get(from);
                if(connections==null)continue;
                for(var item:connections.entrySet()) {
                    TrackEdge edge=item.getValue();String label=from.getNetId()+"->"+item.getKey().getNetId();
                    names.put(edge,label);edges++;text.append(label).append('[');
                    for(TrackEdgeIntersection intersection:edge.getEdgeData().getIntersections()) {
                        intersections++;
                        int id=ids.computeIfAbsent(intersection.id,key->ids.size());
                        text.append(id).append(':').append(Long.toHexString(Double.doubleToLongBits(intersection.location)))
                            .append(':').append(intersection.target.getFirst().toShortString()).append(':')
                            .append(intersection.target.getSecond().toShortString()).append(':')
                            .append(Long.toHexString(Double.doubleToLongBits(intersection.targetLocation))).append(';');
                    }
                    text.append(']');
                }
            }
            text.append("deferred:");
            for(TrackEdge edge:s.graph.deferredIntersectionUpdates)text.append(names.get(edge)).append(';');
            text.append("published:").append(s.graph.getConnection(Couple.create(s.graph.nodes.get(s.first),s.graph.nodes.get(s.second)))!=null)
                .append(':').append(s.graph.getConnection(Couple.create(s.graph.nodes.get(s.second),s.graph.nodes.get(s.first)))!=null);
            check(edges==(shape.crossing+shape.disjoint)*2+2,"edge adoption count");
            check((shape.crossing==0)==(intersections==0),"crossing fixture did not exercise intersections");
            String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8)));
            return new Trial(elapsed,digest,intersections,s.graph.deferredIntersectionUpdates.size(),edges);
        } finally {Create.RAILWAYS.trackNetworks=s.previousNetworks;Create.RAILWAYS.sync=s.previousSync;}
    }
    static void run(MinecraftServer server) {
        Map<String,Object> report=new LinkedHashMap<>();
        try {
            check(server.isSameThread(),"not server owner");
            check(Files.isRegularFile(Path.of(".valen-disposable-benchmark")),"not disposable");
            int warmup=Integer.getInteger("valen.rail.warmup",3),samples=Integer.getInteger("valen.rail.samples",7);
            check(warmup>=1&&warmup<=8&&samples>=3&&samples<=15,"bounds");
            List<Map<String,Object>> rows=new ArrayList<>();
            for(Shape shape:SHAPES) {
                String reference=null;List<Long> times=new ArrayList<>();Trial last=null;
                for(int i=-warmup;i<samples;i++) {
                    Trial result=trial(server,shape);last=result;
                    if(reference==null)reference=result.geometry;
                    check(reference.equals(result.geometry),"within-run geometry/order mismatch "+shape.name);
                    if(i>=0)times.add(result.nanos);
                }
                long[] sorted=times.stream().mapToLong(Long::longValue).sorted().toArray();
                Map<String,Object> row=new LinkedHashMap<>();
                row.put("case",shape.name);row.put("existing_edges",(shape.crossing+shape.disjoint)*2);
                row.put("crossing_candidates",shape.crossing);row.put("disjoint_candidates",shape.disjoint);
                row.put("warm_existing_geometry",shape.warm);row.put("curve_chord_blocks",32);
                row.put("samples_ns",times);row.put("median_ns",sorted[sorted.length/2]);
                row.put("geometry_sha256",reference);row.put("intersection_records",last.intersections);
                row.put("deferred_updates",last.deferred);row.put("adopted_edge_count",last.edgeCount);
                rows.add(row);
            }
            report.put("passed",true);report.put("cases",rows);report.put("runtime",System.getProperty("java.runtime.version"));
            report.put("configured_parallelism",System.getProperty("valenmultithreaderd.parallelism"));
            report.put("warmup",warmup);report.put("samples",samples);
            report.put("timed_scope","complete actual TrackGraph.connectNodes including preparation/geometry/UUID/dedup/commit/sync/dirty");
            report.put("fixture_setup_and_validation_timed",false);report.put("production_installed",false);
        }catch(Throwable failure){failure.printStackTrace();report.put("passed",false);report.put("error",failure.toString());}
        finally{
            try{Files.writeString(Path.of("rail-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}
            catch(Throwable failure){failure.printStackTrace();}
            server.halt(false);
        }
    }
}
