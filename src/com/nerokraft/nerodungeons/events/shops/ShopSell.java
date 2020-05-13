package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.shops.Currencies;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Economics;
import com.nerokraft.nerodungeons.utils.Items;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class ShopSell {
	public static boolean sell(Player customer, Shop shop, ItemFrame frame, Economics economy, int quantity) {
		if (frame == null || frame.getItem().getType() == Material.AIR) {
			shop.getShops().removeShop(shop);
			return false;
		}
		String payment = shop.getShops().getPlugin().getEconomy().currencyToString(shop.getCurrency());
		double ownerWallet = shop.getShops().getPlugin().getEconomy().balance(customer, shop.getCurrency());
		int totalAmount = quantity * shop.getAmount();
		double totalPayed = Math
				.floor((shop.getCost() * shop.getShops().getPlugin().getEconomy().getValueDecay()) * quantity);
		Material material = frame.getItem().getType();
		boolean sold = PlayerUtil.hasPermission("nerodungeons.nomoney", shop.getPlayer()) || shop.getAdminShop();
		boolean canSellSelf = shop.getPlayer().getUniqueId().equals(customer.getUniqueId())
				&& PlayerUtil.hasPermission("nerodungeons.buy.own", customer);
		Location aboveHead = customer.getLocation(); // above head particles
		aboveHead.setY(aboveHead.getY() + 2.5f);
		if (((totalPayed + economy.getWalletBuffer(shop.getCurrency())) <= ownerWallet || shop.getAdminShop())
				&& PlayerUtil.hasPermission("nerodungeons.sell", customer)) {
			Inventory playerInventory = customer.getInventory();
			ItemStack stack = frame.getItem();
			boolean inInventory = playerInventory.containsAtLeast(stack, totalAmount);
			if (inInventory) {
				if (sold == false && !customer.getUniqueId().equals(shop.getPlayer().getUniqueId())) {
					sold = shop.getCurrency() == Currencies.REWARD_POINTS
							? economy.modifyRewards(shop.getPlayer(), -totalPayed)
							: economy.withdraw(shop.getPlayer(), totalPayed);
				}
				if (sold) {
					if (!customer.getUniqueId().equals(shop.getPlayer().getUniqueId())) {
						if (shop.getCurrency() == Currencies.REWARD_POINTS) {
							economy.modifyRewards(customer, totalPayed);
						} else {
							economy.deposit(customer, totalPayed);
						}
					}
					Items.removeFromInventory(stack, totalAmount, playerInventory);
					Chest chest = null;
					if (!shop.getAdminShop()) {
						chest = (Chest) shop.getChest().getState();
					}
					if (chest != null && !shop.getAdminShop()
							&& Items.invSpace(chest.getInventory(), material) >= totalAmount) {
						if (shop.getMaterial().getMaxStackSize() >= totalAmount) {
							stack.setAmount(totalAmount);
							chest.getInventory().addItem(stack);
						} else {
							stack.setAmount(stack.getMaxStackSize());
							int stacksNeeded = (int) Math.floor(totalAmount / shop.getMaterial().getMaxStackSize());
							int remainder = totalAmount - (stacksNeeded * shop.getMaterial().getMaxStackSize());
							for (int i = 0; i < stacksNeeded; i++) {
								chest.getInventory().addItem(stack);
							}
							if (remainder > 0) {
								stack.setAmount(remainder);
								chest.getInventory().addItem(stack);
							}
						}
					}
					if (shop.getPlayer().getUniqueId().equals(customer.getUniqueId()) == false) {
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSoldPlayer")
								.replace("%s", shop.getOwnerName()).replace("%a", "" + totalAmount)
								.replace("%n", shop.getName()).replace("%p", "" + totalPayed).replace("%t", payment),
								ChatColor.BLUE, customer);
					}
					if (shop.getPlayer().isOnline()) {
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSoldShop")
								.replace("%s", customer.getName()).replace("%a", "" + totalAmount)
								.replace("%n", shop.getName()).replace("%p", "" + totalPayed).replace("%t", payment),
								ChatColor.GREEN, shop.getPlayer());
					}
					shop.getFrameLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, aboveHead, 20, 1, 1, 1);
					customer.getWorld().playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
				} else {
					double difference = Math.floor(totalPayed - ownerWallet);
					Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedMoney")
							.replace("%s",
									shop.getShops().getPlugin().getEconomy().currencyToString(shop.getCurrency()))
							.replace("%d", "" + difference), ChatColor.RED, customer);
				}
			} else if (!inInventory) {
				Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNoItem"),
						ChatColor.LIGHT_PURPLE, customer);
			}
		} else if (canSellSelf) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSellSelf"), ChatColor.GRAY,
					customer);
		} else if (!PlayerUtil.hasPermission("nerodungeons.sell", customer)) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("CommandLackPermission"),
					ChatColor.RED, customer);
		}
		return false;
	}
}
