package com.nerokraft.nerodungeons;

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.stream.JsonReader;
import com.nerokraft.nerodungeons.commands.shops.CommandCreateShop;
import com.nerokraft.nerodungeons.events.loadouts.LoadoutInteract;
import com.nerokraft.nerodungeons.events.shops.ShopInteract;
import com.nerokraft.nerodungeons.shops.NeroShop;

public class NeroDungeons extends JavaPlugin {
	protected Map<String, JsonReader> config;
	//private final NeroShop shop;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		/*this.loadConfigs("shops");
		this.loadConfigs("dungeons");*/
		new NeroShop(this);
		getServer().getPluginManager().registerEvents(new LoadoutInteract(this), this);
		getServer().getPluginManager().registerEvents(new ShopInteract(this), this);
		this.getCommand("nerodungeon").setExecutor(new CommandCreateShop(this));
		
	}
	
	@Override
	public void onDisable() {
		
	}


}
