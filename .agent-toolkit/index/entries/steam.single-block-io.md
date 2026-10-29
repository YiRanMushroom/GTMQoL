+++
title = "Steam single blocks get fluid input/output tanks and a ghost circuit slot (SimpleSteamMachineMixin)"
keys = [ "programmablecircuitslottrait", "steam single blocks", "steamtweaks.circuitslots", "steamtweaks.fluidtanks", "trait priority" ]
updated = "2026-10-29 05:03:57"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssemblerUI.java"
snippet = "// A steam machine's steam tank is an IN fluid tank as well, but it is not a recipe input and has one slot."

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/SimpleSteamMachineMixin.java"
snippet = "private void gtmqol$attachTraits(CallbackInfo ci) {"
+++
Inject targets only the full constructor (require 1): since GTCEu 327 the 2-arg one delegates via this(...), so `<init>*` would attach twice. Without priority 2 the UI drew the steam tank as recipe slots and a 2-input mixer indexed past its one slot; MagicalAssemblerUI likewise skipped/crashed on the steam tank (its check is now only a fallback). 1.20.1's steam filter tests f.getFluid().defaultFluidState() (FluidStack has no is(tag)). Circuit slot visibility: steam.ghost-circuits.