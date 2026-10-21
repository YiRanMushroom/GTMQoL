+++
title = "ForeignMachineType record: one foreign recipe type -> GT recipe type + single blocks or multiblock"
keys = [ "catalyst", "counterpart", "gregification core", "singleblock", "withcrafting", "withmodifiers", "withmoreproxies", "withproxies", "withrecipetype", "withtooltip" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/ForeignMachineType.java"
snippet = "public record ForeignMachineType(String name, String englishName, Supplier<List<RecipeType<?>>> proxies,"
+++
Proxies and converter are both nullable: PB's incubator/gene extractor/beehive/breeder have neither and get all recipes from custom recipe logic added via withRecipeType. withMoreProxies lets one type take several foreign types through one converter (Create deployer: deploying + item_application + sandpaper_polishing).