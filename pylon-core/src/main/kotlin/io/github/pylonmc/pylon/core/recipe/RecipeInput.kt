package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

sealed interface RecipeInput {
    data class Item(val items: MutableSet<ItemTypeWrapper>, val amount: Int) : RecipeInput {
        constructor(amount: Int, vararg items: ItemStack) : this(items.mapTo(mutableSetOf()) { ItemTypeWrapper(it) }, amount)
        constructor(tag: Tag<ItemTypeWrapper>, amount: Int) : this(tag.values, amount)

        init {
            require(amount > 0) { "Amount must be greater than zero, but was $amount" }
            require(items.isNotEmpty()) { "Items set must not be empty" }
        }

        val representativeItems: Set<ItemStack> by lazy {
            items.mapTo(mutableSetOf()) { it.createItemStack().asQuantity(amount) }
        }

        val representativeItem: ItemStack by lazy {
            representativeItems.first()
        }

        fun matches(itemStack: ItemStack): Boolean {
            if (itemStack.amount < amount) return false
            return contains(itemStack)
        }

        operator fun contains(itemStack: ItemStack): Boolean = ItemTypeWrapper(itemStack) in items
    }

    data class Fluid(val fluids: MutableSet<PylonFluid>, val amountMillibuckets: Double) : RecipeInput {
        constructor(amountMillibuckets: Double, vararg fluids: PylonFluid) : this(fluids.toMutableSet(), amountMillibuckets)
        constructor(amountMillibuckets: Double, tag: Tag<PylonFluid>) : this(tag.values, amountMillibuckets)

        init {
            require(amountMillibuckets > 0) { "Amount in millibuckets must be greater than zero, but was $amountMillibuckets" }
            require(fluids.isNotEmpty()) { "Fluids set must not be empty" }
        }

        fun matches(fluid: PylonFluid, amountMillibuckets: Double): Boolean {
            if (amountMillibuckets < this.amountMillibuckets) return false
            return contains(fluid)
        }

        operator fun contains(fluid: PylonFluid): Boolean = fluid in fluids
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(item: ItemStack, amount: Int = item.amount) = Item(amount, item)

        @JvmStatic
        fun of(fluid: PylonFluid, amountMillibuckets: Double) = Fluid(amountMillibuckets, fluid)
    }
}