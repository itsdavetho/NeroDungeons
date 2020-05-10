package com.nerokraft.nerodungeons.events.shops;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nerokraft.nerodungeons.NeroDungeons;
import com.nerokraft.nerodungeons.shops.Currency;
import com.nerokraft.nerodungeons.shops.Shop;
import com.nerokraft.nerodungeons.utils.Items;
import com.nerokraft.nerodungeons.utils.Output;
import com.nerokraft.nerodungeons.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ShopCreate extends Shop {
	private Player player;
	private ItemFrame frame;
	private final NeroDungeons plugin;

	public ShopCreate(Player player, ItemFrame frame, NeroDungeons instance) {
		super(frame.getLocation(), null, player.getUniqueId(), player.getName(), null, -1, -1, false,
				Currency.REWARD_POINTS, instance.getShops());
		this.player = player;
		this.frame = frame;
		this.plugin = instance;
		this.informPlayer();
	}

	public void informPlayer() {
		player.sendMessage("Shop editing mode entered.");
		player.sendMessage("Place an item on the frame to proceed");
		player.sendMessage("Right click while sneaking to cancel");
	}

	public void updateItem(ShopInteract shopInteract) {
		boolean creatingShop = shopInteract.isCreating(player);
		if (creatingShop) {
			Material itemInHand = player.getInventory().getItemInMainHand().getType();
			if (itemInHand != null && itemInHand != Material.AIR) {
				ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
				ItemStack stack = new ItemStack(itemInHand, 1);
				stack.setItemMeta(meta);
				Output.sendMessage("Set item to " + Items.getName(itemInHand.name()), ChatColor.GREEN, player);
				Items.removeFromInventory(stack, 1, player.getInventory());
				super.setMaterial(stack.getType());
				frame.setItem(stack);
				TextComponent t = new TextComponent(
						"[NeroShop] Click here or type /nd cs <amount> <cost-each> [money] to finish configuring");
				t.setColor(ChatColor.BLUE);
				t.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/nd cs 1 "));
				player.spigot().sendMessage(t);
			}
		}
	}

	public static boolean handle(Player player, ItemFrame frame, Block b, PlayerInteractEntityEvent event, ShopInteract si) {
		boolean canCreateShop = PlayerUtil.hasPermission("nerodungeons.createshop", player) && PlayerUtil.canBuild(b.getLocation(), player);
		if(!canCreateShop) {
			return false;
		}
		boolean sneaking = player.isSneaking();
		ShopCreate creator = si.getCreator(player);
		if(creator == null && sneaking) {
			creator = si.addShopCreator(player, frame);
			return true;
		} else if(creator != null) {
			Material itemInHand = player.getInventory().getItemInMainHand().getType();
			boolean editAdminShop = itemInHand.equals(Material.BLAZE_ROD)
					&& PlayerUtil.hasPermission("nerodungeons.admin", player);
			if(editAdminShop && sneaking) {
				creator.setAdminShop(!creator.getAdminShop());
				String type = creator.getAdminShop() ? "an admin" : "a regular";
				ChatColor color = creator.getAdminShop() ? ChatColor.GOLD : ChatColor.BLUE;
				Output.sendMessage("Creating " + type + " shop", color, player);
				return true;
			} else if(!editAdminShop && sneaking) {
				si.removeShopCreator(player);
				return false;
			} else if(!sneaking) {
				creator.updateItem(si);
				return true;
			}
		}
		
		return false;
	}
	
	public void insertShop() {
		if (super.getAmount() > 0 && super.getCost() > 0) {
			if(super.getMaterial() == null) { // why would this happen ? this should prevent it anyways.
				super.setMaterial(Material.COBBLESTONE);
			}
			Shop shop = new Shop(super.getFrameLocation(), super.getChestLocation(), player.getUniqueId(), player.getName(),
					super.getMaterial(), super.getCost(), super.getAmount(), super.getAdminShop(), super.getCurrency(), getPlugin().getShops());
			this.getPlugin().getShops().addShop(shop);
			Output.sendMessage("Shop created", ChatColor.GREEN, player);
			this.getPlugin().getShops().getShopInteractions().removeShopCreator(player, false);
			this.player = null;
			this.frame = null;
		}
	}
	
	private NeroDungeons getPlugin() {
		return this.plugin;
	}
	
	public boolean waitingForItem() {
		return (super.getMaterial() == null);
	}

	public boolean waitingForChest() {
		return ((super.getChest().getType().equals(Material.CHEST)) == false) && this.getAdminShop() == false;
	}
}
