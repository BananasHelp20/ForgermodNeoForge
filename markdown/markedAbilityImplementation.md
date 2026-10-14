# Marked ability implementation record

Only entries marked for implementation in abilityRecommendations.md are approved. Repeated entries share one implementation across their listed weapon families and gemstone variants. Abilities are learned through augmentation; each weapon retains three active and five unique passive slots (two passives for daggers), plus Material Hit separately. Missing balance values below were chosen with the user's permission.

Quench Point was removed at the user's request on 2026-10-14; retired saved entries are ignored.

## Completion status

All implementation-marked entries are complete. The final live-file audit also verified re-added marks and updated weapon assignments.

Heating Up's corrected maximum-level behavior has been implemented; see its completed entry below.

## Explosion - completed

- Weapons: Infernal Claymore and all four gemstone variants.
- Grants Resistance IV for two seconds, then immediately creates a vanilla creeper-strength (power 3) explosion. Its causing player is excluded from damage and knockback; blocks are not destroyed.
- Rank I-IV cooldowns: 60/55/50/45 seconds. Activation completes immediately, so cooldown starts immediately.
- Learned-only key dispatch and cooldown persistence are integrated. New ability deadlines follow ability identity through key swaps and are independent across registered variants; prior dagger cooldowns are preserved.
- Server coverage: all five variants, no unlearned activation, enemy damage, survival at 1 HP, no self-knockback, terrain preservation, replay rejection, independent variant deadlines, rank-IV cooldown and Resistance expiry. Reviewed build/lightweight/server checks passed all 62 required tests.

## Cinder Decoy - completed

- Weapons: Infernal Claymore and all four gemstone variants.
- Ember-colored humanoid silhouette with the caster's head attracts visible hostile mobs within ten blocks. Hostile contact, melee or projectile impact detonates a power-two explosion with no terrain destruction or self-damage.
- Ignites surviving unobstructed hostile enemies within four blocks for 3/4/5/6 seconds at ranks I-IV. Silhouette lifetime is 6/8/10/12 seconds; cooldown is 45 seconds after ending or cancellation.
- One non-persistent, non-lootable decoy per caster. Switching, death, logout, dimension change and expiry remove it and restore valid previous mob targets. It can coexist with Explosion on the other key.
- Reviewed tests cover all variants, targeting/restoration, explosion/ignition, owner safety, equipment protection, contact, real hostile arrows, expiry, logout, switching and independent cooldowns. Final build/lightweight/server checks passed all 66 required tests. Client visual appearance still needs in-game review.

## Cold Bellows - completed

- Weapons: Infernal Claymore and all four gemstone variants, held in the main hand while a shield is used.
- Successful damage blocks ignite the source attacker (including the shooter of a projectile) for 3/4/5/6 seconds at ranks I-IV. Failed/canceled blocks and zero blocked damage do not trigger it; allies are excluded.
- Tests cover every variant and the real survival-player raised-shield damage pipeline, plus rejected/canceled/zero blocks, projectile ownership, missing shield and unlearned weapon rejection.
- Minecraft's default test player hardcodes creative mode. Added a survival-mode fixture and reran Explosion/Cinder protection checks with it. Reviewed build/lightweight/server checks passed all 68 required tests.

Heating Up's fire absorption is now part of its default learned effect at every rank: extinguish the wielder's fire to gain heat while capacity remains; at full capacity the wielder stays burning.

## Heating Up - completed

- Weapons: Infernal Claymore and all four gemstone variants.
- Store half of actual melee damage taken, capped at 20 heat. Spend five heat per successful attack to ignite all affected targets, including sweeps, for 3/4/5/6 seconds at ranks I-IV.
- At every learned rank, remaining fire on the wielder becomes heat (one heat per burn second) and is extinguished if capacity remains. Full capacity leaves the wielder burning. Heat is saved on the sword and persists across switching and saves/logout. Only sweep-payment bookkeeping resets; stored heat is shown as a gray tooltip extension rather than a HUD overlay.
- Ranged damage does not fill heat; canceled/failed hits do not spend it. Sweeping targets share one payment, while distinct same-tick attacks pay separately.
- Fire Aspect on the Heating Up sword makes its heat-funded ignition permanent until extinguished. Requires a successful paid hit; ordinary Fire Aspect without Heating Up or enough heat remains timed. Burn persistence survives saving and preserves vanilla damage cadence; water, explicit extinguishing and fire-immune targets are respected.
- Reviewed build/lightweight/server checks passed all 70 required tests, including all five variants, actual survival damage, sweep cost, separate-attack isolation, projectile exclusion, burn absorption/full capacity and exact-stack switching cleanup.

## Fire Resistance - completed

- Weapons: Infernal Claymore and Molten Axe, including all gemstone variants.
- Infinite Fire Resistance while holding the learned weapon in the main hand at level I; level II also qualifies in the offhand and is the maximum.
- Protection ends when neither hand qualifies. Independent potions, hidden effect chains and naturally infinite effects are preserved with their normal timers. Curing removes old potion ownership; continued held protection can resume without resurrecting a cured potion.
- Reviewed build/lightweight/server checks passed all 73 required tests, covering all ten variants, hand restrictions, rank cap, actual survival fire damage, removal, potion mixing/hidden expiry, curing and logout.

## Explosive Hits - completed

- Weapons: Infernal Claymore and all four gemstone variants. Active, as the user confirmed.
- Arms ten successful melee hits. Each produces a small blast dealing exactly 30% of current weapon attack damage to nearby non-allied living targets, including the original victim. No caster damage or terrain changes; blocks obstruct it.
- Radii at ranks I-IV: 1.5/2/2.5/3 blocks. Cooldown is 45 seconds after all ten charges finish or switching cancels them. Failed hits retain charges. Secondary damage cannot recurse into melee-trigger abilities.
- Reviewed build/lightweight/server checks passed all 75 required tests, covering every variant, exact damage/count, no recursion/eleventh blast, failed hits, same-stack arm rejection, switching, walls, rank-IV self-safety, original-victim damage and actual key-handler cooldown behavior.

## Nether Born - completed

- Weapons: Infernal Claymore and Molten Axe, including all gemstone variants.
- Exactly 20% more melee and weapon-explosion damage while holding the learned weapon in the Nether. No bonus in other dimensions or on unrelated projectile damage.
- Chosen cap: one level, because the specified fixed bonus has no separate upgrade behavior; this avoids wasting ingredients on identical upgrades.
- Reviewed build/lightweight/server checks passed all 76 required tests. All ten variants tested against real Nether damage context, with exact melee/explosion bonus, fresh-weapon and Overworld exclusion and cap.

