package ttv.migami.jeg.mteg;

import net.fabricmc.loader.api.FabricLoader;
import ttv.migami.jeg.config.ModConfigSpec;

/** Original dependency5322334 setFireToBlocks is separate from glass/block breaking. */
public final class MtegConfig {
    private MtegConfig(){}
    public static final ModConfigSpec.BooleanValue SET_FIRE_TO_BLOCKS;
    public static final ModConfigSpec SPEC;
    static {
        var builder=new ModConfigSpec.Builder();
        builder.push("gameplay").push("griefing");
        SET_FIRE_TO_BLOCKS=builder.comment("Original MTEG projectile block ignition, independent of bullet block destruction.").define("setFireToBlocks",true);
        builder.pop().pop();SPEC=builder.build();
    }
    public static void load(){SPEC.load(FabricLoader.getInstance().getConfigDir().resolve("mteg-server.toml"));}
    public static boolean setFireToBlocks(){return SET_FIRE_TO_BLOCKS.get();}
}
