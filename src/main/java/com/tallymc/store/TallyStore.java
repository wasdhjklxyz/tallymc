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
  public record Entry(UUID id, String name, int tally, long lastSeen) {}
  private final File file;
  private final Map<UUID, Entry> data = new HashMap<>();

  public TallyStore(Plugin plugin) {
    this.file = new File(plugin.getDataFolder(), "tallymc.yml");
    load();
  }

  public void put(UUID id, String name, int tally) {
    data.put(id, new Entry(id, name, tally, System.currentTimeMillis()));
  }

  public List<Entry> ranked() {
    List<Entry> list = new ArrayList<>(data.values());
    list.sort((a, b) -> Integer.compare(b.tally(), a.tally()));
    return list;
  }

  public void load() {
    if (!file.exists()) return;
    YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
    for (String key : yml.getKeys(false)) {
      try {
        UUID id = UUID.fromString(key);
        String name = yml.getString(key + ".name", "?");
        int tally   = yml.getInt(key + ".tally", 0);
        long seen   = yml.getLong(key + ".lastSeen", 0L);
        data.put(id, new Entry(id, name, tally, seen));
      } catch (IllegalArgumentException ignored) {
      }
    }
  }

  public void save() {
    YamlConfiguration yml = new YamlConfiguration();
    for (Entry e : data.values()) {
      String k = e.id().toString();
      yml.set(k + ".name",     e.name());
      yml.set(k + ".tally",    e.tally());
      yml.set(k + ".lastSeen", e.lastSeen());
    }
    try {
      file.getParentFile().mkdirs();
      yml.save(file);
    } catch (IOException ex) {
    }
  }
}
