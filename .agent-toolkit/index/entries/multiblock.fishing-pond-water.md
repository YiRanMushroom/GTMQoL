+++
title = "Fishing pond water surface: MultiblockFluidRendererTrait offsets + own FishingPondWaterRender"
keys = [ "dynamicrender", "fishing pond", "fluidarearender" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/client/FishingPondWaterRender.java"
snippet = "public class FishingPondWaterRender extends DynamicRender<FishingPondMachine, FishingPondWaterRender> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/FishingPondMachine.java"
snippet = "private Set<BlockPos> waterOffsets() {"
+++
GTCEu's FluidAreaRender can't be reused: it is typed to WorkableMultiblockMachine and reads recipe logic. Same FluidBlockRenderer settings as GTCEu's chem bath, always water. Registered in GTMQoLClient; 1.20.1 uses Codec.unit where 1.21 has MapCodec.unit.