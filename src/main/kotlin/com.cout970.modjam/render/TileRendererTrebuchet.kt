package com.cout970.modjam.render

import com.cout970.modjam.BlockHolder
import com.cout970.modjam.render.Util.customRotate
import com.cout970.modjam.render.Util.getGroup
import com.cout970.modjam.render.Util.getNonGroup
import com.cout970.modjam.render.Util.renderLine
import com.cout970.modjam.render.Util.stackMatrix
import com.cout970.modjam.render.Util.translate
import com.cout970.modjam.tile.TileTrebuchet
import com.cout970.vector.extensions.minus
import com.cout970.vector.extensions.plus
import com.cout970.vector.extensions.toDegrees
import net.minecraft.client.renderer.block.model.ModelResourceLocation

class TileRendererTrebuchet : BaseRenderer<TileTrebuchet>() {

    lateinit var base: List<ModelCache>
    lateinit var bar1: List<ModelCache>
    lateinit var wire1: List<ModelCache>
    lateinit var attach1: List<ModelCache>
    lateinit var rot1: List<ModelCache>
    lateinit var end1: List<ModelCache>
    lateinit var mass1: List<ModelCache>


    override fun renderTile(te: TileTrebuchet, partialTicks: Float) {

        val anim = te.animation
        if (te.world.totalWorldTime % (5 * 20) == 0L) {
            anim.startAnimation(te.world)
        }

        translate(0, 0, 2)

        base.renderTextured()
//        wire1.renderTextured()
//        rot1.renderTextured()
//        end1.renderTextured()

        anim.update(te.world, partialTicks)
        renderAnimation(anim)

        stackMatrix {
            val deg = anim.barKinematic.angles.toDegrees()
            customRotate(deg, anim.barKinematic.start)
            bar1.renderTextured()
            stackMatrix {
                val deg2 = anim.massKinematic.angles.toDegrees()
                customRotate(deg2, anim.massKinematic.start)
                mass1.renderTextured()
                attach1.renderTextured()
            }
        }
    }

    fun renderAnimation(animation: TrebuchetAnimation) {
        animation.apply {
            renderLine(barKinematic.start, barKinematic.end)
            renderLine(inverseBarKinematic.start, inverseBarKinematic.end)
            renderLine(inverseBarKinematic.end, inverseBarKinematic.end + massKinematic.end - massKinematic.start)
        }
    }

    fun reloadModel() {
        val loc = ModelResourceLocation(BlockHolder.trebuchet.registryName!!, "inventory")
        base = getNonGroup(loc)
        bar1 = getGroup(loc, "bar1")
        wire1 = getGroup(loc, "wire1")
        attach1 = getGroup(loc, "attach1")
        rot1 = getGroup(loc, "rot1")
        end1 = getGroup(loc, "end1")
        mass1 = getGroup(loc, "mass1")
    }
}