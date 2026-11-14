package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.client.screen.GunBenchRecipe;

import java.util.ArrayList;
import java.util.List;

public class S2CMessageOpenBlueprint {
    private static final int MAX_SYNCED_RECIPES = 4096;

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageOpenBlueprint> STREAM_CODEC = StreamCodec.of(
            S2CMessageOpenBlueprint::encode,
            S2CMessageOpenBlueprint::decode
    );

    private final InteractionHand hand;
    private final List<RecipeHolder<GunBenchRecipe>> recipes;

    public S2CMessageOpenBlueprint(InteractionHand hand, List<RecipeHolder<GunBenchRecipe>> recipes) {
        this.hand = hand;
        this.recipes = List.copyOf(recipes);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageOpenBlueprint message) {
        buffer.writeEnum(message.hand);
        buffer.writeVarInt(message.recipes.size());
        for (RecipeHolder<GunBenchRecipe> recipe : message.recipes) {
            RecipeHolder.STREAM_CODEC.encode(buffer, recipe);
        }
    }

    private static S2CMessageOpenBlueprint decode(RegistryFriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_SYNCED_RECIPES) {
            throw new IllegalArgumentException("Invalid blueprint recipe count: " + size);
        }

        List<RecipeHolder<GunBenchRecipe>> recipes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            RecipeHolder<?> decoded = RecipeHolder.STREAM_CODEC.decode(buffer);
            if (!(decoded.value() instanceof GunBenchRecipe recipe)) {
                throw new IllegalArgumentException("Blueprint sync contained a non-gun-bench recipe: " + decoded.id());
            }
            recipes.add(new RecipeHolder<>(decoded.id(), recipe));
        }
        return new S2CMessageOpenBlueprint(hand, recipes);
    }

    public static void handle(S2CMessageOpenBlueprint message, MessageContext context) {
        context.execute(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                ClientPlayHandler.handleOpenBlueprint(message);
            }
        });
        context.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public List<RecipeHolder<GunBenchRecipe>> getRecipes() {
        return recipes;
    }
}
