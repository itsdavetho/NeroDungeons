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
import com.nerokraft.utils.Economics;
import com.nerokraft.utils.Items;
import com.nerokraft.utils.Output;
import com.nerokraft.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class ShopSell {
	public static boolean sell(Player customer, Shop shop, ItemFrame frame, Economics economy, int quantity) {
		if (frame == null || frame.getItem().getType() == Material.AIR) {
			shop.getShops().removeShop(shop);
			return false;
		}
		String payment = shop.getShops().getPlugin().getEconomy().currencyToString(shop.getCurrency());
		double ownerWallet = shop.getShops().getPlugin().getEconomy().balance(customer.getUniqueId(),
				shop.getCurrency());
		int totalAmount = quantity * shop.getAmount();
		double totalPayed = Math.floor(shop.getCost() * shop.getShops().getPlugin().getEconomy().getValueDecay())
				* quantity;
		Material material = frame.getItem().getType();
		boolean sold = PlayerUtil.hasPermission("nerodungeons.nomoney", shop.getPlayer()) || shop.getAdminShop();
		boolean canSellSelf = shop.getUUID().equals(customer.getUniqueId())
				&& PlayerUtil.hasPermission("nerodungeons.buy.own", customer);
		Location aboveHead = customer.getLocation(); // above head particles
		Location fwdOfShop = PlayerUtil.nudgeForward(0.23, frame, shop.getFrameLocation());
		aboveHead.setY(aboveHead.getY() + 2.5f);
		if (!canSellSelf && shop.getUUID().equals(customer.getUniqueId())) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSellSelf"), ChatColor.GRAY,
					customer);
			shop.getFrameLocation().getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shop.getFrameLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
			return false;
		}
		if (((totalPayed + economy.getWalletBuffer(shop.getCurrency())) <= ownerWallet || shop.getAdminShop())
				&& PlayerUtil.hasPermission("nerodungeons.sell", customer)) {
			Inventory playerInventory = customer.getInventory();
			ItemStack item = shop.getItem(frame);
			boolean inInventory = playerInventory.containsAtLeast(item, totalAmount);
			if (!inInventory) {
				int stock = shop.getStock(customer.getInventory(), item);
				if (stock > 0) {
					inInventory = true;
					totalAmount = stock;
					totalPayed = Math.floor(((shop.getCost() / shop.getAmount())
							* shop.getShops().getPlugin().getEconomy().getValueDecay()) * totalAmount);
				}
			}
			if (inInventory) {
				Chest chest = null;
				boolean shopHasSpace = shop.getAdminShop();
				if (!shop.getAdminShop()) {
					chest = (Chest) shop.getChest().getState();
					shopHasSpace = Items.invSpace(chest.getInventory(), material) >= totalAmount;
				}
				if (shopHasSpace) {
					if (sold == false && !customer.getUniqueId().equals(shop.getUUID())) {
						sold = shop.getCurrency() == Currencies.REWARD_POINTS
								? economy.modifyRewards(shop.getUUID(), -totalPayed)
								: economy.withdraw(shop.getUUID(), totalPayed);
					}
					if (sold) {
						if (!customer.getUniqueId().equals(shop.getUUID())) {
							if (shop.getCurrency() == Currencies.REWARD_POINTS) {
								economy.modifyRewards(customer.getUniqueId(), totalPayed);
							} else {
								economy.deposit(customer.getUniqueId(), totalPayed);
							}
						}
						Items.removeFromInventory(item, totalAmount, playerInventory);
						if (chest != null && !shop.getAdminShop() && shopHasSpace) {
							if (shop.getMaterial().getMaxStackSize() >= totalAmount) {
								item.setAmount(totalAmount);
								chest.getInventory().addItem(item);
							} else {
								item.setAmount(item.getMaxStackSize());
								int stacksNeeded = (int) Math.floor(totalAmount / shop.getMaterial().getMaxStackSize());
								int remainder = totalAmount - (stacksNeeded * shop.getMaterial().getMaxStackSize());
								for (int i = 0; i < stacksNeeded; i++) {
									chest.getInventory().addItem(item);
								}
								if (remainder > 0) {
									item.setAmount(remainder);
									chest.getInventory().addItem(item);
								}
							}
						}
						if (shop.getUUID().equals(customer.getUniqueId()) == false) {
							Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSoldPlayer")
									.replace("%s", shop.getOwnerName()).replace("%a", "" + totalAmount)
									.replace("%n", shop.getName()).replace("%p", "" + totalPayed)
									.replace("%t", payment), ChatColor.BLUE, customer);
						}
						if (shop.getPlayer() != null) {
							Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSoldShop")
									.replace("%s", customer.getName()).replace("%a", "" + totalAmount)
									.replace("%n", shop.getName()).replace("%p", "" + totalPayed)
									.replace("%t", payment), ChatColor.GREEN, shop.getPlayer());
						}
						shop.getFrameLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, aboveHead, 20, 1, 1,
								1);
						customer.getWorld().playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f,
								1.0f);
						return true;
					} else {
						double difference = Math.floor(totalPayed - ownerWallet);
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNeedMoney")
								.replace("%s",
										shop.getShops().getPlugin().getEconomy().currencyToString(shop.getCurrency()))
								.replace("%d", "" + difference), ChatColor.RED, customer);
						shop.getFrameLocation().getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 100, 0.25,
								0.25, 0.25);
						customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
					}
				} else {
					Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopInvFull"), ChatColor.LIGHT_PURPLE, customer);
					if(shop.getPlayer() != null) {
						Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopInvFullOwner").replace("%s", shop.getName()), ChatColor.YELLOW, shop.getPlayer());
					}
				}
			} else if (!inInventory) {
				Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopNoItem"),
						ChatColor.LIGHT_PURPLE, customer);
				shop.getFrameLocation().getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 100, 0.25, 0.25,
						0.25);
				customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
			}
		} else if (!PlayerUtil.hasPermission("nerodungeons.sell", customer)) {
			Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("CommandLackPermission"),
					ChatColor.RED, customer);
			shop.getFrameLocation().getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shop.getFrameLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
		}
		return false;
	}
}
