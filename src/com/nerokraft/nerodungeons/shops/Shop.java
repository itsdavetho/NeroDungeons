package com.nerokraft.nerodungeons.shops;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Shop {
	private double x, y, z;
	private UUID uuid;
	private String owner;
	private Material material;
	private int cost;
	private int amount = 1;
	private World world;

	public Shop(World world, double x2, double y2, double z2, UUID uuid, String owner, Material sell, int cost,
			int amount) {
		this.x = x2;
		this.y = y2;
		this.z = z2;
		this.uuid = uuid;
		this.owner = owner;
		this.material = sell;
		this.cost = cost;
		this.world = world;
		this.amount = amount;
		System.out.println("[NeroShops] <" + owner + "> Shop at " + x2 + " " + y2 + " " + z2 + " on world " + world.getName()
				+ " selling " + material.name() + " for " + cost + " reward points");
	}

	public int getAmount() {
		return amount;
	}

	public Block getBlock() {
		return world.getBlockAt(getLocation());
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getOwner() {
		return owner;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(getUUID());
	}

	public Material getMaterial() {
		return material;
	}

	public int getCost() {
		return cost;
	}

	public Location getLocation() {
		return new Location(world, x, y, z);
	}
}
