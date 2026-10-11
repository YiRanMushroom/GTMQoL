package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe handler of a {@link GenericStackLikeRecipeCapability}. Recipe matching is here (like
 * {@code NotifiableFluidTank.handleRecipeInner}); subclasses only provide the storage: a pattern buffer slot, an
 * AE key storage, the ME network, ...
 */
public abstract class GenericStackLikeNotifiableHandler<S, I> extends NotifiableRecipeHandlerTrait<I> {

    protected final GenericStackLikeRecipeCapability<S, I> cap;
    protected final IO handlerIO;

    protected GenericStackLikeNotifiableHandler(GenericStackLikeRecipeCapability<S, I> cap, IO handlerIO) {
        this.cap = cap;
        this.handlerIO = handlerIO;
    }

    /** The current non-empty stacks. Not modified by the caller. */
    public abstract List<S> getStacks();

    /** Removes up to {@code amount} of the stack's type. @return the amount removed */
    protected abstract long extract(S stack, long amount, boolean simulate);

    /**
     * Adds up to {@code amount} of the stack's type. @return the amount added. Several simulated inserts are not
     * cumulative, so a handler with limited space may accept more in simulation than it can hold.
     */
    protected abstract long insert(S stack, long amount, boolean simulate);

    /** Called once after {@link #handleRecipeInner} changed the storage (not in simulation). */
    protected void onContentsChanged() {
        notifyListeners();
    }

    @Override
    public @NotNull List<I> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<I> left, boolean simulate) {
        if (io != handlerIO) return left;
        if (io == IO.IN) {
            handleInput(recipe, left, simulate);
        } else if (io == IO.OUT) {
            handleOutput(left, simulate);
        }
        return left;
    }

    private void handleInput(@Nullable GTRecipe recipe, List<I> left, boolean simulate) {
        var type = cap.type;
        List<S> stacks = getStacks();
        if (stacks.isEmpty()) return;
        // What is left of each stack, so a simulation sees earlier ingredients' consumption.
        long[] remaining = new long[stacks.size()];
        for (int i = 0; i < remaining.length; i++) {
            remaining[i] = type.stackAmount(stacks.get(i));
        }
        boolean changed = false;
        for (var it = left.listIterator(); it.hasNext();) {
            I ingredient = it.next();
            long amount = type.ingredientAmount(ingredient);
            for (int i = 0; i < remaining.length && amount > 0; i++) {
                if (remaining[i] <= 0) continue;
                S stack = stacks.get(i);
                if (!type.test(ingredient, stack)) continue;
                long taken = Math.min(amount, remaining[i]);
                if (!simulate) {
                    taken = extract(stack, taken, false);
                    if (taken > 0) addConsumedInput(recipe, type.copyWithAmount(stack, taken));
                }
                if (taken > 0) changed = true;
                remaining[i] -= taken;
                amount -= taken;
            }
            if (amount <= 0) it.remove();
            else it.set(type.withAmount(ingredient, amount));
        }
        if (changed && !simulate) onContentsChanged();
    }

    private void handleOutput(List<I> left, boolean simulate) {
        var type = cap.type;
        boolean changed = false;
        for (var it = left.listIterator(); it.hasNext();) {
            I ingredient = it.next();
            List<S> stacks = type.getStacks(ingredient);
            if (stacks.isEmpty()) {
                it.remove();
                continue;
            }
            long amount = type.ingredientAmount(ingredient);
            long inserted = insert(stacks.get(0), amount, simulate);
            if (inserted > 0) changed = true;
            amount -= inserted;
            if (amount <= 0) it.remove();
            else it.set(type.withAmount(ingredient, amount));
        }
        if (changed && !simulate) onContentsChanged();
    }

    /**
     * The machine whose recipe logic records consumed inputs, see {@link #addConsumedInput}. Handlers that aren't
     * attached traits (e.g. per pattern buffer slot) override this, {@link #getMachine()} throws for them.
     */
    protected @Nullable MetaMachine getRecipeMachine() {
        return getMachine();
    }

    /** Same bookkeeping as gtceu's item and fluid handlers. */
    private void addConsumedInput(@Nullable GTRecipe recipe, S consumed) {
        MetaMachine machine = getRecipeMachine();
        if (machine == null) return;
        I ingredient = cap.type.of(consumed);
        if (machine instanceof MultiblockPartMachine part) {
            for (MultiblockControllerMachine controller : part.getControllers()) {
                RecipeLogic logic = controller.getTrait(RecipeLogic.class);
                if (logic != null && logic.getStartingRecipe() == recipe) {
                    logic.getConsumedInputs().addConsumedInput(cap, ingredient);
                }
            }
        } else {
            machine.getTraitOptional(RecipeLogic.class).map(RecipeLogic::getConsumedInputs)
                    .ifPresent(inputs -> inputs.addConsumedInput(cap, ingredient));
        }
    }

    @Override
    public @NotNull List<Object> getContents() {
        return new ArrayList<>(getStacks());
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (S stack : getStacks()) {
            amount += cap.type.stackAmount(stack);
        }
        return amount;
    }

    @Override
    public RecipeCapability<I> getCapability() {
        return cap;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }
}
