+++
title = "Dimensionally Transcendent Fusion Reactor: fusion recipes of any tier, no buffer/heat/tier cap, white ring render"
keys = [ "dtfr", "dtfr_ring", "dtfrringrender", "eu_to_start" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/client/GTMQoLClient.java"
snippet = "DynamicRenderManager.register(GTMQoL.id(\"dtfr_ring\"), DTFRRingRender.TYPE);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/DTFRMachine.java"
snippet = "public class DTFRMachine extends WorkableElectricMultiblockMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "private static void initDTFR() {"
+++
Port of 7.x. Plain workable machine, so eu_to_start is ignored. Layout is fusion MK3 with every fluid slot as 'X'; energy slots also take substation and laser hatches (new vs 7.x). DTFRRingRender is gtceu's FusionRingRender without bloom, in white; the machine holds the fade state because the render instance is shared (1.20.1 uses getDeltaFrameTime). Crafted in the magical assembler at UV.