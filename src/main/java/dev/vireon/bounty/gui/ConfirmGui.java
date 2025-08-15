package dev.vireon.bounty.gui;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.bounty.BountyResult;
import dev.vireon.bounty.util.ChatUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfirmGui {

    public static void open(Player player, BountyPlugin plugin, OfflinePlayer target, long amount) {
        Gui gui = Gui.gui()
                .title(ChatUtils.format(plugin.getConfig().getString("confirm-gui.title")))
                .rows(plugin.getConfig().getInt("confirm-gui.rows"))
                .disableAllInteractions().create();

        setItem(plugin, "confirm-gui.items.info", gui, target.getName(), amount, event -> {
        });
        setItem(plugin, "confirm-gui.items.cancel", gui, target.getName(), amount, event -> gui.close(player));
        setItem(plugin, "confirm-gui.items.confirm", gui, target.getName(), amount, event -> {
            gui.close(player);

            BountyResult result = plugin.getBountyManager().addBounty(player, target.getUniqueId(), amount);
            switch (result) {
                case SUCCESS -> {
                    if (target.isConnected()) {
                        ChatUtils.sendMessage(target.getPlayer(), ChatUtils.format(
                                plugin.getConfig().getString("messages.bounty-added-to-you"),
                                Placeholder.unparsed("amount", ChatUtils.FORMATTER.format(amount)),
                                Placeholder.unparsed("player", player.getName())
                        ));
                    }

                    ChatUtils.sendMessage(player, ChatUtils.format(
                            plugin.getConfig().getString("messages.bounty-added"),
                            Placeholder.unparsed("amount", ChatUtils.FORMATTER.format(amount)),
                            Placeholder.unparsed("player", target.getName() == null ? "---" : target.getName())
                    ));
                    player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.success")), Sound.Source.MASTER, 1.0f, 1.0f));
                }
                case MAXIMUM_EXCEEDED -> {
                    player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f));
                    ChatUtils.sendMessage(player, ChatUtils.format(plugin.getConfig().getString("messages.maximum-bounty")));
                }
                case INVALID_AMOUNT -> {
                    player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f));
                    ChatUtils.sendMessage(player, ChatUtils.format(plugin.getConfig().getString("messages.minimum-bounty")));
                }
                case NOT_ENOUGH_MONEY -> {
                    player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f));
                    ChatUtils.sendMessage(player, ChatUtils.format(plugin.getConfig().getString("messages.not-enough-money")));
                }
                case BAD_STATS_KD -> {
                    player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f));
                    double kd = plugin.getBountyManager().getPlayerKD(target.getUniqueId());
                    ChatUtils.sendMessage(player, ChatUtils.format(
                            plugin.getConfig().getString("messages.bad-stats-kd"),
                            Placeholder.unparsed("kd", String.format("%.2f", kd)),
                            Placeholder.unparsed("min_kd", String.format("%.2f", plugin.getBountyManager().getMinimumKd()))
                    ));
                }
                case BAD_STATS_DEATHS -> {
                    player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f));
                    int deaths = plugin.getBountyManager().getPlayerDeaths(target.getUniqueId());
                    ChatUtils.sendMessage(player, ChatUtils.format(
                            plugin.getConfig().getString("messages.bad-stats-deaths"),
                            Placeholder.unparsed("deaths", String.valueOf(deaths)),
                            Placeholder.unparsed("min_deaths", String.valueOf(plugin.getBountyManager().getMinimumDeaths()))
                    ));
                }
            }
        });

        gui.setCloseGuiAction(event -> {
            player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.cancel")), Sound.Source.MASTER, 1.0f, 1.0f));
        });

        gui.open(player);
    }

    private static void setItem(BountyPlugin plugin, String path, Gui gui, String playerName, long amount, GuiAction<InventoryClickEvent> action) {
        gui.setItem(plugin.getConfig().getInt(path + ".slot"),
                PaperItemBuilder.from(Material.valueOf(plugin.getConfig().getString(path + ".material")))
                        .name(ChatUtils.format(plugin.getConfig().getString(path + ".name"),
                                Placeholder.unparsed("player", playerName),
                                Placeholder.unparsed("bounty", ChatUtils.FORMATTER.format(amount))
                        ))
                        .lore(ChatUtils.format(plugin.getConfig().getStringList(path + ".lore"),
                                Placeholder.unparsed("player", playerName),
                                Placeholder.unparsed("bounty", ChatUtils.FORMATTER.format(amount))
                        )).asGuiItem(action));
    }

}
