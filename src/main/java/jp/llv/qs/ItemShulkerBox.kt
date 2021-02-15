package jp.llv.qs

import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

/**
 * Created by toyblocks on 2017/01/09.
 */
class ItemShulkerBox(val shulker: ItemStack) : InventoryHolder {
    private val inv = ((shulker.itemMeta as BlockStateMeta).blockState as ShulkerBox).inventory
    override fun getInventory(): Inventory = inv
}