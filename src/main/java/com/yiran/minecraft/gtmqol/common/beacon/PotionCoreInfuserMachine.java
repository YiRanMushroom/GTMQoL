package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import com.google.common.math.LongMath;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.V;

/**
 * Turns potion fluid into time for one effect: every tick it holds potion fluid and has 1A EV, all of it is used. A
 * bottle gives its duration × 2^(level − 1), ×4 for a lingering potion. Every {@link #CORE_TICKS} of time make a
 * Potion Beacon Core of that effect. Potions with more than one effect or an instant effect, or of another effect
 * than the stored one, don't go in.
 */
public class PotionCoreInfuserMachine extends TieredEnergyMachine implements IMuiMachine {

    /** 24 hours. */
    public static final long CORE_TICKS = 24L * 60 * 60 * 20;
    public static final int TANK_CAPACITY = 16_000;

    public static final String KEY = PersonalBeacon.KEY + "infuser.";

    @SaveField
    public final NotifiableFluidTank tank;
    @SaveField
    public final NotifiableItemStackHandler output;
    @SaveField
    @SyncToClient
    private @Nullable ResourceLocation effect;
    @SaveField
    private long storedTicks;

    private @Nullable TickableSubscription tickSubs;
    private long lastWork = Long.MIN_VALUE;

    public PotionCoreInfuserMachine(BlockEntityCreationInfo info) {
        super(info, EV, false);
        this.tank = attachTrait(new NotifiableFluidTank(1, TANK_CAPACITY, IO.IN, IO.IN));
        tank.setFilter(this::accepts);
        this.output = attachTrait(new NotifiableItemStackHandler(1, IO.OUT, IO.OUT));
    }

    /** The one effect of a potion that may go in, or null. */
    public static @Nullable MobEffectInstance effectOf(FluidStack fluid) {
        if (!PotionContainers.isPotion(fluid)) return null;
        Iterator<MobEffectInstance> effects = fluid.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .getAllEffects().iterator();
        if (!effects.hasNext()) return null;
        MobEffectInstance first = effects.next();
        if (effects.hasNext() || first.getEffect().value().isInstantenous()) return null;
        return first;
    }

    private boolean accepts(FluidStack fluid) {
        MobEffectInstance instance = effectOf(fluid);
        if (instance == null) return false;
        return effect == null || instance.getEffect().is(effect);
    }

    /** Ticks of time {@code fluid} gives. */
    public static long ticksOf(FluidStack fluid) {
        MobEffectInstance instance = effectOf(fluid);
        if (instance == null) return 0;
        long perBottle = instance.getDuration();
        perBottle = instance.getAmplifier() >= 40 ? Long.MAX_VALUE :
                LongMath.saturatedMultiply(perBottle, 1L << instance.getAmplifier());
        if (PotionContainers.container(fluid) == Items.LINGERING_POTION) {
            perBottle = LongMath.saturatedMultiply(perBottle, 4);
        }
        return LongMath.saturatedMultiply(perBottle, fluid.getAmount()) / PotionFluidHelper.BOTTLE_AMOUNT;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && tickSubs == null) tickSubs = subscribeServerTick(this::tick);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    // Untested: a potion piped or poured in turns into time at once while it has 1A EV (a strong potion doubles, a
    // lingering one ×4); a potion of another effect, two effects or an instant one is refused; at 24 h a Potion
    // Beacon Core appears; Clear empties it
    private void tick() {
        FluidStack fluid = tank.getStorages()[0].getFluid();
        boolean working = false;
        if (!fluid.isEmpty() && energyContainer.getEnergyStored() >= V[EV]) {
            MobEffectInstance instance = effectOf(fluid);
            if (instance != null && (effect == null || instance.getEffect().is(effect))) {
                effect = instance.getEffect().unwrapKey().map(ResourceKey::location).orElse(null);
                storedTicks = LongMath.saturatedAdd(storedTicks, ticksOf(fluid));
                tank.getStorages()[0].setFluid(FluidStack.EMPTY);
                energyContainer.removeEnergy(V[EV]);
                working = true;
                markAsChanged();
            }
        }
        makeCores();
        if (working) lastWork = getOffsetTimer();
        boolean shown = getOffsetTimer() - lastWork < 20;
        if (getRenderState().getValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS) !=
                (shown ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE)) {
            setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                    shown ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
        }
    }

    private void makeCores() {
        if (effect == null || storedTicks < CORE_TICKS) return;
        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(effect).orElse(null);
        if (holder == null) return;
        while (storedTicks >= CORE_TICKS) {
            ItemStack core = BeaconCoreItem.potionCore(holder);
            if (!output.storage.insertItem(0, core, true).isEmpty()) return;
            output.storage.insertItem(0, core, false);
            storedTicks -= CORE_TICKS;
            markAsChanged();
        }
    }

    private void clear() {
        effect = null;
        storedTicks = 0;
        markAsChanged();
    }

    /** h:mm:ss */
    private static String time(long ticks) {
        long seconds = ticks / 20;
        return String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var effectValue = new StringSyncValue(() -> effect == null ? "" : effect.toString());
        var timeValue = new LongSyncValue(() -> storedTicks);
        syncManager.syncValue("effect", effectValue);
        syncManager.syncValue("time", timeValue);
        syncManager.registerSyncedAction("clear", false, true, buf -> clear());
        String group = "potion_core_infuser";
        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(new TextWidget<>(Text.dynamic(() -> {
                    ResourceLocation id = effectValue.getStringValue().isEmpty() ? null :
                            ResourceLocation.tryParse(effectValue.getStringValue());
                    MobEffect stored = id == null ? null : BuiltInRegistries.MOB_EFFECT.get(id);
                    return stored == null ? Component.translatable(KEY + "none") :
                            Component.translatable(KEY + "effect", stored.getDisplayName());
                })))
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(KEY + "time",
                        time(timeValue.getLongValue()), time(CORE_TICKS)))))
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(8)
                        .child(new FluidSlot()
                                .syncHandler(new FluidSlotSyncHandler(tank.getStorages()[0])
                                        .canFillSlot(true)
                                        .canDrainSlot(true)
                                        .controlsAmount(true))
                                .tooltip(t -> t.addLine(Component.translatable(KEY + "input"))))
                        .child(new ItemSlot()
                                .slot(new ModularSlot(output.storage, 0).slotGroup(group).accessibility(false, true))
                                .background(GTGuiTextures.SLOT))
                        .child(new ButtonWidget<>()
                                .size(44, 18)
                                .overlay(Text.lang(KEY + "clear"))
                                .tooltip(t -> t.addLine(Component.translatable(KEY + "clear.tooltip")))
                                .onMousePressed((context, button) -> {
                                    syncManager.callSyncedAction("clear");
                                    return true;
                                }))));
        syncManager.registerSlotGroup(new SlotGroup(group, 1).setAllowSorting(false));
    }
}
