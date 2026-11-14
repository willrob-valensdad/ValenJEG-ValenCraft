package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.entity.state.ScgunsMobRenderState;
import top.ribs.scguns.entity.monster.SulfurheadEntity;

public class SulfurheadRenderer extends MobRenderer<SulfurheadEntity, ScgunsMobRenderState, SulfurheadModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/sulfurhead.png");

    public SulfurheadRenderer(EntityRendererProvider.Context context) {
        super(context, new SulfurheadModel(context.bakeLayer(ModModelLayers.SULFURHEAD_LAYER)), 0.5F);
        this.addLayer(new SulfurheadGelLayer(this));
        this.addLayer(new SulfurheadPrimeOverlayLayer(this));
    }

    @Override
    public Identifier getTextureLocation(ScgunsMobRenderState state) {
        return TEXTURE;
    }

    @Override
    public ScgunsMobRenderState createRenderState() {
        return new ScgunsMobRenderState();
    }

    @Override
    public void extractRenderState(SulfurheadEntity entity, ScgunsMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attacking = entity.isAttacking();
        state.attackTimeout = entity.getAttackTimeout();
        state.primed = entity.isPrimed();
        state.swelling = entity.getSwelling(partialTicks);
    }

    @Override
    protected void scale(ScgunsMobRenderState state, PoseStack poseStack) {
        float swell = state.swelling;
        float pulse = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = Mth.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        float scale = (1.0F + swell * 0.15F) * pulse;
        poseStack.scale(scale, scale, scale);
    }
}
