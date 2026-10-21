+++
title = "mek_chemical_converter: separate machine for mekanism:chemical_conversion, crafted from our oxidizer"
keys = [ "mekanism gregification", "salt", "sulfur", "type_chemical_conversion" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/mekanism/MekanismGregification.java"
snippet = "type(\"chemical_converter\", \"Mek Chemical Converter\", MekanismRecipeTypes.TYPE_CHEMICAL_CONVERSION,"
+++
Not merged into the oxidizer type: on salt and sulfur the two give different outputs for the same input, and in one GT type gtceu's staging set would keep a random one. Crafted from gtmqol:mek_chemical_oxidizer so the two hammer recipes don't clash.