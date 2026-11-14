package com.mrcrayfish.framework.api.client;
import com.google.gson.JsonParser;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.client.resources.IDataLoader;
import com.mrcrayfish.framework.client.resources.IResourceSupplier;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.apache.commons.lang3.tuple.Pair;
/** Loads authored .scmeta and item-model JSON into the gun property reader. */
public final class FrameworkClientAPI {
    private static final List<IDataLoader<?>> LOADERS = new ArrayList<>();
    private static boolean registered;
    private FrameworkClientAPI() {}
    public static void registerDataLoader(IDataLoader<?> loader) {
        LOADERS.add(loader);
        if (!registered) {
            registered = true;
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new MetaReload());
        }
    }
    public static DataObject getOpenModelData(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom != null && !custom.isEmpty()) {
            DataObject fromTag = DataObject.fromCompound(custom.copyTag());
            if (!fromTag.isEmpty()) return fromTag;
        }
        return DataObject.EMPTY;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void loadAll(ResourceManager manager) {
        for (IDataLoader loader : LOADERS) {
            List<Pair<IResourceSupplier, DataObject>> loaded = new ArrayList<>();
            for (Object supplierObject : loader.getResourceSuppliers()) {
                IResourceSupplier supplier = (IResourceSupplier) supplierObject;
                var resource = manager.getResource(supplier.getLocation());
                if (resource.isEmpty()) continue;
                try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                    loaded.add(Pair.of(supplier, DataObject.parse(reader)));
                } catch (Exception failure) {
                    throw new IllegalStateException("Could not read " + supplier.getLocation(), failure);
                }
            }
            loader.process(loaded);
        }
    }
    private static final class MetaReload implements IdentifiableResourceReloadListener {
        @Override public Identifier getFabricId() { return Identifier.fromNamespaceAndPath("scguns", "framework_meta"); }
        @Override public CompletableFuture<Void> reload(PreparableReloadListener.SharedState state, Executor background, PreparableReloadListener.PreparationBarrier barrier, Executor game) {
            return CompletableFuture.runAsync(() -> loadAll(state.resourceManager()), background).thenCompose(barrier::wait).thenRunAsync(() -> {}, game);
        }
    }
}
