package com.skyblock.skyblock.commands.economy;

import com.skyblock.skyblock.Skyblock;
import com.skyblock.skyblock.SkyblockPlayer;
import com.skyblock.skyblock.utilities.Util;
import com.skyblock.skyblock.utilities.command.Command;
import com.skyblock.skyblock.utilities.command.annotations.Description;
import com.skyblock.skyblock.utilities.command.annotations.RequiresPlayer;
import com.skyblock.skyblock.utilities.command.annotations.Usage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


@RequiresPlayer
@Usage(usage = "/sb deposit <amount|all|half>")
@Description(description = "Deposits coins into your bank account")
public class DepositCommand implements Command {

    @Override
    public void execute(Player player, String[] args, Skyblock plugin) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /sb deposit <amount|all|half>");
            return;
        }

        String amount = args[0];

        SkyblockPlayer skyblockPlayer = SkyblockPlayer.getPlayer(player);

        int depositAmount;
        boolean success;

        if (amount.equalsIgnoreCase("all") || amount.equalsIgnoreCase("half")) {
            int purse = (int) skyblockPlayer.getValue("stats.purse");

            if (amount.equalsIgnoreCase("all")) depositAmount = purse;
            else depositAmount = purse / 2;

            if (purse == 0 || depositAmount == 0) {
                player.sendMessage(ChatColor.RED + "You don't have any coins to deposit!");
                return;
            }

            success =skyblockPlayer.deposit(depositAmount, true);
        } else {
            depositAmount = Integer.parseInt(amount);

            if (depositAmount <= 0) {
                player.sendMessage(ChatColor.RED + "You can't deposit a negative amount!");
                return;
            }

            success = skyblockPlayer.deposit(depositAmount, true);
        }

        if (!success) {
            player.sendMessage(ChatColor.RED + "You don't have enough coins to deposit!");
        } else {
            player.sendMessage(ChatColor.GREEN + "You have successfully deposited " + ChatColor.GOLD + Util.abbreviate(depositAmount) + " coins" + ChatColor.GREEN + "! You now have " + ChatColor.GOLD + Util.abbreviate((int) skyblockPlayer.getValue("bank.balance")) + ChatColor.GREEN + " coins in your account!");
        }
    }
}
