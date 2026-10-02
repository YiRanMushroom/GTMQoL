package com.yiran.minecraft.gtmqol.wireless;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.yiran.minecraft.gtmqol.wireless.energy.DefaultAmperageCommand;
import com.yiran.minecraft.gtmqol.wireless.energy.WirelessEnergyAccessorMachine;
import com.yiran.minecraft.gtmqol.wireless.energy.WirelessEnergyHatchPartMachine;
import com.yiran.minecraft.gtmqol.wireless.energy.WirelessEnergySavedData;
import com.yiran.minecraft.gtmqol.wireless.steam.WirelessSteamSavedData;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Which network a bound player uses. Machines only store the player ({@link WirelessBindingTrait}); the team
 * is looked up on every access, so joining or leaving a team switches networks immediately.
 * Everything here is server side.
 */
public final class WirelessNetworks {

    private WirelessNetworks() {}

    /**
     * Call from the mod constructor.
     */
    public static void init() {
        if (GTCEu.Mods.isFTBTeamsLoaded()) {
            FTBTeamsCompat.registerEvents();
        }
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, WirelessNetworks::onEntityPlace);
        MinecraftForge.EVENT_BUS.addListener(WirelessNetworks::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(DefaultAmperageCommand::register);
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        int tick = event.getServer().getTickCount();
        if (tick % IOStats.SAMPLE_INTERVAL == 0) {
            WirelessSteamSavedData.get().sampleStats(tick);
            WirelessEnergySavedData.get().sampleStats(tick);
        }
    }

    /**
     * Binds wireless machines to the player who placed them, and gives energy ones that player's default
     * amperage. Forge only posts this for entities, after
     * {@code setPlacedBy}; other placers (Building Gadgets, fake players) leave the machine unbound.
     */
    private static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player) || player instanceof FakePlayer) return;
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof MetaMachine machine)) return;
        WirelessBindingTrait binding = machine.getTrait(WirelessBindingTrait.class);
        if (binding != null) {
            binding.bind(player.getUUID(), false);
        }
        int amperage = WirelessEnergySavedData.get().getDefaultAmperage(player.getUUID());
        if (machine instanceof WirelessEnergyHatchPartMachine hatch) {
            hatch.applySettings(amperage, hatch.isOverclocked());
        } else if (machine instanceof WirelessEnergyAccessorMachine accessor) {
            accessor.setAmperage(amperage);
        }
    }

    /**
     * The player's private network if {@code privateNetwork} is set, otherwise their FTB team's (their
     * personal team without FTB Teams). {@code null} if unbound.
     */
    public static @Nullable NetworkId networkId(@Nullable UUID player, boolean privateNetwork) {
        if (player == null) return null;
        if (privateNetwork) return NetworkId.ofPrivate(player);
        UUID team = GTCEu.Mods.isFTBTeamsLoaded() ? FTBTeamsCompat.teamId(player) : null;
        return NetworkId.ofTeam(team != null ? team : player);
    }

    public static String networkName(@Nullable UUID player, boolean privateNetwork) {
        if (player == null) return "-";
        if (!privateNetwork && GTCEu.Mods.isFTBTeamsLoaded()) {
            String team = FTBTeamsCompat.teamName(player);
            if (team != null) return team;
        }
        return playerName(player);
    }

    public static String playerName(UUID player) {
        String name = UsernameCache.getLastKnownUsername(player);
        return name != null ? name : player.toString();
    }
}
