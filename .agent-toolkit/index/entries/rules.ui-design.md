+++
title = "UI design (user's preferences): polished not minimal, centered, no row caps, drop features whose UI is clumsy"
keys = [ "centered", "layout", "list", "scroll", "ui" ]
updated = "2026-10-29 07:14:23"
checked = "2026-10-29"
+++
The user wants UIs designed properly, not just functional (said on the channel UI, 2026-10-29):
- Center content and rows of buttons; put related actions together in one row (e.g. New and Unbind under the channel list).
- No fixed limits on lists ("at most 64 rows"): a widget that draws its rows and grows with the synced list, inside a scrolling ListWidget (GhostIngredientListWidget, ChannelListWidget).
- List rows: hover highlight, the current/bound entry highlighted, details and what a click does in the tooltip, row actions (edit) in the row.
- Every field belongs where it is used: no settings in a form that don't apply there (a password field on "New" was wrong).
- A non-essential feature whose UI would be awkward is dropped instead (channel passwords were removed for that).
Popups: rules.ui-popups.