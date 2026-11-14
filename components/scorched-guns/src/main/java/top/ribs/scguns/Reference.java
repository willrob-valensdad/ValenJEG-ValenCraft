package top.ribs.scguns;

import net.minecraft.resources.Identifier;

public class Reference
{
	public static final String MOD_ID = "scguns";

    public static Identifier id(String string) {
        return Identifier.fromNamespaceAndPath(MOD_ID, string);
    }
}

