+++
title = "Wireless channels (Flux Networks style): computation and optical data hatches, router, channel list UI"
kind = "design"
keys = [ "channel", "computation", "cwu", "flux networks", "optical data", "password", "router" ]
updated = "2026-10-29 07:33:45"
checked = "2026-10-29"
+++
Code: common/wireless/channel/ (ChannelSavedData, ChannelRegistry, ChannelBindingTrait, ChannelUI with ChannelListWidget, ChannelMachines), common/wireless/computation/, common/wireless/optical/. Ported from 1.21 (2026-10-29); the same code apart from the 1.20.1 API differences (build.branch-differences).

User decisions (2026-10-29):
- Channels are live links, not pools, so they don't use WirelessPool/NetworkId. One ChannelKind per kind (computation, optical); energy and steam may move to channels later, which is why the channel layer is kind-agnostic.
- Access: private, team (FTB, same lookup as WirelessNetworks), public. No passwords (tried, dropped by the user as not worth the UI). Only the owner or ops edit/delete. Binding only from the machine's UI.
- UI: the list draws its own rows and grows with the channel count inside a scrolling ListWidget (no row limit); New and Unbind under it; New/edit open a form (name, preset colors, access); delete is shift-click.
- Bindings are saved per GlobalPos in ChannelSavedData, so unloaded ends count for the one-to-one rule; ChannelRegistry holds the loaded ends that requests ask.
- Computation: without a router at most one transmitter and one receiver (a second is refused; if a router is removed with more bound, nothing flows). A bound router (it needn't be loaded) makes it many to many. No bridge components, no energy. ChannelComputation works like gtceu's network switch (per-tick saturation, the channel itself in seen).
- Optical data: always many to many.
- Cross-dimension; both ends must be loaded.
- Recipes: gtceu's hatch or network switch + circuit 5 in the magical assembler (recipes.wireless). The router's front is the network switch's screen.