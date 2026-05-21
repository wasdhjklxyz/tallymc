package com.tallymc.scoreboard;

import com.tallymc.tally.Calculator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
  private static final int MAX_RANKS = 3;
  private final Map<UUID, Scoreboard> boards = new HashMap<>();
  private UUID leaderOverall, leaderMining, leaderCombat,
               leaderExploration, leaderSurvival, leaderAdvancement;

  public void refreshAll() {
    List<Map.Entry<Player, Integer>> ranked = new ArrayList<>();

    double bestMin = -1, bestCom = -1, bestExp = -1, bestSur = -1, bestAdv = -1;
    int bestOverall = Integer.MIN_VALUE;
    leaderOverall = leaderMining = leaderCombat =
        leaderExploration = leaderSurvival = leaderAdvancement = null;

    for (Player p : Bukkit.getOnlinePlayers()) {
      Calculator.Result r = Calculator.compute(p);
      int s = (int) Math.round(r.tally());
      ranked.add(Map.entry(p, s));

      UUID id = p.getUniqueId();
      if (s > bestOverall)            { bestOverall = s; leaderOverall = id; }
      if (r.miningTally()  > bestMin) { bestMin = r.miningTally();  leaderMining = id; }
      if (r.combatTally()  > bestCom) { bestCom = r.combatTally();  leaderCombat = id; }
      if (r.explorationTally() > bestExp) { bestExp = r.explorationTally(); leaderExploration = id; }
      if (r.survivalTally() > bestSur){ bestSur = r.survivalTally(); leaderSurvival = id; }
      if (r.advancementTally() > bestAdv){ bestAdv = r.advancementTally(); leaderAdvancement = id; }
    }

    ranked.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

    for (Player p : Bukkit.getOnlinePlayers()) {
      render(p, ranked);
      updateTabName(p);
    }
  }

  public void refresh(Player p) {
    int s = (int) Math.round(Calculator.compute(p).tally());
    List<Map.Entry<Player, Integer>> solo = new ArrayList<>();
    solo.add(Map.entry(p, s));
    render(p, solo);
  }

  private void render(Player viewer, List<Map.Entry<Player, Integer>> ranked) {
    Scoreboard board = boards.computeIfAbsent(viewer.getUniqueId(),
        k -> Bukkit.getScoreboardManager().getNewScoreboard());

    Objective old = board.getObjective("tally_side");
    if (old != null) old.unregister();

    Objective obj = board.registerNewObjective(
        "tally_side", Criteria.DUMMY,
        Component.text("Tally", NamedTextColor.GOLD, TextDecoration.BOLD));
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);

    Calculator.Result r = Calculator.compute(viewer);

    List<Component> lines = new ArrayList<>();

    lines.add(Component.text("Rankings", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    int shown = Math.min(MAX_RANKS, ranked.size());
    for (int i = 0; i < shown; i++) {
      lines.add(rankLine(i, ranked.get(i).getKey(), ranked.get(i).getValue()));
    }

    int viewerRank = -1;
    for (int i = 0; i < ranked.size(); i++) {
      if (ranked.get(i).getKey().getUniqueId().equals(viewer.getUniqueId())) {
        viewerRank = i;
        break;
      }
    }

    if (viewerRank >= shown) {
      lines.add(Component.text("  ...", NamedTextColor.DARK_GRAY));
      lines.add(rankLine(viewerRank,
                         ranked.get(viewerRank).getKey(),
                         ranked.get(viewerRank).getValue()));
    }

    lines.add(blank(0));
    lines.add(Component.text("Personal", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    UUID v = viewer.getUniqueId();
    lines.add(catLine(" ⛏", "Mining", r.miningTally(), NamedTextColor.AQUA,
                      v.equals(leaderMining)));
    lines.add(catLine(" ⚔", "Combat", r.combatTally(), NamedTextColor.RED,
                      v.equals(leaderCombat)));
    lines.add(catLine(" ✦", "Exploration", r.explorationTally(), NamedTextColor.GREEN,
                      v.equals(leaderExploration)));
    lines.add(catLine(" ❤", "Survival", r.survivalTally(), NamedTextColor.LIGHT_PURPLE,
                      v.equals(leaderSurvival)));
    lines.add(catLine(" ★", "Advancement", r.advancementTally(), NamedTextColor.YELLOW,
                      v.equals(leaderAdvancement)));

    int score = lines.size();
    for (Component line : lines) {
      addLine(obj, line, score--);
    }

    viewer.setScoreboard(board);
  }

  public void remove(Player p) {
    boards.remove(p.getUniqueId());
  }

  private void updateTabName(Player p) {
    Component name = Component.text()
        .append(crownsFor(p))
        .append(Component.text(p.getName(), NamedTextColor.WHITE))
        .build();
    p.playerListName(name);
  }

  private static Component crown(NamedTextColor color) {
    return Component.text("♛", color);
  }

  private static Component catLine(String icon, String name, double value,
                                   NamedTextColor color, boolean isLeader) {
    var b = Component.text()
        .append(Component.text(icon + " ", color))
        .append(Component.text(name + " ", NamedTextColor.WHITE));
    if (isLeader) {
      b.append(Component.text("♛ ", color));
    }
    b.append(Component.text(String.format("%.0f", value), color));
    return b.build();
  }

  private void addLine(Objective obj, Component text, int score) {
    String entry = LegacyComponentSerializer.legacySection().serialize(text);
    obj.getScore(entry).setScore(score);
  }

  private static Component rankLine(int index, Player p, int score) {
    Component prefix = (index == 0)
        ? Component.text(" ♛ ", NamedTextColor.GOLD)
        : Component.text(" " + (index + 1) + ". ", NamedTextColor.GRAY);
    return Component.text()
        .append(prefix)
        .append(Component.text(p.getName() + " ", NamedTextColor.WHITE))
        .append(Component.text(String.valueOf(score), NamedTextColor.YELLOW))
        .build();
  }

  private static Component blank(int n) {
    return Component.text("\u00A7r".repeat(n + 1));
  }

  public Component crownsFor(Player p) {
    UUID id = p.getUniqueId();
    var c = Component.text();
    if (id.equals(leaderOverall))     c.append(crown(NamedTextColor.GOLD));
    if (id.equals(leaderMining))      c.append(crown(NamedTextColor.AQUA));
    if (id.equals(leaderCombat))      c.append(crown(NamedTextColor.RED));
    if (id.equals(leaderExploration)) c.append(crown(NamedTextColor.GREEN));
    if (id.equals(leaderSurvival))    c.append(crown(NamedTextColor.LIGHT_PURPLE));
    if (id.equals(leaderAdvancement)) c.append(crown(NamedTextColor.YELLOW));
    return c.build();
  }
}
