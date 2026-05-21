package com.tallymc.command;

import com.tallymc.tally.Calculator;
import com.tallymc.tally.Calculator.Result;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.stream.Collectors;

public class TallyCommand implements CommandExecutor, TabCompleter {
  private static final TextColor BORDER = TextColor.color(0x4A4A4A);
  private static final TextColor TITLE  = TextColor.color(0xFFD24A);
  private static final TextColor LABEL  = TextColor.color(0xB8B8B8);

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

    showTally(sender, target);
    return true;
  }

  private boolean showTally(CommandSender sender, Player target) {
    Result r = Calculator.compute(target);
    Component bar = Component.text(
        "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", BORDER);
    String name = target.getName() != null ? target.getName() : "Unkown";

    sender.sendMessage(bar);
    sender.sendMessage(Component.text()
        .append(Component.text(
            "  " + name,
            TextColor.color(0xFFFFFF), TextDecoration.BOLD))
        .append(Component.text(
            "'s Tally",
            TITLE, TextDecoration.BOLD))
        .build());

    sender.sendMessage(bar);
    sender.sendMessage(line(
          "⛏", "Mining", r.miningTally(), TextColor.color(0x4FC3F7)));
    sender.sendMessage(line(
          "⚔", "Combat", r.combatTally(), TextColor.color(0xEF5350)));
    sender.sendMessage(line(
          "✦", "Exploration", r.explorationTally(), TextColor.color(0x66BB6A)));
    sender.sendMessage(line(
          "❤", "Survival", r.survivalTally(), TextColor.color(0xCE93D8)));
    sender.sendMessage(line(
          "★", "Advancement", r.advancementTally(), TextColor.color(0xFFCA28)));

    sender.sendMessage(bar);
    sender.sendMessage(Component.text()
        .append(Component.text("  Total ", TITLE, TextDecoration.BOLD))
        .append(Component.text(
            String.format("%.0f", r.tally()),
            TextColor.color(0xFFFFFF), TextDecoration.BOLD))
        .build());
    sender.sendMessage(bar);

    return true;
  }

  private static Component line(String icon, String name, double tally,
                                TextColor color) {
    return Component.text()
        .append(Component.text("  " + icon + " ", color))
        .append(Component.text(name + " ", LABEL))
        .append(Component.text(String.format("%.0f", tally), color))
        .build();
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
