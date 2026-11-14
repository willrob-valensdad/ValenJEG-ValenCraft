package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Unique
    private static final Set<Identifier> CUSTOM_GUI_ICON_ITEMS = Set.of(
            Mod.loc("lunge_mine")
    );

    @Shadow
    protected abstract UnbakedModel getModel(Identifier location);

    @Shadow
    protected abstract void registerModelAndLoadDependencies(ModelResourceLocation location, UnbakedModel model);

    @Inject(
            method = "<init>(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/Map;Ljava/util/Map;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadSpecialItemModelAndDependencies(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V", ordinal = 0)
    )
    private void onAfterItemModels(BlockColors colors, ProfilerFiller profiler,
                                   Map<Identifier, BlockModel> blockModels,
                                   Map<Identifier, List<BlockStateModelLoader.LoadedJson>> blockStates,
                                   CallbackInfo ci) {
        for (Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            if (usesGuiIconModel(entry.getKey().location(), entry.getValue())) {
                Identifier itemId = entry.getKey().location();
                Identifier iconId = itemId.withPath(path -> path + "_icon");
                Identifier modelPath = iconId.withPrefix("item/");
                UnbakedModel model = this.getModel(modelPath);
                ModelResourceLocation inventoryLocation = ModelResourceLocation.inventory(iconId);
                this.registerModelAndLoadDependencies(inventoryLocation, model);

                if (CUSTOM_GUI_ICON_ITEMS.contains(itemId)) {
                    Identifier thirdPersonId = itemId.withPath(path -> path + "_3d");
                    UnbakedModel thirdPersonModel = this.getModel(thirdPersonId.withPrefix("item/"));
                    this.registerModelAndLoadDependencies(ModelResourceLocation.inventory(thirdPersonId), thirdPersonModel);
                }
            }
        }
    }

    @Unique
    private static boolean usesGuiIconModel(Identifier itemId, Item item) {
        return item instanceof GunGeoItem || CUSTOM_GUI_ICON_ITEMS.contains(itemId);
    }
}
