package top.ribs.scguns.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public final class ScgunsColorCompat {
    private ScgunsColorCompat() {}

    public static int legacyColor(ChatFormatting formatting, int fallback) {
        TextColor color = TextColor.fromLegacyFormat(formatting);
        return color != null ? color.getValue() : fallback;
    }
}
