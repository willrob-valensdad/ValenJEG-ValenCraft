package top.ribs.scguns.init;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ModMuzzleFlashes {
    private static final Map<String, Identifier> muzzleFlashes = new HashMap<>();

    public static void registerMuzzleFlash(String flashType, Identifier texture) {
        muzzleFlashes.put(flashType, texture);
    }

    public static Identifier getMuzzleFlashTexture(String flashType) {
        return muzzleFlashes.getOrDefault(flashType,
                Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_1.png"));
    }

    public static void init() {
        registerMuzzleFlash("flash_type_1", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_1.png"));
        registerMuzzleFlash("flash_type_2", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_2.png"));
        registerMuzzleFlash("flash_type_3", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_3.png"));
        registerMuzzleFlash("flash_type_4", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_4.png"));
        registerMuzzleFlash("flash_type_5", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_5.png"));
        registerMuzzleFlash("flash_type_6", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_6.png"));
        registerMuzzleFlash("flash_type_7", Identifier.fromNamespaceAndPath("scguns", "textures/effect/muzzle_flash_7.png"));
    }
}




