package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.NeroDungeons;

public class ShopInteract implements Listener {
	@SuppressWarnings("unused")
	private final NeroDungeons instance;
	public ShopInteract(NeroDungeons inst) {
		instance = inst;
	}
	
	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();
		if(entity instanceof ItemFrame) {
			@SuppressWarnings("unused")
			Location l = entity.getLocation();
			final ItemFrame frame = (ItemFrame) entity;
	        final ItemStack item = frame.getItem();
	       // if(isShop(l)) {
	        	player.sendMessage("Fuck! " + item.getType().name());
	        //}
		}
		
	}
}
