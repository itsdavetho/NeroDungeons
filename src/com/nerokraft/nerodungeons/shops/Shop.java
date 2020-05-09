package com.nerokraft.nerodungeons.shops;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.gson.annotations.Expose;

public class Shop {
	private transient UUID uuid;
	private transient Material material;
	private transient World worldObj;

	@Expose
	private double x, y, z, cx, cy, cz;
	@Expose
	private String world;
	private transient String owner, id;
	@Expose
	private int cost = 1, amount = 1;
	@Expose
	private boolean adminShop = false;
	private Currency currency;

	public Shop setup(String owner, UUID uuid) {
		this.worldObj = Bukkit.getWorld(world);
		this.uuid = uuid;
		this.owner = owner;
		if (Material.matchMaterial(id) != null) {
			this.material = Material.getMaterial(id);
		} else {
			Bukkit.getLogger().warning("Couldn't setup shop " + id + " " + owner);
		}
		System.out.println("[NeroShops] <" + owner + "> Shop at " + x + " " + y + " " + z + " on world " + world
				+ " selling " + material.name() + " for " + cost + " reward points");
		return this;
	}

	public Shop(Location frame, Location chest, UUID uuid, String owner, Material material, int cost, int amount,
			boolean adminShop, Currency currency) {
		this.x = frame.getX();
		this.y = frame.getY();
		this.z = frame.getZ();
		this.setUUID(uuid);
		this.setOwner(owner);
		this.cost = cost;
		this.worldObj = frame.getWorld();
		this.world = worldObj.getName();
		this.amount = amount;
		this.setCurrency(currency);
		if (material != null && cost > 0 && amount > 0) {
			this.setMaterial(material);
			System.out.println("[NeroShops] <" + owner + "> Shop at " + x + " " + y + " " + z + " on world " + world
					+ " selling " + this.getID() + " for " + cost + " reward points");
		}

	}
	
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}	
	
	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public Block getBlock() {
		return worldObj.getBlockAt(getFrameLocation());
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(getUUID());
	}

	public void setMaterial(Material material) {
		this.material = material;
		this.id = getID();
	}

	public Material getMaterial() {
		return material;
	}

	public void setChestLocation(Location location) {
		this.cx = location.getX();
		this.cy = location.getY();
		this.cz = location.getZ();
	}

	public Location getChestLocation() {
		return new Location(getWorld(), cx, cy, cz);
	}

	public Block getChest() {
		return getWorld().getBlockAt(getChestLocation());
	}

	public String getID() {
		return this.getMaterial().name();
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getCost() {
		return cost;
	}

	public Location getFrameLocation() {
		return new Location(worldObj, x, y, z);
	}

	public World getWorld() {
		return worldObj;
	}

	public void setAdminShop(boolean adminShop) {
		this.adminShop = adminShop;
	}

	public boolean getAdminShop() {
		return this.adminShop;
	}

	public Currency getCurrency() {
		return this.currency;
	}
}
