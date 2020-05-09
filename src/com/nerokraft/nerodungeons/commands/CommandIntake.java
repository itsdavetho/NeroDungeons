package com.nerokraft.nerodungeons.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.NeroDungeons;

public class CommandIntake implements CommandExecutor {
	final NeroDungeons instance;
	private HashMap<Player, String[]> players = new HashMap<Player, String[]>();

	public CommandIntake(NeroDungeons plugin) {
		instance = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().info("This command cannot be executed from the console. Sorry!");
			return false;
		}
		addPlayer((Player) sender, args);
		if (args[0].equalsIgnoreCase("cs") || args[0].equalsIgnoreCase("createshop")) {
			new CommandCreateShop(this, (Player) sender);
		}
		return false;
	}

	public NeroDungeons getPlugin () {
		return instance;
	}
	
	public void addPlayer(Player p, String[] args) {
		players.put(p, args);
	}
	
	public String[] getArgs(Player p) {
		return players.get(p);
	}

	public void clearPlayer(Player p) {
		players.remove(p);
	}
}
