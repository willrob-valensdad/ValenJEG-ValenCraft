package ttv.migami.jeg.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks loaded entities without traversing the world's mutable lookup iterator.
 * The lock protects only membership and copying; callbacks run after it is released.
 */
public final class EntityTickIndex<L,E> {
    private final Map<L,LinkedHashMap<Integer,E>> levels=new HashMap<>();

    public synchronized void loaded(L level,int id,E entity) {
        levels.computeIfAbsent(level,ignored->new LinkedHashMap<>()).put(id,entity);
    }

    public synchronized void unloaded(L level,int id,E entity) {
        var entities=levels.get(level);
        if(entities==null || entities.get(id)!=entity)return;
        entities.remove(id);
        if(entities.isEmpty())levels.remove(level);
    }

    public synchronized List<E> snapshot(L level) {
        var entities=levels.get(level);
        return entities==null?List.of():List.copyOf(entities.values());
    }

    public synchronized void clear() {
        levels.clear();
    }
}
