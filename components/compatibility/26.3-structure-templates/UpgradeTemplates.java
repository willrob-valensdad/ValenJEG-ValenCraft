import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import com.google.gson.*;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.*;

/** Offline use of Minecraft's own structure upgrader. No runtime hooks. */
public final class UpgradeTemplates {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static String sha(byte[] b) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
    }
    private static void sameGeometry(CompoundTag before, CompoundTag after, String id) {
        if (!Objects.equals(before.get("size"), after.get("size")))
            throw new IllegalStateException("Structure size changed: " + id);
        var a = before.getListOrEmpty("blocks");
        var b = after.getListOrEmpty("blocks");
        if (a.size() != b.size()) throw new IllegalStateException("Block count changed: " + id);
        if (before.getListOrEmpty("entities").size() != after.getListOrEmpty("entities").size())
            throw new IllegalStateException("Entity count changed: " + id);
        for (int i = 0; i < a.size(); i++) {
            var aa = a.getCompoundOrEmpty(i);
            var bb = b.getCompoundOrEmpty(i);
            for (String key : List.of("pos", "state"))
                if (!Objects.equals(aa.get(key), bb.get(key)))
                    throw new IllegalStateException("Block geometry changed: " + id + " " + i);
        }
    }
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Path output = Path.of(args[0]);
        Files.createDirectories(output);
        var fixer = DataFixers.getDataFixer();
        int target = SharedConstants.WORLD_VERSION;
        List<Object> receipts = new ArrayList<>();
        for (int arg = 1; arg < args.length; arg++) {
            Path input = Path.of(args[arg]);
            Path destination = output.resolve(input.getFileName());
            if (Files.exists(destination)) throw new IOException("Refusing overwrite: " + destination);
            List<Object> changes = new ArrayList<>();
            Path temporary = output.resolve(input.getFileName() + ".partial");
            try (ZipFile zip = new ZipFile(input.toFile());
                 ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(temporary))) {
                for (var entry : Collections.list(zip.entries())) {
                    byte[] bytes = zip.getInputStream(entry).readAllBytes();
                    String name = entry.getName();
                    if (name.startsWith("data/") && name.contains("/structure/") && name.endsWith(".nbt")) {
                        var old = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
                        int version = old.getIntOr("DataVersion", 500);
                        if (version < target) {
                            System.out.println("UPGRADE " + input.getFileName() + " " + name + " " + version);
                            System.out.flush();
                            var updated = DataFixTypes.STRUCTURE.updateToCurrentVersion(fixer, old.copy(), version);
                            updated.putInt("DataVersion", target);
                            sameGeometry(old, updated, name);
                            var buffer = new ByteArrayOutputStream();
                            NbtIo.writeCompressed(updated, buffer);
                            byte[] replacement = buffer.toByteArray();
                            var reread = NbtIo.readCompressed(new ByteArrayInputStream(replacement), NbtAccounter.unlimitedHeap());
                            if (!updated.equals(reread)) throw new IOException("NBT roundtrip failed: " + name);
                            changes.add(Map.of("entry", name, "from", version, "to", target,
                                "source_sha256", sha(bytes), "output_sha256", sha(replacement),
                                "blocks", updated.getListOrEmpty("blocks").size(),
                                "entities", updated.getListOrEmpty("entities").size()));
                            bytes = replacement;
                        }
                    }
                    var copy = new ZipEntry(name);
                    copy.setTime(entry.getTime());
                    out.putNextEntry(copy); out.write(bytes); out.closeEntry();
                }
            }
            if (changes.isEmpty()) Files.delete(temporary);
            else {
                Files.move(temporary, destination);
                receipts.add(Map.of("source", input.toString(), "output", destination.toString(),
                    "source_sha256", sha(Files.readAllBytes(input)), "output_sha256", sha(Files.readAllBytes(destination)),
                    "changes", changes));
                Files.writeString(output.resolve("receipt.json"), JSON.toJson(receipts) + "\n");
                System.out.println("FINISHED " + input.getFileName() + " " + changes.size());
            }
        }
        System.out.println("COMPLETE " + receipts.size() + " jars");
        System.exit(0);
    }
}
