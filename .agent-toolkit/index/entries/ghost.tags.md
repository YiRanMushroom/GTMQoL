+++
title = "Tag ghost ingredients: a recipe input any marked item or fluid of a tag satisfies (built 2026-11-12)"
kind = "code"
keys = [ "fluid_tag", "ghost tag", "item_tag", "itemtaglist", "lookupcontents" ]
updated = "2026-11-13 03:27:02"
checked = "2026-11-13"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/TagGhostIngredient.java"
snippet = "public abstract class TagGhostIngredient<T> extends GhostIngredient<TagKey<T>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredient.java"
snippet = "public static List<Object> lookupContents(Collection<? extends GhostIngredient<?>> marks) {"
+++
Same design as the 1.21 entry ghost.tags (there: why, MI marks). Kinds item_tag / fluid_tag with FriendlyByteBuf writer/reader instead of a StreamCodec; the name uses a hand-built tag.<registry>.<ns>.<path> key (Forge 1.20 has no Tags.getTagTranslationKey); fluid tags test defaultFluidState().is(tag), a fluid mark's tags come from BuiltInRegistries.FLUID.wrapAsHolder. Marks give tag ghosts through lookupContents in getContents (not compressIngredients, which also sees recipe contents). No MI on 1.20, so nothing here makes tag ghosts yet but KubeJS ("#ns:path" strings, TagKeys).