package com.cout970.modjam.block

import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs

abstract class BlockBase(material: Material) : BlockContainer(material) {

    init {
        setCreativeTab(CreativeTabs.REDSTONE)
    }
}