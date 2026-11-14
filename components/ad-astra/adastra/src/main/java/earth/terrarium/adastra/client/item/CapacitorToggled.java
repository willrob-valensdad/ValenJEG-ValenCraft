package earth.terrarium.adastra.client.item;

import com.mojang.serialization.MapCodec;
import earth.terrarium.adastra.common.items.EtrionicCapacitorItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

public record CapacitorToggled() implements RangeSelectItemModelProperty {
    public static final MapCodec<CapacitorToggled> CODEC = MapCodec.unit(new CapacitorToggled());

    @Override
    public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        return EtrionicCapacitorItem.active(stack) ? 0.0f : 1.0f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }
}
