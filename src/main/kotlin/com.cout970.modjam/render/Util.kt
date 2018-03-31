package com.cout970.modjam.render

import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.vec3Of
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*

object Util {

    fun renderLine(pos: IVector3, end: IVector3, color: IVector3 = vec3Of(1, 1, 1)) {
        val tes = Tessellator.getInstance()
        val t = tes.buffer
        val r = color.x.toFloat()
        val g = color.y.toFloat()
        val b = color.z.toFloat()
        val a = 1.0f

        glDisable(GL_TEXTURE_2D)
        GlStateManager.glLineWidth(2f)
        t.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        t.pos(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex()
        t.pos(end.x, end.y, end.z).color(r, g, b, a).endVertex()
        tes.draw()
        glEnable(GL_TEXTURE_2D)
    }

    fun interp(a: Float, b: Float, level: Float) = a + (b - a) * level

    fun expInterp(a: Float, b: Float, exp: Float, level: Float): Float {
        val max = Math.pow(exp.toDouble(), 1.0)
        val min = Math.pow(exp.toDouble(), 0.0)
        val e = (Math.pow(exp.toDouble(), level.toDouble()) - min) / (max - min)
        return a + (b - a) * e.toFloat()
    }

    fun cycleLinear(a: Float, b: Float, level: Float): Float {

        return if (level < 0.5) {
            interp(a, b, level * 2f)
        } else {
            interp(b, a, (level - 0.5f) * 2f)
        }
    }

    fun cycleCos(a: Float, b: Float, level: Float): Float {
        return interp(a, b, Math.cos(level.toDouble() * 360.rads).toFloat() * 0.5f + 1f)
    }

    fun customRotate(rot: IVector3, pos: Vec3d) {
        translate(pos.x, pos.y, pos.z)
        rotate(rot.x.toFloat(), rot.y.toFloat(), rot.z.toFloat())
        translate(-pos.x, -pos.y, -pos.z)
    }

    inline fun stackMatrix(func: () -> Unit) {
        GlStateManager.pushMatrix()
        func()
        GlStateManager.popMatrix()
    }

    fun translate(x: Number = 0f, y: Number = 0f, z: Number = 0f) {
        GlStateManager.translate(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun rotate(x: Number = 0f, y: Number = 0f, z: Number = 0f) {
        GlStateManager.rotate(x.toFloat(), 1f, 0f, 0f)
        GlStateManager.rotate(y.toFloat(), 0f, 1f, 0f)
        GlStateManager.rotate(z.toFloat(), 0f, 0f, 1f)
    }

    fun getNonGroup(loc: ModelResourceLocation): List<ModelCache> {
        return createMultiTextureCache(loc, { "[" !in it })
    }

    fun getGroup(loc: ModelResourceLocation, code: String): List<ModelCache> {
        return createMultiTextureCache(loc, { it.contains("[$code]") })
    }
}

val Number.rads: Float get() = Math.toRadians(this.toDouble()).toFloat()
val Float.px: Float get() = this * 1f / 16f
val Double.px: Double get() = this * 1.0 / 16.0
val Int.px: Float get() = this * 1f / 16f