## Crucible Hook - completed

- Weapons: Molten Axe and all four gemstone variants.
- Aimed, unobstructed enemy within twenty blocks is pulled with velocity, then smashed upon entering melee range. No teleportation or wall penetration. Hook expires after two seconds; switching/lost target/sight/dimension cancels it.
- Ranks I-IV: damage 1.25/1.5/1.75/2 times current weapon damage; burn 3/4/5/6 seconds; cooldown 40/35/30/25 seconds after completion/cancellation. Successful smash uses normal durability/material-hit handling.
- Reviewed build/lightweight/server checks passed all 78 required tests. Coverage includes all variants, real mob movement physics, max-rank damage/fire/wear, obstruction, failed-use rejection/no cooldown and switch/full-cooldown behavior.

## Strong-arm - completed

- Weapons: Molten Axe and all four gemstone variants. Aim at an unobstructed armored enemy within current melee range. One randomly chosen equipped armor piece breaks on a successful roll; no health damage is added.
- Rank I-IV chances: 30/55/80/100%; cooldowns: 60/50/40/30 seconds. Valid attempts start cooldown even on a failed roll; missing armored targets do not.
- Tests cover every variant, exactly one broken piece, both deterministic rank-I roll outcomes, armorless rejection and real packet cooldown handling. Review corrected a test corridor that inherited solid terrain; ability obstruction remains intact. Final build/lightweight/server checks passed all 81 required tests.

## Swing Attack - completed

- Weapons: all nine special axe families and all gemstone variants (45 axes). Learned-only instant circular strike deals current weapon attack damage to every visible enemy within current melee range, excluding allies and neutral entities. Blocks stop hits. Successful hits use ordinary weapon wear and learned material effects.
- Rank I-IV cooldowns: 30/25/20/15 seconds; damage and range continue to follow weapon attributes. Swinging empty space also starts the cooldown. Sixteen brief sweep particles show the circular range.
- Reviewed build/lightweight/server checks passed all 83 required tests, covering every variant, front and rear hits, exact damage/wear, fresh rejection, walls, range, neutral mobs, team allies, caster safety and instant cooldown/replay.

## Death Stare - completed

- Weapons: Riftfang Dagger and all four gemstone variants. Teleport beside the living, non-allied mob directly under the crosshair; yaw/pitch are preserved, fall distance reset. Includes airborne targets. Mounted use is rejected.
- Rank I-IV ranges: 30/60/120 blocks/any loaded visible distance; cooldowns: 45/40/35/30 seconds, starting immediately after successful teleport. Failed targeting/landing does not consume it.
- Uses loaded-entity iteration and chunk-path checks to avoid world-sized entity queries or loading unknown terrain. Solid obstacles block targeting; destination checks cover collision, world border, build height, loaded chunks and fluids.
- Reviewed build/lightweight/server checks passed all 86 required tests, including all five variants, fresh rejection, exact destination/view/fall behavior, walls, distant rank-IV use, upward targeting, mounted rejection and packet cooldown.

## Echoing Speed - completed

- Weapons: Riftfang Dagger and all four gemstone variants. Successful melee hits on one living enemy each add 5% attack speed, capped at 20/30/40/50% by rank. A successful hit on another enemy restarts at 5%; rejected hits leave buildup intact.
- Eight seconds without a successful hit, target death/removal/dimension change, weapon switching, death or logout clear the temporary modifier. No permanent attribute changes or removal of unrelated speed bonuses.
- Reviewed build/lightweight/server checks passed all 88 required tests, covering every variant, unlearned rejection, exact gradual buildup/caps, rejected hits, target reset, exact-stack switching, rank-IV expiry/restart and logout with an independent speed modifier.

## Distance Tax - completed

- Weapons: Claymore of the Void and all four gemstone variants. Successful melee damage marks one enemy; after ten seconds, the distance between wielder and target converts into 0.5/0.75/1/1.25 damage per block, capped at 40 at every rank.
- Repeated hits on the same enemy keep the original deadline. Hitting another replaces it. Switching/death/logout/invalid target cancels it. Sparse portal particles show the mark and payout; player-owned magic prevents recursive melee triggers.
- Reviewed build/lightweight/server checks passed all 91 required tests, covering all variants, exact deadline/damage, repeated-hit deadline preservation, one-time payout, rank conversion/cap, target replacement, switching, fresh rejection and logout. Survival test players require explicitly driven player-tick events for scheduled effects.

## Null Receipt - completed

- Weapons: Nullified Axe and all four gemstone variants. Active protection cancels knockback from the next five damaging hits, including absorption damage, without preventing damage itself. All impulses from one hit share one charge. Rejected hits do not spend charges; the sixth damaging hit is unprotected.
- Stores canceled force, capped at five impulse units. The next intrinsic vertical axe slam adds that force to its normal knockback, then consumes the receipt. Ordinary Swing Attack does not release it. State lasts up to thirty seconds; switching, dimension change, death/logout or expiry cancel it.
- Rank I-IV cooldowns: 40/35/30/25 seconds after release or cancellation. Melee/projectile and actual explosion knockback hooks are handled; explosion client impulse is also zeroed.
- Reviewed build/lightweight/server checks passed all 94 required tests, including every variant, fresh/rejected-hit checks, five-hit limit, unchanged damage, multiple impulses per hit, real vertical-slam release, independent circular swing, exact cooldown, real explosion suppression, switching and absorption/cap.

## Overwhelming Smash - completed

- Weapons: all eight marked non-Ignisium axe families and gemstone variants (40 axes). A successfully shield-blocked melee hit destroys the blocking shield; that blocked hit does not gain health damage.
- Adds exactly 20% armor durability wear with fractional carry per armor stack, so five one-point wear attempts gain one extra point. Preserves normal health mitigation. Mobs, whose armor normally lacks vanilla wear, receive only the bonus calculated from the normal vanilla damage/4 baseline.
- Fixed specified values use a single level; no redundant upgrades. Fresh weapons, rejected hits and unmarked Molten Axes are excluded. Weak armor-stack carry and tick-cleared damage-source context do not retain entities/equipment permanently.
- Reviewed build/lightweight/server checks passed all 96 required tests, covering all 40 axes with actual survival PvP shield blocks, fresh rejection, exact 20% wear, unchanged health damage, one-level cap, Molten exclusion and mob/rejected-hit wear. PvP is temporarily enabled and restored in the test fixture.

## False Awakening - completed

