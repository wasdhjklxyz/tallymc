package com.tallymc;

import com.tallymc.command.TallyCommand;
import com.tallymc.scoreboard.ScoreboardManager;
import com.tallymc.scoreboard.ScoreboardListener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class TallyMC extends JavaPlugin implements Listener {
  private ScoreboardManager scoreboard;

  @Override
  public void onEnable() {
    TallyCommand cmd = new TallyCommand();
    getCommand("tally").setExecutor(cmd);
    getCommand("tally").setTabCompleter(cmd);

    this.scoreboard = new ScoreboardManager();
    for (Player p : Bukkit.getOnlinePlayers()) {
      scoreboard.refresh(p);
    }
    Bukkit.getScheduler().runTaskTimer(
        this, scoreboard::refreshAll, 20L, 1200L);
    Bukkit.getPluginManager().registerEvents(
        new ScoreboardListener(scoreboard), this);

    getLogger().info("TallyMC enabled");
  }
}
