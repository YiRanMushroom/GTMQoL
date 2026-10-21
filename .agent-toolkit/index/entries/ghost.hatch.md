+++
title = "Ghost Ingredient Hatch: multiblock part with the marks list; counts as an input bus so any multiblock takes it"
keys = [ "ghost_ingredients", "hatch tooltip", "import_items", "partability" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientHatchPartMachine.java"
snippet = "public class GhostIngredientHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredients.java"
snippet = ".abilities(GHOST_INGREDIENTS, PartAbility.IMPORT_ITEMS)"
+++
Front overlay is gtceu's configurable maintenance hatch gear. gtceu shows <ns>.machine.<id>.tooltip on its own; adding it again with .tooltips shows it twice, so only the pattern-buffer hint is passed to .tooltips.