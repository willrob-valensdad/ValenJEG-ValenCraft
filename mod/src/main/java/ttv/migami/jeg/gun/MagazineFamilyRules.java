package ttv.migami.jeg.gun;

import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import ttv.migami.jeg.item.MagazineItem;

/**
 * Physical magazine family for a gun that already declares mag-fed reload.
 * A family matches only when its base capacity equals the authored gun capacity
 * and its ammo set contains the gun's ammo. Two matching bases fail closed.
 * Legacy extended/drum attachments are not a family.
 */
public final class MagazineFamilyRules {
    public enum AttachmentHint {
        NONE,
        LEGACY_DISABLED,
        INVENTORY_RELOAD,
        WRONG_FAMILY,
        LOOSE_ONLY
    }

    private MagazineFamilyRules() {}

    @Nullable
    public static MagazineItem.MagazineType baseFamily(@Nullable String reloadType, int magazineSize, @Nullable Identifier ammo) {
        if (ammo == null || magazineSize <= 0 || !isMagFed(reloadType)) {
            return null;
        }
        MagazineItem.MagazineType found = null;
        for (MagazineItem.MagazineType type : MagazineItem.MagazineType.values()) {
            if (type.baseType() != type || type.capacity() != magazineSize || !type.supports(ammo)) {
                continue;
            }
            if (found != null) {
                return null;
            }
            found = type;
        }
        return found;
    }

    public static AttachmentHint attachmentHint(
            boolean magazineFeed,
            boolean originalLooseReload,
            @Nullable String reloadType,
            int magazineSize,
            @Nullable Identifier ammo,
            @Nullable MagazineItem.MagazineType carriedMagazine,
            boolean legacyCapacityAttachment
    ) {
        if (!magazineFeed || (carriedMagazine == null && !legacyCapacityAttachment)) {
            return AttachmentHint.NONE;
        }
        if (legacyCapacityAttachment) {
            return AttachmentHint.LEGACY_DISABLED;
        }
        if (originalLooseReload) {
            return AttachmentHint.LOOSE_ONLY;
        }
        MagazineItem.MagazineType family = baseFamily(reloadType, magazineSize, ammo);
        if (family == null) {
            return AttachmentHint.LOOSE_ONLY;
        }
        return carriedMagazine.isVariantOf(family) ? AttachmentHint.INVENTORY_RELOAD : AttachmentHint.WRONG_FAMILY;
    }

    private static boolean isMagFed(@Nullable String reloadType) {
        return "jeg:mag_fed".equals(reloadType) || "jeg:magazine".equals(reloadType);
    }
}