- Weapons: Nightmare Dagger and all four gemstone variants. Aim at an enemy within twenty blocks to mark its real silhouette and create a shadowless violet humanoid echo beside it. Sparse silhouette outlines alternate between real and echo; real rendering/shadow remains unchanged. The echo uses a matching head where possible.
- Visible hostile/aggro mobs within eight blocks briefly target the echo instead of the wielder. Previous valid targets restore afterward. Your next successful real-target melee hit deals 10/15/20/25% extra damage and ends the dream; rejected hits and echo hits do not consume it.
- Rank I-IV durations: 4/5/6/7 seconds; ending/cancellation cooldowns: 30/25/20/15 seconds. Only one echo per wielder, no save/loot/item interaction; switching, dimension change, death/logout, target loss/range or expiry remove it.
- Reviewed build/lightweight/server checks passed all 99 required tests: all variants, fresh rejection, AI distraction/restoration, equipment protection, one-time exact bonus, rejected hit, expiry/cooldown, wall rejection, switching, lethal hit and actual logout. Client appearance still needs in-game visual review.

## Delusion - completed

- All fifteen Somnium variants: arm the next successful melee hit on a mob for thirty seconds; it forgets aggression and cannot reacquire an attack target for twenty seconds, without freezing ordinary wandering. Exact-stack switching, death/logout and expiry cancel; cooldowns 40/35/30/25 seconds after ending. Player targets are not given forced AI behavior.
- Reviewed build and all 101 server tests passed, including all variants, fresh/rejected hits, retaliation memory, reacquisition, switching/cooldown and twenty-second expiry. Fixed the 1.21.1 goal-selector API and protected the expiry fixture against daylight burning.

## Delusion - completed

- All fifteen Somnium variants: next successful melee hit on a mob clears aggression and blocks reacquisition for twenty seconds. Thirty-second arming timeout; no forced player behavior or frozen wandering. Switching/death/logout cancel; cooldowns 40/35/30/25 seconds after ending.
- Reviewed build and all 101 server tests passed: all variants, fresh/rejected hits, retaliation memory, reacquisition, switching/cooldown and twenty-second expiry.

## Always the Gardeners - completed

- All fifteen Lush weapons. Next enemy kill within a sixty-second arming window leaves a vanilla-rendered azalea display and five-minute poison cloud. No terrain replacement or drops. One garden per wielder; poison affects everyone, including wielder, allies and neutral mobs, subject to vanilla effect immunity. Poison I lasts twenty seconds and refreshes while inside. Radius I-IV: 2/2.5/3/3.5 blocks.
- Switching retains placed entities until expiry; death/logout/travel removes them. Unused arming cancels on switching; cooldown after ending: 60/50/40/30 seconds. Entities never save as orphans. Reviewed build and all 103 server tests passed: all variants, fresh/rejected kills, caster/neutral poison, exact duration/radius, neutral-kill exclusion, switching/cooldown and expiry. Updated maxed-gear regression to exhaust actual available offers.

## Grave Vault - completed

- All fifteen Lush weapons, single level. Crouch-right-click opens a vanilla one-row chest interface with nine flower slots; enemy kills place a stored flower above the victim support block if possible, and normal terrain right-click places the first stored flower that can grow there. Each slot holds a normal stack. Item storage persists through vanilla stack save/load and augmentation; no cooldown or free intrinsic grant.
- Dedicated synchronized menu/screen locks the bound weapon against pickup, shift-click, hotbar swap and offhand swap. Server requires exact equipped stack/hotbar and learned passive. Client allows the synchronized replacement of that stack after storage edits. Normal flower survival, collision, permissions, Adventure restrictions and NeoForge placement cancellation apply.
- Reviewed/retested build and all 105 server tests passed: all variants, nine independent slots, persistence, flower-only input, extraction, weapon nesting prevention, number-key guard, switching, real valid/invalid-soil placement and Adventure exclusion. Client appearance has not been visually playtested.

## Echo Hearing - completed

- Replaces the original Echo Pin behavior on all fifteen Pulsite weapons. Activating the learned ability immediately gives Glowing to every loaded entity within a 32/40/48/56-block sphere, including the wielder, other players, mobs and nonliving entities. Walls do not block it. The glow lasts 10/12/14/16 seconds; the 30/25/20/15-second cooldown begins on use.
- The original augmentation ID remains `tooltips.forgermod.ability.echo_pin` so learned weapons and existing cooldowns remain compatible. Living entities receive the vanilla Glowing effect; nonliving entities receive a timed glowing tag, preserving tags that were already present and clearing owned tags on expiry or level exit. The former single-target loud-event listener and thirty-second pin state were removed.
- Reviewed and retested with NeoForge 21.1.93: all 194 required server game tests and the build pass. Tests cover all fifteen weapon variants, both players, mobs, dropped items, a target behind a wall, rank I and IV radius/duration boundaries, cooldowns, timed cleanup, and pre-existing glow.

## Warden Tone - completed

- All fifteen Pulsite weapons, single level. Suppresses owner-originated sculk game-event vibrations while the learned weapon is held, including owned projectile events. ENTITY_DAMAGE remains audible to vibration listeners; other entities/events remain untouched. No player invisibility, damage immunity, saved silent flag or global sound change.
- Uses NeoForge VanillaGameEvent before broadcast, so sensors and vibration AI never receive muted events. No retained state or cooldown; switching immediately stops suppression.
- Reviewed/retested build and all 109 server tests passed: every variant, unlearned/switch rejection, movement/action/block/projectile suppression, unrelated entity protection, damage exception and a real sculk sensor that ignores footsteps but activates from actual damage.

## Echo Salvage - completed

- Echoing Axe and four gemstone variants, following the rewritten marked description. Actual intrinsic vertical slam that hits solid terrain but misses living entities stores one echo for thirty seconds. Empty air and successful neutral hits cannot generate it. No stacking.
- Next vertical strike successfully damaging an enemy releases the echo across its eligible enemy hits: half the weapon attack damage when stored, capped at 2/4/6/8 by rank. Normal armor/absorption mitigation applies. Rejected hits, ordinary melee and circular Swing Attack do not spend it; neutral targets get neither bonus nor consumption. Switching, expiry, death/logout clear state.
- Reviewed/retested build and all 112 server tests passed: all five variants, fresh/air rejection, solid miss, cap/no stacking, rejected and ordinary hits, exact one-time real-slam bonus, circular-swing independence, neutral hit rules, damage below cap, switching and expiry.

## Flame Breath - completed

