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
			System.out.println("destroy44");
			if (s != null) {
				event.setCancelled(ShopDestroy.destroy(player, s, instance) == false);
				System.out.println("destroy4");
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
					System.out.println("system4");
				}
			} else {
				event.setCancelled(shop != null);
				System.out.println("destroy3");
			}
		}
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if (event.getEntity() instanceof ItemFrame) {
			Block block = event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation());
			Shop shop = instance.getShops().getShop(block);
			if (event.getRemover() instanceof Player && shop != null) {
				Player player = (Player) event.getRemover();
				event.setCancelled(ShopDestroy.destroy(player, shop, instance) == false);
				System.out.println("destroy7");
			} else if (shop != null) {
				event.setCancelled(true);
				System.out.println("destroy8");
			} 
			System.out.println("destroy5");
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
			if (Utils.hasPermission("nerodungeons.createshop", player) && instance.getShops().getShop(b) == null
					&& Utils.canBuild(b.getLocation(), player)) {
				ShopCreate.toggleCreator(player, event, this, item, frame);
			} else {
				if (Utils.hasPermission("nerodungeons.buy", player)) {
					Shop shop = instance.getShops().getShop(b);
					if(shop != null) {
						ShopBuy.buy(player, shop);
					}
				}
				System.out.println("system3");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		Material itemInHand = player.getInventory().getItemInMainHand().getType();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()
				&& Utils.hasPermission("nerodungeons.createshop", player)) {
			if (block.getType() == Material.CHEST) {
				ShopCreate creator = instance.getShops().getShopInteractions().getCreator(player);
				if (creator != null && creator.waitingForChest()) {
					boolean adminShop = itemInHand.equals(Material.BLAZE_ROD)
							&& Utils.hasPermission("nerodungeons.admin", player);
					Location chestLocation = block.getLocation();
					creator.setChestLocation(chestLocation);
					if (adminShop) {
						creator.setAdminShop(true);
						Utils.sendMessage("Admin shop selected", ChatColor.GOLD, player);
					} else {
						Utils.sendMessage("Chest selected", ChatColor.DARK_PURPLE, player);
					}
					System.out.println("system2");
					event.setCancelled(true);
				}
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (block.getType() == Material.CHEST) {
				System.out.println("destroy9");
				Shop shop = instance.getShops().getShopByChest(block);
				if (shop != null) {
					System.out.println("system3");
					event.setCancelled(ShopDestroy.destroy(player, shop, instance) == false);
				}
			}
		}

	}

	public ShopCreate addShopCreator(Player player, ItemFrame frame) {
		if (isCreating(player)) {
			Bukkit.getLogger().warning("This probably should not have happened");
			return null;
		}
		ShopCreate creator = new ShopCreate(player, frame, instance);
		creators.put(player, creator);
		return creator;
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
