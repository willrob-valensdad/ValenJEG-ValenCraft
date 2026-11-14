package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import top.ribs.scguns.client.BulletTrail;
import top.ribs.scguns.client.GunRenderType;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.entity.projectile.ShulkshotProjectileEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class BulletTrailRenderingHandler
{
    private static BulletTrailRenderingHandler instance;

    public static BulletTrailRenderingHandler get()
    {
        if(instance == null)
        {
            instance = new BulletTrailRenderingHandler();
        }
        return instance;
    }

    private final Map<Integer, BulletTrail> bullets = new HashMap<>();

    private BulletTrailRenderingHandler() {}

    /**
     * Adds a bullet trail to render into the world
     *
     * @param trail the bullet trail get
     */
    public void add(BulletTrail trail) {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            // Check if the entity is not a ShulkshotProjectileEntity before adding the trail
            if (!(world.getEntity(trail.getEntityId()) instanceof ShulkshotProjectileEntity)) {
                this.bullets.put(trail.getEntityId(), trail);
            }
        }
    }
    /**
     * Removes the bullet for the given entity id.
     *
     * @param entityId the entity id of the bullet
     */
    public void remove(int entityId)
    {
        this.bullets.remove(entityId);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            this.bullets.values().forEach(BulletTrail::tick);
            this.bullets.values().removeIf(BulletTrail::isDead);
        }
        else if(!this.bullets.isEmpty())
        {
            this.bullets.clear();
        }
    }

    @SubscribeEvent
    public void onSubmitCustomGeometry(SubmitCustomGeometryEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || this.bullets.isEmpty()) return;
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for (BulletTrail bulletTrail : this.bullets.values())
        {
            this.renderBulletTrail(bulletTrail, event.getPoseStack(), event.getSubmitNodeCollector(), partialTicks);
        }
    }

    @SubscribeEvent
    public void onRespawn(ClientPlayerNetworkEvent.Clone event)
    {
        this.bullets.clear();
    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        this.bullets.clear();
    }

    private void renderBulletTrail(BulletTrail trail, PoseStack poseStack, SubmitNodeCollector collector, float deltaTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        if(entity == null || trail.isDead())
            return;

        Entity shooter = trail.getShooter();
        if(mc.options.getCameraType().isFirstPerson() && shooter instanceof Player player && player.isLocalPlayer())
            return;

        poseStack.pushPose();

        Vec3 view = mc.gameRenderer.mainCamera().position();
        Vec3 position = trail.getPosition();
        Vec3 motion = trail.getMotion();
        double bulletX = position.x + motion.x * deltaTicks;
        double bulletY = position.y + motion.y * deltaTicks;
        double bulletZ = position.z + motion.z * deltaTicks;
        poseStack.translate(bulletX - view.x(), bulletY - view.y(), bulletZ - view.z());

        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(trail.getYaw())));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(-trail.getPitch() + 90)));

        Vec3 motionVec = new Vec3(motion.x, motion.y, motion.z);
        float trailLength = (float) (motionVec.length() * trail.getTrailLengthMultiplier());
        float red = (float) (trail.getTrailColor() >> 16 & 255) / 255.0F;
        float green = (float) (trail.getTrailColor() >> 8 & 255) / 255.0F;
        float blue = (float) (trail.getTrailColor() & 255) / 255.0F;
        float alpha = 0.3F;
        float trailWidth = (float) (0.035F * trail.getTrailThickness());

        // Prevents the trail length from being longer than the distance to shooter
        if(shooter != null)
        {
            trailLength = (float) Math.min(trailLength, shooter.getEyePosition(deltaTicks).distanceTo(new Vec3(bulletX,bulletY, bulletZ)));
        }

        if(trail.isTrailVisible())
        {
            RenderType bulletType = GunRenderType.getBulletTrail();
            final float finalTrailLength = trailLength;
            collector.submitCustomGeometry(poseStack, bulletType, (pose, builder) -> {
                var matrix = pose.pose();
                builder.addVertex(matrix, 0, 0, -trailWidth).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, 0, 0, trailWidth).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, 0, -finalTrailLength, 0).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, 0, -finalTrailLength, 0).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, -trailWidth, 0, 0).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, trailWidth, 0, 0).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, 0, -finalTrailLength, 0).setColor(red, green, blue, alpha);
                builder.addVertex(matrix, 0, -finalTrailLength, 0).setColor(red, green, blue, alpha);
            });
        }

        if(!trail.getItem().isEmpty())
        {
            poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees((trail.getAge() + deltaTicks) * (float) 50)));
            poseStack.scale(0.275F, 0.275F, 0.275F);

            int combinedLight = LightCoordsUtil.getLightCoords(entity.level(), BlockPos.containing(entity.position()));
            ItemStack stack = trail.getItem();
            RenderUtil.renderModel(stack, ItemDisplayContext.NONE, poseStack, collector, combinedLight, OverlayTexture.NO_OVERLAY, entity.level(), null);
        }

        poseStack.popPose();
    }
}
