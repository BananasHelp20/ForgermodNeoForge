# In-game weapon abilities

English descriptions from the game language file and actual registered pools: 60 active abilities, 49 passives, and nine material-hit effects. Unimplemented proposals are excluded.

Special weapons learn these abilities through augmentation. Each assignment includes its base, Ruby, Amber, Amethyst and Jade variants. Item descriptions appear while holding Shift. Most text below is a historical base learning summary; see [the reviewed implementation queue](ability-review-implementation.md) for the current approved changes. The shared AbilityDescriptions implementation is authoritative for current in-game wording: gray prose and white current-level values, including cooldowns. Base abilities show a separate Shift-only super-upgrade notice below their cooldown, without describing future super effects. Upgrade cards compare ranks and highlight changed values in white.

## Active abilities

### Absolute Nightmare

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Your next two dagger hits inflict Weakness IV and Darkness V for 5 seconds.

**Augmentation card:**

> Two hits inflict Weakness IV and Darkness V for 5-10 seconds.

### Alternating Current

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Your next hit links two mobs within 20 blocks, including allies. For 1 minute, they alternate half-second stuns each second.

**Augmentation card:**

> Link two mobs, including allies; brief stuns alternate for 1 minute.

### Always the Gardeners

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Your next kill plants a 5-minute poison cloud. Everyone inside, including you, receives Poison I for 20 seconds.

**Augmentation card:**

> Your next kill plants a lasting cloud that poisons everyone.

### Area Discharge

**Weapons:** Static Dagger

**Item tooltip:**

> Strike hostile mobs within 5 blocks with lightning for half weapon damage. These hits also build Charge Attack.

**Augmentation card:**

> Lightning hits nearby enemies for half damage and builds charge.

### Blood Rush

**Weapons:** Assassin Dagger, Curseblood Claymore

**Item tooltip:**

> Spend 2 HP to gain Speed II for 10 seconds.

**Augmentation card:**

> Spend 2 HP for 10 seconds of Speed II.

### Blood Sacrafice

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> Sacrifice half your health and absorption to deal double weapon damage for 20 seconds.

**Augmentation card:**

> Spend half your health and absorption for 20 seconds of double damage.

### Bone Guard

**Weapons:** Ghost Axe, Hollow Claymore

**Item tooltip:**

> Activate this ability to summon 1 Skeleton that will fight for you in battle. Reinforcements disappear after 10 minutes

**Augmentation card:**

> Activate this ability to summon 1 Skeleton that will fight for you in battle. Reinforcements disappear after 10 minutes

**Super I-IV name (transforms at original IV):** Necromancer

**Necromancer tooltip:**

> Summon {reinforcements} that fight for you're side. Reinforcements can be customized using Shift+Alt.

The roster placeholder resolves to the actual selected reinforcement names/counts in white. Capacity is4/6/8/10; real equipment unlocks at II and retains wear/enchantments between summons. Skeleton Horse costs3 including its rider; Zombie Horse and bosses are excluded. Shift+Alt opens on modifier release (Shift+Alt+S still rotates abilities). Editing requires no living reinforcements. All groups last10 minutes and are replaced on a ready recast; switching weapons does not despawn them.

### Challenge Roar

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Taunt nearby hostile mobs into targeting you and gain Strength I for 10 seconds.

**Augmentation card:**

> Taunt nearby hostile mobs and gain Strength I for 10 seconds.

### Charge Attack

**Weapons:** Static Dagger

**Item tooltip:**

> Each hit adds 5% damage, up to +100%. Hit 21 releases a lightning strike for 20 extra damage.

**Augmentation card:**

> Build +5% damage per hit; hit 21 releases lightning.

### Cinder Decoy

**Weapons:** Infernal Claymore

**Item tooltip:**

> Leave a decoy that attracts hostile mobs, then explodes and ignites enemies when struck or when it fades.

**Augmentation card:**

> Lure mobs with a decoy that explodes when struck or when it fades.

### Crosswind

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Deflect incoming projectiles for 2 minutes.

**Augmentation card:**

> Deflect incoming projectiles for 2 minutes.

### Crucible Hook

**Weapons:** Molten Axe

**Item tooltip:**

> Pull any mob within 20 blocks to a point 2 blocks in front of you, then smash and ignite it.

**Augmentation card:**

