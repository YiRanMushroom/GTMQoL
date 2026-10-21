+++
title = "ExtendedAE Plus smart doubling on pattern buffers (ISmartDoublingHolder, ScaledProcessingPattern unwrap)"
keys = [ "extendedae_plus", "gtmqol.eap.mixins.json", "pushpattern" ]
updated = "2026-10-21 08:14:04"

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
Unwrap required (user decision; any pattern buffer incl. enlarged ones): AE2 pushes a ScaledProcessingPattern whose equals differs, so slot/worker matching and worker.pattern use getOriginal() (refunds match); @ModifyArg (require = 2) on both InternalSlot.pushPattern calls pushes the scaled inputs. Settings copied onto slot patterns at getAvailablePatterns HEAD and in the setters. UI: "×2" popup. EAP's holder has only the limit; on/off is ours.