package top.ribs.scguns.fabric.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ribs.scguns.fabric.ScorchedFabric;

@Mixin(CreativeModeTab.class)
public class ScorchedCreativeTabMixin {
    @Redirect(
            method = "buildContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;accept(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V"
            )
    )
    private void scguns$contents(CreativeModeTab.DisplayItemsGenerator generator, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        generator.accept(parameters, output);
        ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey((CreativeModeTab) (Object) this).orElse(null);
        if (key != null) {
            ScorchedFabric.MOD_BUS.post(new BuildCreativeModeTabContentsEvent(key, parameters, output));
        }
    }
}
