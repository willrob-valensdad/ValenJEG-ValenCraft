package top.ribs.scguns.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.mojang.blaze3d.platform.InputConstants;
import top.ribs.scguns.Config;

import net.minecraft.resources.Identifier;
/**
 * Author: MrCrayfish
 */
    public class KeyBinds
    {
        public static final KeyMapping.Category SCGUNS_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("scguns", "main"));
        public static final KeyMapping KEY_RELOAD = new KeyMapping("key.scguns.reload", InputConstants.KEY_R, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_UNLOAD = new KeyMapping("key.scguns.unload", InputConstants.KEY_U, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_ATTACHMENTS = new KeyMapping("key.scguns.attachments", InputConstants.KEY_Z, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_MELEE = new KeyMapping("key.scguns.melee", InputConstants.KEY_V, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_INSPECT = new KeyMapping("key.scguns.inspect", InputConstants.KEY_X, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_ENABLE_EXO_HELMET = new KeyMapping("key.scguns.enable_exo_helmet", InputConstants.KEY_B, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_ENABLE_EXO_BOOTS = new KeyMapping("key.scguns.enable_exo_boots", InputConstants.KEY_N, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_ENABLE_EXO_CHESTPLATE = new KeyMapping("key.scguns.enable_exo_chestplate", InputConstants.KEY_M, SCGUNS_CATEGORY);
        public static final KeyMapping KEY_SWAP_AMMO = new KeyMapping("key.scguns.swap_ammo", InputConstants.KEY_H, SCGUNS_CATEGORY);
        public static void registerKeyMappings(RegisterKeyMappingsEvent event)
        {
            event.registerCategory(SCGUNS_CATEGORY);
            event.register(KEY_RELOAD);
            event.register(KEY_UNLOAD);
            event.register(KEY_ATTACHMENTS);
            event.register(KEY_MELEE);
            event.register(KEY_INSPECT);
            event.register(KEY_ENABLE_EXO_HELMET);
            event.register(KEY_ENABLE_EXO_CHESTPLATE);
            event.register(KEY_ENABLE_EXO_BOOTS);
            event.register(KEY_SWAP_AMMO);
        }

        public static KeyMapping getAimMapping()
        {
            Minecraft mc = Minecraft.getInstance();
            return Config.CLIENT.controls.flipControls.get() ? mc.options.keyAttack : mc.options.keyUse;
        }

        public static KeyMapping getShootMapping()
        {
            Minecraft mc = Minecraft.getInstance();
            return Config.CLIENT.controls.flipControls.get() ? mc.options.keyUse : mc.options.keyAttack;
        }
    }
