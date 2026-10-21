+++
title = "BloomSafeModeMixin: stale block bloom (glow overlay) kept drawing after the last glowing face of a section went"
keys = [ "bloomrenderer", "embeddium", "invalidatesectiondata", "sodium" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/BloomSafeModeMixin.java"
snippet = "public abstract class BloomSafeModeMixin {"
+++
1.21 only, client; only matters without Sodium/Embeddium (GTCEu's SafeMode path).