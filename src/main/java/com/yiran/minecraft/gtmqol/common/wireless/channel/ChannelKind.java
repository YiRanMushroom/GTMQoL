package com.yiran.minecraft.gtmqol.common.wireless.channel;

/**
 * What a channel carries. Channels of one kind only take endpoints of that kind, and each machine's channel list
 * only shows its own kind.
 */
public enum ChannelKind {

    /** CWU/t from HPCAs and network switches to research stations and switches. */
    COMPUTATION("computation", true),
    /** Research data from data banks to assembly lines. */
    OPTICAL("optical", false);

    public final String id;
    /**
     * Without a router the channel takes at most one transmitter and one receiver; with more bound and no router,
     * nothing flows.
     */
    public final boolean routerForMany;

    ChannelKind(String id, boolean routerForMany) {
        this.id = id;
        this.routerForMany = routerForMany;
    }

    public String langKey() {
        return "gtmqol.channel.kind." + id;
    }
}
