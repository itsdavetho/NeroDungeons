package com.nerokraft.nerodungeons.commands;

import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.events.shops.ShopCreate;
import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class CommandCreateShop {
	public CommandCreateShop(CommandIntake intake, Player player) {
		if (PlayerUtil.hasPermission("nerodungeons.createshop", player)) {
			String[] args = intake.getArgs(player);
			if (args.length > 2) {
				ShopCreate creator = intake.getPlugin().getShops().getShopInteractions().getCreator(player);
				if (creator == null) {
					Output.sendMessage("You are not editing any shops!", ChatColor.RED, player);
					return;
				}
				Currency currency = Currency.REWARD_POINTS;
				if (args.length > 3) {
					String name = args[3].toUpperCase();
					switch (name) {
					case "REWARDS":
						currency = Currency.REWARD_POINTS;
						break;
					case "GOLD":
					case "ECONOMY":
						currency = Currency.ECONOMY;
						break;
					default:
						player.sendMessage("Incorrect currency. Valid currencies: rewards, gold");
						return;
					}
				}
				if (creator != null && creator.waitingForChest() == false && creator.waitingForItem() == false) {
					int amount = Integer.parseInt(args[1]);
					double cost = Double.parseDouble(args[2]);
					if (amount <= 0) {
						Output.sendMessage("Amount must be more than zero", ChatColor.RED, player);
						return;
					}
					if (cost <= 0) {
						Output.sendMessage("Cost must be more than zero", ChatColor.RED, player);
						return;
					}
					creator.setAmount(amount);
					creator.setCost(cost);
					creator.setCurrency(currency);
					creator.insertShop();
					intake.getPlugin().getShops().saveShops(player);
				} else if (creator != null && creator.waitingForChest() == true) {
					Output.sendMessage("You still need to select a chest", ChatColor.BLUE, player);
				} else if(creator != null && creator.waitingForItem() == true) {
					Output.sendMessage("You still need to set an item in the frame!", ChatColor.RED, player);
				}
			} else {
				Output.sendMessage("Correct syntax: /nd cs [amount] [cost-each]", ChatColor.RED, player);
			}
		}
	}
}
