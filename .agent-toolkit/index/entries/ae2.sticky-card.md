+++
title = "Sticky card for storage buses: partitioned keys stop at a sticky bus instead of falling through (v7 port)"
keys = [ "ae2.stickycard", "expatternprovider", "extendedae", "isticky", "modaccessor", "storagebusinventory" ]
updated = "2026-10-19 20:12:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/NetworkStorageMixin.java"
snippet = "public class NetworkStorageMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/StorageBusPartMixin.java"
snippet = "    private StorageBusPart.StorageBusInventory handler;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/StickyCardItem.java"
snippet = "    public static void registerUpgrades() {"

[[links]]
path = "src/main/resources/META-INF/accesstransformer.cfg"
snippet = "public appeng.parts.storagebus.StorageBusPart$StorageBusInventory"

[[items]]
name = "why-shadow-handler"
value = "StorageBusPartMixin @Shadows the private handler field (needs the AT) because a @WrapOperation with the supertype receiver did not match."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/StorageBusPartMixin.java"
snippet = "private StorageBusPart.StorageBusInventory handler;"

[[items]]
name = "no-upgrade-tooltip-group"
value = "The card registers without a tooltip group (v7 passed group.storage.name); AE2's own storage bus upgrades have none either."
+++
Also MEInventoryHandlerMixin (the flag + shouldStick) and PartSpecialStorageBusMixin (ExtendedAE mod/precise/tag buses, EPPItemAndBlock). Early switch ae2.stickyCard: when off STICKY_CARD stays null and the mixins check it. Upgrades registered in FMLCommonSetupEvent enqueueWork. ModDevGradle applies ATs only to Minecraft, so build.gradle's ModAccessor plugin patches the AE2 jar (accessModCompileOnly) for javac; Forge applies the AT at runtime.