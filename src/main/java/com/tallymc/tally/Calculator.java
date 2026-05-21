package com.tallymc.tally;

import org.bukkit.entity.Player;

public class Calculator {
  public static Result(double tally, double miningFrac, double combatFrac,
                       double explorationFrac, double survivalFrac,
                       double advancementFrac) {}

  private static Result compute(Player player) {
    double miningFrac = Mining.raw(player);
    double combatFrac = 0.0;
    double explorationFrac = 0.0;
    double survivalFrac = 0.0;
    double advancementFrac = 0.0;

    double tally = 100.0 * (Weights.W_MINING      * miningFrac +
                            Weights.W_COMBAT      * combatFrac +
                            Weights.W_EXPLORATION * explorationFrac+
                            Weights.W_SURVIVAL    * survivalFrac +
                            Weights.W_ADVANCEMENT * advancementFrac);

    return new Result(tally, miningFrac, combatFrac, explorationFrac,
                      survivalFrac, advancementFrac);
  }
}
