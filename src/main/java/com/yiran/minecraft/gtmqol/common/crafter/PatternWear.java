package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRecipeLogicModifierTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.yiran.minecraft.gtmqol.common.singleblock.SingleblockFeature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Inputs that change with every craft, like tools: they stay in the machine's inputs and each craft of a run, parallels
 * included, is really simulated on them. {@link #LIMIT} comes after overclocking and cuts a run down to what the
 * inputs still allow; the trait wears them when a run starts, which also covers GT repeating the same recipe. Only
 * patterns with such inputs get here (the recipe holds the pattern), others never simulate.
 * <p>
 * Only single items are worn, a stack of several has nowhere to put the changed one.
 */
public final class PatternWear extends MachineTrait implements IRecipeLogicModifierTrait {

    public static final SingleblockFeature FEATURE = machine -> machine.attachPersistentTrait("patternWear",
            new PatternWear());

    static final String PATTERN_KEY = "crafter_pattern";
    static final String WORN_OUT_KEY = "gtmqol.machine.pattern_crafter.worn_out";

    public static final RecipeModifier LIMIT = (machine, recipe) -> {
        ICrafterPattern pattern = pattern(recipe, machine.getLevel());
        if (pattern == null) return ModifierFunction.IDENTITY;
        int crafts = crafts(recipe);
        int possible = simulate(buses((IRecipeCapabilityHolder) machine), pattern, crafts).crafts;
        if (possible >= crafts) return ModifierFunction.IDENTITY;
        if (possible == 0) return ModifierFunction.cancel(Component.translatable(WORN_OUT_KEY));
        // a little over: contents are cut down to whole numbers
        ContentModifier cut = ContentModifier.multiplier((possible + 1e-9) / crafts);
        ModifierFunction modifier = ModifierFunction.builder().inputModifier(cut).outputModifier(cut).build();
        return current -> {
            GTRecipe limited = modifier.apply(current);
            limited.parallels = 1;
            limited.batchParallels = 1;
            limited.subtickParallels = possible;
            return limited;
        };
    };

    private record Simulation(int crafts, List<Runnable> writes) {}

    private static int crafts(GTRecipe recipe) {
        return recipe.parallels * recipe.subtickParallels * recipe.batchParallels;
    }

    private static @Nullable ICrafterPattern pattern(GTRecipe recipe, Level level) {
        if (!recipe.data.contains(PATTERN_KEY)) return null;
        CompoundTag tag = recipe.data.getCompound(PATTERN_KEY);
        return ICrafterPattern.decode(ItemStack.parseOptional(level.registryAccess(), tag), level);
    }

    private static List<NotifiableItemStackHandler> buses(IRecipeCapabilityHolder holder) {
        List<NotifiableItemStackHandler> buses = new ArrayList<>();
        for (var handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            for (var handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                if (handler instanceof NotifiableItemStackHandler items) buses.add(items);
            }
        }
        return buses;
    }

    /**
     * Wears the inputs of {@code pattern} that change, found among {@code buses}, by {@code crafts} crafts.
     *
     * @return false, leaving them as they are, when they can't do that many
     */
    public static boolean wear(List<NotifiableItemStackHandler> buses, ICrafterPattern pattern, int crafts) {
        Simulation simulation = simulate(buses, pattern, crafts);
        if (simulation.crafts < crafts) return false;
        simulation.writes.forEach(Runnable::run);
        return true;
    }

    /** Up to {@code wanted} crafts on copies; {@code writes} puts the result in the machine. */
    private static Simulation simulate(List<NotifiableItemStackHandler> buses, ICrafterPattern pattern, int wanted) {
        int possible = wanted;
        List<Runnable> writes = new ArrayList<>();
        var inputs = pattern.itemInputs();
        for (int input = 0; input < inputs.size(); input++) {
            if (!inputs.get(input).changes()) continue;
            int done = 0;
            for (NotifiableItemStackHandler items : buses) {
                for (int slot = 0; slot < items.storage.getSlots() && done < wanted; slot++) {
                    ItemStack stack = items.storage.getStackInSlot(slot);
                    if (stack.getCount() != 1 || !inputs.get(input).ingredient().test(stack)) continue;
                    ItemStack current = stack;
                    // what changes may stop matching (exact patterns: one craft per tool)
                    while (done < wanted && !current.isEmpty() && inputs.get(input).ingredient().test(current)) {
                        current = pattern.afterCraft(input, current.copy());
                        done++;
                    }
                    int written = slot;
                    ItemStack result = current;
                    writes.add(() -> items.storage.setStackInSlot(written, result));
                }
            }
            possible = Math.min(possible, done);
        }
        return new Simulation(possible, writes);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null) return true;
        ICrafterPattern pattern = pattern(recipe, getMachine().getLevel());
        if (pattern == null) return true;
        var machine = (IRecipeLogicMachine) getMachine();
        int crafts = crafts(recipe);
        Simulation simulation = simulate(buses(machine), pattern, crafts);
        if (simulation.crafts < crafts) {
            // a repeated recipe that no longer fits: look for it again, LIMIT cuts it down
            machine.getRecipeLogic().markLastRecipeDirty();
            return false;
        }
        simulation.writes.forEach(Runnable::run);
        return true;
    }
}
