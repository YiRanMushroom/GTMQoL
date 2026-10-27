+++
title = "StructurePlanner: default structure to block list via GTCEu structure helpers, abilities never placed"
keys = [ "ability", "auto-build", "blockpatternhelper", "default_structure", "expandablepatternhelper", "getmincount", "getpredicatesfrompattern", "metamachineblock" ]
updated = "2026-10-27 15:11:11"
checked = "2026-10-27"

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
Ability = predicate whose candidates are all machine blocks (hatches, buses, mufflers, machines(...); the controller too, its position is skipped). Only DEFAULT_STRUCTURE. 1.21: ability counts 0 via the helpers' getMinCount/getMaxCount overrides; roots map since predicates lack parents. 1.20: GTCEu #5337 removed those overrides, so each char's predicate is filtered via getPredicatesFromPattern; 1.21 needs that once #5337 is ported. Block choices: autobuild.options.