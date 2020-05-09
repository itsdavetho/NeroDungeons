package com.nerokraft.nerodungeons.events.shops;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
//import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class ShopInteract implements Listener {
	private final NeroDungeons instance;
	private HashMap<Player, ShopCreate> creators = new HashMap<Player, ShopCreate>();

	public ShopInteract(NeroDungeons inst) {
		instance = inst;
	}

	@EventHandler
	public void entityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof ItemFrame) {
			Player player = (Player) event.getDamager();
			Block block = player.getWorld().getBlockAt(event.getEntity().getLocation());
			Shop s = instance.getShops().getShop(block);
			if (s != null) {
				event.setCancelled(ShopDestroy.destroy(player, s, instance) == false);
			}
		}
	}

	@EventHandler
	public void onHangingBreakEvent(HangingBreakEvent event) {
		if (event.getEntity() instanceof ItemFrame) {
			Shop shop = instance.getShops()
					.getShop(event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation()));
			if (event.getCause() == RemoveCause.PHYSICS || event.getCause() == RemoveCause.EXPLOSION
					|| event.getCause() == RemoveCause.OBSTRUCTION) {
				if (shop != null) {
					ShopDestroy.destroy(shop, instance);
				}
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		Player player = (Player) event.getRemover();
		Block block = player.getWorld().getBlockAt(event.getEntity().getLocation());
		Shop shop = instance.getShops().getShop(block);
		if (event.getRemover() instanceof Player && shop != null) {
			event.setCancelled(ShopDestroy.destroy(player, shop, instance) == false);
		} else if (event.getRemover() instanceof Projectile && shop != null) {
			event.getRemover().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();

		if (entity instanceof ItemFrame) {
			// Location l = entity.getLocation();
			final ItemFrame frame = (ItemFrame) entity;
			final ItemStack item = frame.getItem();
			Block b = player.getWorld().getBlockAt(frame.getLocation());
			Utils.verboseOutput(player, b.getBlockData().getMaterial().name(), instance);
			if (Utils.hasPermission("nerodungeons.createshop", player) && instance.getShops().getShop(b) == null && Utils.canBuild(b.getLocation(), player)) {
				ShopCreate.toggleCreator(player, event, this, item, frame);
			} else {
				if (Utils.hasPermission("nerodungeons.buy", player)) {
					Shop shop = instance.getShops().getShop(b);
					ShopBuy.buy(player, shop);
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		Material itemInHand = player.getInventory().getItemInMainHand().getType();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking() && Utils.hasPermission("nerodungeons.createshop", player)) {
			if (block.getType() == Material.CHEST) {
				ShopCreate creator = instance.getShops().getShopInteractions().getCreator(player);
				if (creator != null && creator.waitingForChest()) {
					boolean adminShop = itemInHand.equals(Material.BLAZE_ROD)
							&& Utils.hasPermission("nerodungeons.admin", player);
					if (!adminShop) {
						Location chestLocation = block.getLocation();
						if (Utils.canBuild(chestLocation, player)) {
							Utils.sendMessage("Chest selected", ChatColor.DARK_PURPLE, player);
							creator.setChestLocation(chestLocation);
						}
						event.setCancelled(true);
					} else if (adminShop) {
						creator.setAdminShop(true);
					}
					event.setCancelled(true);
				}
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Shop shop = instance.getShops().getShopByChest(block);
			event.setCancelled(ShopDestroy.destroy(player, shop, instance) == false);
		}

	}

	public void addShopCreator(Player player, ItemFrame frame) {
		if (isCreating(player)) {
			Bukkit.getLogger().warning("This probably should not have happened");
			return;
		}
		ShopCreate creator = new ShopCreate(player, frame, instance);
		creators.put(player, creator);
	}

	public void removeShopCreator(Player player) {
		creators.remove(player);
	}

	public boolean isCreating(Player player) {
		return creators.containsKey(player);
	}

	public ShopCreate getCreator(Player player) {
		return isCreating(player) ? creators.get(player) : null;
	}
}
