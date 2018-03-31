package com.cout970.modjam

import com.cout970.modelloader.api.DefaultBlockDecorator
import com.cout970.modelloader.api.ModelLoaderApi
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


open class CommonProxy {


    open fun preinit() {
        MinecraftForge.EVENT_BUS.register(this)
        TileEntity.register("$MOD_ID:trebuchet", TileTrebuchet::class.java)
    }


    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        val block = BlockTrebuchet().apply {
            registryName = ResourceLocation("$MOD_ID:trebuchet")
        }
        ModelLoaderApi.registerModelWithDecorator(
                ModelResourceLocation(block.registryName!!, "inventory"),
                ResourceLocation(MOD_ID, "models/trebuchet.mcx"), DefaultBlockDecorator
        )
        event.registry.register(block)
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        val itemblock = ItemBlock(BlockHolder.trebuchet).apply {
            registryName = BlockHolder.trebuchet!!.registryName
        }
        event.registry.register(itemblock)
    }

    open fun init() {

    }
}

class ClientProxy : CommonProxy() {

    override fun preinit() {
        super.preinit()
        ClientRegistry.bindTileEntitySpecialRenderer(TileTrebuchet::class.java, TileRendererTrebuchet)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @SubscribeEvent
    fun onModelRegistryReload(event: ModelBakeEvent) {
        TileRendererTrebuchet.reloadModel()
    }
}