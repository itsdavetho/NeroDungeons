package com.nerokraft.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.nerokraft.NeroKraft;
import com.nerokraft.shops.Currencies;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class Economics {
	private Economy eco = null;
	private double tradeValueDecay = 0.400d;
	private double walletBufferRewards = 100;
	private double walletBufferEconomy = 200;

	public Economics(NeroKraft inst) {
		this.setupEconomy(inst);
	}

	public boolean modifyRewards(Player player, double amount) {
		if (amount == 0) {
			return false;
		}
		int wallet = (int) this.balance(player, Currencies.REWARD_POINTS);
		int difference = (int) Math.ceil(wallet + amount);
		if (difference < 0) {
			return false;
		}
		player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).setScore(difference);
		return true;
	}

	public double balance(Player player, Currencies type) {
		if (type == Currencies.ECONOMY) {
			if (this.getEconomy() != null) {
				return this.getEconomy().getBalance(player);
			}
		} else {
			return player.getScoreboard().getObjective("rewardPoints").getScore(player.getName()).getScore();
		}
		return 0.0;
	}

	public boolean deposit(Player player, double amount) {
		if (this.getEconomy() != null) {
			EconomyResponse r;
			r = this.getEconomy().depositPlayer(player, amount);
			if (r.transactionSuccess()) {
				return true;
			} else {
				player.sendMessage("Your balance could not be updated: " + r.errorMessage);
			}
		}
		return false;
	}

	public boolean withdraw(Player player, double amount) {
		if (this.getEconomy() != null) {
			EconomyResponse r;
			r = this.getEconomy().withdrawPlayer(player, amount);
			if (r.transactionSuccess()) {
				return true;
			} else {
				player.sendMessage("Your balance could not be updated: " + r.errorMessage);
			}
		}
		return false;
	}
	
	public String currencyToString(Currencies currency) {
		return currency == Currencies.REWARD_POINTS ? "reward points" : "gold";
	}

	private boolean setupEconomy(NeroKraft inst) {
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

	private void setEconomy(Economy eco) {
		this.eco = eco;
	}

	public Economy getEconomy() {
		return this.eco;
	}

	public double getValueDecay() {
		return this.tradeValueDecay;
	}
	
	public void setValueDecay(double decay) {
		this.tradeValueDecay = decay;
	}

	public double getWalletBuffer(Currencies type) {
		return type == Currencies.REWARD_POINTS ? walletBufferRewards : walletBufferEconomy;
	}
	
	public void setWalletBuffer(double buffer, Currencies type) {
		if(type == Currencies.REWARD_POINTS) {
			this.walletBufferRewards = buffer;
		} else {
			this.walletBufferEconomy = buffer;
		}
	}
}