- Weapons: Ignisium dagger, Ignisium axe, Ignisium claymore; all gemstone variants.
- Instant 35-degree fire cone, current weapon damage, rank I-IV range 4/5/6/7 blocks, burn 3/4/5/6 seconds and cooldown 30/25/20/15 seconds. Blocks and allies stop hits; no terrain edits or recursive melee triggers.
- Tested, reviewed, retested and reviewed again; build and all 115 required server tests passed.

## Charred Edge - completed

- Weapons: Ignisium dagger; all gemstone variants.
- Rank-I passive adds exactly 15% melee damage against hostile enemies with Fire Resistance; fresh gear, neutral targets and secondary spell damage are excluded.
- Tested, reviewed, retested and reviewed again; build and all 116 required server tests passed.

## Magma Toss - completed

- Weapons: Ignisium axe; all gemstone variants.
- One real magma-cream rock projectile per wielder, bursts on first enemy or block for weapon damage; rank I-IV radius 2/2.5/3/3.5 blocks, burn 3/4/5/6 seconds and ending cooldown 35/30/25/20 seconds. Three-second lifetime; ignores neutral/allied entities; solid blocks stop blast damage. Switching/death/logout removes rock, no terrain edits.
- Tested, reviewed, retested and reviewed again; build and all 118 required server tests passed.

## Displace - completed

- Weapons: Inanisium dagger; all gemstone variants.
- Swaps the aimed visible enemy within ten blocks only when both complete bodies fit in loaded, dry destinations; mounted entities are rejected. Preserves facing, resets fall distance and momentum, and rolls back the target if player teleport fails. Rank I-IV cooldown 40/35/30/25 seconds; rejected uses consume no cooldown.
- Tested, reviewed, retested and reviewed again; build and all 119 required server tests passed.

## Backstab - completed

- Weapons: Inanisium dagger, Somnium dagger, Electrium dagger, Taifunite dagger, Vulnusium dagger, Lush dagger, Morsium dagger, Pulsite dagger; all gemstone variants.
- Rank-I passive adds exactly 20% melee damage from the rear, within 60 degrees of directly behind the target. Target yaw determines facing even when looking vertically. Front/side hits, unlearned weapons, allies and secondary damage receive no bonus.
- Tested, reviewed, retested and reviewed again; build and all 120 required server tests passed.

## Void Grip - completed

- Weapons: Inanisium dagger; all gemstone variants.
- Rank-I passive reduces melee knockback strength by exactly 25% without changing damage. Damage-source context is limited to the current server tick and exact held stack; spells, unrelated damage and later impulses do not inherit the reduction.
- Tested, reviewed, retested and reviewed again; build and all 121 required server tests passed.

## Void Pulse - completed

- Weapons: Inanisium dagger, claymore and axe; all gemstone variants.
- Damage-free radial velocity push; rank I-IV radius 4/5/6/7 blocks, strength 0.6/0.8/1/1.2 and cooldown 30/25/20/15 seconds. Visible mobs of every type are pushed, including passive/allied and knockback-resistant mobs; walls still block the push. Eligible PvP players retain the existing targeting rules. No teleportation or damage. A ten-tick inward particle charge precedes the fast outward burst and push; release or cancellation starts the cooldown.
- Tested, reviewed, retested and reviewed again; build and all 122 required server tests passed.

## Gravity Well - completed

- Weapons: Inanisium axe; all gemstone variants.
- Updated to the user clarification: a sustained damage-free physics pull of visible enemies within six blocks to a fixed point two blocks ahead of the caster at activation, followed by a weapon-damage smash only on arrival within melee reach. Rank I-IV strength 0.6/0.7/0.8/0.9 and ending cooldown 40/35/30/25 seconds. Two-second state, exact-stack switching/death/logout cancellation; collision-aware steering without teleportation; damage occurs only once on arrival. Turning or moving does not shift the captured destination; blocked pulls end without a remote smash.
- Tested, reviewed, retested and reviewed again; build and all 123 required server tests passed.

## Lonely Dream - completed

- Weapons: Somnium dagger; all gemstone variants.
- Rank-I passive grants 5% attack speed per enemy within ten blocks, with no invented enemy-count cap. Updates every half-second and immediately on equipment change; removes only its own transient modifier on switching, death or logout. Allies and neutral entities do not count.
- Tested, reviewed, retested and reviewed again; build and all 124 required server tests passed.

## Unfinished Dream - completed

- Weapons: Somnium dagger, Somnium claymore, Somnium axe; all gemstone variants.
- Rank-I passive heals exactly one HP on killing an enemy that has never damaged this wielder in the current session. Absorption-health hits count as damage. Tracks actual damage even before equipping; weak entity history is removed on victim death and player logout. Canceled deaths, neutral kills and unlearned weapons do not heal.
- Tested, reviewed, retested and reviewed again; build and all 125 required server tests passed.

## Just a Dream - completed

- Weapons: Somnium dagger, Somnium claymore, Somnium axe; all gemstone variants.
- Instant safe return using Minecraft 1.21.1 respawn resolution, including cross-dimension beds/anchors and vanilla world-spawn fallback. Loads destination body chunks, checks full collision/fluid/border/build-height safety, preserves anchor charges and clears fall distance/momentum. Mounted use rejected; rank I-IV cooldown 60/50/40/30 seconds.
- Tested, reviewed, retested and reviewed again; build and all 127 required server tests passed.

## Sleepwalker - completed

- Weapons: Somnium dagger; all gemstone variants.
- Rank-I passive grants exactly 10% more melee damage during world times 13000-22999 in skylit dimensions. Daytime storms do not masquerade as night; unlearned gear and other weapon types receive no bonus. Day/night boundary tests preserve the original world clock.
- Tested, reviewed, retested and reviewed again; build and all 128 required server tests passed.

- Gravity Well correction per user clarification: its physics pull deals no damage and targets a captured point two blocks ahead; only arrival at that point within melee reach triggers current weapon damage once and learned material effects.

## Lullaby - completed

- Weapons: Somnium claymore; all gemstone variants.
- Rank I-IV targets 10/12/14/16 blocks with wall-clipped aiming. Mobs sleep for 60 ticks, wake on successful damage including absorbed hits, and recover original AI/gravity/target settings on expiry or cancellation. Players instead receive Slowness II for 60 ticks. Cooldown 40/35/30/25 seconds starts after sleep ends. Concurrent sleepers cannot overwrite one another.
- Tested, reviewed, retested and reviewed again; build and all 130 required server tests passed.

## Dream Feast - completed

