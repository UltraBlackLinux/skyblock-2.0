package com.skyblock.skyblock.features.minions;

import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.skyblock.skyblock.Skyblock;
import com.skyblock.skyblock.SkyblockPlayer;
import com.skyblock.skyblock.features.crafting.gui.RecipeGUI;
import com.skyblock.skyblock.features.island.IslandManager;
import com.skyblock.skyblock.features.minions.items.MinionItem;
import com.skyblock.skyblock.features.minions.items.items.Storage;
import com.skyblock.skyblock.features.minions.items.MinionItemHandler;
import com.skyblock.skyblock.features.minions.items.MinionItemType;
import com.skyblock.skyblock.utilities.Util;
import com.skyblock.skyblock.utilities.item.ItemHandler;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public class MinionListener implements Listener {

    @EventHandler
    public void onRightClickMinion(PlayerArmorStandManipulateEvent event) {
        if (event.getPlayer() == null || event.getRightClicked() == null) return;

        if (!event.getRightClicked().hasMetadata("minion")) return;

        event.setCancelled(true);

        UUID minionId = UUID.fromString(event.getRightClicked().getMetadata("minion_id").get(0).asString());

        SkyblockPlayer player = SkyblockPlayer.getPlayer(event.getPlayer());

        if (player == null || !player.getBukkitPlayer().getWorld().getName().equals(IslandManager.ISLAND_PREFIX + player.getBukkitPlayer().getUniqueId().toString())) return;

        MinionBase minion = Skyblock.getPlugin().getMinionHandler().getMinion(minionId);

        if (minion == null) return;

        minion.showInventory(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getWhoClicked() == null || event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        
        MinionItemHandler mih = Skyblock.getPlugin().getMinionItemHandler();
        ItemStack current = event.getCurrentItem();
        SkyblockPlayer player = SkyblockPlayer.getPlayer((Player) event.getWhoClicked());
        boolean isMinionInv = event.getClickedInventory().getName().contains("Minion");
        Block targetBlock = player.getBukkitPlayer().getTargetBlock((Set<Material>) null, 4);
        boolean isMinionStorage = targetBlock.hasMetadata("minion_id"); 
        if (!player.getBukkitPlayer().getOpenInventory().getTopInventory().getName().contains("Minion")) return;


        MinionBase minion = null;
        if (!isMinionStorage) {
            for (MinionHandler.MinionSerializable serializable :
                    Skyblock.getPlugin().getMinionHandler().getMinions().get(player.getBukkitPlayer().getUniqueId())) {
                if (serializable.getBase().getGui().equals(player.getBukkitPlayer().getOpenInventory().getTopInventory())) {
                    minion = serializable.getBase();
                    break;
                }
            }
        }
        else {
            for (MinionHandler.MinionSerializable serializable :
                    Skyblock.getPlugin().getMinionHandler().getMinions().get(player.getBukkitPlayer().getUniqueId())) {
                if (serializable.getBase().additionalStorage.getLocation().equals(targetBlock.getLocation())) {
                    minion = serializable.getBase();
                    break;
                }
            }
        }
        if (minion == null) return;

        event.setCancelled(true);

        if (isMinionInv || isMinionStorage) {
            if (mih.isRegistered(current) && !isMinionStorage) { //take upgrades
                boolean remove = mih.getRegistered(current).onItemClick(player.getBukkitPlayer(), current);
                if (remove) {
                    for (int i = 0; i < minion.minionItems.length; ++i) {
                        if (minion.minionItems[i] != null && minion.minionItems[i].isThisItem(current)) {
                            player.getBukkitPlayer().getInventory().addItem(mih.getRegistered(current).getItem());
                            minion.minionItems[i].onUnEquip(minion);
                            minion.minionItems[i] = null;
                            break;
                        }
                    }
                    minion.showInventory(player);
                }
            }
            else {
                if (current.getItemMeta().hasDisplayName()) {
                    if (current.getItemMeta().getDisplayName().contains("Collect All")) {
                        minion.collectAll(player);
                        return;
                    }

                    if (current.getItemMeta().getDisplayName().contains("Next Tier") && minion.getLevel() < 11) {
                        new RecipeGUI(Skyblock.getPlugin().getItemHandler().getItem(minion.getMaterial().name() + "_GENERATOR_" + (minion.getLevel() + 1) + ".json"), null, minion.getGui(), player.getBukkitPlayer()).show(player.getBukkitPlayer());
                    }

                    if (current.getItemMeta().getDisplayName().contains("Quick-Upgrade") && current.getItemMeta().hasLore() && current.getItemMeta().getLore().stream().anyMatch((s) -> s.contains("Click to upgrade!"))) {
                        NBTItem nbt = new NBTItem(current);
                        minion.upgrade(player, minion.level + 1, nbt.getString("item"), nbt.getInteger("amount"));
                    }
                }

                if (current.getType().equals(Material.BEDROCK)) minion.pickup(player, minion.getMinion().getLocation());

                if (isMinionStorage && current.getType() != Material.STAINED_GLASS_PANE) { //withdraw chest items
                    if (player.getBukkitPlayer().getInventory().firstEmpty() == -1) {
                        player.getBukkitPlayer().sendMessage(ChatColor.RED + "Your inventory does not have enough free space to add all items!");
                        return;
                    }

                    minion.inventory.remove(minion.inventory.lastIndexOf(current));
                    player.getBukkitPlayer().getInventory().addItem(Util.toSkyblockItem(current)); //BUG: weird collection behviour

                    Item item = player.getBukkitPlayer().getWorld().dropItem(minion.getMinion().getLocation(), Util.toSkyblockItem(current));
                    item.setPickupDelay(Integer.MAX_VALUE);
                    Bukkit.getPluginManager().callEvent(new PlayerPickupItemEvent(player.getBukkitPlayer(), item, 0));
                    Util.delay(item::remove, 1);

                    //player.getBukkitPlayer().updateInventory();
                    player.getBukkitPlayer().closeInventory();
                    //((Storage) minion.additionalStorage.getMetadata("minion_item").get(0)).openInventory((Chest) minion.additionalStorage, player.getBukkitPlayer());
                }
                else
                    minion.collect(player, event.getSlot());
            }
        }
        else if (mih.isRegistered(current)) { //add upgrades
            MinionItem item = mih.getRegistered(current);

            if (!item.canStack) {
                for (int i = 0; i < minion.minionItems.length; ++i) {
                    if (minion.minionItems[i] != null && minion.minionItems[i].isThisItem(current)) return;
                }
            }

            for (int i : minion.getItemSlots(item.getType()) ) {
                if (minion.minionItems[i] == null) {
                    minion.minionItems[i] = item;
                    minion.minionItems[i].onEquip(minion);
                    minion.showInventory(player);
                    break;
                }
            }

            if (current.getAmount() > 1) 
                current.setAmount(current.getAmount() - 1);
            else
                player.getBukkitPlayer().getInventory().clear(event.getSlot());
        }
    }

}
