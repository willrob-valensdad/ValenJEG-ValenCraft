package ttv.migami.jeg.util;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
/** Loaded-entity lifecycle and snapshot semantics; no live world is altered. */
public final class EntityTickIndexContract {
 private record Entity(int id,int generation) {}
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  var index=new EntityTickIndex<String,Entity>();
  List<Entity> originals=new ArrayList<>();
  for(int i=0;i<8192;i++){var e=new Entity(i,0);originals.add(e);index.loaded("world",i,e);}
  List<Entity> tick=index.snapshot("world");int visited=0;
  for(Entity e:tick){
   if(visited++==0){
    for(int i=0;i<8160;i++)index.unloaded("world",i,originals.get(i));
    for(int i=9000;i<10024;i++)index.loaded("world",i,new Entity(i,1));
   }
  }
  check(tick.equals(originals)&&visited==8192,"Entity removals/rehash changed an in-flight tick snapshot");
  check(index.snapshot("world").size()==1056,"Following tick did not reflect load/unload changes");
  var old=new Entity(3,0);var replacement=new Entity(3,1);
  index.loaded("other",3,old);index.loaded("other",3,replacement);index.unloaded("other",3,old);
  check(index.snapshot("other").equals(List.of(replacement)),"Stale unload removed a replacement entity");
  index.unloaded("other",3,replacement);
  check(index.snapshot("other").isEmpty(),"Unloaded level retained an entity");
  index.clear();check(index.snapshot("world").isEmpty(),"Stopped server retained entities");
  var error=new AtomicReference<Throwable>();
  Thread writer=new Thread(()->{try{for(int i=0;i<50000;i++){var e=new Entity(i%128,i);index.loaded("concurrent",e.id(),e);if(i%2==0)index.unloaded("concurrent",e.id(),e);}}catch(Throwable t){error.set(t);}});
  writer.start();int snapshots=0;
  do{var snapshot=index.snapshot("concurrent");check(snapshot.stream().noneMatch(Objects::isNull),"Snapshot contains null");check(new HashSet<>(snapshot).size()==snapshot.size(),"Snapshot contains duplicates");snapshots++;}while(writer.isAlive());
  writer.join();if(error.get()!=null)throw new AssertionError(error.get());
  check(snapshots>0,"Concurrency contract never exercised");
  index.clear();check(index.snapshot("concurrent").isEmpty(),"Concurrent state did not clear");
  System.out.println("ENTITY_TICK_INDEX_CONTRACT_PASS 8192 mutation-safe visits; 50000 concurrent lifecycle events; "+snapshots+" stable snapshots; replacement/level/stop checks");
 }
}
