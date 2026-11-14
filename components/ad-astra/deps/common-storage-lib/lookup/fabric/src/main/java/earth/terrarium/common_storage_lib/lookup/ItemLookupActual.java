package earth.terrarium.common_storage_lib.lookup;

import earth.terrarium.common_storage_lib.lookup.impl.FabricItemLookup;
import net.minecraft.resources.Identifier;
import net.msrandom.multiplatform.annotations.Actual;

public interface ItemLookupActual {
    @Actual
    static <T, C> ItemLookup<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return new FabricItemLookup<>(name, typeClass, contextClass);
    }
}
