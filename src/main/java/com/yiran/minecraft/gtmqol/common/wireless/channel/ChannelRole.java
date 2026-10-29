package com.yiran.minecraft.gtmqol.common.wireless.channel;

/** What an endpoint does on its channel. */
public enum ChannelRole {

    /** Puts something into the channel (an HPCA's or data bank's hatch). */
    TRANSMITTER,
    /** Takes from it (a research station's or assembly line's hatch). */
    RECEIVER,
    /** Lets a {@link ChannelKind#routerForMany} channel have any number of transmitters and receivers. */
    ROUTER;

    private static final ChannelRole[] VALUES = values();

    public static ChannelRole byId(int id) {
        return VALUES[id];
    }
}
