package com.nerokraft.nerodungeons;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NeroShop implements Listener {
	NeroDungeons instance = null;

	public NeroShop(NeroDungeons plugin) {
		instance = plugin;
	}

	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();
		if(entity instanceof ItemFrame) {
			Location l = entity.getLocation();
			final ItemFrame frame = (ItemFrame) entity;
	        final ItemStack item = frame.getItem();
		}
		
	}
}
