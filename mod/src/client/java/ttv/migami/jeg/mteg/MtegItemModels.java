package ttv.migami.jeg.mteg;
import com.google.gson.*;
import java.util.*;
import java.util.function.Consumer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import ttv.migami.jeg.item.attachment.AttachmentType;
import ttv.migami.jeg.item.attachment.GunAttachments;
/** Uses public Fabric/native item-model hooks; original cuboids/UVs/root transforms and continuous
 * original35 part programs remain authoritative. No sampled animation, fakeGeo or reflection. */
public final class MtegItemModels {
    private MtegItemModels(){}
    public static void register(JsonObject descriptors) {
        ModelLoadingPlugin.register(plugin->plugin.modifyItemModelBeforeBake().register((model,context)->{
            var id=context.itemId();
            if(!id.getNamespace().equals("mteg")||!descriptors.has(id.getPath()))return model;
            if(!(model instanceof CompositeModel.Unbaked composite))throw new IllegalStateException("MTEG original part list missing: "+id);
            return new OriginalParts(composite,id.getPath(),descriptors.getAsJsonObject(id.getPath()));
        }));
    }
    private static final class OriginalParts extends WrapperUnbakedItemModel {
        private final String gun;private final JsonObject descriptor;
        OriginalParts(CompositeModel.Unbaked original,String gun,JsonObject descriptor){super(original);this.gun=gun;this.descriptor=descriptor;}
        @Override public ItemModel bake(ItemModel.BakingContext context,Matrix4fc rootTransform) {
            var original=(CompositeModel.Unbaked)wrapped;var names=descriptor.getAsJsonArray("models");
            if(names.size()!=original.models().size())throw new IllegalStateException("MTEG part count changed: "+gun);
            Map<String,ItemModel> models=new LinkedHashMap<>();
            for(int i=0;i<names.size();i++) {
                var source=original.models().get(i);
                if(source instanceof CuboidItemModelWrapper.Unbaked cuboid && descriptor.get("tinted").getAsBoolean())source=new CuboidItemModelWrapper.Unbaked(cuboid.model(),cuboid.transformation(),List.of(DyeTint.INSTANCE));
                models.put(names.get(i).getAsString(),source.bake(context,new Matrix4f()));
            }
            Matrix4f root=sourceRootTransform(rootTransform);
            return (state,stack,resolver,display,level,owner,seed)->{
                Minecraft client=Minecraft.getInstance();boolean local=owner!=null&&owner.asLivingEntity()==client.player;
                float partial=client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                float cooldown=local&&client.player!=null?client.player.getCooldowns().getCooldownPercent(stack,partial):0F;
                List<Draw> draws=new ArrayList<>();
                for(var part:MtegModelProgram.plan(gun,stack,cooldown,local)) {
                    ItemModel model=models.get(part.model());if(model==null)throw new IllegalStateException("Unbaked MTEG original part "+part.model());
                    ItemStackRenderState child=new ItemStackRenderState();model.update(child,stack,resolver,ItemDisplayContext.NONE,level,owner,seed);
                    draws.add(new Draw(child,part.transform()));
                }
                for(var type:List.of(ttv.migami.jeg.item.attachment.AttachmentType.SCOPE,ttv.migami.jeg.item.attachment.AttachmentType.BARREL,ttv.migami.jeg.item.attachment.AttachmentType.STOCK,ttv.migami.jeg.item.attachment.AttachmentType.UNDER_BARREL)){
                    var transform=ttv.migami.jeg.client.render.gun.GunAttachmentTransforms.transform(MtegContent.id(gun),type);
                    var attachment=ttv.migami.jeg.item.attachment.GunAttachments.stack(stack,type);
                    if(transform.isEmpty()||attachment.isEmpty())continue;
                    var t=transform.get();ItemStackRenderState child=new ItemStackRenderState();
                    resolver.updateForTopItem(child,attachment.get(),ItemDisplayContext.NONE,level,owner,seed);
                    draws.add(new Draw(child,new Matrix4f().translation((float)t.x()/16F,((float)t.y()-8F)/16F,(float)t.z()/16F).scale((float)t.scale())));
                }
                var layer=state.newLayer();layer.setLocalTransform(root);layer.setItemTransform(transform(descriptor.getAsJsonObject("display"),display));
                layer.setUsesBlockLight(!descriptor.get("gui_light").getAsString().equals("front"));
                PartsRenderer renderer=new PartsRenderer(List.copyOf(draws));layer.setupSpecialModel(renderer,Boolean.TRUE);
                layer.setExtents(()->renderer.extents().toArray(Vector3fc[]::new));
                if(!draws.isEmpty())layer.setParticleMaterial(draws.get(0).state().pickParticleMaterial(net.minecraft.util.RandomSource.create(seed)));
                state.appendModelIdentityElement(gun);state.appendModelIdentityElement(draws.stream().map(Draw::transform).toList());state.setAnimated();
            };
        }
    }
    /**
     * 26.3 ItemTransform.apply centers geometry even for a special renderer. Each original
     * cuboid child already receives that center in its NONE transform. Undo only the
     * outer automatic center, before root/animation matrices, so source display T/R/S
     * and the child's single center match original RenderUtil.applyTransformType/renderModel.
     */
    static Matrix4f sourceRootTransform(Matrix4fc rootTransform) {
        return new Matrix4f().translation(0.5F,0.5F,0.5F).mul(rootTransform);
    }
    private record Draw(ItemStackRenderState state,Matrix4f transform){}
    private static final class PartsRenderer implements SpecialModelRenderer<Boolean> {
        private final List<Draw> parts;PartsRenderer(List<Draw> parts){this.parts=parts;}
        List<Vector3fc> extents(){List<Vector3fc> result=new ArrayList<>();for(var part:parts)part.state().visitExtents(v->result.add(part.transform().transformPosition(v,new Vector3f())));return result;}
        @Override public void getExtents(Consumer<Vector3fc> consumer){extents().forEach(consumer);}
        @Override public Boolean extractArgument(ItemStack stack){return Boolean.TRUE;}
        @Override public void submit(Boolean ignored,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean foil,int outline){
            for(var part:parts){pose.pushPose();pose.mulPose(part.transform());part.state().submit(pose,collector,light,overlay,outline);pose.popPose();}
        }
    }
    private static ItemTransform transform(JsonObject displays,ItemDisplayContext context) {
        String name=context.getSerializedName();
        if(!displays.has(name)&&context.leftHand())name=name.replace("lefthand","righthand");
        if(!displays.has(name))return ItemTransform.NO_TRANSFORM;
        JsonObject object=displays.getAsJsonObject(name);Vector3f translation=vector(object,"translation",0).div(16F);
        return new ItemTransform(vector(object,"rotation",0),translation,vector(object,"scale",1));
    }
    private static Vector3f vector(JsonObject object,String name,float fallback){if(!object.has(name))return new Vector3f(fallback);JsonArray values=object.getAsJsonArray(name);return new Vector3f(values.get(0).getAsFloat(),values.get(1).getAsFloat(),values.get(2).getAsFloat());}
    private enum DyeTint implements ItemTintSource {
        INSTANCE;
        @Override public int calculate(ItemStack stack,ClientLevel level,LivingEntity entity){
            Item item=GunAttachments.cosmeticItem(stack, AttachmentType.DYE).orElse(null);
            if(item==null)return -1;
            var color=item.getDefaultInstance().get(net.minecraft.core.component.DataComponents.DYE);return color==null?-1:0xFF000000|color.getTextColor();
        }
        @Override public MapCodec<? extends ItemTintSource> type(){return MapCodec.unit(INSTANCE);}
    }
}
