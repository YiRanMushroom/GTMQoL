package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import appeng.api.stacks.GenericStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ME stocking input for items and fluids: the universal input without the other ME types.
 * <p>
 * It used to be a stocking bus with a second, 16 slot fluid list beside it. Such a part loads with its items in the
 * first slots ({@link MEUniversalInputPartMachine#loadAdditional} keeps the 36 slots) and its fluids added after them;
 * an old data stick copy pastes the same way.
 */
public class MEDualInputPartMachine extends MEUniversalInputPartMachine {

    /** The old fluid list's persistent trait, under the machine's {@code traitHolder} tag. */
    private static final String OLD_FLUID_TRAIT = "fluid_config";

    public MEDualInputPartMachine(BlockEntityCreationInfo info) {
        super(info, false);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        List<GenericStack> oldFluids = takeOldFluids(tag, registries);
        super.loadAdditional(tag, registries);
        if (!isAutoPull()) oldFluids.forEach(this::addConfig);
    }

    /**
     * Reads the old fluid list's config with gtceu's own format (a detached list of the same type), and removes it from
     * the tag, which would otherwise log a warning about a trait that no longer exists.
     */
    private List<GenericStack> takeOldFluids(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag traits = tag.getCompound("traitHolder");
        if (!traits.contains(OLD_FLUID_TRAIT)) return List.of();
        List<GenericStack> configs = new ArrayList<>();
        try {
            var old = new ExportOnlyAEFluidList(this, CONFIG_SIZE);
            old.getSyncDataHolder().deserializeNBT(registries, traits.getCompound(OLD_FLUID_TRAIT));
            for (ExportOnlyAEFluidSlot slot : old.getInventory()) {
                if (slot.getConfig() != null) configs.add(slot.getConfig());
            }
        } catch (RuntimeException e) {
            GTMQoL.LOGGER.warn("ME dual input at {}: couldn't read the old fluid config", getBlockPos(), e);
        }
        traits.remove(OLD_FLUID_TRAIT);
        return configs;
    }

    @Override
    protected void readConfigFromTag(HolderLookup.Provider provider, CompoundTag tag) {
        super.readConfigFromTag(provider, tag);
        // a copy from an old dual input: its fluids were apart
        if (isAutoPull() || !tag.contains("FluidConfigStacks")) return;
        CompoundTag fluids = tag.getCompound("FluidConfigStacks");
        fluids.getAllKeys().stream().sorted(Comparator.comparingInt(Integer::parseInt)).forEach(key -> {
            GenericStack config = GenericStack.readTag(provider, fluids.getCompound(key));
            if (config != null) addConfig(config);
        });
    }
}
