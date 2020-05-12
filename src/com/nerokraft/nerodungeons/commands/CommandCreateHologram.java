package com.nerokraft.nerodungeons.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.holograms.Hologram;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class CommandCreateHologram {

	public CommandCreateHologram(CommandIntake intake, CommandSender sender) {
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().info(intake.getPlugin().getMessages().getString("CommandNotConsole"));
		} else {
			Player player = (Player) sender;
			if (PlayerUtil.hasPermission("nerodungeon.createhologram", player)) {
				if (intake.getArgs(player).length > 1) {
					String text = "";
					String[] args = intake.getArgs(player);
					for (int i = 1; i < args.length; i++) {
						if (args[i] != null) {
							text = text + " " + args[i];
						}
					}
					try {
						Location l = player.getLocation();
						l.setY(l.getY() + 0.5);
						Hologram h = intake.getPlugin().getHolograms().createHologram(l, text);
						h.show(player);
					} catch (Exception e) {
						Output.sendDebug("===Hologram failure===", ChatColor.GREEN, player);
						Output.sendDebug(e.getMessage(), ChatColor.RED, player);
						e.printStackTrace();
					}
				} else {
					intake.hintCommandSyntax("nd hologram <text>", sender);
				}
			} else {
				Output.sendMessage(intake.getPlugin().getMessages().getString("CommandLackPermission"), ChatColor.RED,
						player);
			}
		}
	}

}
