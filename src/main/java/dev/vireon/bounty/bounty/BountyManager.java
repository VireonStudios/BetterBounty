package dev.vireon.bounty.bounty;

import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.util.SkinUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
public class BountyManager {

    private final BountyPlugin plugin;

    // Map to store bounties by player UUID
    @Getter
    private final BountyIndex bountyMap = new BountyIndex();

    @Getter
    private long max = -1;
    @Getter
    private long min = 1;

    public void init() {
        max = plugin.getConfig().getLong("settings.maximum-bounty", 100_000_000);
        min = plugin.getConfig().getLong("settings.minimum-bounty", 1L);
    }

    public BountyResult addBounty(Player player, UUID playerId, long amount) {
        Bounty currentBounty = bountyMap.get(playerId).orElse(null);
        if (max != -1 && amount > max) return BountyResult.MAXIMUM_EXCEEDED;
        if (amount < min) return BountyResult.INVALID_AMOUNT;
        if (!plugin.getEconomyManager().has(player, amount)) return BountyResult.NOT_ENOUGH_MONEY;

        if (currentBounty == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            Bounty bounty = new Bounty(playerId, offlinePlayer.getName(), SkinUtils.getSkin(offlinePlayer), amount, System.currentTimeMillis());
            bountyMap.put(bounty);
        } else {
            bountyMap.addAmount(playerId, amount);
            bountyMap.touchLastUpdated(playerId, System.currentTimeMillis());
        }

        plugin.getEconomyManager().remove(player, amount);

        return BountyResult.SUCCESS;
    }

    public Bounty getBounty(UUID playerId) {
        return bountyMap.get(playerId).orElse(null);
    }

    public Collection<Bounty> getAllBounties(BountyIndex.SortField sortField) {
        return bountyMap.listAll(sortField);
    }

    public void removeBounty(UUID playerId) {
        bountyMap.remove(playerId);
        plugin.getDatabase().removeBounty(playerId);
    }

}
