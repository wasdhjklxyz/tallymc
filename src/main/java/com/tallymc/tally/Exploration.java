package com.tallymc.tally;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class Exploration {
  public static double raw(Player player) {
    double total = 0.0;

    total += player.getStatistic(Statistic.WALK_ONE_CM)    / Weights.DIV_WALK_CM;
    total += player.getStatistic(Statistic.SPRINT_ONE_CM)  / Weights.DIV_SPRINT_CM;
    total += player.getStatistic(Statistic.SWIM_ONE_CM)    / Weights.DIV_SWIM_CM;
    total += player.getStatistic(Statistic.CLIMB_ONE_CM)   / Weights.DIV_CLIMB_CM;

    total += player.getStatistic(Statistic.BOAT_ONE_CM)    / Weights.DIV_BOAT_CM;
    total += player.getStatistic(Statistic.HORSE_ONE_CM)   / Weights.DIV_HORSE_CM;
    total += player.getStatistic(Statistic.MINECART_ONE_CM)/ Weights.DIV_MINECART_CM;

    total += player.getStatistic(Statistic.AVIATE_ONE_CM)  / Weights.DIV_ELYTRA_CM;

    total += player.getStatistic(Statistic.STRIDER_ONE_CM) / Weights.DIV_STRIDER_CM;
    total += player.getStatistic(Statistic.PIG_ONE_CM)     / Weights.DIV_PIG_CM;
    total += player.getStatistic(Statistic.HAPPY_GHAST_ONE_CM) / Weights.DIV_HAPPY_GHAST_CM;
    total += player.getStatistic(Statistic.NAUTILUS_ONE_CM)/ Weights.DIV_NAUTILUS_CM;

    return total;
  }
}
