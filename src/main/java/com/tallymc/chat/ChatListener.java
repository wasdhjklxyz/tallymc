package com.tallymc.chat;

import com.tallymc.scoreboard.ScoreboardManager;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChatListener implements Listener {

  private final ScoreboardManager scoreboard;

  public ChatListener(ScoreboardManager scoreboard) {
    this.scoreboard = scoreboard;
  }

  @EventHandler
  public void onChat(AsyncChatEvent event) {
    event.renderer((source, sourceName, message, viewer) ->
        Component.text()
            .append(Component.text("<", NamedTextColor.WHITE))
            .append(scoreboard.crownsFor(source))
            .append(Component.text(source.getName(), NamedTextColor.WHITE))
            .append(Component.text("> ", NamedTextColor.WHITE))
            .append(message.colorIfAbsent(NamedTextColor.WHITE))
            .build()
    );
  }
}
