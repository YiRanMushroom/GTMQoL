+++
title = "Naming and player text: class, ID, lang, config and item names, tooltips, UI labels; use when naming or writing them"
keys = [ "id", "lang", "naming", "tooltip", "user-facing text", "wording" ]
updated = "2026-11-08 04:40:39"
checked = "2026-11-08"

[[items]]
name = "renamed-ids"
value = "Old block/item IDs mapped to new ones"
path = "src/main/java/com/yiran/minecraft/gtmqol/RenamedIds.java"
snippet = "private static final Map<String, String> RENAMED = Map.of("

[[items]]
name = "tooltip-desc-keys"
value = "Example of .desc keys for tooltips passed to .tooltips()"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEDualParts.java"
snippet = "private static final String UNIVERSAL_INPUT_TOOLTIP = \"gtmqol.machine.me_universal_input.desc\";"
+++
Use whenever you name something (class, field, method, block or item ID, lang key, config key, index entry) or write text players see (block and item names, tooltips, UI labels, chat messages, config comments, README).

## Names
- The user's words for a feature are working names and often loose ("pattern matrix"). Before one goes into code, IDs or text, think about what the thing is and does, and pick a name that is easy to understand and easy to search for, in the style of GTCEu and AE2. The Pattern Matrix became the Assembling Matrix: it assembles crafting recipes in bulk, as AE2's Molecular Assembler does one at a time. When the better name differs much from the user's words, say so.
- One concept, one name in every layer: class (AssemblingMatrixMachine), ID (assembling_matrix), lang keys (gtmqol.assembling_matrix.), config key (ae2.assemblingMatrix), index entry and player text use the same words. Parts carry the whole's name (Assembling Matrix Pattern Core), not a shortened one.
- A name says what the thing is or does, not how it was built, what it replaced or what it was called in a conversation.
- Renaming a block or item ID that worlds may already hold: add old -> new to RenamedIds (registry aliases on 1.21, MissingMappingsEvent on 1.20), so saved blocks, machine contents and item stacks load under the new ID. Old IDs stay there for good.

## Player text
- Context-free: written for someone who has never seen the code, the index, the handoff notes or the conversation. No internal names, nicknames or words that only mean something to the developers ("our patterns", "the buffer", "batches", "the work").
- Name things fully and concretely, by the names the player sees in the game (an item's in-game name, a block's name, a button's label), above all the first time: "Crafting Patterns encoded in a Pattern Crafter", not "our patterns".
- Few pronouns (it, they, this, its), and never as the first mention of something; repeat the name when "it" could mean two things.
- Not verbose, but detailed where it helps: every sentence says something the player needs to decide or act; no filler, no implementation details, no history.
- Names say what the thing is or does, in the game's and the parent mod's naming style.
- Tooltips GT-style: short and plain, only what affects the player's decision (no tick counts, batch sizes, nothing the structure preview already shows).

## Machine tooltip lang keys
gtceu's MetaMachineBlock.appendHoverText shows <ns>.machine.<id>.tooltip by itself. Either just register lang under that key (gtceu shows it; ProbableDevices, Create sequenced assembly) or, for tooltips passed via .tooltips(...) or with arguments, use a .desc key (AEDualParts *.desc, steam_parallel_hatch.desc). Never pass the .tooltip key to .tooltips(), or it shows twice.
