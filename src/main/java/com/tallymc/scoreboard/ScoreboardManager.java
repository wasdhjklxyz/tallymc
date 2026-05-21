package com.tallymc.scoreboard;

import com.tallymc.store.TallyStore;
import com.tallymc.tally.Calculator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.Color;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.FireworkEffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
  private static final int MAX_RANKS = 3;
  private final TallyStore store;
  private final Map<UUID, Scoreboard> boards = new HashMap<>();
  private volatile UUID leaderOverall, leaderMining, leaderCombat,
                        leaderExploration, leaderSurvival, leaderAdvancement;

  private static final class Leader {
    double best = Double.NEGATIVE_INFINITY;
    UUID id = null;
    boolean tied = false;

    void consider(UUID candidate, double value) {
      if (value > best) {
        best = value;
        id = candidate;
        tied = false;
      } else if (value == best) {
        tied = true;
      }
    }

    UUID result() {
      return tied ? null : id;
    }
  }

  public ScoreboardManager(TallyStore store) {
    this.store = store;
  }

  public void refreshAll() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      Calculator.Result r = Calculator.compute(p);
      int s = (int) Math.round(r.tally());
      store.put(p.getUniqueId(), p.getName(), s,
                r.miningTally(), r.combatTally(), r.explorationTally(),
                r.survivalTally(), r.advancementTally());
    }

    UUID prevOverall = leaderOverall, prevMining = leaderMining,
         prevCombat = leaderCombat, prevExploration = leaderExploration,
         prevSurvival = leaderSurvival, prevAdvancement = leaderAdvancement;

    recomputeLeaders();

    celebrate(prevOverall,     leaderOverall,     Color.fromRGB(0xFFAA00));
    celebrate(prevMining,      leaderMining,      Color.fromRGB(0x55FFFF));
    celebrate(prevCombat,      leaderCombat,      Color.fromRGB(0xFF5555));
    celebrate(prevExploration, leaderExploration, Color.fromRGB(0x55FF55));
    celebrate(prevSurvival,    leaderSurvival,    Color.fromRGB(0xFF55FF));
    celebrate(prevAdvancement, leaderAdvancement, Color.fromRGB(0xFFFF55));

    List<TallyStore.Entry> ranked = store.ranked();
    for (Player p : Bukkit.getOnlinePlayers()) {
      render(p, ranked);
      updateTabName(p);
    }
  }

  public void refresh(Player p) {
    Calculator.Result r = Calculator.compute(p);
    int s = (int) Math.round(r.tally());
    store.put(p.getUniqueId(), p.getName(), s,
              r.miningTally(), r.combatTally(), r.explorationTally(),
              r.survivalTally(), r.advancementTally());
    recomputeLeaders();
    render(p, store.ranked());
    updateTabName(p);
  }

  private void recomputeLeaders() {
    Leader overall = new Leader(), mining = new Leader(),
           combat = new Leader(), exploration = new Leader(),
           survival = new Leader(), advancement = new Leader();

    for (TallyStore.Entry e : store.entries()) {
      overall.consider(e.id(), e.tally());
      mining.consider(e.id(), e.mining());
      combat.consider(e.id(), e.combat());
      exploration.consider(e.id(), e.exploration());
      survival.consider(e.id(), e.survival());
      advancement.consider(e.id(), e.advancement());
    }

    leaderOverall     = overall.result();
    leaderMining      = mining.result();
    leaderCombat      = combat.result();
    leaderExploration = exploration.result();
    leaderSurvival    = survival.result();
    leaderAdvancement = advancement.result();
  }

  private void render(Player viewer, List<TallyStore.Entry> ranked) {
    Scoreboard board = boards.computeIfAbsent(viewer.getUniqueId(),
        k -> Bukkit.getScoreboardManager().getNewScoreboard());

    Objective old = board.getObjective("tally_side");
    if (old != null) old.unregister();

    Objective obj = board.registerNewObjective(
        "tally_side", Criteria.DUMMY,
        Component.text("Tally", NamedTextColor.GOLD, TextDecoration.BOLD));
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);

    Calculator.Result r = Calculator.compute(viewer);
    UUID v = viewer.getUniqueId();

    List<Component> lines = new ArrayList<>();

    lines.add(Component.text("Rankings", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    int shown = Math.min(MAX_RANKS, ranked.size());
    for (int i = 0; i < shown; i++) {
      lines.add(rankLine(i, ranked.get(i)));
    }

    int viewerRank = -1;
    for (int i = 0; i < ranked.size(); i++) {
      if (ranked.get(i).id().equals(v)) { viewerRank = i; break; }
    }
    if (viewerRank >= shown) {
      lines.add(Component.text("  ...", NamedTextColor.DARK_GRAY));
      lines.add(rankLine(viewerRank, ranked.get(viewerRank)));
    }

    lines.add(blank(0));
    lines.add(Component.text("Personal", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));
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
    persistFinal(p);
    boards.remove(p.getUniqueId());
    p.playerListName(null);
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

  public boolean hasCrown(Player p) {
    UUID id = p.getUniqueId();
    return id.equals(leaderOverall) || id.equals(leaderMining)
        || id.equals(leaderCombat) || id.equals(leaderExploration)
        || id.equals(leaderSurvival) || id.equals(leaderAdvancement);
  }

  private static Component catLine(String icon, String name, double value,
                                   NamedTextColor color, boolean isLeader) {
    var b = Component.text();
    if (isLeader) {
      b.append(Component.text(" ♛" + icon + " ", color));
    } else {
      b.append(Component.text(" " + icon + " ", color));
    }
    b.append(Component.text(name + " ", NamedTextColor.GRAY));
    b.append(Component.text(String.format("%.0f", value), color));
    return b.build();
  }

  private void addLine(Objective obj, Component text, int score) {
    String entry = LegacyComponentSerializer.legacySection().serialize(text);
    obj.getScore(entry).setScore(score);
  }

  private static Component rankLine(int index, TallyStore.Entry e) {
    Component prefix = (index == 0)
        ? Component.text(" ♛ ", NamedTextColor.GOLD)
        : Component.text(" ");
    return Component.text()
        .append(prefix)
        .append(Component.text(e.name() + " ", NamedTextColor.GRAY))
        .append(Component.text(String.valueOf(e.tally()), NamedTextColor.WHITE))
        .build();
  }

  private static Component blank(int n) {
    return Component.text("\u00A7r".repeat(n + 1));
  }

  private void celebrate(UUID oldLeader, UUID newLeader, Color color) {
    if (newLeader == null) return;
    if (newLeader.equals(oldLeader)) return;

    Player p = Bukkit.getPlayer(newLeader);
    if (p == null) return;
    spawnFirework(p, color);
  }

  private void spawnFirework(Player p, Color color) {
    Firework fw = p.getWorld().spawn(p.getLocation(), Firework.class);
    FireworkMeta meta = fw.getFireworkMeta();
    meta.addEffect(FireworkEffect.builder()
        .withColor(color)
        .withFade(color)
        .with(FireworkEffect.Type.BALL_LARGE)
        .trail(true)
        .build());
    meta.setPower(1);
    fw.setFireworkMeta(meta);
  }

  public void persistFinal(Player p) {
    Calculator.Result r = Calculator.compute(p);
    int s = (int) Math.round(r.tally());
    store.put(p.getUniqueId(), p.getName(), s,
              r.miningTally(), r.combatTally(), r.explorationTally(),
              r.survivalTally(), r.advancementTally());
  }
}
