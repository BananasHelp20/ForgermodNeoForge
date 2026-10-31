- I have already worked out a structure for the ancient Sword stand. it lies in: D:\Minecraft Modding internal\Mods\ForgermodNeoForge\run\saves\Structure\generated\minecraft\structures
and is named forgermod_ancient_grave_v2 as a nbt file. The structure should be named the Ancient Grave and spawn as a guarantied room of an ancient city. (up to one)
- when pressing shift+alt+s while holding an item that has 2 abilities (ignores offhanditem), the primary and secondary ability switch places, making the previous primary ability the secondary ability and the other one the primary abilty.

- make the forge, infusion table and augmentation table aswell as all crafting table, furnace and blast furnace recipes jei compatible.
- for passives, only one of the 2 daggers has to have them. (e.g. dual wield only has to be on the dagger in the main or offhand) (only works when the daggers are the same and one of them has dual wield).
- make gemstone ores rarer (same rarity as diamond, but only one ore block per vein). Implement a saphire ore, it has the same rarity as ancient debries and spawns in all dimensions. (stone, deepslate, endstone, obsidian, netherrack, blackstone varieties) For designing, use the lighter sapphire gemstone colors, and orient yourself by the other gemstone ore blocks. Make a blackstone variety for all other gemstone ores based on their other ore designs and blackstone.
- all weapons can now have a third ability (also include that as a tertiar ability in the keybindings, default: 3)
- make a fitting design for the augmentation table but keep the enchanting particles (abilies are simelar to enchantments)
- all weapons except for daggers can now have up to 5 passives
- the transition between the overlay overlaying the 2 slots and the following overlay takes way too long. (the overlay overlaying the 2 slots should end earlier so that the other overlay can start sooner) (augmentation table)
- when using a dead calm dagger in dual wield and both of them have double jump or windwalker, you are able to do an additional jump in the air. (if both of them have windwalker, you gain 2 additional jumps, if only one of them has windwalker, and the otherone has double jump, only one additional jump is added like both having double jump). When double jump hits max level, it changes it's name to Windwalker, and gains an aditional jump in the air.
- the notices "this item can be..." and "[hold shift] to..." should always be below the enchantments (seperated with a /n) if the item has enchantments. Enchantments should also have a "title" like Enchantments:/n[enchantments]. like with active abilites: Active Abilities:/n[abilities] and passives: Passives:/n[passives]
- find a new name for warden's needle based on it's texture and material. Find 5 fitting names and present them to me for me to choose

## Current task checklist (2026-10-25)

Check an item only after its implementation, relevant tests, and code review are complete. Existing instructions above remain intact; this checklist tracks the current work queue.

- [x] Thunderclap: remove the extra delay between a completed hit and the next chain.
- [x] Skull Throw: play an explosion and skeleton-death sound together on impact.
- [x] Strong-arm: allow armored summoned/allied mobs, preserve the fully charged critical-hit requirement, and allow repeated activation without switching weapons.
- [x] Crucible Grappler: accelerate block grappling more quickly; make Grappler IV block grappling free of cooldown while retaining the normal mob-attack cooldown; replace the current hook only after a new reachable aim point is validated.
- [x] Echo Salvage: persist 2/3/4/5 stored Vertical Strikes on the weapon, remove the per-hit damage cap and 30-second expiry, and release stored echoes on the next successful melee hit as an automatic Vertical Strike.
- [x] Blood Sacrifice: require more than 4 actual health points to activate; absorption does not bypass this requirement.
- [x] Distance Tax: make it an active ability arming the next successful melee hit; preserve learned ranks and keep it inactive when three other active slots are occupied.
- [x] Static Field: form a line with two marked mobs, close and fill a triangle/quadrilateral with three/four, retain every marked corner, and affect neutral/allied mobs. Full build and all 403 required GameTests passed; code review complete.
- [x] Augmentation table: implement the animated forge/enchanting machine design with rotating assemblies, runes, floating core/weapon, and retained enchanting particles; verify rendering assets and synchronized state.
- [x] Necromancer UI: center equipment/previews and the remove icon, remove roster numbering, and widen the reinforcement picker so names fit.
- [x] Absolute Nightmare: use vanilla cave ambience for activation. Build succeeded, all 403 required GameTests passed twice, and code review is complete.

