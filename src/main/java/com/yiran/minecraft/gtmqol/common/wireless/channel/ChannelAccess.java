package com.yiran.minecraft.gtmqol.common.wireless.channel;

/** Who sees and binds a channel. The owner and operators always can. */
public enum ChannelAccess {

    /** Only the owner. */
    PRIVATE,
    /** The owner's FTB team (their personal team without FTB Teams, so then only the owner). */
    TEAM,
    /** Everyone. */
    PUBLIC;

    private static final ChannelAccess[] VALUES = values();

    public static ChannelAccess byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : PRIVATE;
    }

    public ChannelAccess next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public String langKey() {
        return "gtmqol.channel.access." + name().toLowerCase();
    }

    /** Short form for the list. */
    public String shortLangKey() {
        return langKey() + ".short";
    }
}
