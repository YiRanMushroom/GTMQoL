+++
title = "Tag ghost ingredients: a recipe input any marked item or fluid of a tag satisfies (built 2026-11-12)"
kind = "code"
keys = [ "fluid_tag", "ghost tag", "issatisfiedby", "item_tag", "itemtaglist", "lookupcontents" ]
updated = "2026-11-13 03:27:02"
checked = "2026-11-13"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/TagGhostIngredient.java"
snippet = "public abstract class TagGhostIngredient<T> extends GhostIngredient<TagKey<T>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredient.java"
snippet = "public static List<Object> lookupContents(Collection<? extends GhostIngredient<?>> marks) {"
+++
Built 2026-11-12 on both branches, for the user's consistency ask: gregified MI recipes whose fluid output toGT turns into GT's are marked by the GT material's fluid tag (mainOutputMark), so marking GT's fluid or a pattern making it picks them. Kinds item_tag / fluid_tag (ItemTagGhostIngredient, FluidTagGhostIngredient over TagGhostIngredient; TagKey.hashedCodec, so "#ns:path" in JSON; Serializer.of also takes TagKeys and "#ns:path" strings as item tags). Matching: GhostIngredient.isSatisfiedBy (default equals; tag kinds test the mark's item/fluid); the hatch handler and PatternOutputMarks remove a required ghost when any mark satisfies it. Lookup: mark providers return GhostIngredient.lookupContents(marks) from getContents (marks plus a tag ghost per tag of each mark's item/fluid; PatternBufferIngredients too), so the tag node of a recipe meets them; NOT in compressIngredients, which StagingRecipeDB also runs on recipe contents (recipe-side item ghosts must not get tag nodes). LootFabricatorLogic only reads ItemStack-valued marks, so the extra contents don't disturb it. Viewer: toEntryList is EntryList<?> now (tag kinds return MUI ItemTagList / FluidTagList).