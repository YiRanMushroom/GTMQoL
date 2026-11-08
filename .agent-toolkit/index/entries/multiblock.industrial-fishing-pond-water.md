+++
title = "Fishing pond water surface: MultiblockFluidRendererTrait offsets + own IndustrialFishingPondWaterRender"
keys = [ "dynamicrender", "fishing pond", "fluidarearender" ]
updated = "2026-10-19 20:12:37"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/IndustrialFishingPondWaterRender.java"
snippet = "public class IndustrialFishingPondWaterRender extends DynamicRender<IndustrialFishingPondMachine, IndustrialFishingPondWaterRender> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/IndustrialFishingPondMachine.java"
snippet = "private Set<BlockPos> waterOffsets() {"
+++
GTCEu's FluidAreaRender can't be reused: it is typed to WorkableMultiblockMachine and reads recipe logic. Same FluidBlockRenderer settings as GTCEu's chem bath, always water. Registered in GTMQoLClient; 1.20.1 uses Codec.unit where 1.21 has MapCodec.unit.