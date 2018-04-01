package com.cout970.modjam.block

import com.cout970.modjam.AABB
import com.cout970.modjam.block.BlockTrebuchet.Companion.PROPERTY_ACTIVE
import com.cout970.modjam.render.cut
import com.cout970.modjam.render.rotateBox
import com.cout970.modjam.render.value
import com.cout970.modjam.tile.TileBase
import com.cout970.modjam.tile.TileGap
import com.cout970.modjam.tile.TileTrebuchet
import com.cout970.vector.extensions.vec3Of
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class BlockGap : BlockBase(Material.WOOD) {

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

    override fun isFullBlock(state: IBlockState?): Boolean = false
    override fun isOpaqueCube(state: IBlockState?) = false
    override fun isFullCube(state: IBlockState?) = false

    companion object {

        fun getBoxes(facing: EnumFacing): List<AABB> {

            return (TileTrebuchet.getGlobalCollisionBoxes()).map {
                val origin = EnumFacing.SOUTH.rotateBox(vec3Of(0.5), it)
                facing.rotateBox(vec3Of(0.5), origin)
            }
        }

        fun getBoxesInBlock(relPos: BlockPos, facing: EnumFacing): List<AABB> {
            val boxes = getBoxes(facing)
            val thisBox = FULL_BLOCK_AABB.offset(relPos)
            return boxes.mapNotNull { it.cut(thisBox) }
        }

        fun getRelativeBoxesInBlock(pos: BlockPos, module: TileGap): List<AABB> {
            val relPos = module.parentPos.subtract(pos)
            return getBoxesInBlock(BlockPos.ORIGIN.subtract(relPos), module.facing).map {
                it.offset(relPos)
            }
        }
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun addCollisionBoxToList(state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB,
                                       collidingBoxes: MutableList<AxisAlignedBB>, entityIn: Entity?,
                                       isActualState: Boolean) {
        val active = state.value(PROPERTY_ACTIVE)
        if (active != null && active) {
            val tile = worldIn.getTileEntity(pos) as? TileGap ?: return

            val boxes = getRelativeBoxesInBlock(pos, tile).map { it.offset(pos) }
            boxes.filterTo(collidingBoxes) { entityBox.intersects(it) }
            return
        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState)
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun getSelectedBoundingBox(state: IBlockState, worldIn: World, pos: BlockPos): AxisAlignedBB {
        val active = state.value(PROPERTY_ACTIVE)
        if (active != null && active) {
            val tile = worldIn.getTileEntity(pos) as TileBase
            val module = tile as TileGap

            val player = Minecraft.getMinecraft().player

            val start = player.getPositionEyes(0f)
            val look = player.getLook(0f)
            val blockReachDistance = Minecraft.getMinecraft().playerController!!.blockReachDistance
            val end = start.addVector(
                    look.x * blockReachDistance,
                    look.y * blockReachDistance,
                    look.z * blockReachDistance
            )

            val res = getRelativeBoxesInBlock(pos, module)
                    .associate { it to rayTrace(pos, start, end, it) }
                    .filter { it.value != null }
                    .map { it.key to it.value }
                    .sortedBy { it.second!!.hitVec.distanceTo(start) }
                    .firstOrNull()?.first

            return res?.offset(pos) ?: Block.FULL_BLOCK_AABB
        }
        return super.getSelectedBoundingBox(state, worldIn, pos)
    }

    @Suppress("OverridingDeprecatedMember")
    override fun collisionRayTrace(blockState: IBlockState, worldIn: World, pos: BlockPos, start: Vec3d,
                                   end: Vec3d): RayTraceResult? {
        val active = blockState.value(PROPERTY_ACTIVE)
        if (active != null && active) {
            val tile = worldIn.getTileEntity(pos) as TileBase
            val module = tile as TileGap

            return getRelativeBoxesInBlock(pos, module)
                    .associate { it to rayTrace(pos, start, end, it) }
                    .filter { it.value != null }
                    .map { it.key to it.value }
                    .sortedBy { it.second!!.hitVec.distanceTo(start) }
                    .firstOrNull()?.second
        }
        return this.rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos))
    }

    //removedByPlayer

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun getRenderType(state: IBlockState): EnumBlockRenderType {
        val active = state.value(PROPERTY_ACTIVE)
        return if (active != null && active) EnumBlockRenderType.INVISIBLE else super.getRenderType(state)
    }

    override fun breakBlock(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (!worldIn.isRemote) {
            val active = state.value(PROPERTY_ACTIVE)
            if (active != null && active) {
                val tile = worldIn.getTileEntity(pos) as TileBase
                val module = tile as TileGap

                val parent = worldIn.getTileEntity(module.parentPos)
                if (parent is TileTrebuchet) {
                    parent.onStructureBreak(pos)
                }
            }
        }
        super.breakBlock(worldIn, pos, state)
    }
}