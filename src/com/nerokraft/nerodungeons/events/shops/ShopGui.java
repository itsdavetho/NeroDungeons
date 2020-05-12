package com.nerokraft.nerodungeons.events.shops;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.utils.Output;

import net.md_5.bungee.api.ChatColor;

public class ShopGui implements Listener {
	private Player player;
	private Inventory inv;
	private ItemFrame frame;
	
	public ShopGui(NeroDungeons instance, ItemFrame frame, Player player) {
		this.frame = frame;
		this.player = player;
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	public void show() { // we get the frame, as it holds all the shop item's metadata
		this.inv = Bukkit.createInventory(null, 27);
		ItemStack item = frame.getItem();
		ItemStack cancel = new ItemStack(Material.BARRIER, 1);
		ItemMeta cancelMeta = cancel.getItemMeta();
		cancelMeta.setDisplayName("Cancel");
		cancelMeta.setLore(Arrays.asList("Discontinue shopping here"));
		cancel.setItemMeta(cancelMeta);
		inv.setItem(0, cancel);
		inv.setItem(13, item);
		player.openInventory(inv);
	}
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		if(e.getInventory() == this.inv) {
			Output.sendMessage("you click", ChatColor.BLUE, player);
			e.setCancelled(true);
		}
	}
}