> Pull a mob 2 blocks in front of you, then smash and ignite it.

### Death Stare

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Teleport beside the mob under your crosshair within 30 blocks.

**Augmentation card:**

> Teleport beside the mob under your crosshair.

### Deep Wound

**Weapons:** Assassin Dagger

**Item tooltip:**

> Your next critical dagger hit deals double damage and inflicts Slowness III for 5 seconds.

**Augmentation card:**

> Your next crit deals double damage and Slowness III for 5 seconds.

### Delusion

**Weapons:** Dreambound Claymore, Dreamweaver Axe, Nightmare Dagger

**Item tooltip:**

> Your next hit makes a mob lose its aggression for 20 seconds.

**Augmentation card:**

> Your next hit calms a mob for 20 seconds.

### Direct Current

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Mark two mobs with melee hits to stun them and create damaging barriers.

**Augmentation card:**

> Mark two mobs with melee hits to stun them and create damaging barriers.

**Super I-IV name (transforms at original IV):** Static Field

### Displace

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Swap places with a visible enemy within 10 blocks.

**Augmentation card:**

> Swap places with a visible enemy within 10 blocks.

### Downdraft Bell

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> For 10 seconds, pull airborne mobs and elytra flyers above you down while inside your radius. Unlimited height.

**Augmentation card:**

> For 10 seconds, pull nearby airborne mobs and elytra flyers down.

### Dream Feast

**Weapons:** Dreambound Claymore

**Item tooltip:**

> Restore 8 hunger and 8 saturation.

**Augmentation card:**

> Restore 8 hunger and 8 saturation.

### Echo Hearing

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Listen for nearby sounds and briefly outline noisy entities. Only you see these outlines.

**Augmentation card:**

> Listen for sounds; privately outline noisy entities.

### Ender Swing

**Weapons:** Nullified Axe

**Item tooltip:**

> Strike enemies within 3 blocks for weapon damage and launch them up to 20 blocks upward.

**Augmentation card:**

> Strike enemies within 3 blocks and launch them into the air.

### Explosion

**Weapons:** Infernal Claymore

**Item tooltip:**

> Explode without harming yourself or blocks, and gain Resistance IV for 2 seconds.

**Augmentation card:**

> Explode safely and gain Resistance IV for 2 seconds.

### Explosive Hits

**Weapons:** Infernal Claymore

**Item tooltip:**

> Your next 10 hits explode for 30% weapon damage to nearby enemies, without harming you or blocks.

**Augmentation card:**

> Ten hits explode for 30% weapon damage. You and blocks are safe.

### Eye of the Storm

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Whirl entities within 10 blocks for 5 seconds, levitating living targets.

**Augmentation card:**

> Whirl nearby entities and levitate living targets for 5 seconds.

### False Awakening

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Mark an enemy within 20 blocks and distract mobs with a decoy. Your next hit on the marked enemy deals bonus damage.

**Augmentation card:**

> Distract mobs with a decoy; deal bonus damage to the marked enemy.

### Feather Shelter

**Weapons:** Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Give yourself and nearby allies Slow Falling for 10 seconds.

**Augmentation card:**

> Grant yourself and nearby allies 10 seconds of Slow Falling.

### Flame Breath

**Weapons:** Emberfang Dagger, Infernal Claymore, Molten Axe

**Item tooltip:**

> Breathe a cone of fire that deals weapon damage and ignites enemies ahead.

**Augmentation card:**

> Breathe fire to damage and ignite enemies ahead.

### Flameborn

**Weapons:** Emberfang Dagger, Infernal Claymore, Molten Axe

**Item tooltip:**

> Gain Fire Resistance for 30 seconds and ignite yourself and enemies within 10 blocks.

**Augmentation card:**

> Gain Fire Resistance; ignite yourself and nearby enemies.

### Flaming Stomp

**Weapons:** Molten Axe

**Item tooltip:**

> Your next axe attack becomes a fiery 5x5 slam, dealing weapon damage and igniting enemies for 5 seconds.

**Augmentation card:**

> Your next axe attack becomes a fiery 5x5 area slam.

### Ghostwalk

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Hide yourself, armor and held items for 10 seconds, ending when you attack.

**Augmentation card:**

> Hide yourself, armor and held items for 10 seconds, ending when you attack.

**Super I-IV name (transforms at original IV):** Jumpscare Horror

### Gravity Well