- [x] `/augment upgrade <player>` without an ability name: upgrade every learned augment by one level where possible on mainhand and offhand; retain named-ability behavior.
- [x] One-task naming correction consumed: applied the subsequent Gravity Well ender-sound request to Gravity Swing. Future Gravity Well tasks refer to Gravity Well unless corrected again.

- [x] Vertical Strike: add a heavy impact sound for player and reinforcement strikes, including ability-triggered and Echo Salvage strikes. Build and all 404 required GameTests passed; code reviewed.

- [x] Flaming Stomp: retain the heavy Vertical Strike impact and add a hot, fiery burst at impact. Build and all 404 required GameTests passed twice; code reviewed.

- [x] Void Pulse: reversed Ender Pearl teleport sound during inward particles, followed by normal teleport sound on pulse release. Custom audio verified; build and all 405 required GameTests passed twice; reviewed.

- [x] Upwind: no activation sound; play its wind sound at the affected enemy when the empowered hit launches it. Build and all 405 required GameTests passed; reviewed.

- [x] Windcharge: use the vanilla wind-charge burst sound when launching, including Skybreaker launch; preserve its heavy landing cue. Build and all 405 required GameTests passed twice; reviewed.

- [x] Ringing Steel: give the empowered hit a sharp metallic impact, resonant ringing tail and brief sparks at the enemy; preserve gameplay effects. Build and all 405 required GameTests passed twice; reviewed.

- [x] Strengthened Bones: play its sound when the qualifying kill grants invulnerability, rather than when armed. Build and all 406 required GameTests passed; reviewed.

- [x] Crucible Hook: play hook/chain grappling audio on a successful cast, followed by the fiery heavy Vertical Strike sound when its smash lands; block grappling plays cast only. Build and all 406 required GameTests passed; reviewed.

- [x] Rooting Roots: upgrade the sound with deeper cracking roots and disturbed earth. Build and all 406 required GameTests passed twice; reviewed.

- [x] Gravity Swing (labelled Gravity Well; earlier one-task correction applied): added ender departure/airborne-arrival sounds and a deeper gravity-pressure cue. Fixed maximum-height audibility; build and all 406 required GameTests passed; reviewed.

- [x] Pocket Storm: verified sound plays only on the empowered melee hit, never arming, unrelated damage or cancellation; added native regression checks. Build and all 406 required GameTests passed; reviewed.

- [x] Static Blade: moved sound from arming to the releasing slash; dual wield plays it once for each launched blade. Verified silent arming and no duplicate/rejected-release sound; build and all 407 required GameTests passed; reviewed.

- [x] Sonically Charged critical hit: added a fitting sonic impact sound only when the charged critical lands. Verified normal hits and subsequent uncharged crits do not play it; build and all 408 required GameTests passed twice; reviewed.

- [x] Alternating Current: electrical sound travels with each current jump. Native direction/wall/cancellation and packet checks passed; full build and all 409 required GameTests passed; reviewed.
- [x] Direct Current / Static Field: electrical sound travels along live paths, including closed edges, at most once per second per edge. Build and all 410 required GameTests passed twice; reviewed.
- [x] Chain Lightning: each successful hop carries spatial electrical travel audio, including reinforcement passives. Failed chains remain silent. Build and all 410 required GameTests passed twice; reviewed.

- [x] Gravity Well (not Gravity Swing): added ender-like pull and impact audio while preserving the heavy Vertical Strike smash. Build and all 410 required GameTests passed twice; reviewed.

- [x] Static Field barriers: restored jagged lightning boundary animation and retained particle-only interior fill. Full build and all 410 required GameTests passed twice; reviewed.
- [x] Electrical sounds: extracted actual Minecraft Dungeons lightning sounds from the installed game; added three lighter current mixes and three heavier Chain Lightning mixes, preserving traveling playback. Sources documented; build and all 410 required GameTests passed twice; reviewed.

- [x] Stepping Through the Void: normal view-direction blink only, with no Etherwarp/speed boost; level I range 10 blocks and existing cooldowns. Removed floor sliding and corrected diagonal swept collisions. Full build and all 412 required GameTests passed; reviewed.
- [x] Ringing Steel: fixed allied reinforcement targeting and saved bank-weapon wear/breakage; replaced stone/bell hit layers with a real metallic impact and decaying steel ring. Native nonlethal/lethal reinforcement tests and all 413 required GameTests passed; reviewed.