- Weapons: Somnium claymore; all gemstone variants.
- Restores exactly eight hunger points (four food icons), capped at 20, without adding saturation; refuses activation when hunger is full. Ranks reduce the instant cooldown from 60 to 50/40/30 seconds.
- Tested, reviewed, retested and reviewed again; build and all 131 required server tests passed.

## Daydreamer - completed

- Weapons: Somnium axe; all gemstone variants.
- Rank-I passive grants Jump Boost I only while the learned axe is held in the main hand during daylight (world times 0-12999 and 23000-23999 in skylit dimensions). Night, putting it away, death and logout remove the owned grant. Previous and newly acquired potion effects, including stronger Jump Boost and hidden effect chains, retain their remaining durations.
- Tested, reviewed, retested and reviewed again; build and all 132 required server tests passed.

## Pillow Armor - completed

- Weapons: Somnium axe; all gemstone variants.
- Rank-I passive multiplies fall damage by 0.75 while the learned axe is held in the main hand. Real damage tests confirm the exact reduction, fresh/offhand rejection and unchanged non-fall damage.
- Tested, reviewed, retested and reviewed again; build and all 133 required server tests passed.

## Magnetic Throw - completed

- Weapons: Electrium dagger, Electrium claymore, Electrium axe; all gemstone variants.
- All Electrium weapon types launch the wall-clipped aimed enemy within actual melee reach along the full look vector, including skyward aim. Rank I-IV velocity 1.1/1.3/1.5/1.7; instant cooldown 25/20/15/10 seconds. No added damage or teleportation; player targets receive synchronized motion.
- Tested, reviewed, retested and reviewed again; build and all 134 required server tests passed.

## Live Wire - completed

- Weapons: Electrium dagger; all gemstone variants.
- Ranks I-IV grant an eight-second reflection window. Successful enemy melee damage, including damage absorbed by hearts, reflects two indirect-magic damage without recursive reflection; lethal melee hits also earn reflection. Exact-stack/hotbar/dimension switching, expiry, death and logout clean up the state. Cooldown after ending: 40/35/30/25 seconds.
- Tested, reviewed, retested and reviewed again; build and all 136 required server tests passed.

## Quick Recharge - completed

- Weapons: Electrium dagger, Electrium claymore, Electrium axe; all gemstone variants.
- Rank-I passive uses the real charged-critical event followed by successful damage (including absorbed hits) to shorten only the currently assigned primary ability cooldown of the equipped variant by 20 ticks. Triggers at most once per 40 ticks, clamps readiness to the current tick, and preserves secondary and other variant entries. Failed, noncritical and uncharged hits cannot recharge; disabled cooldowns are unchanged.
- Tested, reviewed, retested and reviewed again; build and all 137 required server tests passed.

## Conductive Edge - completed

- Weapons: Electrium dagger, Electrium claymore, Electrium axe; all gemstone variants.
- Rank-I passive increases successful enemy melee damage by exactly 20 percent only while the target is in water or exposed to rain. Fresh weapons, dry or roof-sheltered enemies and spells receive no bonus. All 15 variants tested, with an actual Plains rain fixture and roof check.
- Tested, reviewed, retested and reviewed again; build and all 138 required server tests passed.

## Static blade - completed

- Weapons: Electrium dagger; all gemstone variants.
- Next swing (air, block or enemy) launches a server-authoritative four-block-wide, half-block-high echo along the view direction captured on that swing. Travels one block per tick up to 20 blocks; full-width collision sampling stops at solid blocks, while enemies are pierced once each for current weapon damage. Arms for 30 seconds; released flight has its own deadline. Exact-stack switching/death/logout cancels; ranks reduce ending cooldown to 40/35/30/25 seconds. Uses particles and compact state without a ticking projectile entity.
- Tested, reviewed, retested and reviewed again; build and all 139 required server tests passed.

## Lightning Rod - completed

- Weapons: Electrium dagger, Electrium claymore, Electrium axe; all gemstone variants.
- Rank-I passive prevents actual lightning strikes from injuring or igniting the wielder. Under open sky, an additional cosmetic strike rolls once per second with 1/60 chance, independent of storms. Strikes add exactly 10 absorption HP and Strength I for 30 seconds; repeated flashes cannot stack hearts. Owned absorption consumption is tracked so switching/expiry preserves unrelated absorption and Strength, including stronger potion chains. Cosmetic server bolts expire without changing copper, powering rods, igniting blocks or damaging nearby entities.
- Tested, reviewed, retested and reviewed again; build and all 140 required server tests passed.

## Alternating Current - completed

- Weapons: Electrium dagger, Electrium claymore, Electrium axe; all gemstone variants.
- Arms for 30 seconds. A successful melee hit links its living enemy to the nearest visible enemy within 20 blocks for one minute; the second is stunned for two ticks immediately, then the harmless stun alternates every 20 ticks. Walls block pulses; death, separation beyond 20 blocks, switching and logout cancel. Rank I-IV ending cooldown 120/100/80/60 seconds. Shared sleep/stun leases preserve original AI, gravity and targets through overlaps and dimension travel; Lullaby uses the same locks. Stunned players cannot start melee, active abilities, echo releases, dual attacks, vertical strikes or double jumps; camera direction is preserved.
- Tested, reviewed, retested and reviewed again; build and all 144 required server tests passed.

## Direct Current - completed

- Weapons: Electrium dagger, Electrium claymore, Electrium axe; all gemstone variants.
- Ranks I-IV allow 2/3/4/4 melee-marked enemies, each within 50 blocks of every other anchor. Two marks start 20-second stuns and visible barriers; additional marks refresh that duration. Crossing a barrier deals 4 HP with a 10-tick per-entity guard, including fast dashes. Rank IV is named Static Field in item tooltips and upgrade cards: three visible anchors form a triangular field dealing 1 HP per second to all living occupants, including neutral creatures and the caster; other players retain normal PvP protection. Walls block barrier/field formation. Exact-stack switching, death, range separation and expiry release stuns. Ending cooldown 120/100/80/60 seconds. Visibility is checked once per pair per tick and particle counts are bounded.
- Tested, reviewed, retested and reviewed again; build and all 147 required server tests passed.

## Stunning Presence - completed

- Weapons: Electrium claymore; all gemstone variants.
- Ranks I-IV provide a 30-second aura with 4/6/8/10-block radius, one HP per pulse and six-tick stuns; pulses repeat every 40/33/27/20 ticks. Visible enemies only, including cancellation on switching, death and logout; ending cooldown 60/50/40/30 seconds. Shared locks enforce position before and after entity ticks, block melee damage emitted by frozen attackers, avoid redundant motion packets and preserve client camera rotation with relative rotation flags.
- Tested, reviewed, retested and reviewed again; build and all 149 required server tests passed.

