package com.tallymc.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.stream.Collectors;

public class TallyCommand implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(@NotNull CommandSender sender,
                           @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    Player target;
    if (args.length == 0) {
      if (!(sender instanceof Player p)) {
        sender.sendMessage(Component.text(
              "Must specify a player: /tally <player>", NamedTextColor.RED));
        return true;
      }
      target = p;
    } else {
      target = Bukkit.getPlayerExact(args[0]);
      if (target == null) {
        sender.sendMessage(Component.text(
              "Player not found or offline: " + args[0], NamedTextColor.RED));
        return true;
      }
    }

    target.giveExpLevels(10);

    sender.sendMessage("Hello, world!");
    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender,
                                    @NotNull Command command,
                                    @NotNull String alias,
                                    @NotNull String[] args) {
    if (args.length == 1) {
      String prefix = args[0].toLowerCase();
      return Bukkit.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(n -> n.toLowerCase().startsWith(prefix))
        .collect(Collectors.toList());
    }
    return List.of();
  }
}
