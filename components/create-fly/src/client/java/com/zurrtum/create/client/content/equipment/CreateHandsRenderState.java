package com.zurrtum.create.client.content.equipment;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Create-only values captured alongside Minecraft's existing first-person hand state. */
public final class CreateHandsRenderState {
    public final GadgetState zapper = new GadgetState();
    public final GadgetState potatoCannon = new GadgetState();
    public final ItemStackRenderState extendoGrip = new ItemStackRenderState();
    public @Nullable Identifier backtankTexture;

    public static CreateHandsRenderState of(FirstPersonHandsAndItemsRenderState state) {
        return ((Access) state).create$hands();
    }

    public static final class GadgetState {
        public float leftRecoil;
        public float rightRecoil;
        public boolean skipLeftEquip;
        public boolean skipRightEquip;
    }

    public interface Access {
        CreateHandsRenderState create$hands();
    }
}
