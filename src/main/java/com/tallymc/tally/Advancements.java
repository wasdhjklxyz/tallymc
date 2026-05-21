package com.tallymc.tally;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class Advancements {
  public static double raw(Player player) {
    double total = 0.0;
    Iterator<Advancement> it = Bukkit.advancementIterator();

    while (it.hasNext()) {
      Advancement adv = it.next();
      String keyPath = adv.getKey().getKey();

      if (keyPath.startsWith("recipes/")) continue;

      if (player.getAdvancementProgress(adv).isDone()) {
        Weights.Tier tier = Weights.ADVANCEMENT_TIERS
            .getOrDefault(keyPath, Weights.Tier.COMMON);
        total += tier.weight;
      }
    }
    return total;
  }

  public static int completedCount(Player player) {
    int count = 0;
    Iterator<Advancement> it = Bukkit.advancementIterator();
    while (it.hasNext()) {
      Advancement adv = it.next();
      if (adv.getKey().getKey().startsWith("recipes/")) continue;
      if (player.getAdvancementProgress(adv).isDone()) count++;
    }
    return count;
  }
}
