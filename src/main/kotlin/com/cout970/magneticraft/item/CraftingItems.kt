package com.cout970.magneticraft.item

import com.cout970.magneticraft.Magneticraft
import com.cout970.magneticraft.item.core.IItemMaker
import com.cout970.magneticraft.item.core.ItemBase
import com.cout970.magneticraft.item.core.ItemBuilder
import com.cout970.magneticraft.misc.CreativeTabMg
import com.cout970.magneticraft.misc.world.isClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult

/**
 * Created by cout970 on 2017/07/22.
 */
object CraftingItems : IItemMaker {

    lateinit var guideBook: ItemBase private set
    lateinit var crafting: ItemBase private set

    enum class Type(val meta: Int) {
        SULFUR(0),
        ALTERNATOR(1),
        MOTOR(2),
        COIL(3),
        MAGNET(4),
        MESH(5),
        STRING_FABRIC(6);

        fun stack(amount: Int = 1): ItemStack = ItemStack(crafting, amount, meta)
    }

    val meta = Type.values().map { it.name.toLowerCase() to it.meta }.toMap()

    override fun initItems(): List<Item> {
        val builder = ItemBuilder().apply {
            creativeTab = CreativeTabMg
        }

        crafting = builder.withName("crafting").copy {
            variants = meta.map { it.value to it.key }.toMap()
        }.build()

        guideBook = builder.withName("guide_book").copy {
            onItemRightClick = {
                if (it.playerIn.isSneaking || it.worldIn.isClient) {
                  // PS: let's try to activate that code, is this some kind of hidden reset to go to the TOC of the guide book?
                    it.default
                } else {
                    val pos = it.playerIn.position
                    it.playerIn.openGui(Magneticraft, -2, it.worldIn, pos.x, pos.y, pos.z)
                    ActionResult(EnumActionResult.SUCCESS, it.playerIn.getHeldItem(it.handIn))
                }
            }
        }.build()

        return listOf(crafting, guideBook)
    }
}