**Weapons:** Nullified Axe

**Item tooltip:**

> Pull visible enemies within 6 blocks to a point ahead of you, then smash those that reach it for weapon damage.

**Augmentation card:**

> Pull enemies close, then smash them for weapon damage.

### Green Feast

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Consume 1 wheat to heal 1 heart and restore 2 saturation.

**Augmentation card:**

> Consume 1 wheat for 1 heart and 2 saturation.

### Just a Dream

**Weapons:** Dreambound Claymore, Dreamweaver Axe, Nightmare Dagger

**Item tooltip:**

> Teleport to your respawn point, or world spawn if it is unavailable.

**Augmentation card:**

> Teleport to your respawn point, or world spawn if unavailable.

### Live Wire

**Weapons:** Static Dagger

**Item tooltip:**

> For 8 seconds, melee attackers take 2 damage when they hit you.

**Augmentation card:**

> For 8 seconds, melee attackers take 2 damage on hit.

### Lucid Dreaming

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Your next dagger kill grants Speed II for 30 seconds and halves fall damage for 15 seconds.

**Augmentation card:**

> Your next kill grants Speed II and halves fall damage.

### Lullaby

**Weapons:** Dreambound Claymore

**Item tooltip:**

> Put a targeted entity to sleep for 3 seconds, until damaged. Players receive Slowness II instead.

**Augmentation card:**

> Put any mob to sleep until hit. Slows players instead.

### Magma Toss

**Weapons:** Molten Axe

**Item tooltip:**

> Throw a molten rock that bursts on impact, dealing weapon damage and igniting nearby enemies.

**Augmentation card:**

> Throw a molten rock that bursts and ignites nearby enemies.

### Magnetic Throw

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Launch an enemy within melee range in the direction you look.

**Augmentation card:**

> Launch a nearby enemy in your look direction.

### Null Receipt

**Weapons:** Nullified Axe

**Item tooltip:**

> Absorb knockback from five incoming hits, then release it with a vertical strike.

**Augmentation card:**

> Store knockback from five incoming hits; release it in a slam.

### Pocket Storm

**Weapons:** Skybreaker Axe

**Item tooltip:**

> Your next melee hit smashes nearby enemies for weapon damage and strong knockback.

**Augmentation card:**

> Your next hit creates an area smash with strong knockback.

### Poisoned Vein

**Weapons:** Leafcutter Dagger

**Item tooltip:**

> Your next dagger kill leaves a 5-second cloud. Everyone within 3 blocks, including you, receives Poison I for 30 seconds.

**Augmentation card:**

> Your next kill leaves a cloud that poisons everyone, including you.

### Ringing Steel

**Weapons:** Shrieking Claymore

**Item tooltip:**

> Your next melee hit deals 15 durability damage to the enemy's held weapon.

**Augmentation card:**

> Your next hit deals 15 durability damage to the enemy's weapon.

### Rooting Roots

**Weapons:** Leafcutter Dagger

**Item tooltip:**

> Root and poison hostile mobs within 10 blocks for 10 seconds.

**Augmentation card:**

> Root and poison nearby hostile mobs for 10 seconds.

### Skull Throw

**Weapons:** Ghost Axe

**Item tooltip:**

> Fire a fast wither skull up to 500 blocks. It bursts on impact for weapon damage and Wither I for 5 seconds.

**Augmentation card:**

> Fire a fast skull up to 500 blocks; bursts on impact for weapon damage and Wither.

### Sonic Boom

**Weapons:** Resonance Dagger

**Item tooltip:**

> Fire a piercing sonic blast up to 50 blocks for double attack damage. Blocks stop it.

**Augmentation card:**

> Fire a piercing sonic blast for double damage. Blocks stop it.

### Sonically Charged Crit

**Weapons:** Resonance Dagger

**Item tooltip:**

> Your next critical dagger hit adds 15% of the target's maximum health as damage.

**Augmentation card:**

> Your next crit adds 15% of the target's maximum health as damage.

### Static blade

**Weapons:** Static Dagger

**Item tooltip:**

> Your next swing sends a piercing echo up to 20 blocks for weapon damage. A matching offhand dagger sends a second echo.

**Augmentation card:**

> Your next swing fires an echo; matching dual daggers fire two.

### Stepping Through the Void

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Teleport up to 10 blocks in your look direction, stopping before obstacles. A matching offhand dagger doubles the range.

