package com.cout970.modjam.render

import com.cout970.modjam.BlockHolder
import com.cout970.modjam.tile.TileTrebuchet
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer

object TileRendererTrebuchet : TileEntitySpecialRenderer<TileTrebuchet>() {

    lateinit var base: ModelCache

    override fun render(te: TileTrebuchet, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GlStateManager.bindTexture(0)
        base.render()
        GlStateManager.popMatrix()
    }

    fun reloadModel() {
        val loc = ModelResourceLocation(BlockHolder.trebuchet.registryName!!, "inventory")
        base = createCache(loc)
    }
}