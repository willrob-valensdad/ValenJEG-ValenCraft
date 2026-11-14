package ttv.migami.jeg.station.probe;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;

/** Actual native baked model and block-entity-renderer readback; no world edits. */
final class StructureFidelityClientProbe {
    static void probe(Minecraft client,JsonObject result) throws Exception {
        if (!Boolean.getBoolean("valen.structure.fidelity") ||
            !client.gameDirectory.toPath().toRealPath().equals(StructureFidelityContracts.FIXTURE.resolve("client").toRealPath()))
            throw new IllegalStateException("Refusing non-fixture model readback");
        StructureFidelityContracts.manifest();
        JsonObject server=JsonParser.parseString(Files.readString(StructureFidelityContracts.FIXTURE.resolve("server/valen-structure-fidelity-native.json"))).getAsJsonObject();
        if (!server.get("status").getAsString().equals("pass")) throw new IllegalStateException("Native server field/registry audit has not passed");
        var models=client.getModelManager().getBlockStateModelSet();JsonArray rows=new JsonArray(),failures=new JsonArray();
        for (JsonElement entry:server.getAsJsonArray("palettes")) {
            JsonObject source=entry.getAsJsonObject();var state=StructureFidelityContracts.resolve(source.getAsJsonObject("state"));
            JsonObject row=source.deepCopy();var model=models.get(state);row.addProperty("native_model_class",model.getClass().getName());
            row.addProperty("same_as_missing",model==models.missingModel());List<BlockStateModelPart> parts=new ArrayList<>();model.collectParts(RandomSource.create(0),parts);
            int quads=0;for(var part:parts){quads+=part.getQuads(null).size();for(Direction face:Direction.values())quads+=part.getQuads(face).size();}
            row.addProperty("native_parts",parts.size());row.addProperty("native_quads",quads);boolean renderer=false;
            if (state.getBlock() instanceof EntityBlock factory) {
                var entity=factory.newBlockEntity(BlockPos.ZERO,state);
                if(entity!=null){var actual=client.getBlockEntityRenderDispatcher().getRenderer(entity);renderer=actual!=null;if(renderer)row.addProperty("native_entity_renderer",actual.getClass().getName());}
            }
            if (!state.isAir() && state.getRenderShape()==RenderShape.MODEL && (model==models.missingModel() || quads==0) && !renderer)
                failures.add(state+" has no actual native model/renderer");
            row.addProperty("requires_in_game_visual_review",true);rows.add(row);
        }
        result.add("native_structure_models",rows);result.add("failures",failures);result.addProperty("status",failures.isEmpty()?"pass":"failed");
        result.addProperty("visual_acceptance",false);result.addProperty("world_mutated",false);
        Files.writeString(StructureFidelityContracts.FIXTURE.resolve("client/valen-structure-fidelity-models.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
}
