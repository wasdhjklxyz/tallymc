package com.tallymc.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CycleTask implements Runnable {

  private final ScoreboardManager scoreboard;
  private int second = 0;

  private CycleTask(ScoreboardManager scoreboard) {
    this.scoreboard = scoreboard;
  }

  public static void start(Plugin plugin, ScoreboardManager scoreboard) {
    Bukkit.getScheduler().runTaskTimer(
        plugin, new CycleTask(scoreboard), 20L, 20L);
  }

  @Override
  public void run() {
    second++;
    scoreboard.tickCycle(second);
  }
}
