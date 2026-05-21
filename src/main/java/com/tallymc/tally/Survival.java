package com.tallymc.tally;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class Survival {
  public static double raw(Player player) {
    long playTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
    double playHours = playTicks / 20.0 / 3600.0;
    int deaths = player.getStatistic(Statistic.DEATHS);
    double raw = playHours * Weights.SURVIVAL_PER_HOUR +
                 deaths    * Weights.SURVIVAL_PER_DEATH;
    return Math.max(0.0, raw);
  }
}
