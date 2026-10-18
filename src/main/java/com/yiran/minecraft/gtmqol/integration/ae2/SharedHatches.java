package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroupColor;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.widgets.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pattern buffers stay distinct, but what a slot is missing for a recipe may come from the machine's shared parts:
 * its undyed buses and hatches that aren't distinct (the ones gtceu adds to every non-distinct group), and the parts
 * that bypass distinctness (ghost ingredient hatches...; a slot only matches when it takes all the contents, so these
 * are needed here too). A slot still has to provide something. A toggle on the buffer, on by default; proxies follow
 * their buffer.
 */
public final class SharedHatches {

    public static final String TOGGLE_KEY = "gtmqol.gui.pattern_buffer.shared_hatches";

    private SharedHatches() {}

    /** A pattern buffer machine, with the toggle. */
    public interface Holder {

        boolean gtmqol$sharesHatches();

        void gtmqol$setSharesHatches(boolean shares);
    }

    /** A pattern buffer's (or proxy's) recipe handler list. */
    public interface HandlerList {

        boolean gtmqol$sharesHatches();

        /** The shared parts of the machine handling a recipe right now; empty otherwise. */
        void gtmqol$setSharedHatches(List<RecipeHandlerList> shared);
    }

    /** The shared parts among a machine's input handler lists. */
    public static List<RecipeHandlerList> of(List<RecipeHandlerList> inputs, RecipeHandlerList self) {
        List<RecipeHandlerList> shared = new ArrayList<>();
        for (RecipeHandlerList list : inputs) {
            if (list != self && (list.doesCapabilityBypassDistinct() ||
                    RecipeHandlerGroupColor.UNDYED.equals(list.getGroup()))) {
                shared.add(list);
            }
        }
        return shared;
    }

    /** What is left of {@code left} after the shared parts. */
    public static Map<RecipeCapability<?>, List<Object>> handle(List<RecipeHandlerList> shared, IO io, GTRecipe recipe,
                                                               Map<RecipeCapability<?>, List<Object>> left,
                                                               boolean simulate) {
        for (RecipeHandlerList list : shared) {
            if (left.isEmpty()) break;
            left = list.handleRecipe(io, recipe, left, simulate);
        }
        return left;
    }

    public static MachineUIPanelBuilder addToggle(MachineUIPanelBuilder builder, Holder holder) {
        var value = new BooleanSyncValue(holder::gtmqol$sharesHatches, holder::gtmqol$setSharesHatches).allowC2S();
        var right = builder.rightConfigurators();
        return builder.rightConfigurators(right.andThen(f -> f.child(new ToggleButton()
                .value(value)
                .overlay(GTGuiTextures.BUTTON_ITEM_ALLOW_INPUT_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(r -> r
                        .addLine(Text.lang(TOGGLE_KEY + (value.getBoolValue() ? ".enabled" : ".disabled")))
                        .addLine(Text.lang(TOGGLE_KEY + ".info"))))));
    }
}
