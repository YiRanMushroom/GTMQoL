+++
title = "Registrate default creative tab set to null, else items added to SEARCH twice (\"already exists in the tab's list\")"
keys = [ "defaultcreativetab", "gtregistration", "main_tab" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);"
+++
1.21 only (NeoForge throws on the duplicate).