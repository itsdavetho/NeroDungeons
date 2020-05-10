package com.nerokraft.nerodungeons;

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.stream.JsonReader;
import com.nerokraft.nerodungeons.commands.CommandIntake;
import com.nerokraft.nerodungeons.events.loadouts.LoadoutInteract;
import com.nerokraft.nerodungeons.events.shops.ShopInteract;
import com.nerokraft.nerodungeons.shops.NeroShop;
import com.nerokraft.nerodungeons.utils.Economics;

public class NeroDungeons extends JavaPlugin {
	protected Map<String, JsonReader> config;
	private NeroShop neroShops;
	private Economics economics;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		ShopInteract shopInteraction = new ShopInteract(this);
		neroShops = new NeroShop(this, shopInteraction);
		getServer().getPluginManager().registerEvents(new LoadoutInteract(this), this);
		getServer().getPluginManager().registerEvents(shopInteraction, this);
		this.getCommand("nerodungeon").setExecutor(new CommandIntake(this));
		setEconomy(new Economics(this));

	}

	public void setEconomy(Economics e) {
		this.economics = e;
	}
	
	public Economics getEconomy() {
		return this.economics;
	}
	
	@Override
	public void onDisable() {

	}

	public NeroShop getShops() {
		return neroShops;
	}
}
