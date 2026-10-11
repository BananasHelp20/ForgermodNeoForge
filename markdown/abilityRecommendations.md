# Special weapon ability recommendations

This file contains **marked abilities awaiting implementation** and **77 new, unapproved replacement proposals**. Already implemented recommendations have been removed; their behavior and testing history are recorded in [markedAbilityImplementation.md](markedAbilityImplementation.md).

Entries marked `[implement]` retain your existing approval and descriptions. Unmarked entries are new ideas for your review; they are not approved or implemented. Each discarded unmarked proposal has one replacement, keeping its active or passive type. Any numbers in the new proposals are starting suggestions, not final balance decisions.

Weapon sections cover the base item and its Ruby, Amber, Amethyst and Jade variants. A weapon can learn two active and two unique passive abilities, plus Material Hit separately. Dual Wield and the intrinsic axe slam remain separate. This list offers alternatives; it does not add every proposal to a weapon at once. Gemstones keep their existing bonuses. Rusty and Carbon Steel equipment remain outside this list.

## Design rules for new proposals

- Keep a clear trigger and a clear result: a short buff, one armed strike, one projectile or a straightforward passive condition.
- Use familiar Minecraft effects, combat, movement and items. Avoid hidden pattern tracking, elaborate markers and complicated AI predictions.
- Directed attacks stop at solid blocks. Movement uses velocity unless the proposal explicitly calls for teleportation; teleports require safe destinations.
- Secondary or reflected damage must not recursively trigger hit abilities. Combat attacks exclude allies unless explicitly stated otherwise.
- Temporary combat effects do not destroy terrain or equipment unless the proposal explicitly describes equipment damage. Flower placement and normal mining still follow Minecraft rules.
- New active abilities need appropriate cooldowns; 20-60 seconds is a starting range, with stronger effects taking longer. Rank upgrades should improve simple values such as duration, range or damage.
- Temporary summons are capped and drop no loot. Ongoing combat states cancel on switching, death or logout; stored item inventories remain on their weapon.

## Ignisium

### Emberfang Dagger — `infernal_dagger`

1. **Flame Breath — Active.** Breathe a short cone of fire that burns enemies in front of you. Suggested range: four blocks.
2. **Charred Edge — Passive.** Deal 15% more melee damage to enemies with Fire Resistance.

### Molten Axe — `infernal_axe`

1. **Magma Toss — Active.** Throw a molten rock that bursts against the first enemy or block it hits, damaging and igniting nearby enemies without breaking terrain.

## Inanisium

### Riftfang Dagger — `dagger_of_the_void`

1. **Displace — Active.** Swap places with the visible enemy you aim at within ten blocks, if both destinations are clear.
2. **Backstab — Passive.** Deal 20% more damage when striking an enemy from behind.
3. **Void Grip — Passive.** Your melee hits produce 25% less knockback, keeping enemies within dagger reach.

### Claymore of the Void — `claymore_of_the_void`

1. **Void Pulse — Active.** Release a damage-free pulse that pushes nearby enemies away from you.
2. **Starless Guard — Active.** Gain Resistance II for five seconds.
3. **Distant Edge — Passive.** Deal 15% more melee damage when the enemy is at least three blocks away, within your normal attack reach.

### Nullified Axe — `axe_of_the_void`

1. **Gravity Well — Active.** Pull enemies within six blocks toward you with one burst of force. Does not teleport them or deal damage.
2. **Void Mail — Passive.** Take 20% less damage from projectiles while holding the axe.

## Somnium

### Nightmare Dagger — `dreambound_dagger`

1. [implement] **Lonely Dream — Passive.** + 5% attack speed for each enemy within 10 blocks radius.
2. [implement] **Unfinished Dream — Passive.** Killing an enemy before it could hit you heals you half a heart.
3. [implement] **Just a Dream - Active** Teleport to your respawn point
4. [implement] **Sleepwalker - passive** deal 10% more damage at night.

### Dreambound Claymore — `dreambound_claymore`

