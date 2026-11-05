# Implemented-feature review — 5 November 2026

## Scope

Reviewed the current local implementation across the major systems below, including the uncommitted metal-tool artwork and new relic displays. This is a source/resource review backed by the automated suite, not a claim that every ability combination, multiplayer race or generated world has been manually playtested.

Recommendations are **not automatic permission to change existing gameplay**. This task adds the requested dagger pedestal and axe stand; the other findings below have deliberately not been silently fixed.

## First things I would fix

### 1. Rusty Dagger is registered but has no item model — high priority

`ModItems` registers `rusty_dagger`, but neither resource root contains `models/item/rusty_dagger.json`. Its model-generation call is commented out. It can therefore appear as a missing model when held or in inventory, even though the new pedestal has its own baked weapon geometry.

The current texture audit only checks texture references in models that already exist. It cannot detect a registered item whose model is completely absent.

**Recommendation:** finish the rusty dagger's item artwork/model, enable its model-generation path, and add a packaged registered-item-to-model coverage check. Give Rusty Axe a finished dedicated model too; its existing model uses the explicitly named `axe_fallback` OBJ.

Sources: [item registration](../src/main/java/net/bananashelp20/forgermod/item/ModItems.java), [model provider](../src/main/java/net/bananashelp20/forgermod/datagen/ModItemModelProvider.java), [texture audit](../scripts/check_item_texture_references.py).

### 2. The rusty weapons promise restoration, but only the claymore has a recipe — high priority

Both rusty dagger and rusty axe tooltips say “Can be restored.” The recipe provider contains a restoration recipe consuming `RUSTY_CLAYMORE`, but no corresponding recipe consuming `RUSTY_DAGGER` or `RUSTY_AXE`; neither is referenced by the current recipe JSON files.

**Recommendation:** add explicit restoration routes for dagger and axe, with sensible component costs and advancement unlocks. Until then, either make the unfinished progression clear or avoid promising it in those two tooltips. Also decide where the new relic displays should spawn naturally; adding creative blocks alone does not create a survival progression route.

Sources: [recipes](../src/main/java/net/bananashelp20/forgermod/datagen/ModRecipeProvider.java), [tooltips](../src/main/resources/assets/forgermod/lang/en_us.json).

### 3. Forge recipes have two competing sources of truth — high priority for maintenance/datapacks

**Resolved in the subsequent implementation:** Forge and Infusion now use recipe-manager recipes, with all 147 built-ins migrated to JSON, recipe-driven slots/hints, explicit costs, reload-safe progress and optional JEI categories/transfer support. See [machine-recipes.md](machine-recipes.md). The description below records the original finding.

`ForgeRecipe` is registered as a recipe type, but `ForgeBlockEntity.hasRecipe()` uses parallel `FORGE_RECIPE_INPUTS` / `FORGE_RECIPE_OUTPUTS` arrays. The recipe-manager path is commented out. Infusion Table matching likewise uses parallel hard-coded arrays.

**Consequence:** editing or adding recipe JSON does not necessarily change what the machine accepts. Ordinary datapack expectations and recipe-viewer integration can disagree with gameplay. Array order and equal lengths become hidden correctness requirements.

**Recommendation:** make machine recipes consistently recipe-manager driven, with explicit input counts/template rules. Add tests proving a data-defined recipe is actually craftable and a replaced recipe is no longer accepted. Remove obsolete debug `System.out.println` calls in `ForgeRecipe.matches()` when cleaning up that path; the current machine path bypasses that method, so this is not a claim of current tick-by-tick log spam.

Sources: [Forge block entity](../src/main/java/net/bananashelp20/forgermod/block/entity/custom/ForgeBlockEntity.java), [replacement machine recipe](../src/main/java/net/bananashelp20/forgermod/recipe/MachineRecipe.java), [Infusion block entity](../src/main/java/net/bananashelp20/forgermod/block/entity/custom/InfusionTableBlockEntity.java).

## Other confirmed inconsistencies

### 4. Original Sword Stand facing is not explicitly transformed for rotated structures

**Resolved in the subsequent implementation:** the original stand now implements rotation and mirroring, with regression coverage for every horizontal facing, mirror, rotation and full/opening state combination. Its animation and one-time reward are preserved. The text below records the original finding.

