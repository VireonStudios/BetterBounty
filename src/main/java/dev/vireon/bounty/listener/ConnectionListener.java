package dev.vireon.bounty.listener;

import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.bounty.Bounty;
import dev.vireon.bounty.util.SkinUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {

    private final BountyPlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bounty bounty = plugin.getBountyManager().getBounty(player.getUniqueId());
        if (bounty == null) return;

        bounty.setSkinTexture(SkinUtils.getSkin(player));

        // Update the bounty name in the map so it sorts correctly
        plugin.getBountyManager().getBountyMap().updateName(player.getUniqueId(), player.getName());
    }

}
