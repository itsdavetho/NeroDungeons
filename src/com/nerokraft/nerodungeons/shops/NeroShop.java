package com.nerokraft.nerodungeons.shops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.utils.Utils;

public class NeroShop {
	NeroDungeons instance = null;
	private HashMap<Block, Shop> shops = new HashMap<Block, Shop>();

	public NeroShop(NeroDungeons plugin) {
		instance = plugin;
		loadShops();
	}

	public void loadShop(Shop shop) {
		shops.put(shop.getBlock(), shop);
	}

	public void unloadShop(Shop shop) {
		shops.remove(shop.getBlock());
	}

	public Shop getShop(Block b) {
		if (shops.containsKey(b) && shops.get(b) instanceof Shop) {
			return shops.get(b);
		}
		return null;
	}

	private void loadShops() {
		File[] shops = Utils.getConfigs("shops", instance);
		for (File f : shops) {
			try {
				String path = instance.getDataFolder() + "/shops/" + f.getName();
				BufferedReader br = new BufferedReader(new FileReader(path));
				Gson gson = new Gson();
				JsonObject data = gson.fromJson(br, JsonObject.class);
				JsonObject info = data.getAsJsonObject("info");
				String owner = info.get("owner").getAsString();
				UUID uuid = UUID.fromString(info.get("uuid").getAsString());
				JsonArray shopsArray = data.getAsJsonArray("shops");
				for (JsonElement e : shopsArray) {
					World world = null;
					double x = 0, y = 0, z = 0;
					Material material = Material.COBBLESTONE; // fallback material
					int cost = 25, amount = 1; // nothing is free
					JsonObject jo = e.getAsJsonObject();
					String itemId = jo.get("id").getAsString().toUpperCase();
					String worldName = jo.get("world").getAsString();
					world = Bukkit.getServer().getWorld(worldName);
					if (world == null) {
						Bukkit.getLogger().warning("[NeroShop] <" + owner + "> has a shop on an invalid world: " + world);
						continue;
					} else if (Material.matchMaterial(itemId) == null) {
						Bukkit.getLogger().warning("[NeroShop] <" + owner + "> has a shop with an invalid material: " + itemId);
						continue;
					}
					x = jo.get("x").getAsDouble();
					y = jo.get("y").getAsDouble();
					z = jo.get("z").getAsDouble();
					cost = jo.get("cost").getAsInt();
					amount = jo.get("amount").getAsInt();
					material = Material.getMaterial(itemId);
					loadShop(new Shop(world, x, y, z, uuid, owner, material, cost, amount));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				Bukkit.getLogger().warning("[NeroShop] Malformed JSON");
			}
		}
	}
}
