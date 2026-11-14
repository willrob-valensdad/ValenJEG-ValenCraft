package com.zurrtum.create.client.vanillin.item;

import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedItemModelBufferer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemChunkLayerSortedListBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.MeshHelper;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.vanillin.Vanillin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ItemModels {
    public static final TagKey<Item> NO_INSTANCING = TagKey.create(Registries.ITEM, Vanillin.rl("no_instancing"));
    private static final Model EMPTY_MODEL = new SimpleModel(List.of());
    private static final RendererReloadCache<BakedModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(key -> bakeModel(key.world(),
        key.stack(),
        key.displayContext()
    ));
    private static final RendererReloadCache<BakedModelKey, Boolean> SUPPORT_CACHE = new RendererReloadCache<>(key -> {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
            state, key.stack(), key.displayContext(), key.world(), null, 0);
        return BakedItemModelBufferer.supports(state);
    });

    public static boolean isSupported(ItemStack stack, ItemDisplayContext context) {
        return !stack.is(NO_INSTANCING) && SUPPORT_CACHE.get(
            new BakedModelKey(Minecraft.getInstance().level, stack, context));
    }

    public static ItemModel getModel(ItemStack stack) {
        return Minecraft.getInstance().getModelManager().getItemModel(stack.get(DataComponents.ITEM_MODEL));
    }

    public static Model get(Level world, ItemStack itemStack, ItemDisplayContext displayContext) {
        if (itemStack.isEmpty()) {
            return EMPTY_MODEL;
        }
        ClientLevel clientWorld = world instanceof ClientLevel ? (ClientLevel) world : null;
        return MODEL_CACHE.get(new BakedModelKey(clientWorld, itemStack, displayContext));
    }

    public static Model bakeModel(ClientLevel world, ItemStack itemStack, ItemDisplayContext displayContext) {
        var builder = ItemChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();
        BakedItemModelBufferer.bufferItemStack(
            itemStack, world, displayContext, (renderType, shaded, data) -> {
                var material = ModelUtil.getItemMaterial(renderType);
                Objects.requireNonNull(material, "Admitted item render material");
                if (itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
                    material = SimpleMaterial.builderOf(material).transparency(Transparency.ORDER_INDEPENDENT).build();
                }
                Mesh mesh = MeshHelper.blockVerticesToMesh(
                    data,
                    "source=ItemModels,ItemStack=" + itemStack + ",renderType=" + renderType
                );
                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }, (renderType, material, mesh, translucent) -> {
                if (translucent && itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
                    material = SimpleMaterial.builderOf(material).transparency(Transparency.ORDER_INDEPENDENT).build();
                }
                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }
        );
        return new SimpleModel(builder.build());
    }

    public record BakedModelKey(ClientLevel world, ItemStack stack, ItemDisplayContext displayContext) {
        public BakedModelKey {
            stack = stack.copy();
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, ItemStack.hashItemAndComponents(stack), stack.getCount(), displayContext);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BakedModelKey(
                ClientLevel otherWorld, ItemStack otherStack, ItemDisplayContext otherDisplayContext
            ))) {
                return false;
            }
            boolean stackEqual = stack.getCount() == otherStack.getCount()
                && ItemStack.isSameItemSameComponents(stack, otherStack);
            return world == otherWorld && stackEqual && displayContext == otherDisplayContext;
        }
    }

}
