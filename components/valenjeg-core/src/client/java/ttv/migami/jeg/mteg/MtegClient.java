package ttv.migami.jeg.mteg;
/** Public native model-loading bridge, preserving all authored MTEG part programs. */
public final class MtegClient {
 private MtegClient(){}
 public static void init(){
  ttv.migami.jeg.enchantment.LegacyEnchantmentTrailClient.register();
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(MtegTrailPayload.TYPE,(payload,context)->context.client().execute(()->{if(ttv.migami.jeg.network.NetworkHandler.shouldRenderLegacyBulletTrail())ttv.migami.jeg.client.render.BulletTrailRenderer.upsertMtegTrail(payload.trail(),payload.length());}));
  MtegParticles.register();MtegProjectileRenderer.register();
  MtegItemModels.register(MtegContent.resource("/assets/mteg/valen/item-model-descriptors.json"));
 }
}
