package com.nerokraft.nerodungeons.events.shops;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
//import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Utils;

public class ShopInteract implements Listener {
	private final NeroDungeons instance;
	private HashMap<Player, ShopCreate> creators = new HashMap<Player, ShopCreate>();

	public ShopInteract(NeroDungeons inst) {
		instance = inst;
	}

	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame) {
			// Location l = entity.getLocation();
			final ItemFrame frame = (ItemFrame) entity;
			final ItemStack item = frame.getItem();
			Block b = player.getWorld().getBlockAt(frame.getLocation());
			if (instance.getConfig().getInt("verboseoutput") > 0
					&& (Utils.hasPermission("nerodungeons.verbose", player))) {
				player.sendMessage(b.getBlockData().getMaterial().name());
			}
			if (instance.getShops().getShop(b) == null) { // there's no shop here, we can proceed to do other things
				if (instance.getConfig().getInt("verboseoutput") > 0
						&& (Utils.hasPermission("nerodungeons.verbose", player))) {
					player.sendMessage("Fuck! no shop " + item.getType().name());
				}
				if (!isCreating(player)
						&& player.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD)) {
					addShopCreator(player, frame);
					event.setCancelled(true);
				} else if (isCreating(player)) {
					if (player.isSneaking()) {
						removeShopCreator(player);
						event.setCancelled(true);
					} else {
						getCreator(player).process(event);
					}
				}
			} else {
				Shop shop = instance.getShops().getShop(b);
				player.sendMessage("Shop here: " + shop.getOwner() + " is selling " + shop.getMaterial().name()
						+ " for " + shop.getCost() + " reward points");
				player.getInventory().addItem(new ItemStack(shop.getMaterial(), shop.getAmount()));
			}
		}
	}

	public void addShopCreator(Player player, ItemFrame frame) {
		if (isCreating(player)) {
			Bukkit.getLogger().warning("This probably should not have happened");
			return;
		}
		ShopCreate creator = new ShopCreate(player, frame, this);
		creators.put(player, creator);
	}

	public void removeShopCreator(Player player) {
		creators.remove(player);
	}

	public boolean isCreating(Player player) {
		return creators.containsKey(player);
	}

	public ShopCreate getCreator(Player player) {
		return isCreating(player) ? creators.get(player) : null;
	}
}
