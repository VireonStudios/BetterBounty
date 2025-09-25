package dev.vireon.bounty.command;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.bounty.BountyIndex;
import dev.vireon.bounty.gui.ConfirmGui;
import dev.vireon.bounty.gui.MainGui;
import dev.vireon.bounty.util.ChatUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("bounty")
public class MainCommand extends BaseCommand {

    private final BountyPlugin plugin;

    public MainCommand(BountyPlugin plugin) {
        super(plugin.getConfig().getStringList("settings.commands"));
        this.plugin = plugin;
    }

    @Default
    public void onDefault(Player player) {
        MainGui.open(player, BountyIndex.SortField.AMOUNT, null, plugin);
    }

    @SubCommand("add")
    public void onAdd(Player player, Player target, long amount) {
        ConfirmGui.open(player, plugin, target, amount);
    }

    @SubCommand("remove")
    @Permission("bounty.remove")
    public void onRemove(CommandSender sender, @Suggestion("online-players") String targetName) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore()) {
            ChatUtils.sendMessage(sender, ChatUtils.format(plugin.getConfig().getString("messages.player-not-found")));
            return;
        }

        BountyIndex bountyIndex = plugin.getBountyManager().getBountyMap();
        if (bountyIndex.remove(target.getUniqueId())) {
            ChatUtils.sendMessage(sender, ChatUtils.format(
                    plugin.getConfig().getString("messages.bounty-removed"),
                    Placeholder.unparsed("player", target.getName() == null ? "---" : target.getName())
            ));
        } else {
            ChatUtils.sendMessage(sender, ChatUtils.format(plugin.getConfig().getString("messages.no-bounty")));
        }
    }

    @SubCommand("reload")
    @Permission("bounty.reload")
    public void onReload(CommandSender sender) {
        plugin.getConfig().reload();
        ChatUtils.sendMessage(sender, ChatUtils.format(plugin.getConfig().getString("messages.reload")));
    }

}
