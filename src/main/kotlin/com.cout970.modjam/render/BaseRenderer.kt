package com.cout970.modjam.render

import com.cout970.modjam.render.Util.stackMatrix
import com.cout970.modjam.render.Util.translate
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity

abstract class BaseRenderer<T : TileEntity> : TileEntitySpecialRenderer<T>() {


    override fun render(te: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        stackMatrix {
            translate(x, y, z + 2)
            renderTile(te, partialTicks)
        }
    }

    abstract fun renderTile(te: T, partialTicks: Float)

    fun List<ModelCache>.renderTextured() {
        groupBy { it.texture }.forEach { (texture, elems) ->
            texture?.let { bindTexture(it) }
            elems.forEach { it.render() }
        }
    }

    override fun isGlobalRenderer(te: T) = true
}