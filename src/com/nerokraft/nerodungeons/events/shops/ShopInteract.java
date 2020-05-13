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
import org.bukkit.plugin.Plugin;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.holograms.Hologram;
import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;

public class ShopInteract implements Listener {
	private final NeroDungeons instance;
	private HashMap<Player, ShopCreate> creators = new HashMap<Player, ShopCreate>();
	private HashMap<Player, ShopGui> guis = new HashMap<Player, ShopGui>();

	public ShopInteract(NeroDungeons inst) {
		instance = inst;
	}

	public ShopGui inGui(Player player) {
		return this.guis.containsKey(player) ? this.guis.get(player) : null;
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
			} else if (creators.containsKey(player)
					&& creators.get(player).getFrameLocation().equals(event.getEntity().getLocation())) {
				this.removeShopCreator(player, true);
				event.setCancelled(true);
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

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if (event.getEntity() instanceof ItemFrame) {
			boolean cancel = false;
			Block block = event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation());
			Shop shop = instance.getShops().getShop(block);
			if (event.getRemover() instanceof Player && shop != null) {
				Player player = (Player) event.getRemover();
				if (shop.getUUID().equals(player.getUniqueId())
						&& PlayerUtil.canBuild(shop.getFrameLocation(), player)) {
					cancel = ShopDestroy.destroy(shop);
				}
			}
			cancel = removeCreatorByLocation(event.getEntity().getLocation());
			event.setCancelled(cancel);
		}
	}

	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		final Player player = event.getPlayer();
		final Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame) {
			final ItemFrame frame = (ItemFrame) entity;
			Block b = player.getWorld().getBlockAt(frame.getLocation());
			Shop shop = instance.getShops().getShop(b);
			if (shop == null) {
				boolean result = ShopCreate.handle(player, frame, b, event, this);
				event.setCancelled(result);
			} else {
				if(!player.isSneaking()) {
					ShopGui gui = new ShopGui(shop, frame, player);
					gui.show();
					guis.put(player, gui);
				} else { 
					showHologram(shop, player, frame);
				}
				event.setCancelled(true);
			}
		}
	}

	private void showHologram(Shop shop, Player player, ItemFrame frame) {
		String currencyName = shop.getCurrency() == Currency.REWARD_POINTS ? "reward points" : "gold";
		String text = shop.getName() + " [" + shop.getCost() + " " + currencyName + " for " + shop.getAmount() + "]";
		try {
			final Hologram h = instance.getHolograms().createHologram(PlayerUtil.nudgeForward(0.23d, frame, frame.getLocation()), text);
			h.show(player);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)instance, new Runnable() {
				public void run() {
					try {
						h.hide(player);
					} catch (Exception e) {
						Output.sendDebug(e.getMessage(), ChatColor.RED, player);
						e.printStackTrace();
					}
				}
			}, (20l*3l));
		} catch(Exception e) {
			Output.sendDebug(e.getMessage(), ChatColor.RED, player);
			e.printStackTrace();
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
					Output.sendMessage(instance.getMessages().getString("ShopChestSelected"), ChatColor.DARK_PURPLE,
							player);
					event.setCancelled(true);
				} else if (creator != null && creator.getAdminShop()) {
					Output.sendMessage(instance.getMessages().getString("AdminShopNoChest"), ChatColor.DARK_BLUE,
							player);
					event.setCancelled(true);
				}
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (block.getType() == Material.CHEST) {
				Shop shop = instance.getShops().getShopByChest(block);
				if (shop != null) {
					if (shop.getUUID().equals(player.getUniqueId())
							&& PlayerUtil.canBuild(shop.getChestLocation(), player)) {
						ShopDestroy.destroy(shop);
					}
					event.setCancelled(true);
				}
			}
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
			Output.sendMessage(instance.getMessages().getString("ShopCreatorCancelled"), ChatColor.RED, player);
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
