package com.cout970.modjam.render

import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.vec3Of
import com.cout970.vector.extensions.xd
import com.cout970.vector.extensions.yd
import com.cout970.vector.extensions.zd
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
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

    fun translate(vec: IVector3) {
        GlStateManager.translate(vec.x, vec.y, vec.z)
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

    fun drawWireBetween(start: IVector3, end: IVector3, weight: Double) {
        val tes = Tessellator.getInstance()
        val buffer = tes.buffer

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL)

        val points = interpolateWire(start, end, weight)

        for (p in 0..points.size - 2) {
            drawLine(buffer, points[p], points[p + 1])
        }
        tes.draw()
    }

    fun interpolateWire(start: IVector3, end: IVector3, mass: Double): List<IVector3> {
        val list = mutableListOf<Vec3d>()
        val distance = start.distanceTo(end)
        val middle = Vec3d(
                (start.xd + end.xd) / 2,
                (start.yd + end.yd) / 2 - distance * mass,
                (start.zd + end.zd) / 2)

        for (i in 0..10) {
            val p = i / 10.0
            val x = interpolate(start.xd, middle.xd, end.xd, p)
            val y = interpolate(start.yd, middle.yd, end.yd, p)
            val z = interpolate(start.zd, middle.zd, end.zd, p)
            list.add(Vec3d(x, y, z))
        }
        return list
    }

    fun interpolate(fa: Double, fb: Double, fc: Double, x: Double): Double {
        val a = 0.0
        val b = 0.5
        val c = 1.0
        val L0 = (x - b) / (a - b) * ((x - c) / (a - c))
        val L1 = (x - a) / (b - a) * ((x - c) / (b - c))
        val L2 = (x - a) / (c - a) * ((x - b) / (c - b))
        return fa * L0 + fb * L1 + fc * L2
    }

    fun drawLine(t: BufferBuilder, a: Vec3d, b: Vec3d) {
        val w = 0.0625 / 2
        t.pos(a.xd, a.yd - w, a.zd).tex(0.0, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(a.xd, a.yd + w, a.zd).tex(0.125, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd + w, b.zd).tex(0.125, 1.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd - w, b.zd).tex(0.0, 1.0).normal(0f, 1f, 0f).endVertex()

        t.pos(a.xd, a.yd, a.zd - w).tex(0.0, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(a.xd, a.yd, a.zd + w).tex(0.125, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd, b.zd + w).tex(0.125, 1.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd, b.zd - w).tex(0.0, 1.0).normal(0f, 1f, 0f).endVertex()

        t.pos(a.xd - w, a.yd, a.zd).tex(0.0, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(a.xd + w, a.yd, a.zd).tex(0.125, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd + w, b.yd, b.zd).tex(0.125, 1.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd - w, b.yd, b.zd).tex(0.0, 1.0).normal(0f, 1f, 0f).endVertex()
        //inverted
        t.pos(a.xd, a.yd + w, a.zd).tex(0.0, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(a.xd, a.yd - w, a.zd).tex(0.125, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd - w, b.zd).tex(0.125, 1.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd + w, b.zd).tex(0.0, 1.0).normal(0f, 1f, 0f).endVertex()

        t.pos(a.xd, a.yd, a.zd + w).tex(0.0, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(a.xd, a.yd, a.zd - w).tex(0.125, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd, b.zd - w).tex(0.125, 1.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd, b.yd, b.zd + w).tex(0.0, 1.0).normal(0f, 1f, 0f).endVertex()

        t.pos(a.xd + w, a.yd, a.zd).tex(0.0, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(a.xd - w, a.yd, a.zd).tex(0.125, 0.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd - w, b.yd, b.zd).tex(0.125, 1.0).normal(0f, 1f, 0f).endVertex()
        t.pos(b.xd + w, b.yd, b.zd).tex(0.0, 1.0).normal(0f, 1f, 0f).endVertex()
    }

    fun rotateFromCenter(facing: EnumFacing, optional: Float = 0f) {
        val angle = when (facing) {
            EnumFacing.NORTH -> 0f
            EnumFacing.SOUTH -> 180f
            EnumFacing.WEST -> 90f
            EnumFacing.EAST -> -90f
            else -> 0f
        } + optional
        translate(0.5, 0.5, 0.5)
        rotate(0f, angle, 0f)
        translate(-0.5, -0.5, -0.5)
    }
}

val Number.rads: Float get() = Math.toRadians(this.toDouble()).toFloat()
val Float.px: Float get() = this * 1f / 16f
val Double.px: Double get() = this * 1.0 / 16.0
val Int.px: Float get() = this * 1f / 16f