1. **Lullaby — Active.** Put the aimed mob to sleep for three seconds. It cannot move or attack; damage wakes it immediately. Players receive brief Slowness instead.
2. **Dream Feast — Active.** Restore four hunger points, equal to two food icons.
3. **Rested Blade — Passive.** After sleeping through the night, deal 15% more melee damage until the next sunset.
4. **Dream Catcher — Passive.** Negative potion effects applied to you while holding the blade last 20% less time.
5. [implement] **Just a Dream - Active** Teleport to your respawn point
6. [implement] **Unfinished Dream — Passive.** Killing an enemy before it could hit you heals you half a heart.

### Dreamweaver Axe — `dreambound_axe`

1. **Drowsy Blow — Active.** Your next vertical strike gives every enemy it hits Weakness II for five seconds.
2. **Wake-up Call — Active.** Remove Slowness and Weakness from yourself and nearby allies.
3. **Daydreamer — Passive.** Gain Jump Boost I while holding the axe during daytime.
4. **Pillow Armor — Passive.** Take 25% less fall damage while holding the axe.
5. [implement] **Just a Dream - Active** Teleport to your respawn point
6. [implement] **Unfinished Dream — Passive.** Killing an enemy before it could hit you heals you half a heart.

## Electrium

### Static Dagger — `dagger_of_thunder`

1. **Spark Shot — Active.** Fire a small electrical bolt that damages the first enemy it hits. It deals extra damage if that enemy is wet.
2. **Live Wire — Active.** For eight seconds, enemies that hit you in melee receive two damage in return. Reflected damage cannot reflect again.
3. **Quick Recharge — Passive.** A successful fully charged critical hit shortens your primary ability cooldown by one second. Can trigger once every two seconds.
4. **Conductive Edge — Passive.** Deal 20% more melee damage to enemies standing in water or exposed to rain.
5. [implement] **Static blade - active** The next swing will release an echo of the swing (4 blocks wide, half a block tall) traveling up to 20 blocks in the direction the player faced at that point. It pierces through all enemies on it's way, dealing the weapon's damage.
6. [implement] **Lightning Rod — Passive.** Getting struck by lightning happens more often (and without a thunderstorm having to be present) and doesn't deal damage. Gives the player 5 absorbtion hearts and strength 1.
7. [implement] **Alternating Current — Active.** Hit an enemy, if there is another enemy within 20 blocks, a stun (dealing no damage, stunning for .1 second) travels between the 2 enemies (hit first enemy -> stun second enemy, wait 1 second, stun first enemy, wait 1 second, stun second enemy,...) for 1 minute
8. [implement] **Direct Current - active** Hit 2 (up to 4(if more than 2, beginning a barrier at 2 with way more stunning time)) enemies (within 50 blocks distance from each other), stunning them for 20 seconds. During that time, there is an electrical barrier between them that deals 2 hearts damage when crossing. (last upgrade upgrades this ability to "Static Field" which creates a field at 3 marked entities additionally to the barriers between the enemies where all entities within that field are being dealt 1hp damage per second)

### Claymore of Thunder — `claymore_of_thunder`

1. [implement] **Stunning Presence — Active.** All enemies in a radius of up to 10 blocks gradually get stunned every (level 1: 2 | max: 1) seconds (dealing half a heart per stun and stopping them for .3 seconds each stun) for 30 seconds
2. **Thunder Guard — Active.** For eight seconds, block the next incoming hostile projectile and restore two health points. The protection ends after that projectile.
3. [implement] **Alternating Current — Active.** Hit an enemy, if there is another enemy within 20 blocks, a stun (dealing no damage, stunning for .1 second) travels between the 2 enemies (hit first enemy -> stun second enemy, wait 1 second, stun first enemy, wait 1 second, stun second enemy,...) for 1 minute
4. [implement] **Lightning Rod — Passive.** Getting struck by lightning happens more often (and without a thunderstorm having to be present) and doesn't deal damage. Gives the player 5 absorbtion hearts and strength 1.
5. [implement] **Direct Current - active** Hit 2 (up to 4(if more than 2, beginning a barrier at 2 with way more stunning time)) enemies (within 50 blocks distance from each other), stunning them for 20 seconds. During that time, there is an electrical barrier between them that deals 2 hearts damage when crossing. (last upgrade upgrades this ability to "Static Field" which creates a field at 3 marked entities additionally to the barriers between the enemies where all entities within that field are being dealt 1hp damage per second)

