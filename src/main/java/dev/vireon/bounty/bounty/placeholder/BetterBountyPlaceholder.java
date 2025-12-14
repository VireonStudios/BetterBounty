package dev.vireon.bounty.placeholder;

import dev.vireon.bounty.BountyPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BetterBountyPlaceholder extends PlaceholderExpansion {

    private final BountyPlugin plugin;

    public BetterBountyPlaceholder(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "betterbounty";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Vireon";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0";

        if (params.equalsIgnoreCase("bounty")) {
            return String.valueOf(
                plugin.getBountyManager()
                      .getBounty(player.getUniqueId())
                      .getAmount()
            );
        }
        return null;
    }
          }
