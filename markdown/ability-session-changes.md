# Ability changes from this review session

Written on 24 October 2026. Covers the ability review and implementation completed in this conversation through 23 October 2026, ending before the review of **43: Magma Toss**.

This is a summary of the implemented gameplay changes, not just the description edits. It distinguishes changed mechanics from abilities whose existing behavior was only described more clearly. The implementation was pushed through `3aa7a3e`; the final gameplay commit was `1ed06ce`.

## Reading the values

- Progressions list levels in order, starting at I. Super-form levels restart at I.
- Damage and health values are HP unless explicitly given as hearts; 2 HP equals 1 heart.
- Cooldowns and durations are in seconds unless stated otherwise. Distances are in blocks.
- Values below are the normal progression. Existing gemstone modifiers still apply unless a specific change says otherwise.
- An extra template level is not automatically a named super version. Explicit hard caps also apply to templates and older over-levelled weapons.

## Lightning abilities

### Area Discharge and Anger of Zeus

Area Discharge retains its five-block, half-weapon-damage lightning attack. Its I–IV cooldowns are now **180 / 130 / 80 / 30 seconds**.

Added **Anger of Zeus I–IV** after Area Discharge IV:

| Super level | Waves | Initial range | Cooldown |
| --- | --- | --- | --- |
| I | 1 | 10 | 300 s |
| II | 2 | 20 | 260 s |
| III | 3 | 30 | 220 s |
| IV | 4 | 40 | 180 s |

- Initial strikes deal twice weapon damage to hostile and aggroed neutral creatures.
- All initial and chain targets are selected and marked immediately on activation. A target is selected only once per cast.
- Waves strike 500 ms apart. A wave without targets is skipped.
- Each initial strike has a 50% chance of starting a chain, with up to five additional jumps.
- Each jump searches within ten blocks of the previous target, waits 200 ms and deals half weapon damage.
- Chains may extend beyond the initial wave radius, including beyond 40 blocks.
- Cooldown starts on activation. Switching weapons cancels remaining queued strikes.
- Lightning preserves player kill credit and Charge Attack interaction without damaging terrain.

### Charge Attack and Lightning Rod

| Level | Charging hits | Maximum damage bonus | Following discharge hit | Lightning damage |
| --- | --- | --- | --- | --- |
| I | 20 | +100% | 21 | 20 |
| II | 18 | +133.33% | 19 | 23 |
| III | 17 | +166.67% | 18 | 27 |
| IV | 15 | +200% | 16 | 30 |

The discharge happens on the hit after charging finishes: **hit 16 at maximum level**, superseding the earlier hit-21 proposal. Successful killing hits also build charge. At maximum rank, having Lightning Rod adds the self-strike needed to trigger it, including indoors or without open sky.

### Direct Current and Static Field

Direct Current now marks enemies by **aiming at them and pressing the ability activation key**, rather than by melee hits.

- Aiming range is 50 blocks; the existing 50-block connection limit is retained.
- Base I–III can mark 2 / 3 / 4 enemies.
- Marked enemies are connected in selection order, with consecutive links only—not every possible diagonal.
- Base links deal 4 damage every 0.5 seconds to enemies touching or crossing them.
- Marked anchors, the caster and nonenemies do not take the connection damage.

Static Field uses the same marking system. With more than two marked enemies, it fills the horizontal area enclosed by the anchors, independent of selection order. With two anchors, it remains a connection.

- Super I–IV field damage is 1 / 1.25 / 1.5 / 1.75 per one-second pulse.
- Each pulse stuns affected enemies for 0.5 seconds; it is not an uninterrupted stun.
- The shared 20-second lifetime starts when the first connection forms between two enemies. Marking more enemies does not restart it.
- Marked enemies remain immune to their own field. Existing line-of-sight blocking and anchor immobilization are retained.

## Health, food and kill-triggered buffs

### Blood Sacrafice

- Costs half the player's combined **current health plus absorption**, spending absorption first.
- Example: 20 HP + 5 absorption HP becomes 12.5 HP + 0 absorption HP.
- Grants double weapon damage for 15 / 20 / 25 / 30 seconds, or 35 seconds at the extra template level V.
- Cooldown is a constant **90 seconds** across levels. Higher levels increase duration, not the cost or cooldown efficiency.

### Dream Feast

Now restores both hunger and saturation through a hard maximum of V:

| Level | Hunger points | Displayed hunger bars | Saturation points | Cooldown |
| --- | --- | --- | --- | --- |
| I | 2 | 1 | 2 | 120 s |
| II | 4 | 2 | 4 | 100 s |
| III | 6 | 3 | 6 | 75 s |
| IV | 8 | 4 | 8 | 50 s |
| V | 10 | 5 | 10 | 30 s |

### Green Feast

Consumes one wheat, heals 2 HP and restores matching hunger and saturation: 2 / 4 / 6 / 8 points, or 10 at template V. It can activate when hunger is missing even if health is full. Existing cooldowns are retained.

### Lucid Dreaming

The next-kill Speed effect and 50% fall-damage reduction now share the same 30-second duration, or 31 seconds with Jade. Fall protection no longer uses a separate lifetime.

