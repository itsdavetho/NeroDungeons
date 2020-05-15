package com.nerokraft.shops;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nerokraft.NeroKraft;
import com.nerokraft.events.shops.ShopInteract;
import com.nerokraft.utils.Output;

import net.md_5.bungee.api.ChatColor;

public class NeroShop {
	private NeroKraft instance = null;
	private ShopInteract shopInteract = null;
	private HashMap<Block, Shop> shops = new HashMap<Block, Shop>();

	public NeroShop(NeroKraft plugin, ShopInteract shopInteract) {
		instance = plugin;
		this.shopInteract = shopInteract;
		try(Connection connection = DriverManager.getConnection(getUrl())) {
			String query = "CREATE TABLE IF NOT EXISTS shops(RowId BIGINT AUTO_INCREMENT PRIMARY KEY, uuidMost long, uuidLeast long, item_id varchar(32), adminShop integer, currency integer, canSell integer, cost double, amount integer, world varchar(16),  x integer, y integer, z integer, cx integer, cy integer, cz integer)";
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		loadShops();
	}

	public NeroKraft getPlugin() {
		return this.instance;
	}

	public Shop getShop(Block block) {
		return getShop(block, false);
	}

	public Shop getShop(Block block, boolean chest) {
		if (chest) {
			for (Shop s : shops.values()) {
				if (s.getChestLocation().equals(block.getLocation())) {
					return s;
				}
			}
		} else {
			return shops.containsKey(block) ? shops.get(block) : null;
		}
		return null;
	}

	public ShopInteract getShopInteractions() {
		return shopInteract;
	}

	private String getUrl() {
		return "jdbc:sqlite:" + instance.getDataFolder() + "/shops/neroshops.db";
	}

	public Shop loadShops() {
		try (Connection connection = DriverManager.getConnection(getUrl())) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(15);
			String query = "SELECT * FROM shops";
			ResultSet result = statement.executeQuery(query);
			while (result.next()) {
				World world = Bukkit.getWorld(result.getString("world"));
				if (world != null) {
					String itemId = result.getString("item_id");
					if (Material.matchMaterial(itemId) != null) {
						Material material = Material.getMaterial(itemId);
						if (material != Material.AIR) { // this probably wont happen
							long uuidMost = result.getLong("uuidMost");
							long uuidLeast = result.getLong("uuidLeast");
							UUID uuid = new UUID(uuidMost, uuidLeast);
							String owner = Bukkit.getPlayer(uuid).getName();
							System.out.println("---for " + owner + "'s " + material.name() + " shop---");
							Location frameLocation = new Location(world, result.getDouble("x"), result.getDouble("y"),
									result.getDouble("z"));
							Location chestLocation = new Location(world, result.getDouble("cx"), result.getDouble("cy"),
									result.getDouble("cz"));
							double cost = result.getDouble("cost");
							int amount = result.getInt("amount");
							boolean adminShop = result.getInt("adminShop") > 0;
							Currencies currency = Currencies.valueOf(result.getString("currency"));
							if (currency != null) {
								boolean canSell = result.getInt("canSell") > 0;
								long rowid = result.getLong(1);
								System.out.println(
										"rowid: " + rowid);
								Shop shop = new Shop(frameLocation, chestLocation, uuid, owner, material, cost, amount,
										adminShop, currency, this, canSell, rowid);
								Block block = world.getBlockAt(frameLocation);
								this.shops.put(block, shop);
							} else {
								System.out.println("Bad currency: " + result.getString("currency"));
							}
						} else {
							System.out.println("Just air");
						}
					} else {
						System.out.println("Bad material " + itemId);
					}
				} else { 
					System.out.println("Bad world " + result.getString("world"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void removeShop(Shop shop) {
		try (Connection connection = DriverManager.getConnection(getUrl())) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(15);
			String query = "DELETE FROM shops WHERE rowid = " + shop.getRowId();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateShop(Shop s, Player p) {
		try (Connection connection = DriverManager.getConnection(getUrl())) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(15);
			long uuidMost = p.getUniqueId().getMostSignificantBits();
			long uuidLeast = p.getUniqueId().getLeastSignificantBits();
			String query = "REPLACE INTO shops (uuidMost,uuidLeast,item_id,adminShop,currency,canSell,cost,amount,world,x,y,z,cx,cy,cz) VALUES (\r\n"
					+ "  '" + uuidMost + "',\r\n" + "  '" + uuidLeast + "',\r\n" + "  '" + s.getItemId() + "',\r\n"
					+ "  '" + s.getAdminShop() + "',\r\n" + "  '" + s.getCurrency() + "',\r\n" + "  '" + s.getCanSell()
					+ "',\r\n" + "  '" + s.getCost() + "',\r\n" + "  '" + s.getAmount() + "',\r\n" + "  '"
					+ s.getWorld().getName() + "',\r\n" + "  '" + s.getFrameLocation().getX() + "',\r\n" + "  '"
					+ s.getFrameLocation().getY() + "',\r\n" + "  '" + s.getFrameLocation().getZ() + "',\r\n" + "  '"
					+ s.getChest().getX() + "',\r\n" + "  '" + s.getChest().getY() + "',\r\n" + "  '"
					+ s.getChest().getZ() + "'\r\n" + ")";
			Output.sendDebug(query, ChatColor.GREEN, p);
			statement.executeUpdate(query);
		} catch (SQLException e) {
			Output.sendMessage("Sorry, there was an error while attempting to save your shop.", ChatColor.RED, p);
			e.printStackTrace();
		}
	}
}
