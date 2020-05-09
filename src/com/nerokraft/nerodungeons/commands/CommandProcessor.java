package com.nerokraft.nerodungeons.commands;

import org.bukkit.entity.Player;

public class CommandProcessor {
	private Player player;
	
	public CommandProcessor(Player player) {
		this.setPlayer(player);
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return this.player;
	}
}