## Heavy Current - completed

- Weapons: Electrium axe; all gemstone variants.
- Rank-I passive multiplies enemy melee damage by 1.15 before normal armor reduction, only when all four normal armor slots contain correctly matched ArmorItem pieces. Missing pieces, Elytra, fresh weapons and spell damage receive no bonus. Actual damage-event probes verify all five axe variants.
- Tested, reviewed, retested and reviewed again; build and all 150 required server tests passed.

## Downdraft Bell - completed

- Weapons: Taifunite dagger, Taifunite claymore, Taifunite axe; all gemstone variants.
- All Taifunite weapon types force visible airborne enemies downward once within rank I-IV radius 4/6/8/10, preserving horizontal velocity, position, HP and existing fall distance. Minimum downward speed 0.8/1.0/1.2/1.4; already faster descents, grounded targets, swimming targets and neutral mobs are unchanged. Instant cooldown 30/25/20/15 seconds. A shared vertical-only client correction preserves actual sprint momentum and uses a native motion fallback for peers without that channel.
- Tested, reviewed, retested and reviewed again; build and all 151 required server tests passed.

## Crosswind - completed

- Weapons: Taifunite dagger, Taifunite claymore, Taifunite axe; all gemstone variants.
- All Taifunite weapon types grant two minutes of projectile deflection. Before entity ticks, incoming projectiles within 12 blocks are steered sideways at their existing speed when their predicted path threatens the player; own shots are excluded. Impact-event fallback blocks last-moment and guided hits. Switching, dimension changes, death and logout end protection; already deflected flight keeps its physical momentum. Rank I-IV ending cooldown 180/160/140/120 seconds. Real arrow flight, owner filtering and cancellation tested.
- Tested, reviewed, retested and reviewed again; build and all 153 required server tests passed.

## Slipstream - completed

- Weapons: Taifunite dagger; all gemstone variants.
- Rank-I passive grants a non-stacking 10 percent base movement-speed bonus while actually sprinting directly behind a moving ally within three blocks and clear sight. Position observations handle real player movement rather than relying on stale server velocity. Stopping, overtaking, turning away, blocked sight, switching, death and logout remove the owned modifier. Allied teams, owned pets and cooperative players are recognized.
- Tested, reviewed, retested and reviewed again; build and all 154 required server tests passed.

## Upwind - completed

- Weapons: Taifunite dagger, Taifunite claymore, Taifunite axe; all gemstone variants.
- Arms the next successful melee hit for up to 30 seconds. After vanilla hit knockback, adds rank-scaled horizontal knockback and upward velocity 0.6/0.8/1.0/1.2, respecting knockback resistance. No added damage, teleportation or artificial fall damage. Misses, blocks and ability damage do not consume it. Exact stack switching cancels it. Cooldown 30/25/20/15 seconds after hit or cancellation.
- Tested, reviewed, retested and reviewed again; build and all 155 required server tests passed.

## Windcharge - completed

- Weapons: Taifunite dagger, Taifunite claymore, Taifunite axe; all gemstone variants.
- Vertical velocity launch preserves horizontal sprint motion: dagger/claymore heights 5/10/15/20 blocks, axes 10/16/23/30 (unobstructed normal gravity). Axe rank IV is titled Skybreaker and arms one falling melee or ground smash, radius 2, current weapon damage plus vanilla mace fall scaling; initial melee victim is not hit twice. Successful smash prevents its landing damage; lesser ranks retain normal fall rules. Flight cancels on switching, fluids, climbing, death or 10-second timeout. Cooldown 30/25/20/15 seconds, instant on ordinary launch and after Skybreaker flight ends.
- Tested, reviewed, retested and reviewed again; build and all 156 required server tests passed.

## Vacuum Burst - completed

- Weapons: Taifunite dagger, Taifunite claymore, Taifunite axe; all gemstone variants.
- One velocity gust pulls visible dropped items and experience orbs within 6/8/10/12 blocks toward the player at speed 0.9. No teleportation, immediate collection, pickup-delay changes or item consumption; normal ownership and pickup rules still apply. Solid blocks stop the pull. Cooldown 30/25/20/15 seconds starts on use.
- Tested, reviewed, retested and reviewed again; build and all 157 required server tests passed.

## Headwind - completed

- Weapons: Taifunite dagger, Taifunite claymore, Taifunite axe; all gemstone variants.
- Learned passive halves only the ground-sprinting hunger exhaustion branch while the weapon is in the main hand. Jumping, swimming, generic exhaustion and offhand equipment retain vanilla costs. A required version-specific ServerPlayer mixin targets exactly the fourth exhaustion call in Minecraft 1.21.1 checkMovementStatistics; actual sprint, jump and swimming paths verified on NeoForge 21.1.93. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 158 required server tests passed.

## Feather Shelter - completed

- Weapons: Taifunite claymore, Taifunite axe; all gemstone variants.
- On use, gives the wielder and visible allied living entities Slow Falling for 10 seconds, with 4/6/8/10-block radius. Allies include teams, owned pets and cooperative players when PvP is disabled; corrected this shared ally check for Slipstream too. Enemies and targets behind walls are excluded. Vanilla effect merging preserves stronger pre-existing effects. Cooldown 60/50/40/30 seconds starts on use.
- Tested, reviewed, retested and reviewed again; build and all 159 required server tests passed.

## Pocket Storm - completed

- Weapons: Taifunite axe; all gemstone variants.
- Arms the next successful melee hit for up to 30 seconds. After normal hit processing, smashes enemies within 3 blocks of the victim: secondary enemies take current weapon damage; the original victim gets knockback without double damage. Horizontal knockback strength 1.0/1.2/1.4/1.6 with a small lift respects resistance. Lethal hits still trigger the smash; walls, allies and the wielder are excluded. No terrain changes. Cooldown 40/35/30/25 seconds after hit or cancellation. Also fixed Upwind to consume its armed gust on lethal hits.
- Tested, reviewed, retested and reviewed again; build and all 160 required server tests passed.

## Blood Rush - completed

- Weapons: Vulnusium dagger, Vulnusium claymore; all gemstone variants.
- Spends exactly 2 real health points, preserving absorption, and grants Speed II for 10 seconds. Activation is refused at 2 HP or less even with absorption available; refused uses spend nothing and grant no effect. Vanilla potion merging preserves stronger effects. Cooldown 30/25/20/15 seconds starts on use.
- Tested, reviewed, retested and reviewed again; build and all 161 required server tests passed.

