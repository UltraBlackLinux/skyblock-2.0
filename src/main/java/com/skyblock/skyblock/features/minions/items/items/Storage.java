package com.skyblock.skyblock.features.minions.items.items;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;

import com.skyblock.skyblock.utilities.Util;
import com.skyblock.skyblock.SkyblockPlayer;
import com.skyblock.skyblock.utilities.item.ItemBase;

import de.tr7zw.nbtapi.NBTItem;

import com.skyblock.skyblock.Skyblock;
import com.skyblock.skyblock.features.minions.MinionBase;
import com.skyblock.skyblock.features.minions.items.ListeningMinionItem;
import com.skyblock.skyblock.features.minions.items.MinionItem;
import com.skyblock.skyblock.features.minions.items.MinionItemType;

public class Storage extends ListeningMinionItem {
    public final int capacity;
    public Storage(String name, int capacity) {
        super(plugin.getItemHandler().getItem(name + "_STORAGE.json"), name.toLowerCase() + "_storage",  MinionItemType.STORAGE, false);
        this.capacity = capacity;
    }

    private MinionBase findAdjacentMinion(World w, Location loc) {
        ArrayList<Entity> nearbyEnts = new ArrayList<>(w.getNearbyEntities(loc, 1, 1, 1));
        for (Entity ent : nearbyEnts) {
            if (ent instanceof ArmorStand && ent.hasMetadata("minion_id")) {
                return Skyblock.getPlugin().getMinionHandler().getMinion(UUID.fromString(ent.getMetadata("minion_id").get(0).asString()));
            }
        }

        return null;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onMinionChestPlace(BlockPlaceEvent event) {
        if (SkyblockPlayer.getPlayer(event.getPlayer()).isOnIsland() && event.getBlockPlaced().getState() instanceof Chest
                && ((Chest) event.getBlockPlaced().getState()).getBlockInventory().getTitle().equals(getItem().getItemMeta().getDisplayName())) {
            MinionBase minion = findAdjacentMinion(event.getPlayer().getWorld(), event.getBlockPlaced().getLocation());
            if (minion == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "You need to place this next to a minion!");
                event.setCancelled(true);
                return;
            }

            if (minion.additionalStorage != null) {
                event.getPlayer().sendMessage(ChatColor.RED + "This minion already has a chest!");
                event.setCancelled(true);
                return;
            }
            
            System.out.println(minion.getUuid().toString());
            event.getBlockPlaced().setMetadata("minion_id", new FixedMetadataValue(Skyblock.getPlugin(), minion.getUuid().toString()));
            event.getBlockPlaced().setMetadata("capacity", new FixedMetadataValue(Skyblock.getPlugin(), capacity));
            //event.getBlockPlaced().setMetadata("minion_item", new FixedMetadataValue(Skyblock.getPlugin(), this));
            minion.additionalStorage = event.getBlockPlaced(); //Todo: Check if already has
        }
    }

    @EventHandler
    public void onMinionchestBreak(BlockBreakEvent event) {
        if (event.getBlock().hasMetadata("minion_id")) {
            MinionBase minion = Skyblock.getPlugin().getMinionHandler().getMinion(
                UUID.fromString(event.getBlock().getMetadata("minion_id").get(0).asString()));

            minion.additionalStorage = null;
            while(minion.inventory.size() > minion.getMaxStorage()/64) {
                event.getPlayer().getWorld().dropItem(event.getBlock().getLocation(), Util.toSkyblockItem(minion.inventory.get(minion.inventory.size() - 1))); //send to inventory?
                minion.inventory.remove(minion.inventory.size() - 1);
            }
        }
    }

    @EventHandler
    public void onMinonChestOpen(InventoryOpenEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Chest && ((Chest) holder).getBlockInventory().getTitle().equals(getItem().getItemMeta().getDisplayName())) {
            e.setCancelled(true);
            Chest chest = ((Chest) holder);
            openInventory(chest, (Player) e.getPlayer());
        }
    }

    public void openInventory(Chest chest, Player player) {
        MinionBase minion = Skyblock.getPlugin().getMinionHandler().getMinion(UUID.fromString(chest.getMetadata("minion_id").get(0).asString()));
        Inventory gui = Bukkit.createInventory(null, 27, "Minion Chest");
        Util.fillEmpty(gui);
        int lines = capacity / 64 / 3;
        int itemIndex = 0;
        if (lines == 1) {
            for (int i = 0; i < 3; ++i) {
                gui.setItem(12 + i, minion.getMaxStorage() / 64 + itemIndex < minion.getInventory().size()
                                ? minion.getInventory().get(minion.getMaxStorage() / 64 + itemIndex++)
                                : new ItemStack(Material.AIR));
            }
        }
        else {
            for (int x = 0; x < lines; ++x) { //horizontal
                for (int y = 0; y < 3; ++y) { //vertical 3-packs
                    gui.setItem((int) (x + y * 9 + 4 - Math.floor(lines/2)),
                                minion.getMaxStorage() / 64 + itemIndex < minion.getInventory().size()
                                ? minion.getInventory().get(minion.getMaxStorage() / 64 + itemIndex++)
                                : new ItemStack(Material.AIR));
                }
            }
        }
        player.openInventory(gui);
    }
}
