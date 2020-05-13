package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Economics;
import com.nerokraft.nerodungeons.utils.Items;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class ShopBuy {
	public static void buy(Player customer, Shop shop, ItemFrame frame, Economics economy) {
		buy(customer, shop, frame, economy, 1);
	}

	public static boolean buy(Player customer, Shop shop, ItemFrame frame, Economics economy, int quantity) {
		if (shop == null) {
			return false;
		}
		ShopGui gui = shop.getShops().getShopInteractions().inGui(customer);
		if (gui != null) {
			Location shopLocation = frame.getLocation();
			Location fwdOfShop = PlayerUtil.nudgeForward(0.23, frame, shopLocation);
			double totalCost = (shop.getCost() * quantity);
			int totalAmount = (shop.getAmount() * quantity);
			if (!PlayerUtil.hasPermission("nerodungeons.buy", customer)) {
				Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNoPermission"),
						ChatColor.YELLOW, customer);
				shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
				customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
				return false;
			}
			String currencyName = shop.getCurrency() == Currency.REWARD_POINTS ? "reward points" : "gold";
			String msg = totalAmount + "x" + shop.getName() + " (" + totalCost + " " + currencyName + ")";
			if (shop.getUUID().equals(customer.getUniqueId())
					&& !PlayerUtil.hasPermission("nerodungeons.buy.own", customer)) {
				Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopBuyOwn"), ChatColor.RED,
						customer);
				shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
				customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
				return false;
			}
			if (Items.invSpace(customer.getInventory(), shop.getMaterial()) < totalAmount) {
				Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedRoom"), ChatColor.RED,
						customer);
				shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
				customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
				return false;
			}
			double rewardPoints = customer.getScoreboard().getObjective("rewardPoints").getScore(customer.getName())
					.getScore();
			double customerWallet = shop.getCurrency() == Currency.ECONOMY ? economy.balance(customer) : rewardPoints;
			Location aboveHead = customer.getLocation(); // above head particles
			aboveHead.setY(aboveHead.getY() + 2.5f);
			if ((shop.getCost() * quantity) > customerWallet
					&& !PlayerUtil.hasPermission("nerodungeons.nomoney", customer)) {
				customer.getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 50, 0.25, 0.25, 0.25);
				customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				double difference = (shop.getCost() * quantity) - customerWallet;
				Output.sendMessage(
						shop.getShops().getPlugin().getMessages().getString("ShopNeedMoney")
								.replace("%d", "" + difference).replace("%s", currencyName),
						ChatColor.RED, customer);
				return false;
			} else {
				Player owner = shop.getPlayer();
				boolean sold = PlayerUtil.hasPermission("nerodungeons.nomoney", customer);
				if (sold == false) {
					if (shop.getCurrency() == Currency.REWARD_POINTS) {
						sold = economy.modifyRewards(customer, -totalCost);
						if (sold && !shop.getAdminShop()) {
							economy.modifyRewards(owner, totalCost);
						}
					} else {
						sold = economy.withdraw(customer, totalCost);
						if (sold && !shop.getAdminShop()) {
							economy.deposit(owner, totalCost);
						}
					}
				}
				if (!shop.getAdminShop()) {
					Chest chest = (Chest) shop.getChest().getState();
					Inventory shopInv = chest.getInventory();
					ItemStack stack = new ItemStack(shop.getMaterial());
					if (!shopInv.containsAtLeast(stack, totalAmount)) {
						if (owner.isOnline()) {
							Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedStock")
									.replace("%s", shop.getName()), ChatColor.LIGHT_PURPLE, owner);
						}
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNoStock"),
								ChatColor.DARK_AQUA, customer);
						shopLocation.getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 50, 0.25, 0.25, 0.25);
						customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
						return false;
					} else {
						Items.removeFromInventory(stack, (totalAmount), shopInv);
					}
				}
				if (sold) {
					boolean buySelf = customer.getUniqueId().equals(owner.getUniqueId());
					if (!buySelf) {
						Output.sendMessage(
								shop.getShops().getPlugin().getMessages().getString("ShopBought") + " " + msg,
								ChatColor.BLUE, customer);
					}
					if (owner.isOnline()) {
						Output.sendMessage(customer.getName() + " "
								+ shop.getShops().getPlugin().getMessages().getString("ShopBought").toLowerCase() + " "
								+ msg, ChatColor.GREEN, owner);
					}
					if (shop.getMaterial().getMaxStackSize() >= totalAmount) {
						ItemStack stack = new ItemStack(shop.getMaterial(), (totalAmount));
						stack.setItemMeta(frame.getItem().getItemMeta());
						customer.getInventory().addItem(stack);
					} else {
						ItemStack stack = new ItemStack(shop.getMaterial(), shop.getMaterial().getMaxStackSize());
						stack.setItemMeta(frame.getItem().getItemMeta());
						int stacksNeeded = (int) Math.floor(totalAmount / shop.getMaterial().getMaxStackSize());
						int remainder = totalAmount - (stacksNeeded * shop.getMaterial().getMaxStackSize());
						for (int i = 0; i < stacksNeeded; i++) {
							customer.getInventory().addItem(stack);
						}
						if (remainder > 0) {
							stack.setAmount(remainder);
							customer.getInventory().addItem(stack);
						}
					}
					shopLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, aboveHead, 20, 1, 1, 1);
					customer.getWorld().playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
				}
			}

			return true;
		}
		return false;
	}
}
