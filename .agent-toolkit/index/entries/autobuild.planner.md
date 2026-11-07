+++
title = "StructurePlanner: default structure to block list via GTCEu structure helpers, abilities never placed"
keys = [ "ability", "auto-build", "block choice", "block preference", "blockpatternhelper", "default_structure", "expandablepatternhelper", "getpredicatesfrompattern", "metamachineblock", "multiblockschemainfo" ]
updated = "2026-11-07 08:03:30"
checked = "2026-11-07"

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
Ability = candidates are all machines registered to a PartAbility; other machines get placed. Only DEFAULT_STRUCTURE. GTCEu #5337 removed the count overrides, so each char's predicate is filtered before layout (getPredicatesFromPattern, withContents): abilities dropped unless the char only takes them; a chosen block drops the other options. Position choices are checked against the filtered predicate. 1.21 still uses count overrides.