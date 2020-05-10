package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Output;

import net.md_5.bungee.api.ChatColor;

public class ShopDestroy {
	public static boolean destroy(Shop shop) {
		if(shop == null) {
			return false;
		}
		Entity[] es = shop.getWorld().getChunkAt(shop.getBlock()).getEntities();
		for(Entity e : es) {
			if(e instanceof ItemFrame && e.getLocation().equals(shop.getFrameLocation())) {
				((ItemFrame) e).setItem(new ItemStack(Material.AIR));
				break;
			}
		}
		shop.getShops().removeShop(shop);
		if(shop.getPlayer().isOnline()) {
			Output.sendMessage("Your shop was destroyed!", ChatColor.YELLOW, shop.getPlayer());
		}
		return true;
	}

}
