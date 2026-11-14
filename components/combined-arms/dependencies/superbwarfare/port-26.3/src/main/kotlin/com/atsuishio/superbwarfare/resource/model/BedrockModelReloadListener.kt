package com.atsuishio.superbwarfare.resource.model

import com.atsuishio.superbwarfare.Mod
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.GsonUtil
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO
import com.google.gson.Gson
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.GsonHelper
import net.minecraft.util.profiling.ProfilerFiller

abstract class BedrockModelReloadListener<T> @JvmOverloads constructor(
    val modelPath: String,
    val animPath: String = ""
) : SimplePreparableReloadListener<Map<Identifier, BedrockModelPOJO>>(), IdentifiableResourceReloadListener {
    val gson: Gson = GsonUtil.CLIENT_GSON
    val models: MutableMap<Identifier, T> = hashMapOf()
    val animFiles: MutableMap<Identifier, BedrockAnimationFile> = hashMapOf()
    val animations: MutableMap<Identifier, List<BedrockAnimation>> = hashMapOf()

    val idToModelPaths: MutableMap<Identifier, Identifier> = hashMapOf()
    val animPathToIds: MutableMap<Identifier, Identifier> = hashMapOf()

    override fun getFabricId(): Identifier = Mod.loc("reload/$modelPath")

    public override fun prepare(
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ): Map<Identifier, BedrockModelPOJO> {
        val map = hashMapOf<Identifier, BedrockModelPOJO>()
        val modelConverter = FileToIdConverter.json(this.modelPath)

        for ((location, resource) in modelConverter.listMatchingResources(resourceManager).entries) {
            var id = modelConverter.fileToId(location)
            id = Identifier.fromNamespaceAndPath(id.namespace, id.path.removeSuffix(".geo"))
            try {
                resource.openAsReader().use {
                    val pojo = GsonHelper.fromJson(this.gson, it, BedrockModelPOJO::class.java)
                    val existed = map.put(location, pojo)
                    idToModelPaths[id] = location
                    if (existed != null) {
                        throw IllegalStateException("Duplicate model resource $location")
                    }
                }
            } catch (e: Exception) {
                Mod.LOGGER.error("Error while reading model $location", e)
            }
        }

        if (this.animPath.isNotEmpty()) {
            val animConverter = FileToIdConverter.json(this.animPath)

            for ((location, resource) in animConverter.listMatchingResources(resourceManager).entries) {
                var id = animConverter.fileToId(location)
                id = Identifier.fromNamespaceAndPath(id.namespace, id.path.removeSuffix(".animation"))

                try {
                    resource.openAsReader().use {
                        val file = GsonHelper.fromJson(this.gson, it, BedrockAnimationFile::class.java)
                        val existed = this.animFiles.put(location, file)
                        animPathToIds[location] = id
                        if (existed != null) {
                            throw IllegalStateException("Duplicate animation resource $location")
                        }
                    }
                } catch (e: Exception) {
                    Mod.LOGGER.error("Error while reading animation $location", e)
                }
            }
        }

        return map
    }

    public override fun apply(
        map: Map<Identifier, BedrockModelPOJO>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        this.models.clear()
        this.animations.clear()
    }

    fun getModel(path: Identifier): T? = this.models[path]

    fun getAnimation(path: Identifier): List<BedrockAnimation>? = this.animations[path]
}
