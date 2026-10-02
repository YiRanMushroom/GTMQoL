package com.yiran.minecraft.gtmqol.wireless;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.wireless.energy.WirelessEnergySavedData;
import com.yiran.minecraft.gtmqol.wireless.steam.WirelessSteamSavedData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * Which player a wireless machine is bound to, and whether it uses their private or team network.
 * Machines placed by a player are bound to them (see {@link WirelessNetworks}); anything else starts unbound
 * and can be bound by anyone, from the UI or with a data stick. Only the bound player (or an operator) can
 * change or unbind it afterwards.
 *
 * <p>Data stick: shift-right-click copies the binding onto the stick (bound player only); right-click
 * pastes it (only by that same player, onto machines that are unbound or already theirs). With a stick that
 * holds no binding, right-click binds an unbound machine to the clicking player.</p>
 *
 * <p>Covers have no machine to attach a trait to, so they hold one created with
 * {@link #WirelessBindingTrait(ISyncManaged, BooleanSupplier)} as a sync field, like GTCEu's {@code FilterHandler}.
 * Only the methods of this class work then; the {@link MachineTrait} ones that need a machine throw.</p>
 */
public class WirelessBindingTrait extends MachineTrait {

    public static final String BOUND_KEY = "gtmqol.wireless.message.bound";
    public static final String UNBOUND_KEY = "gtmqol.wireless.message.unbound";
    public static final String ALREADY_BOUND_KEY = "gtmqol.wireless.message.already_bound";
    public static final String NOT_ALLOWED_KEY = "gtmqol.wireless.message.not_allowed";
    public static final String MOVED_TO_TEAM_KEY = "gtmqol.wireless.message.moved_to_team";
    public static final String COPIED_KEY = "gtmqol.wireless.message.copied";
    public static final String STICK_OTHER_PLAYER_KEY = "gtmqol.wireless.message.stick_other_player";
    public static final String STICK_NAME_KEY = "gtmqol.wireless.data_stick.name";

    private static final String STICK_TAG = "GTMQoLWirelessBinding";

    @SaveField
    @SyncToClient
    private @Nullable UUID boundPlayer;

    @SaveField
    @SyncToClient
    private boolean privateNetwork;

    private final @Nullable ISyncManaged owner;
    private final @Nullable BooleanSupplier ownerRemote;

    public WirelessBindingTrait() {
        this.owner = null;
        this.ownerRemote = null;
    }

    /**
     * Not attached to a machine; for covers.
     */
    public WirelessBindingTrait(ISyncManaged owner, BooleanSupplier isRemote) {
        this.owner = owner;
        this.ownerRemote = isRemote;
    }

    @Override
    public @Nullable ISyncManaged getParentSyncObject() {
        return owner != null ? owner : super.getParentSyncObject();
    }

    @Override
    public boolean isRemote() {
        return ownerRemote != null ? ownerRemote.getAsBoolean() : super.isRemote();
    }

    @Override
    public void markAsChanged() {
        if (owner == null) {
            super.markAsChanged();
            return;
        }
        // ISyncManaged's default, which MachineTrait overrides.
        owner.markAsChanged();
        owner.getSyncDataHolder().setHasDirtyChildSyncObject(true);
    }

    public @Nullable UUID getBoundPlayer() {
        return boundPlayer;
    }

    public boolean isBound() {
        return boundPlayer != null;
    }

    public boolean isPrivateNetwork() {
        return privateNetwork;
    }

    /**
     * Server only; {@code null} on the client or while unbound.
     */
    public @Nullable NetworkId getNetworkId() {
        if (isRemote()) return null;
        return WirelessNetworks.networkId(boundPlayer, privateNetwork);
    }

    public String getNetworkName() {
        if (isRemote()) return "";
        return WirelessNetworks.networkName(boundPlayer, privateNetwork);
    }

    public String getBoundPlayerName() {
        if (isRemote()) return "";
        return boundPlayer == null ? "-" : WirelessNetworks.playerName(boundPlayer);
    }

    /**
     * No permission check; for placement. Players go through the methods below. {@code null} unbinds.
     */
    public void bind(@Nullable UUID player, boolean privateNetwork) {
        this.boundPlayer = player;
        this.privateNetwork = privateNetwork;
        syncDataHolder.markClientSyncFieldDirty("boundPlayer");
        syncDataHolder.markClientSyncFieldDirty("privateNetwork");
    }

    private void unbind() {
        bind(null, false);
    }

    private boolean canConfigure(Player player) {
        return player.getUUID().equals(boundPlayer) || player.hasPermissions(2);
    }

    /**
     * Sends the player a message and returns false if they may not change this machine.
     */
    private boolean checkConfigure(Player player) {
        if (canConfigure(player)) return true;
        player.displayClientMessage(Component.translatable(NOT_ALLOWED_KEY, getBoundPlayerName()), true);
        return false;
    }

    /**
     * The bind/unbind button: anyone can bind an unbound machine to themselves, in team mode.
     */
    public void toggleBinding(Player player) {
        if (boundPlayer == null) {
            bind(player.getUUID(), false);
            player.displayClientMessage(Component.translatable(BOUND_KEY, getBoundPlayerName()), true);
        } else if (checkConfigure(player)) {
            unbind();
            player.displayClientMessage(Component.translatable(UNBOUND_KEY), true);
        }
    }

    public void setPrivateNetwork(Player player, boolean privateNetwork) {
        if (boundPlayer == null || !checkConfigure(player)) return;
        this.privateNetwork = privateNetwork;
        syncDataHolder.markClientSyncFieldDirty("privateNetwork");
    }

    /**
     * Moves the bound player's whole private network (steam and EU) into their current team network and
     * switches this machine to team mode.
     */
    public void movePrivateToTeam(Player player) {
        if (boundPlayer == null || !checkConfigure(player)) return;
        NetworkId from = NetworkId.ofPrivate(boundPlayer);
        NetworkId to = WirelessNetworks.networkId(boundPlayer, false);
        var steam = WirelessSteamSavedData.get();
        BigInteger steamAmount = steam.getStored(from);
        steam.moveAll(from, to);
        var energy = WirelessEnergySavedData.get();
        BigInteger energyAmount = energy.getStored(from);
        energy.moveAll(from, to);
        this.privateNetwork = false;
        syncDataHolder.markClientSyncFieldDirty("privateNetwork");
        player.displayClientMessage(Component.translatable(MOVED_TO_TEAM_KEY,
                FormattingUtil.formatNumbers(steamAmount), FormattingUtil.formatNumbers(energyAmount),
                getNetworkName()), true);
    }

    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            if (boundPlayer == null) {
                player.displayClientMessage(Component.translatable(NOT_ALLOWED_KEY, "-"), true);
            } else if (player.getUUID().equals(boundPlayer)) {
                CompoundTag binding = new CompoundTag();
                binding.putUUID("player", boundPlayer);
                binding.putBoolean("private", privateNetwork);
                CompoundTag tag = new CompoundTag();
                tag.put(STICK_TAG, binding);
                dataStick.setTag(tag);
                dataStick.setHoverName(Component.translatable(STICK_NAME_KEY, getBoundPlayerName()));
                player.displayClientMessage(Component.translatable(COPIED_KEY), true);
            } else {
                player.displayClientMessage(Component.translatable(NOT_ALLOWED_KEY, getBoundPlayerName()), true);
            }
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = dataStick.getTag();
            if (tag != null && tag.contains(STICK_TAG)) {
                pasteBinding(player, tag.getCompound(STICK_TAG));
            } else if (boundPlayer == null) {
                bind(player.getUUID(), false);
                player.displayClientMessage(Component.translatable(BOUND_KEY, getBoundPlayerName()), true);
            } else {
                player.displayClientMessage(Component.translatable(ALREADY_BOUND_KEY, getBoundPlayerName()), true);
            }
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    private void pasteBinding(Player player, CompoundTag binding) {
        UUID stickPlayer = binding.getUUID("player");
        if (!player.getUUID().equals(stickPlayer)) {
            player.displayClientMessage(Component.translatable(STICK_OTHER_PLAYER_KEY,
                    WirelessNetworks.playerName(stickPlayer)), true);
            return;
        }
        if (boundPlayer != null && !boundPlayer.equals(stickPlayer)) {
            player.displayClientMessage(Component.translatable(ALREADY_BOUND_KEY, getBoundPlayerName()), true);
            return;
        }
        bind(stickPlayer, binding.getBoolean("private"));
        player.displayClientMessage(Component.translatable(BOUND_KEY, getBoundPlayerName()), true);
    }
}
