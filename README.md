# TallyMC

A Minecraft plugin that calculates a single networth-style number for each
player, computed from cumulative stats and advancements. Weights are tuned so
that the different playstyles (mining, combat, exploration, survival,
progression) are roughly comparable - no single path runs away with the
leaderboard.

## How the score works

Each of the five categories produces a raw sub-score. That raw score is divided
by a **fixed par target** (not the live server max), giving a 0–1 fraction. The
fractions are weighted and summed, then scaled to 0–100.

```
networth = 100 × (0.25 × min(1, mining_raw      / par_mining) +
                  0.25 × min(1, combat_raw      / par_combat) +
                  0.20 × min(1, explore_raw     / par_explore) +
                  0.10 × min(1, survival_raw    / par_survival) +
                  0.10 × min(1, advancement_raw / par_advancement))
```

### Why fixed par instead of server-max

A par target is a configurable "this is what a 100% in this category looks
like" reference value. Because it does not depend on other players, a player's
networth **only changes when that player actually does something**. It never
drops because someone else mined more debris. This makes the leaderboard a
stable ranking rather than a moving target.

The trade-off: par values are an estimate and need tuning. The defaults below
are aimed at a moderately active SMP player over roughly a few months of play.
**Run the server for a week or two, look at real numbers, then adjust par.** A
category where everyone instantly hits 1.0 has par set too low; one where
nobody breaks 0.3 has it set too high.

The `min(1, ...)` clamp means a category caps at 100% - going far beyond par
does not keep inflating the score, which keeps any one grind from dominating.

## Default weights

All values below are defaults and are fully customizable in `config.yml`.

### Mining sub-score

Raw mining score is the sum of `(blocks_mined × weight)` for each ore. Deepslate
variants get a 1.3× multiplier on top of the base weight, since they are harder
to mine.

| Ore                 | Weight | Notes                                         |
|---------------------|--------|-----------------------------------------------|
| ancient_debris      | 40     | Netherite grind; the real endgame mining |
| emerald_ore         | 25     | Rare, single-block veins                      |
| diamond_ore         | 15     | Base; most diamonds come from deepslate |
| gold_ore            | 3      | Common-ish; also obtainable via bartering     |
| lapis_ore           | 3      |                                               |
| redstone_ore        | 2      |                                               |
| iron_ore            | 2      |                                               |
| coal_ore            | 1      |                                               |
| nether_quartz_ore   | 1      | Mined in bulk                                 |
| copper_ore          | 1      | Common                                        |
| deepslate_* variant | ×1.3   | Multiplier applied on top of base weight      |

Note: deepslate diamond is the normal way to get diamonds (15 × 1.3 = 19.5),
so that figure is effectively your real diamond weight.

**Default par_mining: `2000`**

### Combat sub-score

Raw combat score is the sum of `(kills × weight)`.

| Kill                | Weight | Notes                                       |
|---------------------|--------|---------------------------------------------|
| ender_dragon        | 500    | Major milestone; see caveat below           |
| wither              | 300    |                                             |
| warden              | 250    | Hardest non-boss mob                        |
| elder_guardian      | 100    |                                             |
| raid_win            | 60     | Completing a village raid                   |
| piglin_brute        | 30     |                                             |
| hostile_mob         | 1      | Generic floor for every other hostile mob   |

Caveat: dragon kill credit goes to whoever lands the final hit. On an SMP this
can cause drama. Consider configuring the dragon kill as a one-time server-wide
bonus instead of per-player (config option `dragon_kill_mode: server | player`).

**Default par_combat: `1500`**

### Exploration sub-score

```
biomes_visited       × 20       (via advancement)
structures_found     × 30       (via advancement)
distance_walked      ÷ 10000    (1 pt per 10 km)
distance_by_boat     ÷ 15000    (1 pt per 15 km)
distance_by_elytra   ÷ 20000    (1 pt per 20 km)
```

Walking is worth more per km than flying by design - the category rewards
exploration *effort*, and elytra travel is cheap once you have wings.

**Default par_explore: `1000`**

### Survival sub-score

```
play_time_hours      × 2        (presence matters a little)
deaths               × -10      (mild penalty, keeps it honest)
```

The raw survival score is **clamped to a minimum of 0**, so a player with many
deaths cannot drag their total networth negative.

**Default par_survival: `300`**

### Advancement sub-score

Each advancement is bucketed into a difficulty tier. Raw score is the sum of
`(advancements_earned × tier_weight)`.

| Tier     | Weight | Example                          |
|----------|--------|----------------------------------|
| common   | 5      | "Stone Age", first crafts        |
| uncommon | 15     | "Diamonds!", first Nether visit  |
| hard     | 40     | "Cover Me With Diamonds"         |
| extreme  | 300    | "How Did We Get Here?"           |

Any advancements added in 26.1 should be assigned to a tier manually in
`config.yml` - the tier *buckets* are version-stable, but the membership list
is not auto-generated.

**Default par_advancement: `800`**

## Category weights summary

| Category    | Weight | Default par |
|-------------|--------|-------------|
| Mining      | 0.25   | 2000        |
| Combat      | 0.25   | 1500        |
| Exploration | 0.20   | 1000        |
| Survival    | 0.10   | 300         |
| Advancement | 0.10   | 800         |

Category weights sum to 1.00. Final networth is on a 0–100 scale.

## Tuning checklist

1. Ship with the defaults above.
2. Let the SMP run ~1–2 weeks.
3. For each category, check the spread of sub-score fractions across players.
4. If most players sit near 1.0 → raise that par. If most sit below ~0.3 →
   lower it.
5. Re-tune individual ore / mob / advancement weights only if one specific
   activity is clearly over- or under-rewarded relative to effort.

## Future

Chest / ender-chest / block-ownership tracking and the chest-hover overlay are
**deliberately not included**. Minecraft has no native API for "who placed this
block" or for player chest contents - implementing it means logging every
`BlockPlaceEvent` to persistent storage and scanning inventories on open/close,
which is a large, performance-sensitive, and privacy-invasive feature in its
own right. It is best built as a separate plugin or a v2 feature once core
scoring is stable.
