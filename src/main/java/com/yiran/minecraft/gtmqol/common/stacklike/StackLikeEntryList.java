package com.yiran.minecraft.gtmqol.common.stacklike;

import brachy.modularui.integration.recipeviewer.entry.EntryList;

import java.util.List;

/** The stacks of a {@link GenericStackLikeType} ingredient, for ModularUI's recipe viewer slots. */
public record StackLikeEntryList<S>(Class<S> type, List<S> stacks) implements EntryList<S> {

    @Override
    public List<S> getStacks() {
        return stacks;
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public Class<S> getType() {
        return type;
    }
}
