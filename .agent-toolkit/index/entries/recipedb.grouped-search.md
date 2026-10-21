+++
title = "RecipeDB grouped search: why GTCEu's recipe lookup explodes with many distinct buses, and the pruning idea"
keys = [ "combinatorial", "fromholder", "lag", "pattern buffer", "performance", "recipeiterator" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"
+++
## Problem
GTCEu's `RecipeDB` is a tree (one ingredient per level, leaves = recipes). `RecipeDB.RecipeIterator` does a DFS:
1. `fromHolder` flattens every input handler of the machine into one list of ingredient entries.
2. The root pushes a search frame for every entry.
3. Whenever an entry's ingredient matches a branch node, it pushes a frame for **every** entry again (the `b -> { for (j...) stack.push(new SearchFrame(j, b)); }` lambda passed to `Either.ifRight` in `getNext`).

There is no visited set, so a node is reached once per way of picking, for each ingredient on its path, an entry holding it: cost is roughly the product of how many entries hold each ingredient. With many distinct buses / dual hatches / several buffers or ME parts on one machine sharing ingredients (circuits, solder...) this was severe lag. Most of those paths mix ingredients from two groups, which `RecipeRunner` rejects anyway.

## Idea
A recipe can only run if one handler group supplies all its inputs (one distinct bus; one pattern worker plus the buffer's shared inventory/tank and circuit; or the non-distinct pool). So only the frames pushed in step 3 are filtered: after an entry of group G, only push entries that may be in the same group (`canFollow(from, to)`). Search order stays the tree's order. The relation is checked only between neighbours on a path, so it is a relaxation: it never drops a recipe the runner would accept, it only prunes paths that mix groups. Cost: each visited branch still loops over all entries and allocates their frames before the filter (O(entries) per branch vs. the combinatorial branches removed).

## v8 savings
- 7.x: every one of a buffer's 27 slots was its own handler, so 30 pushed patterns sharing ingredients gave `30^depth` paths (the original motivation).
- v8 buffer: one worker per buffer plus one per proxy; AE2 only pushes a different pattern into a free worker, so a buffer holds at most proxies+1 patterns; its two aggregate handlers are compressed as one (equal ingredients merged across workers). Without proxies the 7.x case can't happen.
- Still a product in v8: the same ingredients in many separate RHLs (distinct buses, dual hatches, several buffers/ME parts). Grouped search makes that `buses x one bus's nodes`. A buffer with many proxies gains less (only worker-mixing paths are pruned).

Code: recipedb.grouped-list (relation), recipedb.pattern-buffer-split, recipedb.mixins, recipedb.toggle. GTCEu rules mirrored: recipedb.v8-handler-groups. History: recipedb.history-7x. 1.21.1 has the same feature.