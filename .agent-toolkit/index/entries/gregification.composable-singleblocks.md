+++
title = "Composable electric single blocks: ComposableTieredMachine + SingleblockFeature (batch mode, ghost marks, pattern wear)"
keys = [ "batch_mode", "batchmodetrait", "ghost_marks", "gregification core", "iattachconfiguratorstrait", "workabletiered" ]
updated = "2026-11-08 19:50:49"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/singleblock/SingleblockFeature.java"
snippet = "public interface SingleblockFeature {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/singleblock/ComposableTieredMachine.java"
snippet = "public class ComposableTieredMachine extends SimpleTieredMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "public static GTMQoLMachineBuilder<MachineDefinition, ComposableTieredMachine> tieredMachine(String name, int tier,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/generation/GTMQoLMachineBuilder.java"
snippet = "public GTMQoLMachineBuilder<D, M> workableTiered("
+++
User asked for this so single blocks with extra features don't each need a machine class. Features attach persistent traits; an IAttachConfiguratorsTrait brings its own UI buttons, no UI mixin. Call workableTiered after the machine's own tooltips to keep GT's tooltip order. Used by gregified single blocks (all ELECTRIC_TIERS, BATCH_MODE + GHOST_MARKS), the greenhouse (no features) and the pattern crafter (PatternWear.FEATURE); steam machines not covered.