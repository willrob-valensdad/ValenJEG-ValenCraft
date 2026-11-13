package ttv.migami.jeg.gun;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.item.attachment.AttachmentType;
import ttv.migami.jeg.item.attachment.GunAttachments;

public final class GunScopeSupport {
    private static final Identifier TELESCOPIC_SIGHT = Reference.id("telescopic_sight");
    private static final Identifier COMBAT_SCOPE = Reference.id("combat_scope");
    private static volatile boolean boltActionRifleScopeEnabled = false;

    private GunScopeSupport() {
    }

    public static boolean isBoltActionRifleScopeEnabled() {
        return boltActionRifleScopeEnabled;
    }

    public static boolean isBoltActionRifleScopeEnabled(ItemStack stack) {
        return hasMagnifiedOptic(stack);
    }

    public static boolean hasTelescopicSight(ItemStack stack) {
        return scopeIs(stack, TELESCOPIC_SIGHT);
    }

    public static boolean hasCombatScope(ItemStack stack) {
        return scopeIs(stack, COMBAT_SCOPE);
    }

    /** Tube optics. Reflex, monocle, and holographic sights stay on the open-sight path. */
    public static boolean hasMagnifiedOptic(ItemStack stack) {
        return hasTelescopicSight(stack) || hasCombatScope(stack);
    }

    private static boolean scopeIs(ItemStack stack, Identifier scopeId) {
        return GunAttachments.id(stack, AttachmentType.SCOPE)
                .filter(scopeId::equals)
                .isPresent();
    }

    public static boolean hasSpyglassScope(ItemStack stack) {
        return GunAttachments.stack(stack, AttachmentType.SCOPE)
                .map(ItemStack::getItem)
                .filter(SpyglassItem.class::isInstance)
                .isPresent();
    }

    public static void setBoltActionRifleScopeEnabled(boolean enabled) {
        boltActionRifleScopeEnabled = enabled;
    }
}
