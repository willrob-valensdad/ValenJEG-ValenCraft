package earth.terrarium.common_storage_lib.resources.item;

import net.msrandom.multiplatform.annotations.Actual;

public class ItemResourceActual {
    @Actual
    private static ItemResource getCraftingRemainder(ItemResource resource) {
        return ItemResource.of(resource.getCachedStack().getItem().getCraftingRemainder().create());
    }

    @Actual
    private static boolean hasCraftingRemainder(ItemResource resource) {
        return resource.getCachedStack().getItem().getCraftingRemainder().create().isEmpty();
    }
}
