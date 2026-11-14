package ttv.migami.jeg.tech.astra;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/** Offline native APIs and resource decoding; does not launch a server or client. */
public final class CoalGeneratorNativeContracts {
    private static int checks;
    private static void require(boolean pass, String label) {
        checks++;
        if (!pass) throw new AssertionError(label);
    }
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Native contract harness only, matching the existing station contract's
        // bootstrap registration phase; production registration never reopens registries.
        var frozen = MappedRegistry.class.getDeclaredField("frozen"); frozen.setAccessible(true);
        var intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders"); intrusive.setAccessible(true);
        var allTags = MappedRegistry.class.getDeclaredField("allTags"); allTags.setAccessible(true);
        var unbound = Class.forName("net.minecraft.core.MappedRegistry$TagSet").getDeclaredMethod("unbound"); unbound.setAccessible(true);
        List<Registry<?>> registries = List.of(BuiltInRegistries.BLOCK, BuiltInRegistries.BLOCK_ENTITY_TYPE,
            BuiltInRegistries.ITEM, BuiltInRegistries.DATA_COMPONENT_TYPE);
        for (Registry<?> registry : registries) {
            frozen.set(registry, false); allTags.set(registry, unbound.invoke(null));
            if (registry != BuiltInRegistries.DATA_COMPONENT_TYPE) intrusive.set(registry, new IdentityHashMap<>());
        }
        AstraTechContent.registerContent();
        team.reborn.energy.impl.EnergyImpl.init();
        for (Registry<?> registry : registries) registry.freeze();
        var lookup = net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        for (var pending : BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup)) pending.apply();

        AtomicInteger dirty = new AtomicInteger(), consumed = new AtomicInteger();
        CoalGeneratorPower power = new CoalGeneratorPower(dirty::incrementAndGet);
        require(!power.tick(() -> 1600, consumed::incrementAndGet), "Donor ignition does not generate early");
        require(consumed.get() == 1 && power.burnRemaining() == 1600, "Consume exactly one fuel on ignition");
        require(power.tick(() -> 0, consumed::incrementAndGet) && power.getAmount() == 20 && power.burnRemaining() == 1599, "Original 20 E/t generation");
        power.restore(CoalGeneratorPower.CAPACITY, 1599, 1600);
        require(!power.tick(() -> 1600, consumed::incrementAndGet) && power.burnRemaining() == 1599 && consumed.get() == 1, "Full buffer pauses current burn and new fuel consumption");
        power.restore(9990, 2, 1600);
        power.tick(() -> 0, consumed::incrementAndGet);
        require(power.getAmount() == 10000 && power.burnRemaining() == 1, "Partial final generation clamps capacity");
        power.restore(0, 0, 0); power.tick(() -> 50000, consumed::incrementAndGet);
        require(power.burnRemaining() == 20000 && power.burnTotal() == 20000, "Original burn cap");
        power.restore(200, 10, 1600);
        SimpleEnergyStorage target = new SimpleEnergyStorage(1000, 100, 100);
        try (var outer = Transaction.openOuter()) {
            require(power.insert(100, outer) == 0, "Generator rejects external insertion");
            require(EnergyStorageUtil.move(power.output(), target, 500, outer) == 100, "Original 100 E transfer limit");
            try (var nested = outer.openNested()) { EnergyStorageUtil.move(power.output(), target, 100, nested); nested.commit(); }
        }
        require(power.getAmount() == 200 && target.getAmount() == 0, "Parent abort restores both participants after nested commit");
        try (var transaction = Transaction.openOuter()) {
            require(EnergyStorageUtil.move(power.output(), target, 100, transaction) == 100, "Finite output moved");
            transaction.commit();
        }
        require(power.getAmount() == 100 && target.getAmount() == 100 && dirty.get() > 0, "Committed transfer conserves energy and marks owner dirty");
        boolean rejected = false;
        try (var transaction = Transaction.openOuter()) { power.extract(-1, transaction); }
        catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Negative extraction rejected");

        var generator = new CoalGeneratorBlockEntity(BlockPos.ZERO, AstraTechContent.COAL_GENERATOR.defaultBlockState());
        require(CoalGeneratorBlockEntity.isFuel(new ItemStack(Items.COAL)), "Native coal fuel component");
        require(!CoalGeneratorBlockEntity.isFuel(new ItemStack(Items.LAVA_BUCKET)) && !CoalGeneratorBlockEntity.isFuel(new ItemStack(Items.STONE)), "Original bucket restriction and nonfuel rejection");
        // This classpath harness does not install Fabric's ItemVariantCache mixin.
        // Set up native inventory directly for serialization contracts only.
        // insertHeld, charging, real TechReborn items and transfer contexts remain
        // strict in CoalGeneratorWorldChecks under the proper Fabric launcher.
        generator.setItem(1, new ItemStack(Items.COAL, 12));
        generator.power().restore(150, 37, 1600);

        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
        generator.saveAdditional(output);
        var restored = new CoalGeneratorBlockEntity(BlockPos.ZERO, AstraTechContent.COAL_GENERATOR.defaultBlockState());
        restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, lookup, output.buildResult()));
        require(restored.power().getAmount() == 150 && restored.power().burnRemaining() == 37 && restored.power().burnTotal() == 1600, "Native energy and burn persistence roundtrip");
        require(restored.getItem(1).is(Items.COAL) && restored.getItem(1).getCount() == 12 && restored.getItem(0).isEmpty(), "Native fuel inventory persistence roundtrip");
        require(restored.getSlotsForFace(net.minecraft.core.Direction.DOWN).length == 1
            && restored.getSlotsForFace(net.minecraft.core.Direction.DOWN)[0] == 1, "Original fuel-only sided automation");
        var tag = output.buildResult(); tag.putLong("Energy", Long.MAX_VALUE); tag.putInt("CookTime", Integer.MAX_VALUE); tag.putInt("CookTimeTotal", Integer.MAX_VALUE);
        restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, lookup, tag));
        require(restored.power().getAmount() == 10000 && restored.power().burnRemaining() == 20000, "Malformed save bounds cannot create unbounded energy or burn");
        var recipeJson = JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/jeg/recipe/valen_coal_generator.json")));
        var recipe = Recipe.DIRECT_CODEC.parse(net.minecraft.resources.RegistryOps.create(
            com.mojang.serialization.JsonOps.INSTANCE, lookup), recipeJson).getOrThrow();
        require(recipe.getClass().getSimpleName().contains("Shaped"), "Native 26.3 source recipe decodes");
        System.out.println("Ad Astra coal-generator classpath contracts PASS: " + checks + " checks; pure buffer/transaction/native inventory-persistence/recipe only. Item interaction and real energy interoperability require CoalGeneratorWorldChecks under proper Fabric; no world/client/server launched");
    }
}