- [x] Rooting Roots: rebuilt root-heavy growth, binding and retreat audio with six custom mixes and bounded local overlap. Build and all 416 required GameTests passed; reviewed.

- [x] Strengthened Bones: replaced quiet cues with a distinct full-volume bone-crack impact on the protection-granting hit. Verified native attacks across all five dagger variants; build and all 416 required GameTests passed twice; reviewed.

- [x] Void Pulse: added a resonant End Portal sound pair with reversed charge and forward release, preserving timing and Gravity Well audio. Build and all 416 required GameTests passed twice; assets verified; reviewed.

- [x] Echo Salvage: added a distinct sculk/sonic discharge with descending echoes; verified native and converted Vertical Strike releases and reinforcement attacks. Build and all 416 required GameTests passed twice; assets verified; reviewed.

- [x] Jade tooltip: replaced the long bonus sentence with two separate green lines: +1 effect level / +1 second effect duration. Build and all 416 required GameTests passed; reviewed.

- [x] Ancient City add-on chests: added complete vanilla Ancient City loot plus an independent 10% chance for one sapphire in both templates. Verified placement, unique seeds, one-time generation and unchanged vanilla loot across 256 fixed seeds; build and all 418 required GameTests passed; reviewed.

- [x] Ancient City add-on: raised selected placement by exactly one block in normal and fallback paths, preserving horizontal search and rotation. Verified 48 city seeds; build and all 418 required GameTests passed; reviewed.

- [x] Stepping Through the Void: fixed eye-ray surface landings on nearby blocks/slabs through nonblocking plants and refined shortened safe landings before walls/unsafe headroom. Corrected native ray inside-point precision. Build and all 421 required GameTests passed twice after fixes; reviewed.

- [x] Strengthened Bones sound: replaced both mixes with original non-vocal locking/protection tones, with no mob samples or skeletal rattles. Assets verified; build and all 421 required GameTests passed; reviewed.

- [x] Mod settings: added readable labels/help for all six options, screen headings and list button. Build passed; complete key coverage and packaged translations verified; reviewed.

- [x] After the remaining description review: apply approved Magma Toss wording with the full current-rank "{duration} seconds" phrase in white (review notes item43); no gameplay change. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply approved Magnetic Throw wording from review item44; no gameplay/range change. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Null Receipt wording from item45, with the complete current-value "{hitCount} incoming hits" phrase white. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply approved Pocket Storm wording from item46; no gameplay change. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Poisoned Vein item47 wording/Jade-conditional styles and exempt its caster from poison, preserving other occupants and existing durations/cooldown. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Ringing Steel item48 wording with white current durability damage; damage both eligible held weapons and scale damage by rank after confirming the missing progression. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Rooting Roots item49 immediately under the new direct-implementation workflow; range white, conditional duration color, 11 seconds for both effects with Jade. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Skull Throw item50 directly, with approved conditional colors and Wither10/12/14/16 seconds plus1 with Jade. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Sonic Boom item51 directly with white 50 block range and white numeric current weapon damage; no combat rebalance. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Sonically Charged Crit item52 directly with white current percentage and approved5/10/15/20% progression capped20%. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Static Blade item53 directly with white current range/damage and approved dual-wield wording; retain gameplay. Implemented directly under the new user directive; verified and reviewed.

- [x] After the remaining description review: apply Stepping Through the Void item54 wording and both-dagger augmentation requirement now because gameplay changes; record future wording-only edits for later. Implemented directly under the new user directive; verified and reviewed.

- [ ] Deferred wording-only review55 Storing Anger: apply approved annotated sentence later, first duration phrase white, both duration placeholders synchronized; no gameplay change.

- [ ] Deferred wording-only review56 Strengthened Bones: apply approved absolute-invulnerability sentence later with white current duration phrase; no gameplay change.

- [ ] Deferred wording-only review57 Strong-arm: apply approved critical-hit sentence later, complete "{chance}% chance" phrase white; preserve current fully charged critical-hit mechanics and progression.

- [x] Review58 Stunning Presence: apply approved white current radius/pulse wording and expand targets to every other living entity, caster unaffected; Verified and reviewed: build, all433 required GameTests and rerun unit checks passed.
- [ ] Deferred material-effect description wording: all nine use "Apply {materialEffect} {amplifier} for {duration} seconds on hit." Effect/amplifier/duration white or jade-green, "for" gray; retain gameplay.

