package com.tallymc;

import com.tallymc.command.TallyCommand;
import com.tallymc.scoreboard.ScoreboardManager;
import com.tallymc.scoreboard.ScoreboardListener;
import com.tallymc.chat.ChatListener;
import com.tallymc.store.TallyStore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TallyMC extends JavaPlugin {
  private TallyStore store;
  private ScoreboardManager scoreboard;

  @Override
  public void onEnable() {
    TallyCommand cmd = new TallyCommand();
    getCommand("tally").setExecutor(cmd);
    getCommand("tally").setTabCompleter(cmd);

    this.store = new TallyStore(this);
    this.scoreboard = new ScoreboardManager(store);
    for (Player p : Bukkit.getOnlinePlayers()) {
      scoreboard.refresh(p);
    }
    Bukkit.getScheduler().runTaskTimer(
        this, scoreboard::refreshAll, 20L, 1200L);
    Bukkit.getPluginManager().registerEvents(
        new ScoreboardListener(scoreboard), this);
    Bukkit.getPluginManager().registerEvents(
        new ChatListener(scoreboard), this);
    Bukkit.getScheduler().runTaskTimer(
        this, store::save, 6000L, 6000L);

    getLogger().info("TallyMC enabled");
  }

  @Override
  public void onDisable() {
    if (store != null) store.save();
    getLogger().info("TallyMC disabled");
  }
}
