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

## Pyromaniac passive

- Pyromaniac retains its ID and learned rank but now occupies a passive slot on Emberfang daggers. Successful player melee damage to a mob while burning extinguishes the wielder and heals exactly 2 HP at all ranks, capped at maximum health. It has no activation or cooldown and no longer transfers fire.
- A matching dual-wield pair shares Pyromaniac from either hand without stacking. Rejected hits and hits while not burning do nothing; switching to another weapon removes the passive benefit. Extinguishing also clears infinite burning through the existing fire hook.
- Removed the obsolete armed-state class/test, updated tooltip/card text and markdown/abilityDescriptions.md, and adjusted old active-source/tooltip tests to use current ability categories.
- Validation: build and all 271 required GameTests passed in build/pyromaniac-passive-corrected.log; review run is recorded in build/pyromaniac-passive-reviewed.log. Coverage includes actual attacks, all five gemstone variants/ranks, repeat ignition without cooldown, healing cap, invalid hits, old saved assignments and shared offhand passives.

## Heating Up replacement

- Heating Up remains a learned Infernal Claymore passive with the same ID/rank, but now cancels non-lava fire damage and heals 1 HP every 40/35/30/25 burning ticks at ranks I-IV (existing super-template rank V: 20 ticks). Chosen conservative healing values are documented in the in-game description and markdown/abilityDescriptions.md.
- Neither protection nor healing applies while in lava; lava damage is always excluded from protection. Healing requires the equipped main-hand passive and positive remaining fire ticks, does not extinguish fire, and stops/reset progress on unequipping, extinguishing, full health or entering lava.
- Removed the old heat accumulation/spending, automatic extinguishing, attack ignition, Fire Aspect infinite-burn conversion and heat tooltip. Existing heat custom data is ignored; the general InfiniteBurnEvents helper remains for other effects.
- Validation: build and all 271 required GameTests passed in build/heating-up-fire-healing.log; review run is recorded in build/heating-up-fire-healing-reviewed.log. Tests cover all variants/ranks, actual fire/lava damage, lava immersion, recovery timing and cap, equipment changes, old saved stacks and ordinary Fire Aspect behavior.

## Stepping Through the Void collision fix

- The old point sampler rejected downward aim into the ground and required at least half a block of clearance. The new path checks swept player boxes in quarter-block segments, refines the first obstructed segment to its farthest safe fraction, and permits any meaningful available movement.
- Passable plants have no collision and do not obstruct the step. When downward aim reaches a floor, movement continues forward along that floor; vertical aim remains intact in open air. Walls/ceilings stop the path without sliding sideways or passing through them.
- Existing collision dimensions, loaded-chunk checks, world border/build-height bounds, relative camera rotation, 10/20-block ranges and cooldowns are preserved. Updated tooltip/card descriptions and markdown/abilityDescriptions.md.
- Validation: build and all 273 required GameTests passed in build/void-step-passable-plants.log; review run is recorded in build/void-step-passable-plants-reviewed.log. Added downward short/tall-grass tests for all Riftfang variants and a close-wall test; updated ceiling/world-height expectations for sub-half-block clearance. Existing airborne direction, paired-range and camera-packet tests pass.

## Immediate Ender Swing

- Ender Swing now strikes on ability-key activation with no other augment or followup attack required. Removed its armed state and ordinary axe attack hook; the existing 60-second cooldown starts immediately, even with no nearby target.
- Preserved damage, one-block radius, super-rank bonus, 20-block lift and clearance checks, ally filtering, visuals/sound and axe offhand restrictions. Removed Ender Swing from the reasons to send ordinary axe-combo packets.
- Ability cooldown duration is captured before activation so an instant attack breaking the equipped weapon cannot erase its cooldown. Updated the in-game tooltip/card and markdown/abilityDescriptions.md.
- Validation: build and all 273 required GameTests passed in build/ender-swing-immediate.log. The reviewed build and all 274 required GameTests passed in build/ender-swing-immediate-reviewed.log, including a new tertiary-slot last-durability cooldown regression. Existing tests now check immediate damage/lift and independent activation/cooldowns across all five Nullified Axe variants.
