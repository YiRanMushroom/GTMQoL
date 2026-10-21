+++
title = "AbstractWidgetMenuMixin: close a disposed ContextMenuButton's menu (crash in MenuPanel.closeAllMenus)"
keys = [ "abstractmenubutton", "addunformedwarning", "auto-build", "modularui bug", "not in a valid state", "structure error list" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/AbstractWidgetMenuMixin.java"
snippet = "public class AbstractWidgetMenuMixin {"
+++
Both branches (identical code; 1.21.1 has it too). GTCEu side: GTMultiblockTextUtil.addUnformedWarning (DynamicWidget with one hover-opened button per missing part ability). Listed under "mixins" (common), not "client", in gtmqol.mixins.json.