## Summons and decoys

### Bone Guard

- Summons now last **ten minutes**, rather than one minute.
- Cooldown starts on activation. Reusing the ability when ready removes the old group and spawns its replacement beside the player.
- Switching weapons does not despawn the group. Expiry, owner death, logout and dimension travel clean it up, including its tracked projectiles.
- Base rosters remain I: 1 Skeleton; II: 2 Skeletons; III: 1 Skeleton and 1 Bogged.

### Necromancer

Replaced the old fixed super rosters and free armor/enchanted weapons with a customizable reinforcement roster.

- Capacity is **4 / 6 / 8 / 10** at super I–IV.
- Shift+Alt opens the editor while holding the Morsium weapon. It opens on modifier release so Shift+Alt+S still rotates abilities.
- New loadouts start with Skeletons. Upgrading retains the selected roster; the player can spend the new capacity in the editor.
- Editing is allowed only when the group has no living reinforcements.

| Capacity cost | Reinforcements |
| --- | --- |
| 1 | Skeleton, Zombie, Zombie Villager |
| 2 | Husk, Drowned, Stray, Bogged, Zombified Piglin, Phantom |
| 3 | Wither Skeleton; Skeleton Horse **including its Skeleton rider** |
| 4 | Zoglin |

Bosses, including the Wither, are excluded. Zombie Horse is excluded.

From **Necromancer II**, each selected reinforcement has a real weapon slot and four armor slots. Mounted entries equip the rider.

- Supplied equipment retains its durability and enchantments between deaths and resummons; it is not recreated at full durability.
- Armor takes player-style durability damage. Supplied melee/ranged weapons wear and use enchantment hooks, including native enchanted crossbows and contact-XP Mending. Temporary thrown tridents cannot be collected to duplicate gear.
- Without supplied equipment, the reinforcement uses its default mob equipment.
- Removing or replacing an entry returns all its supplied equipment to inventory, or drops it at the player's feet if inventory is full. Breaking the controlling weapon also returns the banked gear.
- A persistent server-side equipment bank prevents weapon copies from duplicating the supplied items. A living group locks that bank against editing or simultaneous use by another wielder.
- Summons follow the owner when idle, retain friendly targeting/retaliation and cannot be farmed for loot, equipment or XP. Conversion and inventory-extraction interactions are blocked.
- Bone Guard's ten-minute lifetime and ready-recast replacement also apply to Necromancer.

### Cinder Decoy

- Its damage budget snapshots the player's **current health at creation**, excluding absorption—not maximum health or later health changes.
- Incoming damage accumulates against that budget. Touching an enemy or taking a small hit no longer detonates it immediately.
- Lifetime is ten minutes. Death, expiry and a ready recast trigger the fire explosion; cancellation cleanup is one-shot.
- Cooldown starts on activation, allowing a ready recast to explode and replace the old decoy without waiting for it to expire.
- Visible hostile mobs in its attraction range are drawn to it.

### False Awakening

The bonus against the marked enemy now works repeatedly while that enemy is distracted by the decoy, for the session's duration. A nonlethal hit no longer consumes it. No distraction means no bonus; a lethal hit still removes the echo.

## Taunts, suppression and detection

### Challenge Roar and Intimidating Roar

Challenge Roar now locks initially taunted hostile mobs onto the caster throughout its Strength window. It also prevents them from damaging other targets during that lock, including mobs using brain-based targeting. Delusion's suppression takes precedence; the taunt can resume afterward while still active.

Added **Intimidating Roar I–IV** after Challenge Roar IV:

| Super level | Range | Strength duration | Cooldown |
| --- | --- | --- | --- |
| I | 35 | 20 s | 60 s |
| II | 40 | 24 s | 60 s |
| III | 45 | 27 s | 60 s |
| IV | 50 | 30 s | 60 s |

It adds a moving Weakness I and Slowness I aura while Strength is active, affecting enemies entering range. Owned effects clear when enemies leave or the ability ends, while independent stronger/longer effects are preserved. Existing base Roar values remain unchanged.

### Deep Wound

Hard maximum is now III, including templates and older over-cap stacks. Cooldowns are **120 / 75 / 30 seconds**. The next-critical double-damage and Slowness behavior was retained; the normal maximum Slowness is III.

### Delusion

Now progresses through V. Suppression durations are **10 / 14 / 18 / 22 / 25 seconds**, with **120 / 100 / 80 / 60 / 40-second** cooldowns. During suppression, the mob cannot target anything.

### Echo Hearing

Private post-sound outline duration now scales through I–IV as **1 / 1.35 / 1.65 / 2 seconds**, with another second for Jade. Outlines are private to the listener; other viewers and independent glowing effects are not overwritten.

## Movement, pulling and area control

### Crucible Hook and Crucible Grappler

Base Hook's aimed mob pull, automatic smash, rope and cooldowns are retained. Added **Crucible Grappler I–IV** after Hook IV:

