package top.ribs.scguns.common;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.Reference;
import top.ribs.scguns.annotation.Validator;
import net.minecraft.core.HolderLookup;
import top.ribs.scguns.compat.resource.LegacyJsonResourceReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public class CustomGunLoader extends LegacyJsonResourceReloadListener
{
    private static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Identifier.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.registerTypeAdapter(ItemStack.class, JsonDeserializers.ITEM_STACK);
        builder.registerTypeAdapter(FireMode.class, JsonDeserializers.FIRE_MODE);
        builder.registerTypeAdapter(ReloadType.class, JsonDeserializers.RELOAD_TYPE);
        builder.registerTypeAdapter(GripType.class, JsonDeserializers.GRIP_TYPE);
        return builder.create();
    });

    private static CustomGunLoader instance;

    private Map<Identifier, CustomGun> customGunMap = new HashMap<>();

    public CustomGunLoader()
    {
        super("custom_guns");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler)
    {
        ImmutableMap.Builder<Identifier, CustomGun> builder = ImmutableMap.builder();
        objects.forEach((resourceLocation, object) ->
        {
            try
            {
                CustomGun customGun = GSON_INSTANCE.fromJson(object, CustomGun.class);
                if(customGun != null && Validator.isValidObject(customGun))
                {
                    builder.put(resourceLocation, customGun);
                }
                else
                {
                    ScorchedGuns.LOGGER.error("Couldn't load data file {} as it is missing or malformed", resourceLocation);
                }
            }
            catch(InvalidObjectException e)
            {
                ScorchedGuns.LOGGER.error("Missing required properties for {}", resourceLocation);
                e.printStackTrace();
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
        });
        this.customGunMap = builder.build();
    }

    /**
     * Writes all custom guns into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeCustomGuns(FriendlyByteBuf buffer)
    {
        HolderLookup.Provider registries = registryProvider(buffer);
        buffer.writeVarInt(this.customGunMap.size());
        this.customGunMap.forEach((id, gun) -> {
            buffer.writeIdentifier(id);
            buffer.writeNbt(gun.serializeNBT(registries));
        });
    }

    /**
     * Reads all registered guns from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered guns from the server
     */
    public static ImmutableMap<Identifier, CustomGun> readCustomGuns(FriendlyByteBuf buffer)
    {
        HolderLookup.Provider registries = registryProvider(buffer);
        int size = buffer.readVarInt();
        if(size > 0)
        {
            ImmutableMap.Builder<Identifier, CustomGun> builder = ImmutableMap.builder();
            for(int i = 0; i < size; i++)
            {
                Identifier id = buffer.readIdentifier();
                CustomGun customGun = new CustomGun();
                customGun.deserializeNBT(registries, buffer.readNbt());
                builder.put(id, customGun);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddServerReloadListenersEvent event)
    {
        CustomGunLoader customGunLoader = new CustomGunLoader();
        event.addListener(Identifier.fromNamespaceAndPath("scguns", "custom_guns"), customGunLoader);
        CustomGunLoader.instance = customGunLoader;
    }

    @Nullable
    public static CustomGunLoader get()
    {
        return instance;
    }

    private static HolderLookup.Provider registryProvider(FriendlyByteBuf buffer)
    {
        return buffer instanceof RegistryFriendlyByteBuf registryBuffer ? registryBuffer.registryAccess() : Gun.builtInRegistryProvider();
    }
}
