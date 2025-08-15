package dev.vireon.bounty.bounty;

import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.util.SkinUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
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
    
    // Stat blocking configuration
    @Getter
    private boolean statBlockingEnabled = false;
    @Getter
    private String statBlockingMode = "kd";
    @Getter
    private double minimumKd = 1.0;
    @Getter
    private int minimumDeaths = 10;

    public void init() {
        max = plugin.getConfig().getLong("settings.maximum-bounty", 100_000_000);
        min = plugin.getConfig().getLong("settings.minimum-bounty", 1L);
        
        // Load stat blocking configuration
        statBlockingEnabled = plugin.getConfig().getBoolean("settings.stat-blocking.enabled", false);
        statBlockingMode = plugin.getConfig().getString("settings.stat-blocking.mode", "kd");
        minimumKd = plugin.getConfig().getDouble("settings.stat-blocking.minimum-kd", 1.0);
        minimumDeaths = plugin.getConfig().getInt("settings.stat-blocking.minimum-deaths", 10);
    }

    public BountyResult addBounty(Player player, UUID playerId, long amount) {
        Bounty currentBounty = bountyMap.get(playerId).orElse(null);
        if (max != -1 && amount > max) return BountyResult.MAXIMUM_EXCEEDED;
        if (amount < min) return BountyResult.INVALID_AMOUNT;
        if (!plugin.getEconomyManager().has(player, amount)) return BountyResult.NOT_ENOUGH_MONEY;
        
        // Check player stats if stat blocking is enabled
        if (statBlockingEnabled) {
            BountyResult statResult = checkPlayerStats(playerId);
            if (statResult != BountyResult.SUCCESS) {
                return statResult;
            }
        }

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
    
    /**
     * Check if a player meets the stat requirements for having a bounty placed on them
     * @param playerId The UUID of the player to check
     * @return BountyResult.SUCCESS if stats are acceptable, or a specific error result
     */
    private BountyResult checkPlayerStats(UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        
        // If player has never played, they have no stats, so block the bounty
        if (!offlinePlayer.hasPlayedBefore()) {
            // Treat new players as having bad stats
            return "kd".equals(statBlockingMode) ? BountyResult.BAD_STATS_KD : BountyResult.BAD_STATS_DEATHS;
        }
        
        // Only check stats for online players since we need access to their statistics
        if (!offlinePlayer.isOnline()) {
            // For offline players, we can't check their stats, so allow the bounty
            // This is a reasonable compromise since most stat checking will happen for active players
            return BountyResult.SUCCESS;
        }
        
        Player onlinePlayer = offlinePlayer.getPlayer();
        if (onlinePlayer == null) {
            return BountyResult.SUCCESS;
        }
        
        if ("kd".equals(statBlockingMode)) {
            // Check K/D ratio
            int kills = onlinePlayer.getStatistic(Statistic.PLAYER_KILLS);
            int deaths = onlinePlayer.getStatistic(Statistic.DEATHS);
            
            // If player has no deaths, their K/D is effectively infinite (good)
            if (deaths == 0) {
                return kills >= 1 ? BountyResult.SUCCESS : BountyResult.BAD_STATS_KD;
            }
            
            double kd = (double) kills / deaths;
            return kd >= minimumKd ? BountyResult.SUCCESS : BountyResult.BAD_STATS_KD;
        } else {
            // Check minimum deaths
            int deaths = onlinePlayer.getStatistic(Statistic.DEATHS);
            return deaths >= minimumDeaths ? BountyResult.SUCCESS : BountyResult.BAD_STATS_DEATHS;
        }
    }
    
    /**
     * Get the K/D ratio for a player (for error messages)
     * @param playerId The UUID of the player
     * @return The K/D ratio, or 0.0 if unable to calculate
     */
    public double getPlayerKD(UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (!offlinePlayer.isOnline()) return 0.0;
        
        Player onlinePlayer = offlinePlayer.getPlayer();
        if (onlinePlayer == null) return 0.0;
        
        int kills = onlinePlayer.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = onlinePlayer.getStatistic(Statistic.DEATHS);
        
        return deaths == 0 ? kills : (double) kills / deaths;
    }
    
    /**
     * Get the death count for a player (for error messages)
     * @param playerId The UUID of the player
     * @return The death count, or 0 if unable to get
     */
    public int getPlayerDeaths(UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (!offlinePlayer.isOnline()) return 0;
        
        Player onlinePlayer = offlinePlayer.getPlayer();
        if (onlinePlayer == null) return 0;
        
        return onlinePlayer.getStatistic(Statistic.DEATHS);
    }

}
