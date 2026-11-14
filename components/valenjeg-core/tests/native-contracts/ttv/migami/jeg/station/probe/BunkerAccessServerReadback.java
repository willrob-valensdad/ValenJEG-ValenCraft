package ttv.migami.jeg.station.probe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Read-only bunker acknowledgement and decor comparison. Does not teleport, place, or remove entities. */
final class BunkerAccessServerReadback {
    private static final Path FIXTURE = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
    private static final UUID PROBE = UUID.fromString("08d8976e-e59a-3751-ac2c-140b28fb5309");
    private static final String ID = "jeg:valen_minipurge/underground_shelter";
    private static final String SHA = "e205876f5ae33f3ae9a04652fbb1c2661e754568e7a7758d2c0305c59025dfdd";
    private static final Path FROZEN_R35 = Path.of(System.getProperty("valenjeg.evidence.f0fb38c54d87", "VALENJEG_EVIDENCE_NOT_CONFIGURED/valen-bunker-probe.json"));
    private static final String FROZEN_R35_SHA = "cc99a6f8739b7a8e762689d4cadbe0a7c8edc4f225eb31beac4ac1db9174f5bf";
    private static final long FROZEN_SEED = -4056840922791553232L;
    private static final BlockPos FROZEN_ORIGIN = new BlockPos(383992, 76, 609448);
    private static final Rotation FROZEN_ROTATION = Rotation.COUNTERCLOCKWISE_90;
    private static final BoundingBox FROZEN_BOUNDS = new BoundingBox(383992, 76, 609416, 384024, 101, 609448);

    private BunkerAccessServerReadback() {}

    static void acknowledge(MinecraftServer server, ServerPlayer player, JsonObject request, JsonObject receipt) throws Exception {
        guard(server, player);
        ServerLevel level = player.level();
        receipt.addProperty("result", "authoritative readback; this action does not move the player");
        receipt.addProperty("teleport_used", false);
        receipt.addProperty("walk_proof", false);
        receipt.addProperty("game_mode", player.gameMode.getGameModeForPlayer().getName());
        receipt.addProperty("x", player.getX());
        receipt.addProperty("y", player.getY());
        receipt.addProperty("z", player.getZ());
        receipt.addProperty("yaw", player.getYRot());
        receipt.addProperty("pitch", player.getXRot());
        receipt.addProperty("server_tick", server.getTickCount());
        receipt.addProperty("level_game_time", level.getGameTime());
        receipt.addProperty("dimension", level.dimension().identifier().toString());
        receipt.addProperty("on_ground", player.onGround());
        receipt.addProperty("health", player.getHealth());
        receipt.addProperty("fall_distance", player.fallDistance);
        receipt.addProperty("flying", player.getAbilities().flying);
        receipt.addProperty("fall_flying", player.isFallFlying());
        receipt.addProperty("no_physics", player.noPhysics);
        BlockPos feet = player.blockPosition();
        receipt.add("feet_block", blockAt(level, feet));
        receipt.add("below_block", blockAt(level, feet.below()));
        receipt.addProperty("on_climbable", player.onClimbable());
        if (request.has("block_x") || request.has("block_y") || request.has("block_z")) {
            if (!request.has("block_x") || !request.has("block_y") || !request.has("block_z"))
                throw new IllegalArgumentException("block readback needs block_x, block_y, and block_z");
            int bx = request.get("block_x").getAsInt(), by = request.get("block_y").getAsInt(), bz = request.get("block_z").getAsInt();
            if (bx < 344999 || bx > 345048 || by < -19 || by > 65 || bz < 512779 || bz > 512825)
                throw new IllegalArgumentException("block readback stays inside the recorded bunker box");
            receipt.add("requested_block", blockAt(level, new BlockPos(bx, by, bz)));
        }
        if(Boolean.getBoolean("valen.bunker.readbackOnly"))bindFrozen(level, player, receipt);
        if (request.has("observation_id")) receipt.addProperty("observation_id", request.get("observation_id").getAsString());
        receipt.addProperty("perspective", "client camera type is not a server field; bunker_access.perspective is the client acknowledgement");
    }