**Augmentation card:**

> Teleport 10 blocks ahead, or 20 with a matching offhand dagger.

### Storing Anger

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Store your damage instead of dealing it for 10 seconds, then release it on your next dagger hit.

**Augmentation card:**

> Store damage for 10 seconds; release it on your next hit.

### Strengthened Bones

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Your next dagger kill grants 10 seconds of invulnerability.

**Augmentation card:**

> Your next kill grants 10 seconds of invulnerability.

### Strong-arm

**Weapons:** Molten Axe

**Item tooltip:**

> Your next fully charged critical hit has a 30% chance to break one random armor piece.

**Augmentation card:**

> Your next fully charged critical hit can break one armor piece.

### Stunning Presence

**Weapons:** Claymore of Thunder

**Item tooltip:**

> For 30 seconds, repeatedly damage and briefly stun visible enemies within 10 blocks.

**Augmentation card:**

> Repeatedly damage and stun nearby enemies for 30 seconds.

### Sudden Presence

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Teleport behind the nearest hostile mob within 20 blocks.

**Augmentation card:**

> Teleport behind the nearest hostile mob within 20 blocks.

### Swing Attack

**Weapons:** Dreamweaver Axe, Echoing Axe, Ghost Axe, Molten Axe, Nullified Axe, Skybreaker Axe, Verdant Axe, Voltage Axe, Woundmaker Axe

**Item tooltip:**

> Strike all visible enemies within melee range for weapon damage.

**Augmentation card:**

> Strike all visible enemies within melee range.

### Thunderclap

**Weapons:** Claymore of Thunder, Static Dagger

**Item tooltip:**

> Dash to an aimed mob within 20 blocks with a full melee hit plus half weapon damage as lightning. Chain to the nearest unhit mob within 10 blocks of each victim until none remain.

**Augmentation card:**

> Dash and slash through nearby mobs; each hit adds half weapon damage as lightning.

### Upwind

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Your next melee hit launches the enemy upward with extra knockback.

**Augmentation card:**

> Your next hit launches the enemy with extra knockback.

### Vacuum Burst

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Pull nearby dropped items and experience toward you.

**Augmentation card:**

> Pull nearby drops and experience toward you.

### Void Pulse

**Weapons:** Claymore of the Void, Nullified Axe, Riftfang Dagger

**Item tooltip:**

> Release a burst that pushes nearby mobs away without dealing damage.

**Augmentation card:**

> Push nearby mobs away without dealing damage.

### Windcharge

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Launch upward and avoid damage on your next landing.

**Augmentation card:**

> Launch upward and avoid damage on your next landing.

**Axe super I-IV name (transforms at original IV):** Skybreaker

### Windy Dash

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Dash in the direction you look.

**Augmentation card:**

> Dash in the direction you look.

## Passives

### Aftershock

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Fully charged hits deal 1 extra damage after 1 second.

**Augmentation card:**

> Charged hits echo for 1 damage after 1 second.

### Backstab

**Weapons:** Assassin Dagger, Dead Calm Dagger, Deathwisper Dagger, Leafcutter Dagger, Nightmare Dagger, Resonance Dagger, Riftfang Dagger, Static Dagger

**Item tooltip:**

> Deal 20% more melee damage from behind.

**Augmentation card:**

> Deal 20% more melee damage from behind.

### Chain Lightning

**Weapons:** Static Dagger

**Item tooltip:**

> Hits can chain lightning between nearby living entities for half weapon damage. Each jump has a 50% chance, up to 10 jumps.

**Augmentation card:**

> Hits can chain lightning for half weapon damage.

### Charred Edge

**Weapons:** Emberfang Dagger

**Item tooltip:**

> Deal 15% more melee damage to enemies with Fire Resistance.

**Augmentation card:**

> Deal 15% more damage to enemies with Fire Resistance.

### Cold Bellows

**Weapons:** Infernal Claymore

**Item tooltip:**

> Shield blocks ignite the attacker, including projectile shooters.

**Augmentation card:**

> Shield blocks ignite attackers, including shooters.

### Compost

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Eat rotten flesh without getting Hunger.

**Augmentation card:**

> Eat rotten flesh without getting Hunger.

### Conductive Edge

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Deal 20% more melee damage to enemies in water or rain.

