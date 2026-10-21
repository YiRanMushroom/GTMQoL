+++
title = "Missing-part highlight of an unformed multiblock: fix 0-255 colors and make it draw through blocks (1.21)"
keys = [ "aabbhighlightrenderer", "depth test", "gtmqolrendertypes", "invalid structure", "renderlinebox", "see_through_lines" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/AABBHighlightColorMixin.java"
snippet = "public class AABBHighlightColorMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/AABBHighlightSeeThroughMixin.java"
snippet = "public class AABBHighlightSeeThroughMixin {"
+++
1.21 only, client. The highlight is the button under "Invalid structure" in the multiblock UI. 1.20.1's GTCEu draws its own no-depth quads and has neither bug. SEE_THROUGH_LINES = vanilla lines without depth test/write, drawn to the main target.