## Final strike - completed

- Weapons: Vulnusium dagger, Vulnusium claymore, Vulnusium axe; all gemstone variants.
- Chosen fixed bonus: 50% extra melee damage to the most recently successfully hit enemy while it has strictly less than 10% maximum health. The first hit against a new enemy is not boosted; hitting another enemy replaces the target. Blocked hits and ability damage do not establish the melee target. Exact stack/slot switching, dimension changes, death and logout clear it. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 162 required server tests passed.

## Blood Sacrafice - completed

- Weapons: Vulnusium dagger, Vulnusium claymore, Vulnusium axe; all gemstone variants.
- On activation halves real health and absorption independently, then doubles outgoing attributed weapon damage to non-allied targets for 20 seconds, including melee and weapon ability damage. Reusing the active stance spends no further health. Exact stack/slot switching, dimension change, expiry, death or logout ends it; no health refund. Cooldown 60/50/40/30 seconds begins when the stance ends. Fixed cost and duration; ranks improve cooldown.
- Tested, reviewed, retested and reviewed again; build and all 163 required server tests passed.

## Scar Compass - completed

- Weapons: Vulnusium dagger, Vulnusium claymore, Vulnusium axe; all gemstone variants.
- Successful melee hits mark the most recent living enemy with a 10-minute Glowing effect; a new enemy replaces the old mark. Shared ownership prevents one wielder removing another mark. Existing and externally applied Glowing effects retain their own timers and are restored when the last mark ends. Dispelled potions are not resurrected. Switching exact stack/slot, expiry, dimension travel, death, unloading or logout cleans ownership. No extra ticking entities; fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 164 required server tests passed.

## Revengeful Exit - completed

- Weapons: Vulnusium dagger, Vulnusium claymore, Vulnusium axe; all gemstone variants.
- Dying to a living enemy releases a five-block blood explosion for current weapon damage to nearby living entities, including neutral creatures, with walls blocking the area blast. The killer takes an additional five times weapon damage, even outside that radius; an in-radius killer takes six times in one hit so immunity frames cannot erase part of the effect. Normal PvP and armor rules apply. Environmental deaths and unlearned weapons do not trigger it. Blood particles and explosion sound without terrain damage. A recursion guard prevents death-burst chains. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 166 required server tests passed.

## Sunfed - completed

- Weapons: Lush dagger, Lush claymore, Lush axe; all gemstone variants.
- Learned main-hand passive restores exactly one hunger point after every 15 continuous seconds outdoors in Minecraft daylight, for all marked Lush weapon types. Uses the overhead motion-blocking heightmap rather than asynchronously updated skylight, and Minecraft isDay so darkness during thunderstorms does not qualify. No saturation, capped at 20 food. Roofs, night, full food, switching exact stack/slot, dimension changes, death or logout reset the interval. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 167 required server tests passed.

## photosythesis - completed

- Weapons: Lush dagger, Lush claymore, Lush axe; all gemstone variants.
- Implemented the approved photosythesis entry as Photosynthesis in the UI. Chosen slow repair rate: one durability point after every 15 continuous seconds holding the learned weapon during Minecraft daylight, including indoors. Only the main-hand weapon repairs; damage cannot go below zero and augmentation/item data is preserved. Night, full durability, exact stack/slot switching, dimension changes, death and logout reset the interval. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 168 required server tests passed.

## Green Feast - completed

- Weapons: Lush dagger, Lush claymore, Lush axe; all gemstone variants.
- Consumes exactly one wheat from ordinary inventory or offhand to heal 2 health points, capped at maximum health. Full health and missing wheat reject activation without spending ingredients or starting cooldown. Inventory changes synchronize normally; weapon data is preserved. Cooldown 20/15/10/5 seconds starts on use.
- Tested, reviewed, retested and reviewed again; build and all 169 required server tests passed.

## Sapling Harvest - completed

- Weapons: Lush dagger, Lush claymore, Lush axe; all gemstone variants.
- Enemy kills while the learned weapon is held have exactly a 15% chance to add one random sapling to existing drops. Uses the actual saplings item tag, including applicable tagged mod/datapack items. Existing drops are preserved; canceled drops and disabled mob loot add nothing. Ordinary pickup rules apply. Both deterministic roll outcomes, all variants, loot preservation and rule/cancellation behavior tested. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 170 required server tests passed.

## Compost - completed

- Weapons: Lush dagger, Lush claymore, Lush axe; all gemstone variants.
- Prevents only Hunger from consuming rotten flesh while the learned weapon is held in either hand, allowing a main-hand food with an offhand axe without changing axe offhand-use restrictions. A required 1.21.1 food-completion mixin filters that food effect before application. Nutrition, saturation, consumption, other food effects, pre-existing Hunger and Hunger from other sources are preserved. Actual item finishUsingItem paths tested for all variants and both hands. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 171 required server tests passed.

## Lumberjack - completed

- Weapons: Lush axe; all gemstone variants.
- Learned Verdant Axes mine blocks in the logs tag without durability loss, including stripped logs and nether stems. The normal tool-use/statistic result is retained. Other block mining and melee combat still cost normal durability, and unaugmented axes receive no benefit. Tested every axe variant and logs at the final durability point. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 172 required server tests passed.

## Ghostwalk - completed

- Weapons: Morsium dagger, Morsium claymore, Morsium axe; all gemstone variants.
- Grants vanilla Invisibility for ten seconds; worn armor remains visible. Original IV transforms to Jumpscare Horror I; super I-IV invisibility lasts 10/12/14/16 seconds and the opening entity attack doubles incoming damage once. Valid entity attacks, block attacks, air swings and dagger offhand swings end it, including misses. Existing and independently applied invisibility retain their own durations and are restored when the owned effect ends; dispelling does not resurrect removed potions. Natural potion expiry is distinguished from dispelling, including expiry-order edge cases. Exact stack/slot switching, dimension travel, death or logout cancels it. Cooldown 30/25/20/15 seconds begins when Ghostwalk ends. Reuses the validated swing packet; no ticking marker entities.
- Tested, reviewed, retested and reviewed again; build and all 174 required server tests passed.

## Soul Hunger - completed

