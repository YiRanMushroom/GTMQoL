+++
title = "Steam single blocks get fluid input/output tanks and a ghost circuit slot (SimpleSteamMachineMixin)"
keys = [ "programmablecircuitslottrait", "steam single blocks", "steamtweaks.circuitslots", "steamtweaks.fluidtanks", "trait priority" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/SimpleSteamMachineMixin.java"
snippet = "private void gtmqol$attachTraits(CallbackInfo ci) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssemblerUI.java"
snippet = "// A steam machine's steam tank is an IN fluid tank as well, but it is not a recipe input and has one slot."
+++
Inject target must be `<init>*`: plain `<init>` matched only one of SimpleSteamMachine's two constructors. Without priority 2 the UI drew the steam tank as recipe slots and a 2-input mixer indexed past its one slot; MagicalAssemblerUI likewise skipped/crashed on the steam tank (its check is now only a fallback). Circuit slot visibility: steam.ghost-circuits.