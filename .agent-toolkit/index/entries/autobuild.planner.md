+++
title = "StructurePlanner: default structure to block list via GTCEu structure helpers, abilities never placed"
keys = [ "ability", "auto-build", "blockpatternhelper", "default_structure", "expandablepatternhelper", "getpredicatesfrompattern", "metamachineblock", "multiblockschemainfo" ]
updated = "2026-10-27 15:16:02"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "public static List<Entry> plan(MultiblockControllerMachine controller, BuildSettings settings) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static Char2ObjectMap<MultiPredicate> filter(Char2ObjectMap<MultiPredicate> predicates,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "static boolean isAbility(BasePredicate predicate) {"
+++
Ability = predicate whose candidates are all machine blocks (hatches, buses, mufflers, machines(...); the controller too, its position is skipped). Only DEFAULT_STRUCTURE. GTCEu #5337 (build 325) removed the helpers' min/max count overrides, so each char's predicate is filtered before GTCEu lays it out (getPredicatesFromPattern override, MultiPredicate.withContents): abilities dropped unless the char only takes abilities. 1.21 still uses the old count overrides. Block choices: autobuild.options.