**Augmentation card:**

> Deal 20% more melee damage to wet enemies.

### Daydreamer

**Weapons:** Dreamweaver Axe

**Item tooltip:**

> Gain Jump Boost II during the day.

**Augmentation card:**

> Gain Jump Boost II during the day.

### Death March

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Deal more dagger damage as the target loses health, up to double damage.

**Augmentation card:**

> Deal more damage to wounded enemies, up to double.

### Death's Due

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Kills grant 25% more experience.

**Augmentation card:**

> Kills grant 25% more experience.

### Distance Tax

**Weapons:** Claymore of the Void

**Item tooltip:**

> Your last hit marks an enemy. After 10 seconds, it takes damage based on your distance, up to 40 damage.

**Augmentation card:**

> Mark on hit; distance deals damage after 10 seconds, up to 40.

### Double Jump

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Jump once in midair. Matching dual-wielded daggers can add more jumps.

**Augmentation card:**

> Jump once in midair. Matching dual-wielded daggers can add more jumps.

**Super I-IV name (transforms at original IV):** Windwalker

### Dual Wield

**Weapons:** Assassin Dagger, Dead Calm Dagger, Deathwisper Dagger, Emberfang Dagger, Leafcutter Dagger, Nightmare Dagger, Resonance Dagger, Riftfang Dagger, Rusty Dagger, Static Dagger

**Item tooltip:**

> Wield two daggers of the same material to attack with both. Gemstones may differ.

**Augmentation card:**

> Attack with two daggers of the same material.

### Echo Appetite

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Breaking sculk restores half a drumstick; killing a Warden restores 2 drumsticks.

**Augmentation card:**

> Restore hunger by breaking sculk or killing a Warden.

### Echo Salvage

**Weapons:** Echoing Axe

**Item tooltip:**

> A vertical strike that hits terrain but misses enemies stores bonus damage for your next successful slam, lasting 30 seconds.

**Augmentation card:**

> A slam that hits terrain stores bonus damage for the next slam.

### Echoing Speed

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Consecutive hits on the same enemy build attack speed. Changing targets or pausing for 8 seconds resets the bonus.

**Augmentation card:**

> Build attack speed by repeatedly hitting the same enemy.

### Final strike

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> Deal 50% more damage to your last hit enemy when it is below 10% health.

**Augmentation card:**

> Deal 50% more damage to your last hit enemy below 10% health.

### Fire Resistance

**Weapons:** Infernal Claymore, Molten Axe

**Item tooltip:**

> Gain Fire Resistance while held in your main hand.

**Augmentation card:**

> Fire Resistance in your main hand; at II, either hand.

### Grave Vault

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Store flowers in a nine-slot vault. Sneak-right-click to open it; kills and right-clicks on blocks plant stored flowers.

**Augmentation card:**

> Store flowers; sneak-right-click to open. Kills plant a flower.

### Headwind

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Sprinting consumes half as much hunger.

**Augmentation card:**

> Sprinting consumes half as much hunger.

### Heating Up

**Weapons:** Infernal Claymore

**Item tooltip:**

> Heal slowly instead of taking fire damage while burning. Does not work in lava.

**Augmentation card:**

> Fire heals you instead of hurting you. Excludes lava.

### Heavy Current

**Weapons:** Voltage Axe

**Item tooltip:**

> Deal 15% more melee damage to fully armored enemies.

**Augmentation card:**

> Deal 15% more melee damage to fully armored enemies.

### Homesick

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Standing on sculk grants Speed II and Strength I for 3 seconds.

**Augmentation card:**

> Standing on sculk grants Speed II and Strength I.

### Last Stand

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Survive a fatal hit with 1 HP once every 2 minutes.

**Augmentation card:**

> Survive a fatal hit with 1 HP once every 2 minutes.

### Leech

**Weapons:** Assassin Dagger

**Item tooltip:**

> Heal for 10% of the damage you deal with this dagger.

**Augmentation card:**

> Dagger damage heals you.

### Lightning Rod

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Attract lightning under open sky. Strikes grant 5 absorption hearts and Strength I instead of harming you.

**Augmentation card:**

> Attract harmless lightning for 5 absorption hearts and Strength I.

### Lonely Dream

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Gain 5% attack speed for each enemy within 10 blocks.

**Augmentation card:**

> Gain 5% attack speed per enemy within 10 blocks.

