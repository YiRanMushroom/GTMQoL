+++
title = "BuildOptions: what the settings window offers, computed identically on both sides; server validates choices against it"
keys = [ "auto-build", "block choice", "block preference", "choicepoint", "expand", "getblockpreferences" ]
updated = "2026-10-27 15:15:51"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "public record BuildOptions(List<Slice> slices, List<IntIntPair> bounds, List<ChoicePoint> choices) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static Char2ObjectMap<BasePredicate> applyChoices(BlockPattern pattern, BuildSettings settings,"
+++
A chosen block becomes GTCEu's block preference for its char (MultiblockSchemaInfo.getBlockPreferences) and the char's other non-ability predicates are filtered out (autobuild.planner), so a choice can make a structure with a minimum count unformable (player's choice). Block choices are not offered for expandable patterns yet (size sliders only). BuildOptions is the same file on 1.21.1.