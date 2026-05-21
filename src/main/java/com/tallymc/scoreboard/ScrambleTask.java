package com.tallymc.scoreboard;

import com.tallymc.scoreboard.ScoreboardManager.RankMode;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class ScrambleTask implements Runnable {

  private final ScoreboardManager scoreboard;
  private final RankMode mode;       // null for a Personal burst
  private final boolean personal;
  private int ticksLeft;
  private BukkitTask task;

  private ScrambleTask(ScoreboardManager sb, RankMode mode,
                       boolean personal, int ticks) {
    this.scoreboard = sb;
    this.mode = mode;
    this.personal = personal;
    this.ticksLeft = ticks;
  }

  public static void startRankings(Plugin plugin, ScoreboardManager sb,
                                   RankMode mode, int ticks) {
    ScrambleTask st = new ScrambleTask(sb, mode, false, ticks);
    st.task = Bukkit.getScheduler().runTaskTimer(plugin, st, 0L, 2L);
  }

  public static void startPersonal(Plugin plugin, ScoreboardManager sb,
                                   int ticks) {
    ScrambleTask st = new ScrambleTask(sb, null, true, ticks);
    st.task = Bukkit.getScheduler().runTaskTimer(plugin, st, 0L, 2L);
  }

  @Override
  public void run() {
    if (ticksLeft > 0) {
      if (personal) scoreboard.renderPersonalScramble();
      else          scoreboard.renderRankingsScramble(mode);
      ticksLeft -= 2;
    } else {
      scoreboard.finishScramble();
      if (task != null) task.cancel();
    }
  }
}