### Voltage Axe — `axe_of_thunder`

1. **Thunderclap — Active.** Destroy hostile projectiles within six blocks with one electrical burst. Does not damage enemies or blocks.
2. **Magnetic Throw — Active.** Launch the aimed enemy within melee reach forward along your look direction. Uses velocity, without teleporting or adding damage.
3. **Insulated Handle — Passive.** Take 25% less magic damage, including damage from Poison and Wither.
4. **Heavy Current — Passive.** Deal 15% more melee damage to enemies wearing armor in all four normal armor slots.
5. [implement] **Lightning Rod — Passive.** Getting struck by lightning happens more often (and without a thunderstorm having to be present) and doesn't deal damage. Gives the player 5 absorbtion hearts and strength 1.
6. [implement] **Alternating Current — Active.** Hit an enemy, if there is another enemy within 20 blocks, a stun (dealing no damage, stunning for .1 second) travels between the 2 enemies (hit first enemy -> stun second enemy, wait 1 second, stun first enemy, wait 1 second, stun second enemy,...) for 1 minute
7. [implement] **Direct Current - active** Hit 2 (up to 4(if more than 2, beginning a barrier at 2 with way more stunning time)) enemies (within 50 blocks distance from each other), stunning them for 20 seconds. During that time, there is an electrical barrier between them that deals 2 hearts damage when crossing. (last upgrade upgrades this ability to "Static Field" which creates a field at 3 marked entities additionally to the barriers between the enemies where all entities within that field are being dealt 1hp damage per second)

## Taifunite

### Dead Calm Dagger — `storming_dagger`

1. [implement] **Downdraft Bell — Active.** Strike the air to force nearby airborne enemies downward once. It cancels ascent without teleporting or adding artificial fall damage; grounded enemies are unaffected.
2. [implement] **Crosswind — Active.** Every Projectile being shot at you in the next 2 minutes is redirected to miss you.
3. **Air Bubble — Active.** Gain Water Breathing for thirty seconds and Dolphin's Grace for ten seconds.
4. [implement] **Slipstream — Passive.** Running immediately behind a moving allied entity gives a small movement benefit. You lose it when overtaking, stopping or breaking sight, so it supports formation travel rather than free speed.
5. **Skimming Step — Passive.** Move 20% faster through shallow water while your head remains above the surface.
6. [implement] **Upwind — Active.** the next hit hits the enemy with extra knockback and launches it in the air
7. [implement] **Windcharge - active** launches the player 5 blocks in the air (last upgrade -> 20 blocks)

### Storming Claymore — `storming_claymore`

1. [implement] **Downdraft Bell — Active.** Strike the air to force nearby airborne enemies downward once. It cancels ascent without teleporting or adding artificial fall damage; grounded enemies are unaffected.
2. [implement] **Crosswind — Active.** Every Projectile being shot at you in the next 2 minutes is redirected to miss you.
3. [implement] **Upwind — Active.** the next hit hits the enemy with extra knockback and launches it in the air
4. **Feather Shelter — Active.** Give yourself and nearby allies Slow Falling for ten seconds.
5. **Headwind — Passive.** Sprinting uses half the normal hunger exhaustion while holding the blade.
6. **Air Reserve — Passive.** Recover your air supply twice as quickly after coming out of water.
7. [implement] **Windcharge - active** launches the player 5 blocks in the air (last upgrade -> 20 blocks)

### Skybreaker Axe — `storming_axe`

