package com.skyblock.skyblock.features.objectives.impl.starting;

import com.skyblock.skyblock.event.SkyblockCraftEvent;
import com.skyblock.skyblock.features.objectives.Objective;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;

public class PickaxeObjective extends Objective {

    public PickaxeObjective() {
        super("craft_pickaxe", "Craft a wood pickaxe");
    }

    @EventHandler
    public void onCraft(SkyblockCraftEvent e) {
        if (!isThisObjective(e.getPlayer())) return;

        if (e.getRecipe().getResult().getType().equals(Material.WOOD_PICKAXE)) complete(e.getPlayer());
    }
}
