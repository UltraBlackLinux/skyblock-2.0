package com.skyblock.skyblock.features.fairysouls;

import com.skyblock.skyblock.Skyblock;
import com.skyblock.skyblock.SkyblockPlayer;
import com.skyblock.skyblock.enums.SkyblockStat;
import com.skyblock.skyblock.utilities.Util;
import com.skyblock.skyblock.utilities.gui.Gui;
import com.skyblock.skyblock.utilities.item.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class TiaGUI extends Gui {

    public TiaGUI(Player opener) {
        super("Fairy", 54, new HashMap<String, Runnable>() {{
            put(ChatColor.GREEN + "Exchange Fairy Souls", () -> {
                try {
                    File file = new File(Skyblock.getPlugin().getDataFolder(), "fairy_souls.json");
                    JSONArray rewards = (JSONArray) ((JSONObject) new JSONParser().parse(new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)))).get("rewards");

                    SkyblockPlayer player = SkyblockPlayer.getPlayer(opener);
                    int claimed = (int) player.getValue("fairySouls.claimed");
                    int found = ((ArrayList<Location>) player.getValue("fairySouls.found")).size() - claimed;

                    if (found < 5) return;

                    for (Object o : rewards) {
                        JSONObject json = (JSONObject) o;
                        long amount = (long) json.get("amount");

                        if (amount == claimed + 5) {
                            long health = (long) json.get("health");
                            long defense = (long) json.get("defense");
                            long strength = (long) json.get("strength");
                            long speed = (long) json.get("speed");

                            player.addStat(SkyblockStat.MAX_HEALTH, (int) health);
                            player.addStat(SkyblockStat.HEALTH, (int) health);
                            player.addStat(SkyblockStat.DEFENSE, (int) defense);
                            player.addStat(SkyblockStat.STRENGTH, (int) strength);
                            player.addStat(SkyblockStat.SPEED, (int) speed);

                            opener.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + ChatColor.BOLD + "FAIRY SOUL EXCHANGE\n" +
                                    ChatColor.RESET + ChatColor.WHITE + "You gained permanent stat boosts!");
                            opener.sendMessage(" ");
                            opener.sendMessage(ChatColor.DARK_GRAY + "  +" + ChatColor.GREEN + health + " HP " + ChatColor.RED + "❤ Health");
                            opener.sendMessage(ChatColor.DARK_GRAY + "  +" + ChatColor.GREEN + defense + " " + ChatColor.GREEN + "❈ Defense");
                            opener.sendMessage(ChatColor.DARK_GRAY + "  +" + ChatColor.GREEN + strength + " " + ChatColor.RED + "❁ Strength");
                            if (speed > 0) opener.sendMessage(ChatColor.DARK_GRAY + "  +" + ChatColor.GREEN + speed + " " + ChatColor.WHITE + "✦ Speed");

                            opener.playSound(opener.getLocation(), Sound.FIREWORK_TWINKLE, 10, 1);
                            opener.playSound(opener.getLocation(), Sound.FIREWORK_TWINKLE2, 10, 1);

                            player.setValue("fairySouls.claimed", claimed + 5);

                            opener.closeInventory();

                            break;
                        }
                    }
                } catch (IOException | ParseException ex) {
                    ex.printStackTrace();

                    opener.sendMessage(ChatColor.RED + "An error occurred while trying to exchange fairy souls.");
                    opener.closeInventory();
                }
            });
        }});

        Util.fillEmpty(this);

        SkyblockPlayer player = SkyblockPlayer.getPlayer(opener);
        int souls = ((ArrayList<Location>) player.getValue("fairySouls.found")).size() - (int) player.getValue("fairySouls.claimed");

        ItemStack claim = new ItemBuilder(ChatColor.GREEN + "Exchange Fairy Souls", Material.SKULL_ITEM, 1, (byte) SkullType.PLAYER.ordinal()).addLore("&7Find " + ChatColor.LIGHT_PURPLE + "Fairy Souls &7around the", "&7world and bring them back to me", "&7and I will reward you with", "&7permanent stat boosts!", " ", "&7Fairy Souls: " +  ChatColor.LIGHT_PURPLE + souls + "&7/" + ChatColor.LIGHT_PURPLE + "5", " ", (souls >= 5 ? ChatColor.YELLOW + "Click to exchange" : ChatColor.RED + "You don't have enough Fairy Souls!")).toItemStack();

        addItem(22, Util.idToSkull(claim, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk2OTIzYWQyNDczMTAwMDdmNmFlNWQzMjZkODQ3YWQ1Mzg2NGNmMTZjMzU2NWExODFkYzhlNmIyMGJlMjM4NyJ9fX0="));
        addItem(49, Util.buildCloseButton());
    }
}
