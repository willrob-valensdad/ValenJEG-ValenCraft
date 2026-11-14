package com.zurrtum.create.client.compat.rei.renderer;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FluidStackRenderer(EntryRenderer<FluidStack> origin) implements EntryRenderer<FluidStack> {
    @Override
    public void render(
        EntryStack<FluidStack> entry,
        GuiGraphics graphics,
        Rectangle bounds,
        int mouseX,
        int mouseY,
        float delta
    ) {
        FluidStack stack = entry.getValue();
        Fluid fluid = stack.getFluid();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        int color = AllFluidConfigs.getTint(null, 0, 0, 0, fluid.defaultFluidState(), model, fluid, stack.getPatch()) | 0xff000000;
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            model.stillMaterial().sprite(),
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            color
        );
    }

    @Override
    @Nullable
    public Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        Tooltip tooltip = origin.getTooltip(entry, context);
        if (tooltip == null) {
            return null;
        }
        List<Tooltip.Entry> entries = tooltip.entries();
        Tooltip.Entry first = entries.getFirst();
        if (first.isText()) {
            FluidStack stack = entry.getValue();
            if (stack.getFluid() == AllFluids.POTION) {
                DataComponentMap components = stack.getComponents();
                PotionContents contents = components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                BottleType bottleType = components.getOrDefault(
                    AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
                    BottleType.REGULAR
                );
                Component name = contents.getName(PotionFluidHandler.itemFromBottleType(bottleType)
                    .getDescriptionId() + ".effect.");
                List<Tooltip.Entry> list = new ArrayList<>();
                list.add(Tooltip.entry(name));
                Float scale = components.get(DataComponents.POTION_DURATION_SCALE);
                if (scale == null) {
                    if (bottleType == BottleType.LINGERING) {
                        scale = Items.LINGERING_POTION.components()
                            .getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
                    } else {
                        scale = 1.0f;
                    }
                }
                PotionContents.addPotionTooltip(
                    contents.getAllEffects(),
                    text -> list.add(Tooltip.entry(text)),
                    scale,
                    context.vanillaContext().tickRate()
                );
                contents.addToTooltip(
                    context.vanillaContext(),
                    text -> list.add(Tooltip.entry(text)),
                    context.getFlag(),
                    components
                );
                entries.removeFirst();
                entries.addAll(0, list);
            }
        }
        return tooltip;
    }
}
