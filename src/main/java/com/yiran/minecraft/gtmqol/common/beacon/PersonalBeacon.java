package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEMachines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.component.DataComponentType;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createSingleOverlayTieredHullMachineModel;

/**
 * The Personal Beacon ({@link PersonalBeaconMachine}), its parts, its cores and the Potion Core Infuser
 * ({@link PotionCoreInfuserMachine}): registration, structure, recipes and text. Needs wireless FE, its only energy.
 */
public final class PersonalBeacon {

    public static final String KEY = "gtmqol.personal_beacon.";

    private static final ResourceLocation CASING_TEXTURE = GTCEu
            .id("block/casings/solid/machine_casing_stable_titanium");
    private static final ResourceLocation NO_TEXTURE = GTCEu.id("block/void");

    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, GTMQoL.MOD_ID);

    /** The effect of a Potion Beacon Core. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<MobEffect>>> CORE_EFFECT =
            COMPONENTS.registerComponentType("beacon_core_effect", builder -> builder
                    .persistent(MobEffect.CODEC)
                    .networkSynchronized(MobEffect.STREAM_CODEC));

    private static final TicketController TICKETS = new TicketController(GTMQoL.id("personal_beacon"),
            (level, helper) -> {
                // a controller that is gone (its feature turned off, say) keeps no chunks loaded
                for (BlockPos owner : List.copyOf(helper.getBlockTickets().keySet())) {
                    if (!isController(level, owner)) {
                        helper.removeAllTickets(owner);
                    }
                }
            });

    /** A core with an effect of its own: id, name, effect. */
    private record EffectCore(String id, String name, Supplier<Holder<MobEffect>> effect) {}

    private static final List<EffectCore> EFFECT_CORE_TYPES = List.of(
            new EffectCore("flight_beacon_core", "Flight Beacon Core", () -> BeaconEffects.FLIGHT),
            new EffectCore("flight_speed_beacon_core", "Flight Speed Beacon Core", () -> BeaconEffects.FLIGHT_SPEED),
            new EffectCore("haste_beacon_core", "Haste Beacon Core", () -> MobEffects.DIG_SPEED),
            new EffectCore("saturation_beacon_core", "Saturation Beacon Core", () -> MobEffects.SATURATION),
            new EffectCore("resistance_beacon_core", "Resistance Beacon Core", () -> MobEffects.DAMAGE_RESISTANCE),
            new EffectCore("health_boost_beacon_core", "Health Boost Beacon Core", () -> MobEffects.HEALTH_BOOST));

    public static final List<ItemEntry<BeaconCoreItem>> EFFECT_CORES = new ArrayList<>();
    public static ItemEntry<BeaconCoreItem> POTION_CORE;
    public static MachineEntry<MachineDefinition> CORE_SOCKET;
    public static MachineEntry<MachineDefinition> BINDING_HATCH;
    public static MachineEntry<MachineDefinition> CHUNK_LOADER;
    public static MachineEntry<MachineDefinition> INFUSER;
    public static GTRecipeTypeEntry INFUSER_RECIPES;
    public static MachineEntry<MultiblockMachineDefinition> CONTROLLER;

    private PersonalBeacon() {}

    public static void init(IEventBus modBus) {
        COMPONENTS.register(modBus);
        BeaconEffects.init(modBus);
        modBus.addListener(RegisterTicketControllersEvent.class, event -> event.register(TICKETS));
        GTRegistrate registrate = GTMQoLAddon.registrate();
        for (EffectCore type : EFFECT_CORE_TYPES) {
            EFFECT_CORES.add(registrate.item(type.id(), properties -> new BeaconCoreItem(properties, type.effect()))
                    .lang(type.name())
                    .model((context, provider) -> provider.generated(context, GTMQoL.id("item/beacon_core")))
                    .register());
        }
        POTION_CORE = registrate.item("potion_beacon_core", properties -> new BeaconCoreItem(properties, null))
                .lang("Potion Beacon Core")
                .model((context, provider) -> provider.generated(context, GTMQoL.id("item/beacon_core")))
                .register();
        GTMQoLAddon.tabVariants(POTION_CORE, PersonalBeacon::potionCores);

        CORE_SOCKET = GTMQoLAddon.machine("personal_beacon_core_socket", PersonalBeaconCoreSocketMachine::new)
                .tier(EV)
                .rotationState(RotationState.NONE)
                .modelProperty(IS_FORMED, false)
                .model(socketModel())
                .langValue("Personal Beacon Core Socket")
                .tooltips(Component.translatable(KEY + "socket.desc"))
                .register();
        BINDING_HATCH = GTMQoLAddon.machine("personal_beacon_binding_hatch", PersonalBeaconBindingHatchMachine::new)
                .tier(EV)
                .rotationState(RotationState.NONE)
                .modelProperty(IS_FORMED, false)
                .model(createSingleOverlayTieredHullMachineModel(
                        GTMQoL.id("block/overlay/machine/personal_beacon_binding"), NO_TEXTURE))
                .langValue("Personal Beacon Binding Hatch")
                .tooltips(Component.translatable(KEY + "binding.desc"))
                .register();
        CHUNK_LOADER = GTMQoLAddon.machine("personal_beacon_chunk_loader", PersonalBeaconChunkLoaderMachine::new)
                .tier(EV)
                .rotationState(RotationState.NONE)
                .modelProperty(IS_FORMED, false)
                .model(createSingleOverlayTieredHullMachineModel(
                        GTMQoL.id("block/overlay/machine/personal_beacon_chunk_loader"), NO_TEXTURE))
                .langValue("Personal Beacon Chunk Loader")
                .tooltips(Component.translatable(KEY + "chunk_loader.desc"))
                .register();
        INFUSER_RECIPES = GTMQoLAddon.registrate().recipeType("potion_core_infuser", GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(0, 1, 1, 0)
                .setEUIO(IO.IN)
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
                .addCustomRecipeLogic(PotionCoreInfuserRecipes.INSTANCE)
                .setSound(GTSoundEntries.CHEMICAL)
                .lang("Potion Core Infuser")
                .register();
        INFUSER = GTMQoLAddon.machine("potion_core_infuser", PotionCoreInfuserMachine::new)
                .tier(EV)
                .recipeType(INFUSER_RECIPES)
                .rotationState(RotationState.NON_Y_AXIS)
                .modelProperty(RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .workableTieredHullModel(GTCEu.id("block/machines/brewery"))
                .langValue("Potion Core Infuser")
                .tooltips(Component.translatable(PotionCoreInfuserMachine.KEY + "desc.0"),
                        Component.translatable(PotionCoreInfuserMachine.KEY + "desc.1"),
                        Component.translatable(PotionCoreInfuserMachine.KEY + "desc.2"))
                .register();
        var controller = GTMQoLAddon.multiblock("personal_beacon", PersonalBeaconMachine::new);
        // glass around it like a beacon's: nothing behind it is hidden, and it lights up like one
        controller.block().properties(p -> p.noOcclusion().lightLevel(state -> 15)).build();
        CONTROLLER = controller
                .rotationState(RotationState.NONE)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(GTBlocks.CASING_TITANIUM_STABLE)
                .pattern(PersonalBeacon::pattern)
                .allowExtendedFacing(false)
                .allowFlip(false)
                .hasBER(true)
                .modelProperty(RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(controllerModel().andThen(b -> b.addDynamicRenderer(PersonalBeaconBeamRender::new)))
                .langValue("Personal Beacon")
                .tooltips(Component.translatable(KEY + "tooltip.0"), Component.translatable(KEY + "tooltip.1"),
                        Component.translatable(KEY + "tooltip.2"), Component.translatable(KEY + "tooltip.3"),
                        Component.translatable(KEY + "tooltip.4"))
                .register();
        addLang(registrate);
    }

    /** Vanilla's beacon on a titanium base ({@code models/block/personal_beacon}); its parts show titanium casing. */
    private static MachineBuilder.ModelInitializer controllerModel() {
        return (ctx, prov, builder) -> {
            builder.partialState().setModel(prov.models().getExistingFile(GTMQoL.id("block/personal_beacon")));
            builder.addTextureOverride("all", CASING_TEXTURE);
        };
    }

    /** An EV hull with the socket and its core on all six sides ({@code models/block/personal_beacon_core_socket}). */
    private static MachineBuilder.ModelInitializer socketModel() {
        return (ctx, prov, builder) -> {
            BlockModelBuilder model = prov.models().nested()
                    .parent(prov.models().getExistingFile(GTMQoL.id("block/personal_beacon_core_socket")));
            GTMachineModels.tieredHullTextures(model, EV);
            builder.partialState().setModel(model);
            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    private static IBlockPattern pattern(MultiblockMachineDefinition definition) {
        MultiPredicate casing = blocks(GTBlocks.CASING_TITANIUM_STABLE.get());
        MultiPredicate exposed = blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                .or(machines(CORE_SOCKET).setPreviewCount(1))
                .or(machines(BINDING_HATCH).setPreviewCount(1))
                .or(machines(CHUNK_LOADER).setPreviewCount(1))
                .or(abilities(WirelessFEMachines.FE_INPUT).setMinGlobalLimited(1).setPreviewCount(1));
        return PersonalBeaconPyramid.pattern()
                .where('c', controller(definition))
                .where('s', exposed)
                .where('i', casing)
                .where(' ', any())
                .build();
    }

    /** A Potion Beacon Core of every effect the Potion Core Infuser takes, for the creative tab. */
    private static List<ItemStack> potionCores() {
        List<ItemStack> cores = new ArrayList<>();
        BuiltInRegistries.MOB_EFFECT.holders().forEach(effect -> {
            if (!effect.value().isInstantenous()) cores.add(BeaconCoreItem.potionCore(effect));
        });
        return cores;
    }

    private static boolean isController(ServerLevel level, BlockPos pos) {
        return CONTROLLER != null && level.getBlockState(pos).is(CONTROLLER.getBlock());
    }

    static void forceChunk(ServerLevel level, BlockPos owner, ChunkPos chunk, boolean add) {
        TICKETS.forceChunk(level, owner, chunk.x, chunk.z, add, true);
    }

    public static void addRecipes(RecipeOutput provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("personal_beacon"))
                .inputItems(Items.BEACON)
                .inputItems(GTBlocks.CASING_TITANIUM_STABLE.asStack(16))
                .inputItems(GTItems.EMITTER_EV, 4)
                .inputItems(GTItems.FIELD_GENERATOR_EV, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputItems(plateDouble, Titanium, 8)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .outputItems(CONTROLLER)
                .duration(600)
                .EUt(VA[EV])
                .save(provider);
        part(provider, CORE_SOCKET, GTItems.SENSOR_EV.asStack(), Items.ENDER_EYE.getDefaultInstance());
        part(provider, BINDING_HATCH, GTItems.EMITTER_EV.asStack(), Items.ENDER_PEARL.getDefaultInstance());
        part(provider, CHUNK_LOADER, GTItems.FIELD_GENERATOR_EV.asStack(), Items.ENDER_EYE.getDefaultInstance());
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("potion_core_infuser"))
                .inputItems(GTMachines.HULL[EV].asStack())
                .inputItems(Items.BREWING_STAND)
                .inputItems(GTItems.ELECTRIC_PUMP_EV, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(plate, Titanium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .outputItems(INFUSER)
                .duration(400)
                .EUt(VA[EV])
                .save(provider);
        // after the Nanobot Beacon's modules: a titanium frame, an EV circuit and what the effect is about
        core(provider, 0, Items.ELYTRA.getDefaultInstance(), GTItems.FIELD_GENERATOR_EV.asStack());
        core(provider, 1, new ItemStack(Items.FEATHER, 16), GTItems.ELECTRIC_MOTOR_EV.asStack(2));
        core(provider, 2, new ItemStack(Items.GLOWSTONE_DUST, 16),
                GTItems.ELECTRIC_PISTON_EV.asStack(2));
        core(provider, 3, new ItemStack(Items.GOLDEN_CARROT, 16),
                Items.CAKE.getDefaultInstance());
        core(provider, 4, new ItemStack(Items.OBSIDIAN, 16),
                new ItemStack(Items.SHIELD));
        core(provider, 5, new ItemStack(Items.GOLDEN_APPLE, 8),
                new ItemStack(Items.GLISTERING_MELON_SLICE, 4));
    }

    private static void part(RecipeOutput provider, MachineEntry<MachineDefinition> part,
                             ItemStack component, ItemStack extra) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(part.getId())
                .inputItems(GTMachines.HULL[EV].asStack())
                .inputItems(component)
                .inputItems(extra)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(plate, Titanium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .outputItems(part)
                .duration(400)
                .EUt(VA[EV])
                .save(provider);
    }

    private static void core(RecipeOutput provider, int index, ItemStack theme,
                             ItemStack part) {
        ItemEntry<BeaconCoreItem> core = EFFECT_CORES.get(index);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(core.getId())
                .inputItems(frameGt, Titanium)
                .inputItems(CustomTags.EV_CIRCUITS)
                .inputItems(theme)
                .inputItems(part)
                .inputFluids(SolderingAlloy.getFluid(L))
                .outputItems(core.asStack())
                .duration(400)
                .EUt(VA[EV])
                .save(provider);
    }

    private static void addLang(GTRegistrate lang) {
        lang.addRawLang("effect.gtmqol.flight", "Flight");
        lang.addRawLang("effect.gtmqol.flight_speed", "Flight Speed");
        lang.addRawLang(KEY + "tooltip.0", "Gives the players bound in its Personal Beacon Binding Hatches the " +
                "effects of its beacon cores, in any dimension");
        lang.addRawLang(KEY + "tooltip.1", "Each core of an effect is one level of it");
        lang.addRawLang(KEY + "tooltip.2", "Every 15 s, per online player: the voltage of each effect level's " +
                "tier (level 1: LV), Flight 1A EV, in FE from Wireless FE Input Hatches");
        lang.addRawLang(KEY + "tooltip.3", "Core sockets, hatches and chunk loaders only go on the outer ring " +
                "of a layer");
        lang.addRawLang(KEY + "tooltip.4", "Potion Beacon Cores are made from potions in the Potion Core Infuser");
        lang.addRawLang(KEY + "socket.desc", "Holds one beacon core for its Personal Beacon");
        lang.addRawLang(KEY + "binding.desc", "Gives one player its Personal Beacon's effects, anywhere");
        lang.addRawLang(KEY + "chunk_loader.desc", "Keeps the chunks of its Personal Beacon loaded");
        lang.addRawLang(KEY + "potion_core.name", "Potion Beacon Core (%s)");
        lang.addRawLang(KEY + "core.tooltip", "Gives %s in a Personal Beacon");
        lang.addRawLang(KEY + "core.tooltip.levels", "Each core in the beacon adds a level");
        lang.addRawLang(KEY + "core.tooltip.single", "More than one adds nothing");
        lang.addRawLang(KEY + "socket.label", "Beacon Core");
        lang.addRawLang(KEY + "binding.unbound", "Not bound");
        lang.addRawLang(KEY + "binding.bound", "Bound to %s");
        lang.addRawLang(KEY + "binding.bind", "Bind");
        lang.addRawLang(KEY + "binding.bind.tooltip", "Bind this hatch to you");
        lang.addRawLang(KEY + "binding.unbind", "Unbind");
        lang.addRawLang(KEY + "binding.no_effects", "The beacon has no cores");
        lang.addRawLang(KEY + "binding.effects", "Your levels:");
        lang.addRawLang(KEY + "binding.row", "%s: %s / %s");
        lang.addRawLang(KEY + "binding.up", "Raise. Shift: highest");
        lang.addRawLang(KEY + "binding.down", "Lower. Shift: off");
        lang.addRawLang(KEY + "status.stopped", "Stopped");
        lang.addRawLang(KEY + "status.starting", "Starting");
        lang.addRawLang(KEY + "status.working", "Working: %s / %s s");
        lang.addRawLang(KEY + "status.output_full", "Working");
        lang.addRawLang(KEY + "status.not_formed", "Structure not formed");
        lang.addRawLang(KEY + "status.no_cores", "No beacon cores");
        lang.addRawLang(KEY + "status.no_energy", "Not enough FE");
        lang.addRawLang(KEY + "running", "Running");
        lang.addRawLang(KEY + "stopped", "Stopped");
        lang.addRawLang(KEY + "size", "Layers: %s, bound players: %s");
        lang.addRawLang(KEY + "cost", "Last cycle: %s FE");
        lang.addRawLang(KEY + "recheck", "Check structure");
        lang.addRawLang(KEY + "recheck.tooltip", "Checks the structure again, after adding a layer");
        lang.addRawLang(KEY + "chunks.on", "Chunk loading: on");
        lang.addRawLang(KEY + "chunks.off", "Chunk loading: off");
        lang.addRawLang(KEY + "effects", "Effects:");
        lang.addRawLang(KEY + "effects.none", "No beacon cores");
        lang.addRawLang(KEY + "effect", "%s %s");
        String infuser = PotionCoreInfuserMachine.KEY;
        lang.addRawLang(infuser + "desc.0", "Turns potions into time for one effect");
        lang.addRawLang(infuser + "desc.1", "Each level of a potion doubles its time, a lingering potion counts 4 times");
        lang.addRawLang(infuser + "desc.2", "24 hours of time make a Potion Beacon Core");
        lang.addRawLang(infuser + "effect", "Effect: %s");
        lang.addRawLang(infuser + "none", "Effect: none");
        lang.addRawLang(infuser + "time", "Stored: %s / %s");
        lang.addRawLang(infuser + "input", "Potions with one effect that is not instant");
        lang.addRawLang(infuser + "clear", "Clear");
        lang.addRawLang(infuser + "clear.tooltip", "Clears the stored time and effect");
    }
}
