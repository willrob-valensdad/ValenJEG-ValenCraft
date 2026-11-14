package com.zurrtum.create.content.trains.graph;

import com.google.gson.GsonBuilder;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.Create;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/** TEST-ONLY: complete original and candidate calls in one initialized Fabric JVM. */
public final class RailPairedFixture implements ModInitializer {
    static final class UnknownEdge extends TrackEdge {
        UnknownEdge(TrackEdge edge){super(edge.node1,edge.node2,edge.turn,AllTrackMaterials.ANDESITE);}
    }
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if(Boolean.getBoolean("valen.rail.fixture"))server.execute(()->run(server));
        });
    }
    static RailFixture.Trial trial(MinecraftServer server,RailFixture.Shape shape,boolean original)throws Exception {
        RailFixture.Setup s=RailFixture.setup(shape);
        // Match the author's connection-container types. Node iteration remains
        // ordered solely to make exact result/UUID comparison deterministic.
        Map<TrackNode,Map<TrackNode,TrackEdge>> identity=new IdentityHashMap<>();
        s.graph().connectionsByNode.forEach((node,map)->identity.put(node,new IdentityHashMap<>(map)));
        s.graph().connectionsByNode=identity;
        try {
            if(shape.name().equals("unsupported32"))
                for(var map:s.graph().connectionsByNode.values())
                    map.replaceAll((target,edge)->edge.node1.netId==3?new UnknownEdge(edge):edge);
            long elapsed;
            if(original) {
                long start=System.nanoTime();
                s.graph().valen$connectNodesOriginal(server.overworld(),s.first(),s.second(),s.turn());
                elapsed=System.nanoTime()-start;
            } else {
                long start=System.nanoTime();
                s.graph().connectNodes(server.overworld(),s.first(),s.second(),s.turn());
                elapsed=System.nanoTime()-start;
            }
            StringBuilder text=new StringBuilder();int intersections=0,edges=0;
            Map<UUID,Integer> ids=new LinkedHashMap<>();IdentityHashMap<TrackEdge,String> names=new IdentityHashMap<>();
            for(TrackNode from:s.graph().nodes.values()) {
                Map<TrackNode,TrackEdge> connections=s.graph().connectionsByNode.get(from);
                if(connections==null)continue;
                for(var item:connections.entrySet()) {
                    TrackEdge edge=item.getValue();String label=from.netId+"->"+item.getKey().netId;
                    names.put(edge,label);edges++;text.append(label).append('[');
                    for(TrackEdgeIntersection x:edge.edgeData.getIntersections()) {
                        intersections++;int id=ids.computeIfAbsent(x.id,key->ids.size());
                        text.append(id).append(':').append(Double.doubleToLongBits(x.location)).append(':')
                            .append(x.target.getFirst().toShortString()).append(':').append(x.target.getSecond().toShortString()).append(':')
                            .append(Double.doubleToLongBits(x.targetLocation)).append(';');
                    }
                    text.append(']');
                }
            }
            text.append("deferred:");for(TrackEdge edge:s.graph().deferredIntersectionUpdates)text.append(names.get(edge)).append(';');
            RailFixture.check(edges==(shape.crossing()+shape.disjoint())*2+2,"adoption count");
            RailFixture.check((shape.crossing()==0)==(intersections==0),"fixture intersections");
            String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8)));
            return new RailFixture.Trial(elapsed,digest,intersections,s.graph().deferredIntersectionUpdates.size(),edges);
        }finally{Create.RAILWAYS.trackNetworks=s.previousNetworks();Create.RAILWAYS.sync=s.previousSync();}
    }
    static RailFixture.Trial[] pair(MinecraftServer server,RailFixture.Shape shape,boolean originalFirst)throws Exception {
        RailFixture.Trial a,b;
        if(originalFirst){a=trial(server,shape,true);b=trial(server,shape,false);}
        else{b=trial(server,shape,false);a=trial(server,shape,true);}
        RailFixture.check(a.geometry().equals(b.geometry())&&a.intersections()==b.intersections()
            &&a.deferred()==b.deferred()&&a.edgeCount()==b.edgeCount(),"paired outputs "+shape.name());
        return new RailFixture.Trial[]{a,b};
    }
    static long median(List<Long> values){long[] x=values.stream().mapToLong(Long::longValue).sorted().toArray();return x[x.length/2];}
    static void run(MinecraftServer server) {
        Map<String,Object> report=new LinkedHashMap<>();
        try {
            RailFixture.check(server.isSameThread(),"owner");RailFixture.check(Files.isRegularFile(Path.of(".valen-disposable-benchmark")),"disposable");
            boolean reversed=Boolean.getBoolean("valen.rail.reverse");
            int samples=Integer.getInteger("valen.rail.samples",15),warmup=Integer.getInteger("valen.rail.warmup",8);
            RailFixture.check(samples>=3&&samples<=15&&warmup>=1&&warmup<=8,"bounds");
            List<RailFixture.Shape> shapes=new ArrayList<>(List.of(RailFixture.SHAPES));
            shapes.add(new RailFixture.Shape("unsupported32",32,0,true));
            List<Map<String,Object>> firstUse=new ArrayList<>();
            for(RailFixture.Shape shape:shapes) {
                var pair=pair(server,shape,!reversed);
                firstUse.add(Map.of("case",shape.name(),"original_ns",pair[0].nanos(),"candidate_ns",pair[1].nanos()));
            }
            var training=new RailFixture.Shape("warmup_fresh_cold32",32,0,false);
            for(int i=0;i<40;i++)pair(server,training,((i&1)==0)^reversed);
            List<Map<String,Object>> rows=new ArrayList<>();
            for(RailFixture.Shape shape:shapes) {
                List<Long> oldTimes=new ArrayList<>(),newTimes=new ArrayList<>();String reference=null;
                RailFixture.Trial last=null;
                for(int i=-warmup;i<samples;i++) {
                    var results=pair(server,shape,((i&1)==0)^reversed);last=results[0];
                    if(reference==null)reference=last.geometry();
                    RailFixture.check(reference.equals(last.geometry()),"within-run equality");
                    if(i>=0){oldTimes.add(results[0].nanos());newTimes.add(results[1].nanos());}
                }
                Map<String,Object> row=new LinkedHashMap<>();row.put("case",shape.name());
                row.put("original_samples_ns",oldTimes);row.put("candidate_samples_ns",newTimes);
                row.put("original_median_ns",median(oldTimes));row.put("candidate_median_ns",median(newTimes));
                row.put("geometry_sha256",reference);row.put("intersection_records",last.intersections());
                row.put("deferred_updates",last.deferred());row.put("adopted_edge_count",last.edgeCount());row.put("equal_output",true);
                rows.add(row);
            }
            report.put("passed",true);report.put("cases",rows);report.put("first_use_observations",firstUse);
            report.put("same_jvm_paired",true);report.put("reversed_initial_order",reversed);
            report.put("global_full_call_warmup_per_runner",40);report.put("per_case_warmup",warmup);report.put("samples",samples);
            report.put("runtime",System.getProperty("java.runtime.version"));report.put("parallelism",System.getProperty("valenmultithreaderd.parallelism"));
            report.put("direct_calls_symmetric",true);report.put("observers_in_timed_path",false);report.put("production_installed",false);
            report.put("scope","Complete connectNodes preparation/geometry/UUID/dedup/publication; each call gets distinct fresh graph and Bezier objects.");
        }catch(Throwable failure){failure.printStackTrace();report.put("passed",false);report.put("error",failure.toString());}
        finally {
            try{Files.writeString(Path.of("rail-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}
            catch(Throwable e){e.printStackTrace();}
            server.halt(false);
        }
    }
}
