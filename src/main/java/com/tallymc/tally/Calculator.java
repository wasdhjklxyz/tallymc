package com.tallymc.tally;

import org.bukkit.entity.Player;

public class Calculator {
  public record Result(double tally, double miningTally, double combatTally,
                       double explorationTally, double survivalTally,
                       double advancementTally) {}

  public static Result compute(Player player) {
    double miningTally      = Mining.raw(player);
    double combatTally      = Combat.raw(player);
    double explorationTally = Exploration.raw(player);
    double survivalTally    = Survival.raw(player);
    double advancementTally = Advancements.raw(player);

    double tally = miningTally + combatTally + explorationTally +
                   survivalTally + advancementTally;

    return new Result(tally, miningTally, combatTally, explorationTally,
                      survivalTally, advancementTally);
  }
}