1. [implement] **Crosswind — Active.** Every Projectile being shot at you in the next 2 minutes is redirected to miss you.
2. [implement] **Pocket Storm -active** The next hit will create an areal smash attack with great knockback to all affected enemies.
3. [implement] **Downdraft Bell — Active.** Strike the air to force nearby airborne enemies downward once. It cancels ascent without teleporting or adding artificial fall damage; grounded enemies are unaffected.
4. **Vacuum Burst — Active.** Pull nearby dropped items and experience orbs toward you with one gust.
5. **Sky Hunter — Passive.** Deal 20% more damage to phantoms, blazes and ghasts.
6. **Windproof — Passive.** Take 30% less knockback while airborne.
7. [implement] **Upwind — Active.** the next hit hits the enemy with extra knockback and launches it in the air
8. [implement] **Windcharge - active** launches the player 10 blocks in the air (last upgrade upgrades ability to "Skybreaker", launching the player 30 blocks in the air, making it able to do an arial mace attack hitting all enemies within 2 blocks of the hit (also when hitting the ground))

## Vulnusium

### Assassin Dagger — `curseblood_dagger`

1. **Blood Rush — Active.** Spend two health points to gain Speed II for ten seconds. Cannot be used if that health cost would kill you.
2. **Clotting Cut — Active.** Your next melee hit prevents the enemy from healing for eight seconds.
3. **Fresh Blood — Passive.** Deal 20% more damage on a hit against an enemy at full health.
4. [implement] **Final strike— Passive.** if the last hit enemy has less than 10% health, grants a damage bonus to that enemy.
5. [implement] **Blood Sacrafice - active** sacrifice 50% of your health (including absorption health) and deal double damage for the next 20 seconds.
6. [implement] **Scar Compass — Passive.** The most recent enemy that's been hit, always has an 10-minute glow effect until the effect disapates, or you hit another enemy.
7. [implement] **Revengeful Exit — Passive.** When you die to an enemy, explode with an areal blood explosion (dealing weapon damage to all entities in a radius of 5 blocks) and damage the enemy that killed you 5 times your weapon damage.

### Curseblood Claymore — `curseblood_claymore`

1. **Blood Guard — Active.** Spend two health points to gain six absorption health points for ten seconds. Cannot kill you or stack its own absorption.
2. **Crimson Cleave — Active.** Arm your next melee hit. On landing, spend two health points to deal two additional damage to enemies near the victim. Cannot trigger if the health cost would kill you.
3. [implement] **Scar Compass — Passive.** The most recent enemy that's been hit, always has an 10-minute glow effect until the effect disapates, or you hit another enemy.
4. [implement] **Revengeful Exit — Passive.** When you die to an enemy, explode with an areal blood explosion (dealing weapon damage to all entities in a radius of 5 blocks) and damage the enemy that killed you 5 times your weapon damage.
5. [implement] **Blood Sacrafice - active** sacrifice 50% of your health (including absorption health) and deal double damage for the next 20 seconds.
6. [implement] **Final strike— Passive.** if the last hit enemy has less than 10% health, grants a damage bonus to that enemy.

### Woundmaker Axe — `curseblood_axe`

1. **Blood Pact — Active.** Spend four health points to heal a nearby aimed ally by four health points. Cannot kill you and does not spend health on a full-health ally.
2. **Rupture — Active.** Your next melee hit removes up to eight absorption health points from its victim before normal damage is applied.
3. **Blood Armor — Passive.** Take 15% less melee damage while below half health.
4. **Hungry Blade — Passive.** Breaking an enemy shield grants Strength I for five seconds.
5. [implement] **Blood Sacrafice - active** sacrifice 50% of your health (including absorption health) and deal double damage for the next 20 seconds.
6. [implement] **Final strike— Passive.** if the last hit enemy has less than 10% health, grants a damage bonus to that enemy.
7. [implement] **Scar Compass — Passive.** The most recent enemy that's been hit, always has an 10-minute glow effect until the effect disapates, or you hit another enemy.
8. [implement] **Revengeful Exit — Passive.** When you die to an enemy, explode with an areal blood explosion (dealing weapon damage to all entities in a radius of 5 blocks) and damage the enemy that killed you 5 times your weapon damage.

## Overgrown / Lush

### Leafcutter Dagger — `nature_dagger`

