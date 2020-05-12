package com.nerokraft.nerodungeons.holograms;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

import com.nerokraft.nerodungeons.NeroDungeons;

public class Holograms {
	private final NeroDungeons plugin;
	private Set<Hologram> holograms = new HashSet<Hologram>();

	public Holograms(NeroDungeons plugin) {
		this.plugin = plugin;
	}

	public Hologram createHologram(Location location, String text)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException, NoSuchFieldException {
		Hologram hologram = new Hologram(text, location, this);
		holograms.add(hologram);
		return hologram;
	}

	public void removeHologram(Hologram h) {
		this.holograms.remove(h);
	}

	public Set<Hologram> getHolograms() {
		return this.holograms;
	}

	public NeroDungeons getPlugin() {
		return this.plugin;
	}
}
