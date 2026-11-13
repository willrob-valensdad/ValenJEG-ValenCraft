package ttv.migami.jeg.client.render.gun;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.item.attachment.AttachmentType;
import ttv.migami.jeg.item.attachment.GunAttachments;
import ttv.migami.jeg.item.attachment.PaintJobCanItem;

/**
 * Paint jobs select an authored texture only when that file is already in the resource pack.
 * A missing paint file keeps the base gun or attachment texture. This class does not synthesize art.
 */
public final class GunCosmeticTextures {
    public static final String GUN_TEXTURE_ROOT = "textures/animated/gun/";
    public static final String PAINT_JOB_GUN_ROOT = "textures/animated/gun/paintjob/";

    private GunCosmeticTextures() {
    }

    public static Identifier gunTexture(ItemStack gunStack, String gunId) {
        String safeId = gunId == null || gunId.isBlank() ? "abstract_gun" : gunId;
        String paintJob = paintJob(gunStack);
        if (paintJob != null) {
            Identifier painted = Reference.id(PAINT_JOB_GUN_ROOT + paintJob + "/" + safeId + ".png");
            if (exists(painted)) {
                return painted;
            }
        }
        return Reference.id(GUN_TEXTURE_ROOT + safeId + ".png");
    }

    public static String paintJob(ItemStack gunStack) {
        if (gunStack == null || gunStack.isEmpty()) {
            return null;
        }
        Item item = GunAttachments.cosmeticItem(gunStack, AttachmentType.PAINT_JOB).orElse(null);
        if (!(item instanceof PaintJobCanItem paintJobCanItem)) {
            return null;
        }
        String paintJob = paintJobCanItem.paintJob();
        return paintJob == null || paintJob.isBlank() ? null : paintJob;
    }

    public static boolean exists(Identifier id) {
        return id != null && Minecraft.getInstance().getResourceManager().getResource(id).isPresent();
    }
}
