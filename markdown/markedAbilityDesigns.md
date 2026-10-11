# Approved marked ability designs

Snapshot of the marked descriptions at the beginning of the full implementation pass. Completion and test results are tracked in [markedAbilityChecklist.md](markedAbilityChecklist.md) and [markedAbilityImplementation.md](markedAbilityImplementation.md).

## Flame Breath

**Weapons:** Ignisium:dagger, Ignisium:axe, Ignisium:claymore

1. [implement] **Flame Breath — Active.** Breathe a short cone of fire that burns enemies in front of you. Suggested range: four blocks.

## Charred Edge

**Weapons:** Ignisium:dagger

2. [implement] **Charred Edge — Passive.** Deal 15% more melee damage to enemies with Fire Resistance.

## Magma Toss

**Weapons:** Ignisium:axe

1. [implement] **Magma Toss — Active.** Throw a molten rock that bursts against the first enemy or block it hits, damaging and igniting nearby enemies without breaking terrain.

## Displace

**Weapons:** Inanisium:dagger

1. [implement] **Displace — Active.** Swap places with the visible enemy you aim at within ten blocks, if both destinations are clear.

## Backstab

**Weapons:** Inanisium:dagger, Somnium:dagger, Electrium:dagger, Taifunite:dagger, Vulnusium:dagger, Lush:dagger, Morsium:dagger, Pulsite:dagger

2. [implement] **Backstab — Passive.** Deal 20% more damage when striking an enemy from behind.

## Void Grip

**Weapons:** Inanisium:dagger

3. [implement] **Void Grip — Passive.** Your melee hits produce 25% less knockback, keeping enemies within dagger reach.

## Void Pulse

**Weapons:** Inanisium:claymore

1. [implement] **Void Pulse — Active.** Release a damage-free pulse that pushes all nearby enemies away from you.

## Gravity Well

**Weapons:** Inanisium:axe

1. [implement] **Gravity Well — Active.** Pull enemies within six blocks toward you with one burst of force and immediately do a smash attack when it is in reach. Does not teleport them. User clarification: the pull deals no damage; the arrival smash deals weapon damage with a visual smash.

## Lonely Dream

**Weapons:** Somnium:dagger

1. [implement] **Lonely Dream — Passive.** + 5% attack speed for each enemy within 10 blocks radius.

## Unfinished Dream

**Weapons:** Somnium:dagger, Somnium:claymore, Somnium:axe

2. [implement] **Unfinished Dream — Passive.** Killing an enemy before it could hit you heals you half a heart.
6. [implement] **Unfinished Dream — Passive.** Killing an enemy before it could hit you heals you half a heart.

## Just a Dream

**Weapons:** Somnium:dagger, Somnium:claymore, Somnium:axe

3. [implement] **Just a Dream - Active** Teleport to your respawn point
5. [implement] **Just a Dream - Active** Teleport to your respawn point

## Sleepwalker

**Weapons:** Somnium:dagger

4. [implement] **Sleepwalker - passive** deal 10% more damage at night.

## Lullaby

**Weapons:** Somnium:claymore

1. [implement] **Lullaby — Active.** Put the aimed mob to sleep for three seconds. It cannot move or attack; damage wakes it immediately. Players receive brief Slowness instead.

## Dream Feast

**Weapons:** Somnium:claymore

2. [implement] **Dream Feast — Active.** Restore eight hunger points, equal to 4 food icons.

## Daydreamer

**Weapons:** Somnium:axe

3. [implement] **Daydreamer — Passive.** Gain Jump Boost I while holding the axe during daytime.

## Pillow Armor

**Weapons:** Somnium:axe

4. [implement] **Pillow Armor — Passive.** Take 25% less fall damage while holding the axe.

## Magnetic Throw

**Weapons:** Electrium:dagger, Electrium:claymore, Electrium:axe

2. [implement] **Magnetic Throw — Active.** Launch the aimed enemy within melee reach forward along your look direction. Uses velocity, without teleporting or adding damage.

## Live Wire

**Weapons:** Electrium:dagger

2. [implement] **Live Wire — Active.** For eight seconds, enemies that hit you in melee receive two damage in return. Reflected damage cannot reflect again.

## Quick Recharge

**Weapons:** Electrium:dagger, Electrium:claymore, Electrium:axe

3. [implement] **Quick Recharge — Passive.** A successful fully charged critical hit shortens your primary ability cooldown by one second. Can trigger once every two seconds.

