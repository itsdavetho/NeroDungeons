package com.nerokraft.nerodungeons.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.md_5.bungee.api.ChatColor;

public class PlayerUtil {
	public static LuckPerms getPermissionProvider() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
			LuckPerms api = provider.getProvider();
			return api;
		}
		return null;
	}

	@SuppressWarnings("unused")
	public static boolean hasPermission(String permission, Player player) {
		ImmutableContextSet cs = getPermissionProvider().getContextManager().getContext(player);
		QueryOptions qo = getPermissionProvider().getContextManager().getQueryOptions(player);
		CachedPermissionData data = loadUser(player).getCachedData().getPermissionData(qo);
		Tristate result = data.checkPermission(permission);
		return ((result.asBoolean() == true) || player.hasPermission(permission));
	}

	public static User loadUser(Player p) {
		if (!p.isOnline()) {
			throw new IllegalStateException("Player is offline!");
		}
		return getPermissionProvider().getUserManager().getUser(p.getUniqueId());
	}

	public static boolean canBuild(Location location, Player player) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
		if (claim != null && claim.isAdminClaim() && !PlayerUtil.hasPermission("nerodungeons.admin", player)) {
			Output.sendMessage("You can't build here", ChatColor.GREEN, player);
		}
		if (claim != null && (claim.ownerID != null && claim.ownerID.equals(player.getUniqueId()))
				|| PlayerUtil.hasPermission("nerodungeons.grief", player)) {
			return true;
		} else if (claim != null) {
			Output.sendMessage("Sorry, but you cannot set up shop in someone else's claim!", ChatColor.RED, player);
		} else if (claim == null) {
			Output.sendMessage("Sorry! You must choose inside a claim that you own", ChatColor.RED, player);
		}
		return false;
	}
}
