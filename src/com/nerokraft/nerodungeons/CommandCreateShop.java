package com.nerokraft.nerodungeons;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		if(instance.hasPermission("nerodungeons.createshop", ((Player) sender).getPlayer()) || sender.hasPermission("nerodungeons.createshop")) {
			sender.sendMessage("Hello world!");
			return true;
		}
		return false;
	}
	
}
