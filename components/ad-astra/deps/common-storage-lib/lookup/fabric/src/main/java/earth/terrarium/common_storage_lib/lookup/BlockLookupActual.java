package earth.terrarium.common_storage_lib.lookup;

import earth.terrarium.common_storage_lib.lookup.impl.FabricBlockLookup;
import net.minecraft.resources.Identifier;
import net.msrandom.multiplatform.annotations.Actual;

public interface BlockLookupActual {
    @Actual
    static <T, C> BlockLookup<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return new FabricBlockLookup<>(name, typeClass, contextClass);
    }
}
