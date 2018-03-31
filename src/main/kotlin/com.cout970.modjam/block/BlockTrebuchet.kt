package com.cout970.modjam.block

import com.cout970.modjam.tile.TileTrebuchet
import net.minecraft.block.material.Material
import net.minecraft.world.World

class BlockTrebuchet : BlockBase(Material.WOOD) {

    override fun createNewTileEntity(worldIn: World?, meta: Int) = TileTrebuchet()
}