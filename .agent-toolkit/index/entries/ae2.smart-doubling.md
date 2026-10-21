+++
title = "ExtendedAE Plus smart doubling on pattern buffers (ISmartDoublingHolder, ScaledProcessingPattern unwrap)"
keys = [ "extendedae_plus", "gtmqol.eap.mixins.json", "pushpattern" ]
updated = "2026-10-21 08:27:56"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/EAPMixinPlugin.java"
snippet = "    private static final String EAP_MOD_ID = \"extendedae_plus\";"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/eap/MEPatternBufferSmartDoublingMixin.java"
snippet = "    @ModifyVariable(method = \"pushPattern\", at = @At(\"HEAD\"), argsOnly = true)\n    private IPatternDetails gtmqol$unwrapScaledPattern(IPatternDetails patternDetails) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/SmartDoubling.java"
snippet = "    public static IPatternDetails unwrap(IPatternDetails pattern) {"
+++
The unwrap is required (user decision): slot/worker matching and worker.pattern use the original so refunds match, while @ModifyArg (require = 2) on both InternalSlot.pushPattern calls still pushes the scaled inputs. Settings (toggle default on, limit 0 = none) are copied onto slot patterns at getAvailablePatterns HEAD and in the setters. UI: "×2" left configurator popup. On 1.20.1 (EAP 1.6.1) ISmartDoublingHolder has both the toggle and the limit; 1.21's NeoForge EAP only has the limit.