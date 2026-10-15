# Current project context

## Ordinary tool material scope correction

The user clarified that ordinary tool sets are only for tungsten, titanium, damask, carbon steel, steel, reinforced iron, meteoric iron and scrap. Supreme materials are excluded from this ordinary tool expansion.

- Removed the nine extra ordinary sets for Inanisium, Ignisium, Morsium, Somnium, Vulnusium, Lush, Pulsite, Electrium and Taifunite (36 registered pickaxes/axes/hoes/swords).
- Existing special daggers, claymores and ability axes are preserved, as are material tiers, ingots and processing progression.
- Removed matching ordinary-tool textures and translations, regenerated recipes/models/advancements/item tags, and restricted both authoring scripts to the eight approved metals.
- MaterialProgressionGameTests asserts the exact approved set and absence of all 36 retired registry IDs; its stat progression stops at Carbon Steel rather than the removed ordinary Inanisium set.
- Validation: runData succeeded; build plus all 269 required GameTests passed (build/approved-metal-tools-only.log). Static resource/script checks passed. The built forgermod-0.2.0.jar contains no retired assets and includes all 32 approved models and recipes.
- This checkout initially had no sessionContext.md; this file records the current change without recreating older session history. Prior work is recorded in Git and src/changes.txt.
