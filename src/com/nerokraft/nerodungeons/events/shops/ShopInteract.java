package com.nerokraft.nerodungeons.events.shops;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

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
				ShopDestroy.destroy(s);
				event.setCancelled(true);
			} else if(creators.containsKey(player) && creators.get(player).getFrameLocation().equals(event.getEntity().getLocation())) {
				this.removeShopCreator(player, true);
			}
		}
	}

	@EventHandler
	public void onHangingBreakEvent(HangingBreakEvent event) {
		if (event.getEntity() instanceof ItemFrame) {
			boolean cancel = false;
			Shop shop = instance.getShops()
					.getShop(event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation()));
			if (shop != null) {
				cancel = ShopDestroy.destroy(shop);
			} else {
				cancel = removeCreatorByLocation(event.getEntity().getLocation());
			}
			event.setCancelled(cancel);
		}
	}

	private boolean removeCreatorByLocation(Location location) {
		for (ShopCreate creator : creators.values()) {
			if (creator.getFrameLocation().equals(location)) {
				this.removeShopCreator(creator.getPlayer(), true);
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if (event.getEntity() instanceof ItemFrame) {
			boolean cancel = false;
			Block block = event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation());
			Shop shop = instance.getShops().getShop(block);
			if (event.getRemover() instanceof Player && shop != null) {
				Player player = (Player) event.getRemover();
				if(shop.getUUID().equals(player.getUniqueId()) && PlayerUtil.canBuild(shop.getFrameLocation(), player)) {
					cancel = ShopDestroy.destroy(shop);
				}
			}
			cancel = removeCreatorByLocation(event.getEntity().getLocation());
			event.setCancelled(cancel);
		}
	}

	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame) {
			final ItemFrame frame = (ItemFrame) entity;
			Block b = player.getWorld().getBlockAt(frame.getLocation());
			Shop shop = instance.getShops().getShop(b);
			if (shop == null) {
				boolean result = ShopCreate.handle(player, frame, b, event, this);
				event.setCancelled(result);
			} else {
				ShopBuy.buy(player, shop, frame, instance.getEconomy());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()
				&& PlayerUtil.hasPermission("nerodungeons.createshop", player)) {
			if (block.getType() == Material.CHEST) {
				ShopCreate creator = instance.getShops().getShopInteractions().getCreator(player);
				if (creator != null && creator.waitingForChest() && !creator.getAdminShop()) {
					Location chestLocation = block.getLocation();
					creator.setChestLocation(chestLocation);
					Output.sendMessage("Chest selected", ChatColor.DARK_PURPLE, player);
					event.setCancelled(true);
				} else if (creator != null && creator.getAdminShop()) {
					Output.sendMessage("You do not need to select a chest for an admin shop", ChatColor.DARK_BLUE,
							player);
					event.setCancelled(true);
				}
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (block.getType() == Material.CHEST) {
				Shop shop = instance.getShops().getShopByChest(block);
				if (shop != null) {
					if(shop.getUUID().equals(player.getUniqueId()) && PlayerUtil.canBuild(shop.getChestLocation(), player)) {
						ShopDestroy.destroy(shop);
					}
					event.setCancelled(true);
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
		removeShopCreator(player, true);
	}

	public void removeShopCreator(Player player, boolean notify) {
		if (notify) {
			Output.sendMessage("Cancelled shop creation", ChatColor.RED, player);
		}
		creators.remove(player);
	}

	public boolean isCreating(Player player) {
		return creators.containsKey(player);
	}

	public ShopCreate getCreator(Player player) {
		return isCreating(player) ? creators.get(player) : null;
	}
}