- [x] Deferred wording-only review59 Sudden Presence: apply approved sentence with complete current "{blocks} blocks" range phrase white; preserve mechanics. Superseded by and completed with confirmedI-VII progression; build/all434 GameTests and rerun unit checks passed.

- [x] Review59 Sudden Presence gameplay follow-up: increase maximum radius to40 blocks and apply approved white range description; confirm per-rank progression, then implement/test/review/commit/push before60 Swing Attack. Supersedes the earlier wording-only deferral for59. Superseded by and completed with confirmedI-VII progression; build/all434 GameTests and rerun unit checks passed.

- [x] Review59 final implementation: Sudden Presence regular levelsI-VII have10/15/20/25/30/35/40-block radius, approved current range phrase white; cap commands/templates/offers and verify target boundaries, text/previews and safe landing before commit/push. Superseded by and completed with confirmedI-VII progression; build/all434 GameTests and rerun unit checks passed.

- [x] After Sudden Presence review59 is implemented/verified/committed/pushed: add Stepping Through the Void super ability "Warping Space", matching requested Hypixel SkyBlock AOTV behavior. Clarify Etherwarp versus normal blink, super-level values and cooldown before dependent implementation; test/review/commit/push separately. Completed with final confirmed range/cooldown rules; build/all438 required GameTests and rerun unit checks passed.

- [x] Warping Space/Void Step confirmed follow-up: base levelsI-III should blink5/10/15 blocks; super normal blink starts20 and caps40. Super adds Etherwarp when sneaking. Etherwarp progression recommendation30/40/50/60 pending user response; super blink intermediate ranges, cooldown and dual-wield Etherwarp policy still need resolution. Completed with final confirmed range/cooldown rules; build/all438 required GameTests and rerun unit checks passed.

- [x] Warping Space cooldown/dual-wield follow-up: normal regular+super blink doubles in valid dual wield; Etherwarp reaches up to100 blocks with an additional independent cooldown. Asked whether dual Etherwarp scales60/80/100/100 or is100 at every super rank, and whether cooldown is fixed30 seconds or45/40/35/30; awaiting answers before implementation. Completed with final confirmed range/cooldown rules; build/all438 required GameTests and rerun unit checks passed.

- [x] Warping Space final rules: regular blink5/10/15, super blink20/25/30/40, single Etherwarp30/40/50/60; dual doubles normal blink and doubles Etherwarp capped100. Independent Etherwarp45/40/35/30-second cooldown and7-second blink cooldown. Verify all ranks/variants, safe block targeting, dual cap, failed casts, cooldown separation, descriptions and upgrades, then commit/push. Completed with final confirmed range/cooldown rules; build/all438 required GameTests and rerun unit checks passed.

- [ ] Deferred wording-only review60 Swing Attack: apply approved sentence with complete current "{damage} damage" phrase white; preserve current targeting/range/damage/cooldowns.

- [x] Review61 Upwind approved unchanged; verified existing level-scaled knockback/launch and matching upgrade preview (0.6/0.8/1.0/1.2 atI-IV before resistance). No implementation change needed.

- [ ] Deferred wording-only review62 Vacuum Burst: approved "Pull items and xp within a {radius} blocks radius around you into your inventory." Full radius phrase white. Existing6/8/10/12 radius and30/25/20/15-second cooldown already meet the level-scaling request; preserve pickup/targeting mechanics.

- [x] Review63 Void Pulse: approved living-entities description, full current radius phrase white; expand targeting to all other living entities including nonhostile players while excluding caster/nonliving entities. Preserve damage-free push, radius/scaling/visibility/effects/cooldowns; test/review/commit/push.

- [ ] Deferred wording-only review64 Windcharge: "Launch about {height} blocks upward and negate falldamage on your next landing." Full current height phrase white; preserve weapon/rank launch values and landing protection.

- [x] Review64a Skybreaker: apply approved super description with full current height/radius phrases white; target all other living entities including allies/players for direct mace bonus and landing smash. Preserve launch/damage/fall protection/effects/cooldowns; test/review/commit/push.

- [ ] Deferred wording-only review65 Windy Dash: use exact "Dash into the direction you look." All prose gray; preserve dash physics and cooldown.

