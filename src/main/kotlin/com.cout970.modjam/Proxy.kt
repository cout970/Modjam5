package com.cout970.modjam

import com.cout970.modelloader.api.DefaultBlockDecorator
import com.cout970.modelloader.api.ModelLoaderApi
import com.cout970.modjam.render.TileRendererTrebuchet
import com.cout970.modjam.tile.TileTrebuchet
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
import net.minecraftforge.registries.IForgeRegistry


open class CommonProxy {

    open fun preinit() {
        MinecraftForge.EVENT_BUS.register(this)
        TileEntity.register("$MOD_ID:trebuchet", TileTrebuchet::class.java)
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.registerBlock(BlockHolder.trebuchet, "trebuchet", "trebuchet")
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.registerItemBlock(BlockHolder.trebuchet)
    }

    private fun IForgeRegistry<Block>.registerBlock(instance: Block, name: String, model: String) {
        instance.registryName = ResourceLocation("$MOD_ID:$name")

        ModelLoaderApi.registerModelWithDecorator(
                ModelResourceLocation(instance.registryName!!, "inventory"),
                ResourceLocation(MOD_ID, "models/$model.mcx"), DefaultBlockDecorator
        )
        register(instance)
    }

    private fun IForgeRegistry<Item>.registerItemBlock(instance: Block) {
        register(ItemBlock(instance).apply { registryName = instance.registryName })
    }

    open fun init() = Unit
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