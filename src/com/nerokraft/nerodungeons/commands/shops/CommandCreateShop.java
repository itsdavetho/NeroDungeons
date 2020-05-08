package com.nerokraft.nerodungeons.commands.shops;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.utils.Utils;

public class CommandCreateShop implements CommandExecutor {
	final NeroDungeons instance;

	public CommandCreateShop(NeroDungeons plugin) {
		instance = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			Bukkit.getLogger().info("This command cannot be executed from the console. Sorry!");
			return false;
		}
		if(Utils.hasPermission("nerodungeons.admin", ((Player) sender).getPlayer()) || sender.hasPermission("nerodungeons.admin")) {
			sender.sendMessage("Hello world!");
			return true;
		}
		return false;
	}
	
}