- Aim at a block within 25 / 30 / 35 / 40 blocks to grapple toward it.
- Pulling accelerates rather than teleporting the player. Releasing preserves velocity.
- Right-clicking, switching weapons or losing a valid anchor disconnects the grapple.
- Added **auto-release on arrival**, enabled by default and configurable per player, including multiplayer.
- Super mob hits deal 2.25 / 2.5 / 2.75 / 3 times weapon damage and ignite for 7 / 8 / 9 / 10 seconds. Super cooldown is 25 seconds.

### Displace

Below IV, swaps work with any visible living entity within ten blocks, including passive and allied mobs. At IV and above, any visible loaded entity is valid—including items and projectiles—with no fixed distance limit. Visibility, loaded-world and safe destination checks still apply.

### Gravity Swing — formerly Ender Swing

Renamed the ability while retaining its saved ID. It now has a hard maximum of III:

| Level | Swing radius | Lift height | Extra gravity and fall-damage multiplier | Cooldown |
| --- | --- | --- | --- | --- |
| I | 2 | 10 | +50% | 90 s |
| II | 3.5 | 15 | +100% | 75 s |
| III | 5 | 20 | +150% | 60 s |

Modifiers last until landing, replace rather than stack, and clean up on fluid/climbing/unload. Ceiling safety remains, as does the existing higher-rank 1.2-times direct hit.

### Gravity Well

Now pulls **one aimed enemy within six blocks** and automatically performs a normal 3×3 Vertical Strike on arrival, rather than finishing with one ordinary hit. It reuses the strike's animation, area damage and passive interactions; separately learning Vertical Strike is not required for this ability-supplied attack.

### Eye of the Storm

Now progresses through V:

- Radius: **5 / 7 / 9 / 10.5 / 12 blocks**.
- Storm and enemy Levitation duration: **5 / 9 / 13 / 16 / 20 seconds**, plus one second with Jade.
- Captured enemies remain inside the moving storm instead of being left behind as the player moves.
- Captured riders dismount so riding cannot bypass confinement. Allies, passive mobs and nonliving entities are not captured.
- Cleanup removes owned Levitation while preserving independent effects. The existing 60-second cooldown is retained.

### Just a Dream

Hard maximum is now II, including templates. Cooldowns are **60 / 30 seconds**. Cross-dimensional return to the respawn point, with world-spawn fallback, is retained and clarified in the description.

## Fire and invisibility

### Explosion and Blazing Blast

Added **Blazing Blast I–IV** after Explosion IV. It retains the initial explosion and Resistance, then fires:

- Four ghast fireballs at the four cardinal headings relative to the player's facing.
- Eight blaze fireballs between those headings, at 22.5° + 45° increments.

Projectile damage is **100 / 115 / 130 / 145%** of vanilla damage. Super cooldowns are **120 / 110 / 100 / 90 seconds**. Same-burst projectiles cannot collide with or redirect each other; they retain owner attribution and expire after 200 active ticks.

Added the server-controlled **Explosion terrain-damage setting**, disabled by default. It controls block destruction and terrain ignition for both the initial Explosion and Blazing Blast projectiles. Base Explosion's other effects and cooldown progression are unchanged.

### Flameborn

The fire burst now deals **50% weapon damage at I**, rising to **150% at extra template II**. It damages nearby noncaster entities and ignites all nearby entities within ten blocks, including allies and neutral mobs. The caster keeps Fire Resistance; terrain is untouched. Existing durations and cooldowns are retained.

### Ghostwalk and Jumpscare Horror

Invisibility now ends only after a successful melee hit, including an absorbed hit. Air swings, block swings and rejected attacks do not reveal the player or consume the opening attack. Jumpscare Horror retains its doubled invisible opening-hit damage. Existing duration, cooldown and equipment/effect restoration are retained.

## Description and presentation changes

Descriptions now show **current-level values in white**, with ordinary text in gray. Slash-separated lists of every level are no longer used in rendered descriptions. This applies to weapon/template tooltips, augmentation descriptions, upgrade previews and current cooldowns. Configurable activation keys remain configurable.

Reviewed descriptions now describe the actual current roster, damage, range, duration and conditional effects. Examples include Necromancer's selected reinforcements, Charge Attack's maximum-level Lightning Rod sentence and Death Stare's omitted range restriction at maximum level.

The following received reviewed wording/current-value presentation **without a separate gameplay rework in this session**:

- Blood Rush
- Crosswind
- Death Stare
- Downdraft Bell
- Explosive Hits
- Feather Shelter
- Flame Breath
- Flaming Stomp
- Live Wire
- Lullaby

Unreviewed abilities and passives received current-value formatting only. **Magma Toss was shown as the next review item, but its mechanics were not changed.** Changes to the Skull Throw flight regression were test-fixture changes, not a gameplay rebalance.

## Verification and remaining checks

The final implementation passed the build and **all 344 required GameTests twice**, recorded in `build/necromancer-enchantments-review.log` and `build/necromancer-final-review.log`. This records the completed implementation runs; tests were not rerun merely to write this document.

The Necromancer editor still needs a manual visual check in a running Minecraft client.

For the detailed completion checklist, see [ability-review-implementation.md](ability-review-implementation.md). Runtime values and behavior remain authoritative in the source code; this document records this session's changes rather than future edits.
