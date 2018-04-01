package com.cout970.modjam.tile

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity

abstract class TileBase : TileEntity() {

    override fun getUpdatePacket(): SPacketUpdateTileEntity {
        return SPacketUpdateTileEntity(pos, 0, serializeNBT())
    }

    override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) {
        if (pkt.tileEntityType == 0) {
            deserializeNBT(pkt.nbtCompound)
        } else {
            receiveFromServer(pkt.nbtCompound)
        }
    }

    open fun receiveFromServer(data: NBTTagCompound) = Unit

    fun sendToClient(data: NBTTagCompound) {
        if (world.isRemote) return
        val packet = SPacketUpdateTileEntity(pos, 1, data)

        world.playerEntities
                .filterIsInstance<EntityPlayerMP>()
                .filter { getDistanceSq(it.posX, it.posY, it.posZ) <= (64 * 64) }
                .forEach { it.connection.sendPacket(packet) }
    }

    fun sendUpdateToNearPlayers() {
        if (world.isRemote) return
        val packet = updatePacket

        world.playerEntities
                .filterIsInstance<EntityPlayerMP>()
                .filter { getDistanceSq(it.posX, it.posY, it.posZ) <= (64 * 64) }
                .forEach { it.connection.sendPacket(packet) }
    }
}