package com.cout970.modjam

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger
import kotlin.system.measureTimeMillis

const val MOD_ID = "trebuchet"
const val MOD_NAME = "Trebuchet"
const val MOD_VERSION = "0.0.0"

lateinit var LOGGER: Logger

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION, modLanguage = "kotlin")
class Trebuchet {

    companion object {
        @SidedProxy(serverSide = "com.cout970.modjam.CommonProxy", clientSide = "com.cout970.modjam.ClientProxy")
        @JvmStatic
        lateinit var proxy: CommonProxy
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        LOGGER = event.modLog

        LOGGER.info("[$MOD_NAME] starting preinit")
        val time = measureTimeMillis {
            proxy.preinit()
        }

        LOGGER.info("[$MOD_NAME] preinit done in $time miliseconds")
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {

        LOGGER.info("[$MOD_NAME] starting init")
        val time = measureTimeMillis {
            proxy.init()
        }

        LOGGER.info("[$MOD_NAME] init done in $time miliseconds")
    }
}