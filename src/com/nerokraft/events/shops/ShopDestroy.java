package com.nerokraft.events.shops;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.shops.Shop;
import com.nerokraft.utils.Output;

import net.md_5.bungee.api.ChatColor;

public class ShopDestroy {
	public static boolean destroy(Shop shop) {
		if(shop == null) {
			return false;
		}
		Entity[] es = shop.getWorld().getChunkAt(shop.getBlock()).getEntities();
		for(Entity e : es) {
			if(e instanceof ItemFrame && e.getLocation().equals(shop.getFrameLocation())) {
				final ItemFrame frame = (ItemFrame) e;
				shop.getShops().setShopMeta(frame, 0l);
				((ItemFrame) e).setItem(new ItemStack(Material.AIR));
				break;
			}
		}
		shop.getShops().removeShop(shop);
		if(shop.getPlayer() != null) {
			Output.sendMessage("Your shop was destroyed!", ChatColor.YELLOW, shop.getPlayer());
		}
		return true;
	}

}
