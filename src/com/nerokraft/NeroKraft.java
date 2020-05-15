package com.nerokraft;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.stream.JsonReader;
import com.nerokraft.commands.CommandIntake;
import com.nerokraft.events.shops.ShopGui;
import com.nerokraft.events.shops.ShopInteract;
import com.nerokraft.holograms.Holograms;
import com.nerokraft.shops.NeroShop;
import com.nerokraft.utils.Economics;

public class NeroKraft extends JavaPlugin {
	protected Map<String, JsonReader> config;
	private NeroShop neroShops;
	private Economics economics;
	private Holograms holograms;
	private ResourceBundle messages;
	private ShopGui gui;
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		ShopInteract shopInteraction = new ShopInteract(this);
		this.neroShops = new NeroShop(this, shopInteraction);
		this.holograms = new Holograms(this);
		this.economics = new Economics(this);
		this.messages = ResourceBundle.getBundle("MessagesBundle", new Locale("en", "US"));
		System.out.println("NeroShops set up complete");
		this.getCommand("nerodungeon").setExecutor(new CommandIntake(this));
		getServer().getPluginManager().registerEvents(this.neroShops.getShopInteractions(), this);
	}

	public ShopGui getGUI() {
		return this.gui;
	}

	public Economics getEconomy() {
		return this.economics;
	}

	public Holograms getHolograms() {
		return this.holograms;
	}

	public ResourceBundle getMessages() {
		return this.messages;
	}

	@Override
	public void onDisable() {

	}

	public NeroShop getShops() {
		return neroShops;
	}
}
