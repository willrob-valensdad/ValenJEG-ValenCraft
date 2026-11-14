package earth.terrarium.common_storage_lib.lookup;

import earth.terrarium.common_storage_lib.lookup.impl.FabricEntityLookup;
import net.minecraft.resources.Identifier;
import net.msrandom.multiplatform.annotations.Actual;

public interface EntityLookupActual {
    @Actual
    static <T, C> EntityLookup<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return new FabricEntityLookup<>(name, typeClass, contextClass);
    }
}
