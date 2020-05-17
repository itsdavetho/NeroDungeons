package com.nerokraft.events.shops;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.shops.Currencies;
import com.nerokraft.shops.Shop;
import com.nerokraft.shops.ShopOwner;
import com.nerokraft.utils.Economics;
import com.nerokraft.utils.Items;
import com.nerokraft.utils.Output;
import com.nerokraft.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class ShopBuy {
	public static void buy(Player customer, Shop shop, ItemFrame frame, Economics economy) {
		buy(customer, shop, frame, economy, 1);
	}

	public static boolean buy(Player customer, Shop shop, ItemFrame frame, Economics economy, int quantity) {
		if (shop == null) {
			return false;
		}
		if (frame == null || frame.getItem().getType() == Material.AIR) {
			shop.getShops().removeShop(shop);
			return false;
		}
		Location shopLocation = frame.getLocation();
		Location fwdOfShop = PlayerUtil.nudgeForward(0.23, frame, shopLocation);
		int totalAmount = (shop.getAmount() * quantity);
		double totalCost = shop.getCost() * quantity;
		if (!PlayerUtil.hasPermission("nerodungeons.buy", customer)) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNoPermission"),
					ChatColor.YELLOW, customer);
			shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
			return false;
		}
		String currencyName = economy.currencyToString(shop.getCurrency());
		if (shop.getUUID().equals(customer.getUniqueId())
				&& !PlayerUtil.hasPermission("nerodungeons.buy.own", customer)) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopBuyOwn"), ChatColor.RED,
					customer);
			shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
			return false;
		}
		int invSpace = Items.invSpace(customer.getInventory(), shop.getItem(frame));
		if (invSpace < totalAmount) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedRoom"), ChatColor.RED,
					customer);
			shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
			return false;
		}
		double customerWallet = economy.balance(customer.getUniqueId(), shop.getCurrency());
		Location aboveHead = customer.getLocation(); // above head particles
		aboveHead.setY(aboveHead.getY() + 2.5f);
		if (totalCost > customerWallet && !PlayerUtil.hasPermission("nerodungeons.nomoney", customer)) {
			customer.getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 50, 0.25, 0.25, 0.25);
			customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			double difference = totalCost - customerWallet;
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedMoney")
					.replace("%d", "" + difference).replace("%s", currencyName), ChatColor.RED, customer);
			return false;
		} else {
			ShopOwner shopOwner = new ShopOwner(shop.getUUID());
			Player owner = shopOwner.getPlayer();
			ItemStack item = shop.getItem(frame);
			if (!shop.getAdminShop()) {
				Chest chest = (Chest) shop.getChest().getState();
				Inventory shopInv = chest.getInventory();
				if (!shopInv.containsAtLeast(item, totalAmount)) {
					if (owner != null) {
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedStock")
								.replace("%s", shop.getName()), ChatColor.LIGHT_PURPLE, owner);
					}
					int stock = shop.getStock(shopInv, item);
					if (stock > 0) {
						totalAmount = stock;
						totalCost = Math.ceil((double) totalAmount * (shop.getCost() / shop.getAmount()));
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopLastStock"),
								ChatColor.AQUA, customer);
					} else {
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNoStock"),
								ChatColor.DARK_AQUA, customer);
						shopLocation.getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 100, 0.25, 0.25, 0.25);
						customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
						return false;
					}
				}
				Items.removeFromInventory(item, totalAmount, shopInv);
			}
			boolean sold = PlayerUtil.hasPermission("nerodungeons.nomoney", customer);
			if (sold == false) {
				sold = shop.getCurrency() == Currencies.REWARD_POINTS
						? economy.modifyRewards(customer.getUniqueId(), -totalCost)
						: economy.withdraw(customer.getUniqueId(), totalCost);
			}
			if (sold && !shop.getAdminShop()) {
				if (shop.getCurrency() == Currencies.REWARD_POINTS) {
					economy.modifyRewards(shop.getUUID(), totalCost);
				} else {
					economy.deposit(shop.getUUID(), totalCost);
				}
			}
			if (sold) {
				String msg = totalAmount + "x" + shop.getName() + " (" + totalCost + " " + currencyName + ")";
				boolean buySelf = customer.getUniqueId().equals(shop.getUUID());
				if (!buySelf) {
					Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopBought") + " " + msg,
							ChatColor.BLUE, customer);
				}
				if (owner != null) {
					Output.sendMessage(customer.getName() + " "
							+ shop.getShops().getPlugin().getMessages().getString("ShopBought").toLowerCase() + " "
							+ msg, ChatColor.GREEN, owner);
				}
				if (shop.getMaterial().getMaxStackSize() >= totalAmount) {
					item.setAmount(totalAmount);
					customer.getInventory().addItem(item);
				} else {
					item.setAmount(item.getMaxStackSize());
					int stacksNeeded = (int) Math.floor(totalAmount / shop.getMaterial().getMaxStackSize());
					int remainder = totalAmount - (stacksNeeded * shop.getMaterial().getMaxStackSize());
					for (int i = 0; i < stacksNeeded; i++) {
						customer.getInventory().addItem(item);
					}
					if (remainder > 0) {
						item.setAmount(remainder);
						customer.getInventory().addItem(item);
					}
				}
				shopLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, aboveHead, 20, 1, 1, 1);
				customer.getWorld().playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
			}
			return sold;
		}
	}
}