### Lumberjack

**Weapons:** Verdant Axe

**Item tooltip:**

> Breaking logs costs no durability.

**Augmentation card:**

> Breaking logs costs no durability.

### Lush Jungle

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Kills bonemeal the block beneath the enemy.

**Augmentation card:**

> Kills bonemeal the block beneath the enemy.

### Nether Born

**Weapons:** Infernal Claymore, Molten Axe

**Item tooltip:**

> Deal 20% more weapon damage in the Nether.

**Augmentation card:**

> Deal 20% more weapon damage in the Nether.

### Overwhelming Smash

**Weapons:** Dreamweaver Axe, Echoing Axe, Ghost Axe, Nullified Axe, Skybreaker Axe, Verdant Axe, Voltage Axe, Woundmaker Axe

**Item tooltip:**

> Break blocking shields and deal 20% more damage to armor durability.

**Augmentation card:**

> Break shields and deal 20% more armor durability damage.

### Photosynthesis

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Restore 1 weapon durability every 15 seconds in daylight.

**Augmentation card:**

> Slowly repair this weapon in daylight.

### Pillow Armor

**Weapons:** Dreamweaver Axe

**Item tooltip:**

> Take 25% less fall damage.

**Augmentation card:**

> Take 25% less fall damage.

### Pyromaniac

**Weapons:** Emberfang Dagger

**Item tooltip:**

> Hit a mob while burning to extinguish yourself and heal 2 HP.

**Augmentation card:**

> Hit while burning to extinguish yourself and heal 2 HP.

### Quick Recharge

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Fully charged critical hits shorten your primary cooldown by 1 second, once every 2 seconds.

**Augmentation card:**

> Charged crits reduce your primary cooldown by 1 second.

### Resonance Crit

**Weapons:** Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Fully charged hits critically strike jumping enemies, even while you are grounded.

**Augmentation card:**

> Fully charged hits critically strike jumping enemies.

### Revenge

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Deal 20% more dagger damage to undead.

**Augmentation card:**

> Deal 20% more damage to undead.

### Revengeful Exit

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> When killed by an enemy, deal weapon damage within 5 blocks and five times weapon damage to your killer.

**Augmentation card:**

> When killed by an enemy, blast nearby foes and deal fivefold damage to your killer.

### Sapling Harvest

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Kills have a 15% chance to drop an extra sapling.

**Augmentation card:**

> Kills have a 15% chance to drop an extra sapling.

### Scar Compass

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> Your most recently hit enemy glows for 10 minutes.

**Augmentation card:**

> Your last hit enemy glows for 10 minutes.

### Sleepwalker

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Deal 10% more melee damage at night.

**Augmentation card:**

> Deal 10% more melee damage at night.

### Slipstream

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Sprint behind a moving ally within 3 blocks to gain 10% movement speed.

**Augmentation card:**

> Sprint behind a moving ally for 10% more speed.

### Soul Hunger

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Kills grant 1 absorption heart, up to 3 hearts. These fade after 10 seconds without Soul Hunger equipped; switching back in time keeps them.

**Augmentation card:**

> Kills grant absorption, up to 3 hearts. Switch back within 10 seconds to keep them.

### Stone Song

**Weapons:** Shrieking Claymore

**Item tooltip:**

> Gain 2 armor below Y=0.

**Augmentation card:**

> Gain 2 armor below Y=0.

### Sunfed

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Sunlight restores 1 hunger and 1 saturation every 15 seconds.

**Augmentation card:**

> Sunlight restores hunger and saturation every 15 seconds.

### Unfinished Dream

**Weapons:** Dreambound Claymore, Dreamweaver Axe, Nightmare Dagger

**Item tooltip:**

> Killing an enemy before it damages you heals half a heart.

**Augmentation card:**

> Kill an enemy before it hurts you to heal half a heart.

### Vertical Strike

**Weapons:** Dreamweaver Axe, Echoing Axe, Ghost Axe, Molten Axe, Nullified Axe, Skybreaker Axe, Verdant Axe, Voltage Axe, Woundmaker Axe

**Item tooltip:**

> Three fully charged attacks within 10 seconds trigger an area slam. Requires an empty offhand.

**Augmentation card:**

> Three charged attacks trigger an area slam. Keep your offhand empty.

### Void Grip

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Your melee hits cause 25% less knockback.

