package com.nerokraft.shops;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ShopOwner {
	private Player owner = null;
	private OfflinePlayer offlineOwner = null;
	
	public ShopOwner(UUID uuid) {
		Player owner = Bukkit.getServer().getPlayer(uuid);
		if(owner != null && owner.isOnline()) {
			this.owner = owner;
		} else {
			offlineOwner = Bukkit.getOfflinePlayer(uuid);
		}
	}
	
	public String getName() {
		String ownerName = "nobody";
		if(getPlayer() == null && getOfflinePlayer() != null) {
			ownerName = getOfflinePlayer().getName();
		} else if(getPlayer() != null){
			ownerName = getPlayer().getName();
		}
		return ownerName;
	}

	public Player getPlayer() {
		return owner;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return offlineOwner;
	}
}
