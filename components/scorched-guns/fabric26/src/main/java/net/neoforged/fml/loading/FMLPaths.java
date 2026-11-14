package net.neoforged.fml.loading;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
public final class FMLPaths {
    public static final Path GAMEDIR = FabricLoader.getInstance().getGameDir();
    public static final Path CONFIGDIR = FabricLoader.getInstance().getConfigDir();
    private FMLPaths() {}
    public static Path getOrCreateGameRelativePath(Path path) {
        return GAMEDIR.resolve(path);
    }
}
