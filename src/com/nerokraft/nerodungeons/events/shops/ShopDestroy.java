package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class ShopDestroy {

	public static boolean destroy(Player player, Shop shop, NeroDungeons instance) {
		if(shop != null && !shop.getUUID().equals(player.getUniqueId()) && !Utils.hasPermission("nerodungeons.admin", player)) {
			Utils.sendMessage("You can't destroy that", ChatColor.RED, player);
			return false;
		} else if(shop != null) { 			
			instance.getShops().removeShop(shop);
			Utils.sendMessage("Shop removed", ChatColor.GREEN, player);
			return true;
		}
		return true;
	}
	
	public static void destroy(Shop shop, NeroDungeons instance) {
		if(shop == null) {
			return;
		}
		instance.getShops().removeShop(shop);
		if(shop.getPlayer().isOnline()) {
			Utils.sendMessage("Your shop was destroyed!", ChatColor.YELLOW, shop.getPlayer());
		}
	}

}
