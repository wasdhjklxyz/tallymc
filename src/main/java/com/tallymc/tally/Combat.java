package com.tallymc.tally;

import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Combat {
  public static double raw(Player player) {
    double total = 0.0;
    long weightedKills = 0;

    for (var entry : Weights.KILL_WEIGHTS.entrySet()) {
      int kills = player.getStatistic(Statistic.KILL_ENTITY, entry.getKey());
      total += kills * entry.getValue();
      weightedKills += kills;
    }

    long allMobKills = player.getStatistic(Statistic.MOB_KILLS);
    long genericKills = Math.max(0, allMobKills - weightedKills);
    total += genericKills * Weights.WEIGHT_HOSTILE_MOB;

    int raidWins = player.getStatistic(Statistic.RAID_WIN);
    total += raidWins * Weights.WEIGHT_RAID_WIN;

    return total;
  }
}
