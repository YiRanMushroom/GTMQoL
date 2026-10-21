+++
title = "mek_chemical_converter: separate machine for mekanism:gas_conversion, crafted from our oxidizer"
keys = [ "infusion_conversion", "mekanism gregification", "salt", "sulfur" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "type(\"chemical_converter\", \"Mek Chemical Converter\", MekanismRecipeType.GAS_CONVERSION,"
+++
Not merged into the oxidizer type: on salt and sulfur the two give different outputs for the same input, and in one GT type gtceu's staging would keep a random one. Crafted from gtmqol:mek_chemical_oxidizer so the two hammer recipes don't clash. Mek 10.4 also has infusion_conversion (item -> infuse type), not proxied yet (1.21's chemical_conversion covers both).