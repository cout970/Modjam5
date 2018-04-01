package com.cout970.modjam.tile

import com.cout970.modjam.block.BlockTrebuchet
import com.cout970.modjam.render.TrebuchetAnimation
import com.cout970.vector.extensions.plus
import com.cout970.vector.extensions.vec3Of
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB

class TileTrebuchet : TileBase(), ITickable {

    val facing: EnumFacing
        get() {
            val state = world.getBlockState(pos)
            if (state.propertyKeys.contains(BlockTrebuchet.PROPERTY_FACING))
                return state.getValue(BlockTrebuchet.PROPERTY_FACING)
            return EnumFacing.NORTH
        }

    val active: Boolean
        get() {
            val state = world.getBlockState(pos)
            if (state.propertyKeys.contains(BlockTrebuchet.PROPERTY_ACTIVE))
                return state.getValue(BlockTrebuchet.PROPERTY_ACTIVE)
            return false
        }

    val animation = TrebuchetAnimation()
    private var ready = true
    private var cooldown = 0

    override fun getRenderBoundingBox(): AxisAlignedBB {
        return INFINITE_EXTENT_AABB
    }

    override fun receiveFromServer(data: NBTTagCompound) {
        val event = data.getInteger("event")
        if (event == 1) {
            animation.startAnimation(world, true)
        } else if (event == 2) {
            animation.startAnimation(world, false)
        }
    }

    override fun update() {
        if (cooldown > 0) {
            cooldown--
            if (!ready && cooldown == 0) {
                fire()
            }
        }
    }

    private fun fire() {
        val spawn = vec3Of(pos.x, pos.y, pos.z) + vec3Of(facing.directionVec.x * 3, 25, facing.directionVec.z * 3)
        val tnt = EntityTNTPrimed(world)
        tnt.setPosition(spawn.x, spawn.y, spawn.z)
        tnt.motionX = facing.frontOffsetX * -6.0
        tnt.motionZ = facing.frontOffsetZ * -6.0
        tnt.fuse = 120

        world.spawnEntity(tnt)
    }

    fun onClick(playerIn: EntityPlayer) {

        if (!world.isRemote) {
            if (cooldown == 0) {
                if (ready) {
                    sendToClient(NBTTagCompound().apply { setInteger("event", 1) })
                    cooldown = 30 // 1.5 sec
                } else {
                    sendToClient(NBTTagCompound().apply { setInteger("event", 2) })
                    cooldown = 100 // 5 sec
                }
                ready = !ready
            }
        }
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setBoolean("ready", ready)
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        ready = compound.getBoolean("ready")
        super.readFromNBT(compound)
    }

    override fun getMaxRenderDistanceSquared(): Double = 128.0 * 128.0
}