+++
title = "ExtendedAE Plus smart doubling on pattern buffers (ISmartDoublingHolder, ScaledProcessingPattern unwrap)"
keys = [ "extendedae_plus", "gtmqol.eap.mixins.json", "pushpattern" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/eap/MEPatternBufferSmartDoublingMixin.java"
snippet = "    @ModifyVariable(method = \"pushPattern\", at = @At(\"HEAD\"), argsOnly = true)\n    private IPatternDetails gtmqol$unwrapScaledPattern(IPatternDetails patternDetails) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/SmartDoubling.java"
snippet = "    public static IPatternDetails unwrap(IPatternDetails pattern) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/EAPMixinPlugin.java"
snippet = "    private static final String EAP_MOD_ID = \"extendedae_plus\";"

[[items]]
name = "neoforge-holder"
value = "NeoForge EAP's ISmartDoublingHolder only has the upper limit; the on/off switch is a method our mixin adds."
+++
The unwrap is required (user decision): slot/worker matching and worker.pattern use the original so refunds match, while @ModifyArg (require = 2) on both InternalSlot.pushPattern calls still pushes the scaled inputs. Settings (toggle default on, limit 0 = none) are copied onto slot patterns at getAvailablePatterns HEAD and in the setters. UI: "x2" left configurator popup. NeoForge EAP's ISmartDoublingHolder only has the limit; the on/off is ours.