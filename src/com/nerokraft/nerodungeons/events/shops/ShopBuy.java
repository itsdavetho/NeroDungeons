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
		if (shop == null) {
			return;
		}
		Location shopLocation = frame.getLocation();
		double x = shopLocation.getX();
		double z = shopLocation.getZ();
		switch (frame.getFacing()) {
		case NORTH:
			z -= 0.2;
			break;
		case SOUTH:
			z += 0.2;
			break;
		case WEST:
			x -= 0.2;
			break;
		case EAST:
			x += 0.2;
			break;
		default:
			break;
		}

		Location fwdOfShop = new Location(shopLocation.getWorld(), x, shopLocation.getY(), z);

		if (!PlayerUtil.hasPermission("nerodungeons.buy", customer)) {
			Output.sendMessage("You are not permitted to buy items from this store.", ChatColor.YELLOW, customer);
			shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
			return;
		} else if (shop.getUUID().equals(customer.getUniqueId())
				&& !PlayerUtil.hasPermission("nerodungeons.buy.own", customer)) {
			Output.sendMessage("You can't buy your own item!", ChatColor.RED, customer);
			shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
			customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
			return;
		}
		String currencyName = shop.getCurrency() == Currency.REWARD_POINTS ? "reward points" : "gold";
		String msg = shop.getAmount() + "x" + shop.getName() + " (" + shop.getCost() + " " + currencyName + ")";
		if (!customer.isSneaking()) {
			Output.sendMessage("[" + shop.getOwner() + "] " + msg, ChatColor.DARK_GREEN, customer);
		} else {
			if (Items.invSpace(customer.getInventory(), shop.getMaterial()) < shop.getAmount()) {
				Output.sendMessage("You should clear some space in your inventory before buying something.",
						ChatColor.RED, customer);
				shopLocation.getWorld().spawnParticle(Particle.BARRIER, fwdOfShop, 1);
				customer.playSound(shopLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2f);
				return;
			}
			double rewardPoints = customer.getScoreboard().getObjective("rewardPoints").getScore(customer.getName())
					.getScore();
			double customerWallet = shop.getCurrency() == Currency.ECONOMY ? economy.balance(customer) : rewardPoints;
			Location aboveHead = customer.getLocation(); // above head particles
			aboveHead.setY(aboveHead.getY() + 2.5f);
			if (shop.getCost() > customerWallet && !PlayerUtil.hasPermission("nerodungeons.nomoney", customer)) {
				shopLocation.getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 50, 0.25, 0.25, 0.25);
				customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				Output.sendMessage("Sorry, you don't have enough funds to purchase this!", ChatColor.RED, customer);
				return;
			} else {
				Player owner = shop.getPlayer();
				boolean sold = PlayerUtil.hasPermission("nerodungeons.nomoney", customer);

				if (!shop.getAdminShop()) {
					Chest chest = (Chest) shop.getChest().getState();
					Inventory shopInv = chest.getInventory();
					ItemStack stack = new ItemStack(shop.getMaterial());
					if (!shopInv.containsAtLeast(stack, shop.getAmount())) {
						if (owner.isOnline()) {
							Output.sendMessage("You need to restock your " + shop.getName() + " shop",
									ChatColor.LIGHT_PURPLE, owner);
						}
						Output.sendMessage("Sorry, but that shop is currently out of stock!", ChatColor.DARK_AQUA,
								customer);
						shopLocation.getWorld().spawnParticle(Particle.FALLING_WATER, aboveHead, 50, 0.25, 0.25, 0.25);
						customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
						return;
					} else {
						Items.removeFromInventory(stack, shop.getAmount(), shopInv);
					}
				}
				if (sold == false) {
					if (shop.getCurrency() == Currency.REWARD_POINTS) {
						sold = economy.modifyRewards(customer, -shop.getCost());
						if (sold && !shop.getAdminShop()) {
							economy.modifyRewards(owner, shop.getCost());
						}
					} else {
						sold = economy.withdraw(customer, shop.getCost());
						if (sold && !shop.getAdminShop()) {
							economy.deposit(owner, shop.getCost());
						}
					}
				}
				if (sold) {
					boolean buySelf = customer.getUniqueId().equals(owner.getUniqueId());
					if (!buySelf) {
						Output.sendMessage("Bought " + msg, ChatColor.BLUE, customer);
					}
					if (owner.isOnline()) {
						Output.sendMessage(customer.getName() + " bought " + msg, ChatColor.GREEN, owner);
					}
					ItemStack stack = new ItemStack(shop.getMaterial(), shop.getAmount());
					stack.setItemMeta(frame.getItem().getItemMeta());
					customer.getInventory().addItem(stack);
					shopLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, aboveHead, 20, 1, 1, 1);
					customer.getWorld().playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
				}
			}
		}
	}
}
