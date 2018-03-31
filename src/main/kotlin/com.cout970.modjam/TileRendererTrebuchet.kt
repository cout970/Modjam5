package com.cout970.modjam

import com.cout970.modelloader.api.IRenderCache
import com.cout970.modelloader.api.ModelLoaderApi
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11

object TileRendererTrebuchet : TileEntitySpecialRenderer<TileTrebuchet>() {

    lateinit var base: ModelCache

    override fun render(te: TileTrebuchet, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GlStateManager.bindTexture(0)

        base.render()
        GlStateManager.popMatrix()
    }

    fun createCache(loc: ModelResourceLocation): ModelCache {
        val model = ModelLoaderApi.getModel(loc) ?: error("Unable to load model: $loc")

        return ModelCache {
            val tessellator = Tessellator.getInstance()
            val buffer = tessellator.buffer

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)

            model.bakedQuads[null]?.forEach {
                buffer.addVertexData(it.vertexData)
            }

            buffer.setTranslation(0.0, 0.0, 0.0)

            tessellator.draw()


//            renderModelParts(model.modelData, parts)
        }
    }

    fun reloadModel() {
        val loc = ModelResourceLocation(BlockHolder.trebuchet!!.registryName!!, "inventory")
        base = createCache(loc)
    }
}

class ModelCache(val func: () -> Unit) : IRenderCache {
    private var id: Int = -1

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