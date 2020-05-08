package com.nerokraft.nerodungeons.shops;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Shop {
	private int x, y, z;
	private UUID uuid;
	private String owner;
	private Material itemForSale;
	private int cost;
	private World world;
	
	public Shop(String world, int x, int y, int z, UUID uuid, String owner, Material sell, int cost) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.uuid = uuid;
		this.owner = owner;
		this.itemForSale = sell;
		this.cost = cost;
		this.world = Bukkit.getWorld(world);
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
	
	public Material itemForSale() {
		return itemForSale;
	}
	
	public int getCost() {
		return cost;
	}
	
	public Location getLocation() {
		return new Location(world, x, y, z);
	}
}