- [ ] Deferred wording-only review66 Aftershock: approved dynamic damage/delay sentence; "{damage} delayed extra damage" and "{delay} second(s)" white, after gray, suffix s when delay>1. Preserve gameplay.

- [x] Review67 Backstab: approved percentage-only white description; apply20% rear melee bonus to any living target, retaining vanilla damage rules and existing restrictions on other passives. Verify40 variants, passive/allied/front-side/secondary hits, text and other-passive targeting; review/commit/push.

- [ ] Deferred wording-only review68 Chain Lightning: approved current chance+"% chance" and max jump-count+"mobs" white, prose gray; preserve actual targeting/damage/jump behavior.
- [ ] Apply expanded review presentation for all upcoming abilities: name/type, compatible weapons, summary, super form, level changes/min/max, current text/white highlights, and color-annotated proposal; begin69 Charred Edge. Ongoing format directive, complete only after all remaining reviews.

- [ ] Deferred wording-only review69 Charred Edge: accepted proposal "Deal {percent}% more damage to enemies with Fire Resistance." Current15% white; condition/prose gray, no Jade-green. Preserve gameplay.

- [ ] Deferred wording-only review70 Cold Bellows: accepted "Blocking an attack with a shield ignites the attacker for {duration} seconds." Complete current duration phrase white, other prose gray; preserve mechanics/Jade behavior.

- [ ] Deferred wording-only review71 Compost: exact "Eating rotten flesh doesn't give you the Hunger effect." Entirely gray; preserve mechanics.

- [ ] Deferred wording-only review72 Conductive Edge: "Deal {percent}% more damage to enemies exposed to water." Whole20% white, other prose gray; retain existing water-or-rain enemy melee behavior.

- [ ] Deferred wording-only review73 Daydreamer: accepted "Gain Jump Boost {amplifier} during the day." Full effect+amplifier white normally/Jade-green with Jade, other prose gray; preserve conditional indefinite duration and mechanics.

- [x] Review74 Death March: approved target-health wording, complete current maximum percentage+more damage phrase white; regular ranksI-V have maximum+50/100/150/200/250%, shared runtime/current text/upgrade values and capV. Verify all variants/ranks/health fractions, direct-only/unlearned, templates/commands and reinforcement passive rank; review/commit/push.

- [x] Review75 Death's Due: approved white current percentage wording, rankI25% and rankII50% XP bonus, capII; shared runtime/text/previews including reinforcement passive weapon rank and existing bonus-then50%-penalty order. Verify15 variants, rounding, real kills, caps and guard rank; review/commit/push.

- [x] Review76 Double Jump: approved white current extra-jump phrase, baseI1/baseII2 jumps per dagger. Keep Windwalker as next upgrade afterII (confirmed); await requested superI-IV counts, then implement progression/text/previews/legacy mapping, test/review/commit/push. Completed with final confirmed progression/dual rules and legacy migration; build/all451 GameTests and rerun unit checks passed.

- [x] Review76 final Double Jump/Windwalker rules: base1/2 extra jumps, super2/3/3/4; height-only upgrades at superI/III, dual sums counts and uses higher height (up to8). Apply approved white count wording/current previews, migrate legacy ranks while preserving item/template data, verify budgets/momentum/reset/dual/caps/transfer then review/commit/push. Completed with final confirmed progression/dual rules and legacy migration; build/all451 GameTests and rerun unit checks passed.

- [ ] Deferred wording-only review77 Dual Wield: exact "Allows you to dual wield daggers of the same material. Only one dagger needs this passive." All gray; preserve mechanics.

- [x] Axe animation import: preserve all six authored clips/keyframes from Downloads/axe_playerplaceholder_animations(1).bbmodel; extract embedded fallback mesh/texture from Axe - Converted(1).bbmodel for mod axes missing artwork, preserving existing textures.
- [x] Alternate axe_swing/axe_swing2 for ordinary axe attacks; render authored player bones and held axe in first/third person and sync other viewers, clean up on switch/death/logout; use idle/walk as applicable.
- [x] Use axe_jump_attack for all Vertical Strike variants (native third strike, Flaming Stomp, Gravity Well, Crucible Hook smash, Echo Salvage conversion and related axe slams); user explicitly requires unchanged animation and delayed hit timing to match authored impact (~1.25s).
- [x] Map axe_grtg_axe_slam to Gravity Swing (casting pose + long swirling field fits group gravity effect; Gravity Well is a Vertical Strike and uses jump clip); preserve authored six-second visual and align cast effect at~1.625s. Handle switch cancellation and independent ability cooldown/session behavior.
- [x] Test/review all animation resources, model fallback coverage, timings/cancellation/critical charge/combat regressions; run datagen/build and meaningful native/client validation; fix failures and rerun, then scoped commit/push preserving unrelated changes. Resume manual description review at78 Echo Appetite only after animation task complete.

