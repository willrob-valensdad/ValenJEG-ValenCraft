package com.atsuishio.superbwarfare.resource.model

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBedrockModel
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller

object ArmorModelReloadListener : BedrockModelReloadListener<TreeBedrockModel>("models/bedrock/armor") {
    override fun apply(
        map: Map<Identifier, BedrockModelPOJO>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        models.clear()
        map.forEach { (location, pojo) ->
            models[location] = TreeBedrockModel.bake(pojo)
        }
    }
}