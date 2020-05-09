package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;

import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class ShopBuy {
	public static void buy(Player player, Shop shop) {
		if(shop == null) {
			System.out.println(shop);
			System.out.println("shop a null");
			return;
		}
		int playerCurrency = player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).getScore();
		if (shop.getUUID().equals(player.getUniqueId()) && !Utils.hasPermission("nerodungeons.buy.own", player)) {
			Utils.sendMessage("You can't buy your own item!", ChatColor.RED, player);
			return;
		}
		if (!player.isSneaking()) {
			int cost = (int) Math.ceil(shop.getAmount() * shop.getCost());
			String currencyName = shop.getCurrency() == Currency.REWARD_POINTS ? "reward points" : "gold";
			player.sendMessage("[" + shop.getOwner() + "] " + shop.getAmount() + "x" + shop.getID() + " for "
					+ cost + " " + currencyName);
		} else {
			// buy
			int amount = 1 * shop.getAmount();
			int cost = (int) Math.ceil(shop.getAmount() * shop.getCost());
			if(Utils.invSpace(player.getInventory(), shop.getMaterial()) < amount) {
				Utils.sendMessage("You should free up some inventory room before attempting to buy this!", ChatColor.DARK_RED, player);
				return;
			}
			String currencyName = (shop.getCurrency() == Currency.REWARD_POINTS ? "reward points" : "gold");
			if (playerCurrency >= shop.getCost()) {
				if (!shop.getAdminShop()) {
					Chest chest = (Chest) shop.getChest().getState();
					Inventory shopInv = chest.getInventory();
					ItemStack stack = new ItemStack(shop.getMaterial());
					if (!shopInv.containsAtLeast(stack, amount)) {
						Player owner = shop.getPlayer();
						if (owner.isOnline()) {
							Utils.sendMessage("You need to restock your " + shop.getID() + " shop",
									ChatColor.LIGHT_PURPLE, owner);
						}
						Utils.sendMessage("Sorry, but that shop is currently out of stock!", ChatColor.DARK_AQUA,
								player);
						return;
					} else {
						Utils.removeFromInventory(stack, amount, shopInv);
					}
				}
				int difference = playerCurrency - cost;
				player.getInventory().addItem(new ItemStack(shop.getMaterial(), shop.getAmount()));
				player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).setScore(difference);
				String msg = "Bought " + amount + "x" + shop.getID() + " for " + cost + " " + currencyName;
				player.sendMessage(msg);
				Player owner = shop.getPlayer();
				if (shop.getCurrency() == Currency.REWARD_POINTS) {
					Score score = owner.getScoreboard().getObjective("rewardPoints").getScore(owner.getName());
					int rewardPoints = score.getScore();
					score.setScore(rewardPoints + cost);
				} else {
					// ... economy
				}
				if (owner.isOnline()) {
					msg = player.getName() + " " + msg.toLowerCase();
					Utils.sendMessage(msg, ChatColor.GREEN, owner);
				}

			} else {
				int difference = cost - playerCurrency;
				player.sendMessage("Sorry, you don't have enough " + currencyName + " to buy that");
				player.sendMessage("You need " + difference + " more " + currencyName);
			}
		}
	}
}
