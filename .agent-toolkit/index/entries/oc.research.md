+++
title = "Research recipes under overclocking: each OC halves the total CWU (gtceu: -2.5%), EU/t still 4x"
keys = [ "cwu", "duration_is_total_cwu", "hpca", "research station" ]
updated = "2026-10-29 06:19:28"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ResearchOverclockMixin.java"
snippet = "public class ResearchOverclockMixin {"
+++
User's choice (2026-10-29): a research recipe's duration is its total CWU and the station advances by the CWU it draws per tick (all the HPCA offers), so speed only comes from CWU/t; gtceu's ModifierFunction.build lambda takes 2.5% per OC off the total. Ours: total x 2^-OC (wraps the lambda's Math.max(FF)F; recheck lambda$build$1 on bumps). Part of the OC overhaul switch (GTMQoLMixinPlugin). Both branches.