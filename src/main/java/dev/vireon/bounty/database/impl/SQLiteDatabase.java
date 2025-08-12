package dev.vireon.bounty.database.impl;

import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.bounty.Bounty;
import dev.vireon.bounty.bounty.BountyIndex;
import dev.vireon.bounty.database.Database;
import dev.vireon.bounty.database.Queries;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@RequiredArgsConstructor
public class SQLiteDatabase implements Database {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String BOUNTIES_TABLE = "bounties";

    protected final BountyPlugin plugin;

    private File file;

    @Override
    public void onEnable() {
        plugin.getDataFolder().mkdirs();

        file = new File(plugin.getDataFolder(), "database.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create database file: " + e.getMessage());
            }
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(Queries.CREATE_BOUNTIES_TABLE.getQuery(BOUNTIES_TABLE));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Database thrown an exception!", e);
        }

        fetchAllBounties();
    }

    @Override
    public void onDisable() {
        saveAllBounties();
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Database thrown an exception!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fetchAllBounties() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(Queries.GET_ALL.getQuery(BOUNTIES_TABLE));
            while (resultSet.next()) {
                UUID playerId = UUID.fromString(resultSet.getString("uniqueId"));
                String playerName = resultSet.getString("playerName");
                String skinTexture = resultSet.getString("skinTexture");
                long amount = resultSet.getLong("amount");
                long lastUpdated = resultSet.getLong("lastUpdated");

                Bounty bounty = new Bounty(playerId, playerName, skinTexture, amount, lastUpdated);
                plugin.getBountyManager().getBountyMap().put(bounty);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Database thrown an exception!", e);
        }
    }

    @Override
    public void saveAllBounties() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(Queries.SAVE_BOUNTY.getQuery(BOUNTIES_TABLE))) {

            connection.setAutoCommit(false);

            for (Bounty bounty : plugin.getBountyManager().getAllBounties(BountyIndex.SortField.AMOUNT)) {
                statement.setString(1, bounty.getPlayerId().toString());
                statement.setString(2, bounty.getPlayerName());
                statement.setString(3, bounty.getSkinTexture());
                statement.setLong(4, bounty.getAmount());
                statement.setLong(5, bounty.getLastUpdated());
                statement.addBatch();
            }

            statement.executeBatch();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Database thrown an exception!", e);
        }
    }

    @Override
    public void removeBounty(UUID playerId) {
        EXECUTOR.execute(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(Queries.DELETE_BOUNTY.getQuery(BOUNTIES_TABLE))) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Database thrown an exception!", e);
            }
        });
    }

}
