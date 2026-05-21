package com.tallymc.tally;

import org.bukkit.entity.Player;

public class Calculator {
  public record Result(double tally, double miningTally, double combatTally,
                       double explorationTally, double survivalTally,
                       double advancementTally) {}

  public static Result compute(Player player) {
    double miningTally      = fraction(Mining.raw(player), Weights.PAR_MINING);
    double combatTally      = 0.0;
    double explorationTally = 0.0;
    double survivalTally    = 0.0;
    double advancementTally = 0.0;

    double tally = 100.0 * (Weights.W_MINING      * miningTally +
                            Weights.W_COMBAT      * combatTally +
                            Weights.W_EXPLORATION * explorationTally +
                            Weights.W_SURVIVAL    * survivalTally +
                            Weights.W_ADVANCEMENT * advancementTally);

    return new Result(tally, miningTally, combatTally, explorationTally,
                      survivalTally, advancementTally);
  }
}
