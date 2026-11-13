package ttv.migami.jeg.client.mesh;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import ttv.migami.jeg.structure.mesh.HbmQuarterTurn;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

/**
 * Bakes the pinned HBM meshes once per reload. Triangles become quads by repeating the third
 * vertex, which is what ObjUtil does for the quad tessellator. Techne boxes use the 26.3
 * ModelPart.Cube face and mirror layout. Fence rails stay on the source z=0.5 or x=0.5 plane.
 */
public final class HbmStaticMeshModel {
    private HbmStaticMeshModel() {}

    public record Unbaked(String kind, String mesh, String particle, float yaw, int worldRotation, boolean north, boolean south, boolean east, boolean west, String sourceAxis, int sourceDegrees) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("kind").forGetter(Unbaked::kind),
                Codec.STRING.fieldOf("mesh").forGetter(Unbaked::mesh),
                Codec.STRING.fieldOf("particle").forGetter(Unbaked::particle),
                Codec.FLOAT.optionalFieldOf("yaw", 0F).forGetter(Unbaked::yaw),
                Codec.INT.optionalFieldOf("worldRotation", 0).forGetter(Unbaked::worldRotation),
                Codec.BOOL.optionalFieldOf("north", false).forGetter(Unbaked::north),
                Codec.BOOL.optionalFieldOf("south", false).forGetter(Unbaked::south),
                Codec.BOOL.optionalFieldOf("east", false).forGetter(Unbaked::east),
                Codec.BOOL.optionalFieldOf("west", false).forGetter(Unbaked::west),
                Codec.STRING.optionalFieldOf("sourceAxis", "y").forGetter(Unbaked::sourceAxis),
                Codec.INT.optionalFieldOf("sourceDegrees", 0).forGetter(Unbaked::sourceDegrees)
        ).apply(instance, Unbaked::new));

        @Override public MapCodec<? extends CustomUnbakedBlockStateModel> codec() { return CODEC; }
        @Override public void resolveDependencies(ResolvableModel.Resolver resolver) { resolver.markDependency(Identifier.parse(particle)); }
        @Override public BlockStateModel bake(ModelBaker baker) {
            List<BakedQuad> quads = switch (kind) {
                case "wavefront" -> Wavefront.quads(baker, Identifier.parse(mesh), Identifier.parse(particle.replace("_particle", "")), yaw, sourceAxis, sourceDegrees);
                case "pole_top" -> Techne.poleTop(baker, Identifier.parse("jeg:block/hbm_pole_top"));
                case "satellite" -> Techne.satellite(baker, Identifier.parse("jeg:block/hbm_satellite_receiver"), yaw);
                case "fence" -> FenceMesh.quads(baker, north, south, east, west);
                default -> throw new IllegalStateException("Unsupported HBM mesh kind: " + kind);
            };
            quads = QuarterTurn.apply(quads, worldRotation);
            var sprite = quads.get(0).materialInfo().sprite();
            var material = new net.minecraft.client.resources.model.sprite.Material.Baked(sprite, false);
            return new Baked(quads, material);
        }
    }

    private record Baked(List<BakedQuad> quads, net.minecraft.client.resources.model.sprite.Material.Baked particle) implements BlockStateModel {
        @Override public void collectParts(RandomSource random, List<BlockStateModelPart> parts) { parts.add(new Part(quads, particle)); }
        @Override public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() { return particle; }
        @Override public int materialFlags() { return meshMaterialFlags(quads); }
    }

    private record Part(List<BakedQuad> quads, net.minecraft.client.resources.model.sprite.Material.Baked particle) implements BlockStateModelPart {
        @Override public List<BakedQuad> getQuads(Direction direction) { return direction == null ? quads : List.of(); }
        @Override public boolean useAmbientOcclusion() { return false; }
        @Override public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial() { return particle; }
        @Override public int materialFlags() { return meshMaterialFlags(quads); }
    }

    private static int meshMaterialFlags(List<BakedQuad> quads) {
        int flags = 0;
        for (BakedQuad quad : quads) flags |= quad.materialInfo().flags();
        return flags;
    }

    static final class Wavefront {
        static List<BakedQuad> quads(ModelBaker baker, Identifier obj, Identifier texture, float yaw, String sourceAxis, int sourceDegrees) {
            var baked = baker.materials().get(new Material(texture), () -> obj.toString());
            var lines = baker.compute(new MeshText(obj));
            List<float[]> v = new ArrayList<>(); List<float[]> vt = new ArrayList<>(); List<float[]> vn = new ArrayList<>();
            List<BakedQuad> quads = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith("v ")) v.add(floats(line));
                else if (line.startsWith("vt ")) vt.add(floats(line));
                else if (line.startsWith("vn ")) vn.add(floats(line));
                else if (line.startsWith("f ")) {
                    var corners = line.substring(2).trim().split("\\s+");
                    if (corners.length != 3) throw new IllegalStateException(obj + " face is not a triangle: " + line);
                    float[][] pos = new float[4][]; float[][] uv = new float[4][]; float[] normal = null;
                    for (int i = 0; i < 3; i++) {
                        var bits = corners[i].split("/");
                        if (bits.length < 2 || bits[1].isEmpty()) throw new IllegalStateException(obj + " face is missing a UV: " + line);
                        float[] rawVertex = v.get(index(bits[0], v.size()));
                        float[] spun = sourceDegrees == 0 ? rawVertex : sourceSpin(rawVertex, sourceAxis, sourceDegrees);
                        float[] at = rotateY(spun, yaw);
                        pos[i] = new float[] {at[0] + 0.5F, at[1], at[2] + 0.5F};
                        float[] raw = vt.get(index(bits[1], vt.size()));
                        uv[i] = new float[] {raw[0], raw[1]};
                        if (bits.length > 2 && !bits[2].isEmpty()) {
                            float[] rawNormal = vn.get(index(bits[2], vn.size()));
                            normal = rotateY(sourceDegrees == 0 ? rawNormal : sourceSpinDirection(rawNormal, sourceAxis, sourceDegrees), yaw);
                        }
                    }
                    pos[3] = pos[2]; uv[3] = uv[2];
                    if (normal == null) normal = cross(pos[0], pos[1], pos[2]);
                    quads.add(quad(baked, pos, uv, normal));
                }
            }
            if (quads.isEmpty()) throw new IllegalStateException(obj + " has no faces");
            return List.copyOf(quads);
        }
    }

    static final class Techne {
        static List<BakedQuad> poleTop(ModelBaker baker, Identifier texture) {
            var baked = baker.materials().get(new Material(texture), () -> "hbm_pole_top");
            List<BakedQuad> out = new ArrayList<>();
            box(out, baked, 0, 1, 0, 0, 0, 12, 8, 12, -6, 16, -6, 0, 0, 0, 0);
            box(out, baked, 0, 23, 0, 0, 0, 4, 16, 4, 4, 4, -8, 0, 0, 0, 0);
            box(out, baked, 0, 23, 0, 0, 0, 4, 16, 4, 4, 4, 4, 0, 0, 0, 0);
            box(out, baked, 0, 23, 0, 0, 0, 4, 16, 4, -8, 4, -8, 0, 0, 0, 0);
            box(out, baked, 0, 23, 0, 0, 0, 4, 16, 4, -8, 4, 4, 0, 0, 0, 0);
            box(out, baked, 0, 47, 0, 0, 0, 4, 2, 4, -2, 14, -2, 0, 0, 0, 0);
            return List.copyOf(out);
        }
        static List<BakedQuad> satellite(ModelBaker baker, Identifier texture, float yaw) {
            var baked = baker.materials().get(new Material(texture), () -> "hbm_satellite_receiver");
            List<BakedQuad> out = new ArrayList<>();
            box(out, baked, 0, 0, 0, 0, 0, 12, 16, 12, -6, 8, -6, 0, 0, 0, yaw);
            float rx = -0.2617994F, ry = -0.4363323F;
            box(out, baked, 10, 28, 3, 9, -8, 8, 8, 2, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 39, 3, 7, -10, 8, 2, 3, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 28, 1, 9, -10, 2, 8, 3, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 28, 11, 9, -10, 2, 8, 3, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 39, 3, 17, -10, 8, 2, 3, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 44, 6, 12, -11, 2, 2, 3, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 49, 6.5F, 12.5F, -14, 1, 1, 3, -3, 6, 0, rx, ry, 0, yaw);
            box(out, baked, 0, 53, 6, 12, -16, 2, 2, 2, -3, 6, 0, rx, ry, 0, yaw);
            return List.copyOf(out);
        }
        private static void box(List<BakedQuad> out, net.minecraft.client.resources.model.sprite.Material.Baked sprite, int tu, int tv, float ox, float oy, float oz, float w, float h, float d, float rx, float ry, float rz, float ax, float ay, float az, float outerYaw) {
            float x0 = ox, x1 = ox + w;
            float swap = x1; x1 = x0; x0 = swap;
            float y0 = oy, y1 = oy + h, z0 = oz, z1 = oz + d;
            float[][] corners = {{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},{x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
            int[][] faces = {{5,4,0,1},{2,3,7,6},{0,4,7,3},{1,0,3,2},{5,1,2,6},{4,5,6,7}};
            float[][][] uvs = uv(tu, tv, w, h, d);
            for (int f = 0; f < 6; f++) {
                float[][] pos = new float[4][]; float[][] uv = new float[4][];
                for (int i = 0; i < 4; i++) {
                    int reversed = 3 - i;
                    pos[i] = place(corners[faces[f][reversed]], rx, ry, rz, ax, ay, az, outerYaw);
                    uv[i] = uvs[f][reversed];
                }
                out.add(quad(sprite, pos, uv, cross(pos[0], pos[1], pos[2])));
            }
        }
        private static float[] place(float[] p, float rx, float ry, float rz, float ax, float ay, float az, float outerYaw) {
            var v = new Vector3f(p[0], p[1], p[2]);
            rotX(v, ax); rotY(v, ay); rotZ(v, az);
            v.add(rx, ry, rz); v.mul(0.0625F);
            rotY(v, outerYaw); rotZ(v, (float) Math.PI);
            v.add(0.5F, 1.5F, 0.5F);
            return new float[] {v.x, v.y, v.z};
        }
        private static float[][][] uv(int tu, int tv, float w, float h, float d) {
            float s = 1F / 64F;
            float u0 = tu * s, u1 = (tu + d) * s, u2 = (tu + d + w) * s, u3 = (tu + d + w + w) * s, u4 = (tu + d + w + d) * s, u5 = (tu + d + w + d + w) * s;
            float v0 = tv * s, v1 = (tv + d) * s, v2 = (tv + d + h) * s;
            return new float[][][] {
                    {{u2, v0}, {u1, v0}, {u1, v1}, {u2, v1}},
                    {{u3, v1}, {u2, v1}, {u2, v0}, {u3, v0}},
                    {{u1, v1}, {u0, v1}, {u0, v2}, {u1, v2}},
                    {{u2, v1}, {u1, v1}, {u1, v2}, {u2, v2}},
                    {{u4, v1}, {u2, v1}, {u2, v2}, {u4, v2}},
                    {{u5, v1}, {u4, v1}, {u4, v2}, {u5, v2}}
            };
        }
    }

    static final class FenceMesh {
        static List<BakedQuad> quads(ModelBaker baker, boolean north, boolean south, boolean east, boolean west) {
            var rail = baker.materials().get(new Material(Identifier.parse("jeg:block/hbm_metal_fence")), () -> "hbm_metal_fence");
            var post = baker.materials().get(new Material(Identifier.parse("jeg:block/hbm_metal_fence_post")), () -> "hbm_metal_fence_post");
            boolean hasX = west || east, hasZ = north || south;
            boolean straightX = !hasZ && west && east, straightZ = !hasX && north && south;
            if (!hasX && !hasZ) hasX = true;
            List<BakedQuad> out = new ArrayList<>();
            if (hasX) plane(out, rail, true, west ? 0F : 0.4375F, east ? 1F : 0.5625F);
            if (hasZ) plane(out, rail, false, north ? 0F : 0.4375F, south ? 1F : 0.5625F);
            if (!straightX && !straightZ) cube(out, post, 0.375F, 0.625F);
            if (out.isEmpty()) throw new IllegalStateException("Fence mesh produced no faces");
            return List.copyOf(out);
        }
        private static void plane(List<BakedQuad> out, net.minecraft.client.resources.model.sprite.Material.Baked sprite, boolean alongX, float a, float b) {
            float[][] pos = alongX
                    ? new float[][] {{a,0,0.5F},{b,0,0.5F},{b,1,0.5F},{a,1,0.5F}}
                    : new float[][] {{0.5F,0,a},{0.5F,0,b},{0.5F,1,b},{0.5F,1,a}};
            float[][] uv = {{a,1F},{b,1F},{b,0F},{a,0F}};
            out.add(quad(sprite, pos, uv, alongX ? new float[] {0,0,1} : new float[] {1,0,0}));
            float[][] back = {pos[3], pos[2], pos[1], pos[0]};
            float[][] buv = {uv[3], uv[2], uv[1], uv[0]};
            out.add(quad(sprite, back, buv, alongX ? new float[] {0,0,-1} : new float[] {-1,0,0}));
        }
        private static void cube(List<BakedQuad> out, net.minecraft.client.resources.model.sprite.Material.Baked sprite, float min, float max) {
            float[][] c = {{min,0,min},{max,0,min},{max,1,min},{min,1,min},{min,0,max},{max,0,max},{max,1,max},{min,1,max}};
            int[][] faces = {{4,5,1,0},{3,2,6,7},{0,1,2,3},{1,5,6,2},{4,0,3,7},{5,4,7,6}};
            float[][] uv = {{min,1F},{max,1F},{max,0F},{min,0F}};
            for (int[] face : faces) {
                float[][] pos = {c[face[0]], c[face[1]], c[face[2]], c[face[3]]};
                out.add(quad(sprite, pos, new float[][] {uv[0], uv[1], uv[2], uv[3]}, cross(pos[0], pos[1], pos[2])));
            }
        }
    }

    private static BakedQuad quad(net.minecraft.client.resources.model.sprite.Material.Baked sprite, float[][] pos, float[][] uv, float[] normal) {
        var info = BakedQuad.MaterialInfo.of(sprite, sprite.sprite().transparency(), CuboidFace.NO_TINT, shade(normal), 0);
        return new BakedQuad(at(pos[0]), at(pos[1]), at(pos[2]), at(pos[3]), pack(sprite, uv[0]), pack(sprite, uv[1]), pack(sprite, uv[2]), pack(sprite, uv[3]), shade(normal), info);
    }
    private static org.joml.Vector3fc at(float[] p) { return new Vector3f(p[0], p[1], p[2]); }
    private static long pack(net.minecraft.client.resources.model.sprite.Material.Baked sprite, float[] uv) {
        return UVPair.pack(sprite.sprite().getU(uv[0]), sprite.sprite().getV(uv[1]));
    }
    private static Direction shade(float[] n) {
        float ax = Math.abs(n[0]), ay = Math.abs(n[1]), az = Math.abs(n[2]);
        if (ay >= ax && ay >= az) return n[1] >= 0 ? Direction.UP : Direction.DOWN;
        if (ax >= az) return n[0] >= 0 ? Direction.EAST : Direction.WEST;
        return n[2] >= 0 ? Direction.SOUTH : Direction.NORTH;
    }
    private static float[] cross(float[] a, float[] b, float[] c) {
        float x1 = b[0]-a[0], y1 = b[1]-a[1], z1 = b[2]-a[2], x2 = c[0]-a[0], y2 = c[1]-a[1], z2 = c[2]-a[2];
        return new float[] {y1*z2-z1*y2, z1*x2-x1*z2, x1*y2-y1*x2};
    }
    private static float[] rotateY(float[] p, float yaw) {
        double c = Math.cos(yaw), s = Math.sin(yaw);
        return new float[] {(float) (p[0]*c + p[2]*s), p[1], (float) (p[2]*c - p[0]*s)};
    }
    /** Right-handed source rotation about the model center (0, 0.5, 0), before yaw. Degrees 0 does not call this. */
    private static float[] sourceSpin(float[] p, String axis, int degrees) {
        float[] spun = spin(p[0], p[1] - 0.5F, p[2], axis, degrees);
        return new float[] {spun[0], spun[1] + 0.5F, spun[2]};
    }
    /** Same rotation for a direction. No center translation, so shade and the geometric normal stay aligned. */
    private static float[] sourceSpinDirection(float[] n, String axis, int degrees) {
        return spin(n[0], n[1], n[2], axis, degrees);
    }
    private static float[] spin(float x, float y, float z, String axis, int degrees) {
        int turns = Math.floorMod(degrees, 360);
        if (turns % 90 == 0) return quarter(x, y, z, axis, turns / 90);
        double rad = Math.toRadians(degrees);
        float c = (float) Math.cos(rad), s = (float) Math.sin(rad);
        return switch (axis) {
            case "x" -> new float[] {x, y * c - z * s, y * s + z * c};
            case "y" -> new float[] {x * c + z * s, y, z * c - x * s};
            case "z" -> new float[] {x * c - y * s, x * s + y * c, z};
            default -> throw new IllegalStateException("sourceAxis " + axis);
        };
    }
    private static float[] quarter(float x, float y, float z, String axis, int k) {
        return switch (axis) {
            case "x" -> switch (k) {
                case 0 -> new float[] {x, y, z};
                case 1 -> new float[] {x, -z, y};
                case 2 -> new float[] {x, -y, -z};
                default -> new float[] {x, z, -y};
            };
            case "y" -> switch (k) {
                case 0 -> new float[] {x, y, z};
                case 1 -> new float[] {z, y, -x};
                case 2 -> new float[] {-x, y, -z};
                default -> new float[] {-z, y, x};
            };
            case "z" -> switch (k) {
                case 0 -> new float[] {x, y, z};
                case 1 -> new float[] {-y, x, z};
                case 2 -> new float[] {-x, -y, z};
                default -> new float[] {y, -x, z};
            };
            default -> throw new IllegalStateException("sourceAxis " + axis);
        };
    }
    private static void rotX(Vector3f v, float a) { float c=(float)Math.cos(a), s=(float)Math.sin(a), y=v.y*c-v.z*s, z=v.y*s+v.z*c; v.y=y; v.z=z; }
    private static void rotY(Vector3f v, float a) { float c=(float)Math.cos(a), s=(float)Math.sin(a), x=v.x*c+v.z*s, z=v.z*c-v.x*s; v.x=x; v.z=z; }
    private static void rotZ(Vector3f v, float a) { float c=(float)Math.cos(a), s=(float)Math.sin(a), x=v.x*c-v.y*s, y=v.x*s+v.y*c; v.x=x; v.y=y; }
    private static int index(String text, int size) { int i = Integer.parseInt(text); return i < 0 ? size + i : i - 1; }
    private static float[] floats(String line) { var p = line.split("\\s+"); return new float[] {Float.parseFloat(p[1]), Float.parseFloat(p[2]), p.length > 3 ? Float.parseFloat(p[3]) : 0}; }
    private record MeshText(Identifier id) implements ModelBaker.SharedOperationKey<List<String>> {
        @Override public List<String> compute(ModelBaker baker) { return read(id); }
    }

    private static List<String> read(Identifier id) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(id).orElseThrow(() -> new IllegalStateException("Missing HBM mesh " + id));
        try (var reader = resource.openAsReader()) { return reader.lines().toList(); }
        catch (Exception failure) { throw new IllegalStateException("Unreadable HBM mesh " + id, failure); }
    }

    /** Quarter turn about the block center after the source axis pose, yaw, and translation. UVs stay on the source face. */
    static final class QuarterTurn {
        static List<BakedQuad> apply(List<BakedQuad> quads, int rotation) {
            if (rotation == 0) return quads;
            Rotation turn = Rotation.values()[rotation];
            List<BakedQuad> out = new ArrayList<>();
            for (BakedQuad quad : quads) {
                var info = quad.materialInfo();
                var turned = BakedQuad.MaterialInfo.of(new net.minecraft.client.resources.model.sprite.Material.Baked(info.sprite(), false), info.sprite().transparency(), info.tintIndex(), turn.rotate(info.shadeDirectionOverride()), info.lightEmission());
                out.add(new BakedQuad(point(quad.position0(), rotation), point(quad.position1(), rotation), point(quad.position2(), rotation), point(quad.position3(), rotation), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), turn.rotate(quad.direction()), turned));
            }
            return List.copyOf(out);
        }
        private static org.joml.Vector3fc point(org.joml.Vector3fc position, int rotation) {
            float[] turned = HbmQuarterTurn.aroundBlockCenter(position.x(), position.y(), position.z(), rotation);
            return new Vector3f(turned[0], turned[1], turned[2]);
        }
    }
}
