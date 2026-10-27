+++
title = "BuildOptions: what the settings window offers, computed identically on both sides; server validates choices against it"
keys = [ "auto-build", "block choice", "block preference", "choicepoint", "expand", "getblockpreferences" ]
updated = "2026-10-27 15:11:12"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "public record BuildOptions(List<Slice> slices, List<IntIntPair> bounds, List<ChoicePoint> choices) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static void applyChoices(BlockPattern pattern, BuildSettings settings, AbstractStructureHelper helper,"
+++
1.21: a chosen block goes into getBlockPreferences() keyed by the node holding the predicate (GTCEu looks up by the node it walks); the root's other non-ability predicates get counts of 0. 1.20 (#5337): preference per char in MultiblockSchemaInfo, the other non-ability predicates are filtered out. Either way a choice can make a structure with a minimum count unformable (player's choice). No block choices for expandable patterns yet (size sliders only).