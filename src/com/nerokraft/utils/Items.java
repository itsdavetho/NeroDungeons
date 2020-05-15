package com.nerokraft.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Items {
	public static void removeFromInventory(ItemStack stack, int remove, Inventory inv) {
		int idx = 0;
		while (remove > 0 && idx < inv.getSize()) {
			ItemStack item = inv.getItem(idx);
			if (item != null && item.isSimilar(stack)) {
				int amountIn = item.getAmount();
				if (amountIn > remove) {
					item.setAmount(amountIn - remove);
					remove = 0;
				} else if (item.getAmount() <= remove) {
					inv.clear(idx);
					remove = remove - amountIn;
				}
			}
			idx++;
		}
	}

	public static int invSpace(Inventory inv, Material m) {
		int count = 0;
		ItemStack[] items = inv.getStorageContents();
		for (ItemStack item : items) {
			if (item == null || item.getType() == Material.AIR) {
				count += m.getMaxStackSize();
			} else if (item.getType().equals(m) && item.getAmount() < m.getMaxStackSize()) {
				count += m.getMaxStackSize() - item.getAmount();
			}
		}
		return count;
	}

	public static String getName(String id) {
		return id.replace("_", " ").toLowerCase();
	}
}
