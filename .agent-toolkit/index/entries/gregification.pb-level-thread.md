+++
title = "PB representative recipes built on the level's thread (recipe viewer worker thread crashed making bees)"
keys = [ "accessing legacyrandomsource from multiple threads", "bee entity", "buildrepresentativerecipes", "emi crash", "legacyrandomsource", "onlevelthread", "productive bees" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "static void onLevelThread(Consumer<Level> build) {"
+++
Seen on 1.21 with EMI: canner catch recipes, breeder babies, incubator baby and getCachedEntity shared the random with the server's natural spawner. Ported as is; on 1.20.1 dev the viewer is JEI, whether JEI builds on another thread is unchecked. Representative recipes get a leading '/' id (addRepresentative) to mark them synthetic.