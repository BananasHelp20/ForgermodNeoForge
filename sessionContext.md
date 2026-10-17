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


## Homesick passive for Electrium

- Added Homesick to all 15 registered Electrium weapon pools (dagger/claymore/axe and all gemstone variants) as a learned single-rank passive. Fresh weapons do not receive it automatically; normal augmentation offers/templates and passive limits apply.
- Standing on a base sculk block refreshes Speed II and Strength I for 60 ticks. Strength uses the same three-second refresh duration as Speed because its duration was unspecified. Refreshes are throttled to avoid per-tick attribute/network churn. Leaving sculk or unequipping lets the short effects expire naturally without deleting unrelated buffs; stronger, longer and infinite external effects are preserved.
- Only grounded, living, non-spectator wielders qualify. Other sculk-family blocks (sensors/catalysts, etc.) are not base sculk. Matching dual-wield Electrium daggers share a learned copy from either hand without stacking. Jade retains its existing +1 effect level/+1 second bonus, including when the providing dagger is in the offhand.
- A bounded server-sent sculk-soul ring and sculk-charge particles surround the wielder only while the condition holds. No persistent entities, state maps or new rendering assets are used. Appearance still needs an in-game look.
- Updated English tooltip/card, markdown ability descriptions and all Electrium weapon rows, plus src/changes.txt.
- Validation: build and all 282 required GameTests passed in build/homesick.log. Final review run is recorded in build/homesick-reviewed.log. New coverage checks all 15 variants and pool exclusions, fresh/learned behavior, Jade, sculk/airborne/spectator/offhand conditions, real player-tick refresh/expiration, shared dagger passives and external effect preservation. Fully restart Minecraft to load the new subscriber.

## Independent material palette reference

- User explicitly prohibited looking at current metal textures for this design task. No existing texture was opened or sampled. Fresh palettes are in markdown/metalPalettes.md, reusable ramps in markdown/metalPalettes.json, and swatches in markdown/metalPalettes.svg.
- Directions: tungsten = dark blue graphite; titanium = muted violet silver; magnesium = pale silver with a faint mint tint; dolomite = warm chalk with rose-gray shadows and matte facets. Each has six dark-to-light colors and pixel-art usage notes. These are proposed references for later texture creation, not applied assets.
- Verified JSON/SVG parsing, matching hex values across all three artifacts, strictly increasing ramp luminance, and surface percentages totaling 100 per material.
- Preserve unrelated working-tree texture edits/deletions and the user-created textures/item/palletes directory. No commit/push was requested for this palette task.
- Follow-up: meteoric iron was added as a fifth independent palette, with a smoky gray body, charcoal shadows and warm nickel/champagne highlights. Six-color ramp: #29272B, #4C494A, #73716D, #9D9A8D, #CCC7AE, #F3E9CB. Use sparse angular highlight seams for its visual identity. Markdown, JSON and SVG now include it; existing textures remain uninspected.

## Three ingot recolors

- User clarified that the ingot set is meteoric iron, tungsten and titanium. The original request also asks for magnesium/dolomite as rocky raw pieces, not ingots (completed below). Old target textures must not be opened or sampled; they were written directly without reading them. Approved ingot references are carbon steel, steel and vanilla netherite/gold/iron/copper. Inspected carbon steel, steel, netherite and iron only.
- Final textures are direct palette swaps of src/main/examples/item/iron_ingot.png, preserving all 256 pixel positions and alpha values. Two neighboring dark shades and two neighboring middle shades merge to fit each saved six-color ramp. No silhouette or layout edits. Existing generated item models already reference these exact texture names.
- scripts/recolor_metal_ingots.ps1 reproduces the three outputs and markdown/metalIngotPreview.png using System.Drawing. It reads only the approved iron source and palette JSON, never existing destination textures. Palette notes/JSON status now document which ramps have been applied.
- Verified the new PNGs are 16x16, retain the exact reference alpha mask, use all six palette colors only, and match the generated model references. Inspected the enlarged output preview. Preserve unrelated user texture edits/deletions and staged state; this non-ability task does not request a commit/push.

## Magnesium and dolomite raw recolors

