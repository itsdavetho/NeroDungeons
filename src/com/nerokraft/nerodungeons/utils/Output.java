package com.nerokraft.nerodungeons.utils;

import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.NeroDungeons;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Output {
	public static void sendMessage(String message, ChatColor color, Player player) {
		TextComponent t = new TextComponent("[NeroShop] " + message);
		t.setColor(color);
		player.spigot().sendMessage(t);
	}

	public static void verboseOutput(Player player, String message, NeroDungeons inst) {
		if (inst.getConfig().getInt("verboseoutput") > 0 && PlayerUtil.hasPermission("nerodungeons.verbose", player)) {
			player.sendMessage(message);
		}
	}

	public static void sendDebug(String message, ChatColor color, Player player) {
		if (PlayerUtil.hasPermission("nerodungeons.debug", player)) {
			sendMessage(message, color, player);
		}
	}

}
