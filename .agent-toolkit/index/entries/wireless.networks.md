+++
title = "Wireless networks: NetworkId (team vs private pool), binding trait, placement binding, FTB Teams, data stick"
keys = [ "ftbteamscompat", "gtmqolwirelessbinding", "to team" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/NetworkId.java"
snippet = "public record NetworkId(boolean privateNetwork, UUID id) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessBindingTrait.java"
snippet = "public class WirelessBindingTrait extends MachineTrait {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessNetworks.java"
snippet = "    public static @Nullable NetworkId networkId(@Nullable UUID player, boolean privateNetwork) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/WirelessNetworks.java"
snippet = "    private static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {"
+++
Inspired by GTMThings' wireless energy; 1.21 has the same. Machines store only player + private flag; the team is looked up on every access. Placement listener is LOWEST priority, skips FakePlayer. Unbind / private toggle / "To team" (moves steam and EU private pools): bound player or ops (level 2). FTB party join/leave moves team pools (FTBTeamsCompat); private pools never move. Data stick copy uses the stack NBT tag and hover name.