- Completed the raw-piece portion of the same request: magnesium is a direct palette swap of unrefined_steel.png; dolomite uses unrefined_carbon_steel.png. These two references were explicitly authorized and were opened; old magnesium/dolomite target textures were not read. No new ingots for these materials.
- scripts/recolor_raw_materials.ps1 reproduces the outputs and markdown/rawMaterialPreview.png without reading destination textures. Magnesium uses five palette shades (no near-white glint), dolomite all six, with adjacent source shades merged while preserving the source pixel layout and alpha mask.
- Verified both new PNGs are 16x16, every alpha matches the corresponding approved source, every opaque color belongs to its saved palette, and the generated model paths already resolve to these textures. Inspected the enlarged comparison preview. Updated design notes/JSON status and changelog. Gradle processResources passed for all five outputs; packaged PNG hashes match the new source textures. git diff --check passed.

## Steel-based ingot revision (supersedes vanilla iron reference above)

- User requested redoing the three ingots with steel_ingot.png as the reference. scripts/recolor_metal_ingots.ps1 now reads only that approved source and the saved palette JSON. It does not open destination textures before replacement.
- Steel's seven opaque colors map to six palette roles: #090B0D outline, #13171B deep shadow, #1C2023 and #20282C shadow, #252F33 base, #323B3E light, #3D4548 glint. All source pixel positions and alpha values are preserved exactly. The three new PNGs and markdown/metalIngotPreview.png are regenerated; source notes updated. Raw magnesium/dolomite are unaffected.
- Validation: all three new PNGs pass a full per-pixel comparison against steel's alpha and the specified palette mapping, and the enlarged preview was inspected. Gradle processResources passed.

## New titanium sponge design

- User explicitly prohibited opening the current titanium_sponge.png. It was not read before replacement. Researched real titanium sponge online via Toho Titanium's product page/photo and Osaka Titanium's sponge page; references and full prompt are in markdown/titaniumSpongeDesign.md.
- Built-in imagegen produced a new jagged, porous metal cluster with two lobes and dark cavities in the saved violet-silver titanium family. Original generated source is preserved in markdown/designSources/titaniumSpongeGenerated.png. scripts/export_titanium_sponge.ps1 reads only that source and the palette JSON, cropping to 14x14 within a 16x16 canvas, sampling and palette-snapping with hard alpha.
- Final texture replaces the runtime titanium_sponge.png and still uses its existing generated item model. Five palette shades, 106 opaque/150 transparent pixels, binary alpha and one-pixel margin verified. Inspected markdown/titaniumSpongePreview.png on both light and dark backgrounds. No gameplay/item registration changes; preserve unrelated user edits and staged deletions.
- Gradle processResources passed; verified the generated item model reference and identical source/packaged PNG hashes. git diff --check passed. No commit/push requested for this asset task.

## Tungsten powder recolor

- User prohibited looking at the previous tungsten_powder.png. It was not opened or sampled before replacement. Inspected only the authorized vanilla gunpowder, glowstone_dust and redstone PNGs under src/main/examples/item/. Chose gunpowder's granular pile for a direct recolor.
- scripts/recolor_tungsten_powder.ps1 reads only gunpowder and the saved palette, never existing destination textures. Source roles: #2D2D2D outline, #3E3E3E deep shadow, #494949 shadow, #505050 and #545454 base, #727272 light, #8A8A8A glint. The new runtime texture retains the exact 16x16 source layout and alpha mask with all six tungsten colors, 88 opaque/168 transparent pixels and binary alpha.
- Inspected markdown/tungstenPowderPreview.png on dark and light backgrounds. Verified every output pixel against the approved source and role mapping, all six colors, dimensions and existing generated item-model path. Gradle processResources passed. Updated palette notes/status and src/changes.txt. Preserve unrelated user modifications and staged deletions; no commit/push requested.

## Item texture folder migration

