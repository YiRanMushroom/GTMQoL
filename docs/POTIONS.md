# Potions: brewery, canner, potion fluids (`common/potion/`, potion mixins)

Split out of `context.md`. On 1.21 none of gtceu's custom recipe logic ran at first (brewery, canner, forming press
naming, ...); `gtceufix/GTRecipeTypeMixin` fixes that (`PORT_1_21.md`). Forming press naming is verified in game.
Everything here builds on it.

## Brewery outputs keep their potion

- `gtceufix/GTRecipeBuilderMixin` (1.21 only): fluid outputs keep their data components, so the brewery's output
  is the right potion. Built.
- `gtceufix/GTEmiRecipeMixin` (both branches): EMI looks recipes up from a specific potion (R). Verified in game.
- Details of both are in `PORT_1_21.md`.

## Canner (`CannerLogicMixin`)

A feature mixin, not in `gtceufix/`, on both branches. Representative recipes for EMI: bottling and draining
potions, for each container (bottle, splash, lingering). Bottling/draining verified in game on 1.21; the 1.20.1 port
is not built.

## Potion fluid inputs in EMI (`gtceufix/FluidRecipeCapabilityMixin`)

Both branches, not built.

- Potion fluid inputs showed Uncraftable Potion (brewery, canner filling). Gtceu maps a component fluid ingredient
  over the fluid's tag to a tag list, and EMI's `EmiTags.getIngredient` merges potion + flowing potion back into
  `c:potion` by fluid alone. The mixin lists only source fluids instead.
- In ATM10 it was still wrong: other mods' potion fluids (Immersive Engineering first, Create) are in `c:potion`
  too, so several sources were left and EMI merged them again; pattern transfer took IE's. The mixin now lists a
  single fluid, gtceu's if present (the user's choice). Display only: gtceu's brewery still takes other mods'
  potions through the tag.
- `GTRecipeViewerWidgetPotionMixin` adds "Any mod's version works" (flashing green, via MUI's tooltip auto-update) to
  such an input slot's tooltip when the tag has other mods' fluids. Lang is in `PotionContainers` (needs `runData`).
  It is apart from `GTRecipeViewerWidgetMixin` (OC preview) so that `overclocking.overhaul=false` doesn't drop it.

## Potion fluid names

- `gtceufix/PotionFluidTypeMixin` (1.21 only) uses the potion item's keys; gtceu's own `fluid.gtceu.potion.effect.*`
  have no translations.
- `PotionFluidNameMixin` (both) appends the first effect's potency and duration, so normal / long / strong differ.
  Container-marked fluids (below) are named like the item.
- Not built.

## Splash and lingering potions (1.21 only, not built; 1.20.1 port after)

Made in the brewery (the user's choice; a canner item-to-item version was written and removed).

- `PotionContainers`: data component `gtmqol:potion_container` (the potion `Item`) on gtceu's potion fluid; absent
  means a plain potion.
- `BreweryLogicMixin`: when gtceu found nothing, applies vanilla's container mixes to potion fluids, 750 mB like
  gtceu. The mixes come from `PotionBrewingContainerMixesAccessor` (`PotionBrewing$Mix` opened by our AT). EMI gets a
  recipe per potion.
  - Dragon's breath's bottle is lost (the brewery has no item output).
  - Marked fluids aren't brewed further (gtceu compares all components).
- `BottleItemFluidHandlerMixin` fills into the marked item; `PotionItemFluidHandlerMixin` drains splash / lingering
  into the marked fluid. The canner shows fill / drain in EMI for each container.
