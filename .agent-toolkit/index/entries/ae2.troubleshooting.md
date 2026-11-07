+++
title = "Troubleshooting AE crafting, batches and the Pattern Matrix: see the 1.21 index"
kind = "howto"
keys = [ "batch debugging", "crafting job stuck", "pattern matrix bug" ]
updated = "2026-11-07 09:35:58"
checked = "2026-11-07"
+++
The full guide (a job's path from plan to matrix output, what is saved, switches, symptom -> where to look) is ae2.troubleshooting in the 1.21 index (E:\code\Minecraft\GTMQoL). Same classes and flow here; 1.20 differences in build.branch-differences (AE2 15: getOutputs() is an array, NBT instead of components, no registries in serialization, no hidden-from-recipe-viewers tag on the scaled pattern item).