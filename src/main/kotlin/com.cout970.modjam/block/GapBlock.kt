package com.cout970.modjam.block

import com.cout970.modjam.block.BlockTrebuchet.Companion.PROPERTY_ACTIVE
import com.cout970.modjam.tile.TileGap
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.world.World

class GapBlock : BlockBase(Material.WOOD) {

    override fun createNewTileEntity(worldIn: World?, meta: Int) = TileGap()


    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, PROPERTY_ACTIVE)
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        val active = (meta and 1) == 1

        return defaultState.withProperty(PROPERTY_ACTIVE, active)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        val active = state.getValue(PROPERTY_ACTIVE)
        return (if (active) 1 else 0)
    }
}