## Conductive Edge

**Weapons:** Electrium:dagger, Electrium:claymore, Electrium:axe

4. [implement] **Conductive Edge — Passive.** Deal 20% more melee damage to enemies standing in water or exposed to rain.

## Static blade

**Weapons:** Electrium:dagger

5. [implement] **Static blade - active** The next swing will release an echo of the swing (4 blocks wide, half a block tall) traveling up to 20 blocks in the direction the player faced at that point. It pierces through all enemies on it's way, dealing the weapon's damage.

## Lightning Rod

**Weapons:** Electrium:dagger, Electrium:claymore, Electrium:axe

6. [implement] **Lightning Rod — Passive.** Getting struck by lightning happens more often (and without a thunderstorm having to be present) and doesn't deal damage. Gives the player 5 absorbtion hearts and strength 1.
4. [implement] **Lightning Rod — Passive.** Getting struck by lightning happens more often (and without a thunderstorm having to be present) and doesn't deal damage. Gives the player 5 absorbtion hearts and strength 1.
5. [implement] **Lightning Rod — Passive.** Getting struck by lightning happens more often (and without a thunderstorm having to be present) and doesn't deal damage. Gives the player 5 absorbtion hearts and strength 1.

## Alternating Current

**Weapons:** Electrium:dagger, Electrium:claymore, Electrium:axe

7. [implement] **Alternating Current — Active.** Hit an enemy, if there is another enemy within 20 blocks, a stun (dealing no damage, stunning for .1 second) travels between the 2 enemies (hit first enemy -> stun second enemy, wait 1 second, stun first enemy, wait 1 second, stun second enemy,...) for 1 minute
3. [implement] **Alternating Current — Active.** Hit an enemy, if there is another enemy within 20 blocks, a stun (dealing no damage, stunning for .1 second) travels between the 2 enemies (hit first enemy -> stun second enemy, wait 1 second, stun first enemy, wait 1 second, stun second enemy,...) for 1 minute
6. [implement] **Alternating Current — Active.** Hit an enemy, if there is another enemy within 20 blocks, a stun (dealing no damage, stunning for .1 second) travels between the 2 enemies (hit first enemy -> stun second enemy, wait 1 second, stun first enemy, wait 1 second, stun second enemy,...) for 1 minute

## Direct Current

**Weapons:** Electrium:dagger, Electrium:claymore, Electrium:axe

8. [implement] **Direct Current - active** Hit 2 (up to 4(if more than 2, beginning a barrier at 2 with way more stunning time)) enemies (within 50 blocks distance from each other), stunning them for 20 seconds. During that time, there is an electrical barrier between them that deals 2 hearts damage when crossing. (last upgrade upgrades this ability to "Static Field" which creates a field at 3 marked entities additionally to the barriers between the enemies where all entities within that field are being dealt 1hp damage per second)
5. [implement] **Direct Current - active** Hit 2 (up to 4(if more than 2, beginning a barrier at 2 with way more stunning time)) enemies (within 50 blocks distance from each other), stunning them for 20 seconds. During that time, there is an electrical barrier between them that deals 2 hearts damage when crossing. (last upgrade upgrades this ability to "Static Field" which creates a field at 3 marked entities additionally to the barriers between the enemies where all entities within that field are being dealt 1hp damage per second)
7. [implement] **Direct Current - active** Hit 2 (up to 4(if more than 2, beginning a barrier at 2 with way more stunning time)) enemies (within 50 blocks distance from each other), stunning them for 20 seconds. During that time, there is an electrical barrier between them that deals 2 hearts damage when crossing. (last upgrade upgrades this ability to "Static Field" which creates a field at 3 marked entities additionally to the barriers between the enemies where all entities within that field are being dealt 1hp damage per second)

## Stunning Presence

**Weapons:** Electrium:claymore

1. [implement] **Stunning Presence — Active.** All enemies in a radius of up to 10 blocks gradually get stunned every (level 1: 2 | max: 1) seconds (dealing half a heart per stun and stopping them for .3 seconds each stun) for 30 seconds

## Heavy Current

**Weapons:** Electrium:axe

4. [implement] **Heavy Current — Passive.** Deal 15% more melee damage to enemies wearing armor in all four normal armor slots.

## Downdraft Bell

**Weapons:** Taifunite:dagger, Taifunite:claymore, Taifunite:axe

