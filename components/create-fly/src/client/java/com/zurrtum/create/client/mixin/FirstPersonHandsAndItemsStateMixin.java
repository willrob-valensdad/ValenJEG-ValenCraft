package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.CreateHandsRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FirstPersonHandsAndItemsRenderState.class)
public class FirstPersonHandsAndItemsStateMixin implements CreateHandsRenderState.Access {
    @Unique
    private final CreateHandsRenderState create$hands = new CreateHandsRenderState();

    @Override
    public CreateHandsRenderState create$hands() {
        return create$hands;
    }
}
