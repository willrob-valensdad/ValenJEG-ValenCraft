package com.mrcrayfish.framework.client.resources;
import com.mrcrayfish.framework.api.serialize.DataObject;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
public interface IDataLoader<T extends IResourceSupplier> {
    List<T> getResourceSuppliers();
    void process(List<Pair<T, DataObject>> loaded);
    boolean ignoreMissing();
}
