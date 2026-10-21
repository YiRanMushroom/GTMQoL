+++
title = "StructurePlanner: default structure to block list via GTCEu structure helpers, abilities never placed"
keys = [ "ability", "abilityfilter", "addroots", "auto-build", "blockpatternhelper", "default_structure", "expandablepatternhelper", "getmincount", "roots" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "static boolean isAbility(BasePredicate predicate) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "public static List<Entry> plan(MultiblockControllerMachine controller, BuildSettings settings) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "// The roots only come out of the pattern's provider per position, so lay it out once with GTCEu's"
+++
Ability detection by predicate type name (Ability/TieredAbility) is a heuristic. Only DEFAULT_STRUCTURE, no substructures. The roots map (node -> char's predicate) exists because MultiPredicate has no parent link. 1.20.1-specific: roots are filled inside createHelper; for expandable patterns (no symbol map) by laying the pattern out once with GTCEu's defaults and asking getPredicateFromPos at every position. Block choices: autobuild.options.