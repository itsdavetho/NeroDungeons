package com.nerokraft.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
	private NeroKraft inst;

	public Economics(NeroKraft inst) {
		this.setupEconomy(inst);
		this.inst = inst;
	}

	public double balance(UUID uuid, Currencies type) {
		Player player = Bukkit.getServer().getPlayer(uuid);
		OfflinePlayer offlinePlayer = null;
		if(player == null) {
			offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
			if(offlinePlayer == null) {
				System.out.println("No such player " + uuid);
				return 0.0;
			}
		}
		if (type == Currencies.ECONOMY) {
			if (this.getEconomy() != null) {
				return this.getEconomy().getBalance((player != null ? player : offlinePlayer));
			}
		} else {
			return player.getScoreboard().getObjective("rewardPoints").getScore((player != null ? player.getName() : offlinePlayer.getName())).getScore();
		}
		return 0.0;
	}

	public String currencyToString(Currencies currency) {
		return currency == Currencies.REWARD_POINTS ? "reward points" : "gold";
	}

	public boolean deposit(UUID uuid, double amount) {
		Player player = Bukkit.getServer().getPlayer(uuid);
		OfflinePlayer offlinePlayer = null;
		if(player == null) {
			offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
			if(offlinePlayer == null) {
				System.out.println("No such player " + uuid);
				return false;
			}
		}
		if (this.getEconomy() != null) {
			EconomyResponse r;
			r = this.getEconomy().depositPlayer(player != null ? player : offlinePlayer, amount);
			if (r.transactionSuccess()) {
				return true;
			} else {
				if(player != null) {
					player.sendMessage(inst.getMessages().getString("EconomyNoWork") + ": " + r.errorMessage);
				}
			}
		}
		return false;
	}

	public Economy getEconomy() {
		return this.eco;
	}
	
	public double getValueDecay() {
		return this.tradeValueDecay;
	}

	public double getWalletBuffer(Currencies type) {
		return type == Currencies.REWARD_POINTS ? walletBufferRewards : walletBufferEconomy;
	}

	public boolean modifyRewards(UUID uuid, double amount) {
		if (amount == 0) {
			return false;
		}
		Player player = Bukkit.getServer().getPlayer(uuid);
		OfflinePlayer offlinePlayer = null;
		if(player == null) {
			offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
			if(offlinePlayer == null) {
				System.out.println("No such player " + uuid);
				return false;
			}
		}
		int wallet = (int) this.balance(uuid, Currencies.REWARD_POINTS);
		int difference = (int) Math.ceil(wallet + amount);
		if (difference < 0) {
			return false;
		}
		player.getScoreboard().getObjective("rewardPoints").getScore(player != null ? player.getName() : offlinePlayer.getName()).setScore(difference);
		return true;
	}

	private void setEconomy(Economy eco) {
		this.eco = eco;
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
	
	public void setValueDecay(double decay) {
		this.tradeValueDecay = decay;
	}

	public void setWalletBuffer(double buffer, Currencies type) {
		if(type == Currencies.REWARD_POINTS) {
			this.walletBufferRewards = buffer;
		} else {
			this.walletBufferEconomy = buffer;
		}
	}
	
	public boolean withdraw(UUID uuid, double amount) {
		if (this.getEconomy() != null) {
			EconomyResponse r;
			Player player = Bukkit.getServer().getPlayer(uuid);
			OfflinePlayer offlinePlayer = null;
			if(player == null) {
				offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
				if(offlinePlayer == null) {
					System.out.println("No such player " + uuid);
					return false;
				}
			}
			r = this.getEconomy().withdrawPlayer(player != null ? player : offlinePlayer, amount);
			if (r.transactionSuccess()) {
				return true;
			} else {
				player.sendMessage(inst.getMessages().getString("EconomyNoWork") + ": " + r.errorMessage);
			}
		}
		return false;
	}
}