**Augmentation card:**

> Your melee hits cause 25% less knockback.

### Warden Tone

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Your actions and projectiles do not alert sculk. Taking damage still does.

**Augmentation card:**

> Avoid alerting sculk, except when taking damage.
## Material-hit effects

These are learned separately through augmentation. Each material has one effect; the tooltip substitutes its name, effect strength and weapon-specific duration into the following in-game template:

> On hit: %1$s %2$s for %3$s seconds

The tables below expand that template into the actual English item descriptions for every registered special weapon variant. Roman numerals here are the status-effect strength, not the augmentation rank.

### Glowing Hit (Ignisium)

**Weapons:** Emberfang Dagger, Infernal Claymore, Molten Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Glowing II for 53.35 seconds | On hit: Glowing II for 100.00 seconds | On hit: Glowing II for 177.80 seconds |
| Ruby | On hit: Glowing II for 53.35 seconds | On hit: Glowing II for 100.00 seconds | On hit: Glowing II for 177.80 seconds |
| Amber | On hit: Glowing II for 53.35 seconds | On hit: Glowing II for 100.00 seconds | On hit: Glowing II for 177.80 seconds |
| Amethyst | On hit: Glowing II for 58.80 seconds | On hit: Glowing II for 100.00 seconds | On hit: Glowing II for 153.85 seconds |
| Jade | On hit: Glowing III for 53.85 seconds | On hit: Glowing III for 101.00 seconds | On hit: Glowing III for 179.55 seconds |

### Blindness Hit (Inanisium)

**Weapons:** Riftfang Dagger, Claymore of the Void, Nullified Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Blindness II for 2.15 seconds | On hit: Blindness II for 4.00 seconds | On hit: Blindness II for 7.10 seconds |
| Ruby | On hit: Blindness II for 2.15 seconds | On hit: Blindness II for 4.00 seconds | On hit: Blindness II for 7.10 seconds |
| Amber | On hit: Blindness II for 2.15 seconds | On hit: Blindness II for 4.00 seconds | On hit: Blindness II for 7.10 seconds |
| Amethyst | On hit: Blindness II for 2.35 seconds | On hit: Blindness II for 4.00 seconds | On hit: Blindness II for 6.15 seconds |
| Jade | On hit: Blindness III for 2.65 seconds | On hit: Blindness III for 5.00 seconds | On hit: Blindness III for 8.90 seconds |

### Nausea Hit (Somnium)

**Weapons:** Nightmare Dagger, Dreambound Claymore, Dreamweaver Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Nausea V for 5.35 seconds | On hit: Nausea V for 10.00 seconds | On hit: Nausea V for 17.80 seconds |
| Ruby | On hit: Nausea V for 5.35 seconds | On hit: Nausea V for 10.00 seconds | On hit: Nausea V for 17.80 seconds |
| Amber | On hit: Nausea V for 5.35 seconds | On hit: Nausea V for 10.00 seconds | On hit: Nausea V for 17.80 seconds |
| Amethyst | On hit: Nausea V for 5.90 seconds | On hit: Nausea V for 10.00 seconds | On hit: Nausea V for 15.40 seconds |
| Jade | On hit: Nausea VI for 5.85 seconds | On hit: Nausea VI for 11.00 seconds | On hit: Nausea VI for 19.55 seconds |

### Slowness Hit (Electrium)

**Weapons:** Static Dagger, Claymore of Thunder, Voltage Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Slowness IV for 2.65 seconds | On hit: Slowness IV for 5.00 seconds | On hit: Slowness IV for 8.90 seconds |
| Ruby | On hit: Slowness IV for 2.65 seconds | On hit: Slowness IV for 5.00 seconds | On hit: Slowness IV for 8.90 seconds |
| Amber | On hit: Slowness IV for 2.65 seconds | On hit: Slowness IV for 5.00 seconds | On hit: Slowness IV for 8.90 seconds |
| Amethyst | On hit: Slowness IV for 2.95 seconds | On hit: Slowness IV for 5.00 seconds | On hit: Slowness IV for 7.70 seconds |
| Jade | On hit: Slowness V for 3.20 seconds | On hit: Slowness V for 6.00 seconds | On hit: Slowness V for 10.65 seconds |

### Levitation Hit (Taifunite)

