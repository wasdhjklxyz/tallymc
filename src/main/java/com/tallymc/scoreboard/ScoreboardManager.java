package com.tallymc.scoreboard;

import com.tallymc.store.TallyStore;
import com.tallymc.tally.Calculator;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

public class ScoreboardManager {
  private static final int MAX_RANKS = 5;
  private static final int SCRAMBLE_TICKS = 10;

  private final TallyStore store;
  private final Plugin plugin;
  private final Map<UUID, Scoreboard> boards = new HashMap<>();

  private volatile UUID leaderOverall, leaderMining, leaderCombat,
                        leaderExploration, leaderSurvival, leaderAdvancement;

  enum RankMode { TOTAL, MINING, COMBAT, EXPLORATION, SURVIVAL, ADVANCEMENT }

  // --- per-player change tracking ---------------------------------------

  /** A player's rank + value in one mode, as of the last refresh. */
  private record Snap(int rank, double value) {}

  /** What changed for one player in one mode since the last refresh. */
  private record Change(boolean rankMoved, boolean valueMoved) {}
  private static final Change NO_CHANGE = new Change(false, false);

  // per mode: UUID -> last snapshot
  private final Map<RankMode, Map<UUID, Snap>> lastSnaps =
      new EnumMap<>(RankMode.class);
  // per mode: UUID -> what changed at the last refresh
  private final Map<RankMode, Map<UUID, Change>> changes =
      new EnumMap<>(RankMode.class);
  // modes with at least one change, not yet revealed via scramble
  private final Set<RankMode> dirtyModes = EnumSet.noneOf(RankMode.class);

  // per player: previous Personal category values (for the Personal scramble)
  private record Personal(double mining, double combat, double exploration,
                           double survival, double advancement) {}
  private final Map<UUID, Personal> lastPersonal = new HashMap<>();
  // per player: which Personal categories changed at the last refresh
  private final Map<UUID, boolean[]> personalChanged = new HashMap<>();
  private boolean personalDirty = false;   // any player's Personal changed

  // --- cycle / animation state ------------------------------------------

  private RankMode lastTickMode = null;
  private volatile boolean animating = false;

  public ScoreboardManager(Plugin plugin, TallyStore store) {
    this.plugin = plugin;
    this.store = store;
  }

  // --- cycle ------------------------------------------------------------

  private static RankMode modeForSecond(int sec) {
    int s = sec % 45;
    if (s < 30) return RankMode.TOTAL;
    if (s < 33) return RankMode.MINING;
    if (s < 36) return RankMode.COMBAT;
    if (s < 39) return RankMode.EXPLORATION;
    if (s < 42) return RankMode.SURVIVAL;
    return RankMode.ADVANCEMENT;
  }

  public void tickCycle(int second) {
    if (animating) return;   // a burst owns the board

    RankMode mode = modeForSecond(second);
    boolean switched = (mode != lastTickMode);
    lastTickMode = mode;

    // entering a mode that changed -> scramble reveal
    if (switched && dirtyModes.contains(mode)) {
      dirtyModes.remove(mode);
      animating = true;
      ScrambleTask.startRankings(plugin, this, mode, SCRAMBLE_TICKS);
      return;
    }

    for (Player p : Bukkit.getOnlinePlayers()) {
      render(p, mode, false);
    }
  }

  // --- heavy refresh ----------------------------------------------------

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
    detectChanges();          // rankings: fills changes + dirtyModes
    detectPersonalChanges();  // personal: fills personalChanged + personalDirty

    celebrate(prevOverall,     leaderOverall,     Color.fromRGB(0xFFAA00));
    celebrate(prevMining,      leaderMining,      Color.fromRGB(0x55FFFF));
    celebrate(prevCombat,      leaderCombat,      Color.fromRGB(0xFF5555));
    celebrate(prevExploration, leaderExploration, Color.fromRGB(0x55FF55));
    celebrate(prevSurvival,    leaderSurvival,    Color.fromRGB(0xFF55FF));
    celebrate(prevAdvancement, leaderAdvancement, Color.fromRGB(0xFFFF55));

