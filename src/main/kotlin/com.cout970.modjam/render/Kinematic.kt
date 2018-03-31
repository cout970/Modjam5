package com.cout970.modjam.render

import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*

data class Kinematic(var start: IVector3, var direction: IVector3, var angles: IVector3) {

    val end: IVector3
        get() = start.add(direction.rotateX(angles.x).rotateY(angles.y).rotateZ(angles.z))

    fun withXrot(x: Float) {
        angles = vec3Of(x, angles.yf, angles.zf)
    }
}