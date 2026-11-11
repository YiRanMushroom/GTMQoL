+++
title = "Dynamic renders (fishing pond water, fusion ring) culled with the controller's block: render box not forwarded (1.21)"
kind = "code"
keys = [ "blockentitywithbermodelrenderer", "culling", "dynamic render", "fishing pond water", "frustum", "getrenderboundingbox" ]
updated = "2026-11-11 08:19:08"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/BlockEntityWithBERModelRendererMixin.java"
snippet = "public AABB getRenderBoundingBox(T blockEntity) {"

[[links]]
path = "src/main/resources/gtmqol.mixins.json"
snippet = "gtceufix.BlockEntityWithBERModelRendererMixin"
+++
GTCEu 1.21's BlockEntityWithBERModelRenderer doesn't override NeoForge's IBlockEntityRendererExtension.getRenderBoundingBox(T), so the frustum check uses the default box (the block itself) and MachineModel.getRenderBoundingBox, which merges the boxes of the machine's dynamic renders, is never asked. Anything a dynamic render draws away from the controller disappears once the controller's block leaves the view: our Industrial Fishing Pond's water, GT's fusion ring, and the like. The mixin adds the override and forwards to the block's baked model when it is an IBlockEntityRendererBakedModel of that block entity type. Client mixin. 1.20 doesn't need it: MetaMachine.getRenderBoundingBox() forwards there. Remove once GTCEu forwards it itself (check on a bump, skill.gtceu-upgrade).