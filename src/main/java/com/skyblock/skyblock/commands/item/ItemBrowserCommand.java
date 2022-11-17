package com.skyblock.skyblock.commands.item;

import com.skyblock.skyblock.Skyblock;
import com.skyblock.skyblock.utilities.command.Command;
import com.skyblock.skyblock.utilities.command.annotations.Description;
import com.skyblock.skyblock.utilities.command.annotations.RequiresPlayer;
import com.skyblock.skyblock.utilities.command.annotations.Usage;
import com.skyblock.skyblock.utilities.gui.Gui;
import com.skyblock.skyblock.utilities.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresPlayer
@Usage(usage = "/sb itembrowser <page>/search <name>")
@Description(description = "Shows a list of all items in the game")
public class ItemBrowserCommand implements Command {

    @Override
    public void execute(Player player, String[] args, Skyblock plugin) {
        if (args[0].equalsIgnoreCase("search")) {

        } else {
            try {
                int page = Integer.parseInt(args[0]) - 1;
                Gui itemBrowser = new Gui("ItemBrowser", 54, new HashMap<>());

                int start = page * 45;
                int end = page * 45 + 45;

                int index = 0;
                int setItemIndex = 0;
                List<ItemStack> items = new ArrayList<>();

                for (Map.Entry<String, ItemStack> entry : plugin.getItemHandler().getItems().entrySet()) {
                    items.add(entry.getValue());
                }

                for (int i = start; i < end; i++) {
                    try {
                        ItemStack item = items.get(i);
                        itemBrowser.addItem(setItemIndex, item);
                        itemBrowser.clickEvents.put(item.getItemMeta().getDisplayName(), () -> {
                            player.getInventory().addItem(item);
                        });
                        setItemIndex++;
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }

                itemBrowser.fillEmpty(new ItemBuilder(" ", Material.STAINED_GLASS_PANE, (short) 15).toItemStack());
                itemBrowser.addItem(53, new ItemBuilder(ChatColor.GREEN + "Next Page", Material.ARROW).toItemStack());
                itemBrowser.clickEvents.put(ChatColor.GREEN + "Next Page", () -> {
                    player.closeInventory();
                    player.performCommand("sb itembrowser " + (page + 2));
                    itemBrowser.clickEvents.clear();
                });

                itemBrowser.show(player);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Not a number");
            }
        }
    }
}
