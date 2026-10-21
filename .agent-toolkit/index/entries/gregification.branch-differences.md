+++
title = "Gregification on 1.20.1: sources and what differs from 1.21"
keys = [ "gregification core", "open question", "status" ]
updated = "2026-10-21 08:41:32"
checked = "2026-10-21"
+++
Gregification on 1.20.1, what differs from 1.21:
- Sources: Mekanism 10.4 (23 types), Create 6.0.8 (12 types), Productive Bees 1.20.1-12.6.0 (5 types + canner logic). Modern Industrialization is 1.21 only (MI on 1.20.1 is Fabric-only), so no MI source, no GTMaterialTags.
- Mek: 10.4 still has `infusion_conversion` (item -> infuse type) next to `gas_conversion`; only gas_conversion is proxied (by mek_chemical_converter). `withMoreProxies` would allow adding it if a GT type can take infuse-type outputs.
- PB: everything NBT; no stability upgrade (no chance bonus); vanilla bees have no genes (read 0). ProductiveLib: status.untested.
- 1.21's gtceufix for partial data-component ingredients (PB configurable combs) has no 1.20.1 counterpart; whether NBT comb ingredients hit a similar lookup problem in gtceu 1.20.1 is open.