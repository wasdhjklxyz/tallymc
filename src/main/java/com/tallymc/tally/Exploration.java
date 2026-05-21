package com.tallymc.tally;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class Exploration {
  public static double raw(Player player) {
    double total = 0.0;
    total += player.getStatistic(Statistic.WALK_ONE_CM) / Weights.DIV_WALK_CM;
    total += player.getStatistic(Statistic.BOAT_ONE_CM) / Weights.DIV_BOAT_CM;
    total += player.getStatistic(Statistic.AVIATE_ONE_CM) / Weights.DIV_ELYTRA_CM;
    return total;
  }
}
