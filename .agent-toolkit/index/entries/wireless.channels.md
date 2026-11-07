+++
title = "Wireless channels (Flux Networks style): computation and optical data hatches, router, channel list UI"
kind = "design"
keys = [ "channel", "computation", "cwu", "flux networks", "optical data", "password", "router" ]
updated = "2026-11-07 09:54:54"
checked = "2026-11-07"

[[items]]
name = "kinds"
value = "one ChannelKind per kind; routerForMany = one-to-one without a router"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/channel/ChannelKind.java"
snippet = "public enum ChannelKind {"

[[items]]
name = "bind-rule"
value = "bind: refuses a second transmitter/receiver on a routerForMany channel without a router; bindings by GlobalPos"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/channel/ChannelSavedData.java"
snippet = "public @Nullable String bind(Player player, int id, ChannelKind kind, GlobalPos pos, ChannelRole role) {"

[[items]]
name = "edit-rights"
value = "who may edit/delete: owner or op"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/channel/ChannelSavedData.java"
snippet = "public static boolean canEdit(Player player, Channel channel) {"

[[items]]
name = "registry"
value = "loaded endpoints that requests ask"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/channel/ChannelRegistry.java"
snippet = "public final class ChannelRegistry {"

[[items]]
name = "computation"
value = "computation flow like gtceu's network switch (seen, saturation)"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/computation/ChannelComputation.java"
snippet = "public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {"

[[items]]
name = "ui"
value = "channel list UI, form, New/Unbind"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/channel/ChannelUI.java"
snippet = "public static final String DELETE_TOOLTIP_KEY = PREFIX + \"form.delete.tooltip\";"
+++
Code: common/wireless/channel/ (ChannelSavedData, ChannelRegistry, ChannelBindingTrait, ChannelUI with ChannelListWidget, ChannelMachines), common/wireless/computation/, common/wireless/optical/.

User decisions (2026-10-29):
- Channels are live links, not pools, so they don't use WirelessPool/NetworkId. One ChannelKind per kind (computation, optical); energy and steam may move to channels later, which is why the channel layer is kind-agnostic.
- Access: private, team (FTB, same lookup as WirelessNetworks), public. No passwords (tried, dropped by the user as not worth the UI). Only the owner or ops edit/delete. Binding only from the machine's UI.
- UI: the list draws its own rows and grows with the channel count inside a scrolling ListWidget (no row limit); New and Unbind under it; New/edit open a form (name, preset colors, access); delete is a shift-click on the form's Delete button (unbinds everything bound).
- Bindings are saved per GlobalPos in ChannelSavedData, so unloaded ends count for the one-to-one rule; ChannelRegistry holds the loaded ends that requests ask.
- Computation: without a router at most one transmitter and one receiver (a second is refused; if a router is removed with more bound, nothing flows). A bound router (it needn't be loaded) makes it many to many. No bridge components, no energy. ChannelComputation works like gtceu's network switch (per-tick saturation, the channel itself in seen).
- Optical data: always many to many.
- Cross-dimension; both ends must be loaded.
- Recipes: gtceu's hatch or network switch + circuit 5 in the magical assembler (recipes.wireless). The router's front is the network switch's screen.