1. **Bramble Shot — Active.** Fire a thorn that damages and briefly slows the first enemy it hits.
2. **Sunfed — Passive.** Restore one hunger point every fifteen seconds while standing outside in daylight and holding the dagger. Does not add saturation.
3. **Leaf Cover — Passive.** Gain Resistance I while crouching inside leaves.

### Overgrown Claymore — `overgrown_claymore`

1. **Green Feast — Active.** Consume one wheat from your inventory to restore four health points. Requires missing health.
2. **Sap Shield — Active.** Gain four absorption health points for eight seconds. While that absorption remains, melee hits cannot knock you back.
3. **Deep Roots — Passive.** Take 20% less melee damage while crouching on dirt, grass or moss.
4. **Compost — Passive.** Eating rotten flesh while holding the blade does not give you Hunger.

### Verdant Axe — `nature_axe`

1. **Oakskin — Active.** Gain six armor points for ten seconds, but receive Slowness I for the first five seconds.
2. **Sapling Harvest — Passive.** Enemy kills have a 15% chance to drop one extra oak sapling.
3. **Lumberjack — Passive.** Breaking logs with this axe does not consume weapon durability.

## Morsium

### Deathwisper Dagger — `hollow_dagger`

1. **Banish — Active.** Strike the aimed undead enemy within melee reach for twice your weapon damage. Has no effect on living targets.
2. **Ghostwalk — Active.** Gain Invisibility for five seconds. Attacking ends it; worn armor stays visible.
3. **Soul Hunger — Passive.** Enemy kills grant two absorption health points, up to six from this passive.
4. **Pale Blade — Passive.** Deal 15% more damage to invisible enemies.

### Hollow Claymore — `hollow_claymore`

1. **Grave Chill — Active.** Give the visible enemy you aim at Slowness IV for four seconds.
2. **Soul Cleave — Active.** Your next melee hit gains two bonus damage for each other enemy near the victim, capped at eight bonus damage.
3. **Last Stand — Passive.** Once every two minutes, an otherwise fatal hit leaves you at one health point instead.
4. **Grave Strength — Passive.** Weakness reduces your attack damage by only half its normal amount while holding the blade.

### Ghost Axe — `hollow_axe`

1. **Bone Guard — Active.** Summon one temporary friendly skeleton for fifteen seconds. It shoots nearby enemies and drops no items or experience. Only one can exist per wielder.
2. **Skull Throw — Active.** Fire a slow wither skull that bursts on its first impact, damaging nearby enemies and applying brief Wither without changing terrain.
3. **Death's Due — Passive.** Enemy kills grant 25% more experience, without changing their item drops.
4. **Undead Ward — Passive.** Take 15% less damage from undead enemies.

## Pulsite

### Warden's Needle — `shrieking_dagger`

1. **Pressure Needle — Active.** Your next melee hit ignores half of the victim's armor. Shield blocking still works normally.
2. **Echo Appetite — Passive.** Breaking a sculk block restores one hunger point, without adding saturation.
3. **Resonant Crit — Passive.** Fully charged hits against jumping enemies count as critical hits even when you are standing on the ground.

### Shrieking Claymore — `shrieking_claymore`

1. **Dissonance — Active.** Your next melee hit removes one random positive potion effect from the enemy.
2. **Ringing Steel — Active.** Your next melee hit also damages the victim's held weapon by fifteen durability points. Does not affect armor or shields.
3. **Stone Song — Passive.** Gain two armor points while holding the blade below Y=0.
4. **Aftershock — Passive.** Each fully charged melee hit causes one extra point of damage one second later. Echo damage cannot trigger hit abilities or create another echo.

### Echoing Axe — `shrieking_axe`

1. **Rebound — Active.** Aim at a solid wall within three blocks to launch yourself away from its face with velocity.
2. **Challenge Roar — Active.** Make nearby hostile mobs target you. Has no effect on players or allied mobs.
3. **Bedrock Grip — Passive.** Take 50% less damage from falling anvils and dripstone while holding the axe.
