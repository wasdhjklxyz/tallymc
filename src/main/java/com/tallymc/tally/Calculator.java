package com.tallymc.tally;

import org.bukkit.entity.Player;

public class Calculator {
  public record Result(double tally, double miningTally, double combatTally,
                       double explorationTally, double survivalTally,
                       double advancementTally) {}

  public static Result compute(Player player) {
    double miningTally      = Mining.raw(player);
    double combatTally      = 0.0;
    double explorationTally = 0.0;
    double survivalTally    = 0.0;
    double advancementTally = 0.0;

    double tally = miningTally + combatTally + explorationTally +
                   survivalTally + advancementTally;

    return new Result(tally, miningTally, combatTally, explorationTally,
                      survivalTally, advancementTally);
  }
}
