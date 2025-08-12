package dev.vireon.bounty.database;

public enum Queries {

    CREATE_BOUNTIES_TABLE("CREATE TABLE IF NOT EXISTS %s (" +
            "uniqueId VARCHAR(36) PRIMARY KEY," +
            "playerName VARCHAR(32) NOT NULL," +
            "skinTexture TEXT," +
            "amount BIGINT NOT NULL," +
            "lastUpdated BIGINT NOT NULL" +
            ");"),

    GET_ALL("SELECT * FROM %s"),

    DELETE_BOUNTY("DELETE FROM %s WHERE uniqueId = ?"),

    SAVE_BOUNTY("REPLACE INTO %s (uniqueId, playerName, skinTexture, amount, lastUpdated) VALUES (?, ?, ?, ?, ?);"),

    ;

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQuery(Object... variables) {
        return this.query.formatted(variables);
    }

}
