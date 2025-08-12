package dev.vireon.bounty.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyManager {

    void init();

    void remove(OfflinePlayer player, double amount);

    void add(OfflinePlayer player, double amount);

    double getMoney(OfflinePlayer player);

    void setMoney(OfflinePlayer player, double amount);

    default boolean has(OfflinePlayer player, double amount) {
        return getMoney(player) >= amount;
    }

}
