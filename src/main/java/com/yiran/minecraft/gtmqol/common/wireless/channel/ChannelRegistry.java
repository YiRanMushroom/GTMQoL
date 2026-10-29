package com.yiran.minecraft.gtmqol.common.wireless.channel;

import net.minecraft.core.GlobalPos;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The endpoints of each channel that are loaded right now, which is what a request asks. {@link ChannelBindingTrait}
 * keeps it current. Server only; cleared when the server stops.
 */
public final class ChannelRegistry {

    private static final Int2ObjectOpenHashMap<Map<GlobalPos, ChannelBindingTrait>> LOADED =
            new Int2ObjectOpenHashMap<>();

    private ChannelRegistry() {}

    static void add(int channel, GlobalPos pos, ChannelBindingTrait endpoint) {
        LOADED.computeIfAbsent(channel, id -> new Object2ObjectLinkedOpenHashMap<>()).put(pos, endpoint);
    }

    static void remove(int channel, GlobalPos pos) {
        var endpoints = LOADED.get(channel);
        if (endpoints == null) return;
        endpoints.remove(pos);
        if (endpoints.isEmpty()) LOADED.remove(channel);
    }

    /** Loaded endpoints of the channel with that role, in the order they were loaded. */
    public static List<ChannelBindingTrait> loaded(int channel, ChannelRole role) {
        var endpoints = LOADED.get(channel);
        if (endpoints == null) return List.of();
        List<ChannelBindingTrait> list = new ArrayList<>();
        for (ChannelBindingTrait endpoint : endpoints.values()) {
            if (endpoint.role == role) list.add(endpoint);
        }
        return list;
    }

    static void onDeleted(int channel) {
        var endpoints = LOADED.remove(channel);
        if (endpoints == null) return;
        Collection<ChannelBindingTrait> copy = new ArrayList<>(endpoints.values());
        copy.forEach(ChannelBindingTrait::onChannelDeleted);
    }

    static void clear() {
        LOADED.clear();
    }
}
