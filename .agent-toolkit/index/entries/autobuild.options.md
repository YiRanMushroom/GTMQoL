+++
title = "BuildOptions: what the settings window offers, computed identically on both sides; server validates choices against it"
keys = [ "auto-build", "block choice", "block preference", "choicepoint", "expand", "getblockpreferences" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "public record BuildOptions(List<Slice> slices, List<IntIntPair> bounds, List<ChoicePoint> choices) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static void applyChoices(BlockPattern pattern, BuildSettings settings, AbstractStructureHelper helper,"
+++
A chosen block goes into getBlockPreferences() keyed by the node holding the predicate (GTCEu looks up by the node it walks, not the root); the root's other non-ability predicates get counts of 0, so a choice can make a structure with a minimum count unformable (player's choice). Block choices are not offered for expandable patterns yet (size sliders only).