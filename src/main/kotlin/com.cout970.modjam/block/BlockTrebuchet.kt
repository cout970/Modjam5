package com.cout970.modjam.block

import com.cout970.modjam.tile.TileTrebuchet
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockTrebuchet : BlockBase(Material.WOOD) {

    companion object {
        val PROPERTY_FACING = PropertyEnum.create("facing", EnumFacing::class.java, EnumFacing.HORIZONTALS.toList())
        val PROPERTY_ACTIVE = PropertyBool.create("active")
    }

    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, PROPERTY_FACING, PROPERTY_ACTIVE)
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        val active = (meta and 1) == 1
        val facing = EnumFacing.getHorizontal(meta ushr 1)

        return defaultState.withProperty(PROPERTY_ACTIVE, active).withProperty(PROPERTY_FACING, facing)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        val active = state.getValue(PROPERTY_ACTIVE)
        val facing = state.getValue(PROPERTY_FACING)

        return (if (active) 1 else 0) or (facing.horizontalIndex)
    }

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) {
        worldIn.setBlockState(pos, state.withProperty(PROPERTY_ACTIVE, false).withProperty(PROPERTY_FACING, placer.horizontalFacing))
    }

    override fun createNewTileEntity(worldIn: World?, meta: Int) = TileTrebuchet()

    override fun onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        val tile = worldIn.getTileEntity(pos) as? TileTrebuchet ?: return false
        tile.onClick(playerIn)
        return true
    }

    override fun isFullBlock(state: IBlockState?): Boolean = false
    override fun isOpaqueCube(state: IBlockState?) = false
    override fun isFullCube(state: IBlockState?) = false
}