    static void decor(MinecraftServer server, ServerPlayer player, JsonObject receipt) throws Exception {
        guard(server, player);
        ServerLevel level = player.level();
        var resource = server.getResourceManager().getResource(Identifier.parse("jeg:structure/valen_minipurge/underground_shelter.nbt")).orElseThrow();
        byte[] bytes;
        try (var in = resource.open()) { bytes = in.readAllBytes(); }
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        if (!sha.equals(SHA)) throw new IllegalStateException("Bunker template bytes differ from the frozen authored structure");
        CompoundTag root;
        try (var in = new ByteArrayInputStream(bytes)) { root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()); }
        List<BlockState> palette = new ArrayList<>();
        for (Tag tag : root.getListOrEmpty("palette")) palette.add(BunkerWorldProbe.state((CompoundTag)tag));
        Structure structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.parse(ID))).value();
        StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), structure);
        if (!start.isValid() || start.getPieces().size() != 1 || !(start.getPieces().getFirst() instanceof PoolElementStructurePiece piece))
            throw new IllegalStateException("Synthetic player must already be inside the one placed bunker piece; this readback does not generate or teleport");
        if (!(piece.getElement() instanceof SinglePoolElement element) || !element.getTemplateLocation().toString().equals(ID))
            throw new IllegalStateException("Placed piece is not the authored single-pool shelter");
        Rotation rotation = piece.getRotation();
        BlockPos origin = piece.getPosition();
        if(Boolean.getBoolean("valen.bunker.readbackOnly"))bindFrozen(level, player, receipt);
        receipt.addProperty("result", "read-only decor and source route; no entity or block edits");
        receipt.addProperty("template_sha256", sha);
        receipt.addProperty("rotation", rotation.toString());
        receipt.addProperty("mirror", "NONE");
        receipt.addProperty("single_pool_element_ignores_entities", false);
        receipt.addProperty("ignore_entities_source", "SinglePoolElement.getSettings calls StructurePlaceSettings.setIgnoreEntities(false) before placeInWorld");
        receipt.add("template_origin", xyz(origin.getX(), origin.getY(), origin.getZ()));
        JsonArray stairs = new JsonArray(), opening = new JsonArray(), doors = new JsonArray();
        int stairIndex = 0;
        for (Tag tag : root.getListOrEmpty("blocks")) {
            CompoundTag block = (CompoundTag)tag;
            ListTag pos = block.getListOrEmpty("pos");
            int lx = pos.getIntOr(0, 0), ly = pos.getIntOr(1, 0), lz = pos.getIntOr(2, 0);
            BlockState expected = palette.get(block.getIntOr("state", -1)).rotate(rotation);
            BlockPos world = StructureTemplate.transform(new BlockPos(lx, ly, lz), Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
            boolean shaft = lx >= 0 && lx <= 5 && lz >= 0 && lz <= 5;
            if (shaft && expected.getBlock() instanceof StairBlock) {
                JsonObject row = new JsonObject();
                row.addProperty("index", stairIndex++);
                row.add("world", xyz(world.getX(), world.getY(), world.getZ()));
                row.add("stand", xyz(world.getX() + 0.5, world.getY() + 1.0, world.getZ() + 0.5));
                row.addProperty("facing", expected.getValue(StairBlock.FACING).getSerializedName());
                row.addProperty("half", expected.getValue(StairBlock.HALF).getSerializedName());
                row.addProperty("world_matches_source", level.getBlockState(world).equals(expected));
                stairs.add(row);
            }
            if (ly == 25 && lx >= 1 && lx <= 4 && lz >= 1 && lz <= 4) {
                JsonObject row = new JsonObject();
                row.add("world", xyz(world.getX(), world.getY(), world.getZ()));
                row.addProperty("source_air", expected.isAir());
                row.addProperty("world_air", level.getBlockState(world).isAir());
                opening.add(row);
            }
            if (expected.getBlock() instanceof DoorBlock && expected.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                JsonObject row = new JsonObject();
                row.add("world", xyz(world.getX(), world.getY(), world.getZ()));
                row.add("stand", xyz(world.getX() + 0.5, world.getY(), world.getZ() + 0.5));
                row.addProperty("id", BuiltInRegistries.BLOCK.getKey(expected.getBlock()).toString());
                row.addProperty("facing", expected.getValue(DoorBlock.FACING).getSerializedName());
                row.addProperty("hinge", expected.getValue(DoorBlock.HINGE).getSerializedName());
                row.addProperty("open", expected.getValue(DoorBlock.OPEN));
                row.addProperty("world_matches_source", level.getBlockState(world).equals(expected));
                doors.add(row);
            }
        }
        receipt.add("shaft_stairs_high_to_low", sortStairs(stairs));
        receipt.add("shaft_top_opening", opening);
        receipt.add("source_door_targets", doors);
        receipt.addProperty("route_use", "Walk targets are stand positions on shaft_stairs_high_to_low, entered from shaft_top_opening. Door targets are lower halves only. Neither list teleports the player.");
        receipt.addProperty("painting_position_expectation", "Authored transformed attachment and its box center. Predicted vanilla containing(center) is diagnostic only.");
        List<Entity> present = level.getEntitiesOfClass(Entity.class, AABB.of(start.getBoundingBox()).inflate(2));
        JsonArray expectedRows = new JsonArray();
        int matched = 0, missing = 0;
        for (Tag tag : root.getListOrEmpty("entities")) {
            CompoundTag entry = (CompoundTag)tag;
            ListTag pos = entry.getListOrEmpty("pos");
            Vec3 local = new Vec3(pos.getDoubleOr(0, 0), pos.getDoubleOr(1, 0), pos.getDoubleOr(2, 0));
            Vec3 transformed = StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).add(origin.getX(), origin.getY(), origin.getZ());
            CompoundTag nbt = entry.getCompoundOrEmpty("nbt");
            String entityId = nbt.getStringOr("id", "");
            JsonObject row = new JsonObject();
            row.addProperty("id", entityId);
            row.add("authored_rotated_pos", xyz(transformed.x, transformed.y, transformed.z));
            Vec3 expectedPos = transformed;
            AABB authoredBox = null;
            BlockPos authoredAnchor = null;
            if (entityId.equals("minecraft:painting") || entityId.equals("minecraft:item_frame") || entityId.equals("minecraft:glow_item_frame")) {
                boolean painting = entityId.equals("minecraft:painting");
                Direction sourceFacing = painting
                        ? Direction.from2DDataValue(nbt.getByteOr("facing", (byte)0))
                        : Direction.from3DDataValue(nbt.getByteOr("Facing", (byte)0));
                Direction rotatedFacing = rotateHorizontal(sourceFacing, rotation);
                BlockPos shifted = BlockPos.containing(transformed);
                ListTag blockPos = entry.getListOrEmpty("blockPos");
                authoredAnchor = StructureTemplate.transform(new BlockPos(blockPos.getIntOr(0, 0), blockPos.getIntOr(1, 0), blockPos.getIntOr(2, 0)), Mirror.NONE, rotation, BlockPos.ZERO).offset(origin);
                row.add("authored_anchor", xyz(authoredAnchor.getX(), authoredAnchor.getY(), authoredAnchor.getZ()));
                row.addProperty("rotated_facing", rotatedFacing.getSerializedName());
                if (painting) {
                    PaintingVariant variant = level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT)
                            .getOrThrow(ResourceKey.create(Registries.PAINTING_VARIANT, Identifier.parse(nbt.getStringOr("variant", "")))).value();
                    Vec3 predictedCenter = paintingCenter(shifted, rotatedFacing, variant.width(), variant.height());
                    row.add("predicted_vanilla_containing_anchor", xyz(shifted.getX(), shifted.getY(), shifted.getZ()));
                    row.add("predicted_vanilla_center", xyz(predictedCenter.x, predictedCenter.y, predictedCenter.z));
                    row.addProperty("predicted_vanilla_diagnostic_only", true);
                    row.addProperty("anchor_would_differ_under_vanilla_setPos", !authoredAnchor.equals(shifted));
                    expectedPos = paintingCenter(authoredAnchor, rotatedFacing, variant.width(), variant.height());
                    authoredBox = paintingBox(expectedPos, rotatedFacing, variant.width(), variant.height());
                    JsonArray collision = collision(level, authoredBox);
                    row.addProperty("variant", nbt.getStringOr("variant", ""));
                    row.addProperty("width", variant.width());
                    row.addProperty("height", variant.height());
                    row.add("authored_box", box(authoredBox));
                    row.add("pop_box_collision", collision);
                    row.addProperty("pop_box_blocked", !collision.isEmpty());
                } else {
                    row.add("anchor_after_setPos", xyz(shifted.getX(), shifted.getY(), shifted.getZ()));
                    row.addProperty("anchor_rewritten_by_setPos", !authoredAnchor.equals(shifted));
                    expectedPos = Vec3.atCenterOf(shifted).relative(rotatedFacing, -0.46875);
                }
            }
            if (entityId.equals("minecraft:armor_stand")) {
                ListTag rotationTag = nbt.getListOrEmpty("Rotation");
                float yaw = rotationTag.getFloatOr(0, 0);
                row.addProperty("source_yaw", yaw);
                row.addProperty("expected_yaw_after_clockwise_180", rotation == Rotation.CLOCKWISE_180 ? Mth.wrapDegrees(yaw + 180.0F) : yaw);
            }
            row.add("expected_pos", xyz(expectedPos.x, expectedPos.y, expectedPos.z));
            Entity found = nearest(present, entityId, expectedPos, 0.05);
            row.addProperty("matched_saved_entity", found != null);
            if (found instanceof HangingEntity hanging) row.addProperty("saved_facing", hanging.getDirection().getSerializedName());
            if (found instanceof Painting painting) {
                row.addProperty("saved_variant", painting.getVariant().unwrapKey().map(key -> key.identifier().toString()).orElse("unkeyed"));
                if (authoredAnchor != null) {
                    row.add("saved_anchor", xyz(painting.getPos().getX(), painting.getPos().getY(), painting.getPos().getZ()));
                    row.addProperty("saved_anchor_matches_authored", painting.getPos().equals(authoredAnchor));
                }
                if (authoredBox != null) row.addProperty("saved_box_matches_authored", sameBox(painting.getBoundingBox(), authoredBox));
            }
            if (found != null) {
                matched++;
                row.add("saved_pos", xyz(found.getX(), found.getY(), found.getZ()));
                row.addProperty("saved_yaw", found.getYRot());
            } else {
                missing++;
                JsonArray drops = new JsonArray();
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(expectedPos, expectedPos).inflate(8))) {
                    if (!item.getItem().is(Items.PAINTING)) continue;
                    JsonObject drop = new JsonObject();
                    drop.add("pos", xyz(item.getX(), item.getY(), item.getZ()));
                    drop.addProperty("count", item.getItem().getCount());
                    drops.add(drop);
                }
                row.add("nearby_painting_item_entities", drops);
            }
            expectedRows.add(row);
        }
        receipt.add("source_entities", expectedRows);
        receipt.addProperty("source_entity_count", expectedRows.size());
        receipt.addProperty("matched_saved_entities", matched);
        receipt.addProperty("unmatched_source_entities", missing);
        JsonArray extra = new JsonArray();
        for (Entity entity : present) {
            String key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (entity instanceof ServerPlayer || key.equals("minecraft:item")) continue;
            if (!(entity instanceof BlockAttachedEntity) && !key.equals("minecraft:armor_stand")) continue;
            boolean claimed = false;
            for (var expectedRow : expectedRows) {
                JsonObject row = expectedRow.getAsJsonObject();
                if (!row.get("id").getAsString().equals(key) || !row.get("matched_saved_entity").getAsBoolean()) continue;
                JsonArray saved = row.getAsJsonArray("saved_pos");
                if (saved != null && Math.abs(saved.get(0).getAsDouble() - entity.getX()) < 0.001
                        && Math.abs(saved.get(1).getAsDouble() - entity.getY()) < 0.001
                        && Math.abs(saved.get(2).getAsDouble() - entity.getZ()) < 0.001) claimed = true;
            }
            if (!claimed) {
                JsonObject row = new JsonObject();
                row.addProperty("id", key);
                row.add("pos", xyz(entity.getX(), entity.getY(), entity.getZ()));
                extra.add(row);
            }
        }
        receipt.add("saved_decor_not_in_source", extra);
        receipt.addProperty("pose_and_geometry_rewritten", false);
        receipt.addProperty("acceptance", "Counts and positions are a readback. Visual inspection of the surviving decor remains a native check.");
    }

    private static JsonArray sortStairs(JsonArray stairs) {
        List<JsonObject> rows = new ArrayList<>();
        stairs.forEach(element -> rows.add(element.getAsJsonObject()));
        rows.sort((a, b) -> Integer.compare(b.getAsJsonArray("world").get(1).getAsInt(), a.getAsJsonArray("world").get(1).getAsInt()));
        JsonArray sorted = new JsonArray();
        int index = 0;
        for (JsonObject row : rows) {
            row.addProperty("down_route_index", index++);
            sorted.add(row);
        }
        return sorted;
    }

    private static Entity nearest(List<Entity> present, String id, Vec3 pos, double limit) {
        Entity found = null;
        double best = limit * limit;
        for (Entity entity : present) {
            if (!BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(id)) continue;
            double dx = entity.getX() - pos.x, dy = entity.getY() - pos.y, dz = entity.getZ() - pos.z;
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance <= best) { best = distance; found = entity; }
        }
        return found;
    }

    private static JsonArray collision(ServerLevel level, AABB box) {
        JsonArray rows = new JsonArray();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX) && rows.size() < 32; x++) {
            for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY) && rows.size() < 32; y++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ) && rows.size() < 32; z++) {
                    cursor.set(x, y, z);
                    if (!box.intersects(cursor)) continue;
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.AIR) || state.getCollisionShape(level, cursor).isEmpty()) continue;
                    JsonObject row = new JsonObject();
                    row.add("pos", xyz(x, y, z));
                    row.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    static Vec3 paintingCenter(BlockPos anchor, Direction facing, int width, int height) {
        Vec3 attached = Vec3.atCenterOf(anchor).relative(facing, -0.46875);
        double horizontal = (width & 1) == 0 ? 0.5 : 0.0;
        double vertical = (height & 1) == 0 ? 0.5 : 0.0;
        return attached.relative(facing.getCounterClockWise(), horizontal).relative(Direction.UP, vertical);
    }

    static AABB paintingBox(Vec3 center, Direction facing, int width, int height) {
        double xSize = facing.getAxis() == Direction.Axis.X ? 0.0625 : width;
        double zSize = facing.getAxis() == Direction.Axis.Z ? 0.0625 : width;
        return AABB.ofSize(center, xSize, height, zSize);
    }

    static Direction rotateHorizontal(Direction facing, Rotation rotation) {
        if (facing.getAxis() == Direction.Axis.Y) return facing;
        return switch (rotation) {
            case CLOCKWISE_180 -> facing.getOpposite();
            case CLOCKWISE_90 -> facing.getClockWise();
            case COUNTERCLOCKWISE_90 -> facing.getCounterClockWise();
            default -> facing;
        };
    }

    private static boolean sameBox(AABB saved, AABB authored) {
        return saved.minX == authored.minX && saved.minY == authored.minY && saved.minZ == authored.minZ
                && saved.maxX == authored.maxX && saved.maxY == authored.maxY && saved.maxZ == authored.maxZ;
    }

    private static JsonArray box(AABB box) {
        JsonArray array = new JsonArray();
        array.add(box.minX); array.add(box.minY); array.add(box.minZ);
        array.add(box.maxX); array.add(box.maxY); array.add(box.maxZ);
        return array;
    }

    private static JsonArray xyz(double x, double y, double z) {
        JsonArray array = new JsonArray();
        array.add(x); array.add(y); array.add(z);
        return array;
    }

    private static void bindFrozen(ServerLevel level, ServerPlayer player, JsonObject receipt) throws Exception {
        byte[] frozenBytes = Files.readAllBytes(FROZEN_R35);
        String frozenSha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(frozenBytes));
        if (!frozenSha.equals(FROZEN_R35_SHA)) throw new IllegalStateException("Frozen R35 receipt SHA differs");
        JsonObject frozen = JsonParser.parseString(new String(frozenBytes, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray origin = frozen.getAsJsonArray("native_template_origin");
        JsonArray bounds = frozen.getAsJsonArray("native_bounds");
        if (!ID.equals(frozen.get("id").getAsString()) || frozen.get("world_seed").getAsLong() != FROZEN_SEED
                || !"COUNTERCLOCKWISE_90".equals(frozen.get("rotation").getAsString()) || frozen.get("forced_starts").getAsBoolean()
                || origin.get(0).getAsInt() != FROZEN_ORIGIN.getX() || origin.get(1).getAsInt() != FROZEN_ORIGIN.getY() || origin.get(2).getAsInt() != FROZEN_ORIGIN.getZ()
                || bounds.get(0).getAsInt() != FROZEN_BOUNDS.minX() || bounds.get(1).getAsInt() != FROZEN_BOUNDS.minY() || bounds.get(2).getAsInt() != FROZEN_BOUNDS.minZ()
                || bounds.get(3).getAsInt() != FROZEN_BOUNDS.maxX() || bounds.get(4).getAsInt() != FROZEN_BOUNDS.maxY() || bounds.get(5).getAsInt() != FROZEN_BOUNDS.maxZ())
            throw new IllegalStateException("Frozen R35 seed, id, origin, rotation, or bounds differ");
        if (level.getSeed() != FROZEN_SEED) throw new IllegalStateException("Live world seed is not the frozen R35 seed");
        Structure structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.parse(ID))).value();
        StructureStart start = level.structureManager().getStructureWithPieceAt(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), structure);
        if (!start.isValid() || start.getPieces().size() != 1 || !(start.getPieces().getFirst() instanceof PoolElementStructurePiece piece))
            throw new IllegalStateException("Live world has no single bunker piece at the player; this readback does not generate");
        BoundingBox live = start.getBoundingBox();
        if (piece.getRotation() != FROZEN_ROTATION || !piece.getPosition().equals(FROZEN_ORIGIN)
                || live.minX() != FROZEN_BOUNDS.minX() || live.minY() != FROZEN_BOUNDS.minY() || live.minZ() != FROZEN_BOUNDS.minZ()
                || live.maxX() != FROZEN_BOUNDS.maxX() || live.maxY() != FROZEN_BOUNDS.maxY() || live.maxZ() != FROZEN_BOUNDS.maxZ())
            throw new IllegalStateException("Live bunker start does not match the frozen R35 origin, rotation, and bounds");
        receipt.addProperty("frozen_r35_sha256", frozenSha);
        receipt.addProperty("frozen_r35_bound", true);
        receipt.addProperty("live_seed", level.getSeed());
        receipt.addProperty("live_rotation", piece.getRotation().toString());
        receipt.add("live_origin", xyz(piece.getPosition().getX(), piece.getPosition().getY(), piece.getPosition().getZ()));
        JsonArray liveBounds = new JsonArray();
        liveBounds.add(live.minX()); liveBounds.add(live.minY()); liveBounds.add(live.minZ());
        liveBounds.add(live.maxX()); liveBounds.add(live.maxY()); liveBounds.add(live.maxZ());
        receipt.add("live_bounds", liveBounds);
        receipt.addProperty("generation", false);
        receipt.addProperty("inventories_replayed", false);
    }

    private static JsonObject blockAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        JsonObject row = new JsonObject();
        row.add("pos", xyz(pos.getX(), pos.getY(), pos.getZ()));
        row.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        row.addProperty("block_state", state.toString());
        return row;
    }

    private static void guard(MinecraftServer server, ServerPlayer player) throws Exception {
        if (!Boolean.getBoolean("valen.fixture.bunkerAccess") || !Boolean.getBoolean("valen.station.probe")
                || !server.getServerDirectory().toRealPath().equals(FIXTURE.toRealPath())
                || !Files.isRegularFile(FIXTURE.resolve("../preparation.json")) || server.getPort() != 25589
                || !player.getName().getString().equals("ValenPackProbe") || !player.getUUID().equals(PROBE)
                || !player.getIpAddress().equals("127.0.0.1") || !player.isAlive())
            throw new IllegalStateException("Bunker readback requires the copied fixture and the loopback synthetic player");
    }
}
