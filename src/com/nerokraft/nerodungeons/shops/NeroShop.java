package com.nerokraft.nerodungeons.shops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.utils.Utils;

public class NeroShop implements Listener {
	NeroDungeons instance = null;
	private HashMap<String, Shop> shops = new HashMap<String, Shop>();

	public NeroShop(NeroDungeons plugin) {
		instance = plugin;
		loadShops();
	}

	private void loadShops() {
		File[] shops = Utils.getConfigs("shops", instance);
		for(File f : shops) {
			String path = instance.getDataFolder() + "/shops/" + f.getName();
			try {
				int x = 0, y = 0, z = 0, cost = 25; // nothing is free
				String owner = null;
				UUID uuid = null;
				String world = null;
				Material mat = Material.COBBLESTONE;
				
				BufferedReader br = new BufferedReader(new FileReader(path));
				Gson g = new Gson();
				JsonObject js = g.fromJson(br, JsonObject.class);
				for(JsonElement e : js.getAsJsonArray("shops")) {
					JsonObject jo = e.getAsJsonObject();
					x = jo.get("x").getAsInt();
					y = jo.get("y").getAsInt();
					z = jo.get("z").getAsInt();
					world = jo.get("world").getAsString();
					mat = Material.getMaterial(jo.get("id").getAsString());
				}
				for(JsonElement e : js.getAsJsonArray("info")) {
					JsonObject jo = e.getAsJsonObject();
					owner = jo.get("owner").getAsString();
					uuid = UUID.fromString(jo.get("uuid").getAsString());
				}
				
				new Shop(world, x, y, z, uuid, owner, mat, cost);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
