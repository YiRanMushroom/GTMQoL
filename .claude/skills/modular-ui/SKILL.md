---
name: modular-ui
description: "ModularUI windows and popups: looks, the user's preferences, popups, MUI traps; use when building or changing a UI"
---

<!-- agent-toolkit: generated from index entry skill.modular-ui: change it with index_update("skill.modular-ui"), not here -->

Use when building or changing a ModularUI window or popup.

## Looks
- Not "it works, so it's done": fitting and good-looking. Layout aligned and balanced, sizes that suit the content, icons that show what a button does (an existing icon of the thing itself beats a borrowed one), tooltips on icon-only buttons.
- Check how it looks, not only that it functions; when something looks off, fix it before calling the UI finished.
- Windows centered and roomy, not small side panels.

## The user's preferences and ModularUI traps
The user wants UIs designed properly, not just functional (said on the channel UI, 2026-10-29):
- Center content and rows of buttons; put related actions together in one row (e.g. New and Unbind under the channel list).
- No fixed limits on lists ("at most 64 rows"): a widget that draws its rows and grows with the synced list, inside a scrolling ListWidget (GhostIngredientListWidget, ChannelListWidget).
- List rows: hover highlight, the current/bound entry highlighted, details and what a click does in the tooltip, row actions (edit) in the row.
- Every field belongs where it is used: no settings in a form that don't apply there (a password field on "New" was wrong).
- A non-essential feature whose UI would be awkward is dropped instead (channel passwords were removed for that).
ModularUI traps (each broke a UI once):
- TextFieldWidget writes its value only on unfocus: a button that reads the field needs .autoUpdateOnChange(true), else it sends the old text.
- A form that asks the server closes only when the server reports success (a synced counter with a changeListener, as ChannelUI does); on failure it stays open and shows the error in the form.
- A row with a hidden (setEnabledIf) button needs .collapseDisabledChildren(), else the hidden one keeps its space and the rest is off center.
- Rectangle.cornerRadius on 1.21: MUI's GuiDraw.drawRoundedRect adds the bottom-left corner's vertices without the pose, so a line runs from the screen's top left to the shape. Use plain rectangles (1.20's MUI has the pose).
- Plain `new Widget<>()` instances count as equal and a parent refuses a child equal to one it has (`Failed to add child`): give spacers different names, as the energy cube does.
- tooltipDynamic builds once and only again when marked dirty: a tooltip showing live state needs .tooltipAutoUpdate(true), else it keeps its first text.
- Shift-click tries every other slot group, highest priority first, and storage outranks the player's inventory. So machine outputs go to the inputs when they are separate groups: put all the machine's slots in one registered group (.slotGroup(name), PatternCrafterUI).
- GTGuiScreen(panel) uses the gtceu:standard theme, which the theme reload leaves without the backgrounds of slots and off buttons (context.md): pass ThemeAPI.DEFAULT_ID, as machine UIs do.
- A GenericListSyncHandler compares entries with equals and resends the whole list when one differs; MUI writes the packet later on the network thread from a cache the server thread clears and refills, so a resend racing that write crashed the client (NPE in StreamCodec composite, 2026-11-07). Entries holding an ItemStack (no equals of its own) need an equals by content (PatternEntry, Stored), else they resend every tick.

## Popups
Every popup of ours goes through PopupWidgets.popup instead of PopupPanel.createPopupPanel, so they all behave the same (void miner, fishing pond, cyclic stored list, ghost marks, pattern batching). Its button calls MUI's IPanelHandler.togglePanel() (second click closes), not openPanel(). Number settings in a popup: counterRow (click ±1, Shift ±shiftStep, Ctrl ×2/÷2; Mth.clamp on 1.20.1, Math.clamp on 1.21).

- popups: PopupWidgets.popup: every popup of ours

Code:
- src/main/java/com/yiran/minecraft/gtmqol/common/ui/PopupWidgets.java
