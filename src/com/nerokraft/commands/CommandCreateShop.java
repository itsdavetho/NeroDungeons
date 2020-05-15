package com.nerokraft.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nerokraft.events.shops.ShopCreate;
import com.nerokraft.shops.Currencies;
import com.nerokraft.shops.Shop;
import com.nerokraft.utils.Output;
import com.nerokraft.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class CommandCreateShop {
	public CommandCreateShop(CommandIntake intake, CommandSender sender) {
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().info(intake.getPlugin().getMessages().getString("CommandNotConsole"));
			return;
		}
		Player player = (Player) sender;
		if (PlayerUtil.hasPermission("nerodungeons.createshop", player)) {
			String[] args = intake.getArgs(player);
			if (args.length > 2) {
				ShopCreate creator = intake.getPlugin().getShops().getShopInteractions().getCreator(player);
				if (creator == null) {
					Output.sendMessage(intake.getPlugin().getMessages().getString("ShopNotEditing"), ChatColor.RED, player);
					return;
				}
				Currencies currency = Currencies.REWARD_POINTS;
				if (args.length > 3) {
					String name = args[3].toUpperCase();
					switch (name) {
					case "REWARDS":
						currency = Currencies.REWARD_POINTS;
						break;
					case "GOLD":
					case "ECONOMY":
						currency = Currencies.ECONOMY;
						break;
					default:
						Output.sendMessage(intake.getPlugin().getMessages().getString("ShopInvalidCurrency"), ChatColor.RED, player);
						return;
					}
				}
				if (creator != null && creator.waitingForChest() == false && creator.waitingForItem() == false) {
					int amount = Integer.parseInt(args[1]);
					double cost = Double.parseDouble(args[2]);
					if (amount <= 0) {
						Output.sendMessage(intake.getPlugin().getMessages().getString("ShopAmountZero"), ChatColor.RED, player);
						return;
					}
					if (cost <= 0) {
						Output.sendMessage(intake.getPlugin().getMessages().getString("ShopCostZero"), ChatColor.RED, player);
						return;
					}
					creator.setAmount(amount);
					creator.setCost(cost);
					creator.setCurrency(currency);
					Shop shop = creator.insertShop();
					if(shop == null) {
						Output.sendMessage(intake.getPlugin().getMessages().getString("ShopCreateFail").replace("%s", creator.getMaterial().getMaxStackSize() + ""), ChatColor.RED, player);
						creator = null;
					}
				} else if (creator != null && creator.waitingForChest() == true) {
					Output.sendMessage(intake.getPlugin().getMessages().getString("ShopSelectChest"), ChatColor.BLUE, player);
				} else if(creator != null && creator.waitingForItem() == true) {
					Output.sendMessage(intake.getPlugin().getMessages().getString("ShopSelectItem"), ChatColor.RED, player);
				}
			} else {
				Output.sendMessage(intake.getPlugin().getMessages().getString("CommandSyntaxHint") + ": /nd cs [###] [$$$]", ChatColor.RED, player);
			}
		} else { 
			Output.sendMessage(intake.getPlugin().getMessages().getString("CommandLackPermission"), ChatColor.RED, player);
		}
	}
}
