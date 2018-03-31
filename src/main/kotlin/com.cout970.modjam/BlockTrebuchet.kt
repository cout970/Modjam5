package com.cout970.modjam

import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.world.World

class BlockTrebuchet : BlockContainer(Material.WOOD) {

    init {
        setCreativeTab(CreativeTabs.REDSTONE)
    }

    override fun createNewTileEntity(worldIn: World?, meta: Int) = TileTrebuchet()
}