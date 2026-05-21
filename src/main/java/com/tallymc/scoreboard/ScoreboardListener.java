package com.tallymc.scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {
  private final ScoreboardManager scoreboard;

  public ScoreboardListener(ScoreboardManager scoreboard) {
    this.scoreboard = scoreboard;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    scoreboard.refreshAll();
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    scoreboard.remove(e.getPlayer());
    scoreboard.refreshAll();
  }
}
