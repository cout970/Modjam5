package com.cout970.modjam.tile

import com.cout970.modjam.AABB
import com.cout970.modjam.BlockHolder
import com.cout970.modjam.block.BlockTrebuchet
import com.cout970.modjam.render.TrebuchetAnimation
import com.cout970.modjam.render.rotatePoint
import com.cout970.modjam.render.toAABBWith
import com.cout970.vector.extensions.plus
import com.cout970.vector.extensions.times
import com.cout970.vector.extensions.vec3Of
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

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

    val constructionItems = StackList()

    init {
        populateConstructionMaterials()
    }

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

    private fun doWork() {
        if (cooldown == 0) {
            cooldown = if (ready) {
                sendToClient(NBTTagCompound().apply { setInteger("event", 1) })
                30 // 1.5 sec
            } else {
                sendToClient(NBTTagCompound().apply { setInteger("event", 2) })
                100 // 5 sec
            }
            ready = !ready
        }
    }

    private fun build(playerIn: EntityPlayer) {

        if (playerIn.capabilities.isCreativeMode) {
            constructionItems.items.clear()
        } else {
            //...
        }

        if (constructionItems.items.isEmpty()) {
            val state = world.getBlockState(pos)
            world.setBlockState(pos, state.withProperty(BlockTrebuchet.PROPERTY_ACTIVE, true))
            placeBlocks()
        }
        sendUpdateToNearPlayers()
    }

    fun onClick(state: IBlockState, playerIn: EntityPlayer) {

        if (!world.isRemote) {
            if (state.getValue(BlockTrebuchet.PROPERTY_ACTIVE)) {
                doWork()
            } else {
                build(playerIn)
            }
        }
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setBoolean("ready", ready)
        compound.setTag("constructionItems", constructionItems.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        ready = compound.getBoolean("ready")
        constructionItems.deserializeNBT(compound.getCompoundTag("constructionItems"))
        super.readFromNBT(compound)
    }

    override fun getMaxRenderDistanceSquared(): Double = 128.0 * 128.0

    fun populateConstructionMaterials() {
        constructionItems.items.apply {
            add(ItemStack(Blocks.LOG, 32))
            add(ItemStack(Blocks.PLANKS, 64))
            add(ItemStack(Blocks.IRON_BLOCK, 2))
            add(ItemStack(Blocks.COBBLESTONE, 16))
            add(ItemStack(Items.IRON_INGOT, 16))
            add(ItemStack(Items.STRING, 32))
        }
    }

    fun placeBlocks() {
        val state = BlockHolder.gap.defaultState.withProperty(BlockTrebuchet.PROPERTY_ACTIVE, true)
        val facing = this.facing

        repeat(scheme.size) { y ->
            repeat(scheme[y].size) { z ->
                repeat(scheme[y][z].size) { x ->
                    if (scheme[y][z][x]) {
                        val realPos = applyFacing(BlockPos(x, y, z))
                        world.setBlockState(pos.add(realPos), state)
                        (world.getTileEntity(pos.add(realPos)) as? TileGap)?.let {
                            it.parentPos = pos
                            it.facing = facing
                            it.sendUpdateToNearPlayers()
                        }
                    }
                }
            }
        }
    }

    fun applyFacing(pos: BlockPos): BlockPos {
        val center = BlockPos(2, 0, 0)
        val origin = pos.subtract(center)
        val normalized = EnumFacing.SOUTH.rotatePoint(origin)

        return facing.rotatePoint(normalized)
    }

    fun onStructureBreak(pos: BlockPos) {
        repeat(scheme.size) { y ->
            repeat(scheme[y].size) { z ->
                repeat(scheme[y][z].size) { x ->
                    if (scheme[y][z][x]) {
                        val realPos = applyFacing(BlockPos(x, y, z))
                        val state = BlockHolder.gap.defaultState.withProperty(BlockTrebuchet.PROPERTY_ACTIVE, false)
                        world.setBlockState(pos.add(realPos), state)
                    }
                }
            }
        }
    }

    companion object {
        val scheme = run {
            val t = true
            val n = false
            listOf(
                    listOf(
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t)
                    ),
                    listOf(
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n)
                    ),
                    listOf(
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n)
                    ),
                    listOf(
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n)
                    ),
                    listOf(
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(t, t, n, t, t),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n)
                    ),
                    listOf(
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n)
                    ),
                    listOf(
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(t, t, t, t, t),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n),
                            booleanArrayOf(n, n, n, n, n)
                    )
            )
        }

        private const val PIXEL = 1 / 16f

        private infix fun Vec3d.to(other: Vec3d) = this toAABBWith other

        fun getGlobalCollisionBoxes(): List<AABB> = listOf(
                Vec3d(24.000, 0.000, -23.000) * PIXEL to Vec3d(36.000, 8.000, 85.000) * PIXEL,
                Vec3d(26.000, 48.000, -2.000) * PIXEL to Vec3d(34.000, 51.000, 80.000) * PIXEL,
                Vec3d(1.000, 12.500, -24.000) * PIXEL to Vec3d(15.000, 17.500, -19.000) * PIXEL,
                Vec3d(-18.000, 0.000, 24.000) * PIXEL to Vec3d(-10.000, 99.000, 32.000) * PIXEL,
                Vec3d(-17.000, 2.185, 25.685) * PIXEL to Vec3d(-11.000, 94.815, 118.315) * PIXEL,
                Vec3d(-17.000, 4.018, -22.098) * PIXEL to Vec3d(-11.000, 95.982, 32.098) * PIXEL,
                Vec3d(-23.000, 0.000, 176.000) * PIXEL to Vec3d(39.000, 3.000, 182.000) * PIXEL,
                Vec3d(15.000, 12.000, -24.500) * PIXEL to Vec3d(16.000, 18.000, -18.500) * PIXEL,
                Vec3d(-20.000, 0.000, -23.000) * PIXEL to Vec3d(-8.000, 8.000, 85.000) * PIXEL,
                Vec3d(-20.000, 0.000, 85.000) * PIXEL to Vec3d(-8.000, 8.000, 193.000) * PIXEL,
                Vec3d(24.000, 0.000, 85.000) * PIXEL to Vec3d(36.000, 8.000, 193.000) * PIXEL,
                Vec3d(-23.000, 0.000, 144.000) * PIXEL to Vec3d(39.000, 3.000, 150.000) * PIXEL,
                Vec3d(-23.000, 0.000, 112.000) * PIXEL to Vec3d(39.000, 3.000, 118.000) * PIXEL,
                Vec3d(-23.000, 0.000, 79.000) * PIXEL to Vec3d(39.000, 3.000, 85.000) * PIXEL,
                Vec3d(-23.000, 0.000, 48.000) * PIXEL to Vec3d(39.000, 3.000, 54.000) * PIXEL,
                Vec3d(-23.000, 0.000, -16.000) * PIXEL to Vec3d(39.000, 3.000, -10.000) * PIXEL,
                Vec3d(26.000, 0.000, 24.000) * PIXEL to Vec3d(34.000, 99.000, 32.000) * PIXEL,
                Vec3d(27.000, 2.185, 25.685) * PIXEL to Vec3d(33.000, 94.815, 118.315) * PIXEL,
                Vec3d(27.000, 4.018, -22.098) * PIXEL to Vec3d(33.000, 95.982, 32.098) * PIXEL,
                Vec3d(-18.000, 48.000, -2.000) * PIXEL to Vec3d(-10.000, 51.000, 80.000) * PIXEL,
                Vec3d(-18.000, 86.000, 18.000) * PIXEL to Vec3d(-10.000, 91.000, 43.000) * PIXEL,
                Vec3d(26.000, 86.000, 18.000) * PIXEL to Vec3d(34.000, 91.000, 43.000) * PIXEL,
                Vec3d(-19.000, 91.000, 25.000) * PIXEL to Vec3d(35.000, 97.000, 31.000) * PIXEL,
                Vec3d(-10.000, 80.000, 24.000) * PIXEL to Vec3d(-8.000, 98.000, 32.000) * PIXEL,
                Vec3d(24.000, 80.000, 24.000) * PIXEL to Vec3d(26.000, 98.000, 32.000) * PIXEL,
                Vec3d(-32.000, 0.000, 23.000) * PIXEL to Vec3d(48.000, 3.000, 33.000) * PIXEL,
                Vec3d(14.000, 4.000, -32.000) * PIXEL to Vec3d(16.000, 6.000, -4.000) * PIXEL,
                Vec3d(14.000, 4.000, -4.000) * PIXEL to Vec3d(16.000, 6.000, 24.000) * PIXEL,
                Vec3d(14.000, 4.000, 52.000) * PIXEL to Vec3d(16.000, 6.000, 80.000) * PIXEL,
                Vec3d(14.000, 4.000, 24.000) * PIXEL to Vec3d(16.000, 6.000, 52.000) * PIXEL,
                Vec3d(14.000, 4.000, 108.000) * PIXEL to Vec3d(16.000, 6.000, 136.000) * PIXEL,
                Vec3d(14.000, 4.000, 80.000) * PIXEL to Vec3d(16.000, 6.000, 108.000) * PIXEL,
                Vec3d(14.000, 4.000, 164.000) * PIXEL to Vec3d(16.000, 6.000, 192.000) * PIXEL,
                Vec3d(14.000, 4.000, 136.000) * PIXEL to Vec3d(16.000, 6.000, 164.000) * PIXEL,
                Vec3d(-0.000, 4.000, 164.000) * PIXEL to Vec3d(2.000, 6.000, 192.000) * PIXEL,
                Vec3d(-0.000, 4.000, 136.000) * PIXEL to Vec3d(2.000, 6.000, 164.000) * PIXEL,
                Vec3d(-0.000, 4.000, 108.000) * PIXEL to Vec3d(2.000, 6.000, 136.000) * PIXEL,
                Vec3d(-0.000, 4.000, 80.000) * PIXEL to Vec3d(2.000, 6.000, 108.000) * PIXEL,
                Vec3d(-0.000, 4.000, 52.000) * PIXEL to Vec3d(2.000, 6.000, 80.000) * PIXEL,
                Vec3d(-0.000, 4.000, 24.000) * PIXEL to Vec3d(2.000, 6.000, 52.000) * PIXEL,
                Vec3d(-0.000, 4.000, -4.000) * PIXEL to Vec3d(2.000, 6.000, 24.000) * PIXEL,
                Vec3d(-0.000, 4.000, -32.000) * PIXEL to Vec3d(2.000, 6.000, -4.000) * PIXEL,
                Vec3d(2.000, 4.000, -32.000) * PIXEL to Vec3d(14.000, 6.000, -30.000) * PIXEL,
                Vec3d(0.000, 0.000, -32.000) * PIXEL to Vec3d(16.000, 4.000, -16.000) * PIXEL,
                Vec3d(0.000, 0.000, -16.000) * PIXEL to Vec3d(16.000, 4.000, 0.000) * PIXEL,
                Vec3d(0.000, 0.000, 16.000) * PIXEL to Vec3d(16.000, 4.000, 32.000) * PIXEL,
                Vec3d(0.000, 0.000, 0.000) * PIXEL to Vec3d(16.000, 4.000, 16.000) * PIXEL,
                Vec3d(0.000, 0.000, 80.000) * PIXEL to Vec3d(16.000, 4.000, 96.000) * PIXEL,
                Vec3d(0.000, 0.000, 64.000) * PIXEL to Vec3d(16.000, 4.000, 80.000) * PIXEL,
                Vec3d(0.000, 0.000, 48.000) * PIXEL to Vec3d(16.000, 4.000, 64.000) * PIXEL,
                Vec3d(0.000, 0.000, 32.000) * PIXEL to Vec3d(16.000, 4.000, 48.000) * PIXEL,
                Vec3d(0.000, 0.000, 144.000) * PIXEL to Vec3d(16.000, 4.000, 160.000) * PIXEL,
                Vec3d(0.000, 0.000, 128.000) * PIXEL to Vec3d(16.000, 4.000, 144.000) * PIXEL,
                Vec3d(0.000, 0.000, 112.000) * PIXEL to Vec3d(16.000, 4.000, 128.000) * PIXEL,
                Vec3d(0.000, 0.000, 96.000) * PIXEL to Vec3d(16.000, 4.000, 112.000) * PIXEL,
                Vec3d(0.000, 0.000, 176.000) * PIXEL to Vec3d(16.000, 4.000, 192.000) * PIXEL,
                Vec3d(0.000, 0.000, 160.000) * PIXEL to Vec3d(16.000, 4.000, 176.000) * PIXEL,
                Vec3d(-27.631, 1.186, 25.000) * PIXEL to Vec3d(-14.369, 25.814, 31.000) * PIXEL,
                Vec3d(29.369, 1.186, 25.000) * PIXEL to Vec3d(42.631, 25.814, 31.000) * PIXEL,
                Vec3d(-31.927, 0.000, 29.643) * PIXEL to Vec3d(-14.073, 2.000, 65.357) * PIXEL,
                Vec3d(-31.927, -0.000, -9.357) * PIXEL to Vec3d(-14.073, 2.000, 26.357) * PIXEL,
                Vec3d(30.073, 0.000, -9.357) * PIXEL to Vec3d(47.927, 2.000, 26.357) * PIXEL,
                Vec3d(30.073, -0.000, 29.643) * PIXEL to Vec3d(47.927, 2.000, 65.357) * PIXEL,
                Vec3d(10.000, 4.000, -2.000) * PIXEL to Vec3d(12.000, 10.000, 0.000) * PIXEL,
                Vec3d(4.000, 4.000, -4.000) * PIXEL to Vec3d(12.000, 10.000, -2.000) * PIXEL,
                Vec3d(4.000, 4.000, -2.000) * PIXEL to Vec3d(6.000, 10.000, 0.000) * PIXEL,
                Vec3d(-8.000, 16.000, 72.000) * PIXEL to Vec3d(24.000, 24.000, 80.000) * PIXEL,
                Vec3d(-8.000, 0.000, 72.000) * PIXEL to Vec3d(-4.000, 16.000, 80.000) * PIXEL,
                Vec3d(20.000, 0.000, 72.000) * PIXEL to Vec3d(24.000, 16.000, 80.000) * PIXEL,
                Vec3d(-7.000, 2.192, 76.450) * PIXEL to Vec3d(-4.000, 23.808, 117.550) * PIXEL,
                Vec3d(20.000, 2.192, 76.450) * PIXEL to Vec3d(23.000, 23.808, 117.550) * PIXEL,
                Vec3d(-8.000, 0.000, 111.000) * PIXEL to Vec3d(0.000, 8.000, 119.000) * PIXEL,
                Vec3d(16.000, 0.000, 111.000) * PIXEL to Vec3d(24.000, 8.000, 119.000) * PIXEL,
                Vec3d(-18.000, 9.607, -26.893) * PIXEL to Vec3d(-10.000, 20.393, -16.107) * PIXEL,
                Vec3d(26.000, 9.607, -26.893) * PIXEL to Vec3d(34.000, 20.393, -16.107) * PIXEL,
                Vec3d(-20.000, 13.000, -23.500) * PIXEL to Vec3d(36.000, 17.000, -19.500) * PIXEL,
                Vec3d(-22.000, 11.000, -23.250) * PIXEL to Vec3d(-20.000, 19.000, -19.750) * PIXEL,
                Vec3d(-22.000, 10.934, -25.566) * PIXEL to Vec3d(-20.000, 19.066, -17.434) * PIXEL,
                Vec3d(-22.000, 13.250, -25.500) * PIXEL to Vec3d(-20.000, 16.750, -17.500) * PIXEL,
                Vec3d(-22.000, 10.934, -25.566) * PIXEL to Vec3d(-20.000, 19.066, -17.434) * PIXEL,
                Vec3d(-21.500, 9.697, -26.803) * PIXEL to Vec3d(-20.500, 16.061, -20.439) * PIXEL,
                Vec3d(-21.500, 14.500, -28.500) * PIXEL to Vec3d(-20.500, 15.500, -20.500) * PIXEL,
                Vec3d(-21.500, 13.939, -26.803) * PIXEL to Vec3d(-20.500, 20.303, -20.439) * PIXEL,
                Vec3d(-21.500, 14.000, -22.000) * PIXEL to Vec3d(-20.500, 22.000, -21.000) * PIXEL,
                Vec3d(-21.500, 13.939, -22.561) * PIXEL to Vec3d(-20.500, 20.303, -16.197) * PIXEL,
                Vec3d(-21.500, 14.500, -22.500) * PIXEL to Vec3d(-20.500, 15.500, -14.500) * PIXEL,
                Vec3d(-21.500, 9.697, -22.561) * PIXEL to Vec3d(-20.500, 16.061, -16.197) * PIXEL,
                Vec3d(-21.500, 8.000, -22.000) * PIXEL to Vec3d(-20.500, 16.000, -21.000) * PIXEL,
                Vec3d(0.000, 12.000, -24.500) * PIXEL to Vec3d(1.000, 18.000, -18.500) * PIXEL,
                Vec3d(36.000, 10.934, -25.566) * PIXEL to Vec3d(38.000, 19.066, -17.434) * PIXEL,
                Vec3d(36.000, 11.000, -23.250) * PIXEL to Vec3d(38.000, 19.000, -19.750) * PIXEL,
                Vec3d(36.000, 10.934, -25.566) * PIXEL to Vec3d(38.000, 19.066, -17.434) * PIXEL,
                Vec3d(36.000, 13.250, -25.500) * PIXEL to Vec3d(38.000, 16.750, -17.500) * PIXEL,
                Vec3d(36.500, 14.500, -28.500) * PIXEL to Vec3d(37.500, 15.500, -20.500) * PIXEL,
                Vec3d(36.500, 13.939, -26.803) * PIXEL to Vec3d(37.500, 20.303, -20.439) * PIXEL,
                Vec3d(36.500, 14.000, -22.000) * PIXEL to Vec3d(37.500, 22.000, -21.000) * PIXEL,
                Vec3d(36.500, 13.939, -22.561) * PIXEL to Vec3d(37.500, 20.303, -16.197) * PIXEL,
                Vec3d(36.500, 14.500, -22.500) * PIXEL to Vec3d(37.500, 15.500, -14.500) * PIXEL,
                Vec3d(36.500, 9.697, -22.561) * PIXEL to Vec3d(37.500, 16.061, -16.197) * PIXEL,
                Vec3d(36.500, 8.000, -22.000) * PIXEL to Vec3d(37.500, 16.000, -21.000) * PIXEL,
                Vec3d(36.500, 9.697, -26.803) * PIXEL to Vec3d(37.500, 16.061, -20.439) * PIXEL
        ).map { it.offset(vec3Of(0, 0, 2)) }
    }
}