- User reorganized textures/item and requested reference updates. Migrated 146 models for 146 moved texture paths (including the authored void axe's texture/particle slots). Changed only texture slots, preserving all model parents and item ids; inherited gemstone OBJ models still point to the original base model. No PNG pixels were read or changed during this task and no user-staged moves/deletions were altered.
- ModItemModelProvider now overrides basic-item generation and resolves textures for basic/handheld/dagger/block-item helpers through the actual organized folders using ExistingFileHelper. The folder list includes gemstones, metals/{ingots,raw,shards,tools,weapons/{normal,special,rusty}}, other/weapon_parts, other and the item root. Missing and duplicate texture names fail explicitly. Flat-root textures still present are intentionally retained there.
- Updated scripts/recolor_metal_ingots.ps1 to read metals/ingots/steel_ingot.png; raw and powder scripts use metals/raw for moved references/destinations. Their three ingot outputs and titanium sponge remain at the item root because those files were not moved by the user. Did not rerun design scripts or overwrite artwork.
- Added scripts/check_item_texture_references.py for source or packaged-jar auditing and markdown/itemTextureMigration.md listing the 25 truly absent tool PNGs. These are 3 Damask, 4 Meteoric Iron, 4 Reinforced Iron, 3 Scrap, 3 Steel, 4 Titanium and 4 Tungsten. All were deleted without replacement paths; they remain missing and block full data generation until art is supplied. The audit reports these with exit code 1 instead of masking them. Earlier commentary estimated 24; verified count is 25.
- Validation: full Gradle build and the existing standalone checks passed. Refreshed only generated resources using cleanProcessResources and rebuilt jar. Verified all 146 new PNG paths exist in the jar with no stale flat copies, unchanged parents and valid animation sidecars. Source and jar audits both report zero unresolved relocations and exactly the 25 genuine missing textures; jar audit is build/item-texture-jar-audit.log. No commit/push requested for this non-ability task.

## IngotCraft texture references

- Extracted 43 unchanged item PNGs from D:/William Riegler/Downloads/ingotcraft-1.21.1-2.4.11-neoforge.jar into the project-root examples/ingotCraft/ directory, as requested. PNG integrity and byte-for-byte equality to archive entries verified; no animation metadata was present. Source filename and archive checksum are recorded in the ignored folder's README.
- Added /examples/ingotCraft/ to .gitignore. These third-party references are outside src/main/resources and are not packaged with the mod. No asset designs or source textures were changed.

## Meteoric iron lead-ingot reference (supersedes steel-based version)

- User requested IngotCraft's lead ingot as the meteoric-iron reference. The reference folder is now src/main/examples/ingotCraft/ (user relocated it); added that path to .gitignore while retaining the old root rule. Viewed only the approved lead reference, not the existing meteoric-iron texture before replacement.
- scripts/recolor_meteoric_iron_ingot.ps1 applies the saved six-color meteoric-iron palette to lead_ingot.png, preserving exact 16x16 pixel positions and alpha: 135 opaque/121 transparent. Roles: #0D1931 outline; #1F2B43 and #26324A deep shadow; #2D3951 shadow; #3F4B63 base; #626E86 and #8D97AC light; #B6BFD0 glint. Existing generated item model still points to the same runtime path.
- Removed meteoric iron from scripts/recolor_metal_ingots.ps1 so that script now reproduces only tungsten/titanium from steel. Cropped its preview to those two materials and added markdown/meteoricIronIngotPreview.png for lead/new meteor/dark-light comparison. Updated palette notes and changelog. Other runtime textures were not changed.
- All 256 output pixels verified against the lead alpha and exact palette mapping; all six colors occur. Preview inspected and relocated reference ignore rule verified. No commit/push requested.
- Gradle processResources passed; the packaged PNG hash matches the new runtime texture. git diff --check passed.
- Prior task cancellation: user stopped titanium-sponge redesign and will design it personally. The rock draft had already been written before the stop; do not resume that design or rerun its export script without a new instruction. Its in-progress draft/script changes were left untouched by this meteoric-iron task.

## Reinforced iron IngotCraft steel reference

- User requested IngotCraft steel_ingot.png as the reinforced-iron ingot reference and explicitly prohibited looking at the old reinforced-iron texture. Read/viewed only src/main/examples/ingotCraft/steel_ingot.png before direct replacement; never opened or sampled the old target.
- Added a neutral iron-silver palette with ten roles/colors in markdown/metalPalettes.json to preserve all ten source shading steps. Unlike the other six-color ramps, this entry overrides the roles list. Full source-to-target mapping is in markdown/metalPalettes.md. scripts/recolor_reinforced_iron_ingot.ps1 reproduces only this asset and markdown/reinforcedIronIngotPreview.png.
- Verified all 256 pixels against source alpha and exact palette mapping: 16x16, 135 opaque/121 transparent pixels, binary alpha, ten distinct shades retained. Inspected the output on light/dark backgrounds; the existing generated item model references the unchanged runtime filename. Gradle processResources passed. No other runtime texture was changed; keep the user-cancelled titanium-sponge task stopped.

## Raw titanium and raw tungsten recolors

- User requested unrefined steel and unrefined carbon steel as references and prohibited opening previous raw textures. Used metals/raw/unrefined_steel.png for titanium and metals/raw/unrefined_carbon_steel.png for tungsten. Only these approved references were read/viewed before replacing the two destinations.
- scripts/recolor_raw_titanium_tungsten.ps1 applies the saved six-color palettes to the two reference layouts and writes only root-level raw_titanium.png and raw_tungsten.png plus markdown/rawTitaniumTungstenPreview.png. Existing generated item-model paths remain valid. It does not write magnesium, dolomite or the cancelled titanium sponge.
- Verified all 256 pixels of each output against the source alpha and specified palette mapping: 16x16, binary alpha, all six shades, 139 opaque pixels for titanium and 141 for tungsten. Inspected the output preview. Gradle processResources passed. Updated palette notes/status and src/changes.txt; no commit/push requested.

## Damask and reinforced iron billet designs

- User offered shards, steel/titanium/tungsten ingots, unrefined steel/carbon steel, then Sturdy Sheet.url and Zinc Ingot.url as possible directions. Inspected the approved references only, not previous billet textures. Selected Sturdy Sheet's layered plate for Damask and Zinc Ingot's cast chunk for reinforced iron. Earlier rough-bar planning was superseded after considering the two supplied sheet/chunk references.
- The shortcut URLs rejected automated downloads, so corresponding PNGs were retrieved from Creators-of-Create/Create's mc1.21.1/dev source branch. Local originals are src/main/examples/create/{sturdy_sheet,zinc_ingot}.png, newly ignored in .gitignore and outside packaged mod resources. Full links and mapping notes are in markdown/billetDesigns.md.
- scripts/recolor_billets.ps1 reproduces only damask_billet.png and reinforced_iron_billet.png plus markdown/billetPreview.png. Exact 16x16 source layouts/alpha preserved; Damask uses a new six-color gray-steel palette saved in metalPalettes.json, reinforced iron uses the first nine shades of its existing ten-color palette (no polished final glint). Source shading is mapped consistently, with no spatial edits.
- Verified 106 opaque/150 transparent pixels and six colors for Damask; 113 opaque/143 transparent and nine colors for reinforced iron, correct existing item-model paths and binary alpha. Preview inspected; Gradle processResources passed. User-cancelled titanium sponge remains untouched. No commit/push requested.

## Titanium sponge: explicit restart with crushed raw zinc

- User explicitly reopened this previously cancelled task by supplying Crushed Raw Zinc.url as the reference. This authorizes this specific reference-based revision; do not revive the old generated rock/sponge designs. Neither existing target nor previous generated drafts were viewed/sampled for the revision.
- The shortcut points to a Create Wiki image endpoint that could not be retrieved. Used the corresponding crushed_raw_zinc.png from Creators-of-Create/Create's mc1.21.1/dev source branch, stored in the already ignored src/main/examples/create/ folder. Full source URL and mapping are in markdown/titaniumSpongeReference.md.
- scripts/export_titanium_sponge.ps1 is now a simple exact reference recolor; removed its imagegen/downsampling/pit-retention code. It reads only the new approved source and saved palette, writes only titanium_sponge.png and markdown/titaniumSpongeCrushedZincPreview.png. Earlier generated assets were not deleted but are no longer used by the script.
- All 256 output pixels verified: 16x16, exact source alpha/layout, 98 opaque/158 transparent, binary alpha and all six titanium colors. Preview inspected on dark/light backgrounds, existing generated model still points to the same runtime path, and Gradle processResources passed. Updated palette notes/status and src/changes.txt. No other runtime textures changed; no commit/push requested.

## Damask billet chunk revision (supersedes sheet design)

- User rejected Damask billet as looking like a sheet of iron. Replaced only damask_billet.png with a direct recolor of the approved metals/raw/unrefined_carbon_steel.png. Did not open/sample the old Damask texture. Uses palette indices 0-4 (matte finish without white glint), 141 opaque/115 transparent pixels, exact source alpha/layout.
- scripts/recolor_billets.ps1 now supports -SelectedMaterial damask; this revision used it and verified the relocated reinforced-iron billet hash stayed unchanged. Corrected that script's reinforced output path to the user's metals/raw folder without regenerating that asset. Current Damask runtime/model path remains item/damask_billet. Updated billetDesigns.md, palette finish notes and billetPreview.png.
- Verified all 256 pixels against reference mapping, inspected preview, Gradle processResources passed and packaged Damask hash matches. No commit/push requested. Preserve unrelated staged texture moves.

## Damask billet Zinc Ingot reference (supersedes raw chunk revision)

- User explicitly selected Zinc Ingot.url for Damask billet. Used the existing approved src/main/examples/create/zinc_ingot.png corresponding to that shortcut, never the previous Damask texture. scripts/recolor_billets.ps1 now uses Zinc Ingot for both billets with their own palettes; regenerated only Damask using -SelectedMaterial damask. Reinforced-iron runtime hash verified unchanged.
- Damask uses six saved shades: source colors #213B3F->0, #31544E->1, #506D63/#788273->2, #929B87/#A5C0A0->3, #B9E9C1/#D6FFDC->4, #FFFFFF->5. Exact source shape/alpha: 16x16, 113 opaque/143 transparent. Runtime/model path remains item/damask_billet.
- Updated billetDesigns.md, palette finish and billetPreview.png. Verified all 256 output pixels, all six shades, preview and packaged PNG hash; Gradle processResources passed. No commit/push requested.

## Damask billet actual ingot palette (supersedes proposed palette)

- User specified Zinc Ingot as shape reference and Damask ingot as palette reference. Read only approved zinc_ingot.png and textures/item/metals/ingots/damask_ingot.png, never the previous billet texture. Sampled all nine ingot colors in luminance order: #111111, #312929, #352D2E, #353535, #3B393B, #5E5E5E, #666566, #737173, #828282. Mapped the nine Zinc Ingot source shades one-to-one.
- scripts/recolor_billets.ps1 derives Damask's colors directly from the ingot PNG, checks there are nine shades before writing, and leaves reinforced iron's JSON palette logic unchanged. Used -SelectedMaterial damask; verified ingot and reinforced-iron hashes unchanged. The old six-color Damask proposal in metalPalettes.json/md is explicitly marked superseded for this billet. Updated billetDesigns.md and billetPreview.png.
- Verified all 256 pixels against exact mapping/source alpha, 113 opaque/143 transparent, output palette exactly equals ingot palette. Preview inspected, Gradle processResources passed, packaged PNG hash matches. No commit/push requested; preserve unrelated staged moves.

## Raw meteoric iron crushed-raw reference

- User selected Crushed Raw.url as raw meteoric iron shape reference and the saved meteoric iron palette; explicitly prohibited looking at the previous texture. The shortcut points to Create crushed copper ore. Wiki endpoint inaccessible; retrieved corresponding official Create mc1.21.1/dev crushed_raw_copper.png into ignored src/main/examples/create/. Never opened/sampled the old target.
- scripts/recolor_raw_meteoric_iron.ps1 reads only that approved source and metalPalettes.json, writes item/raw_meteoric_iron.png and markdown/rawMeteoricIronPreview.png. Source family shading mapped consistently to the saved six-color meteoric iron palette. Full mapping and source URL in markdown/rawMeteoricIronReference.md; linked from metalPalettes.md. Existing item-model path remains unchanged.
- All 256 pixels verified against mapping and exact source alpha/layout: 16x16, 107 opaque/149 transparent, binary alpha, all six shades. Preview inspected, Gradle processResources passed, packaged PNG hash matches. No other runtime textures changed; no commit/push requested.

## Latest item asset reorganization

- User moved the remaining 10 flat-root runtime PNGs into the organized folders. Updated generated item model texture slots only: metals/ingots/{meteoric_iron_ingot,reinforced_iron_ingot,titanium_ingot,tungsten_ingot}; metals/raw/{damask_billet,reinforced_iron_billet,raw_meteoric_iron,raw_titanium,raw_tungsten,titanium_sponge}. Model parents, transforms, ids unchanged. Existing ModItemModelProvider already searches these locations and needed no edit.
- Updated eight recolor/export scripts' outputs to these paths; tungsten powder now reads the relocated src/main/examples/vanilla/gunpowder.png. Updated current billet/sponge/raw/palette docs and itemTextureMigration.md. No PNGs were read, viewed, regenerated, modified or restored; preserved user staging and new item/palletes assets.
- Gradle cleanProcessResources jar -x createMinecraftArtifacts passed. Verified 10 new jar paths and absence of their old copies, packaged model equality, unchanged non-texture JSON fields, literal script paths, PowerShell syntax and animation sidecars. Source and jar audits: zero unresolved relocations, same 25 genuinely missing tool PNGs. Audit logs: build/item-asset-restructure-{source,jar}-audit.log. Model snapshot: build/item-asset-restructure-before.json. No commit/push requested.

## Missing custom tool textures no longer block data generation

- User explicitly wants tools to work before their art exists. ModItemModelProvider.itemTexture now returns the matching minecraft:item/iron_{pickaxe,axe,hoe,shovel,sword} when no custom PNG is found; other missing names use minecraft:item/barrier. Logs a warning with instructions to rerun data generation after adding art. Existing custom files always take priority; ambiguous files still throw. No placeholder PNGs are created and no deleted art restored.
- Ran runData successfully (25 expected warnings), regenerating all 25 unfinished tool models with vanilla layer0 textures. No generated data recipes/tags changed. Updated itemTextureMigration.md; older notes about missing-tool generation failures are superseded.
- Full Gradle build -x createMinecraftArtifacts passed including existing standalone checks. Verified all 25 placeholder mappings in source and jar, unchanged model parents/transforms, and existing custom carbon-steel pickaxe retained. Source and jar texture audits both pass with zero unresolved references. Logs: build/missing-texture-datagen.log, build/missing-texture-build.log. No ability changes or commit/push requested.

## Player-facing ability text cleanup

- User requested removing excessive, redundant and internal information from passive/active descriptions. Reviewed all 108 registered abilities (59 active/49 passive), editing only English description/card translation values: 196 changed strings, roughly 35% shorter tooltips. Removed hardcoded cooldown tables because SwordItemWithEffect already adds the actual configured cooldown row, arm-time boilerplate, long per-rank tables, implementation details and decorative particle explanations. Kept activation triggers, meaningful costs/effects and important restrictions (lava exclusion, self-poisoning, empty offhand, matching daggers, critical hits). Super-form benefits remain concise named clauses.
- Updated scripts/update_ability_docs.py output (markdown/abilityDescriptions.md and weaponAbilities.md) from existing build/ability-catalog.json. Added .card summaries for passive.axe and passive.dagger so cards no longer fall back to full descriptions. No Java/mechanical changes, no names/keys/format placeholders changed. Material-effect format remains unchanged.
- Gradle processResources jar -x createMinecraftArtifacts passed. Validated unique JSON keys, all 108 catalog descriptions/card summaries, unchanged unrelated translations/placeholders/names, description documentation parity and exact packaged language JSON. git diff --check passed. No gameplay test needed for text-only changes and no commit/push requested. Review baselines/editorial map stored only under ignored build/ability-descriptions-before.json and build/ability-description-edits.json.

## Homesick restricted to Pulsite (supersedes Electrium assignment)

- User requested Homesick only on Pulsite weapons. Changed AdditionalAbilities.HOMESICK pool to Pulsite dagger/claymore/axe and HomesickEvents runtime gate to PulsiteWeapon. All 15 base/gemstone variants supported. Existing Electrium saves carrying this id are filtered out by Augmentations.learned and cannot activate it; no changes to buffs, Jade reinforcement, sculk requirements, particles or matching-dagger sharing.
- Updated HomesickGameTests to Pulsite fixtures and added all-15-Electrium regression coverage for augmentation, command level setting, merging and stale saved data. Existing full-registry pool test excludes every other material. Registry export confirms exactly five daggers, five claymores and five axes, all Pulsite. Refreshed abilityDescriptions.md and weaponAbilities.md from actual registry catalog.
- Full build and all 283 required GameTests passed; reviewed code and reran all 283 successfully. Logs: build/homesick-pulsite.log and build/homesick-pulsite-reviewed.log. git diff --check passed. Per AGENTS.md, commit/push this ability correction only; preserve unrelated dirty texture/model/text edits and existing staged asset moves.

## Direct Current allies and Static Field correction

- User requested all mobs as Direct Current targets and stunned/highly damaged Static Field occupants instead of knockback. Scoped validTarget now includes every living Mob (passive/allied included), retaining existing player-target PvP checks. Both anchor selection and ongoing validation use it. Added dedicated direct_current damage type with additive no_knockback and bypasses_armor tags and death messages; vanilla 1.21.1/NeoForge source confirms NO_KNOCKBACK support.
- Static Field acquires a separate two-tick control-lock lease renewed for every interior occupant, including caster as before for field damage. Cancellation releases both anchor and interior leases. Walls block field formation; unrefreshed interior leases expire normally. Field damage is 4/5/6/7 HP every 10 ticks at super I/II/III/IV, matching barrier cadence; shared shockReady prevents barrier/interior double hits. Replaces old 1-1.75 HP per second field. Asked optional damage preference asynchronously; no reply arrived, so used the stated barrier-rate assumption after allowing time to answer. Earlier session authorization permits choosing missing balance values.
- Updated concise Direct Current descriptions/cards, abilityDescriptions.md and markedAbilityImplementation.md. Added owned-wolf/passive-cow anchor regression and strengthened field/control/no-knockback/damage/cancel tests. Full build and all 284 required GameTests passed twice (build/direct-current-fix.log and build/direct-current-reviewed.log). Verified packaged damage JSON/tags/language and unrelated translations unchanged. Commit/push this ability correction separately per AGENTS.md; preserve unrelated staging and earlier texture/text edits.

## Green upgrade descriptions in augmentation UI

- User requested highlighting exactly what an upgrade changes, using the resulting value in green. Added common read-only AugmentUpgradeDescriptions: compares the offered old rank against rank+1, emits localized stat/effect components, colors changed values #248A3D, and leaves labels/unchanged details gray. Supports the actual current runtime formulas, cooldown-only upgrades, reinforcement roster/equipment changes (unchanged mob types remain gray), Jade duration bonuses, template/weapon-specific Windcharge and super forms. It does not apply augments or modify mechanics/balance.
- AugmentationTableScreen uses compact comparison text on upgrade cards and full ability explanation plus comparisons on hover. New cards keep their learning summaries with translation fallback. Retains gold super headings, mutes rejected highlights, keeps completed cards comparing original offered ranks, and fixes completed hover rank formatting to use the output stack. Body fitting uses the same 37px width/36px height with scale 0.5 down to 0.35; all current text fits at 0.4 or larger. Original padding/slot/selection behavior retained.
- Existing early Double Jump ranks, Material Hit ranks and some other ranks have no runtime improvement. Their cards explicitly say Effects unchanged at this level; no new bonuses were invented. This is an existing mechanical limitation, now exposed clearly by the preview.
- Added four GameTests covering every registered weapon/template offer without mutation, completed-state stability, numeric values, green/muted component styles, Jade, super forms, and unchanged reinforcement groups. Full build and all 288 required tests passed (build/upgrade-previews-reviewed.log and build/upgrade-previews-final.log; first pass also passed). Exported 161 distinct card components; verified all fit using glyph advances from the pinned vanilla client font. QA outputs: build/upgrade-preview-components.json, build/vanilla-font-advances.json, build/upgrade-preview-layout.json, build/check_upgrade_preview_layout.py. Packaged language JSON matches source, no duplicate translation keys, all learning summaries retained; git diff --check passed.
- Updated scripts/update_ability_docs.py intro, regenerated abilityDescriptions.md, added markdown/augmentationUpgradePreviews.md. This is one UI feature with no ability implementation or gameplay changes; no commit/push requested. Preserve all unrelated staged texture moves and earlier uncommitted asset/model/text changes.

## Current-rank ability descriptions and separate super hint

- User requested descriptions show only the current ability state, with a separate Shift-only super-upgrade notice under cooldown. Added common AbilityDescriptions, sharing AugmentUpgradeDescriptions runtime-stat values; routes special-weapon expanded descriptions, templates and learning/resulting-rank previews through it. Dynamic counts, durations, damage, ranges, rosters, weapon-type Windcharge and Jade effects replace level ranges and future-form clauses. Fixed-effect abilities retain their concise unchanged text. No gameplay/balance changes.
- Direct Current explicitly shows 2/3/4 targets at stored ranks 1/2/3. Only stored ranks 4..7 describe Static Field and its actual damage. Ghostwalk/Jumpscare Horror, Bone Guard/Necromancer, Double Jump/Windwalker and axe Windcharge/Skybreaker describe their present form only. Nonaxe Windcharge never offers Skybreaker. Templates describe both current launch heights because they have no receiving weapon type yet.
- AbilityDescriptions.appendSuperHint is called only in expanded tooltips, after the active cooldown or passive description. Short localized notice appears only on learned base forms with a real super successor (stored ranks 1..3), disappearing on conversion. Existing dynamic key names, light-gray descriptions, independent cooldowns, green upgrade comparisons and gold super upgrade headings are retained. Added explicit current-rank learning summaries and corrected singular jump/current armor wording.
- Added three GameTests for actual target counts, base/super/Jade separation, expanded/collapsed tooltip ordering and full-catalog side-effect-free descriptions. Updated existing DaggerReviewGameTests to recognize resolved current-description keys while retaining its Shift/style/configured-key assertions. Initial run found old-key test expectations and a test false positive matching the id null_receipt; corrected both. Full build and all 291 required tests passed in build/current-descriptions-reviewed.log. Final wording review/recheck recorded in build/current-descriptions-final.log.
- Exported 274 unique current descriptions and 119 learning cards; build/check_current_descriptions.py resolves all format arguments and verifies all learning cards fit the existing 37x36px body using pinned vanilla font metrics. All 161 upgrade cards also fit. Artifacts: build/current-ability-components.json, current-ability-resolved.json, learning-card-components.json and learning-card-layout.json. Updated changes.txt and ability doc intro/base summaries. This is a display-only feature; no ability mechanics implemented and no commit/push requested. Preserve unrelated staged assets and earlier dirty changes.
- During final verification user requested shutdown in 20 minutes; successfully ran shutdown.exe /s /t 1200 and informed them shutdown /a cancels it. Continue the description task while timer runs; do not cancel it automatically.

## Legendary presentation of super-ability names

- Added Augmentations.displayAbilityName for presentation: named super forms at stored ranks 4..7 have a GOLD parent and exactly one obfuscated k glyph on either side, separated by normal spaces. Inner name explicitly remains readable. abilityName remains the plain translated identity for existing logic, exports and progression assertions. No saved data, ranks, mechanics or translations changed.
- Routed weapon/template tooltips, augmentation offer names (cards and hover), anvil replacement prompts and single-target augment command feedback through the display method. Gray parent tooltip text cannot override the gold name; rank/key labels remain gray. Rejected card title/hover override only the gold parent with the existing muted gray, preserving their disabled state.
- Added one catalog-wide GameTest covering every real super form/rank, variants and templates, ordinary names unchanged, exactly two obfuscated glyphs, readable spaces/name, inherited gold, no style leaking into rank text, and muted rejected names. Export: build/super-name-components.json. Font-metric layout check build/check_super_name_layout.py confirms all five decorated names fit the existing title box/padding (smallest scale .375 for Jumpscare Horror). Full verification log: build/super-name-style-tests.log. Display-only feature; no ability implementation or automatic commit/push. Existing shutdown timer remains scheduled; no shutdown commands issued this turn.

## Ender Swing 3-block radius

- User requested level-I radius 3 blocks. Changed shared EnderSwingAbility radius from 1 to 3, so the damage query, exact squared-distance filter and sweep particle ring agree. The existing upgraded rank retains this radius and its existing damage multiplier; cooldown, targeting, lift, ceiling checks and offhand restrictions are unchanged.
- Updated the base/current-rank/learning descriptions and regenerated abilityDescriptions.md. Strengthened existing real-activation GameTest with an enemy exactly 3 blocks away (damaged and lifted), an enemy at 3.1 blocks (unaffected), and existing ally/self/ceiling/cooldown checks. Test air clearance widened for the new boundary fixture.
- Per AGENTS.md, test/review/retest/review and selectively commit/push this ability correction. Verification logs: build/ender-swing-radius-tests.log and build/ender-swing-radius-reviewed.log. Preserve the still-uncommitted glyph-position and legendary-item-name changes; snapshots under build/ender-swing-radius-before.
