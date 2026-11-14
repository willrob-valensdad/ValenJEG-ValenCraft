package top.ribs.scguns.client.render.gun;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Static bridge for the 1.20-era item display transforms used by the custom gun renderer.
 * The compact table is generated from assets/scguns/models/item/*.json by Hotfix 34.
 */
public final class ScgunsLegacyItemTransforms {
    public static final Transform IDENTITY = new Transform(0,0,0, 0,0,0, 1,1,1);
    private static final String RESOURCE = "/assets/scguns/legacy_item_transforms.json";
    private static final Map<String, EnumMap<ItemDisplayContext, Transform>> TRANSFORMS = load();

    private ScgunsLegacyItemTransforms() {}

    private static Map<String, EnumMap<ItemDisplayContext, Transform>> load() {
        Map<String, EnumMap<ItemDisplayContext, Transform>> result = new HashMap<>();
        try (var stream = ScgunsLegacyItemTransforms.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) return Map.of();
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> itemEntry : root.entrySet()) {
                EnumMap<ItemDisplayContext, Transform> contexts = new EnumMap<>(ItemDisplayContext.class);
                for (Map.Entry<String, JsonElement> contextEntry : itemEntry.getValue().getAsJsonObject().entrySet()) {
                    try {
                        ItemDisplayContext context = ItemDisplayContext.valueOf(contextEntry.getKey());
                        JsonObject object = contextEntry.getValue().getAsJsonObject();
                        float[] r = vec(object.getAsJsonArray("rotation"), 0, 0, 0);
                        float[] t = vec(object.getAsJsonArray("translation"), 0, 0, 0);
                        float[] s = vec(object.getAsJsonArray("scale"), 1, 1, 1);
                        contexts.put(context, new Transform(r[0],r[1],r[2], t[0],t[1],t[2], s[0],s[1],s[2]));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                result.put(itemEntry.getKey(), contexts);
            }
        } catch (Exception ignored) {
            return Map.of();
        }
        return Map.copyOf(result);
    }

    private static float[] vec(JsonArray array, float x, float y, float z) {
        if (array == null || array.size() != 3) return new float[]{x,y,z};
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    public static Transform get(ItemStack stack, ItemDisplayContext context) {
        if (stack == null || stack.isEmpty()) return IDENTITY;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !"scguns".equals(id.getNamespace())) return IDENTITY;
        EnumMap<ItemDisplayContext, Transform> byContext = TRANSFORMS.get(id.getPath());
        return byContext == null ? IDENTITY : byContext.getOrDefault(context, IDENTITY);
    }

    public static Transform firstPersonRightHand(ItemStack stack) {
        return get(stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
    }

    /**
     * Mirrors the original 1.20 renderer exactly: it selected the transform for the
     * requested display context, then called BakedModel.applyTransform(..., false).
     * Left-hand JSON entries are therefore already authoritative and must not be
     * mirrored a second time here.
     */
    public static void apply(ItemStack stack, PoseStack poseStack, ItemDisplayContext context) {
        Transform t = get(stack, context);
        poseStack.translate(t.translationX, t.translationY, t.translationZ);
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.XP.rotationDegrees(t.rotationX)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.YP.rotationDegrees(t.rotationY)));
        poseStack.mulPose(top.ribs.scguns.fabric.Poses.of(Axis.ZP.rotationDegrees(t.rotationZ)));
        poseStack.scale(t.scaleX, t.scaleY, t.scaleZ);
    }

    public record Transform(
            float rotationX, float rotationY, float rotationZ,
            float translationX, float translationY, float translationZ,
            float scaleX, float scaleY, float scaleZ) {}
}
