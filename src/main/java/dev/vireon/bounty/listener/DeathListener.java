package dev.vireon.bounty.listener;

import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.bounty.Bounty;
import dev.vireon.bounty.util.ChatUtils;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public class DeathListener implements Listener {

    private final BountyPlugin plugin;

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (killer.getUniqueId().equals(player.getUniqueId())) return;

        Bounty bounty = plugin.getBountyManager().getBounty(player.getUniqueId());
        if (bounty == null) return;

        plugin.getBountyManager().removeBounty(player.getUniqueId());
        plugin.getEconomyManager().add(killer, bounty.getAmount());

        ChatUtils.sendMessage(killer, ChatUtils.format(
                plugin.getConfig().getString("messages.bounty-claimed"),
                Placeholder.unparsed("amount", ChatUtils.FORMATTER.format(bounty.getAmount())),
                Placeholder.unparsed("player", bounty.getPlayerName())
        ));

        String soundKey = plugin.getConfig().getString("settings.sounds.claim");
        if (soundKey != null && !soundKey.isEmpty()) {
            killer.playSound(Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

}