- Weapons: Morsium dagger, Morsium claymore, Morsium axe; all gemstone variants.
- Each valid enemy kill adds two absorption health points, up to six owned by this passive. A separate transient capacity and consumed-health ledger keep the cap independent of existing absorption. Absorbed damage spends the owned portion first; later kills replenish it. Absorption potions and refreshes coexist without overwriting the passive or being removed on switching. The remaining owned health and capacity clear on exact stack/slot switching, dimension travel, death or logout. Canceled deaths and fresh weapons grant nothing. No artificial expiry; hearts last until consumed or the weapon is put away. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 175 required server tests passed.

## Last Stand - completed

- Weapons: Morsium dagger, Morsium claymore, Morsium axe; all gemstone variants.
- Learned passive prevents one fatal hit above one HP, leaving exactly one real HP after normal absorption consumption. A persistent player-wide two-minute cooldown survives respawn and prevents variant swapping from bypassing it. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 176 required server tests passed.

## Bone Guard - completed

- Weapons: Morsium claymore, Morsium axe; all gemstone variants.
- Summons one friendly vanilla-rendered skeleton for 60 seconds, with ordinary skeleton bow damage and rank-scaled 8/10/12/14-block targeting range. It shoots only visible enemies; arrows cannot hurt allies or be collected. Summon, gear and arrows cannot persist or yield loot/experience. Weapon switching retains summons; lifecycle cleanup, expiry, death and logout remove the summon and its shots. Ending cooldown 60/50/40/30 seconds. Original IV becomes Necromancer I, with 2/3/5/7 allies at super I-IV; max composition is bogged1, stray1, skeleton2, wither skeleton3. Friendly golems remain mutually neutral; all groups last one minute.
- Updated implementation passed two reviewed full builds with all 244 server tests, including all types, super ranks, arrow safety and persistence/expiry.

## Skull Throw - completed

- Weapons: Morsium axe; all gemstone variants.
- Fires a slow blue wither skull, using vanilla dangerous-skull flight. First enemy or block impact deals current weapon damage to visible enemies within 2/2.5/3/3.5 blocks and applies Wither I for five seconds. No terrain damage, vanilla skull healing or extra vanilla impact damage. Caster and allies are excluded. Flight expires after five seconds or switching/death/logout; ending cooldown 50/40/30/20 seconds.
- Tested, reviewed, retested and reviewed again; build and all 180 required server tests passed.

## Death's Due - completed

- Weapons: Morsium dagger, Morsium claymore, Morsium axe; all gemstone variants.
- Learned passive increases the existing enemy XP award by 25 percent, rounding down to whole points and saturating safely at the integer limit. Item drops, neutral creatures, canceled awards and uncredited kills are unchanged. Tested all fifteen variants and actual zombie death/orb creation (five XP becomes six). Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 182 required server tests passed.

## Echo Appetite - completed

- Weapons: Pulsite dagger, Pulsite claymore, Pulsite axe; all gemstone variants.
- Completed sculk mining restores exactly one food point (half a drumstick) without saturation for all Pulsite variants. Includes sculk veins, both sensors, catalysts and shriekers; other blocks do not feed. Normal tool wear and block drops remain intact, and a final-durability break still earns its food point. A credited Warden kill while holding a learned Pulsite weapon restores four food points (two drumsticks), capped at full hunger, without saturation. Fixed strength, maximum rank I.
- The Warden-kill extension was reviewed and tested on all fifteen Pulsite variants, with canceled and uncredited deaths excluded and a real Warden death confirming the hunger cap and unchanged saturation. After repairing an unrelated large-radius Echo Hearing test fixture, the build and all 197 required server tests passed.

## Resonance Crit - completed

- Weapons: Pulsite dagger, Pulsite claymore; all gemstone variants.
- Fully charged melee hits against visible-class enemy targets rising in a jump become genuine critical hits, including grounded attackers. Requires airborne positive vertical movement; grounded, falling, swimming, lava, climbing and elytra targets do not qualify. Uses at least the vanilla 1.5 damage multiplier without lowering stronger critical modifiers. Runs before critical-hit listeners so existing charged-critical abilities see the critical. Tested real charged attacks across ten variants. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 185 required server tests passed.

## Aftershock - completed

- Weapons: Pulsite dagger, Pulsite claymore, Pulsite axe; all gemstone variants.
- Successful fully charged melee hits, including normal sweep victims, schedule one damage point after 20 ticks. A dedicated armor-bypassing damage type bypasses immunity frames without resetting them and cannot trigger melee-hit abilities or another echo. Normal absorption, resistance and player protection still apply. Exact stack/slot switching, dimension changes, death and logout cancel pending echoes; dead enemies are skipped. Tested all fifteen variants, actual timing, recent immunity, no recursion and invalid-hit/switch cases. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 187 required server tests passed.

## Ringing Steel - completed

- Weapons: Pulsite claymore; all gemstone variants.
- Arms one successful enemy melee hit for up to 30 seconds. Damages one held sword, axe, bow, crossbow, trident or mace by 15 durability using normal enchantment rules, checking the offhand if the main hand has no weapon. Armor, shields and nonweapon tools are excluded; a hit without a weapon still consumes the strike. Spell/blocked hits do not consume it. Switching, expiry, dimension changes, death and logout cancel it. Ending cooldown 30/25/20/15 seconds; ranks improve cooldown only. Tested five variants, weapon breakage, offhand fallback and excluded equipment.
- Tested, reviewed, retested and reviewed again; build and all 188 required server tests passed.

## Stone Song - completed

- Weapons: Pulsite claymore; all gemstone variants.
- Learned main-hand blade grants exactly two additional armor points only below Y=0. Uses one synchronized transient modifier, so repeated ticks cannot stack it and it is not saved permanently. Unequipping, moving to Y=0 or above, death, logout and dimension travel remove it. Offhand blades and fresh weapons grant no armor. Tested all five variants and the fractional negative-Y boundary. Fixed strength, maximum rank I.
- Tested, reviewed, retested and reviewed again; build and all 189 required server tests passed.

## Challenge Roar - completed

- Weapons: Pulsite dagger, Pulsite claymore, Pulsite axe; all gemstone variants.
- An instant visible roar makes hostile mobs within 16/20/24/28 blocks target the wielder, including those behind walls. Supports ordinary target AI, brain attack/anger memories and wardens; players, allies, passive and neutral creatures are excluded. A taunt applied during a stun is retained when the stun releases without canceling immobilization. Normal AI handles later aggression; brain anger memories last thirty seconds. No damage or terrain changes. Instant cooldown 30/25/20/15 seconds. Tests cover all fifteen variants, wardens, piglins, walls, rank I and IV range, pets, neutral mobs and stun overlap.
- Revised range and hostile-only targeting tested and reviewed; the build and all 195 required server tests passed, including the new rank IV boundary test.
