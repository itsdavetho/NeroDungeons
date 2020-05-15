package com.nerokraft.shops;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.utils.Items;

public class Shop {
	private boolean adminShop = false;
	private int amount = 1;
	private boolean canSell = true;
	private Double cost = 1.0;
	private Currencies currency;
	private String itemId;
	private NeroShop inst;
	private Material material;
	private String owner;
	private long rowId;
	private Location shopLocation, chestLocation;
	private UUID uuid;

	public Shop(Location frameLocation, Location chestLocation, UUID uuid, String owner, Material material, double cost,
			int amount, boolean adminShop, Currencies currency, NeroShop shops, boolean canSell, long rowid) {
		this.setUUID(uuid);
		this.setOwnerName(owner);
		this.cost = cost;
		this.amount = amount;
		this.adminShop = adminShop;
		this.inst = shops;
		this.canSell = canSell;
		this.uuid = uuid;
		this.rowId = rowid;
		this.setCurrency(currency);
		this.shopLocation = frameLocation;
		this.chestLocation = chestLocation;
		if (material != null && cost > 0 && amount > 0) {
			this.setMaterial(material);
			System.out.println("[NeroShops] <" + owner + "> Shop at " + shopLocation.getX() + " " + shopLocation.getX()
					+ " " + shopLocation.getX() + " on world " + shopLocation.getWorld().getName() + " selling "
					+ this.getItemId() + " for " + cost + " " + inst.getPlugin().getEconomy().currencyToString(currency));
		}
	}

	public boolean getAdminShop() {
		return this.adminShop;
	}

	public int getAmount() {
		return amount;
	}

	public Block getBlock() {
		if (getFrameLocation() == null) {
			Bukkit.getLogger().warning("[NeroShop] no shop location: " + owner + "'s " + this.getItemId() + " shop");
			return null;
		}
		return getFrameLocation().getBlock();
	}

	public boolean getCanSell() {
		return this.canSell;
	}

	public Block getChest() {
		if(getChestLocation() != null) {
			return getWorld().getBlockAt(getChestLocation());
		}
		return null;
	}

	public Location getChestLocation() {
		return this.chestLocation;
	}

	public double getCost() {
		return Math.ceil(cost);
	}

	public Currencies getCurrency() {
		return this.currency;
	}

	public Location getFrameLocation() {
		return this.shopLocation;
	}

	public String getItemId() {
		return this.itemId;
	}

	public Material getMaterial() {
		return material;
	}

	public String getName() {
		return Items.getName(this.getItemId());
	}

	public String getOwnerName() {
		return owner;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(getUUID());
	}

	public long getRowId() {
		return this.rowId;
	}

	public NeroShop getShops() {
		return this.inst;
	}

	public int getStock(Inventory inventory, ItemStack item) {
		int count = 0;
		for (ItemStack i : inventory.getContents()) {
			if (i != null && i.getType() != Material.AIR) {
				if (i.isSimilar(item)) {
					count += i.getAmount();
				}
			}
		}
		return count;
	}

	public UUID getUUID() {
		return uuid;
	}

	public World getWorld() {
		return this.shopLocation.getWorld();
	}

	public void setAdminShop(boolean adminShop) {
		this.adminShop = adminShop;
	}

	public void setAmount(int amount) {
		ItemStack stack = new ItemStack(this.material);
		int max = stack.getMaxStackSize();
		if (amount > max) {
			throw new RuntimeException(
					inst.getPlugin().getMessages().getString("ShopInvalidAmount").replace("%s", "" + max));
		}
		this.amount = amount;
	}

	public void setCanSell(boolean canSell) {
		this.canSell = canSell;
	}

	public void setChestLocation(Location location) {
		this.chestLocation = location;
	}

	public void setCost(double cost) {
		this.cost = Math.floor(cost * amount);
	}

	public void setCurrency(Currencies currency) {
		this.currency = currency;
	}

	public void setMaterial(Material material) {
		this.material = material;
		this.itemId = getItemId();
	}

	public void setOwnerName(String owner) {
		this.owner = owner;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

}