`AncientSwordStandBlock` rotates its collision geometry for its facing, but does not override block-state `rotate` or `mirror`. Structure placement and structure tools transform block states through these methods; merely having a `FACING` property does not implement those transformations.

**Recommendation:** implement rotation/mirroring for the original stand and test it inside all four grave rotations. The two new displays implement and test those transformations already. This is separate from the existing cached-shape tests, which directly assign facing values.

Source: [original stand](../src/main/java/net/bananashelp20/forgermod/block/custom/AncientSwordStandBlock.java).

### 5. Original Sword Stand mining settings disagree with its tags

**Resolved in the subsequent implementation:** the original stand is now pickaxe-mineable and requires an iron-or-better pickaxe for its block drops. Tests cover empty/full stands, bare hands, wood/stone pickaxes, an iron axe and iron/diamond/netherite pickaxes. Claiming the claymore remains an ordinary right-click. The text below records the original finding.

The original stand is tagged as needing an iron tool, but is missing from the pickaxe-mineable list and its block properties do not require the correct tool for drops. That makes the tier tag insufficient to express the apparent mining intention, and pickaxes do not get the intended effective-tool treatment from that list.

**Recommendation:** decide whether it should be freely harvestable or require an iron-or-better pickaxe, then align properties and both tags. The new displays use consistent iron-tool/pickaxe tags and require a correct tool for their block drops; claiming the weapon still just uses right-click.

Sources: [block registration](../src/main/java/net/bananashelp20/forgermod/block/ModBlocks.java), [mining tags](../src/main/java/net/bananashelp20/forgermod/datagen/ModBlockTagProvider.java).

### 6. A few descriptions hide important combat exceptions

- **Live Wire:** the description says enemies that hit you take damage, but the implementation reflects only direct mob/player melee attacks from enemies. Projectiles and environmental damage do not trigger it. Either say “melee hits” or deliberately extend its damage-source policy.
- **Lullaby:** a nonplayer target receives the actual control lock, but an aimed player receives Slowness II instead. Its PvP exception should be stated, or player targeting should be explicitly rejected if the ability is meant only for mobs.
- Damage displayed from weapon attributes is a base weapon value, not a guarantee of final damage after enchantments, temporary buffs, target armor and resistances. Use “base weapon damage” consistently where that distinction matters.

These are scope/clarity recommendations, not a proposal to change all three mechanics without approval.

Sources: [Live Wire](../src/main/java/net/bananashelp20/forgermod/item/custom/abilities/LiveWireEvents.java), [Lullaby](../src/main/java/net/bananashelp20/forgermod/item/custom/abilities/LullabyEvents.java), [shared descriptions](../src/main/java/net/bananashelp20/forgermod/augmentation/AbilityDescriptions.java).

## Recommendations across the other systems

