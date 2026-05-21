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
import java.util.function.ToDoubleFunction;

public class ScoreboardManager {
  private static final int MAX_RANKS = 5;
  private final TallyStore store;
  private final Map<UUID, Scoreboard> boards = new HashMap<>();
  private volatile UUID leaderOverall, leaderMining, leaderCombat,
                        leaderExploration, leaderSurvival, leaderAdvancement;

  enum RankMode { TOTAL, MINING, COMBAT, EXPLORATION, SURVIVAL, ADVANCEMENT }

  private static RankMode modeForSecond(int sec) {
    int s = sec % 45;
    if (s < 30)  return RankMode.TOTAL;
    if (s < 33) return RankMode.MINING;
    if (s < 36) return RankMode.COMBAT;
    if (s < 39) return RankMode.EXPLORATION;
    if (s < 42) return RankMode.SURVIVAL;
    return RankMode.ADVANCEMENT;
  }

  public void tickCycle(int second) {
    RankMode mode = modeForSecond(second);
    for (Player p : Bukkit.getOnlinePlayers()) {
      render(p, mode);
    }
  }

  private static final class Leader {
    double best = Double.NEGATIVE_INFINITY;
    UUID id = null;
    boolean tied = false;

    void consider(UUID candidate, double value) {
      double v = Math.round(value);
      if (v > best) { best = v; id = candidate; tied = false; }
      else if (v == best) { tied = true; }
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

    for (Player p : Bukkit.getOnlinePlayers()) {
      render(p, RankMode.TOTAL);
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
    render(p, RankMode.TOTAL);
    updateTabName(p);
  }

  public void remove(Player p) {
    persistFinal(p);
    boards.remove(p.getUniqueId());
    p.playerListName(null);
  }

  public void persistFinal(Player p) {
    Calculator.Result r = Calculator.compute(p);
    int s = (int) Math.round(r.tally());
    store.put(p.getUniqueId(), p.getName(), s,
              r.miningTally(), r.combatTally(), r.explorationTally(),
              r.survivalTally(), r.advancementTally());
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

  private void render(Player viewer, RankMode mode) {
    Scoreboard board = boards.computeIfAbsent(viewer.getUniqueId(),
        k -> Bukkit.getScoreboardManager().getNewScoreboard());

    Objective old = board.getObjective("tally_side");
    if (old != null) old.unregister();

    Objective obj = board.registerNewObjective(
        "tally_side", Criteria.DUMMY,
        Component.text("Tally", NamedTextColor.GOLD, TextDecoration.BOLD));
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);

    UUID v = viewer.getUniqueId();

    List<TallyStore.Entry> ranked;
    String header;
    ToDoubleFunction<TallyStore.Entry> value;
    switch (mode) {
      case MINING -> {
        ranked = store.rankedBy(TallyStore.Entry::mining);
        header = "⛏ Mining"; value = TallyStore.Entry::mining;
      }
      case COMBAT -> {
        ranked = store.rankedBy(TallyStore.Entry::combat);
        header = "⚔ Combat"; value = TallyStore.Entry::combat;
      }
      case EXPLORATION -> {
        ranked = store.rankedBy(TallyStore.Entry::exploration);
        header = "✦ Exploration"; value = TallyStore.Entry::exploration;
      }
      case SURVIVAL -> {
        ranked = store.rankedBy(TallyStore.Entry::survival);
        header = "❤ Survival"; value = TallyStore.Entry::survival;
      }
      case ADVANCEMENT -> {
        ranked = store.rankedBy(TallyStore.Entry::advancement);
        header = "★ Advancement"; value = TallyStore.Entry::advancement;
      }
      default -> {
        ranked = store.ranked();
        header = "𝍸 Total"; value = e -> e.tally();
      }
    }

    List<Component> lines = new ArrayList<>();
    lines.add(Component.text("Rankings", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));
    //lines.add(Component.text(header, modeColor(mode)));

    int shown = Math.min(MAX_RANKS, ranked.size());
    for (int i = 0; i < shown; i++) {
      lines.add(rankLine(i, ranked.get(i), value, mode));
    }

    // int viewerRank = -1;
    // for (int i = 0; i < ranked.size(); i++) {
    //   if (ranked.get(i).id().equals(v)) { viewerRank = i; break; }
    // }
    // if (viewerRank >= shown) {
    //   lines.add(Component.text("  ...", NamedTextColor.DARK_GRAY));
    //   lines.add(rankLine(viewerRank, ranked.get(viewerRank), value));
    // }

    lines.add(blank(0));
    lines.add(Component.text("Personal", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    TallyStore.Entry me = store.get(v);
    double mMin = me != null ? me.mining()      : 0;
    double mCom = me != null ? me.combat()      : 0;
    double mExp = me != null ? me.exploration() : 0;
    double mSur = me != null ? me.survival()    : 0;
    double mAdv = me != null ? me.advancement() : 0;

    lines.add(catLine(" ⛏", "Mining", mMin, NamedTextColor.AQUA,
                      v.equals(leaderMining)));
    lines.add(catLine(" ⚔", "Combat", mCom, NamedTextColor.RED,
                      v.equals(leaderCombat)));
    lines.add(catLine(" ✦", "Exploration", mExp, NamedTextColor.GREEN,
                      v.equals(leaderExploration)));
    lines.add(catLine(" ❤", "Survival", mSur, NamedTextColor.LIGHT_PURPLE,
                      v.equals(leaderSurvival)));
    lines.add(catLine(" ★", "Advancement", mAdv, NamedTextColor.YELLOW,
                      v.equals(leaderAdvancement)));

    int score = lines.size();
    for (Component line : lines) {
      addLine(obj, line, score--);
    }

    viewer.setScoreboard(board);
  }

  private Component rankLine(int index, TallyStore.Entry e,
                             ToDoubleFunction<TallyStore.Entry> value,
                             RankMode mode) {
    NamedTextColor numColor = modeColor(mode);
    var b = Component.text();

    if (mode == RankMode.TOTAL) {
      boolean crowned = hasCrown(e.id());
      b.append(Component.text(" "))
       .append(crownsFor(e.id()));
      if (crowned) {
        b.append(Component.text(" "));
      }
      b.append(Component.text(e.name() + " ", NamedTextColor.GRAY))
       .append(Component.text(String.format("%.0f", value.applyAsDouble(e)),
                              NamedTextColor.WHITE));
    } else {
      if (index == 0) {
        b.append(Component.text(" "))
         .append(crown(numColor));
      } else {
        b.append(Component.text("  "));
      }
      b.append(Component.text(" " + e.name() + " ", NamedTextColor.GRAY))
       .append(Component.text(String.format("%.0f", value.applyAsDouble(e)),
                              numColor));
    }
    return b.build();
  }

  private static Component crown(NamedTextColor color) {
    return Component.text("♛", color);
  }

  private Component crownsFor(UUID id) {
    var c = Component.text();
    if (id.equals(leaderOverall)) {
      c.append(Component.text("♛", NamedTextColor.WHITE));
    }
    if (id.equals(leaderMining))      c.append(crown(NamedTextColor.AQUA));
    if (id.equals(leaderCombat))      c.append(crown(NamedTextColor.RED));
    if (id.equals(leaderExploration)) c.append(crown(NamedTextColor.GREEN));
    if (id.equals(leaderSurvival))    c.append(crown(NamedTextColor.LIGHT_PURPLE));
    if (id.equals(leaderAdvancement)) c.append(crown(NamedTextColor.YELLOW));
    return c.build();
  }

  public Component crownsFor(Player p) {
    return crownsFor(p.getUniqueId());
  }

  public boolean hasCrown(Player p) {
    return hasCrown(p.getUniqueId());
  }

  public boolean hasCrown(UUID id) {
    return id.equals(leaderOverall) || id.equals(leaderMining) ||
           id.equals(leaderCombat) || id.equals(leaderExploration) ||
           id.equals(leaderSurvival) || id.equals(leaderAdvancement);
  }

  private void updateTabName(Player p) {
    Component name = Component.text()
        .append(crownsFor(p))
        .append(Component.text(p.getName(), NamedTextColor.WHITE))
        .build();
    p.playerListName(name);
  }

  private static Component catLine(String icon, String name, double value,
                                   NamedTextColor color, boolean isLeader) {
    var b = Component.text();
    // if (isLeader) {
    //   b.append(Component.text(" ♛" + icon + " ", color));
    // } else {
    //   b.append(Component.text(" " + icon + " ", color));
    // }
    b.append(Component.text(icon + " ", color));
    b.append(Component.text(name + " ", NamedTextColor.GRAY));
    b.append(Component.text(String.format("%.0f", value), color));
    return b.build();
  }

  private void addLine(Objective obj, Component text, int score) {
    String entry = LegacyComponentSerializer.legacySection().serialize(text);
    obj.getScore(entry).setScore(score);
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

  private static NamedTextColor modeColor(RankMode mode) {
    return switch (mode) {
      case MINING      -> NamedTextColor.AQUA;
      case COMBAT      -> NamedTextColor.RED;
      case EXPLORATION -> NamedTextColor.GREEN;
      case SURVIVAL    -> NamedTextColor.LIGHT_PURPLE;
      case ADVANCEMENT -> NamedTextColor.YELLOW;
      default          -> NamedTextColor.WHITE;
    };
  }
}
