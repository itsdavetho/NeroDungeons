package com.nerokraft.nerodungeons.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.utils.Utils;

public class CommandIntake implements CommandExecutor {
	final NeroDungeons instance;

	public CommandIntake(NeroDungeons plugin) {
		instance = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			Bukkit.getLogger().info("This command cannot be executed from the console. Sorry!");
			return false;
		}
		if(Utils.hasPermission("nerodungeons.admin", ((Player) sender).getPlayer())) {
			sender.sendMessage("Hello world!");
			return true;
		}
		if(args[0].equalsIgnoreCase("cs") || args[0].equalsIgnoreCase("createshop")) {
			
		}
		return false;
	}
	
}
