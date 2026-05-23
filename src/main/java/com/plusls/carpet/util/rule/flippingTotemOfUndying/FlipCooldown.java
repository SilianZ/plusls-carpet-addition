package com.plusls.carpet.util.rule.flippingTotemOfUndying;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class FlipCooldown {
    static private final Map<Player, Long> cooldownMap = new HashMap<>();

    static public void init() {
        cooldownMap.clear();
    }

    static public long getCoolDown(Player Silian_player) {
        return cooldownMap.getOrDefault(Silian_player, 0L);
    }

    static public void setCoolDown(Player Silian_player, long Silian_cooldown) {
        cooldownMap.put(Silian_player, Silian_cooldown);
    }

    static public void removePlayer(Player Silian_player) {
        cooldownMap.remove(Silian_player);
    }

}
