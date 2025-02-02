package com.skyblock.skyblock.event;

import com.skyblock.skyblock.features.npc.NPC;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class SkyblockNPCClickEvent extends SkyblockEvent {

    private Player player;
    private NPC npc;

}
