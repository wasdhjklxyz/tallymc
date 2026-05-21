package com.tallymc.tally;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class Mining {
  public static double raw(Player player) {
    double total = 0.0;

    for (var entry : Weights.ORE_WEIGHTS.entrySet()) {
      int mined = player.getStatistic(Statistic.MINE_BLOCK, entry.getKey());
      total += mined * entry.getValue();
    }

    for (var entry : Weights.DEEPSLATE_VARIANTS.entrySet()) {
      Material deepslate = entry.getKey();
      Material base = entry.getValue();
      Double baseWeight = Weights.ORE_WEIGHTS.get(base);
      if (baseWeight == null) continue;
      int mined = player.getStatistic(Statistic.MINE_BLOCK, deepslate);
      total += mined * baseWeight * Weights.DEEPSLATE_MULT;
    }

    return total;
  }
}
