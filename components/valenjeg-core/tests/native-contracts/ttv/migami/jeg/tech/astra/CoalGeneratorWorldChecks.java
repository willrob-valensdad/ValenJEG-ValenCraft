package ttv.migami.jeg.tech.astra;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.reborn.energy.api.EnergyStorage;

/**
 * COPIED-FIXTURE ONLY. Real server-tick observer; never part of the product JAR.
 * A single root-owned StationProbe hook calls start(server) with valen.astra.probe=true.
 */
public final class CoalGeneratorWorldChecks {
    private static final Path ALLOWED_FIXTURE = Path.of(System.getProperty("valenjeg.fixtureRoot", "VALENJEG_FIXTURE_NOT_CONFIGURED") + "/server");
    private static final BlockPos POS = new BlockPos(104, 220, 104);
    private static boolean started;
    private final MinecraftServer server;
    private final ServerLevel level;
    private final Path output;
    private final JsonObject receipt = new JsonObject();
    private final JsonArray passed = new JsonArray();
    private final List<BlockPos> owned = List.of(POS, POS.east(), POS.west(), POS.north(), POS.south(), POS.above(), POS.below());
    private final List<BlockState> originals = new ArrayList<>();
    private CoalGeneratorBlockEntity generator;
    private Item battery;
    private ItemStack chargedBattery = ItemStack.EMPTY;
    private EnergyStorage boxInput;
    private boolean forcedAdded, done, initializedWorld, receiptOwned;
    private int phase, phaseTick, firstTick, nativeBurn, pausedBurn;
    private long initialBurn, batterySeed, batteryStart, boxSeed, expectedBatteryFirst;

    public static void start(MinecraftServer server) {
        if (!Boolean.getBoolean("valen.astra.probe")) return;
        if (started) throw new IllegalStateException("Coal-generator fixture probe started twice");
        started = true;
        CoalGeneratorWorldChecks probe = new CoalGeneratorWorldChecks(server);
        try { probe.begin(); }
        catch (Throwable failure) { probe.finish(failure); }
        if (!probe.done) ServerTickEvents.END_SERVER_TICK.register(current -> {
            if (current != server || probe.done) return;
            try { probe.tick(); } catch (Throwable failure) { probe.finish(failure); }
        });
    }

    private CoalGeneratorWorldChecks(MinecraftServer server) {
        this.server = server;
        level = server.overworld();
        output = server.getServerDirectory().resolve("valen-astra-tech-probe.json");
        receipt.add("passed", passed);
        receipt.addProperty("status", "running");
        receipt.addProperty("started_utc", Instant.now().toString());
        receipt.addProperty("uses_real_server_ticks", true);
        receipt.addProperty("manual_generator_ticker_calls", 0);
        receipt.addProperty("production_world_modified", false);
        receipt.addProperty("client_started_by_probe", false);
    }

    private void check(boolean pass, String label) {
        if (!pass) throw new AssertionError(label);
    }
    private void passed(String label) { passed.add(label); }
    private void phase(int value) {
        phase = value; phaseTick = server.getTickCount();
        receipt.addProperty("phase", value); write();
    }
    private int elapsed() { return server.getTickCount() - phaseTick; }
    private void write() {
        try { Files.writeString(output, new GsonBuilder().setPrettyPrinting().create().toJson(receipt) + "\n"); }
        catch (Exception failure) { throw new IllegalStateException("Cannot write generator fixture receipt", failure); }
    }
    private List<ItemEntity> drops() { return level.getEntitiesOfClass(ItemEntity.class, new AABB(POS).inflate(3)); }
    private long dropCount(Item item) { return drops().stream().filter(entity -> entity.getItem().is(item)).mapToLong(entity -> entity.getItem().getCount()).sum(); }
    private void clearDrops() { for (ItemEntity entity : drops()) entity.discard(); }

