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
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.nerokraft.NeroKraft;
import com.nerokraft.events.shops.ShopInteract;
import com.nerokraft.utils.Output;

import net.md_5.bungee.api.ChatColor;

public class NeroShop {
    private NeroKraft instance = null;
    private ShopInteract shopInteract = null;
    private HashMap<Long, Shop> shops = new HashMap<Long, Shop>();

    public NeroShop(NeroKraft plugin, ShopInteract shopInteract) {
	instance = plugin;
	this.shopInteract = shopInteract;
	try (Connection connection = DriverManager.getConnection(getUrl())) {
	    String query = "CREATE TABLE IF NOT EXISTS shops(shopId INTEGER PRIMARY KEY, uuidMost long, uuidLeast long, item_id varchar(32), adminShop integer, currency integer, canSell integer, cost double, amount integer, world varchar(16),  x integer, y integer, z integer, cx integer, cy integer, cz integer)";
	    Statement statement = connection.createStatement();
	    statement.executeUpdate(query);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	loadShops();
    }

    public NeroKraft getPlugin() {
	return this.instance;
    }

    public Shop getShop(long shopid) {
	return getShop(shopid, false);
    }

    public Shop getShop(long shopid, boolean chest) {
	return shops.containsKey(shopid) ? shops.get(shopid) : null;
    }

    public Shop getShop(Location chestLocation) {
	for (Shop s : shops.values()) {
	    if (s.getChestLocation().equals(chestLocation)) {
		return s;
	    }
	}
	return null;
    }

    public HashMap<Long, Shop> getShops() {
	return this.shops;
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
		long shopId = result.getLong("shopId");
		if (shopId <= 0) {
		    System.out.println("[NeroShop] No shop ID - " + result);
		} else {
		    World world = Bukkit.getWorld(result.getString("world"));
		    if (world != null) {
			String itemId = result.getString("item_id");
			if (Material.matchMaterial(itemId) != null) {
			    Material material = Material.getMaterial(itemId);
			    if (material != Material.AIR) { // this probably wont happen
				long uuidMost = result.getLong("uuidMost");
				long uuidLeast = result.getLong("uuidLeast");
				UUID uuid = new UUID(uuidMost, uuidLeast);
				ShopOwner shopOwner = new ShopOwner(uuid);
				String ownerName = shopOwner.getName();
				Location frameLocation = new Location(world, result.getDouble("x"),
					result.getDouble("y"), result.getDouble("z"));
				Location chestLocation = new Location(world, result.getDouble("cx"),
					result.getDouble("cy"), result.getDouble("cz"));
				double cost = result.getDouble("cost");
				int amount = result.getInt("amount");
				boolean adminShop = result.getInt("adminShop") > 0;
				Currencies currency = Currencies.valueOf(result.getString("currency"));
				if (currency != null) {
				    boolean canSell = result.getInt("canSell") > 0;
				    Shop shop = new Shop(frameLocation, chestLocation, uuid, ownerName, material, cost,
					    amount, adminShop, currency, this, canSell, shopId);
				    shops.put(shopId, shop);
				} else {
				    System.out.println("[NeroShop] [" + shopId + "] Bad currency: "
					    + result.getString("currency"));
				}
			    } else {
				System.out.println("[NeroShop] [" + shopId + "] Just air");
			    }
			} else {
			    System.out.println("[NeroShop] [" + shopId + "] Bad material: " + itemId);
			}
		    } else {
			System.out.println("[NeroShop] [" + shopId + "] Bad world: " + result.getString("world"));
		    }
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
	    String query = "DELETE FROM shops WHERE shopId = " + shop.getShopId();
	    statement.executeUpdate(query);
	    shops.remove(shop.getShopId());
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    public long addShop(Shop s, Player p) {
	try (Connection connection = DriverManager.getConnection(getUrl())) {
	    Statement statement = connection.createStatement();
	    statement.setQueryTimeout(15);
	    long uuidMost = p.getUniqueId().getMostSignificantBits();
	    long uuidLeast = p.getUniqueId().getLeastSignificantBits();
	    double chestX = 0.0, chestY = 0.0, chestZ = 0.0;
	    if (s.getChest() != null) {
		chestX = s.getChestLocation().getX();
		chestY = s.getChestLocation().getY();
		chestZ = s.getChestLocation().getZ();
	    }
	    String query = "INSERT INTO shops (uuidMost,uuidLeast,item_id,adminShop,currency,canSell,cost,amount,world,x,y,z,cx,cy,cz) VALUES (\r\n"
		    + "  " + uuidMost + ",\r\n" + "  " + uuidLeast + ",\r\n" + "  '" + s.getItemId() + "',\r\n" + "  "
		    + s.getAdminShop() + ",\r\n" + "  '" + s.getCurrency() + "',\r\n" + "  " + s.getCanSell() + ",\r\n"
		    + "  " + s.getCost() + ",\r\n" + "  " + s.getAmount() + ",\r\n" + "  '" + s.getWorld().getName()
		    + "',\r\n" + "  " + s.getFrameLocation().getX() + ",\r\n" + "  " + s.getFrameLocation().getY()
		    + ",\r\n" + "  " + s.getFrameLocation().getZ() + ",\r\n" + "  " + chestX + ",\r\n" + "  " + chestY
		    + ",\r\n" + "  " + chestZ + "\r\n" + ")";
	    // Output.sendDebug(query, ChatColor.GREEN, p);
	    statement.executeUpdate(query);
	    ResultSet rs = statement.getGeneratedKeys();
	    if (rs.next()) {
		long shopId = rs.getLong(1);
		s.setShopId(shopId);
		return shopId;
	    }
	} catch (SQLException e) {
	    Output.sendMessage(instance.getMessages().getString("ShopCreateError"), ChatColor.RED, p);
	    e.printStackTrace();
	}
	return -1;
    }

    public void setShopMeta(ItemFrame frame, long l) {
	if (frame.getItem() != null && frame.getItem().getType() != Material.AIR) {
	    NamespacedKey key = new NamespacedKey(instance, "neroshop");
	    ItemStack stack = frame.getItem();
	    ItemMeta meta = stack.getItemMeta();
	    if (l <= 0 && meta.getPersistentDataContainer().has(key, PersistentDataType.LONG)) {
		meta.getPersistentDataContainer().remove(key);
	    } else {
		meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, l);
	    }
	    stack.setItemMeta(meta);
	    frame.setItem(stack);
	}
    }

    public long getShopMeta(ItemFrame frame) {
	if (frame.getItem() != null && frame.getItem().getType() != Material.AIR) {
	    NamespacedKey key = new NamespacedKey(instance, "neroshop");
	    ItemMeta itemMeta = frame.getItem().getItemMeta();
	    PersistentDataContainer container = itemMeta.getPersistentDataContainer();
	    if (container.has(key, PersistentDataType.LONG)) {
		return container.get(key, PersistentDataType.LONG);
	    }
	}
	return -1l;
    }
}
