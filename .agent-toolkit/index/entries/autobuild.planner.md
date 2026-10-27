+++
title = "StructurePlanner: default structure to block list via GTCEu structure helpers, abilities never placed"
keys = [ "ability", "auto-build", "block choice", "block preference", "blockpatternhelper", "default_structure", "expandablepatternhelper", "getpredicatesfrompattern", "metamachineblock", "multiblockschemainfo" ]
updated = "2026-10-27 15:31:45"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "public static List<Entry> plan(MultiblockControllerMachine controller, BuildSettings settings) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static Char2ObjectMap<MultiPredicate> filter(Char2ObjectMap<MultiPredicate> predicates,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static boolean isAbility(BasePredicate predicate) {"
+++
Ability = predicate whose candidates are all machine blocks (hatches, buses, mufflers; controller too, its spot is skipped). Only DEFAULT_STRUCTURE. GTCEu #5337 removed the count overrides, so each char's predicate is filtered before layout (getPredicatesFromPattern, withContents): abilities dropped unless the char only takes them; a chosen block drops the other options. Position choices are checked against the filtered predicate. 1.21 still uses count overrides.