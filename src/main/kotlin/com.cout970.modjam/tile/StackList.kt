package com.cout970.modjam.tile

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable

class StackList : INBTSerializable<NBTTagCompound> {
    val items = mutableListOf<ItemStack>()

    // return the elements removed
    fun removeFrom(stack: ItemStack): ItemStack {
        return ItemStack.EMPTY
    }

    fun add(stack: ItemStack) {
        items.add(stack)
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        val list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND)
        val temp = mutableListOf<ItemStack>()

        repeat(list.tagCount()) {
            temp.add(ItemStack(list.getCompoundTagAt(it)))
        }

        items.clear()
        items.addAll(temp)
    }

    override fun serializeNBT() = NBTTagCompound().apply {
        val list = NBTTagList()

        items.forEach { list.appendTag(it.serializeNBT()) }

        setTag("items", list)
    }
}