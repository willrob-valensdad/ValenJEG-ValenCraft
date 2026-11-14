package com.zurrtum.create.content.trains.graph;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
/** Intrusive correctness-only hooks. Never packaged into a timed Create JAR. */
public final class RailObserveHooks {
    static Thread owner;static volatile boolean active;static boolean forceOverlap;static long uuids;
    static int failNode=-1;static final RuntimeException FAILURE=new RuntimeException("fixture geometry failure");
    static StringBuilder trace=new StringBuilder();
    static Set<String> threads=ConcurrentHashMap.newKeySet();
    static AtomicInteger calls=new AtomicInteger();
    static CountDownLatch ownerIn,workerIn;
    public static void begin(boolean requireOverlap) {
        owner=Thread.currentThread();active=true;forceOverlap=requireOverlap;uuids=0;failNode=-1;trace.setLength(0);
        threads.clear();calls.set(0);ownerIn=new CountDownLatch(1);workerIn=new CountDownLatch(1);
    }
    static void onOwner() {if(Thread.currentThread()!=owner)throw new AssertionError("owner publication escaped");}
    static void await(CountDownLatch latch) {
        try{if(!latch.await(10,TimeUnit.SECONDS))throw new AssertionError("geometry overlap absent");}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}
    }
    public static UUID uuid() {
        if(!active)return UUID.randomUUID();
        onOwner();trace.append("uuid:").append(++uuids).append(';');return new UUID(0,uuids);
    }
    public static void geometry(TrackNode other) {
        if(!active)return;
        threads.add(Thread.currentThread().getName());calls.incrementAndGet();
        if(other.getNetId()==failNode)throw FAILURE;
        if(forceOverlap) {
            if(Thread.currentThread()==owner){ownerIn.countDown();await(workerIn);}
            else{workerIn.countDown();await(ownerIn);}
        }
    }
    public static void commit(TrackGraph graph,UUID id,double position,TrackNode a,TrackNode b,double target) {
        if(!active)return;onOwner();
        trace.append("add:").append(id).append(':').append(Long.toHexString(Double.doubleToLongBits(position)))
            .append(':').append(a.getNetId()).append(':').append(b.getNetId()).append(':')
            .append(Long.toHexString(Double.doubleToLongBits(target))).append(';');
    }
    public static void defer(TrackEdge edge) {
        if(!active)return;onOwner();trace.append("defer:").append(edge.node1.getNetId()).append(':').append(edge.node2.getNetId()).append(';');
    }
    public static void publish(String kind,TrackNode a,TrackNode b) {
        if(!active)return;onOwner();trace.append(kind).append(':').append(a.getNetId()).append(':').append(b.getNetId()).append(';');
    }
    public static void dirty(){if(active){onOwner();trace.append("dirty;");}}
    public static Map<String,Object> end()throws Exception {
        active=false;
        return Map.of("uuid_calls",uuids,"owner_trace_sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(trace.toString().getBytes(StandardCharsets.UTF_8))),
            "geometry_calls",calls.get(),"geometry_threads",new TreeSet<>(threads),"forced_overlap",forceOverlap);
    }
}
