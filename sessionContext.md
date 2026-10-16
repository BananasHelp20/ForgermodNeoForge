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

## Deep Wound blood burst

- A successful armed Deep Wound critical now emits 48 redstone-block fragments and 32 blood-red dust particles from the victim's body center. It also displays on killing blows; Slowness is still only applied to surviving targets.
- This is particle-only: no explosion, area damage, area status effects or terrain changes. Existing trigger, damage multiplier, Jade behavior and cooldown are unchanged. Updated in-game descriptions and markdown/abilityDescriptions.md.
- Validation: build and all 274 required GameTests passed in build/deep-wound-blood-burst.log; review run is recorded in build/deep-wound-blood-burst-reviewed.log. The existing real-attack test checks an adjacent bystander stays unharmed and unslowed for all five Assassin Dagger variants. Appearance still needs an in-game look.

## Grave Vault finite flower consumption

- Vanilla BlockItem consumption skips decrementing in Creative mode. Both Grave Vault placement paths now normalize the stored count to exactly one less after a confirmed successful flower placement, preserving Survival's single consumption and failed/cancelled placement hooks.
- Manual and kill-triggered placement use the live menu's storage when available, persist the depleted inventory to the weapon, and broadcast inventory/menu changes. Emptying the vault removes saved flower storage; later menu writes/save-load cannot restore spent flowers.
- Validation: build and all 275 required GameTests passed in build/grave-vault-flower-consumption.log; review run is recorded in build/grave-vault-flower-consumption-reviewed.log. New regression coverage repeatedly depletes stock and attempts an extra placement for manual/kill paths in Survival/Creative with open/closed menus, then checks persisted counts. Existing invalid-soil, adventure and all-variant tests remain passing.

## Skybreaker all-mob targeting

- Both Skybreaker's falling-hit bonus and landing smash now target all living mobs, including passive animals, neutral golems and tamed/allied mobs. The wielder stays excluded; player/PvP targeting retains its existing rules.
- Radius, sight/wall checks, damage scaling, knockback and fall protection are unchanged. Updated Windcharge/Skybreaker descriptions.
- Validation: build and all 275 required GameTests passed in build/skybreaker-all-mobs.log; review run is recorded in build/skybreaker-all-mobs-reviewed.log. The all-five-axe/rank landing test now checks damage to a cow, owned wolf and player-created iron golem alongside hostile mobs, radius and wall exclusions. The real super-IV falling-hit test now uses a passive cow.

## Bone Guard / Necromancer enemy targeting

- Hostile mobs now receive a normal nearest-guard target goal, with line-of-sight/range checks and bounded scan intervals. It selects tracked living reinforcements, avoids adding goals to the guards themselves, and does not alter friendly iron-golem targeting.
- Guard arrows retain their player owner for kill/loot/ability credit and now carry the actual shooter's UUID. Successful ranged or melee hits point the victim at that reinforcement. Because vanilla stamps revenge memory after LivingDamageEvent.Post, a server-only pre-AI correction records the actual guard before the next AI tick.
- Pending corrections are cleared on newer damage, entity removal and summon cancellation; existing summon lifetime, weapon-switch persistence, owner/ally protection, golem neutrality and loot restrictions remain intact.
- Validation: build and all 277 required GameTests passed in build/bone-guard-enemy-targeting-verified.log; final review run is recorded in build/bone-guard-enemy-targeting-reviewed.log. New tests exercise proactive zombie/skeleton/spider targeting and ranged/melee retaliation for all seven max-Necromancer reinforcements while retaining player-owned arrows. Existing all-variant friendly-golem, owner protection and cleanup tests pass.

## Necromancer IV crash investigation

- run/crash-reports/crash-2026-10-16_18.18.51-server.txt reports NoSuchMethodError in GuardStray.getArrow: a new inner class called the three-argument ownArrow helper while the still-running game had the older two-argument outer class loaded. The client had started before the targeting edits. Rebuilding while that development game was open produced mixed loaded class versions.
- Restored the original two-argument helper and all four call sites. It now captures the original guard owner of the freshly constructed arrow before transferring ownership to the player, preserving shooter identity, retaliation and kill credit without changing the helper signature.
- Added a real-ticking Necromancer IV group-versus-one-zombie test that observes all four skeleton/bogged/stray archers spawning arrows through their bow AI. Also fixed a cached vanilla AI selection occasionally reverting retaliation to the credited player; actual player hits can still replace that revenge memory.
- Validation: build and all 278 required GameTests passed in build/necromancer-iv-arrow-crash-fixed.log; final review run is recorded in build/necromancer-iv-arrow-crash-reviewed.log. Checked the built jar's helper and stray call-site descriptors match.
- Fully close and relaunch Minecraft before retesting. Reloading the world alone cannot replace Java classes already loaded in that game process.

## Bone Guard / Necromancer level lineups (supersedes earlier counts)

- Bone Guard I: one bow skeleton; II: two bow skeletons; III: one bogged and one skeleton, both with bows.
- Necromancer I: one stone-sword wither skeleton, one bogged and one stray with bows. II: two stone-sword wither skeletons plus bogged/stray. III: bogged, stray and skeleton with bows plus two stone-sword wither skeletons.
- Necromancer IV: six reinforcements: Power II bow bogged, Power II bow stray, Flame bow skeleton, diamond-sword wither skeleton, Flame bow wither skeleton, and iron-sword skeleton. Gear updates use vanilla AbstractSkeleton goal reassessment so both mixed roles work.
- One-minute lifetime, weapon-switch persistence, owner/ally and golem protection, retaliation, noncollectible arrows and no equipment loot are preserved. Arrow helper remains at its original two-argument signature.
- Updated in-game tooltip/card and the complete lineup table in markdown/abilityDescriptions.md. The all-variant/rank test verifies exact mob types and gear/enchantments; real group combat checks four bow users and both sword users, plus burning arrows from Flame bows.
- Validation: build and all 278 required GameTests passed in build/bone-guard-level-lineups.log; final review run is recorded in build/bone-guard-level-lineups-reviewed.log. Restart Minecraft to load the changed lineup implementation.

## Necromancer armor by combat role

- From Necromancer I (stored rank 4) onward, every bow reinforcement wears a full chainmail set and every melee reinforcement wears a full iron set. Assignment uses the equipped weapon, so Necromancer IV's bow wither skeleton gets chainmail and its iron-sword regular skeleton gets iron.
- Bone Guard I-III remain unarmored. Existing lineups, weapons/enchantments, one-minute lifetime, combat AI, retaliation and equipment no-loot rules are unchanged. Updated the game description and markdown/abilityDescriptions.md.
- Validation: build and all 278 required GameTests passed in build/necromancer-role-armor.log; final review run is recorded in build/necromancer-role-armor-reviewed.log. All-rank/all-variant tests check every armor slot, the unarmored lower ranks, and that killing armored reinforcements drops no farmable items. Existing full-group melee/bow/Flame-arrow combat checks pass.
