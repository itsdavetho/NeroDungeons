package com.nerokraft.nerodungeons.holograms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nerokraft.nerodungeons.nms.NmsUtil;

import net.md_5.bungee.api.ChatColor;

public class Hologram {
	private final Holograms instance;
	private Location location;
	private final Object armorStand;
	private Set<Player> players = new HashSet<Player>();
	private String text;
	private int yIndex = 0, id = 0;

	public Hologram(String text, Location location, Holograms instance)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException, NoSuchFieldException {
		this.armorStand = NmsUtil.getArmorStand()
				.getConstructor(NmsUtil.getWorld(), double.class, double.class, double.class)
				.newInstance(NmsUtil.getHandle(NmsUtil.getCraftWorld().cast(location.getWorld())), location.getX(),
						location.getY(), location.getZ());
		setSmall(true);
		setMarker(true);
		setStandInvisible(true);
		setTextVisible(true);
		setText(text);
		this.text = ChatColor.translateAlternateColorCodes('&', text);
		this.instance = instance;
		this.location = adjustY(location);
		this.id = NmsUtil.getEntityId(this.armorStand);
	}

	public void addPlayer(Player player) {
		this.players.add(player);
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
	}

	public Set<Player> getPlayers() {
		return this.players;
	}

	public void show(Set<Player> players) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchFieldException {
		for (Player player : players) {
			this.show(player);
		}
	}

	public void show(Player player) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchFieldException {
		if (this.players.contains(player)) {
			throw new RuntimeException(instance.getPlugin().getMessages().getString("HologramMultiSpawn")
					.replace("%u", player.getName()));
		}
		if (this.text.isEmpty() || this.location == null || this.instance == null) {
			throw new RuntimeException(instance.getPlugin().getMessages().getString("HologramInvalidInput"));
		}
		for(Hologram h : instance.getHolograms()) { // i imagine this could become costly in resources
			if(h.players.contains(player)) {
				h.hide(player);
				break;
			}
		}
		Object packet = NmsUtil.getSpawnEntityPacket(this.armorStand);
		NmsUtil.sendPacket(player, packet);
		this.addPlayer(player);
		this.updateMeta();
	}

	public void hide() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstantiationException, NoSuchFieldException {
		for (Player player : players) {
			this.hide(player);
		}
	}

	public void hide(Player player) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstantiationException, NoSuchFieldException {
		if (this.players.remove(player)) {
			Object packet = NmsUtil.getDestroyEntityPacket(this.id);
			NmsUtil.sendPacket(player, packet);
		}
		if (this.players.isEmpty()) {
			instance.removeHologram(this);
		}
	}

	public void setSmall(boolean small) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		NmsUtil.getArmorStand().getMethod("setSmall", boolean.class).invoke(this.armorStand, small);
	}

	public void setMarker(boolean marker) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		NmsUtil.getArmorStand().getMethod("setMarker", boolean.class).invoke(this.armorStand, marker);
	}

	public void setTextVisible(boolean visible) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		NmsUtil.getArmorStand().getMethod("setCustomNameVisible", boolean.class).invoke(this.armorStand, visible);
	}

	public void setStandInvisible(boolean invisible) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		NmsUtil.getArmorStand().getMethod("setInvisible", boolean.class).invoke(this.armorStand, invisible);
	}

	public void setText(String text) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchFieldException {
		this.text = text;
		Method newICBC = NmsUtil.getIChatBaseComponent().getDeclaredClasses()[0].getMethod("a", String.class);
		Object icbc = newICBC.invoke(null, "{\"text\": \"" + text + "\"}");
		NmsUtil.getArmorStand().getMethod("setCustomName", NmsUtil.getIChatBaseComponent()).invoke(this.armorStand,
				icbc);
		this.updateMeta();
	}

	public void updateMeta() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchFieldException {
		Object dataWatcher = NmsUtil.getEntity().getMethod("getDataWatcher").invoke(this.armorStand);
		Object packet = NmsUtil.getPlayOutEntityMetadata()
				.getConstructor(int.class, NmsUtil.getDataWatcher(), boolean.class)
				.newInstance(NmsUtil.getEntityId(this.armorStand), dataWatcher, true);
		for (Player player : players) {
			NmsUtil.sendPacket(player, packet);
		}
	}

	public void updateLocation(Location location)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException, NoSuchFieldException {
		NmsUtil.getEntity().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class)
				.invoke(this.armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(),
						location.getPitch());
		this.location = adjustY(location);
		Object packet = NmsUtil.getPlayOutEntityTeleport()
				.getConstructor(int.class, NmsUtil.getDataWatcher(), boolean.class).newInstance(this.armorStand);
		for (Player player : players) {
			NmsUtil.sendPacket(player, packet);
		}
	}

	public int getYIndex() {
		return this.yIndex;
	}

	private Location adjustY(Location location) {
		Set<Hologram> holograms = instance.getHolograms();
		for (Hologram h : holograms) {
			if (h.getLocation().equals(location) && h.getId() != this.getId()) {
				this.yIndex++;
			}
		}
		location.setY((this.yIndex * 0.23d)); //
		return location;
	}

	public Location getLocation() {
		return this.location;
	}

	public int getId() {
		return this.id;
	}
}
