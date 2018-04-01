package com.cout970.modjam.tile

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class TileGap : TileBase() {

    var parentPos: BlockPos = BlockPos.ORIGIN
    var facing: EnumFacing = EnumFacing.NORTH

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setInteger("parentX", parentPos.x)
        compound.setInteger("parentY", parentPos.y)
        compound.setInteger("parentZ", parentPos.z)
        compound.setInteger("facing", facing.horizontalIndex)
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        val x = compound.getInteger("parentX")
        val y = compound.getInteger("parentY")
        val z = compound.getInteger("parentZ")
        parentPos = BlockPos(x, y, z)
        facing = EnumFacing.getHorizontal(compound.getInteger("facing"))
        super.readFromNBT(compound)
    }
}