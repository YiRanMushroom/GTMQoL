package com.yiran.minecraft.gtmqol.common.wireless;

import java.util.UUID;

/**
 * A wireless network. Private networks and team networks are separate pools even for the same UUID:
 * a player's personal FTB team has the player's UUID as its id, and its pool moves with the player when
 * they join a party, while the private pool never moves.
 *
 * @param id the player for private networks; the FTB team for team networks (the player's personal team,
 *           i.e. the player's UUID, without FTB Teams)
 */
public record NetworkId(boolean privateNetwork, UUID id) {

    public static NetworkId ofPrivate(UUID player) {
        return new NetworkId(true, player);
    }

    public static NetworkId ofTeam(UUID team) {
        return new NetworkId(false, team);
    }
}
