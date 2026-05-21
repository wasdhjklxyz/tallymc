package com.tallymc.tally;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class Survival {
  public static double raw(Player player) {
    double total = 0.0;

    long playTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
    double playHours = playTicks / 20.0 / 3600.0;
    total += playHours * Weights.SURVIVAL_PER_HOUR;

    int bred = player.getStatistic(Statistic.ANIMALS_BRED);
    total += bred * Weights.SURVIVAL_PER_ANIMAL_BRED;

    double dealtHp = player.getStatistic(Statistic.DAMAGE_DEALT) / 10.0;
    double takenHp = player.getStatistic(Statistic.DAMAGE_TAKEN) / 10.0;
    total += dealtHp * Weights.SURVIVAL_PER_DAMAGE_DEALT;
    total += takenHp * Weights.SURVIVAL_PER_DAMAGE_TAKEN;

    double blockedHp = player.getStatistic(Statistic.DAMAGE_BLOCKED_BY_SHIELD) / 10.0;
    total += blockedHp * Weights.SURVIVAL_PER_DAMAGE_BLOCKED;

    int deaths = player.getStatistic(Statistic.DEATHS);
    total += deaths * Weights.SURVIVAL_PER_DEATH;

    return total;
  }
}