1. [implement] **Downdraft Bell — Active.** Strike the air to force nearby airborne enemies downward once. It cancels ascent without teleporting or adding artificial fall damage; grounded enemies are unaffected.
3. [implement] **Downdraft Bell — Active.** Strike the air to force nearby airborne enemies downward once. It cancels ascent without teleporting or adding artificial fall damage; grounded enemies are unaffected.

## Crosswind

**Weapons:** Taifunite:dagger, Taifunite:claymore, Taifunite:axe

2. [implement] **Crosswind — Active.** Every Projectile being shot at you in the next 2 minutes is redirected to miss you.
1. [implement] **Crosswind — Active.** Every Projectile being shot at you in the next 2 minutes is redirected to miss you.

## Slipstream

**Weapons:** Taifunite:dagger

4. [implement] **Slipstream — Passive.** Running immediately behind a moving allied entity gives a small movement benefit. You lose it when overtaking, stopping or breaking sight, so it supports formation travel rather than free speed.

## Upwind

**Weapons:** Taifunite:dagger, Taifunite:claymore, Taifunite:axe

6. [implement] **Upwind — Active.** the next hit hits the enemy with extra knockback and launches it in the air
3. [implement] **Upwind — Active.** the next hit hits the enemy with extra knockback and launches it in the air
7. [implement] **Upwind — Active.** the next hit hits the enemy with extra knockback and launches it in the air

## Windcharge

**Weapons:** Taifunite:dagger, Taifunite:claymore, Taifunite:axe

7. [implement] **Windcharge - active** launches the player 5 blocks in the air (last upgrade -> 20 blocks)
8. [implement] **Windcharge - active** launches the player 10 blocks in the air (last upgrade upgrades ability to "Skybreaker", launching the player 30 blocks in the air, making it able to do an arial mace attack hitting all enemies within 2 blocks of the hit (also when hitting the ground))

## Vacuum Burst

**Weapons:** Taifunite:dagger, Taifunite:claymore, Taifunite:axe

4. [implement] **Vacuum Burst — Active.** Pull nearby dropped items and experience orbs toward you with one gust.

## Headwind

**Weapons:** Taifunite:dagger, Taifunite:claymore, Taifunite:axe

5. [implement] **Headwind — Passive.** Sprinting uses half the normal hunger exhaustion while holding the blade.

## Feather Shelter

**Weapons:** Taifunite:claymore, Taifunite:axe

4. [implement] **Feather Shelter — Active.** Give yourself and nearby allies Slow Falling for ten seconds.

## Pocket Storm

**Weapons:** Taifunite:axe

2. [implement] **Pocket Storm -active** The next hit will create an areal smash attack with great knockback to all affected enemies.

## Blood Rush

**Weapons:** Vulnusium:dagger, Vulnusium:claymore

1. [implement] **Blood Rush — Active.** Spend two health points to gain Speed II for ten seconds. Cannot be used if that health cost would kill you.

## Final strike

**Weapons:** Vulnusium:dagger, Vulnusium:claymore, Vulnusium:axe

4. [implement] **Final strike— Passive.** if the last hit enemy has less than 10% health, grants a damage bonus to that enemy.
6. [implement] **Final strike— Passive.** if the last hit enemy has less than 10% health, grants a damage bonus to that enemy.

## Blood Sacrafice

**Weapons:** Vulnusium:dagger, Vulnusium:claymore, Vulnusium:axe

5. [implement] **Blood Sacrafice - active** sacrifice 50% of your health (including absorption health) and deal double damage for the next 20 seconds.

## Scar Compass

**Weapons:** Vulnusium:dagger, Vulnusium:claymore, Vulnusium:axe

6. [implement] **Scar Compass — Passive.** The most recent enemy that's been hit, always has an 10-minute glow effect until the effect disapates, or you hit another enemy.
3. [implement] **Scar Compass — Passive.** The most recent enemy that's been hit, always has an 10-minute glow effect until the effect disapates, or you hit another enemy.
7. [implement] **Scar Compass — Passive.** The most recent enemy that's been hit, always has an 10-minute glow effect until the effect disapates, or you hit another enemy.

## Revengeful Exit

**Weapons:** Vulnusium:dagger, Vulnusium:claymore, Vulnusium:axe

