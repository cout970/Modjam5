package com.cout970.modjam.render

import com.cout970.modelloader.api.IRenderCache
import com.cout970.modelloader.api.ModelLoaderApi
import com.cout970.modelloader.api.ModelUtilties.renderModelParts
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class ModelCache(val func: () -> Unit) : IRenderCache {
    private var id: Int = -1
    var texture: ResourceLocation? = null

    override fun render() {
        if (id == -1) {
            id = GlStateManager.glGenLists(1)
            GlStateManager.glNewList(id, GL11.GL_COMPILE)
            func()
            GlStateManager.glEndList()
        }
        GlStateManager.callList(id)
    }

    override fun close() {
        if (id != -1) {
            GlStateManager.glDeleteLists(id, 1)
        }
        id = -1
    }
}

fun createMultiTextureCache(loc: ModelResourceLocation, filter: (String) -> Boolean = { true }): List<ModelCache> {
    val model = ModelLoaderApi.getModel(loc) ?: return emptyList()
    val parts = model.modelData.parts.filter { filter(it.name) }

    val textureGrouped = parts.groupBy { it.texture }
    return textureGrouped.map {
        ModelCache {
            renderModelParts(model.modelData, it.value)
        }.apply { texture = it.key.addPrefix("textures/").addPostfix(".png") }
    }
}

fun ResourceLocation.addPrefix(str: String) = ResourceLocation(resourceDomain, str + resourcePath)
fun ResourceLocation.addPostfix(str: String) = ResourceLocation(resourceDomain, resourcePath + str)