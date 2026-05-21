package com.tallymc.store;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TallyStore {

  public record Entry(UUID id, String name, int tally,
                       double mining, double combat, double exploration,
                       double survival, double advancement,
                       long lastSeen) {}

  private final File file;
  private final Map<UUID, Entry> data = new HashMap<>();

  public TallyStore(Plugin plugin) {
    this.file = new File(plugin.getDataFolder(), "tallymc.yml");
    load();
  }

  public void put(UUID id, String name, int tally,
                  double mining, double combat, double exploration,
                  double survival, double advancement) {
    data.put(id, new Entry(id, name, tally, mining, combat, exploration,
                           survival, advancement,
                           System.currentTimeMillis()));
  }

  public List<Entry> ranked() {
    List<Entry> list = new ArrayList<>(data.values());
    list.sort((a, b) -> Integer.compare(b.tally(), a.tally()));
    return list;
  }

  public List<Entry> entries() {
    return new ArrayList<>(data.values());
  }

  public void load() {
    if (!file.exists()) return;
    YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
    for (String key : yml.getKeys(false)) {
      try {
        UUID id = UUID.fromString(key);
        Entry e = new Entry(
            id,
            yml.getString(key + ".name", "?"),
            yml.getInt(key + ".tally", 0),
            yml.getDouble(key + ".mining", 0),
            yml.getDouble(key + ".combat", 0),
            yml.getDouble(key + ".exploration", 0),
            yml.getDouble(key + ".survival", 0),
            yml.getDouble(key + ".advancement", 0),
            yml.getLong(key + ".lastSeen", 0L));
        data.put(id, e);
      } catch (IllegalArgumentException ignored) {
      }
    }
  }

  public void save() {
    YamlConfiguration yml = new YamlConfiguration();
    for (Entry e : data.values()) {
      String k = e.id().toString();
      yml.set(k + ".name",        e.name());
      yml.set(k + ".tally",       e.tally());
      yml.set(k + ".mining",      e.mining());
      yml.set(k + ".combat",      e.combat());
      yml.set(k + ".exploration", e.exploration());
      yml.set(k + ".survival",    e.survival());
      yml.set(k + ".advancement", e.advancement());
      yml.set(k + ".lastSeen",    e.lastSeen());
    }
    try {
      file.getParentFile().mkdirs();
      yml.save(file);
    } catch (IOException ex) {
    }
  }

  public List<Entry> rankedBy(java.util.function.ToDoubleFunction<Entry> key) {
    List<Entry> list = new ArrayList<>(data.values());
    list.sort((a, b) -> Double.compare(key.applyAsDouble(b), key.applyAsDouble(a)));
    return list;
  }

  public Entry get(UUID id) {
    return data.get(id);
  }
}
