+++
title = "Steam single blocks get fluid input/output tanks and a ghost circuit slot (SteamTweaks)"
keys = [ "programmablecircuitslottrait", "steam single blocks", "steamtweaks.circuitslots", "steamtweaks.fluidtanks", "trait priority" ]
updated = "2026-11-08 19:26:09"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/SteamTweaks.java"
snippet = "private static void attachSingleblockTraits(SimpleSteamMachine machine) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssemblerUI.java"
snippet = "// A steam machine's steam tank is an IN fluid tank as well, but it is not a recipe input and has one slot."
+++
Attached through MachineFeatures.onCreated (api.machine-features), after the whole constructor ran and before the saved data is read; the persistent trait names ("circuit", "gtmqol_import_fluids", "gtmqol_export_fluids") are saved with the machine, keep them. Without priority 2 the UI drew the steam tank as recipe slots and a 2-input mixer indexed past its one slot; MagicalAssemblerUI likewise skipped/crashed on the steam tank (its check is now only a fallback). Circuit slot visibility: steam.ghost-circuits.