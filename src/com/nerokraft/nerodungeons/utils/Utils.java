package com.nerokraft.nerodungeons.utils;

import java.io.File;
import java.io.FilenameFilter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.nerokraft.nerodungeons.NeroDungeons;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;

public class Utils {
	public static LuckPerms getPermissionProvider() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if(provider != null) {
			LuckPerms api = provider.getProvider();
			return api;
		}
		return null;
	}
	
	public static User loadUser(Player p) {
		if (!p.isOnline()) {
			throw new IllegalStateException("Player is offline!");
		}

		return getPermissionProvider().getUserManager().getUser(p.getUniqueId());
	}

	@SuppressWarnings("unused")
	public static boolean hasPermission(String permission, Player p) {
		ImmutableContextSet cs = getPermissionProvider().getContextManager().getContext(p);
		QueryOptions qo = getPermissionProvider().getContextManager().getQueryOptions(p);
		CachedPermissionData data = loadUser(p).getCachedData().getPermissionData(qo);
		Tristate result = data.checkPermission(permission);
		return result.asBoolean();
	}
	
	public static File[] getConfigs(String configName, NeroDungeons inst) {
		File config = new File(inst.getDataFolder(), configName + "/");
		File[] configs = config.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".json");
			}
		});
		
		return configs;
	}
}