- [x] Axe animation visual review: correct source arm-cube rest rotation/pivots, held-mesh scale/grip, first-person exposed shoulder caps and root/particle alignment; compare real-client poses against the source hierarchy without editing animation keyframes.

- [x] Axe camera-switch crash: read actual crash report, remove visual assertions from normal mod/runtime sources, hard-guard isolated test directory, reproduce three actual attacks across first/third/first person, restore generated normal launch files, verify artifact/classpath exclusion and build, review and scoped commit/push.

- [x] Redesign augmentation table base as anvil/enchanting table/blast furnace mixture using dark netherite, steel and carbon-steel materials; keep floating sapphire, ring assemblies and enchanting particles.
- [x] Darken ring metalwork; make rings rest flat and stationary on the worktop outside crafting, lift/tilt/rotate during crafting, then slow and settle over about two seconds on completion/cancel. Check visual synchronization, real client and regression tests; restore normal launch settings.

- [x] Assemble the entire current augmentation table into a self-contained Blockbench project for final touch-ups, including both rings/runes, floating sapphire, named editable groups, embedded textures and preview animations.

- [x] Design an infusion table for gemstone setting on valuable weapons: red carpet, precise tools and an anvil-like rest. Keep the remaining materials dark metal, with red accents. Deliver editable Blockbench concept and rendered preview.

- [x] Apply the edited infusion-table JSON without restoring removed objects. Only show the currently used gemstone during valid infusion, and lay the actual weapon on the small anvil; clear displays on cancel/finish/invalid or blocked crafting, preserve item data and independent table state.
- [x] Make ONLY the hammer handle wooden. Preserve dark metals/red accents elsewhere; extend the netherite-like metal texture with newly drawn detail and use proportional UV mapping. Update complete Blockbench source and preview, test/review material isolation and rendering/crafting synchronization, restore normal launch settings.

- [x] Make augmentation sapphire rise and rotate slowly only during augmentation, then smoothly return lower and stop on cancellation/completion/stopping, sharing ring easing/timing and retaining weapon clearance. Update complete Blockbench previews and verify in Minecraft/build.

- [x] Remove the resting sapphire gap and replace the custom augmentation crystal with the real Sapphire Gemstone item: flat on the table while idle, then rising/turning upright/slowly spinning and bobbing like a dropped item during augmentation, returning flat on stop/cancel/finish. Keep the weapon preview clear and update Blockbench previews; verify in game/build and restore normal launch settings.

- [x] Fix the infusion table bottom-edge world holes caused by full-footprint face occlusion on an inset model; preserve collision/geometry and verify floor rendering for all facings/lit states.

- [x] Import the upgraded axe_playerplaceholder_animations_2.bbmodel from Downloads without retiming authored keys; preserve smooth curves/new two-handed idle and updated swirl, use its new spinning animation for all Swing Attack variants, align damage timing, verify both camera views/build/GameTests, and restore normal launch settings.

- [x] Display each infusion-table input immediately and independently; animate valid infusion with a rising, rotating gemstone and gemstone-colored infusion sparks, settle on stop, preserve inventory/crafting, and verify synchronization/cancellation/completion/reload and real-client rendering.

- [x] Speed up ordinary axe swings and matching hit deadlines, restore natural walking/sprinting strides, fix shoulder/torso separation and audit all upgraded axe animation mappings/channels; verify native timing and both camera views.

- [x] Replace Vertical Strike fake forward/jump root motion with an automatic real jump, retain voluntary horizontal movement, hold the raised axe while airborne, and resolve all player vertical-strike variants only on landing; test cancellation, damage once, synchronization and in-game physics.

- [x] Ease normal axe swings back to a natural moderate speed with minimal hit delay; add the Vertical Strike raise/hold/landing animation to Skybreaker while preserving its launch, damage, fall protection and cleanup. Verify timings, all variants and real-client flight; commit only this task.
