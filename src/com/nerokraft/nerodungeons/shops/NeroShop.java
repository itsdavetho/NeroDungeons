package com.nerokraft.nerodungeons.shops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.events.shops.ShopInteract;
import com.nerokraft.nerodungeons.utils.Utils;

public class NeroShop {
	private NeroDungeons instance = null;
	private ShopInteract shopInteract = null;
	private HashMap<Block, Shop> shops = new HashMap<Block, Shop>();

	public NeroShop(NeroDungeons plugin, ShopInteract shopInteract) {
		instance = plugin;
		this.shopInteract = shopInteract;
		loadShops();
	}

	public ShopInteract getShopInteractions() {
		return shopInteract;
	}
	
	public void addShop(Shop shop) {
		shops.put(shop.getBlock(), shop);
	}

	public void removeShop(Shop shop) {
		if(shop != null) {
			shops.remove(shop.getBlock());
		} else {
			Bukkit.getLogger().warning("[NeroShop] Shop was null");
		}
	}

	public Shop getShop(Block b) {
		if (shops.containsKey(b) && shops.get(b) instanceof Shop) {
			return shops.get(b);
		}
		return null;
	}
	
	public Shop getShopByChest(Block block) {
		if(block.getType().equals(Material.CHEST)) {
			for(Shop s : shops.values()) {
				if(s.getChestLocation().equals(block.getLocation())) {
					return s;
				}
			}
		}
		return null;
	}

	public HashMap<Block, Shop> getRoster() {
		return this.shops;
	}

	public void saveShops(Player p) {
		String path = instance.getDataFolder() + "/shops/" + p.getUniqueId();
		Gson gson = new Gson();
		HashMap<Block, Shop> roster = getRoster();
		JsonObject json = new JsonObject();
		json.addProperty("owner", p.getName());
		JsonArray shops = new JsonArray();
		for (Shop s : roster.values()) {
			if (s.getUUID().equals(p.getUniqueId())) {
				JsonElement j = gson.toJsonTree(s, Shop.class);
				shops.add(j);
			}
		}
		json.add("shops", shops);
		try {
			//gson.toJson(json, new FileWriter(path));
			Gson test = new Gson();
			test.toJson(json, new FileWriter(new File(path)));
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadShops() {
		File[] shops = Utils.getConfigs("shops", instance);
		for (File f : shops) {
			try {
				String filename = f.getName();
				UUID uuid = UUID.fromString(f.getName());
				if (uuid == null) {
					Bukkit.getLogger().warning("All shop filenames must be UUIDs. Skipped " + filename);
					continue;
				}
				String path = instance.getDataFolder() + "/shops/" + filename;
				BufferedReader br = new BufferedReader(new FileReader(path));
				Gson gson = new Gson();
				JsonObject data = gson.fromJson(br, JsonObject.class);
				String owner = data.get("owner").getAsString();
				JsonArray shopsArray = data.getAsJsonArray("shops");
				for (JsonElement e : shopsArray) {
					Shop s = gson.fromJson(e, Shop.class).setup(owner, uuid);
					addShop(s);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
