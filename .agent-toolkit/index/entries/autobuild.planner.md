+++
title = "StructurePlanner: default structure to block list via GTCEu structure helpers, abilities never placed"
keys = [ "ability", "abilityfilter", "auto-build", "blockpatternhelper", "default_structure", "expandablepatternhelper", "getmincount" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "public static List<Entry> plan(MultiblockControllerMachine controller, BuildSettings settings) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static AbstractStructureHelper createHelper(IBlockPattern pattern, BuildSettings settings,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "static boolean isAbility(BasePredicate predicate) {"
+++
Ability detection by predicate type name (Ability/TieredAbility) is a heuristic. Only DEFAULT_STRUCTURE, no substructures. The roots map exists because GTCEu predicates no longer know their parent (1.20.1 lacks MultiPredicate.getParent() too). Block choices: autobuild.options.