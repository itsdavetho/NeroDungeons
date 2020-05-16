package com.nerokraft.events.shops;

import java.util.ResourceBundle;

import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nerokraft.NeroKraft;
import com.nerokraft.shops.Currencies;
import com.nerokraft.shops.Shop;
import com.nerokraft.utils.Items;
import com.nerokraft.utils.Output;
import com.nerokraft.utils.PlayerUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ShopCreate extends Shop {
	private Player player;
	private ItemFrame frame;
	private final NeroKraft plugin;

	public ShopCreate(Player player, ItemFrame frame, NeroKraft instance) {
		super(frame.getLocation(), null, player.getUniqueId(), player.getName(), null, -1, -1, false,
				Currencies.REWARD_POINTS, instance.getShops(), true, -1);
		this.player = player;
		this.frame = frame;
		this.plugin = instance;
		this.informPlayer();

	}

	public void informPlayer() {
		ResourceBundle rb = plugin.getMessages();
		player.sendMessage(rb.getString("ShopEdit1"));
		player.sendMessage(rb.getString("ShopEdit2"));
		player.sendMessage(rb.getString("ShopEdit3"));
	}

	public void updateItem(ShopInteract shopInteract) {
		boolean creatingShop = shopInteract.isCreating(player);
		if (creatingShop) {
			Material itemInHand = player.getInventory().getItemInMainHand().getType();
			if (itemInHand != null && itemInHand != Material.AIR) {
				if(frame.getItem() != null && frame.getItem().getType() != Material.AIR) { 
					ItemStack onFrame = super.getItem(frame);
					player.getInventory().addItem(onFrame);
				}
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

	public static boolean handle(Player player, ItemFrame frame, PlayerInteractEntityEvent event,
			ShopInteract si) {
		boolean canCreateShop = PlayerUtil.hasPermission("nerodungeons.createshop", player)
				&& PlayerUtil.canBuild(frame.getLocation(), player);
		if (!canCreateShop) {
			return false;
		}
		boolean sneaking = player.isSneaking();
		ShopCreate creator = si.getCreator(player);
		if (creator == null && sneaking) {
			creator = si.addShopCreator(player, frame);
			return true;
		} else if (creator != null) {
			Material itemInHand = player.getInventory().getItemInMainHand().getType();
			boolean editAdminShop = itemInHand.equals(Material.BLAZE_ROD)
					&& PlayerUtil.hasPermission("nerodungeons.admin", player);
			if (editAdminShop && sneaking) {
				creator.setAdminShop(!creator.getAdminShop());
				String shopType = creator.getAdminShop() ? creator.getPlugin().getMessages().getString("ShopAdmin")
						: creator.getPlugin().getMessages().getString("ShopRegular");
				ChatColor color = creator.getAdminShop() ? ChatColor.GOLD : ChatColor.BLUE;
				Output.sendMessage(creator.getPlugin().getMessages().getString("ShopSetType").replace("%s", shopType),
						color, player);
				return true;
			} else if (itemInHand.equals(Material.STICK) && sneaking) {
				creator.setCanSell(!creator.getCanSell());
				Output.sendMessage(
						creator.getCanSell() ? creator.getPlugin().getMessages().getString("ShopCanSell")
								: creator.getPlugin().getMessages().getString("ShopCantSell"),
						creator.getCanSell() ? ChatColor.GREEN : ChatColor.RED, player);
				return true;
			} else if (!editAdminShop && sneaking) {
				si.removeShopCreator(player);
				return false;
			} else if (!sneaking) {
				creator.updateItem(si);
				return true;
			}
		}

		return false;
	}

	public Shop insertShop() {
		if (super.getAmount() > 0 && super.getCost() > 0) {
			if (super.getMaterial() == null) { // why would this happen ? this should prevent it anyways.
				super.setMaterial(Material.COBBLESTONE);
			}
			Shop shop = new Shop(super.getFrameLocation(), super.getChestLocation(), player.getUniqueId(),
					player.getName(), super.getMaterial(), super.getCost(), super.getAmount(), super.getAdminShop(),
					super.getCurrency(), getPlugin().getShops(), super.getCanSell(), -1);
			Output.sendMessage("Shop created", ChatColor.GREEN, player);
			this.getPlugin().getShops().getShopInteractions().removeShopCreator(player, false);
			long shopId = this.getPlugin().getShops().addShop(shop, player);
			getPlugin().getShops().getShops().put(shopId, shop);
			getPlugin().getShops().setShopMeta(frame, shopId);
			this.player = null;
			this.frame = null;
			return shop;
		}
		return null;
	}

	private NeroKraft getPlugin() {
		return this.plugin;
	}

	public boolean waitingForItem() {
		return (super.getMaterial() == null);
	}

	public boolean waitingForChest() {
		if(getChestLocation() == null && !this.getAdminShop()) {
			return true;
		} else if(getChestLocation() != null && !this.getAdminShop()) {
			boolean isChest = getChestLocation().getBlock().getType().equals(Material.CHEST);
			return !isChest;
		}
		return false;
	}
}
