package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;

import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class ShopBuy {
	public static void buy(Player player, Shop shop) {
		int playerCurrency = player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).getScore();
		if (shop.getUUID().equals(player.getUniqueId())
				&& !Utils.hasPermission("nerodungeons.buy.own", player)) {
			Utils.sendMessage("You can't buy your own item!", ChatColor.RED, player);
			return;
		}
		if (!player.isSneaking()) {
			player.sendMessage("Shop here: " + shop.getOwner() + " is selling " + shop.getID() + " for "
					+ shop.getCost() + " reward points");
		} else {
			// buy
			int amount = 1 * shop.getAmount();
			int cost = amount * shop.getCost();
			if (playerCurrency >= shop.getCost()) {
				int difference = playerCurrency - cost;
				player.getInventory().addItem(new ItemStack(shop.getMaterial(), shop.getAmount()));
				player.getScoreboard().getObjective("rewardPoints").getScore(player.getName())
						.setScore(difference);
				player.sendMessage("Bought " + amount + "x" + shop.getID() + " for " + cost);
				Player owner = player.getServer().getPlayer(shop.getUUID());
				if(shop.getCurrency() == Currency.REWARD_POINTS) {
					Score score = owner.getScoreboard().getObjective("rewardPoints").getScore(owner.getName());
					int rewardPoints = score.getScore();
					score.setScore(rewardPoints + cost);
				} else {
					//... economy
				}
				if(owner.isOnline()) {
					owner.sendMessage(player.getName() + " bought " + amount + "x" + shop.getID() + " for " + cost + " " + (shop.getCurrency() == Currency.REWARD_POINTS ? "reward points" : "gold"));
				}
				

			} else {
				int difference = cost - playerCurrency;
				player.sendMessage("Sorry, you don't have enough points to buy that");
				player.sendMessage("You need " + difference + " more points");
			}
		}
	}
}
