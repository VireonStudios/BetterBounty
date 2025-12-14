package dev.vireon.bounty.placeholder;

import dev.vireon.bounty.BountyPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("amount")) {
            if (player == null) return "0";

            return String.valueOf(
                    plugin.getBountyManager()
                            .getBounty(player.getUniqueId())
                            .getAmount()
            );
        }

        return null;
    }

}