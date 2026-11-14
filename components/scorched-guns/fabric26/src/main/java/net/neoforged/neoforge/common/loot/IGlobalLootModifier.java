package net.neoforged.neoforge.common.loot;
import com.mojang.serialization.MapCodec;
public interface IGlobalLootModifier {
    MapCodec<? extends IGlobalLootModifier> codec();
}
