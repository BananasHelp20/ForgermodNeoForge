# Marked ability implementation record

Only entries marked for implementation in abilityRecommendations.md are approved. Repeated entries share one implementation across their listed weapon families and gemstone variants. Abilities are learned through augmentation; each weapon retains two active and two unique passive slots, plus Material Hit separately. Missing balance values below were chosen with the user's permission.

## Quench Point — completed

- Weapons: Emberfang Dagger, including Ruby, Amber, Amethyst and Jade.
- Successful melee hits extinguish a burning victim. Up to ten seconds of remaining fire become a stored bonus for the next successful hit within ten seconds.
- Ranks I–IV convert each second into 0.5, 0.75, 1 and 1.25 damage respectively (caps 5, 7.5, 10 and 12.5).
- A hit can spend a previous charge and store fire from its new victim; unsuccessful or blocked hits do not spend it. Switching, death, logout and expiry clear stored fire.
- Reviewed twice. Build/lightweight checks and 60 required server tests pass, including all five variants, one-time use, rejected hits, switch cleanup, cap and expiry.

## Remaining marked abilities — pending

Null Receipt; Overwhelming Smash; False Awakening; Delusion; Lonely Dream; Unfinished Dream; Just a Dream; Sleepwalker; Static Blade; Lightning Rod; Alternating Current; Direct Current / Static Field; Stunning Presence; Downdraft Bell; Crosswind; Slipstream; Upwind; Windcharge / Skybreaker; Pocket Storm; Final Strike; Blood Sacrifice; Scar Compass; Revengeful Exit.

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

Heating Up's corrected rank-IV behavior: extinguish the wielder's fire to gain heat while capacity remains; at full capacity the wielder stays burning.

## Heating Up - completed

- Weapons: Infernal Claymore and all four gemstone variants.
- Store half of actual melee damage taken, capped at 20 heat. Spend five heat per successful attack to ignite all affected targets, including sweeps, for 3/4/5/6 seconds at ranks I-IV.
- Rank IV follows the corrected design: remaining fire on the wielder becomes heat (one heat per burn second) and is extinguished if capacity remains. Full capacity leaves the wielder burning. Heat clears on switching/death/logout.
- Ranged damage does not fill heat; canceled/failed hits do not spend it. Sweeping targets share one payment, while distinct same-tick attacks pay separately.
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