| System | Current implementation / useful safeguard | Recommended next check or improvement |
| --- | --- | --- |
| Ordinary metal tools and tiers | Eight ordinary tool families; new material sprites keep iron silhouettes and Advanced Handle colors. | Compare mining speed, durability and damage against material acquisition cost in survival. Do not change numeric balance from appearance alone. |
| Special weapons and attacks | Dedicated weapon classes, dagger pairing rules, axe combos and attack-speed tests exist. | Add a small displayed-controls guide covering normal attacks, heavy/area attacks, dual wield and active-slot rotation. Test rebinding and offhand conflicts together. |
| Augmentation and ability ranks | Shared rank caps/cooldowns and server-side activation validation reduce client spoofing and rank drift. | Move remaining duplicated runtime/description/preview formulas into shared per-ability stats. Compare every rank's displayed values with runtime in table-driven tests. |
| Ability targeting and control | Abilities intentionally differ in enemy/allied/neutral/player policies. | Publish a targeting policy for taunts, swaps, pulls, crowd control and friendly fire; test aggroed neutrals, allied players, pets, bosses and immune entities explicitly. |
| Necromancer | Equipment uses an authoritative SavedData bank rather than equipment copies in weapon NBT; six-slot layout loading includes migration from older five-slot data. | Keep old-world migration fixtures, and test server restart, transfer to another player, dimension travel, gear breaking and a copied controlling weapon together. No new duplication issue was established in this review. |
| Grave Vault and custom menus | Inventory persistence and click/quick-move tests already exist. | Expand multiplayer tests for disconnects with carried cursor items, full inventory, death and weapon changes while a menu is open. Do not assume single-player menu tests cover every network sequence. |
| Forge and Infusion Table | Per-block inventory/progress exists; Infusion's selected recipe is instance state rather than a shared static selection. | After unifying recipes, test automation on all sides and reload recipes while both machines are processing. |
| Shardium loot and resource generation | Loot is data-generated and the project has dedicated loot tests. | Retain an exact nine-entry/equal-weight check; randomness should be tested statistically only as a separate smoke test, not a flaky mandatory assertion. |
| Ancient City/grave generation | Custom city assembly appends a grave and searches for a gap, with an outside-edge fallback. | Test several real seeds/rotations for terrain clearance and accessibility. Decide explicitly whether both new relics belong in every grave, separate structures, or randomized rewards. No worldgen injection was made here. |
| Projectile/chunk lifecycle | Skull Throw has bounded tracked flight tickets and cleanup tests. | Add a deliberately slow-chunk-loading runtime test without the GameTest ticket-refresh helper. The existing helper is test infrastructure, not proof the real runtime survives every slow-loading case. |
| Optional Create integration | Compatibility classes/mixins and compatibility tests are separate from the base feature code. | Maintain a release smoke-test matrix with the optional mods absent and with supported Create/Enchantment Industry versions installed; a base-mod test run alone is not that matrix. |
| Localization and documentation | Current-rank values are white and prose gray; some reviewed descriptions use English fallback strings directly in Java. | Move translatable prose into language entries when localizing. Distinguish historical summaries from current mechanics and generate rank tables from shared data where possible. |

## Requested additions implemented in this task

### Ancient Dagger Pedestal

- ID: `forgermod:ancient_dagger_pedestal`.
- A low stepped dark-metal pedestal with a purple cushion; the rusty dagger lies flat on top.
- Right-click once to receive one Rusty Dagger, leaving the pedestal empty.

### Ancient Axe Stand

- ID: `forgermod:ancient_axe_stand`.
- An upright braced dark-metal rack with cloth pads, holding a rusty axe.
- Right-click once to receive one Rusty Axe, leaving the stand empty.

Both reuse the Ancient Sword Stand's existing texture materials and one-time reward concept. They have full/empty models, four facings, rotation/mirroring, creative-tab entries, item models, state-preserving loot, and mining tags. A full inventory drops the reward instead of losing it. Replacing a claimed display does not refill it.

They are static baked displays, not copies of the sword stand's animated opening mechanism. No new crafting recipes, survival worldgen placements or restoration recipes were invented. They can currently be obtained through the Miscellaneous creative tab or `/give`.

Geometry preview: `build/relic-displays-preview.png`. This is a software-rendered geometry/material preview, not an in-game screenshot.

## Verification

- Source-model audit: all 16 facing/full variants, four model geometries and texture references, both item models, and both state-preserving loot tables.
- Four new GameTests cover claim/duplicate prevention, inventory overflow, full/empty drop placement, and facing/rotation/mirroring/cached shapes/mining tags.
- The first suite run exposed a test-helper misuse (asking a helper that requires a block entity to verify that none exists). The assertion was corrected to query the level directly; this was a fixture correction, not a hidden runtime fix.
- Final build and **all 481 required GameTests pass**, recorded in `build/relic-stands-final-tests.log`. The final asset build is in `build/relic-stands-final-package.log`; the packaged texture-reference audit reports no missing local textures, and all ten new display model/blockstate/item/loot files match their source hashes. An in-game visual inspection and real-seed worldgen review remain manual follow-ups.

## Suggested order

1. Finish rusty dagger/axe item presentation and their restoration routes.
2. Pick the survival acquisition rules for the two new displays.
3. Unify Forge/Infusion recipes around data-driven recipes.
4. Original Sword Stand transform/mining alignment — now implemented.
5. Add cross-system multiplayer, migration and slow-chunk tests, then tune balance with survival playtesting.
