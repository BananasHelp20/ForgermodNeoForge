# Current project context

## Ordinary tool material scope correction

The user clarified that ordinary tool sets are only for tungsten, titanium, damask, carbon steel, steel, reinforced iron, meteoric iron and scrap. Supreme materials are excluded from this ordinary tool expansion.

- Removed the nine extra ordinary sets for Inanisium, Ignisium, Morsium, Somnium, Vulnusium, Lush, Pulsite, Electrium and Taifunite (36 registered pickaxes/axes/hoes/swords).
- Existing special daggers, claymores and ability axes are preserved, as are material tiers, ingots and processing progression.
- Removed matching ordinary-tool textures and translations, regenerated recipes/models/advancements/item tags, and restricted both authoring scripts to the eight approved metals.
- MaterialProgressionGameTests asserts the exact approved set and absence of all 36 retired registry IDs; its stat progression stops at Carbon Steel rather than the removed ordinary Inanisium set.
- Validation: runData succeeded; build plus all 269 required GameTests passed (build/approved-metal-tools-only.log). Static resource/script checks passed. The built forgermod-0.2.0.jar contains no retired assets and includes all 32 approved models and recipes.
- This checkout initially had no sessionContext.md; this file records the current change without recreating older session history. Prior work is recorded in Git and src/changes.txt.

## Three-slot ability rotation

- The existing Shift+Alt+S shortcut now rotates displayed active assignments left: A/B/C -> B/C/A -> C/A/B -> A/B/C. Two abilities still swap; fewer than two do nothing. The shortcut itself is unchanged.
- Explicit active order is saved separately from learning order. Legacy swapped stacks remain readable; upgrades, gemstone infusion and save/load preserve assignments. Extraction still takes the first learned augment; relearning appends it after surviving assignments.
- Client, server and expanded tooltip support three-ability cycling. Configured ability keys remain displayed, and cancelling armed sessions before rotation preserves cooldowns by ability identity.
- Validation: build and all 271 required GameTests passed in build/three-ability-rotation-resumed.log. Added packet-level three-cycle persistence and cooldown tests; final review run is recorded in build/three-ability-rotation-reviewed.log.

## Cinder Decoy flame detonation

- Every decoy disappearance replaces the vanilla explosion puff with a custom flame emitter, carried by the existing explosion packet with decoy yaw and rank-scaled radius. Damage, ignition, sound and cleanup use the existing explosion path.
- The client creates a bounded surface silhouette of the head, torso, arms and legs, holds it for three ticks, then bursts outward over twelve ticks while fading. No extra ticking entities or per-flame network packets are created.
- Particle registry/options and geometry are common-side; emitter and flame rendering are client-only. The sprite uses the vanilla flame texture.
- Validation: build and all 271 required GameTests passed in build/cinder-decoy-flame-burst.log; the reviewed build is recorded in build/cinder-decoy-flame-burst-reviewed.log. Visual timing and appearance still need an in-game look.
