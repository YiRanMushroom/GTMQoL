+++
title = "PB representative recipes built on the level's thread (EMI worker thread crashed making bees)"
keys = [ "accessing legacyrandomsource from multiple threads", "bee entity", "legacyrandomsource", "onlevelthread", "productive bees" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/productivebees/ProductiveBeesGregification.java"
snippet = "static void onLevelThread(Consumer<Level> build) {"
+++
Crash came from canner catch recipes, breeder babies, incubator baby and getCachedEntity sharing the random with the server's natural spawner.