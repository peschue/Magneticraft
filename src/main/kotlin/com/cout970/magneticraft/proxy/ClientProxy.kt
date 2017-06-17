package com.cout970.magneticraft.proxy


import com.cout970.magneticraft.MOD_ID
import com.cout970.magneticraft.block.core.BlockBase
import com.cout970.magneticraft.item.core.ItemBase
import com.cout970.magneticraft.registry.blocks
import com.cout970.magneticraft.registry.items
import com.cout970.magneticraft.registry.registerSounds
import com.cout970.magneticraft.tileentity.TileConveyorBelt
import com.cout970.magneticraft.tileentity.TileCrushingTable
import com.cout970.magneticraft.tileentity.core.TileBase
import com.cout970.magneticraft.tilerenderer.TileRendererConveyorBelt
import com.cout970.magneticraft.tilerenderer.TileRendererCrushingTable
import com.cout970.magneticraft.tilerenderer.core.TileRenderer
import com.cout970.magneticraft.util.toModel
import com.cout970.modelloader.api.DefaultBlockDecorator
import com.cout970.modelloader.api.ModelLoaderApi
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.obj.OBJLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

/**
 * This class extends the functionality of CommonProxy but adds
 * thing only for the client: sounds, models, textures and renders
 */
@Suppress("unused")
class ClientProxy : CommonProxy() {

    // List of registered TileEntityRenderers
    val tileRenderers = mutableListOf<TileRenderer<out TileBase>>()

    override fun preInit() {
        super.preInit()

        //Sounds
        registerSounds()

        //Item renders
        items.forEach { item ->
            (item as? ItemBase)?.let { item ->
                item.variants.forEach { variant ->
                    ModelLoader.setCustomModelResourceLocation(item, variant.key,
                            item.registryName!!.toModel(variant.value))
                }
            }
        }

        //ItemBlock renders
        blocks.forEach { (block, itemBlock) ->
            (block as? BlockBase)?.let {
                if (it.overrideItemModel) {
                    it.inventoryVariants.forEach {
                        ModelLoader.setCustomModelResourceLocation(itemBlock, it.key,
                                itemBlock.registryName!!.toModel(it.value))
                    }
                } else {
                    ModelLoader.setCustomModelResourceLocation(itemBlock, 0,
                            itemBlock.registryName!!.toModel("inventory"))
                }
                val mapper = it.getCustomStateMapper()
                if (mapper != null) {
                    ModelLoader.setCustomStateMapper(block, mapper)
                }
                it.customModels.forEach { (state, location) ->
                    ModelLoaderApi.registerModelWithDecorator(ModelResourceLocation(it.registryName, state),
                            location, DefaultBlockDecorator)
                }
            }
        }

        //Model loaders
        OBJLoader.INSTANCE.addDomain(MOD_ID)

        //TileEntity renderers
        register(TileCrushingTable::class.java, TileRendererCrushingTable)
        register(TileConveyorBelt::class.java, TileRendererConveyorBelt)

        //registering model bake event listener, for TESR (TileEntitySpecialRenderer) model reloading
        MinecraftForge.EVENT_BUS.register(this)
    }


    override fun init() {
        super.init()
    }

    override fun postInit() {
        super.postInit()
    }

    override fun getSide() = Side.CLIENT

    /**
     * Updates all the TileEntityRenderers to reload models
     */
    @Suppress("unused")
    @SubscribeEvent
    fun onModelRegistryReload(event: ModelBakeEvent) {
        tileRenderers.forEach { it.onModelRegistryReload() }
    }

    /**
     * Binds a TileEntity class with a TileEntityRenderer and
     * registers the TileEntityRenderer to update it when ModelBakeEvent is fired
     */
    private fun <T : TileBase> register(tileEntityClass: Class<T>, specialRenderer: TileRenderer<T>) {
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, specialRenderer)
        tileRenderers.add(specialRenderer)
    }
}