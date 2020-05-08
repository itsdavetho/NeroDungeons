package com.nerokraft.nerodungeons;

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.stream.JsonReader;
import com.nerokraft.nerodungeons.commands.CommandIntake;
import com.nerokraft.nerodungeons.events.loadouts.LoadoutInteract;
import com.nerokraft.nerodungeons.events.shops.ShopInteract;
import com.nerokraft.nerodungeons.shops.NeroShop;

public class NeroDungeons extends JavaPlugin {
	protected Map<String, JsonReader> config;
	private NeroShop neroShops;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		neroShops = new NeroShop(this);
		getServer().getPluginManager().registerEvents(new LoadoutInteract(this), this);
		getServer().getPluginManager().registerEvents(new ShopInteract(this), this);
		this.getCommand("nerodungeon").setExecutor(new CommandIntake(this));

	}

	@Override
	public void onDisable() {

	}

	public NeroShop getShops() {
		return neroShops;
	}
}
