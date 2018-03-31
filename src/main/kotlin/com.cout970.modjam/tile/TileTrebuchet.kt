package com.cout970.modjam.tile

import com.cout970.modjam.render.TrebuchetAnimation
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB

class TileTrebuchet : TileEntity() {

    val animation = TrebuchetAnimation()

    override fun getRenderBoundingBox(): AxisAlignedBB {
        return INFINITE_EXTENT_AABB
    }
}