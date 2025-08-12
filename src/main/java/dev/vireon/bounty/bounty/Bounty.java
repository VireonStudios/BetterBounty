package dev.vireon.bounty.bounty;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Bounty {

    private final UUID playerId;
    private String playerName;
    private String skinTexture;
    private long amount;
    private long lastUpdated;

}
