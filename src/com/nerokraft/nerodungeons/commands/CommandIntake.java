package com.nerokraft.nerodungeons.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.utils.Output;

import net.md_5.bungee.api.ChatColor;

public class CommandIntake implements CommandExecutor {
	private final NeroDungeons instance;
	private HashMap<CommandSender, String[]> senders = new HashMap<CommandSender, String[]>();

	public CommandIntake(NeroDungeons plugin) {
		instance = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0) {
			args[0] = args[0].toLowerCase();
			addSender(sender, args);
			switch (args[0]) {
			case "cs":
				new CommandCreateShop(this, sender);
				break;
			case "hologram":
				new CommandCreateHologram(this, sender);
				break;
			}
			return true;
		}
		return false;
	}

	public NeroDungeons getPlugin() {
		return instance;
	}

	public void addSender(CommandSender sender, String[] args) {
		senders.put(sender, args);
	}

	public String[] getArgs(Player p) {
		return senders.get(p);
	}

	public void clearPlayer(Player p) {
		senders.remove(p);
	}

	public void hintCommandSyntax(String string, CommandSender sender) {
		string = getPlugin().getMessages().getString("CommandSyntaxHint") + ": " + string;
		if (sender instanceof Player) {
			Output.sendMessage(string, ChatColor.RED, (Player) sender);
		} else {
			Bukkit.getLogger().warning(string);
		}
	}
}
