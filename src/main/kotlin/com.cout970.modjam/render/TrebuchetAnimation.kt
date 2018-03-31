package com.cout970.modjam.render

import com.cout970.vector.extensions.vec3Of
import net.minecraft.world.World

class TrebuchetAnimation {

    val barKinematic = Kinematic(
            vec3Of(0.5, 6.0 - 2.px, 1.5 + 4.px),
            vec3Of(0.0, 0.0, 10.5 + 2.px),
            vec3Of(27.rads, 0.0, 0.0)
    )

    val inverseBarKinematic = Kinematic(
            vec3Of(0.5, 6.0 - 2.px, 1.5 + 4.px),
            vec3Of(0.0, 0.0, -3.125),
            vec3Of(27.rads, 0.0, 0.0)
    )

    val massKinematic = Kinematic(
            vec3Of(0.5, 6.0 - 2.px, -1.5 + 2.px),
            vec3Of(0.0, -1.0, 0.0),
            vec3Of(0.0, 0.0, 0.0)
    )

    private var startTick = -1L

    fun update(world: World, partialTicks: Float) {
        val totalTime = 1.5f * 20f
        val time = if (startTick == -1L) 0f else {
            val ticks = world.totalWorldTime - startTick
            if (ticks > totalTime) {
                startTick = -1
                0f
            } else {
                ticks + partialTicks
            }
        }

//        barKinematic.withXrot(cycleLinear(27.rads, (-120).rads, time / totalTime))
        barKinematic.withXrot(Util.expInterp(27.rads, (-120).rads, 9.8f, time / totalTime))
        inverseBarKinematic.withXrot(barKinematic.angles.x.toFloat())
        massKinematic.withXrot(Util.expInterp((-20).rads, (120).rads, 9.8f, time / totalTime))
    }

    fun startAnimation(world: World) {
        startTick = world.totalWorldTime
    }
}