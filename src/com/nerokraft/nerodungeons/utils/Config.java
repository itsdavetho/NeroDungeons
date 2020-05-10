package com.nerokraft.nerodungeons.utils;

import java.io.File;
import java.io.FilenameFilter;

import com.nerokraft.nerodungeons.NeroDungeons;

public class Config {
	public static File[] getConfigs(String configName, NeroDungeons inst, String extension) {
		File config = new File(inst.getDataFolder(), configName + "/");
		File[] configs;
		if (!extension.isEmpty()) {
			configs = config.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(extension);
				}
			});
		} else {
			configs = config.listFiles();
		}
		return configs;
	}
}
