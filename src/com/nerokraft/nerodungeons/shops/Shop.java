package com.nerokraft.nerodungeons.shops;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.utils.Items;

public class Shop {
	private transient UUID uuid;
	private transient Material material;
	private transient World worldObj;
	private transient NeroShop inst;
	private String owner;

	private double x, y, z, cx, cy, cz;
	private String world;
	private String id;
	private Double cost = 1.0;
	private int amount = 1;
	private boolean adminShop = false;
	private boolean canSell = true;

	private Currencies currency;

	public Shop setup(String owner, UUID uuid, NeroShop inst) {
		this.inst = inst;
		this.worldObj = Bukkit.getWorld(world);
		this.uuid = uuid;
		this.owner = owner;
		Location chest = new Location(worldObj, cx, cy, cz);
		if (chest != null) {
			this.setChestLocation(chest);
		}
		if (Material.matchMaterial(id) != null) {
			this.material = Material.getMaterial(id);
		} else {
			Bukkit.getLogger().warning("Couldn't setup shop " + id + " " + owner);
		}
		return this;
	}

	public Shop(Location frame, Location chest, UUID uuid, String owner, Material material, double cost, int amount,
			boolean adminShop, Currencies currency, NeroShop shops, boolean canSell) {
		this.x = frame.getX();
		this.y = frame.getY();
		this.z = frame.getZ();
		this.setUUID(uuid);
		this.setOwnerName(owner);
		this.cost = cost;
		this.worldObj = frame.getWorld();
		this.world = worldObj.getName();
		this.amount = amount;
		this.adminShop = adminShop;
		this.inst = shops;
		this.canSell = canSell;
		if (chest != null) {
			this.setChestLocation(chest);
		}
		this.setCurrency(currency);
		if (material != null && cost > 0 && amount > 0) {
			this.setMaterial(material);
			System.out.println("[NeroShops] <" + owner + "> Shop at " + x + " " + y + " " + z + " on world " + world
					+ " selling " + this.getID() + " for " + cost + " reward points");
		}
	}

	public NeroShop getShops() {
		return this.inst;
	}

	public void setCurrency(Currencies currency) {
		this.currency = currency;
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

	public int getAmount() {
		return amount;
	}

	public Block getBlock() {
		if (getFrameLocation() == null || worldObj == null || worldObj.getBlockAt(getFrameLocation()) == null) {
			Bukkit.getLogger().warning("[NeroShop] getBlock null");
			return null;
		}
		return worldObj.getBlockAt(getFrameLocation());
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setOwnerName(String owner) {
		this.owner = owner;
	}

	public String getOwnerName() {
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
		this.worldObj = location.getWorld();
	}

	public Location getChestLocation() {
		return new Location(Bukkit.getWorld(world), cx, cy, cz);
	}

	public Block getChest() {
		return getWorld().getBlockAt(getChestLocation());
	}

	public String getID() {
		if (this.getMaterial() == null) {
			return null;
		}
		return this.getMaterial().name();
	}

	public String getName() {
		return Items.getName(this.getID());
	}

	public void setCost(double cost) {
		this.cost = Math.ceil(cost * amount);
	}

	public double getCost() {
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

	public Currencies getCurrency() {
		return this.currency;
	}

	public void setCanSell(boolean canSell) {
		this.canSell = canSell;
	}
	
	public boolean getCanSell() {
		return this.canSell;
	}

}
