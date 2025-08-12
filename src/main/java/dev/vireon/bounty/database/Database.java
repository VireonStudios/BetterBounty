package dev.vireon.bounty.database;

import java.sql.Connection;
import java.util.UUID;

public interface Database {

    void onEnable();

    void onDisable();

    Connection getConnection();

    void fetchAllBounties();

    void saveAllBounties();

    void removeBounty(UUID playerId);

}
