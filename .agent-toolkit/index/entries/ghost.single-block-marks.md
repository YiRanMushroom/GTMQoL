+++
title = "GhostMarksTrait: ghost marks for single blocks in a popup, attached by SingleblockFeature.GHOST_MARKS"
keys = [ "iattachconfiguratorstrait" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostMarksTrait.java"
snippet = "public class GhostMarksTrait extends GhostIngredientHandler implements IAttachConfiguratorsTrait {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/singleblock/SingleblockFeature.java"
snippet = "machine.attachPersistentTrait(\"ghostMarks\", new GhostMarksTrait());"
+++
Single blocks can't have the hatch. Only attached when one of the machine's recipe types takes ghost ingredients (getMaxInputs > 0); gregified single blocks get the feature in Gregification.