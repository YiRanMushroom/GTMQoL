+++
title = "Sticky card for storage buses: partitioned keys stop at a sticky bus instead of falling through (v7 port)"
keys = [ "ae2.stickycard", "extendedae", "isticky", "modaccessor", "storagebusinventory" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/StickyCardItem.java"
snippet = "    public static void registerUpgrades() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/NetworkStorageMixin.java"
snippet = "public class NetworkStorageMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/StorageBusPartMixin.java"
snippet = "    private StorageBusPart.StorageBusInventory handler;"

[[links]]
path = "src/main/resources/META-INF/accesstransformer.cfg"
snippet = "public appeng.parts.storagebus.StorageBusPart$StorageBusInventory"
+++
Also MEInventoryHandlerMixin (the flag + shouldStick) and PartSpecialStorageBusMixin (ExtendedAE mod/precise/tag buses). Early switch ae2.stickyCard: when off STICKY_CARD stays null and the mixins check it. MDG applies ATs only to Minecraft, so build.gradle's ModAccessor plugin patches the AE2 jar (accessCompileOnly) for javac; NeoForge applies the AT at runtime. A @WrapOperation with the supertype receiver did not match, hence the @Shadow of the private type.