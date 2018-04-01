package com.cout970.modjam.render

import com.cout970.modjam.BlockHolder
import com.cout970.modjam.MOD_ID
import com.cout970.modjam.render.Util.customRotate
import com.cout970.modjam.render.Util.drawWireBetween
import com.cout970.modjam.render.Util.getGroup
import com.cout970.modjam.render.Util.getNonGroup
import com.cout970.modjam.render.Util.renderLine
import com.cout970.modjam.render.Util.rotateFromCenter
import com.cout970.modjam.render.Util.stackMatrix
import com.cout970.modjam.render.Util.translate
import com.cout970.modjam.tile.TileTrebuchet
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.minus
import com.cout970.vector.extensions.plus
import com.cout970.vector.extensions.toDegrees
import com.cout970.vector.extensions.vec3Of
import net.minecraft.client.renderer.GlStateManager.disableBlend
import net.minecraft.client.renderer.GlStateManager.enableBlend
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14
import kotlin.math.max

class TileRendererTrebuchet : BaseRenderer<TileTrebuchet>() {

    lateinit var base: List<ModelCache>
    lateinit var bar1: List<ModelCache>
    lateinit var attach1: List<ModelCache>
    lateinit var rot1: List<ModelCache>
    lateinit var end1: List<ModelCache>
    lateinit var mass1: List<ModelCache>

    override fun renderTile(te: TileTrebuchet, partialTicks: Float) {

        if (!te.active) {
            te.constructionItems.items.forEachIndexed { index, itemStack ->
                val txt = "${itemStack.count} x ${itemStack.displayName}"
                Util.renderFloatingLabel(txt, vec3Of(0.5, 1 + index * 6.px, 0.5))
            }

            rotateFromCenter(te.facing, 180f)
            translate(0, 0, 2)
            enableBlend()
            GL14.glBlendColor(1f, 1f, 1f, 0.5f)
            glBlendFunc(GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA)
            base.renderTextured()
            bar1.renderTextured()
            attach1.renderTextured()
            rot1.renderTextured()
            end1.renderTextured()
            mass1.renderTextured()
            disableBlend()

            return
        }

        rotateFromCenter(te.facing, 180f)
        translate(0, 0, 2)

        val anim = te.animation

        anim.update(te.world, partialTicks)

        //debug
//        renderAnimation(anim)

        base.renderTextured()

        stackMatrix {
            customRotate(anim.barKinematic.angles.toDegrees(), anim.barKinematic.start)
            bar1.renderTextured()
            stackMatrix {
                customRotate(anim.massKinematic.angles.toDegrees(), anim.massKinematic.start)
                mass1.renderTextured()
                attach1.renderTextured()
            }
        }

        stackMatrix {
            translate(x = -0.5, y = -0.5f)
            translate(collideBottom(anim.wireKinematic.end, 0.5))
            customRotate(anim.wireKinematic.angles.toDegrees(), vec3Of(0.5f, 0.5f, 0f))
            end1.renderTextured()
        }

        stackMatrix {
            customRotate(anim.handleKinematic.angles.toDegrees(), anim.handleKinematic.start)
            rot1.renderTextured()
        }

        // Wire render

        val offset = vec3Of(4.px, 0, 0)
        val pos1 = anim.wireKinematic.end + offset
        val pos2 = anim.wireKinematic.end - offset

        bindTexture(ResourceLocation("$MOD_ID:textures/blocks/wire.png"))
        drawWireBetween(anim.wireKinematic.start + offset, collideBottom(pos1, 0.5), 0.05)
        drawWireBetween(anim.wireKinematic.start - offset, collideBottom(pos2, 0.5), 0.05)
        drawWireBetween(anim.handleKinematic.start, anim.returnWireKinematic, 0.0)
        drawWireBetween(collideBottom(anim.wireKinematic.end, 5.5.px), anim.returnWireKinematic, 0.005)
    }

    fun collideBottom(pos: IVector3, bottom: Double): IVector3 = vec3Of(pos.x, max(bottom, pos.y), pos.z)

    fun renderAnimation(animation: TrebuchetAnimation) {
        animation.apply {
            renderLine(barKinematic.start, barKinematic.end)
            renderLine(inverseBarKinematic.start, inverseBarKinematic.end)
            renderLine(wireKinematic.start, wireKinematic.end)
            renderLine(handleKinematic.start, handleKinematic.end)
            renderLine(inverseBarKinematic.end, inverseBarKinematic.end + massKinematic.end - massKinematic.start)
        }
    }

    fun reloadModel() {
        val loc = ModelResourceLocation(BlockHolder.trebuchet.registryName!!, "inventory")
        base = getNonGroup(loc)
        bar1 = getGroup(loc, "bar1")
        attach1 = getGroup(loc, "attach1")
        rot1 = getGroup(loc, "rot1")
        end1 = getGroup(loc, "end1")
        mass1 = getGroup(loc, "mass1")
    }
}