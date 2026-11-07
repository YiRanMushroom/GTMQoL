+++
title = "JVM crash minutes into a world: access violation in InventoryChangeTrigger's lambda, no crash report"
kind = "status"
keys = [ "access violation", "c2", "crash", "hs_err", "inventorychangetrigger", "jdk 21.0.1", "jit", "pitfall" ]
updated = "2026-11-07 09:53:15"
checked = "2026-11-07"
+++
Dev runs die with no crash report; the evidence is `run/hs_err_pid*.log`. Problematic frame: C2-compiled `InventoryChangeTrigger$$Lambda.test`, Server thread, reading 0xbc (1.21) or 0xd4 (1.20). Seen 2026-10-01 (1.20), 2026-10-12, 2026-11-06 twice (1.21), so it predates the pattern crafter. It comes 2-3 minutes after start, when that method reaches C2.

1.21 runs on Oracle JDK 21.0.1 (`C:\Program Files\Java\jdk-21`); the 1.20 crash was on Oracle 17.0.12, so it isn't one JDK build. The faulting code reads `ItemStack.entityRepresentation` (normally null) and dereferences it; Java would throw NPE, a fatal access violation means no implicit null check was recorded, which points at the JIT. Likely a C2 bug, not confirmed: no matching public report found. Other hs_err files from 2026-10-01 fault elsewhere (nvoglv64.dll, other C2 frames), and the CPU is an i9-13900HX, so hardware instability isn't ruled out.

To try: run on a newer JDK (jdk-23.0.2 is installed; a current 21 is better), or `-XX:CompileCommand=exclude,net.minecraft.advancements.critereon.ItemPredicate::test` as a stopgap. Record here what worked.