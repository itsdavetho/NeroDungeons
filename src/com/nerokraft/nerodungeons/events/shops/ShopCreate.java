package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ShopCreate extends Shop {
	private Player player;
	private ItemFrame frame;
	private NeroDungeons instance;

	public ShopCreate(Player player, ItemFrame frame, NeroDungeons instance) {
		super(frame.getLocation(), null, player.getUniqueId(), player.getName(), null, -1, -1, false,
				Currency.REWARD_POINTS);
		this.player = player;
		this.frame = frame;
		this.instance = instance;
		selectItem();
	}

	public void selectItem() {
		player.sendMessage("Shop editing mode entered.");
		player.sendMessage("Place an item on the frame to proceed");
		player.sendMessage("Right click while sneaking to cancel");
	}

	public boolean waitingForItem() {
		return (super.getMaterial() == null);
	}

	public boolean waitingForChest() {
		return (super.getChest().getType().equals(Material.CHEST)) == false;
	}

	public void initialize(PlayerInteractEntityEvent event, ShopInteract shopInteract) {
		boolean creatingShop = shopInteract.isCreating(player);
		if (creatingShop) {
			Material itemInHand = player.getInventory().getItemInMainHand().getType();
			if (waitingForItem() && itemInHand != null) {
				ItemStack stack = new ItemStack(itemInHand, 1);
				Utils.sendMessage("Set item to " + stack.getItemMeta().getDisplayName(), ChatColor.GREEN, player);
				super.setMaterial(stack.getType());
				frame.setItem(stack);
				TextComponent t = new TextComponent(
						"[NeroShop] Click here or type /nd cs <amount> <cost-each> [money] to finish configuring");
				t.setColor(ChatColor.BLUE);
				t.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/nd cs 1 "));
				player.spigot().sendMessage(t);
			}
		}
		event.setCancelled(true);
	}

	public void insertShop() {
		if (super.getAmount() > 0 && super.getCost() > 0) {
			Block block = player.getWorld().getBlockAt(super.getFrameLocation());
			Location loca = super.getChestLocation();
			System.out.println(loca);
			Shop shop = new Shop(block.getLocation(), loca, player.getUniqueId(), player.getName(), super.getMaterial(),
					super.getCost(), super.getAmount(), super.getAdminShop(), super.getCurrency());
			this.getInstance().getShops().addShop(shop);
			getInstance().getShops().getShopInteractions().removeShopCreator(player);
			this.instance = null;
			this.player = null;
			this.frame = null;
		}
	}

	private NeroDungeons getInstance() {
		return this.instance;
	}

	public static void toggleCreator(Player player, PlayerInteractEntityEvent event, ShopInteract inst, ItemStack item,
			ItemFrame frame) {
		if (!inst.isCreating(player) && player.isSneaking()) {
			inst.addShopCreator(player, frame);
			event.setCancelled(true);
		} else if (inst.isCreating(player)) {
			if (player.isSneaking()) {
				inst.removeShopCreator(player);
				event.setCancelled(true);
			} else {
				inst.getCreator(player).initialize(event, inst);
			}
		}
	}
}
