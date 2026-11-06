+++
title = "Per-side machine overlays: a DynamicRender baking quads each rebuild, like gtceu's auto output overlays"
keys = [ "bakedquadext", "dynamicrender", "facelayer", "overlay", "pitfall", "rerenderonchanged", "statefacebakery" ]
updated = "2026-11-06 18:49:21"
checked = "2026-11-06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/client/EnergyCubeSidesRender.java"
snippet = "static BakedQuad quad(Direction side"
+++
Machines render in the cutout layer; items pass other render types, so only placed machines may filter on it. gtceu$setFaceLayer isn't visible when compiling (no interface injection here): cast to BakedQuadExt. Without a face layer the quad sits above covers. The field the sides depend on needs @RerenderOnChanged. The client has no AE2 grid: sync what it needs (the cell's connected sides).