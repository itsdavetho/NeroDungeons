package com.nerokraft.events.shops;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nerokraft.shops.Currencies;
import com.nerokraft.shops.Shop;
import com.nerokraft.utils.Economics;
import com.nerokraft.utils.Output;
import com.nerokraft.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class ShopGui implements Listener {
	private Player player;
	private Inventory inv;
	private ItemFrame frame;
	private Shop shop;
	private long lastClick;

	public ShopGui(Shop shop, ItemFrame frame, Player player) {
		this.shop = shop;
		this.frame = frame;
		this.player = player;
	}

	public void show() {
		String owner = shop.getOwnerName();
		owner = (owner.length() > 12) ? (owner.substring(0, 9) + "...") : owner;
		String title = owner + "'s " + shop.getName();
		title = title.length() > 27 ? title.substring(0, 24) + "..." : title;
		title = (title + " shop");
		this.inv = Bukkit.createInventory(null, 27, title);
		double sellingPrice = (shop.getCost() * shop.getShops().getPlugin().getEconomy().getValueDecay());
		double buyingPrice = shop.getCost();
		ItemStack item = shop.getItem(frame);
		String currencyName = shop.getCurrency() == Currencies.REWARD_POINTS ? "reward points" : "gold";
		setTile(0, new ItemStack(Material.BARRIER), "Exit", "Discontinue shopping here");
		if (PlayerUtil.hasPermission("nerodungeons.buy.many", player)) {
			setTile(10, new ItemStack(Material.DIAMOND), "Buy", "Buy " + shop.getAmount() * 3,
					"(" + buyingPrice * 3 + " " + currencyName + ")");
			setTile(11, new ItemStack(Material.GOLD_INGOT), "Buy", "Buy " + shop.getAmount() * 2,
					"(" + buyingPrice * 2 + " " + currencyName + ")");
		}
		setTile(12, new ItemStack(Material.IRON_INGOT), "Buy", "Buy " + shop.getAmount() * 1,
				"(" + buyingPrice * 1 + " " + currencyName + ")");
		inv.setItem(13, item); // just use setItem, don't wish to edit the metadata
		if (shop.getCanSell()) {
			setTile(14, new ItemStack(Material.IRON_INGOT), "Sell", "Sell " + shop.getAmount() * 1,
					"(" + Math.floor(sellingPrice) + " " + currencyName + ")");
			if (PlayerUtil.hasPermission("nerodungeons.buy.many", player)) {
				setTile(15, new ItemStack(Material.GOLD_INGOT), "Sell", "Sell " + shop.getAmount() * 2,
						"(" + Math.floor(sellingPrice * 2) + " " + currencyName + ")");
				setTile(16, new ItemStack(Material.DIAMOND), "Sell", "Sell " + shop.getAmount() * 3,
						"(" + Math.floor(sellingPrice * 3) + " " + currencyName + ")");
				String stock = shop.getAdminShop() ? "~"
						: "" + shop.getStock(((Chest) shop.getChest().getState()).getInventory(), item);
				setTile(22, new ItemStack(Material.PAPER), "Info", "Stock: " + stock);
			}
		}
		player.openInventory(inv);
		shop.getShops().getPlugin().getServer().getPluginManager().registerEvents(this, shop.getShops().getPlugin());
	}

	public Shop getShop() {
		return this.shop;
	}

	public void setTile(int id, ItemStack item, String title, String... lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(title);
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		inv.setItem(id, item);
	}

	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		final Inventory inventory = this.inv;
		Player player = (Player) e.getWhoClicked();
		if (e.getInventory() == inventory) {
			if (e.isLeftClick()) {
				if ((System.currentTimeMillis() / 1000L) - lastClick > 0) {
					int slot = e.getRawSlot();
					Economics eco = shop.getShops().getPlugin().getEconomy();
					if (slot > 0) {
						String stock = shop.getAdminShop() ? "~"
								: "" + shop.getStock(((Chest) shop.getChest().getState()).getInventory(), shop.getItem(frame));
						setTile(22, new ItemStack(Material.PAPER), "Info", "Stock: " + stock);
					}
					switch (slot) {
					case 0:
						player.closeInventory();
						break;
					case 10: // buy 3x
						ShopBuy.buy(player, shop, frame, eco, 3);
						break;
					case 11: // buy 2x
						ShopBuy.buy(player, shop, frame, eco, 2);
						break;
					case 12: // buy 1x
						ShopBuy.buy(player, shop, frame, eco, 1);
						break;
					case 14: // sell 1x
						ShopSell.sell(player, shop, frame, eco, 1);
						break;
					case 15: // sell 2x
						ShopSell.sell(player, shop, frame, eco, 2);
						break;
					case 16: // sell 3x
						ShopSell.sell(player, shop, frame, eco, 3);
						break;
					}
				} else {
					Output.sendMessage(shop.getShops().getPlugin().getMessages().getString("ShopSlowDown"),
							ChatColor.DARK_PURPLE, player);
				}
			}
			e.setCancelled(true);
		}
		lastClick = System.currentTimeMillis() / 1000L;
	}
}
