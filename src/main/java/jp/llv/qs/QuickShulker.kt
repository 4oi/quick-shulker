package jp.llv.qs

import org.bukkit.ChatColor
import org.bukkit.block.ShulkerBox
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by toyblocks on 2017/01/09.
 */
class QuickShulker : JavaPlugin(), Listener {

    companion object {
        fun cloneInventory(from: Inventory, to: Inventory) {
            from.contents.forEachIndexed { i, itemStack -> to.setItem(i, itemStack) }
        }

        fun getBlockStateMeta(item: ItemStack?) = item?.itemMeta as? BlockStateMeta

        fun getShulkerBlockState(item: ItemStack?) = getBlockStateMeta(item)?.blockState as? ShulkerBox

        fun getShulkerInventory(item: ItemStack?) = getShulkerBlockState(item)?.inventory

        fun isShulkerBoxItem(item: ItemStack?) = getShulkerInventory(item) !== null
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player || !sender.hasPermission("quickshulker")) {
            sender.sendMessage("${ChatColor.RED}You cannot execute this command")
            return true
        }
        if (!openShulker(sender)) {
            sender.sendMessage("${ChatColor.RED}You don't have a shulker box in your hand")
        }
        return true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        if ((event.inventory.holder is ItemShulkerBox && isShulkerBoxItem(event.currentItem))
                || (event.click == ClickType.NUMBER_KEY && isShulkerBoxItem(event.whoClicked.inventory.contents[event.hotbarButton]))) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryDrag(event: InventoryDragEvent) {
        if (event.inventory.holder is ItemShulkerBox && isShulkerBoxItem(event.oldCursor)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? ItemShulkerBox ?: return
        val state = getShulkerBlockState(holder.shulker)!!
        cloneInventory(event.inventory ?: throw IllegalStateException(), state.inventory)
        val meta = getBlockStateMeta(holder.shulker)!!
        meta.blockState = state
        holder.shulker.itemMeta = meta
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (!player.hasPermission("quickshulker")) {
            return
        }
        if (openShulker(player)) {
            event.isCancelled = true
        }
    }

    fun openShulker(player: Player): Boolean {
        val item = when {
            isShulkerBoxItem(player.inventory.itemInMainHand) -> player.inventory.itemInMainHand
            isShulkerBoxItem(player.inventory.itemInOffHand) -> player.inventory.itemInOffHand
            else -> {
                return false
            }
        }
        val holder = ItemShulkerBox(item)
        val inventory = server.createInventory(holder, InventoryType.CHEST, "")
        cloneInventory(getShulkerInventory(item)!!, inventory)
        player.openInventory(inventory)
        return true
    }

}