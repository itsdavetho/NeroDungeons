package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ShopCreate {
	private final Player player;
	private Material material = null;
	private final ShopInteract shopInteract;
	private final ItemFrame frame;
	
	public ShopCreate(Player player, ItemFrame frame, ShopInteract shopInteract) {
		this.player = player;
		this.shopInteract = shopInteract;
		this.frame = frame;
		selectItem();
	}
	
	public void selectItem() {
		player.sendMessage("Shop editing mode entered.");
		player.sendMessage("Place an item on the frame to proceed");
		player.sendMessage("Right click while sneaking to cancel");
	}
	
	public boolean waitingForItem() {
		return (material == null);
	}
	
	public void setItem(Material material) {
		this.material = material;
	}

	public void process(PlayerInteractEntityEvent event) {
		boolean creatingShop = shopInteract.isCreating(player);
		if (creatingShop) {
			if (waitingForItem()) {
				ItemStack stack = new ItemStack(player.getInventory().getItemInMainHand().getType(), 1);
				player.sendMessage("Set item to " + stack.getItemMeta().getDisplayName());
				setItem(stack.getType());
				frame.setItem(stack);
				TextComponent t = new TextComponent("Click here or type /nb cs [value] to set the value of your item");
				t.setColor(ChatColor.BLUE);
				t.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/nb cs "));
				player.spigot().sendMessage(t);
			} else {
				player.sendMessage("Waiting for further input...");
			}
		}
		event.setCancelled(true);
	}
}