7. [implement] **Revengeful Exit — Passive.** When you die to an enemy, explode with an areal blood explosion (dealing weapon damage to all entities in a radius of 5 blocks) and damage the enemy that killed you 5 times your weapon damage.
4. [implement] **Revengeful Exit — Passive.** When you die to an enemy, explode with an areal blood explosion (dealing weapon damage to all entities in a radius of 5 blocks) and damage the enemy that killed you 5 times your weapon damage.
8. [implement] **Revengeful Exit — Passive.** When you die to an enemy, explode with an areal blood explosion (dealing weapon damage to all entities in a radius of 5 blocks) and damage the enemy that killed you 5 times your weapon damage.

## Sunfed

**Weapons:** Lush:dagger, Lush:claymore, Lush:axe

2. [implement] **Sunfed — Passive.** Restore one hunger point every fifteen seconds while standing outside in daylight and holding the dagger. Does not add saturation.

## photosythesis

**Weapons:** Lush:dagger, Lush:claymore, Lush:axe

4. [implement] **photosythesis** slowly restore weapon durability during daytime.

## Green Feast

**Weapons:** Lush:dagger, Lush:claymore, Lush:axe

1. [implement] **Green Feast — Active.** Consume one wheat from your inventory to restore two health points. Requires missing health.

## Sapling Harvest

**Weapons:** Lush:dagger, Lush:claymore, Lush:axe

2. [implement] **Sapling Harvest — Passive.** Enemy kills have a 15% chance to drop one extra random sapling.

## Compost

**Weapons:** Lush:dagger, Lush:claymore, Lush:axe

4. [implement] **Compost — Passive.** Eating rotten flesh while holding the blade does not give you Hunger.

## Lumberjack

**Weapons:** Lush:axe

3. [implement] **Lumberjack — Passive.** Breaking logs with this axe does not consume weapon durability.

## Ghostwalk

**Weapons:** Morsium:dagger, Morsium:claymore, Morsium:axe

2. [implement] **Ghostwalk — Active.** Gain Invisibility for ten seconds. Attacking ends it; worn armor stays visible.

## Soul Hunger

**Weapons:** Morsium:dagger, Morsium:claymore, Morsium:axe

3. [implement] **Soul Hunger — Passive.** Enemy kills grant two absorption health points, up to six from this passive.

## Last Stand

**Weapons:** Morsium:dagger, Morsium:claymore, Morsium:axe

3. [implement] **Last Stand — Passive.** Once every two minutes, an otherwise fatal hit leaves you at one health point instead. (only when above 1hp)

## Bone Guard

**Weapons:** Morsium:axe

1. [implement] **Bone Guard — Active.** Summon one temporary friendly skeleton for fifteen seconds. It shoots nearby enemies and drops no items or experience. Only one can exist per wielder.

## Skull Throw

**Weapons:** Morsium:axe

2. [implement] **Skull Throw — Active.** Fire a slow wither skull that bursts on its first impact, damaging nearby enemies and applying brief Wither without changing terrain.

## Death's Due

**Weapons:** Morsium:axe

3. [implement] **Death's Due — Passive.** Enemy kills grant 25% more experience, without changing their item drops.

## Echo Appetite

**Weapons:** Pulsite:dagger, Pulsite:claymore, Pulsite:axe

2. [implement] **Echo Appetite — Passive.** Breaking a sculk block restores one hunger point, without adding saturation.

## Resonant Crit

**Weapons:** Pulsite:dagger, Pulsite:claymore

3. [implement] **Resonant Crit — Passive.** Fully charged hits against jumping enemies count as critical hits even when you are standing on the ground.

## Aftershock

**Weapons:** Pulsite:dagger, Pulsite:claymore, Pulsite:axe

4. [implement] **Aftershock — Passive.** Each fully charged melee hit causes one extra point of damage one second later. Echo damage cannot trigger hit abilities or create another echo.

## Ringing Steel

**Weapons:** Pulsite:claymore

2. [implement] **Ringing Steel — Active.** Your next melee hit also damages the victim's held weapon by fifteen durability points. Does not affect armor or shields.

## Stone Song

**Weapons:** Pulsite:claymore

3. [implement] **Stone Song — Passive.** Gain two armor points while holding the blade below Y=0.

## Challenge Roar

**Weapons:** Pulsite:axe

2. [implement] **Challenge Roar — Active.** Make nearby hostile mobs target you. Has no effect on players or allied mobs.

## Latest live-file corrections

- Death's Due is marked for all three Morsium weapon types.
- Bone Guard is marked for the Hollow Claymore as well as the Ghost Axe.
- Challenge Roar is marked for all three Pulsite weapon types.
- The critical passive is now named Resonance Crit; its approved behavior is unchanged.
