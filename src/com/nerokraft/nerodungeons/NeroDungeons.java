package com.nerokraft.nerodungeons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class NeroDungeons extends JavaPlugin {
	protected Map<String, JsonElement> config;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.loadConfigs("shops");
		this.loadConfigs("dungeons");
		getServer().getPluginManager().registerEvents(new PlayerLoadouts(), this);
		getServer().getPluginManager().registerEvents(new NeroShop(this), this);
		this.getCommand("nd").setExecutor(new CommandCreateShop(this));
	}
	
	@Override
	public void onDisable() {
		
	}

	public LuckPerms getPermissionProvider() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if(provider != null) {
			LuckPerms api = provider.getProvider();
			return api;
		}
		return null;
	}

	public User loadUser(Player p) {
		if (!p.isOnline()) {
			throw new IllegalStateException("Player is offline!");
		}

		return getPermissionProvider().getUserManager().getUser(p.getUniqueId());
	}

	public boolean hasPermission(String permission, Player p) {
		ImmutableContextSet cs = getPermissionProvider().getContextManager().getContext(p);
		QueryOptions qo = getPermissionProvider().getContextManager().getQueryOptions(p);
		CachedPermissionData data = loadUser(p).getCachedData().getPermissionData(qo);
		Tristate result = data.checkPermission(permission);
		return result.asBoolean();
	}

	private void loadConfigs(String configName) {
		File configs = new File(getDataFolder(), configName + "/");
		File[] config = configs.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".json");
			}
		});
		
		for(File f : config) {
			JsonElement j = null;
			try {
				j = new JsonParser().parse(new FileReader(f.getCanonicalFile()));
				this.config.put(configName, j);
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Bukkit.getLogger().info("NeroKraft: Loaded " + config.length + " " + configName);
	}
	
	public JsonElement getConfig(String configName) {
		return this.config.containsKey(configName) ? this.config.get(configName) : null;
	}
}