**Weapons:** Dead Calm Dagger, Storming Claymore, Skybreaker Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Levitation III for 1.60 seconds | On hit: Levitation III for 3.00 seconds | On hit: Levitation III for 5.35 seconds |
| Ruby | On hit: Levitation III for 1.60 seconds | On hit: Levitation III for 3.00 seconds | On hit: Levitation III for 5.35 seconds |
| Amber | On hit: Levitation III for 1.60 seconds | On hit: Levitation III for 3.00 seconds | On hit: Levitation III for 5.35 seconds |
| Amethyst | On hit: Levitation III for 1.75 seconds | On hit: Levitation III for 3.00 seconds | On hit: Levitation III for 4.60 seconds |
| Jade | On hit: Levitation IV for 2.15 seconds | On hit: Levitation IV for 4.00 seconds | On hit: Levitation IV for 7.10 seconds |

### Wither Hit (Vulnusium)

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Wither II for 4.80 seconds | On hit: Wither II for 9.00 seconds | On hit: Wither II for 16.00 seconds |
| Ruby | On hit: Wither II for 4.80 seconds | On hit: Wither II for 9.00 seconds | On hit: Wither II for 16.00 seconds |
| Amber | On hit: Wither II for 4.80 seconds | On hit: Wither II for 9.00 seconds | On hit: Wither II for 16.00 seconds |
| Amethyst | On hit: Wither II for 5.30 seconds | On hit: Wither II for 9.00 seconds | On hit: Wither II for 13.85 seconds |
| Jade | On hit: Wither III for 5.35 seconds | On hit: Wither III for 10.00 seconds | On hit: Wither III for 17.80 seconds |

### Hunger Hit (Overgrown / Lush)

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Hunger IV for 5.35 seconds | On hit: Hunger IV for 10.00 seconds | On hit: Hunger IV for 17.80 seconds |
| Ruby | On hit: Hunger IV for 5.35 seconds | On hit: Hunger IV for 10.00 seconds | On hit: Hunger IV for 17.80 seconds |
| Amber | On hit: Hunger IV for 5.35 seconds | On hit: Hunger IV for 10.00 seconds | On hit: Hunger IV for 17.80 seconds |
| Amethyst | On hit: Hunger IV for 5.90 seconds | On hit: Hunger IV for 10.00 seconds | On hit: Hunger IV for 15.40 seconds |
| Jade | On hit: Hunger V for 5.85 seconds | On hit: Hunger V for 11.00 seconds | On hit: Hunger V for 19.55 seconds |

### Weakness Hit (Morsium)

**Weapons:** Deathwisper Dagger, Hollow Claymore, Ghost Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Weakness IV for 2.65 seconds | On hit: Weakness IV for 5.00 seconds | On hit: Weakness IV for 8.90 seconds |
| Ruby | On hit: Weakness IV for 2.65 seconds | On hit: Weakness IV for 5.00 seconds | On hit: Weakness IV for 8.90 seconds |
| Amber | On hit: Weakness IV for 2.65 seconds | On hit: Weakness IV for 5.00 seconds | On hit: Weakness IV for 8.90 seconds |
| Amethyst | On hit: Weakness IV for 2.95 seconds | On hit: Weakness IV for 5.00 seconds | On hit: Weakness IV for 7.70 seconds |
| Jade | On hit: Weakness V for 3.20 seconds | On hit: Weakness V for 6.00 seconds | On hit: Weakness V for 10.65 seconds |

### Darkness Hit (Pulsite)

**Weapons:** Resonance Dagger, Shrieking Claymore, Echoing Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | On hit: Darkness VI for 3.75 seconds | On hit: Darkness VI for 7.00 seconds | On hit: Darkness VI for 12.45 seconds |
| Ruby | On hit: Darkness VI for 3.75 seconds | On hit: Darkness VI for 7.00 seconds | On hit: Darkness VI for 12.45 seconds |
| Amber | On hit: Darkness VI for 3.75 seconds | On hit: Darkness VI for 7.00 seconds | On hit: Darkness VI for 12.45 seconds |
| Amethyst | On hit: Darkness VI for 4.10 seconds | On hit: Darkness VI for 7.00 seconds | On hit: Darkness VI for 10.75 seconds |
| Jade | On hit: Darkness VII for 4.25 seconds | On hit: Darkness VII for 8.00 seconds | On hit: Darkness VII for 14.20 seconds |
