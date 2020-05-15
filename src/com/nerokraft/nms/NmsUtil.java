package com.nerokraft.nms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NmsUtil {
	// https://www.spigotmc.org/wiki/send-title-to-player-packets/
	public static void sendPacket(Player player, Object packet) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Object handle = getHandle(player);
		Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
		Method currentPacket = playerConnection.getClass().getMethod("sendPacket", NmsUtil.getClass("Packet"));
		currentPacket.invoke(playerConnection, packet);
	}

	public static Class<?> getClass(String name) {
		return NmsUtil.getClass(name, 0);
	}

	public static Class<?> getClass(String name, int type) {
		String pkgName = type < 1 ? "net.minecraft.server" : "org.bukkit.craftbukkit";
		try {
			return Class.forName(
					pkgName + "." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Class<?> getIChatBaseComponent() {
		return NmsUtil.getClass("IChatBaseComponent");
	}

	public static Class<?> getArmorStand() {
		return NmsUtil.getClass("EntityArmorStand");
	}

	public static Class<?> getCraftWorld() {
		return NmsUtil.getClass("CraftWorld", 1);
	}

	public static Class<?> getWorld() {
		return NmsUtil.getClass("World");
	}

	public static Class<?> getPlayOutSpawnEntityLiving() {
		return NmsUtil.getClass("PacketPlayOutSpawnEntityLiving");
	}

	public static Class<?> getEntityLiving() {
		return NmsUtil.getClass("EntityLiving");
	}

	public static Class<?> getEntity() {
		return NmsUtil.getClass("Entity");
	}

	public static Class<?> getPlayOutEntityDestroy() {
		return NmsUtil.getClass("PacketPlayOutEntityDestroy");
	}

	public static Class<?> getDataWatcher() {
		return NmsUtil.getClass("DataWatcher");
	}

	public static Class<?> getPlayOutEntityMetadata() {
		return NmsUtil.getClass("PacketPlayOutEntityMetadata");
	}

	public static Class<?> getPlayOutEntityTeleport() {
		return NmsUtil.getClass("PacketPlayOutEntityTeleport");
	}

	public static Object getHandle(Object object) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		return object.getClass().getMethod("getHandle").invoke(object);
	}

	public static Object getSpawnEntityPacket(Object entity) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return NmsUtil.getPlayOutSpawnEntityLiving().getConstructor(NmsUtil.getEntityLiving()).newInstance(entity);
	}

	public static Object getDestroyEntityPacket(int id) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		return NmsUtil.getPlayOutEntityDestroy().getConstructor(int[].class).newInstance((Object) new int[] { id });
	}

	public static int getEntityId(Object entity) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		return (int) NmsUtil.getArmorStand().getMethod("getId").invoke(entity);
	}
}
