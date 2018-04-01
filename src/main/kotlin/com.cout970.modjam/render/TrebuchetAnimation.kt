package com.cout970.modjam.render

import com.cout970.modjam.render.Util.expInterp
import com.cout970.modjam.render.Util.interp
import com.cout970.vector.extensions.vec3Of
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class TrebuchetAnimation {

    companion object {
        val throwTrack = AnimationTrack(listOf(
                1.5f * 20f to TrebuchetAnimation::throw1,
                2f * 20f to TrebuchetAnimation::throw2
        ))
        val realoadTrack = AnimationTrack(listOf(
                5f * 20f to TrebuchetAnimation::reload1
        ))
    }

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

    val wireKinematic = Kinematic(
            vec3Of(0.0),
            vec3Of(0.0, -0.5, -7.0),
            vec3Of(0.0, 0.0, 0.0)
    )

    val handleKinematic = Kinematic(
            vec3Of(0.5, 1.0 - 1.px, -1.0 - 5.5.px),
            vec3Of(0.0, 0.0, 1.0),
            vec3Of(0.0, 0.0, 0.0)
    )

    var returnWireKinematic = vec3Of(0.5, 1.0 - 1.px, 5.0)

    init {
        throw1(0f)
    }

    private var startTick = -1L
    var currentTrack = throwTrack

    val isActive: Boolean get() = startTick != -1L

    fun update(world: World, partialTicks: Float) {

        if (startTick == -1L) {
            return
        } else {
            val ticks = world.totalWorldTime - startTick
            if (ticks > currentTrack.totalDuration) {
                startTick = -1
                return
            } else {
                var globalTime = ticks + partialTicks

                for ((duration, func) in currentTrack.track) {
                    if (globalTime - duration <= 0) {
                        this.func(globalTime / duration)
                        return
                    } else {
                        globalTime -= duration
                    }
                }
            }
        }
    }

    fun startAnimation(world: World, isThrow: Boolean) {
        startTick = world.totalWorldTime
        currentTrack = if (isThrow) throwTrack else realoadTrack
    }

    fun throw1(time: Float) {

        barKinematic.withXrot(expInterp(27.rads, (-90).rads, 9.8f, time))
        massKinematic.withXrot(expInterp((-22).rads, (90).rads, 9.8f, time))
        wireKinematic.withXrot(expInterp((360).rads, 90.rads, 9.8f, time))
        inverseBarKinematic.angles = barKinematic.angles
        wireKinematic.start = barKinematic.end

        val y = min(max(wireKinematic.end.y.toFloat(), 5.5f.px), 15.px)
        val z = if (y >= 15.px) 5.0f else wireKinematic.end.z.toFloat()
        returnWireKinematic = vec3Of(0.5, y, z)
    }

    fun throw2(time: Float) {

        val center = -90f
        val maxDiff = 60f

        val angle = center - sin(time * 360.rads) * (maxDiff * (1 - time))

        barKinematic.withXrot(angle.rads)
        massKinematic.withXrot(-angle.rads)

        if (time < 0.25f) {
            val newTime = time * 4f
            wireKinematic.withXrot(interp(90f, -90f, newTime).rads) // 90 -> 0
        } else {
            val newTime = (time - 0.25f) * (4f / 3f)
            val newAngle = (-90) - sin(newTime * 360.rads) * (maxDiff * (1 - newTime))
            wireKinematic.withXrot(newAngle.rads)
        }


        inverseBarKinematic.angles = barKinematic.angles
        wireKinematic.start = barKinematic.end
    }

    fun reload1(time: Float) {
        handleKinematic.withXrot(time * 360.rads * 5)

        barKinematic.withXrot(interp(-90f, 27f, time).rads)
        massKinematic.withXrot(interp(90f, -22f, time).rads)

        if (time > 14.px) {
            val newTime = (time - 14.px) / 2.px
            returnWireKinematic = vec3Of(0.5, interp(15.px, 8.px, newTime), interp(5.0f, 3.9f, newTime))
        } else {
            returnWireKinematic = vec3Of(0.5, 15.px, 5.0f)
        }


        if (time < 0.5f) {
            val newTime = time * 2f
            wireKinematic.withXrot(interp(-90f, -45f - 22.5f, newTime).rads)
        } else {
            val newTime = (time - 0.5f) * 2f
            wireKinematic.withXrot(interp(-45f - 22.5f, 0f, newTime).rads)
        }
        inverseBarKinematic.angles = barKinematic.angles
        wireKinematic.start = barKinematic.end
    }

    data class AnimationTrack(val track: List<Pair<Float, TrebuchetAnimation.(Float) -> Unit>>) {
        val totalDuration = track.map { it.first }.sum()
    }
}