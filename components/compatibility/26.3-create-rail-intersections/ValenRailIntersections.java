package com.zurrtum.create.content.trains.graph;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.IntConsumer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Candidate only: one geometry batch, followed by the author's ordered owner publication. */
final class ValenRailIntersections {
    private static final MethodHandle RANGE=findRange();
    private static final int MIN_CANDIDATES=16;
    private static final long MIN_SEGMENT_PAIRS=65536;
    private ValenRailIntersections() {}
    private static MethodHandle findRange() {
        try {
            Class<?> type=Class.forName("java.util.concurrent.ValenMultithreaderd",true,null);
            return MethodHandles.publicLookup().findStatic(type,"parallelFor",
                MethodType.methodType(void.class,int.class,int.class,int.class,IntConsumer.class));
        } catch(ClassNotFoundException ordinaryJdk) {return null;}
        catch(ReflectiveOperationException incompatibleRuntime) {throw new ExceptionInInitializerError(incompatibleRuntime);}
    }
    private static boolean knownLocation(TrackNode node) {
        if(node==null||node.getClass()!=TrackNode.class||node.location==null)return false;
        return knownPoint(node.location);
    }
    private static boolean knownPoint(TrackNodeLocation point) {
        if(point==null)return false;
        Class<?> type=point.getClass();
        return type==TrackNodeLocation.class||type==DiscoveredLocation.class;
    }
    private static boolean knownMap(Map<?,?> map) {
        if(map==null)return false;
        Class<?> type=map.getClass();
        return type==HashMap.class||type==IdentityHashMap.class;
    }
    private static boolean knownIterationMap(Map<?,?> map) {
        // These maps are only iterated; access-ordered LinkedHashMap lookups are
        // deliberately excluded from the connection maps read during collection.
        return knownMap(map)||(map!=null&&map.getClass()==LinkedHashMap.class);
    }
    private static boolean knownVectors(Couple<Vec3> pair) {
        return pair!=null&&pair.getClass()==Couple.class&&pair.getFirst()!=null&&pair.getSecond()!=null
            &&pair.getFirst().getClass()==Vec3.class&&pair.getSecond().getClass()==Vec3.class;
    }
    private static boolean knownTurn(BezierConnection turn) {
        return turn==null||(turn.getClass()==BezierConnection.class&&knownVectors(turn.starts)&&knownVectors(turn.axes));
    }
    private static boolean knownData(EdgeData data) {
        if(data==null||data.getClass()!=EdgeData.class)return false;
        List<TrackEdgeIntersection> values=data.getIntersections();
        if(values==null||values.getClass()!=ArrayList.class)return false;
        for(TrackEdgeIntersection value:values)
            if(value==null||value.getClass()!=TrackEdgeIntersection.class||value.target==null||value.target.getClass()!=Couple.class
                ||!knownPoint(value.target.getFirst())||!knownPoint(value.target.getSecond()))
                return false;
        return true;
    }
    private static boolean knownEdge(TrackEdge edge,TrackNode first,TrackNode second) {
        return edge!=null&&edge.getClass()==TrackEdge.class && knownTurn(edge.turn)&&knownData(edge.edgeData)
            && knownLocation(first)&&knownLocation(second)&&knownLocation(edge.node1)&&knownLocation(edge.node2);
    }
    private static final class Job {
        final TrackGraph graph;final TrackNode first,second;final TrackEdge edge;
        Collection<double[]> intersections;Throwable failure;
        Job(TrackGraph graph,TrackNode first,TrackNode second,TrackEdge edge) {
            this.graph=graph;this.first=first;this.second=second;this.edge=edge;
        }
    }
    static boolean tryConnect(LevelAccessor reader,TrackGraph current,TrackNode first,TrackNode second,
                              TrackEdge edge,TrackEdge reverse,boolean bezier) {
        if(RANGE==null||reader==null||reader.getClass()!=ServerLevel.class||reader.getServer()==null||!reader.getServer().isSameThread()
            ||current.getClass()!=TrackGraph.class||!knownEdge(edge,first,second)||!knownEdge(reverse,second,first)
            ||!knownIterationMap(current.nodes)||!knownMap(current.connectionsByNode)
            ||current.deferredIntersectionUpdates==null||current.deferredIntersectionUpdates.getClass()!=ArrayList.class
            ||!knownIterationMap(Create.RAILWAYS.trackNetworks))return false;
        ArrayList<Job> jobs=new ArrayList<>();
        // Only author geometry implementations may enter a worker. Encountering any
        // unknown implementation leaves the original serial method responsible.
        for(TrackGraph graph:Create.RAILWAYS.trackNetworks.values()) {
            if(graph==null||graph.getClass()!=TrackGraph.class||!knownIterationMap(graph.nodes)||!knownMap(graph.connectionsByNode)
                ||graph.deferredIntersectionUpdates==null||graph.deferredIntersectionUpdates.getClass()!=ArrayList.class)return false;
            for(TrackNode otherFirst:graph.nodes.values()) {
                if(!knownLocation(otherFirst))return false;
                Map<TrackNode,TrackEdge> connections=graph.connectionsByNode.get(otherFirst);
                if(connections==null)continue;
                if(!knownMap(connections))return false;
                for(Map.Entry<TrackNode,TrackEdge> item:connections.entrySet()) {
                    TrackNode otherSecond=item.getKey();TrackEdge other=item.getValue();
                    if(!knownEdge(other,otherFirst,otherSecond))return false;
                    if(graph==current&&(otherFirst==first||otherSecond==first||otherFirst==second||otherSecond==second))continue;
                    if(edge==other)continue;
                    if(other.isInterDimensional()||edge.isInterDimensional())continue;
                    if(first.location.dimension!=otherFirst.location.dimension)continue;
                    if(!bezier&&!other.isTurn())continue;
                    if(other.isTurn()&&other.turn.isPrimary())continue;
                    Map<TrackNode,TrackEdge> back=graph.connectionsByNode.get(otherSecond);
                    if(back!=null) {
                        if(!knownMap(back))return false;
                        TrackEdge otherReverse=back.get(otherFirst);
                        if(otherReverse!=null&&!knownEdge(otherReverse,otherSecond,otherFirst))return false;
                    }
                    jobs.add(new Job(graph,otherFirst,otherSecond,other));
                }
            }
        }
        if(jobs.size()<MIN_CANDIDATES)return false;
        ArrayList<Job> work=new ArrayList<>();
        long segmentPairs=0;
        for(Job job:jobs) {
            try {
                // Match only the author's early geometric rejections. resolve() publishes
                // complete immutable geometry through AtomicReference; no global snapshot.
                if(!overlaps(edge,first,second,job)) {
                    job.intersections=Collections.emptyList();
                    continue;
                }
                long left=edge.turn==null?1:edge.turn.getSegmentCount();
                long right=job.edge.turn==null?1:job.edge.turn.getSegmentCount();
                segmentPairs=Math.min(1_000_000_000L,segmentPairs+left*right);
                work.add(job);
            } catch(Throwable failure) {
                job.failure=failure;
                break; // Later geometry is unnecessary after an ordered preparation failure.
            }
        }
        IntConsumer compute=i->{
            Job job=work.get(i);
            try {job.intersections=edge.getIntersection(first,second,job.edge,job.first,job.second);}
            catch(Throwable failure){job.failure=failure;}
        };
        if(work.size()>=4&&segmentPairs>=MIN_SEGMENT_PAIRS) {
            try {RANGE.invokeExact(0,work.size(),1,compute);}
            catch(Throwable failure){ValenRailIntersections.<RuntimeException>rethrow(failure);}
        } else {
            for(int i=0;i<work.size();i++)compute.accept(i);
        }
        // Geometry failures preserve the original successful mutation prefix and UUID
        // order. Even an empty intersection collection consumes its candidate UUID.
        for(Job job:jobs) {
            if(job.failure!=null)ValenRailIntersections.<RuntimeException>rethrow(job.failure);
            UUID id=UUID.randomUUID();
            for(double[] intersection:job.intersections) {
                double s=intersection[0],t=intersection[1];
                edge.edgeData.addIntersection(current,id,s,job.first,job.second,t);
                reverse.edgeData.addIntersection(current,id,edge.getLength()-s,job.first,job.second,t);
                job.edge.edgeData.addIntersection(job.graph,id,t,first,second,s);
                TrackEdge otherReverse=job.graph.getConnection(Couple.create(job.second,job.first));
                if(otherReverse!=null)otherReverse.edgeData.addIntersection(job.graph,id,job.edge.getLength()-t,first,second,s);
            }
        }
        current.putConnection(first,second,edge);
        current.putConnection(second,first,reverse);
        Create.RAILWAYS.sync.edgeAdded(current,first,second,edge);
        Create.RAILWAYS.sync.edgeAdded(current,second,first,reverse);
        current.markDirty();
        return true;
    }
    private static boolean overlaps(TrackEdge edge,TrackNode first,TrackNode second,Job job) {
        Vec3 a=first.location.getLocation(),b=second.location.getLocation();
        Vec3 c=job.first.location.getLocation(),d=job.second.location.getLocation();
        if(a.y!=b.y||a.y!=c.y||a.y!=d.y)return false;
        if(edge.turn==null)return job.edge.turn.getBounds().intersects(a,b);
        if(job.edge.turn==null)return edge.turn.getBounds().intersects(c,d);
        return edge.turn.getBounds().intersects(job.edge.turn.getBounds());
    }
    @SuppressWarnings("unchecked")
    private static <T extends Throwable>void rethrow(Throwable failure)throws T{throw (T)failure;}
}
