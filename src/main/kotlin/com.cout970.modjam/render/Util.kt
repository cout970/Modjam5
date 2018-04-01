package com.cout970.modjam.render

import com.cout970.modjam.AABB
import com.cout970.modjam.tile.TileTrebuchet
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
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

    fun renderFloatingLabel(str: String, pos: Vec3d) {
        val x = pos.x
        val y = pos.y
        val z = pos.z
        val renderManager = Minecraft.getMinecraft().renderManager
        val fontrenderer = renderManager.fontRenderer
        val f = 1.6f
        val f1 = 0.016666668f * f
        pushMatrix()
        translate(x.toFloat() + 0.0f, y.toFloat() + 0.5f, z.toFloat())
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)
        rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        scale(-f1, -f1, f1)
        disableLighting()
        depthMask(false)
        disableDepth()
        enableBlend()
        tryBlendFuncSeparate(770, 771, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.buffer
        val i = 0

        val j = fontrenderer.getStringWidth(str) / 2
        disableTexture2D()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos((-j - 1).toDouble(), (-1 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((-j - 1).toDouble(), (8 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((j + 1).toDouble(), (8 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((j + 1).toDouble(), (-1 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        tessellator.draw()
        enableTexture2D()
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127)
        enableDepth()
        depthMask(true)
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1)
        enableLighting()
        disableBlend()
        color(1.0f, 1.0f, 1.0f, 1.0f)
        popMatrix()
    }

    fun renderMultiblockHitboxes(facing: EnumFacing) {
        TileTrebuchet.getGlobalCollisionBoxes().map {
            val origin = EnumFacing.SOUTH.rotateBox(vec3Of(0.5), it)
            facing.rotateBox(vec3Of(0.5), origin)
        }.forEach { renderBox(it) }
    }

    fun renderBox(box: AxisAlignedBB, color: IVector3 = vec3Of(1, 1, 1)) {
        val tes = Tessellator.getInstance()
        val t = tes.buffer
        val r = color.xf
        val g = color.yf
        val b = color.zf
        val a = 1f

//        glDisable(GL_TEXTURE_2D)
        bindTexture(0)
        GlStateManager.glLineWidth(2f)
        t.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        t.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex()
        t.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex()

        t.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex()
        t.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex()

        t.pos(box.minX, box.minY, box.minZ).color(r, g, b, a).endVertex()
        t.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex()

        t.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex()
        t.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex()

        t.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex()
        t.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex()

        t.pos(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).endVertex()
        t.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex()

        t.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex()
        t.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex()

        t.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex()
        t.pos(box.maxX, box.maxY, box.minZ).color(r, g, b, a).endVertex()

        t.pos(box.minX, box.maxY, box.minZ).color(r, g, b, a).endVertex()
        t.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex()

        t.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex()
        t.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex()

        t.pos(box.minX, box.maxY, box.maxZ).color(r, g, b, a).endVertex()
        t.pos(box.minX, box.minY, box.maxZ).color(r, g, b, a).endVertex()

        t.pos(box.maxX, box.minY, box.maxZ).color(r, g, b, a).endVertex()
        t.pos(box.maxX, box.minY, box.minZ).color(r, g, b, a).endVertex()

        tes.draw()
//        glEnable(GL_TEXTURE_2D)
    }

}

val Number.rads: Float get() = Math.toRadians(this.toDouble()).toFloat()
val Float.px: Float get() = this * 1f / 16f
val Double.px: Double get() = this * 1.0 / 16.0
val Int.px: Float get() = this * 1f / 16f


fun EnumFacing.rotatePoint(point: BlockPos): BlockPos {
    val rel = point
    val rot = when (this) {
        EnumFacing.DOWN -> return BlockPos(BlockPos(Vec3d(rel).rotatePitch(-90.0f)))
        EnumFacing.UP -> return BlockPos(BlockPos(Vec3d(rel).rotatePitch(90.0f)))
        EnumFacing.NORTH -> return point
        EnumFacing.SOUTH -> 180.0f
        EnumFacing.WEST -> 90.0f
        EnumFacing.EAST -> 360f - 90.0f
    }
    val pos2 = Vec3d(rel).rotateYaw(rot.rads)
    val pos3 = pos2.transform { Math.round(it).toDouble() }
    return BlockPos(pos3)
}

fun EnumFacing.rotatePoint(origin: Vec3d, point: Vec3d): Vec3d {
    val rel = point - origin
    val rot = when (this) {
        EnumFacing.DOWN -> return origin + rel.rotatePitch(90.rads)
        EnumFacing.UP -> return origin + rel.rotatePitch((-90).rads)
        EnumFacing.NORTH -> return point
        EnumFacing.SOUTH -> 180.0f
        EnumFacing.WEST -> 90.0f
        EnumFacing.EAST -> -90.0f
    }
    return origin + rel.rotateYaw(rot.rads)
}


fun EnumFacing.rotateBox(origin: Vec3d, box: AABB): AABB {
    val min = Vec3d(box.minX, box.minY, box.minZ)
    val max = Vec3d(box.maxX, box.maxY, box.maxZ)
    return rotatePoint(origin, min) toAABBWith rotatePoint(origin, max)
}

infix fun BlockPos.toAABBWith(other: BlockPos) = AxisAlignedBB(this, other)
infix fun Vec3d.toAABBWith(other: Vec3d) = AxisAlignedBB(this, other)

fun AxisAlignedBB.cut(other: AxisAlignedBB): AxisAlignedBB? {
    if (!this.intersects(other)) return null
    return AxisAlignedBB(
            Math.max(minX, other.minX), Math.max(minY, other.minY), Math.max(minZ, other.minZ),
            Math.min(maxX, other.maxX), Math.min(maxY, other.maxY), Math.min(maxZ, other.maxZ))
}

fun <T : Comparable<T>> IBlockState.value(prop: IProperty<T>): T? = if (prop in propertyKeys) getValue(prop) else null