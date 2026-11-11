---
name: naming-and-text
description: "Naming and player text: class, ID, lang, config and item names, tooltips, UI labels; use when naming or writing them"
---

<!-- agent-toolkit: generated from index entry skill.naming-and-text: change it with index_update("skill.naming-and-text"), not here -->

Use whenever you name something (class, field, method, block or item ID, lang key, config key, index entry) or write text players see (block and item names, tooltips, UI labels, chat messages, config comments, README).

## Names
- The user's words for a feature are working names and often loose ("pattern matrix"). Before one goes into code, IDs or text, think about what the thing is and does, and pick a name that is easy to understand and easy to search for, in the style of GTCEu and AE2. The Pattern Matrix became the Assembling Matrix: it assembles crafting recipes in bulk, as AE2's Molecular Assembler does one at a time. When the better name differs much from the user's words, say so.
- One concept, one name in every layer: class (AssemblingMatrixMachine), ID (assembling_matrix), lang keys (gtmqol.assembling_matrix.), config key (ae2.assemblingMatrix), index entry and player text use the same words. Parts carry the whole's name (Assembling Matrix Pattern Core), not a shortened one.
- A name says what the thing is or does, not how it was built, what it replaced or what it was called in a conversation.
- Renaming a block or item ID that worlds may already hold: add old -> new to RenamedIds (registry aliases on 1.21, MissingMappingsEvent on 1.20), so saved blocks, machine contents and item stacks load under the new ID. Old IDs stay there for good.

## Player text
- Context-free: written for someone who has never seen the code, the index, the handoff notes or the conversation. No internal names, nicknames or words that only mean something to the developers ("our patterns", "the buffer", "batches", "the work").
- Name things fully and concretely, by the names the player sees in the game (an item's in-game name, a block's name, a button's label), above all the first time: "the Crafting Patterns made for the Pattern Crafter", not "our patterns".
- Few pronouns (it, they, this, its), and never as the first mention of something; repeat the name when "it" could mean two things.
- Not verbose, but detailed where it helps: every sentence says something the player needs to decide or act; no filler, no implementation details, no history.
- Names say what the thing is or does, in the game's and the parent mod's naming style.
- One word per thing, in every tooltip, message and the README: "ghost ingredient" (to mark is the verb; not ghost mark, marked ingredient); "wireless network", a player's "private network" and "team network" (not pool, team mode); "window" (not UI, GUI); mouse actions as "Click", "Right-click", "Shift-click", "Shift-right-click".
- A toggle's tooltip: "<Name>: on. <what it does>" and "<Name>: off", no colour codes, no separate info line.
- Tooltips GT-style: short and plain, only what affects the player's decision (no tick counts, batch sizes, nothing the structure preview already shows).
- The user (2026-11-11): cut hard. Parentheses are almost always filler (keep only key hints like "Shift: ±8" or "0 = none"); don't describe what the player sees happen anyway ("encoded patterns stay in the terminal", "the grid empties"). A result message is the outcome plus, on failure, the reason: "Pattern uploaded" / "Cannot upload" + "Already in an Assembling Matrix".

## Machine tooltip lang keys
gtceu's MetaMachineBlock.appendHoverText shows <ns>.machine.<id>.tooltip by itself. Either just register lang under that key (gtceu shows it; ProbableDevices, Create sequenced assembly) or, for tooltips passed via .tooltips(...) or with arguments, use a .desc key (AEDualParts *.desc, steam_parallel_hatch.desc). Never pass the .tooltip key to .tooltips(), or it shows twice.

- renamed-ids: Old block/item IDs mapped to new ones
- tooltip-desc-keys: Example of .desc keys for tooltips passed to .tooltips()

Code:
- src/main/java/com/yiran/minecraft/gtmqol/RenamedIds.java
- src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/parts/AEDualParts.java
