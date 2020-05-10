package com.nerokraft.nerodungeons.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.nerokraft.nerodungeons.NeroDungeons;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class Economics {
	private Economy eco = null;
	
	public Economics(NeroDungeons inst) {
		this.setupEconomy(inst);
	}

	private boolean setupEconomy(NeroDungeons inst) {
		if (inst.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = inst.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		this.setEconomy(rsp.getProvider());
		return true;
	}

	public double balance(Player player) {
		if(this.getEconomy() != null) {
			return this.getEconomy().getBalance(player);
		}
		return 0.0;
	}
	
	public boolean deposit(Player player, double amount) {
		if (this.getEconomy() != null) {
			EconomyResponse r;
			r = this.getEconomy().depositPlayer(player, amount);
			if(r.transactionSuccess()) {
				return true;
			} else {
				player.sendMessage("Your balance could not be updated: " + r.errorMessage);
			}
		}
		return false;
	}
	
	public boolean withdraw(Player player, double amount) {
		if(this.getEconomy() != null) {
			EconomyResponse r;
			r = this.getEconomy().withdrawPlayer(player, amount);
			if(r.transactionSuccess()) {
				return true;
			} else {
				player.sendMessage("Your balance could not be updated: " + r.errorMessage);
			}
		}
		return false;
	}

	private void setEconomy(Economy eco) {
		this.eco = eco;
	}
	
	public Economy getEconomy() {
		return this.eco;
	}

	public boolean modifyRewards(Player player, double cost) {
		if(cost == 0) {
			return false;
		}
		int wallet = player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).getScore();
		int difference = (int) Math.ceil(wallet + cost);
		if(difference < 0) {
			return false;
		}
		player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).setScore(difference);
		return true;
	}
}
