package com.zurrtum.create.content.trains.graph;

import com.google.gson.GsonBuilder;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.track.BezierConnection;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Intrusive ordered-publication oracle, always separate from timed fixture. */
public final class RailOracle implements ModInitializer {
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if(Boolean.getBoolean("valen.rail.fixture"))server.execute(()->run(server));
        });
    }
    static final class OwnerEdge extends TrackEdge {
        OwnerEdge(TrackEdge edge){super(edge.node1,edge.node2,edge.turn,AllTrackMaterials.ANDESITE);}
        @Override public Vec3 getPosition(TrackGraph graph,double t) {
            RailObserveHooks.onOwner();return super.getPosition(graph,t);
        }
    }
    static final class MutatingData extends EdgeData {
        final TrackNode later;boolean changed;
        MutatingData(TrackEdge edge,TrackNode later){super(edge);this.later=later;}
        @Override public void addIntersection(TrackGraph graph,UUID id,double position,TrackNode a,TrackNode b,double target) {
            RailObserveHooks.onOwner();super.addIntersection(graph,id,position,a,b,target);
            if(!changed){changed=true;later.location.yOffsetPixels=8;}
        }
    }
    static final class MutatingNode extends TrackNode {
        final TrackNode later;boolean changed;
        MutatingNode(TrackNode original,TrackNode later){super(original.location,original.netId,original.normal);this.later=later;}
        @Override public TrackNodeLocation getLocation() {
            RailObserveHooks.onOwner();
            if(!changed){changed=true;later.location.yOffsetPixels=8;}
            return super.getLocation();
        }
    }
    static final class MutatingIntersection extends TrackEdgeIntersection {
        final TrackNode later;
        MutatingIntersection(TrackNode later){this.later=later;}
        @Override public boolean isNear(double position){RailObserveHooks.onOwner();later.location.yOffsetPixels=8;return false;}
    }
    static final class MutatingDeferred extends ArrayList<TrackEdge> {
        final TrackNode later;
        MutatingDeferred(TrackNode later){this.later=later;}
        @Override public boolean add(TrackEdge edge) {
            RailObserveHooks.onOwner();later.location.yOffsetPixels=8;return super.add(edge);
        }
    }
    static TrackNode node(TrackGraph graph,int id) {
        return graph.nodes.values().stream().filter(n->n.netId==id).findFirst().orElseThrow();
    }
    static void replaceNode(TrackGraph graph,TrackNode old,TrackNode replacement) {
        graph.nodes.replace(old.location,replacement);graph.nodesById.put(old.netId,replacement);
        Map<TrackNode,TrackEdge> connections=graph.connectionsByNode.remove(old);graph.connectionsByNode.put(replacement,connections);
        for(var map:graph.connectionsByNode.values()) {
            if(map.containsKey(old)){TrackEdge edge=map.remove(old);map.put(replacement,edge);}
            for(TrackEdge edge:map.values()){if(edge.node1==old)edge.node1=replacement;if(edge.node2==old)edge.node2=replacement;}
        }
    }
    static String digest(TrackGraph graph) throws Exception {
        StringBuilder text=new StringBuilder();
        List<TrackGraph> graphs=new ArrayList<>(Create.RAILWAYS.trackNetworks.values());
        if(!graphs.contains(graph))graphs.add(graph);
        for(TrackGraph g:graphs) {
            text.append("graph:").append(g.id).append(';');
            for(TrackNode from:g.nodes.values()) {
                Map<TrackNode,TrackEdge> connections=g.connectionsByNode.get(from);
                if(connections==null)continue;
                for(var entry:connections.entrySet()) {
                    TrackEdge edge=entry.getValue();text.append(from.getNetId()).append("->").append(entry.getKey().getNetId()).append('[');
                    for(TrackEdgeIntersection x:edge.edgeData.getIntersections()) {
                        text.append(x.id).append(':').append(Double.doubleToLongBits(x.location)).append(':')
                            .append(x.target.getFirst().toShortString()).append(':').append(x.target.getSecond().toShortString())
                            .append(':').append(Double.doubleToLongBits(x.targetLocation)).append(';');
                    }
                    text.append(']');
                }
            }
            text.append("deferred:");
            for(TrackEdge edge:g.deferredIntersectionUpdates)text.append(edge.node1.getNetId()).append("->").append(edge.node2.getNetId()).append(';');
        }
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8)));
    }
    static Map<String,Object> one(MinecraftServer server,String name,RailFixture.Shape shape,boolean original) throws Exception {
        RailFixture.Setup s=RailFixture.setup(shape);
        // Match the author's connection-container types. Node iteration remains
        // ordered solely to make exact result/UUID comparison deterministic.
        Map<TrackNode,Map<TrackNode,TrackEdge>> identity=new IdentityHashMap<>();
        s.graph().connectionsByNode.forEach((node,map)->identity.put(node,new IdentityHashMap<>(map)));
        s.graph().connectionsByNode=identity;
        try {
            BezierConnection newTurn=s.turn();
            if(name.equals("straight_new"))newTurn=null;
            if(name.equals("straight_old")) {
                for(var connections:s.graph().connectionsByNode.values())
                    connections.replaceAll((target,edge)->new TrackEdge(edge.node1,edge.node2,null,AllTrackMaterials.ANDESITE));
            }
            if(name.equals("unknown_owner_edge")) {
                for(var connections:s.graph().connectionsByNode.values())
                    connections.replaceAll((target,edge)->edge.node1.getNetId()==3?new OwnerEdge(edge):edge);
            }
            if(name.equals("filters")) {
                for(TrackNode node:s.graph().nodes.values()) {
                    int id=node.getNetId();
                    if(id==3||id==4)node.location.dimension=Level.NETHER;
                    if(id==5)node.location.dimension=Level.END;
                    if(id==7||id==8)node.location.yOffsetPixels=8;
                }
                TrackNode first=s.graph().nodes.get(s.first()),other=s.graph().nodes.values().stream().filter(n->n.getNetId()==9).findFirst().orElseThrow();
                s.graph().connectionsByNode.put(first,new IdentityHashMap<>(Map.of(other,new TrackEdge(first,other,null,AllTrackMaterials.ANDESITE))));
            }
            if(name.equals("missing_reverse")) {
                for(TrackNode node:s.graph().nodes.values())if(node.getNetId()>2&&node.getNetId()%4==0)s.graph().connectionsByNode.remove(node);
            }
            if(name.equals("duplicate_network_edge")) {
                TrackGraph duplicate=new TrackGraph(new UUID(0,2));duplicate.nodes=new LinkedHashMap<>();duplicate.connectionsByNode=new IdentityHashMap<>();
                for(TrackNode node:s.graph().nodes.values())if(node.getNetId()>2) {
                    duplicate.nodes.put(node.location,node);
                    duplicate.connectionsByNode.put(node,new IdentityHashMap<>(s.graph().connectionsByNode.get(node)));
                }
                Create.RAILWAYS.trackNetworks.put(duplicate.id,duplicate);
            }
            if(name.startsWith("publication_")) {
                TrackNode firstOld=node(s.graph(),3),secondOld=node(s.graph(),4),later=node(s.graph(),35);
                TrackEdge forward=s.graph().connectionsByNode.get(firstOld).get(secondOld);
                TrackEdge reverse=s.graph().connectionsByNode.get(secondOld).get(firstOld);
                switch(name) {
                    case "publication_data" -> forward.edgeData=new MutatingData(forward,later);
                    case "publication_reverse_data" -> {
                        reverse.edgeData=new MutatingData(reverse,later);
                        s.graph().nodes.remove(secondOld.location); // Reachable only through reverse lookup.
                    }
                    case "publication_node" -> replaceNode(s.graph(),firstOld,new MutatingNode(firstOld,later));
                    case "publication_standalone_deferred" -> {
                        TrackGraph other=new TrackGraph(new UUID(0,2));
                        other.nodes=new LinkedHashMap<>();other.connectionsByNode=new IdentityHashMap<>();
                        for(TrackNode node:s.graph().nodes.values())if(node.netId>2) {
                            other.nodes.put(node.location,node);
                            other.connectionsByNode.put(node,new IdentityHashMap<>(s.graph().connectionsByNode.get(node)));
                        }
                        Create.RAILWAYS.trackNetworks.clear();Create.RAILWAYS.trackNetworks.put(other.id,other);
                        s.graph().deferredIntersectionUpdates=new MutatingDeferred(later);
                    }
                    case "publication_intersection" -> {
                        MutatingIntersection prior=new MutatingIntersection(later);
                        prior.id=new UUID(0,999);prior.location=-100;prior.targetLocation=-100;
                        prior.target=com.zurrtum.create.catnip.data.Couple.create(s.first(),s.second());
                        forward.edgeData.getIntersections().add(prior);
                    }
                    default -> throw new AssertionError(name);
                }
            }
            boolean candidate;
            try{Class.forName("com.zurrtum.create.content.trains.graph.ValenRailIntersections");candidate=true;}
            catch(ClassNotFoundException e){candidate=false;}
            boolean nativeRuntime;
            try{Class.forName("java.util.concurrent.ValenMultithreaderd",false,null);nativeRuntime=true;}
            catch(ClassNotFoundException e){nativeRuntime=false;}
            boolean overlap=!original&&name.equals("forced_overlap")&&candidate&&nativeRuntime&&Runtime.getRuntime().availableProcessors()>1;
            RailObserveHooks.begin(overlap);
            boolean expectedFailure=name.equals("failure_prefix");
            if(expectedFailure)RailObserveHooks.failNode=35;
            Throwable failure=null;
            try{
                if(original)s.graph().valen$connectNodesOriginal(server.overworld(),s.first(),s.second(),newTurn);
                else s.graph().connectNodes(server.overworld(),s.first(),s.second(),newTurn);
            }
            catch(Throwable e){failure=e;}
            RailFixture.check(expectedFailure?failure==RailObserveHooks.FAILURE:failure==null,"unexpected failure "+name+": "+failure);
            Map<String,Object> hooks=RailObserveHooks.end();
            if(overlap)RailFixture.check(((Set<?>)hooks.get("geometry_threads")).size()>1,"no worker participation");
            if(original||name.equals("unknown_owner_edge")||name.startsWith("publication_")||!candidate||!nativeRuntime)
                RailFixture.check(((Set<?>)hooks.get("geometry_threads")).size()<=1,"serial fallback escaped owner");
            Map<String,Object> row=new LinkedHashMap<>();row.put("case",name);row.put("ordered_graph_sha256",digest(s.graph()));
            row.putAll(hooks);row.put("expected_failure_identity",expectedFailure);row.put("passed",true);
            return row;
        }finally{RailObserveHooks.active=false;Create.RAILWAYS.trackNetworks=s.previousNetworks();Create.RAILWAYS.sync=s.previousSync();}
    }
    static void run(MinecraftServer server) {
        Map<String,Object> report=new LinkedHashMap<>();
        try {
            RailFixture.check(server.isSameThread(),"owner");RailFixture.check(Files.isRegularFile(Path.of(".valen-disposable-benchmark")),"disposable");
            List<Map<String,Object>> rows=new ArrayList<>(),controls=new ArrayList<>();
            List<RailFixture.Shape> shapes=new ArrayList<>(List.of(RailFixture.SHAPES));
            for(String name:List.of("straight_new","straight_old","filters","missing_reverse","duplicate_network_edge","unknown_owner_edge","failure_prefix","forced_overlap","publication_data","publication_reverse_data","publication_node","publication_intersection","publication_standalone_deferred"))
                shapes.add(new RailFixture.Shape(name,32,0,false));
            for(RailFixture.Shape shape:shapes) {
                Map<String,Object> control=one(server,shape.name(),shape,true);
                Map<String,Object> candidate=one(server,shape.name(),shape,false);
                for(String key:List.of("case","ordered_graph_sha256","uuid_calls","owner_trace_sha256","expected_failure_identity"))
                    RailFixture.check(Objects.equals(control.get(key),candidate.get(key)),"original/candidate mismatch "+shape.name()+" "+key);
                candidate.put("equal_original",true);rows.add(candidate);controls.add(control);
            }
            report.put("passed",true);report.put("cases",rows);report.put("original_controls",controls);
            report.put("same_jvm_exact_original_alias",true);report.put("intrusive_correctness_only",true);report.put("production_installed",false);
        }catch(Throwable failure){failure.printStackTrace();report.put("passed",false);report.put("error",failure.toString());}
        finally {
            try{Files.writeString(Path.of("rail-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}
            catch(Throwable e){e.printStackTrace();}
            server.halt(false);
        }
    }
}