    private void begin() throws Exception {
        check(server.getServerDirectory().toRealPath().equals(ALLOWED_FIXTURE.toRealPath()), "Refusing generator tests outside exact copied fixture server directory");
        check(Boolean.getBoolean("valen.station.probe"), "Generator world probe requires the root-owned station fixture opt-in");
        check(!Files.exists(output), "Prior generator receipt exists; archive/reset the copied fixture before rerunning");
        receiptOwned = true;
        receipt.addProperty("fixture_directory", server.getServerDirectory().toRealPath().toString());
        Path product = Path.of(AstraTech.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        check(Files.isRegularFile(product), "Fixture product must be a staged JAR");
        receipt.addProperty("product_sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(product))));
        Identifier batteryId = Identifier.parse("techreborn:red_cell_battery");
        Identifier boxId = Identifier.parse("techreborn:low_voltage_su");
        check(BuiltInRegistries.ITEM.containsKey(batteryId), "Actual installed TechReborn red-cell battery registry entry missing");
        battery = BuiltInRegistries.ITEM.getValue(batteryId);
        check(battery.getClass().getName().equals("techreborn.items.BatteryItem"), "Actual TechReborn battery entry is not its native BatteryItem");
        check(BuiltInRegistries.BLOCK.containsKey(boxId), "Actual installed TechReborn Battery Box entry missing");
        check(BuiltInRegistries.BLOCK.getValue(Identifier.parse("jeg:valen_coal_generator")) == AstraTechContent.COAL_GENERATOR, "Generator registry does not resolve the real port block");
        receipt.addProperty("battery_id", batteryId.toString());
        receipt.addProperty("battery_class", battery.getClass().getName());
        receipt.addProperty("storage_id", boxId.toString());
        receipt.addProperty("source_target_evidence", "Installed TechReborn-6.2.1.jar assets/techreborn/items/red_cell_battery.json and blockstates/low_voltage_su.json; native BatteryItem and registered LowVoltageSUBlockEntity subclass assertions");
        level.getChunk(POS.getX() >> 4, POS.getZ() >> 4);
        for (BlockPos pos : owned) {
            check(level.getBlockState(pos).isAir() && level.getBlockEntity(pos) == null, "Fixture probe position is occupied: " + pos);
            originals.add(level.getBlockState(pos));
        }
        check(drops().isEmpty(), "Fixture probe region contains unrelated item entities");
        forcedAdded = level.setChunkForced(POS.getX() >> 4, POS.getZ() >> 4, true);
        initializedWorld = true;
        placeGenerator();
        ItemStack held = new ItemStack(Items.COAL, 2);
        generator.insertHeld(held);
        check(held.isEmpty() && generator.getItem(1).getCount() == 2, "Native fuel insertion failed");
        nativeBurn = Math.min(CoalGeneratorPower.MAX_BURN, generator.fuelTicks());
        check(nativeBurn > 20, "Actual native coal burn time is absent or unexpectedly short");
        receipt.addProperty("native_coal_burn_ticks", nativeBurn);
        firstTick = server.getTickCount();
        phase(0);
    }

    private void placeGenerator() {
        level.setBlockAndUpdate(POS, AstraTechContent.COAL_GENERATOR.defaultBlockState());
        check(level.getBlockEntity(POS) instanceof CoalGeneratorBlockEntity, "Registered generator has no real native block entity");
        generator = (CoalGeneratorBlockEntity)level.getBlockEntity(POS);
        check(generator.getType() == AstraTechContent.COAL_GENERATOR_ENTITY, "Generator block/entity registration mismatch");
    }

    private EnergyStorage batteryEnergy() {
        EnergyStorage energy = ContainerItemContext.ofSingleSlot(ContainerStorage.of(generator, null).getSlot(0)).find(EnergyStorage.ITEM);
        check(energy != null, "Actual installed TechReborn battery exposes no TeamReborn ITEM storage; compatibility failed");
        return energy;
    }
    private void replaceGenerator(Long stored, Integer remaining, Integer total) {
        CompoundTag tag = generator.saveWithFullMetadata(server.registryAccess());
        if (stored != null) tag.putLong("Energy", stored);
        if (remaining != null) tag.putInt("CookTime", remaining);
        if (total != null) tag.putInt("CookTimeTotal", total);
        BlockEntity loaded = BlockEntity.loadStatic(POS, level.getBlockState(POS), tag, server.registryAccess());
        check(loaded instanceof CoalGeneratorBlockEntity, "Native save-load did not return registered generator entity");
        level.setBlockEntity(loaded);
        generator = (CoalGeneratorBlockEntity)loaded;
        check(generator.getLevel() == level, "Native replacement generator is detached from copied fixture");
    }
    private void fillGeneratorForIsolation(long amount) {
        generator.clearContent();
        replaceGenerator(amount, 0, 0);
        check(generator.power().getAmount() == amount && generator.power().burnRemaining() == 0, "Explicit native test seed failed");
    }

    private void tick() throws Exception {
        check(server.getTickCount() - firstTick <= 600, "Generator world checks exceeded 600 actual server ticks");
        check(elapsed() <= 100, "Generator phase timed out waiting for actual native ticking: " + phase);
        switch (phase) {
            case 0 -> {
                if (generator.power().burnTotal() == 0) return;
                check(generator.power().burnTotal() == nativeBurn && generator.power().burnRemaining() == nativeBurn, "First actual generator tick did not retain native ignition duration");
                check(generator.power().getAmount() == 0 && generator.getItem(1).getCount() == 1, "Actual ignition consumed wrong fuel or generated early");
                initialBurn = generator.power().burnRemaining();
                passed("Actual registered block ticker ignites one native fuel without generating on the ignition tick");
                phase(1);
            }
            case 1 -> {
                long burned = initialBurn - generator.power().burnRemaining();
                check(generator.power().getAmount() == burned * CoalGeneratorPower.GENERATION, "Actual finite burn does not conserve the 20 E/t source generation rate");
                check(generator.getItem(1).getCount() == 1, "Actual burning consumed a second fuel prematurely");
                if (burned < 20) return;
                receipt.addProperty("observed_native_generated_energy", generator.power().getAmount());
                pausedBurn = generator.power().burnRemaining();
                replaceGenerator(CoalGeneratorPower.CAPACITY, null, null);
                passed("Twenty real burning ticks generated exactly the source-defined finite energy");
                phase(2);
            }
            case 2 -> {
                check(generator.power().getAmount() == CoalGeneratorPower.CAPACITY && generator.power().burnRemaining() == pausedBurn
                    && generator.getItem(1).getCount() == 1, "Full generator did not pause real burn and preserve queued fuel");
                if (elapsed() < 20) return;
                EnergyStorage sided = EnergyStorage.SIDED.find(level, POS, Direction.UP);
                check(sided != null && sided.getAmount() == 10000 && !sided.supportsInsertion(), "Generator is absent from the real network API or accepts external input");
                try (var transaction = Transaction.openOuter()) { check(sided.extract(100, transaction) == 100, "Native output extraction preview failed"); }
                check(sided.getAmount() == 10000, "Native extraction abort changed physical generator amount");
                try (var transaction = Transaction.openOuter()) { check(sided.extract(100, transaction) == 100, "Native committed output extraction failed"); transaction.commit(); }
                check(generator.power().getAmount() == 9900, "Native network output did not debit the physical buffer");
                passed("Full buffer paused twenty actual ticks; native output transaction abort and committed debit verified");
                phase(3);
            }
            case 3 -> {
                if (generator.power().getAmount() < 10000) return;
                check(generator.power().burnRemaining() == pausedBurn - 5 && generator.getItem(1).getCount() == 1, "Actual burn did not resume for exactly five source ticks after 100 E extraction");
                fillGeneratorForIsolation(1000);
                ItemStack realBattery = new ItemStack(battery);
                generator.insertHeld(realBattery);
                check(realBattery.isEmpty() && generator.getItem(0).is(battery), "Actual existing TechReborn battery did not enter native battery slot");
                EnergyStorage itemEnergy = batteryEnergy();
                check(itemEnergy.getAmount() == 0 && itemEnergy.getCapacity() > 0 && itemEnergy.supportsInsertion(), "Actual TechReborn battery has no usable empty native storage");
                batterySeed = generator.power().getAmount(); batteryStart = itemEnergy.getAmount();
                try (var transaction = Transaction.openOuter()) { expectedBatteryFirst = itemEnergy.insert(100, transaction); }
                check(expectedBatteryFirst > 0 && batteryEnergy().getAmount() == batteryStart, "Actual TechReborn battery refuses charging, or insertion preview fails rollback");
                receipt.addProperty("actual_battery_capacity", itemEnergy.getCapacity());
                receipt.addProperty("actual_battery_first_transfer_limit", expectedBatteryFirst);
                passed("Native generator resumes its finite paused burn after network extraction");
                phase(4);
            }
            case 4 -> {
                long actual = batteryEnergy().getAmount();
                if (actual == batteryStart) return;
                check(actual - batteryStart == expectedBatteryFirst, "Actual battery first tick ignored the native target admission limit");
                check(generator.power().getAmount() + actual == batterySeed + batteryStart, "Actual battery charge created or lost energy");
                receipt.addProperty("first_actual_battery_charged_energy", actual);
                phase(5);
            }
            case 5 -> {
                long before = batteryEnergy().getAmount();
                check(generator.power().getAmount() + before == batterySeed + batteryStart, "Continued native battery charging fails conservation");
                if (elapsed() < 12) return;
                long buffer = generator.power().getAmount();
                replaceGenerator(null, null, null);
                check(generator.power().getAmount() == buffer && batteryEnergy().getAmount() == before, "Native save-load lost buffer or real TechReborn battery energy component");
                chargedBattery = generator.removeItemNoUpdate(0);
                check(chargedBattery.is(battery), "Native battery retrieval lost original item");
                receipt.addProperty("actual_battery_energy_after_reload", before);
                passed("Real existing TechReborn battery charges through its native API, conserves energy, and survives native generator inventory/save-load");
                fillGeneratorForIsolation(1000);
                boxSeed = 1000;
                Block box = BuiltInRegistries.BLOCK.getValue(Identifier.parse("techreborn:low_voltage_su"));
                level.setBlockAndUpdate(POS.east(), box.defaultBlockState());
                BlockEntity nativeBox = level.getBlockEntity(POS.east());
                // Native 6.2.1 LowVoltageSUBlock constructs this registered subclass;
                // exact equality with its EnergyStorageBlockEntity base rejects valid storage.
                receipt.addProperty("actual_storage_block", BuiltInRegistries.BLOCK.getKey(level.getBlockState(POS.east()).getBlock()).toString());
                receipt.addProperty("actual_storage_class", nativeBox == null ? "absent" : nativeBox.getClass().getName());
                receipt.addProperty("actual_storage_entity_type", nativeBox == null ? "absent" : BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(nativeBox.getType()).toString());
                check(nativeBox != null, "Actual Battery Box block entity is absent; actual block/class recorded");
                check(Class.forName("techreborn.blockentity.storage.energy.LowVoltageSUBlockEntity").isInstance(nativeBox)
                    && Class.forName("techreborn.blockentity.storage.energy.EnergyStorageBlockEntity").isInstance(nativeBox),
                    "Actual Battery Box is not its native low-voltage energy-storage subclass; actual class recorded");
                check(nativeBox.getType() == BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Identifier.parse("techreborn:low_voltage_su")),
                    "Actual Battery Box entity is not registered under the exact native low_voltage_su type");
                nativeBox.getClass().getMethod("onLoad").invoke(nativeBox);
                nativeBox.getClass().getMethod("setFacing", Direction.class).invoke(nativeBox, Direction.NORTH);
                boxInput = EnergyStorage.SIDED.find(level, POS.east(), Direction.WEST);
                receipt.addProperty("actual_storage_api_class", boxInput == null ? "absent" : boxInput.getClass().getName());
                check(boxInput != null && boxInput.getAmount() == 0 && boxInput.supportsInsertion(), "Actual Battery Box has no empty native west-face input; no substitute permitted");
                long possible;
                try (var transaction = Transaction.openOuter()) { possible = boxInput.insert(100, transaction); }
                check(possible > 0 && boxInput.getAmount() == 0, "Real Battery Box insertion preview rejects energy or fails rollback");
                receipt.addProperty("actual_storage_class", nativeBox.getClass().getName());
                receipt.addProperty("actual_storage_capacity", boxInput.getCapacity());
                receipt.addProperty("storage_on_load", "Native MachineBaseBlockEntity.onLoad invoked after placement, as in existing accepted energy probe");
                phase(6);
            }
            case 6 -> {
                long received = boxInput.getAmount();
                if (received == 0) return;
                check(generator.power().getAmount() + received == boxSeed, "Actual adjacent Battery Box transfer fails physical conservation");
                if (elapsed() < 12 && generator.power().getAmount() > 0) return;
                receipt.addProperty("actual_storage_energy_received", received);
                receipt.addProperty("generator_energy_after_storage_transfer", generator.power().getAmount());
                passed("Automatic generator neighbor output charges the actual existing TechReborn Battery Box native west-face API");
                level.setBlockAndUpdate(POS.east(), Blocks.AIR.defaultBlockState());
                generator.clearContent();
                level.setBlockAndUpdate(POS, Blocks.AIR.defaultBlockState());
                placeGenerator();
                level.setBlockAndUpdate(POS.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
                check(level.getBlockEntity(POS.above()) instanceof HopperBlockEntity, "Actual native input hopper missing");
                ((HopperBlockEntity)level.getBlockEntity(POS.above())).setItem(0, new ItemStack(Items.COAL, 2));
                phase(7);
            }
            case 7 -> {
                HopperBlockEntity hopper = (HopperBlockEntity)level.getBlockEntity(POS.above());
                if (elapsed() < 30) return;
                int hopperFuel = 0;
                for (int slot = 0; slot < hopper.getContainerSize(); slot++) if (hopper.getItem(slot).is(Items.COAL)) hopperFuel += hopper.getItem(slot).getCount();
                check(generator.power().burnTotal() == nativeBurn && generator.power().burnRemaining() > 0, "Actual hopper-delivered fuel never ignited the registered generator");
                check(hopperFuel == 0 && generator.getItem(1).getCount() == 1 && generator.getItem(0).isEmpty(), "Actual hopper did not deliver exactly two fuel items through original fuel-only sided inventory");
                hopper.clearContent();
                level.setBlockAndUpdate(POS.above(), Blocks.AIR.defaultBlockState());
                generator.insertHeld(chargedBattery);
                check(chargedBattery.isEmpty() && generator.getItem(0).is(battery), "Real charged battery reinsertion failed");
                level.setBlockAndUpdate(POS.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
                passed("Actual ticking native hopper delivered two coal into the source fuel slot; exactly one became active burn");
                phase(8);
            }
            case 8 -> {
                if (elapsed() < 20) return;
                HopperBlockEntity hopper = (HopperBlockEntity)level.getBlockEntity(POS.below());
                int extracted = 0;
                for (int slot = 0; slot < hopper.getContainerSize(); slot++) if (hopper.getItem(slot).is(Items.COAL)) extracted += hopper.getItem(slot).getCount();
                check(extracted == 1 && generator.getItem(1).isEmpty() && generator.getItem(0).is(battery), "Actual native lower hopper must extract only fuel and leave the battery slot protected");
                hopper.clearContent();
                level.setBlockAndUpdate(POS.below(), Blocks.AIR.defaultBlockState());
                generator.setItem(1, new ItemStack(Items.COAL, 3));
                long batteryBeforeBreak = batteryEnergy().getAmount();
                check(drops().isEmpty(), "Unexpected item entities exist before native removal test");
                check(level.destroyBlock(POS, true), "Native generator destruction failed");
                check(level.getBlockState(POS).isAir() && level.getBlockEntity(POS) == null, "Native removal retained generator state");
                check(dropCount(AstraTechContent.COAL_GENERATOR_ITEM) == 1 && dropCount(Items.COAL) == 3 && dropCount(battery) == 1, "Native removal did not drop exactly one generator, three fuel and the original battery once");
                ItemEntity batteryDrop = drops().stream().filter(entity -> entity.getItem().is(battery)).findFirst().orElseThrow();
                EnergyStorage droppedEnergy = ContainerItemContext.withConstant(batteryDrop.getItem()).find(EnergyStorage.ITEM);
                check(droppedEnergy != null && droppedEnergy.getAmount() == batteryBeforeBreak, "Actual dropped TechReborn battery lost native energy component");
                level.removeBlock(POS, false);
                check(dropCount(AstraTechContent.COAL_GENERATOR_ITEM) == 1 && dropCount(Items.COAL) == 3 && dropCount(battery) == 1, "Already removed generator duplicated inventory drops");
                receipt.addProperty("actual_battery_energy_in_native_drop", droppedEnergy.getAmount());
                passed("Actual native lower hopper extracts fuel only; removal drops block/fuel/real charged battery exactly once");
                checkNearlyFullRetrieval();
                finish(null);
            }
            default -> throw new AssertionError("Unknown generator fixture phase " + phase);
        }
    }


    private static final class RetrievalPlayer extends net.minecraft.server.level.ServerPlayer {
        RetrievalPlayer(MinecraftServer server, ServerLevel level) {
            super(server, level, new com.mojang.authlib.GameProfile(
                java.util.UUID.fromString("e19ec688-5f2a-45f1-8c64-a07d1ad770d8"), "ValenAstraCheck"),
                net.minecraft.server.level.ClientInformation.createDefault());
        }
        @Override public boolean hasDisconnected() { return false; }
        @Override public void sendSystemMessage(net.minecraft.network.chat.Component message) {}
    }

    /** Real native Inventory and drops; no replacement inventory or add implementation. */
    private void checkNearlyFullRetrieval() {
        clearDrops();
        placeGenerator();
        RetrievalPlayer player = new RetrievalPlayer(server, level);
        try {
            player.setPos(POS.getX() + .5, POS.getY() + 1, POS.getZ() + .5);
            player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server,
                new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),
                player, net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            var inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++)
                inventory.setItem(slot, new ItemStack(Items.STONE, 64));
            inventory.setItem(0, new ItemStack(Items.COAL, 63));
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(Items.STONE, 64));

            // Resolve the suspected API ambiguity using the exact native method,
            // then restore the same fixture setup for the production retrieval path.
            ItemStack nativeInput = new ItemStack(Items.COAL, 5);
            boolean nativePartialResult = inventory.add(nativeInput);
            check(inventory.getItem(0).getCount() == 64 && nativeInput.getCount() == 4,
                "Native near-full control did not admit exactly one coal and retain four");
            receipt.addProperty("native_inventory_partial_add_return", nativePartialResult);
            receipt.addProperty("native_inventory_partial_add_remaining", nativeInput.getCount());
            inventory.setItem(0, new ItemStack(Items.COAL, 63));

            generator.setItem(1, new ItemStack(Items.COAL, 5));
            generator.returnContents(player);
            long playerCoal = 0;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++)
                if (inventory.getItem(slot).is(Items.COAL)) playerCoal += inventory.getItem(slot).getCount();
            long dropped = dropCount(Items.COAL);
            check(playerCoal == 64 && dropped == 4 && generator.isEmpty(),
                "Actual nearly-full inventory retrieval must insert one, drop remaining four, and empty the generator");
            check(playerCoal + dropped == 63 + 5, "Actual retrieval lost or duplicated coal");
            generator.returnContents(player);
            check(inventory.getItem(0).getCount() == 64 && dropCount(Items.COAL) == 4,
                "Repeated empty retrieval duplicated native drops or inventory");
            receipt.addProperty("near_full_retrieval_player_coal_after", playerCoal);
            receipt.addProperty("near_full_retrieval_native_coal_dropped", dropped);
            passed("Actual native Survival ServerPlayer with only one free coal capacity retrieves five fuel: one inserted, four real item drops, no loss or duplicate");
        } finally {
            player.getInventory().clearContent();
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            player.discard();
            generator.clearContent();
            clearDrops();
        }
    }

    private void finish(Throwable failure) {
        if (done) return;
        done = true;
        Throwable result = failure;
        try {
            if (initializedWorld) {
                for (int i = owned.size() - 1; i >= 0; i--) {
                    BlockPos pos = owned.get(i);
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof net.minecraft.world.Container container) container.clearContent();
                    level.setBlockAndUpdate(pos, originals.get(i));
                }
                clearDrops();
                if (forcedAdded) level.setChunkForced(POS.getX() >> 4, POS.getZ() >> 4, false);
                receipt.addProperty("owned_fixture_blocks_restored", true);
                receipt.addProperty("owned_fixture_item_entities_remaining", drops().size());
                receipt.addProperty("owned_forced_ticket_removed", forcedAdded);
                receipt.addProperty("preexisting_forced_ticket_preserved", !forcedAdded);
            }
        } catch (Throwable cleanup) {
            if (result == null) result = cleanup; else result.addSuppressed(cleanup);
            receipt.addProperty("cleanup_error", cleanup.toString());
        }
        receipt.addProperty("status", result == null ? "pass" : "fail");
        receipt.addProperty("finished_utc", Instant.now().toString());
        receipt.addProperty("server_ticks_elapsed", server.getTickCount() - firstTick);
        if (result != null) { receipt.addProperty("error", result.toString()); result.printStackTrace(); }
        // Directory guard failure must not write a test receipt into an unrelated server.
        try {
            if (receiptOwned && server.getServerDirectory().toRealPath().equals(ALLOWED_FIXTURE.toRealPath())) write();
        } catch (Exception io) { io.printStackTrace(); }
        System.out.println("VALEN_ASTRA_TECH_FIXTURE " + receipt);
    }
}
