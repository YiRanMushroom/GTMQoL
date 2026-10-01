package com.yiran.minecraft.gtmqol.wireless;

import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.wireless.steam.WirelessSteamSavedData;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Only touch this class after checking that FTB Teams is loaded. No FTB types leave it, so callers load
 * fine without the mod.
 *
 * <p>Every player has a personal team whose id is the player's UUID. Its network is still separate from
 * the player's private network (see {@link NetworkId}).</p>
 */
final class FTBTeamsCompat {

    private FTBTeamsCompat() {}

    static void registerEvents() {
        TeamEvent.PLAYER_JOINED_PARTY.register(FTBTeamsCompat::onPlayerJoinedParty);
        TeamEvent.PLAYER_LEFT_PARTY.register(FTBTeamsCompat::onPlayerLeftParty);
    }

    static @Nullable UUID teamId(UUID player) {
        Team team = getTeam(player);
        return team != null ? team.getId() : null;
    }

    static @Nullable String teamName(UUID player) {
        Team team = getTeam(player);
        return team != null ? team.getName().getString() : null;
    }

    /**
     * The player's effective team: their party if they are in one, otherwise their personal team.
     * GTCEu's {@code FTBOwner.getUUID()} uses the personal team even inside a party, so it can't be used here.
     */
    private static @Nullable Team getTeam(UUID player) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) return null;
        return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player).orElse(null);
    }

    /**
     * Creating a party and joining one: the player's personal team network goes into the party.
     * FTB fires this for both (the creator joins the party they just made), and the previous team is
     * always the personal team, since leaving a party is the only way into another one.
     * Private networks are never touched.
     */
    private static void onPlayerJoinedParty(PlayerJoinedPartyTeamEvent event) {
        UUID player = event.getPlayer().getUUID();
        UUID party = event.getTeam().getId();
        GTMQoL.LOGGER.info("Wireless: {} joined team {}, moving their team network into it", player, party);
        WirelessSteamSavedData.get().moveAll(NetworkId.ofTeam(event.getPreviousTeam().getId()),
                NetworkId.ofTeam(party));
    }

    /**
     * Leaving a party (or being kicked) leaves its network behind; the last one out takes it into their
     * personal team network, since the party is deleted.
     */
    private static void onPlayerLeftParty(PlayerLeftPartyTeamEvent event) {
        if (!event.getTeamDeleted()) return;
        UUID party = event.getTeam().getId();
        GTMQoL.LOGGER.info("Wireless: team {} disbanded, moving its network to {}", party, event.getPlayerId());
        WirelessSteamSavedData.get().moveAll(NetworkId.ofTeam(party),
                NetworkId.ofTeam(event.getPlayerTeam().getId()));
    }
}
