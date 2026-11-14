package earth.terrarium.adastra.client.renderers.entities.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.mobs.LunarianModel;
import earth.terrarium.adastra.common.entities.mob.Lunarian;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jetbrains.annotations.NotNull;

// LEGACY ENTITY. WILL BE REPLACED IN THE FUTURE.
public class LunarianRenderer extends MobRenderer<Lunarian, LunarianRenderer.State, LunarianModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/lunarian.png");
    public static final Identifier FARMER_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/farmer_lunarian.png");
    public static final Identifier FISHERMAN_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/fisherman_lunarian.png");
    public static final Identifier SHEPHERD_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/shepherd_lunarian.png");
    public static final Identifier FLETCHER_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/fletcher_lunarian.png");
    public static final Identifier LIBRARIAN_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/librarian_lunarian.png");
    public static final Identifier CARTOGRAPHER_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/cartographer_lunarian.png");
    public static final Identifier CLERIC_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/cleric_lunarian.png");
    public static final Identifier ARMORER_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/armorer_lunarian.png");
    public static final Identifier WEAPONSMITH_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/weaponsmith_lunarian.png");
    public static final Identifier TOOLSMITH_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/toolsmith_lunarian.png");
    public static final Identifier BUTCHER_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/butcher_lunarian.png");
    public static final Identifier LEATHERWORKER_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/leatherworker_lunarian.png");
    public static final Identifier MASON_TEXTURE = Identifier.fromNamespaceAndPath(AdAstra.MOD_ID, "textures/entity/mob/lunarian/mason_lunarian.png");

    public LunarianRenderer(EntityRendererProvider.Context context) {
        super(context, new LunarianModel(context.bakeLayer(LunarianModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Lunarian entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.profession = entity.getVillagerData().profession();
    }

    @Override
    public @NotNull Identifier getTextureLocation(State state) {
        VillagerProfession profession = state.profession;
        if (profession.equals(VillagerProfession.ARMORER)) {
            return ARMORER_TEXTURE;
        } else if (profession.equals(VillagerProfession.BUTCHER)) {
            return BUTCHER_TEXTURE;
        } else if (profession.equals(VillagerProfession.CARTOGRAPHER)) {
            return CARTOGRAPHER_TEXTURE;
        } else if (profession.equals(VillagerProfession.CLERIC)) {
            return CLERIC_TEXTURE;
        } else if (profession.equals(VillagerProfession.FARMER)) {
            return FARMER_TEXTURE;
        } else if (profession.equals(VillagerProfession.FISHERMAN)) {
            return FISHERMAN_TEXTURE;
        } else if (profession.equals(VillagerProfession.FLETCHER)) {
            return FLETCHER_TEXTURE;
        } else if (profession.equals(VillagerProfession.LEATHERWORKER)) {
            return LEATHERWORKER_TEXTURE;
        } else if (profession.equals(VillagerProfession.LIBRARIAN)) {
            return LIBRARIAN_TEXTURE;
        } else if (profession.equals(VillagerProfession.MASON)) {
            return MASON_TEXTURE;
        } else if (profession.equals(VillagerProfession.SHEPHERD)) {
            return SHEPHERD_TEXTURE;
        } else if (profession.equals(VillagerProfession.TOOLSMITH)) {
            return TOOLSMITH_TEXTURE;
        } else if (profession.equals(VillagerProfession.WEAPONSMITH)) {
            return WEAPONSMITH_TEXTURE;
        } else {
            return TEXTURE;
        }
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        float g = 0.9375f;
        if (state.isBaby) {
            g *= 0.5f;
            this.shadowRadius = 0.25f;
        } else {
            this.shadowRadius = 0.5f;
        }
        poseStack.scale(g, g, g);
    }

    public static final class State extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState {
        public VillagerProfession profession;
    }
}
