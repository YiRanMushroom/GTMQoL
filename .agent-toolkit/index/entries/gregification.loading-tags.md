+++
title = "LoadingTags: reading tags while recipes load, before the new tags are bound"
keys = [ "addreloadlistenerevent", "getconditioncontext", "getitems cache", "gregification core", "icondition.icontext", "incomplete recipe", "isincomplete", "onchange", "tags during reload", "tagsupdatedevent" ]
updated = "2026-11-12 07:20:55"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "public static synchronized <T> Collection<Holder<T>> get(Registry<T> registry, ResourceLocation tag) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/LoadingTags.java"
snippet = "public static @Nullable Set<Item> items(Ingredient ingredient) {"
+++
User approved LoadingTags as a shared helper. Reads Forge's ICondition.IContext from AddReloadListenerEvent until TagsUpdatedEvent, then the registries. Users on 1.20.1: the Create fan catalysts (gregification.create-fan) and LoadingTags.items for the Create saw's MarkCoveredRecipes inputs. GTMaterialTags (MI item widening) is 1.21 only, since MI is.

Rule (2026-11-12): conversion code never asks another mod's ingredient what it takes (Ingredient getItems / test / getStackingIds, Mekanism's isIncomplete): those cache what they find for good (Forge's invalidateAll only resets getStackingIds), and conversion runs while recipes load, before the reload's tags are bound. The ingredient is the same object the other mod's recipe holds, so its own machines then took nothing from a tag until a /reload (on 1.21 Mekanism's incomplete recipe scan also crashed the server). Fixed by dropping the Mekanism isIncomplete checks, LoadingTags.items (vanilla ingredients read through toJson, compound ones by their children, null for other custom ones) for the Create saw, and the Advanced Chip Engraving product read only from items-only outputs. Item values alone need no tags, so getItems on items-only ingredients is safe (PB combBlock relies on that).