    for (Player p : Bukkit.getOnlinePlayers()) updateTabName(p);

    // Personal changed -> kick its own scramble burst (if nothing animating)
    if (personalDirty && !animating) {
      personalDirty = false;
      animating = true;
      ScrambleTask.startPersonal(plugin, this, SCRAMBLE_TICKS);
    } else if (!animating) {
      RankMode m = lastTickMode != null ? lastTickMode : RankMode.TOTAL;
      for (Player p : Bukkit.getOnlinePlayers()) render(p, m, false);
    }
  }

  public void refresh(Player p) {
    Calculator.Result r = Calculator.compute(p);
    int s = (int) Math.round(r.tally());
    store.put(p.getUniqueId(), p.getName(), s,
              r.miningTally(), r.combatTally(), r.explorationTally(),
              r.survivalTally(), r.advancementTally());
    recomputeLeaders();
    detectChanges();
    detectPersonalChanges();
    if (!animating) {
      RankMode m = lastTickMode != null ? lastTickMode : RankMode.TOTAL;
      render(p, m, false);
    }
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

  // --- scramble callbacks (called by ScrambleTask) ----------------------

  public void renderRankingsScramble(RankMode mode) {
    for (Player p : Bukkit.getOnlinePlayers()) render(p, mode, true);
  }

  public void renderPersonalScramble() {
    RankMode m = lastTickMode != null ? lastTickMode : RankMode.TOTAL;
    for (Player p : Bukkit.getOnlinePlayers()) render(p, m, false, true);
  }

  /** Burst finished — one clean render, release the lock. */
  public void finishScramble() {
    RankMode m = lastTickMode != null ? lastTickMode : RankMode.TOTAL;
    for (Player p : Bukkit.getOnlinePlayers()) render(p, m, false);
    animating = false;
  }

  // --- change detection -------------------------------------------------

  /** For each mode, diff each player's rank+value vs last snapshot. */
  private void detectChanges() {
    for (RankMode mode : RankMode.values()) {
      List<TallyStore.Entry> ranked = rankedFor(mode);
      ToDoubleFunction<TallyStore.Entry> value = valueFor(mode);

      Map<UUID, Snap> prev = lastSnaps.getOrDefault(mode, Map.of());
      Map<UUID, Snap> now = new HashMap<>();
      Map<UUID, Change> modeChanges = new HashMap<>();
      boolean anyChange = false;

      int shown = Math.min(MAX_RANKS, ranked.size());
      for (int i = 0; i < shown; i++) {
        TallyStore.Entry e = ranked.get(i);
        double v = Math.round(value.applyAsDouble(e));
        Snap snap = new Snap(i, v);
        now.put(e.id(), snap);

        Snap old = prev.get(e.id());
        boolean rankMoved  = old != null && old.rank() != i;
        boolean valueMoved = old != null && old.value() != v;
        if (rankMoved || valueMoved) anyChange = true;
        modeChanges.put(e.id(), new Change(rankMoved, valueMoved));
      }

      lastSnaps.put(mode, now);
      changes.put(mode, modeChanges);
      if (anyChange) dirtyModes.add(mode);
    }
  }

  /** Diff each online player's five Personal category values. */
  private void detectPersonalChanges() {
    personalDirty = false;
    for (Player p : Bukkit.getOnlinePlayers()) {
      UUID id = p.getUniqueId();
      TallyStore.Entry e = store.get(id);
      if (e == null) continue;

      double mn = Math.round(e.mining());
      double cb = Math.round(e.combat());
      double ex = Math.round(e.exploration());
      double sv = Math.round(e.survival());
      double ad = Math.round(e.advancement());

      Personal old = lastPersonal.get(id);
      boolean[] ch = new boolean[5];
      if (old != null) {
        ch[0] = old.mining()      != mn;
        ch[1] = old.combat()      != cb;
        ch[2] = old.exploration() != ex;
        ch[3] = old.survival()    != sv;
        ch[4] = old.advancement() != ad;
        for (boolean c : ch) if (c) personalDirty = true;
      }
      personalChanged.put(id, ch);
      lastPersonal.put(id, new Personal(mn, cb, ex, sv, ad));
    }
  }

  // --- ranking data -----------------------------------------------------

  private List<TallyStore.Entry> rankedFor(RankMode mode) {
    return switch (mode) {
      case MINING      -> store.rankedBy(TallyStore.Entry::mining);
      case COMBAT      -> store.rankedBy(TallyStore.Entry::combat);
      case EXPLORATION -> store.rankedBy(TallyStore.Entry::exploration);
      case SURVIVAL    -> store.rankedBy(TallyStore.Entry::survival);
      case ADVANCEMENT -> store.rankedBy(TallyStore.Entry::advancement);
      default          -> store.ranked();
    };
  }

  private static ToDoubleFunction<TallyStore.Entry> valueFor(RankMode mode) {
    return switch (mode) {
      case MINING      -> TallyStore.Entry::mining;
      case COMBAT      -> TallyStore.Entry::combat;
      case EXPLORATION -> TallyStore.Entry::exploration;
      case SURVIVAL    -> TallyStore.Entry::survival;
      case ADVANCEMENT -> TallyStore.Entry::advancement;
      default          -> e -> e.tally();
    };
  }

  // --- leaders ----------------------------------------------------------

  private static final class Leader {
    double best = Double.NEGATIVE_INFINITY;
    UUID id = null;
    boolean tied = false;

    void consider(UUID candidate, double value) {
      double v = Math.round(value);
      if (v > best) { best = v; id = candidate; tied = false; }
      else if (v == best) { tied = true; }
    }

    UUID result() { return tied ? null : id; }
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

  // --- rendering --------------------------------------------------------

  private void render(Player viewer, RankMode mode, boolean scrambleRankings) {
    render(viewer, mode, scrambleRankings, false);
  }

  /**
   * @param scrambleRankings obfuscate changed rank lines (per-element)
   * @param scramblePersonal obfuscate changed Personal numbers
   */
  private void render(Player viewer, RankMode mode,
                      boolean scrambleRankings, boolean scramblePersonal) {
    Scoreboard board = boards.computeIfAbsent(viewer.getUniqueId(),
        k -> Bukkit.getScoreboardManager().getNewScoreboard());

    Objective old = board.getObjective("tally_side");
    if (old != null) old.unregister();

    Objective obj = board.registerNewObjective(
        "tally_side", Criteria.DUMMY,
        Component.text("Tally", NamedTextColor.GOLD, TextDecoration.BOLD));
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);

    UUID v = viewer.getUniqueId();
    List<TallyStore.Entry> ranked = rankedFor(mode);
    ToDoubleFunction<TallyStore.Entry> value = valueFor(mode);
    Map<UUID, Change> modeChanges = changes.getOrDefault(mode, Map.of());

    List<Component> lines = new ArrayList<>();
    lines.add(Component.text("Rankings", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    int shown = Math.min(MAX_RANKS, ranked.size());
    for (int i = 0; i < shown; i++) {
      TallyStore.Entry e = ranked.get(i);
      Change ch = scrambleRankings
          ? modeChanges.getOrDefault(e.id(), NO_CHANGE)
          : NO_CHANGE;
      lines.add(rankLine(i, e, value, mode, ch));
    }

    lines.add(blank(0));
    lines.add(Component.text("Personal", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    TallyStore.Entry me = store.get(v);
    double mMin = me != null ? me.mining()      : 0;
    double mCom = me != null ? me.combat()      : 0;
    double mExp = me != null ? me.exploration() : 0;
    double mSur = me != null ? me.survival()    : 0;
    double mAdv = me != null ? me.advancement() : 0;

    boolean[] pch = scramblePersonal
        ? personalChanged.getOrDefault(v, new boolean[5])
        : new boolean[5];

    lines.add(catLine(" ⛏", "Mining", mMin, NamedTextColor.AQUA, pch[0]));
    lines.add(catLine(" ⚔", "Combat", mCom, NamedTextColor.RED, pch[1]));
    lines.add(catLine(" ✦", "Exploration", mExp, NamedTextColor.GREEN, pch[2]));
    lines.add(catLine(" ❤", "Survival", mSur, NamedTextColor.LIGHT_PURPLE, pch[3]));
    lines.add(catLine(" ★", "Advancement", mAdv, NamedTextColor.YELLOW, pch[4]));

    int score = lines.size();
    for (Component line : lines) addLine(obj, line, score--);

    viewer.setScoreboard(board);
  }

  /** One rank line. Name obfuscated iff rank moved; number iff value moved. */
  private Component rankLine(int index, TallyStore.Entry e,
                             ToDoubleFunction<TallyStore.Entry> value,
                             RankMode mode, Change ch) {
    NamedTextColor numColor = modeColor(mode);
    var b = Component.text();

    if (mode == RankMode.TOTAL) {
      boolean crowned = hasCrown(e.id());
      b.append(Component.text(" ")).append(crownsFor(e.id()));
      if (crowned) b.append(Component.text(" "));
      b.append(text(e.name() + " ", NamedTextColor.GRAY, ch.rankMoved()))
       .append(text(String.format("%.0f", value.applyAsDouble(e)),
                    NamedTextColor.WHITE, ch.valueMoved()));
    } else {
      if (index == 0) {
        b.append(Component.text(" ")).append(crown(numColor));
      } else {
        b.append(Component.text("  "));
      }
      b.append(Component.text(" "))
       .append(text(e.name() + " ", NamedTextColor.GRAY, ch.rankMoved()))
       .append(text(String.format("%.0f", value.applyAsDouble(e)),
                    numColor, ch.valueMoved()));
    }
    return b.build();
  }

  /** Text component, obfuscated iff `scramble`. */
  private static Component text(String s, NamedTextColor color,
                                boolean scramble) {
    return Component.text(s, color)
        .decoration(TextDecoration.OBFUSCATED, scramble);
  }

  // --- crowns -----------------------------------------------------------

  private static Component crown(NamedTextColor color) {
    return Component.text("♛", color);
  }

  private Component crownsFor(UUID id) {
    var c = Component.text();
    if (id.equals(leaderOverall))     c.append(crown(NamedTextColor.WHITE));
    if (id.equals(leaderMining))      c.append(crown(NamedTextColor.AQUA));
    if (id.equals(leaderCombat))      c.append(crown(NamedTextColor.RED));
    if (id.equals(leaderExploration)) c.append(crown(NamedTextColor.GREEN));
    if (id.equals(leaderSurvival))    c.append(crown(NamedTextColor.LIGHT_PURPLE));
    if (id.equals(leaderAdvancement)) c.append(crown(NamedTextColor.YELLOW));
    return c.build();
  }

  public Component crownsFor(Player p) { return crownsFor(p.getUniqueId()); }

  public boolean hasCrown(Player p) { return hasCrown(p.getUniqueId()); }

  public boolean hasCrown(UUID id) {
    return id.equals(leaderOverall) || id.equals(leaderMining)
        || id.equals(leaderCombat) || id.equals(leaderExploration)
        || id.equals(leaderSurvival) || id.equals(leaderAdvancement);
  }

  // --- helpers ----------------------------------------------------------

  private void updateTabName(Player p) {
    Component name = Component.text()
        .append(crownsFor(p))
        .append(Component.text(p.getName(), NamedTextColor.WHITE))
        .build();
    p.playerListName(name);
  }

  /** Personal category line. Only the NUMBER scrambles, iff `scramble`. */
  private static Component catLine(String icon, String name, double value,
                                   NamedTextColor color, boolean scramble) {
    return Component.text()
        .append(Component.text(icon + " ", color))
        .append(Component.text(name + " ", NamedTextColor.GRAY))
        .append(text(String.format("%.0f", value), color, scramble))
        .build();
  }

  private void addLine(Objective obj, Component text, int score) {
    String entry = LegacyComponentSerializer.legacySection().serialize(text);
    obj.getScore(entry).setScore(score);
  }

  private static Component blank(int n) {
    return Component.text("\u00A7r".repeat(n + 1));
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
        .withColor(color).withFade(color)
        .with(FireworkEffect.Type.BALL_LARGE).trail(true).build());
    meta.setPower(1);
    fw.setFireworkMeta(meta);
  }
}
