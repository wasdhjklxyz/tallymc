package com.tallymc.tally;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class Weights {
  public static final Map<Material, Double> ORE_WEIGHTS = Map.ofEntries(
      Map.entry(Material.ANCIENT_DEBRIS,   30.0),
      Map.entry(Material.EMERALD_ORE,      25.0),
      Map.entry(Material.DIAMOND_ORE,      10.0),
      Map.entry(Material.GOLD_ORE,          3.0),
      Map.entry(Material.LAPIS_ORE,         3.0),
      Map.entry(Material.REDSTONE_ORE,      2.0),
      Map.entry(Material.IRON_ORE,          2.0),
      Map.entry(Material.COAL_ORE,          1.0),
      Map.entry(Material.NETHER_QUARTZ_ORE, 1.0),
      Map.entry(Material.COPPER_ORE,        1.0)
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

  public static final double DEEPSLATE_MULT = 1.2;

  public static final Map<EntityType, Double> KILL_WEIGHTS = Map.ofEntries(
      Map.entry(EntityType.ENDER_DRAGON,       500.0),
      Map.entry(EntityType.WITHER,             300.0),
      Map.entry(EntityType.WARDEN,             250.0),
      Map.entry(EntityType.ELDER_GUARDIAN,     100.0),
      Map.entry(EntityType.RAVAGER,             50.0),
      Map.entry(EntityType.EVOKER,              45.0),
      Map.entry(EntityType.PIGLIN_BRUTE,        30.0),
      Map.entry(EntityType.HOGLIN,              15.0),
      Map.entry(EntityType.BREEZE,              15.0),
      Map.entry(EntityType.SHULKER,             12.0),
      Map.entry(EntityType.GUARDIAN,            10.0),
      Map.entry(EntityType.BLAZE,                8.0),
      Map.entry(EntityType.GHAST,                8.0),
      Map.entry(EntityType.VINDICATOR,           8.0),
      Map.entry(EntityType.PILLAGER,             5.0),
      Map.entry(EntityType.WITHER_SKELETON,      5.0),
      Map.entry(EntityType.ENDERMAN,             4.0),
      Map.entry(EntityType.PIGLIN,               3.0),
      Map.entry(EntityType.ZOMBIFIED_PIGLIN,     3.0),
      Map.entry(EntityType.CREEPER,              3.0),
      Map.entry(EntityType.WITCH,                3.0)
  );

  public static final double WEIGHT_RAID_WIN    = 60.0;
  public static final double WEIGHT_HOSTILE_MOB = 1.0;

  public static final double EXPLORE_BIOME     = 20.0;
  public static final double EXPLORE_STRUCTURE = 30.0;

  public static final double DIV_WALK_CM        =  8_000_00.0;  //  8 km / pt
  public static final double DIV_SPRINT_CM      = 10_000_00.0;  // 10 km / pt
  public static final double DIV_SWIM_CM        =  6_000_00.0;  //  6 km / pt
  public static final double DIV_CLIMB_CM       =  5_000_00.0;  //  5 km / pt
  public static final double DIV_BOAT_CM        = 15_000_00.0;  // 15 km / pt
  public static final double DIV_HORSE_CM       = 15_000_00.0;  // 15 km / pt
  public static final double DIV_MINECART_CM    = 18_000_00.0;  // 18 km / pt
  public static final double DIV_ELYTRA_CM      = 25_000_00.0;  // 25 km / pt
  public static final double DIV_STRIDER_CM     =  6_000_00.0;  //  6 km / pt
  public static final double DIV_PIG_CM         =  5_000_00.0;  //  5 km / pt
  public static final double DIV_HAPPY_GHAST_CM = 12_000_00.0;  // 12 km / pt
  public static final double DIV_NAUTILUS_CM    = 10_000_00.0;  // 10 km / pt

  public static final double SURVIVAL_PER_HOUR           =  2.0;
  public static final double SURVIVAL_PER_ANIMAL_BRED    =  1.0;
  public static final double SURVIVAL_PER_DAMAGE_DEALT   =  0.05; // per HP
  public static final double SURVIVAL_PER_DAMAGE_TAKEN   = -0.03; // per HP
  public static final double SURVIVAL_PER_DAMAGE_BLOCKED =  0.10; // per HP
  public static final double SURVIVAL_PER_DEATH          = -10.0;

  public enum Tier {
    COMMON(5.0), UNCOMMON(15.0), HARD(40.0), EXTREME(300.0);
    public final double weight;
    Tier(double weight) { this.weight = weight; }
  }

  public static final Map<String, Tier> ADVANCEMENT_TIERS = Map.ofEntries(
      Map.entry("nether/all_effects",            Tier.EXTREME), // How Did We Get Here?
      Map.entry("nether/all_potions",            Tier.EXTREME), // A Furious Cocktail
      Map.entry("adventure/adventuring_time",    Tier.EXTREME), // all 54 biomes
      Map.entry("nether/explore_nether",         Tier.EXTREME), // Hot Tourist Destinations
      Map.entry("husbandry/balanced_diet",       Tier.EXTREME), // A Balanced Diet
      Map.entry("husbandry/bred_all_animals",    Tier.EXTREME), // Two by Two
      Map.entry("adventure/kill_all_mobs",       Tier.EXTREME), // Monsters Hunted
      Map.entry("adventure/overoverkill",        Tier.EXTREME), // Over-Overkill (mace, 50 hearts)
      Map.entry("adventure/arbalistic",          Tier.EXTREME), // 5 mobs, 1 crossbow shot

      Map.entry("nether/create_full_beacon",     Tier.HARD),    // Beaconator
      Map.entry("husbandry/froglights",          Tier.HARD),    // With Our Powers Combined!
      Map.entry("husbandry/obtain_sniffer_egg",  Tier.HARD),    // Smells Interesting
      Map.entry("husbandry/complete_catalogue",  Tier.HARD),    // A Complete Catalogue
      Map.entry("husbandry/leash_all_frog_variants", Tier.HARD),// When the Squad Hops into Town
      Map.entry("adventure/blowback",            Tier.HARD),    // Blowback
      Map.entry("adventure/hero_of_the_village", Tier.HARD),
      Map.entry("adventure/totem_of_undying",    Tier.HARD),    // Postmortal
      Map.entry("adventure/two_birds_one_arrow", Tier.HARD),
      Map.entry("adventure/sniper_duel",         Tier.HARD),
      Map.entry("adventure/trim_with_all_exclusive_armor_patterns", Tier.HARD), // Smithing with Style
      Map.entry("adventure/fall_from_world_height", Tier.HARD), // Caves & Cliffs
      Map.entry("nether/summon_wither",          Tier.HARD),
      Map.entry("nether/get_wither_skull",       Tier.HARD),    // Spooky Scary Skeleton
      Map.entry("nether/uneasy_alliance",        Tier.HARD),
      Map.entry("nether/return_to_sender",       Tier.HARD),
      Map.entry("nether/fast_travel",            Tier.HARD),    // Subspace Bubble
      Map.entry("end/kill_dragon",               Tier.HARD),    // Free the End
      Map.entry("end/levitate",                  Tier.HARD),    // Great View From Up Here
      Map.entry("end/respawn_dragon",            Tier.HARD),

      Map.entry("story/enter_the_nether",        Tier.UNCOMMON),
      Map.entry("story/follow_ender_eye",        Tier.UNCOMMON), // Eye Spy
      Map.entry("story/enter_the_end",           Tier.UNCOMMON), // The End?
      Map.entry("story/mine_diamond",            Tier.UNCOMMON), // Diamonds!
      Map.entry("story/enchant_item",            Tier.UNCOMMON), // Enchanter
      Map.entry("story/cure_zombie_villager",    Tier.UNCOMMON), // Zombie Doctor
      Map.entry("story/shiny_gear",              Tier.UNCOMMON), // Cover Me with Diamonds
      Map.entry("nether/obtain_ancient_debris",  Tier.UNCOMMON), // Hidden in the Depths
      Map.entry("nether/netherite_armor",        Tier.UNCOMMON), // Cover Me in Debris
      Map.entry("nether/create_beacon",          Tier.UNCOMMON), // Bring Home the Beacon
      Map.entry("nether/obtain_blaze_rod",       Tier.UNCOMMON), // Into Fire
      Map.entry("nether/find_bastion",           Tier.UNCOMMON), // Those Were the Days
      Map.entry("nether/find_fortress",          Tier.UNCOMMON), // A Terrible Fortress
      Map.entry("nether/loot_bastion",           Tier.UNCOMMON), // War Pigs
      Map.entry("nether/charge_respawn_anchor",  Tier.UNCOMMON),
      Map.entry("nether/distract_piglin",        Tier.UNCOMMON), // Oh Shiny
      Map.entry("end/elytra",                    Tier.UNCOMMON), // Sky's the Limit
      Map.entry("end/dragon_egg",                Tier.UNCOMMON), // The Next Generation
      Map.entry("end/dragon_breath",             Tier.UNCOMMON), // You Need a Mint
      Map.entry("end/find_end_city",             Tier.UNCOMMON),
      Map.entry("adventure/trade_at_world_height", Tier.UNCOMMON), // Star Trader
      Map.entry("adventure/voluntary_exile",     Tier.UNCOMMON),
      Map.entry("adventure/salvage_sherd",       Tier.UNCOMMON), // Respecting the Remnants
      Map.entry("adventure/craft_decorated_pot_using_only_sherds", Tier.UNCOMMON), // Careful Restoration
      Map.entry("husbandry/silk_touch_nest",     Tier.UNCOMMON), // Total Beelocation
      Map.entry("husbandry/obtain_netherite_hoe", Tier.UNCOMMON), // Serious Dedication
      Map.entry("husbandry/wax_on",              Tier.UNCOMMON),
      Map.entry("husbandry/tame_an_animal",      Tier.UNCOMMON)
  );
}
