package com.yiran.minecraft.gtmqol.recipedb;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroupColor;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * What {@code RecipeDB.fromHolder} returns with grouped search on: the usual ingredient list, plus for every
 * entry which recipe handler group it came from. {@code RecipeIteratorMixin} uses {@link #canFollow} to only
 * descend into combinations that one group could supply, mirroring how {@code RecipeRunner} groups handlers.
 * See docs/RECIPEDB_REFACTOR.md.
 */
public final class GroupedIngredientList extends ObjectArrayList<List<AbstractMapIngredient>> {

    /** Bypass-distinct handlers: RecipeRunner adds them to every group, so they combine with anything. */
    static final int UNIVERSAL = 0;
    /** A BUS_DISTINCT handler list: only combines with itself. */
    static final int DISTINCT = 1;
    /** Undyed or colored non-distinct handler lists: combine within a color, undyed combine with every color. */
    static final int POOLED = 2;

    /** Color of RecipeHandlerGroupColor.UNDYED (typed as RecipeHandlerGroup there). */
    private static final int UNDYED = -1;

    /** Slot of entries that are not split further (the whole handler list is one slot). */
    static final int WHOLE = 0;
    /** Circuit and shared inventories of a pattern buffer: combine with every slot of that buffer. */
    static final int CATALYST = -1;

    private final IntArrayList kinds = new IntArrayList();
    /** DISTINCT: which handler list. POOLED: the color. */
    private final IntArrayList groups = new IntArrayList();
    /** DISTINCT only: pattern buffer slot, {@link #WHOLE} or {@link #CATALYST}. */
    private final IntArrayList slots = new IntArrayList();

    public static GroupedIngredientList fromHolder(IRecipeCapabilityHolder holder) {
        GroupedIngredientList list = new GroupedIngredientList();
        int distinctLists = 0;
        for (RecipeHandlerList handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            // Same order of checks as RecipeHelper.addToRecipeHandlerMap.
            RecipeHandlerGroup group = handlerList.getGroup();
            if (handlerList.doesCapabilityBypassDistinct()) {
                list.addHandlerList(handlerList, UNIVERSAL, 0);
            } else if (group == RecipeHandlerGroupDistinctness.BUS_DISTINCT) {
                int owner = distinctLists++;
                if (GTCEu.Mods.isAE2Loaded() && PatternBufferIngredients.addSplit(list, handlerList, owner)) {
                    continue;
                }
                list.addHandlerList(handlerList, DISTINCT, owner);
            } else if (group instanceof RecipeHandlerGroupColor color) {
                list.addHandlerList(handlerList, POOLED, color.color());
            } else {
                // BYPASS_DISTINCT set as a group, or an addon's own group: don't restrict it.
                list.addHandlerList(handlerList, UNIVERSAL, 0);
            }
        }
        return list;
    }

    private void addHandlerList(RecipeHandlerList handlerList, int kind, int group) {
        handlerList.getHandlerMap().forEach((cap, handlers) -> {
            for (var handler : handlers) {
                addContents(cap, handler.getContents(), kind, group, WHOLE);
            }
        });
    }

    /** Same conversion as the original fromHolder, recording the group of each entry. */
    void addContents(RecipeCapability<?> cap, List<Object> contents, int kind, int group, int slot) {
        if (!cap.isRecipeSearchFilter() || contents.isEmpty()) {
            return;
        }
        if (cap.shouldBypassDistinct()) {
            kind = UNIVERSAL;
        }
        for (var ingredient : cap.compressIngredients(contents)) {
            add(MapIngredientTypeManager.getFrom(ingredient, cap));
            kinds.add(kind);
            groups.add(group);
            slots.add(slot);
        }
    }

    /** Whether entry {@code to} may come right after entry {@code from} on a path through the recipe tree. */
    public boolean canFollow(int from, int to) {
        int fromKind = kinds.getInt(from);
        int toKind = kinds.getInt(to);
        if (fromKind == UNIVERSAL || toKind == UNIVERSAL) {
            return true;
        }
        // RecipeRunner never mixes a distinct handler list with non-distinct ones.
        if (fromKind != toKind) {
            return false;
        }
        int fromGroup = groups.getInt(from);
        int toGroup = groups.getInt(to);
        if (fromKind == POOLED) {
            return fromGroup == UNDYED || toGroup == UNDYED || fromGroup == toGroup;
        }
        if (fromGroup != toGroup) {
            return false;
        }
        int fromSlot = slots.getInt(from);
        int toSlot = slots.getInt(to);
        return fromSlot == toSlot || fromSlot == CATALYST || toSlot == CATALYST;
    }
}
