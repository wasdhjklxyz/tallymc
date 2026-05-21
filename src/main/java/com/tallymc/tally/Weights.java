package com.tallymc.tally;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class Weights {
  public static final double W_MINING      = 0.25;
  public static final double W_COMBAT      = 0.25;
  public static final double W_EXPLORATION = 0.20;
  public static final double W_SURVIVAL    = 0.10;
  public static final double W_ADVANCEMENT = 0.10;

  public static final Map<Material, Double> ORE_WEIGHTS = Map.ofEntries(
      Map.entry(Material.ANCIENT_DEBRIS,            40.0),
      Map.entry(Material.EMERALD_ORE,               25.0),
      Map.entry(Material.DIAMOND_ORE,               15.0),
      Map.entry(Material.GOLD_ORE,                   3.0),
      Map.entry(Material.LAPIS_ORE,                  3.0),
      Map.entry(Material.REDSTONE_ORE,               2.0),
      Map.entry(Material.IRON_ORE,                   2.0),
      Map.entry(Material.COAL_ORE,                   1.0),
      Map.entry(Material.NETHER_QUARTZ_ORE,          1.0),
      Map.entry(Material.COPPER_ORE,                 1.0)
  );

  public static final Map<Material, Material> DEEPSLATE_VARIANTS = Map.of(
      Material.DEEPSLATE_EMERALD_ORE,  Material.EMERALD_ORE,
      Material.DEEPSLATE_DIAMOND_ORE,  Material.DIAMOND_ORE,
      Material.DEEPSLATE_GOLD_ORE,     Material.GOLD_ORE,
      Material.DEEPSLATE_LAPIS_ORE,    Material.LAPIS_ORE,
      Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE_ORE,
      Material.DEEPSLATE_IRON_ORE,     Material.IRON_ORE,
      Material.DEEPSLATE_COAL_ORE,     Material.COAL_ORE,
      Material.DEEPSLATE_COPPER_ORE,   Material.COPPER_ORE
  );

  public static final double DEEPSLATE_MULT = 1.3;

  public static final Map<EntityType, Double> KILL_WEIGHTS = Map.of(
      EntityType.ENDER_DRAGON,   500.0,
      EntityType.WITHER,         300.0,
      EntityType.WARDEN,         250.0,
      EntityType.ELDER_GUARDIAN, 100.0,
      EntityType.PIGLIN_BRUTE,    30.0
  );

  public static final double WEIGHT_RAID_WIN     = 60.0;
  public static final double WEIGHT_HOSTILE_MOB  = 1.0;

  public static final double EXPLORE_BIOME       = 20.0;
  public static final double EXPLORE_STRUCTURE   = 30.0;
  public static final double DIV_WALK_CM         = 10_000_00.0; // 10km in cm
  public static final double DIV_BOAT_CM         = 15_000_00.0; // 15km in cm
  public static final double DIV_ELYTRA_CM       = 20_000_00.0; // 20km in cm

  public static final double SURVIVAL_PER_HOUR  = 2.0;
  public static final double SURVIVAL_PER_DEATH = -10.0;
}
