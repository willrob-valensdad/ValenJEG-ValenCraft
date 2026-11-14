package top.theillusivec4.curios.api.client;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.SlotContext;
/** Registers Scorched ammo-box renderers on the pack's Trinkets renderer. */
public final class CuriosRendererRegistry {
    private CuriosRendererRegistry() {}
    public static void register(Item item, Supplier<? extends ICurioRenderer> renderer) {
        TrinketRendererRegistry.registerRenderer(item, context -> (TrinketRenderer) (stack, access, model, pose, collector, light, state, yRot, xRot) -> {
            LivingEntity entity = access.inventory().getAttachment().getEntity();
            SlotContext slot = new SlotContext(access.slotType().getId(), entity, access.index());
            renderer.get().render(stack, slot, pose, collector, light, state, new ModelParent<>(model), context, yRot, xRot);
        });
    }
    private static final class ModelParent<S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements RenderLayerParent<S, M> {
        private final M model;
        @SuppressWarnings("unchecked")
        private ModelParent(EntityModel<?> model) { this.model = (M) model; }
        @Override public M getModel() { return model; }
    }
}
