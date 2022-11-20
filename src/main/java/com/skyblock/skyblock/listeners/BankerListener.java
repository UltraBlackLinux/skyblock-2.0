package com.skyblock.skyblock.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankerListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

        List<String> titles = new ArrayList<>(Arrays.asList("Personal Bank Account", "Bank Deposit", "Bank Withdrawal"));

        if (!titles.contains(event.getClickedInventory().getTitle())) return;

        event.setCancelled(true);

        ItemStack stack = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (stack.getType().equals(Material.BARRIER)) {
            player.closeInventory();
            return;
        } else if (stack.getType().equals(Material.ARROW)) {
            player.performCommand("sb banker");
            return;
        }

        if (event.getClickedInventory().getTitle().equals("Personal Bank Account")) {
            if (stack.getType().equals(Material.CHEST)) player.performCommand("sb banker deposit");
            else if (stack.getType().equals(Material.DROPPER)) player.performCommand("sb banker withdraw");
        } else if (event.getClickedInventory().getTitle().equals("Bank Deposit")) {
            if (stack.getType().equals(Material.CHEST)) {
                if (stack.getAmount() == 64) player.performCommand("sb deposit all");
                else if (stack.getAmount() == 32) player.performCommand("sb deposit half");
            }
        } else if (event.getClickedInventory().getTitle().equals("Bank Withdrawal")) {
            if (stack.getType().equals(Material.DROPPER)) {
                if (stack.getAmount() == 64) player.performCommand("sb withdraw all");
                else if (stack.getAmount() == 32) player.performCommand("sb withdraw half");
                else if (stack.getAmount() == 1) player.performCommand("sb withdraw 20%");
            }
        }
    }

}
