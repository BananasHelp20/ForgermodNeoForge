# In-game weapon abilities

English descriptions from the game language file and actual registered pools: 60 active abilities, 49 passives, and nine material-hit effects. Unimplemented proposals are excluded.

Special weapons learn these abilities through augmentation. Each assignment includes its base, Ruby, Amber, Amethyst and Jade variants. Item descriptions appear while holding Shift. Most text below is a historical base learning summary; see [the reviewed implementation queue](ability-review-implementation.md) for the current approved changes. The shared AbilityDescriptions implementation is authoritative for current in-game wording: gray prose and white current-level values, including cooldowns. Base abilities show a separate Shift-only super-upgrade notice below their cooldown, without describing future super effects. Upgrade cards compare ranks and highlight changed values in white.

## Active abilities

### Absolute Nightmare

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Your next two dagger hits inflict Weakness IV and Darkness V for 5 seconds.

**Augmentation card:**

> Your next two dagger hits inflict Weakness IV and Darkness V for 5 seconds.

### Alternating Current

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Aim at 2 mobs within 20 blocks of each other and activate once for each to link them for 1 minute. The current deals 2 damage and stuns both for 0.5 seconds each time it jumps.

**Augmentation card:**

> Mark 2 mobs. For 1 minute, each jump deals 2 damage and stuns both for 0.5 s.

### Always the Gardeners

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Your next kill will create a poison cloud that stays for 5 minutes. Everyone besides you receives Poison I while inside the 2-block radius.

**Augmentation card:**

> Next kill creates a 5-minute, 2-block radius cloud. Applies Poison I to others.

### Area Discharge

**Weapons:** Static Dagger

**Item tooltip:**

> Activating this ability strikes hostile mobs within a 5 block radius with lightning, dealing half your weapon damage.

**Augmentation card:**

> Strike hostile mobs within 5 blocks for half weapon damage.

Anger of Zeus uses initial wave radii of 5/10/15/20 blocks at super levels I/II/III/IV. Lightning chains still reach 10 blocks from each previous target.

### Blood Rush

**Weapons:** Assassin Dagger, Curseblood Claymore

**Item tooltip:**

> On activation pays 1 heart to gain speed II for 10 seconds.

**Augmentation card:**

> On activation pays 1 heart to gain speed II for 10 seconds.

### Blood Sacrafice

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> Requires more than 4 HP. Sacrifice half of your total HP to deal double weapon damage for 15 seconds.

**Augmentation card:**

> Requires more than 4 HP. Sacrifice half of your total HP to deal double weapon damage for 15 seconds.

### Bone Guard

**Weapons:** Ghost Axe, Hollow Claymore

**Item tooltip:**

> Activate this ability to summon 1 Skeleton that will fight for you in battle. Reinforcements disappear after 10 minutes

**Augmentation card:**

> Summon 1 Skeleton to fight for you for 10 minutes.

**Super I-IV name (transforms at original IV):** Necromancer

**Necromancer tooltip:**

> Summon {reinforcements} that fight for you. Customize reinforcements using {configured customization key}.

The roster placeholder resolves to the actual selected reinforcement names/counts in white. Capacity is4/6/8/10; real equipment unlocks at II and retains wear/enchantments between summons. Skeleton Horse costs3 including its rider; Zombie Horse and bosses are excluded. Shift+right-click opens by default, configurable under Customize Necromancer Reinforcements in Controls (Shift+Alt+S still rotates abilities). Editing requires no living reinforcements. All groups last10 minutes and are replaced on a ready recast; switching weapons does not despawn them.

### Challenge Roar

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Agro all hostile mobs within 16 blocks on you, and giving you strength I for 10 seconds. Agroed enemis will not target any other entitiy during that time.

**Augmentation card:**

> Force hostile mobs within 16 blocks to target you. Gain Strength I for 10 s.

### Charge Attack

**Weapons:** Static Dagger

**Item tooltip:**

> Progressively gain +100% damage in the next 20 hits. The last hit fully charges a lightning attack dealing 20 damage.

**Augmentation card:**

> Build +100% damage over 20 hits. Final hit strikes lightning for 20 damage.

### Cinder Decoy

**Weapons:** Infernal Claymore

**Item tooltip:**

> Creates a decoy that atrackts all hostile mobs for 600 seconds. When it disappears or is being dealt the player's health as total damage, the decoy explodes in a fire explosion with a radius of 2 blocks, making enemies burn for 3 seconds.

**Augmentation card:**

> Create a decoy. Defeat or expiry: fiery blast within 2 blocks; ignites for 3 s.

### Crosswind

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Deflect incoming projectiles away from you for 2 minutes. Your own shots are unaffected. Cooldown: 180 seconds.

**Augmentation card:**

> Deflect incoming projectiles away from you for 2 minutes. Your own shots are unaffected. Cooldown: 180 seconds.

### Crucible Hook

**Weapons:** Molten Axe

**Item tooltip:**

> Activate the ability while aiming at a target mob. You will pull the mob to you and automatically hit with a smash attack dealing 21.25 damage. The smash will also set the enemy ablaze. Burn duration: 3 seconds.

**Augmentation card:**

> Pull a mob within 20 blocks to you. Smash for 21.25 damage and ignite for 3 s.

### Death Stare

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Teleport beside the mob you aim at when activating the ability. The mob has to be within a distance of 30 blocks.

**Augmentation card:**

> Teleport beside the mob you aim at when activating the ability. The mob has to be within a distance of 30 blocks.

### Deep Wound

**Weapons:** Assassin Dagger

**Item tooltip:**

> Your next critical hit deals double damage and inflicts Slowness III for 5 seconds.

**Augmentation card:**

> Your next critical hit deals double damage and inflicts Slowness III for 5 seconds.

### Delusion

**Weapons:** Dreambound Claymore, Dreamweaver Axe, Nightmare Dagger

**Item tooltip:**

> Your next hit stops the mob from targeting anything for 10 seconds.

**Augmentation card:**

> Your next hit stops the mob from targeting anything for 10 seconds.

### Direct Current

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Mark up to 2 mobs by aiming at them and pressing the ability activation key. By marking them, they get connected one by one, forming a constant current that connects them. Enemies entering/passing the current are being dealt 4 damage every 0.5s.

**Augmentation card:**

> Mark 2 mobs. Electrical barriers damage and stun mobs between them.

**Super I-IV name (transforms at original IV):** Static Field

### Displace

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Aim at any living entity within 10 blocks to swap places with it when activating the ability while aiming at it.

**Augmentation card:**

> Aim at any living entity within 10 blocks to swap places with it when activating the ability while aiming at it.

### Downdraft Bell

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Force all flying mobs within a 4 block radius to the ground. (phantom's worst nightmare)

**Augmentation card:**

> Force all flying mobs within a 4 block radius to the ground. (phantom's worst nightmare)

### Dream Feast

**Weapons:** Dreambound Claymore

**Item tooltip:**

> Instantly gain 1 hunger bars.

**Augmentation card:**

> Instantly gain 1 hunger bars.

### Echo Hearing

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> In the next 10 seconds after activating this ability, you will be able to hear sounds made from all entities within a 32 block radius. After entities make a sound, they glow for 1 seconds.

**Augmentation card:**

> For 10 s, hear noises within 32 blocks. Highlights noisy entities only for you.

### Gravity Swing

**Weapons:** Nullified Axe

**Item tooltip:**

> Swing at all mobs within a 2 block radius, lifting them up to 10 blocks or the highest clear space below a ceiling. Increase their gravity and fall damage by +50%.

**Augmentation card:**

> Lift all mobs within 2 blocks by up to 10 blocks. Gravity and fall damage +50%.

### Explosion

**Weapons:** Infernal Claymore

**Item tooltip:**

> Explode without harming yourself or blocks, and gain Resistance IV for 2 seconds.

**Augmentation card:**

> Explode safely and gain Resistance IV for 2 seconds.

### Explosive Hits

**Weapons:** Infernal Claymore

**Item tooltip:**

> Your next 10 melee hits create small blasts that deal an additional 30% areal damage. The areal blast has a radius of 3 blocks around the hit enemy.

**Augmentation card:**

> Next 10 melee hits add 30% area damage in a 3-block radius blast.

### Eye of the Storm

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> you create a small storm around you with you being the center. This storm affects all enemies within a 5 block radius and lasts 5 seconds.

**Augmentation card:**

> Create a storm affecting mobs within 5 blocks for 5 s.

### False Awakening

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Create a decoy directly beside the enemy you aim at. When that enemy is distracted, you deal 10% more damage to that enemy for a duration of 30 seconds.

**Augmentation card:**

> Decoy distracts your aimed enemy. Deal 10% more damage to it for 30 s.

### Feather Shelter

**Weapons:** Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Give yourself and allies within 4 blocks slow falling for 10 seconds.

**Augmentation card:**

> Give yourself and allies within 4 blocks slow falling for 10 seconds.

### Flame Breath

**Weapons:** Emberfang Dagger, Infernal Claymore, Molten Axe

**Item tooltip:**

> Incinerate the area in front of you with a range of 7 blocks, igninting enemies for 3 seconds.

**Augmentation card:**

> Incinerate the area in front of you with a range of 7 blocks, igninting enemies for 3 seconds.

### Flameborn

**Weapons:** Emberfang Dagger, Infernal Claymore, Molten Axe

**Item tooltip:**

> You gain fire resistance for 30 seconds, and explode in a burst of fire dealing 7 damage and setting all entities within 10 blocks ablaze for 5 seconds.

**Augmentation card:**

> Fire resistance for 30 s. Explode for 7 damage within 10 blocks; ignite for 5 s.

### Flaming Stomp

**Weapons:** Molten Axe

**Item tooltip:**

> your next axe attack becomes a vertical 5x5 strike dealing 17 damage and setting enemies ablaze for 5 seconds.

**Augmentation card:**

> your next axe attack becomes a vertical 5x5 strike dealing 17 damage and setting enemies ablaze for 5 seconds.

### Ghostwalk

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Become invisible for 10 seconds or until striking an enemy.

**Augmentation card:**

> Become invisible for 10 seconds or until striking an enemy.

**Super I-IV name (transforms at original IV):** Jumpscare Horror

### Gravity Well

**Weapons:** Nullified Axe

**Item tooltip:**

> Aim at an enemy and activate the ability to pull an enemy within 6 blocks to you and immediately strike it with a vertical strike.

**Augmentation card:**

> Aim at an enemy and activate the ability to pull an enemy within 6 blocks to you and immediately strike it with a vertical strike.

### Green Feast

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> remove 1 wheat from your inventory to heal 2 health and gain 1 hungerbars.

**Augmentation card:**

> remove 1 wheat from your inventory to heal 2 health and gain 1 hungerbars.

### Just a Dream

**Weapons:** Dreambound Claymore, Dreamweaver Axe, Nightmare Dagger

**Item tooltip:**

> Immediately teleport to your respawn point across all dimensions. if it's not available, return to the world spawn.

**Augmentation card:**

> Teleport to your respawn point. Uses world spawn if unavailable.

### Live Wire

**Weapons:** Static Dagger

**Item tooltip:**

> In the next 8 seconds, enemies that hit you take 1 hearts damage in return.

**Augmentation card:**

> In the next 8 seconds, enemies that hit you take 1 hearts damage in return.

### Lucid Dreaming

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Your next kill grants Speed II and 50% less fall damage for 30 seconds.

**Augmentation card:**

> Your next kill grants Speed II and 50% less fall damage for 30 seconds.

### Lullaby

**Weapons:** Dreambound Claymore

**Item tooltip:**

> Aim at a mob within 10 blocks to put it to sleep for 3 seconds. Any form of damage wakes it.

**Augmentation card:**

> Aim at a mob within 10 blocks to put it to sleep for 3 seconds. Any form of damage wakes it.

### Magma Toss

**Weapons:** Molten Axe

**Item tooltip:**

> Throw a molten rock that bursts within 2 blocks, dealing weapon damage and igniting enemies for 3 seconds.

**Augmentation card:**

> Burst within 2 blocks; weapon damage, burning for 3 seconds.

### Magnetic Throw

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Aim at an enemy and activate this ability to launch the enemy in the direction you look.

**Augmentation card:**

> Aim at an enemy and activate this ability to launch the enemy in the direction you look.

### Null Receipt

**Weapons:** Nullified Axe

**Item tooltip:**

> Absorb knockback for the next 5 incoming hits, then release it with the next vertical strike.

**Augmentation card:**

> Absorb knockback for the next 5 incoming hits, then release it with the next vertical strike.

### Pocket Storm

**Weapons:** Skybreaker Axe

**Item tooltip:**

> Your next hit smashes enemies with strong knockback.

**Augmentation card:**

> Your next hit smashes enemies with strong knockback.

### Poisoned Vein

**Weapons:** Leafcutter Dagger

**Item tooltip:**

> Your next dagger kill leaves a 5-second cloud. Everyone besides you within 3 blocks receives Poison I for 30 seconds.

**Augmentation card:**

> Next dagger kill: 5-second cloud within 3 blocks. Poisons others for 30 s.

### Ringing Steel

**Weapons:** Shrieking Claymore

**Item tooltip:**

> Your next hit will damage the enemy's held weapon(s) for 15 durability.

**Augmentation card:**

> Your next hit will damage the enemy's held weapon(s) for 15 durability.

### Rooting Roots

**Weapons:** Leafcutter Dagger

**Item tooltip:**

> Activate this ability for roots shooting out of the ground that immobilize and poison all enemies within 10 blocks for 10 seconds.

**Augmentation card:**

> Root and poison enemies within 10 blocks for 10 s.

### Skull Throw

**Weapons:** Ghost Axe

**Item tooltip:**

> Fire a fast skull projectile that bursts within 2 blocks and applies Wither I for 10 seconds.

**Augmentation card:**

> Fire a fast skull projectile that bursts within 2 blocks and applies Wither I for 10 seconds.

### Sonic Boom

**Weapons:** Resonance Dagger

**Item tooltip:**

> Activate this ability to fire a piercing Sonic Boom with a 50 block range for 28 damage.

**Augmentation card:**

> Activate this ability to fire a piercing Sonic Boom with a 50 block range for 28 damage.

### Sonically Charged Crit

**Weapons:** Resonance Dagger

**Item tooltip:**

> Your next critical dagger hit adds 5% of the target's maximum health as damage.

**Augmentation card:**

> Your next critical dagger hit adds 5% of the target's maximum health as damage.

### Static blade

**Weapons:** Static Dagger

**Item tooltip:**

> Your next swing sends out a piercing echo with 20 blocks range for 14 damage, this also works in dual wield.

**Augmentation card:**

> Your next swing sends out a piercing echo with 20 blocks range for 14 damage, this also works in dual wield.

### Stepping Through the Void

**Weapons:** Riftfang Dagger

**Item tooltip:**

> On activation, teleport 5 blocks in the direction you look. Having this ability on both daggers in dual wield doubles the range.

**Augmentation card:**

> Blink 5 blocks in your view direction. Both daggers need this ability to double range.

### Storing Anger

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> In the next 10 seconds, you won't deal any damage since it will be stored for the next hit after those 10 seconds end.

**Augmentation card:**

> Store damage for 10 seconds, then release it on your next hit.

### Strengthened Bones

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Your next dagger kill grants 10 seconds of absolute invulnerability.

**Augmentation card:**

> Your next dagger kill grants 10 seconds of absolute invulnerability.

### Strong-arm

**Weapons:** Molten Axe

**Item tooltip:**

> Your next critical hit has a 30% chance to break a random armor piece of the enemy.

**Augmentation card:**

> Next critical hit: 30% chance to break one enemy armor piece.

### Stunning Presence

**Weapons:** Claymore of Thunder

**Item tooltip:**

> Create an area (10 blocks radius) around you where you stun every entity for a short time every 2 seconds.

**Augmentation card:**

> Stun other living entities within 10 blocks every 2 s.

### Sudden Presence

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Teleport behind the nearest hostile mob within 10 blocks

**Augmentation card:**

> Teleport behind the nearest hostile mob within 10 blocks

### Swing Attack

**Weapons:** Dreamweaver Axe, Echoing Axe, Ghost Axe, Molten Axe, Nullified Axe, Skybreaker Axe, Verdant Axe, Voltage Axe, Woundmaker Axe

**Item tooltip:**

> Strike all enemies within melee range for 17 damage.

**Augmentation card:**

> Strike all enemies within melee range for 17 damage.

### Thunderclap

**Weapons:** Claymore of Thunder, Static Dagger

**Item tooltip:**

> Aim at a mob within 10 blocks and dash to it with a lightning slash. Deal a full melee hit plus half weapon lightning damage. Chain to the nearest unhit mob within 10 blocks of each previous target until none remain.

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

> Pull items and xp within a 6 blocks radius around you into your inventory.

**Augmentation card:**

> Pull items and xp within a 6 blocks radius around you into your inventory.

### Void Pulse

**Weapons:** Claymore of the Void, Nullified Axe, Riftfang Dagger

**Item tooltip:**

> Push living entities in a 4 block radius away from you.

**Augmentation card:**

> Push living entities in a 4 block radius away from you.

### Windcharge

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Launch about 5 blocks upward and negate falldamage on your next landing.

**Augmentation card:**

> Launch about 5 blocks upward and negate falldamage on your next landing.

**Axe super I-IV name (transforms at original IV):** Skybreaker

### Windy Dash

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Dash into the direction you look.

**Augmentation card:**

> Dash into the direction you look.

## Passives

### Aftershock

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Fully charged hits deal 1 delayed extra damage after 1 second.

**Augmentation card:**

> Fully charged hits deal 1 delayed extra damage after 1 second.

### Backstab

**Weapons:** Assassin Dagger, Dead Calm Dagger, Deathwisper Dagger, Leafcutter Dagger, Nightmare Dagger, Resonance Dagger, Riftfang Dagger, Static Dagger

**Item tooltip:**

> Deal 20% more damage from behind.

**Augmentation card:**

> Deal 20% more damage from behind.

### Chain Lightning

**Weapons:** Static Dagger

**Item tooltip:**

> Hits have a 50% chance to chain to nearby enemies on hit. A chain lightning can travel up to 10 mobs.

**Augmentation card:**

> Hits have a 50% chance to chain to nearby enemies on hit. A chain lightning can travel up to 10 mobs.

### Charred Edge

**Weapons:** Emberfang Dagger

**Item tooltip:**

> Deal 15% more damage to enemies with Fire Resistance.

**Augmentation card:**

> Deal 15% more damage to enemies with Fire Resistance.

### Cold Bellows

**Weapons:** Infernal Claymore

**Item tooltip:**

> Blocking an attack with a shield ignites the attacker for 3 seconds.

**Augmentation card:**

> Blocking an attack with a shield ignites the attacker for 3 seconds.

### Compost

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Eating rotten flesh doesn't give you the Hunger effect.

**Augmentation card:**

> Eating rotten flesh doesn't give you the Hunger effect.

### Conductive Edge

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Deal 20% more damage to enemies exposed to water.

**Augmentation card:**

> Deal 20% more damage to enemies exposed to water.

### Daydreamer

**Weapons:** Dreamweaver Axe

**Item tooltip:**

> Gain Jump Boost II during the day.

**Augmentation card:**

> Gain Jump Boost II during the day.

### Death March

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Deal more damage depending on the targets health. Up to 50% more damage.

**Augmentation card:**

> Deal more damage depending on the targets health. Up to 50% more damage.

### Death's Due

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Enemy kills grant 25% more xp.

**Augmentation card:**

> Enemy kills grant 25% more xp.

### Distance Tax

**Weapons:** Claymore of the Void

**Item tooltip:**

> Activate to mark the next mob you hit. After 10 seconds, it takes 0.5 damage per block of separation, up to 40 damage.

**Augmentation card:**

> Arm next hit; after 10 seconds, deal 0.5 damage per block of separation; max 40.

### Double Jump

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Gain the ability to do 1 more jump in midair. Matching dual-wielded daggers combine their jumps.

**Augmentation card:**

> Gain the ability to do 1 more jump in midair. Matching dual-wielded daggers combine their jumps.

**Super I-IV name (transforms at original IV):** Windwalker

### Dual Wield

**Weapons:** Assassin Dagger, Dead Calm Dagger, Deathwisper Dagger, Emberfang Dagger, Leafcutter Dagger, Nightmare Dagger, Resonance Dagger, Riftfang Dagger, Rusty Dagger, Static Dagger

**Item tooltip:**

> Allows you to dual wield daggers of the same material. Only one dagger needs this passive.

**Augmentation card:**

> Dual wield matching-material daggers. Only one needs this passive.

### Echo Appetite

**Weapons:** Echoing Axe, Resonance Dagger, Shrieking Claymore (all gemstone variants)

**Item tooltip / augmentation card:**

> Breaking Sculk restores hunger based on what sculk block you broke. Killing a Warden also restores {hungerBars} hunger bars.

The complete Warden reward is white: 2 hunger bars at I, 4 at II. Other prose is light gray. Maximum II, no super form. Sculk rewards in hunger points at I/II: Sculk2/4, Vein1/2, Sensor3/6, Calibrated Sensor4/8, Catalyst6/12, Shrieker4/8. No saturation is added.

### Echo Salvage

**Weapons:** Echoing Axe

**Item tooltip:**

> If you miss with a Vertical Strike, it stores half it's damage which is being released on the next successful melee hit with an automatic Vertical Strike. Up to 2 Strike Echoes stay stored in this weapon.

**Augmentation card:**

> Missed Vertical Strikes store half damage. Next melee hit releases echoes. Capacity: 2 echoes.

### Echoing Speed

**Weapons:** Riftfang Dagger

**Item tooltip:**

> Consecutive hits on the same enemy grant 5% attack speed per hit, up to 20% more attack speed. Changing targets or not hitting for 8 seconds resets the bonus.

**Augmentation card:**

> Same target: +5% speed per hit, up to +20%. Resets on target change or after 8 s.

### Final strike

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> Deal 50% more damage to the last enemy you hit if it has less than 10% health.

**Augmentation card:**

> Deal 50% more damage to your last target below 10% health.

### Fire Resistance

**Weapons:** Infernal Claymore, Molten Axe

**Item tooltip:**

> Gain Fire Resistance I while holding this weapon in your main hand.

**Augmentation card:**

> Gain Fire Resistance I while holding this weapon in your main hand.

### Grave Vault

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe (all gemstone variants)

**Item tooltip / augmentation card:**

> Sneak-right-click to open a nine-slot flower vault. Enemy kills plant a stored flower where the enemy stood, if possible.

Entirely light gray. Ordinary block right-clicks do not plant or consume stored flowers. One level, no super form or cooldown.

### Headwind

**Weapons:** Dead Calm Dagger, Skybreaker Axe, Storming Claymore

**Item tooltip:**

> Sprinting consumes half as much hunger.

**Augmentation card:**

> Sprinting consumes half as much hunger.

### Heating Up

**Weapons:** Infernal Claymore

**Item tooltip:**

> While burning, heal 1 HP every 2 seconds instead of taking fire damage. Does not work in lava.

**Augmentation card:**

> While burning, heal 1 HP every 2 seconds instead of taking fire damage. Does not work in lava.

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

> Standing on sculk grants Speed II and Strength I for 3 seconds.

### Last Stand

**Weapons:** Deathwisper Dagger, Hollow Claymore, Ghost Axe (all gemstone variants)

**Item tooltip / augmentation card:**

> Survive a fatal hit with 1 HP once every {cooldown}.

Complete1HP and cooldown phrases white; remaining prose light gray. Levels I/II/III:2minutes/1.5minutes/1minute. MaximumIII, no super form. Cooldown belongs to the wielder and does not reset with weapon swaps or respawn. Requires more than1HP before the lethal hit.

### Leech

**Weapons:** Assassin Dagger

**Item tooltip:**

> Melee hits heal you for 10% of the damage dealt.

**Augmentation card:**

> Melee hits heal you for 10% of the damage dealt.

### Lightning Rod

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Attract lightning under open sky, averaging one strike every 600 s. Strikes grant 5 absorption hearts and Strength I for 30 seconds instead of harming you.

**Augmentation card:**

> Lightning averages every 600 s; grants absorption and Strength.

### Lonely Dream

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Gain 5% attack speed for each enemy within 10 blocks.

**Augmentation card:**

> Gain 5% attack speed for each enemy within 10 blocks.

### Lumberjack

**Weapons:** Verdant Axe

**Item tooltip:**

> Breaking logs costs no durability.

**Augmentation card:**

> Breaking logs costs no durability.

### Lush Jungle

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Enemy kills bonemeal the block beneath them, if possible.

**Augmentation card:**

> Enemy kills bonemeal the block beneath them, if possible.

### Nether Born

**Weapons:** Infernal Claymore, Molten Axe

**Item tooltip:**

> Deal 6% more damage in the Nether.

**Augmentation card:**

> Deal 6% more damage in the Nether.


**Levels I-V:** 6% / 12% / 18% / 24% / 30% more damage. The complete percentage + "more damage" phrase is white; remaining text is light gray.

### Overwhelming Smash

**Weapons:** Dreamweaver Axe, Echoing Axe, Ghost Axe, Nullified Axe, Skybreaker Axe, Verdant Axe, Voltage Axe, Woundmaker Axe

**Item tooltip:**

> Break and damage blocking enemies and deal 20% more durability damage to armor.

**Augmentation card:**

> Break and damage blocking enemies and deal 20% more durability damage to armor.

### Photosynthesis

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Restore 1 weapon durability every 15 seconds in daylight.

**Augmentation card:**

> Restore 1 weapon durability every 15 seconds in daylight.


**Levels I-IV:** 1 / 2 / 3 / 5 durability every 15 seconds. Complete repair amount + "weapon durability" and "15 seconds" are white; remaining text is light gray.

### Pillow Armor

**Weapons:** Dreamweaver Axe

**Item tooltip:**

> Take 25% less fall damage.

**Augmentation card:**

> Take 25% less fall damage.

### Pyromaniac

**Weapons:** Emberfang Dagger

**Item tooltip:**

> Hit a mob while burning to extinguish yourself and heal 1 HP.

**Augmentation card:**

> Hit a mob while burning to extinguish yourself and heal 1 HP.


**Levels I-IV:** 1 / 2 / 4 / 6 HP. Complete "heal {hp} HP" phrase is white; remaining text is light gray.

### Quick Recharge

**Weapons:** Claymore of Thunder, Static Dagger, Voltage Axe

**Item tooltip:**

> Fully charged critical hits reduce your primary ability's cooldown by 1 second once every 2 seconds.

**Augmentation card:**

> Charged critical hits: primary cooldown -1 second. Once every 2 seconds.

### Resonance Crit

**Weapons:** Resonance Dagger, Shrieking Claymore

**Item tooltip:**

> Fully charged hits critically strike jumping enemies, even while you are grounded.

**Augmentation card:**

> Fully charged hits critically strike jumping enemies.

### Revenge

**Weapons:** Deathwisper Dagger

**Item tooltip:**

> Deal 6% more damage to undead.

**Augmentation card:**

> Deal 6% more damage to undead.


**Levels I-V:** 6% / 12% / 18% / 24% / 30% more damage. Complete percentage + "more damage" phrase is white; remaining text is light gray.

### Revengeful Exit

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> When killed by an enemy, deal 14 damage within 5 blocks and an additional 70 damage to the killer.

**Augmentation card:**

> On death: 14 damage within 5 blocks, plus 70 damage to your killer.

### Sapling Harvest

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

**Item tooltip:**

> Enemy kills have a 15% chance to drop an extra sapling.

**Augmentation card:**

> Enemy kills have a 15% chance to drop an extra sapling.

### Scar Compass

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

**Item tooltip:**

> Your most recently hit enemy receives Glowing I for 10 minutes.

**Augmentation card:**

> Your most recently hit enemy receives Glowing I for 10 minutes.

### Sleepwalker

**Weapons:** Nightmare Dagger

**Item tooltip:**

> Deal 6% more melee damage at night.

**Augmentation card:**

> Deal 6% more melee damage at night.


**Levels I-V:** 6% / 12% / 18% / 24% / 30% more melee damage. Complete percentage + "more melee damage" phrase is white; remaining text is light gray.

### Slipstream

**Weapons:** Dead Calm Dagger

**Item tooltip:**

> Sprint behind a moving ally within 3 blocks to gain 10% movement speed.

**Augmentation card:**

> Sprint behind a moving ally within 3 blocks to gain 10% movement speed.

### Soul Hunger

**Weapons:** Deathwisper Dagger, Ghost Axe, Hollow Claymore

**Item tooltip:**

> Enemy kills grant 1 absorption heart, up to 3 hearts. These disappear after 10 seconds without Soul Hunger equipped.

**Augmentation card:**

> Kills grant 1 absorption heart (cap 3 hearts). Lost after 10 seconds without this passive.

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

> Sunlight restores 1 hunger and 1 saturation every 15 seconds.

### Unfinished Dream

**Weapons:** Dreambound Claymore, Dreamweaver Axe, Nightmare Dagger

**Item tooltip:**

> Killing an enemy before it damages you heals 2 HP.

**Augmentation card:**

> Killing an enemy before it damages you heals 2 HP.


**Levels I-III:** 2 / 4 / 6 HP. Complete amount + HP phrase is white; remaining text is light gray.

### Vertical Strike

**Weapons:** Dreamweaver Axe, Echoing Axe, Ghost Axe, Molten Axe, Nullified Axe, Skybreaker Axe, Verdant Axe, Voltage Axe, Woundmaker Axe

**Item tooltip:**

> Every third fully charged attack in a 10 second combo triggers an area slam.

**Augmentation card:**

> Every third fully charged attack in a 10 second combo triggers an area slam.

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

> Apply %1$s %2$s for %3$s seconds on hit.

The tables below expand that template into the actual English item descriptions for every registered special weapon variant. Roman numerals here are the status-effect strength, not the augmentation rank.

### Glowing Hit (Ignisium)

**Weapons:** Emberfang Dagger, Infernal Claymore, Molten Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Glowing II for 53.35 seconds on hit. | Apply Glowing II for 100.00 seconds on hit. | Apply Glowing II for 177.80 seconds on hit. |
| Ruby | Apply Glowing II for 53.35 seconds on hit. | Apply Glowing II for 100.00 seconds on hit. | Apply Glowing II for 177.80 seconds on hit. |
| Amber | Apply Glowing II for 53.35 seconds on hit. | Apply Glowing II for 100.00 seconds on hit. | Apply Glowing II for 177.80 seconds on hit. |
| Amethyst | Apply Glowing II for 58.80 seconds on hit. | Apply Glowing II for 100.00 seconds on hit. | Apply Glowing II for 153.85 seconds on hit. |
| Jade | Apply Glowing III for 53.85 seconds on hit. | Apply Glowing III for 101.00 seconds on hit. | Apply Glowing III for 179.55 seconds on hit. |

### Blindness Hit (Inanisium)

**Weapons:** Riftfang Dagger, Claymore of the Void, Nullified Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Blindness II for 2.15 seconds on hit. | Apply Blindness II for 4.00 seconds on hit. | Apply Blindness II for 7.10 seconds on hit. |
| Ruby | Apply Blindness II for 2.15 seconds on hit. | Apply Blindness II for 4.00 seconds on hit. | Apply Blindness II for 7.10 seconds on hit. |
| Amber | Apply Blindness II for 2.15 seconds on hit. | Apply Blindness II for 4.00 seconds on hit. | Apply Blindness II for 7.10 seconds on hit. |
| Amethyst | Apply Blindness II for 2.35 seconds on hit. | Apply Blindness II for 4.00 seconds on hit. | Apply Blindness II for 6.15 seconds on hit. |
| Jade | Apply Blindness III for 2.65 seconds on hit. | Apply Blindness III for 5.00 seconds on hit. | Apply Blindness III for 8.90 seconds on hit. |

### Nausea Hit (Somnium)

**Weapons:** Nightmare Dagger, Dreambound Claymore, Dreamweaver Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Nausea V for 5.35 seconds on hit. | Apply Nausea V for 10.00 seconds on hit. | Apply Nausea V for 17.80 seconds on hit. |
| Ruby | Apply Nausea V for 5.35 seconds on hit. | Apply Nausea V for 10.00 seconds on hit. | Apply Nausea V for 17.80 seconds on hit. |
| Amber | Apply Nausea V for 5.35 seconds on hit. | Apply Nausea V for 10.00 seconds on hit. | Apply Nausea V for 17.80 seconds on hit. |
| Amethyst | Apply Nausea V for 5.90 seconds on hit. | Apply Nausea V for 10.00 seconds on hit. | Apply Nausea V for 15.40 seconds on hit. |
| Jade | Apply Nausea VI for 5.85 seconds on hit. | Apply Nausea VI for 11.00 seconds on hit. | Apply Nausea VI for 19.55 seconds on hit. |

### Slowness Hit (Electrium)

**Weapons:** Static Dagger, Claymore of Thunder, Voltage Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Slowness IV for 2.65 seconds on hit. | Apply Slowness IV for 5.00 seconds on hit. | Apply Slowness IV for 8.90 seconds on hit. |
| Ruby | Apply Slowness IV for 2.65 seconds on hit. | Apply Slowness IV for 5.00 seconds on hit. | Apply Slowness IV for 8.90 seconds on hit. |
| Amber | Apply Slowness IV for 2.65 seconds on hit. | Apply Slowness IV for 5.00 seconds on hit. | Apply Slowness IV for 8.90 seconds on hit. |
| Amethyst | Apply Slowness IV for 2.95 seconds on hit. | Apply Slowness IV for 5.00 seconds on hit. | Apply Slowness IV for 7.70 seconds on hit. |
| Jade | Apply Slowness V for 3.20 seconds on hit. | Apply Slowness V for 6.00 seconds on hit. | Apply Slowness V for 10.65 seconds on hit. |

### Levitation Hit (Taifunite)

**Weapons:** Dead Calm Dagger, Storming Claymore, Skybreaker Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Levitation III for 1.60 seconds on hit. | Apply Levitation III for 3.00 seconds on hit. | Apply Levitation III for 5.35 seconds on hit. |
| Ruby | Apply Levitation III for 1.60 seconds on hit. | Apply Levitation III for 3.00 seconds on hit. | Apply Levitation III for 5.35 seconds on hit. |
| Amber | Apply Levitation III for 1.60 seconds on hit. | Apply Levitation III for 3.00 seconds on hit. | Apply Levitation III for 5.35 seconds on hit. |
| Amethyst | Apply Levitation III for 1.75 seconds on hit. | Apply Levitation III for 3.00 seconds on hit. | Apply Levitation III for 4.60 seconds on hit. |
| Jade | Apply Levitation IV for 2.15 seconds on hit. | Apply Levitation IV for 4.00 seconds on hit. | Apply Levitation IV for 7.10 seconds on hit. |

### Wither Hit (Vulnusium)

**Weapons:** Assassin Dagger, Curseblood Claymore, Woundmaker Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Wither II for 4.80 seconds on hit. | Apply Wither II for 9.00 seconds on hit. | Apply Wither II for 16.00 seconds on hit. |
| Ruby | Apply Wither II for 4.80 seconds on hit. | Apply Wither II for 9.00 seconds on hit. | Apply Wither II for 16.00 seconds on hit. |
| Amber | Apply Wither II for 4.80 seconds on hit. | Apply Wither II for 9.00 seconds on hit. | Apply Wither II for 16.00 seconds on hit. |
| Amethyst | Apply Wither II for 5.30 seconds on hit. | Apply Wither II for 9.00 seconds on hit. | Apply Wither II for 13.85 seconds on hit. |
| Jade | Apply Wither III for 5.35 seconds on hit. | Apply Wither III for 10.00 seconds on hit. | Apply Wither III for 17.80 seconds on hit. |

### Hunger Hit (Overgrown / Lush)

**Weapons:** Leafcutter Dagger, Overgrown Claymore, Verdant Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Hunger IV for 5.35 seconds on hit. | Apply Hunger IV for 10.00 seconds on hit. | Apply Hunger IV for 17.80 seconds on hit. |
| Ruby | Apply Hunger IV for 5.35 seconds on hit. | Apply Hunger IV for 10.00 seconds on hit. | Apply Hunger IV for 17.80 seconds on hit. |
| Amber | Apply Hunger IV for 5.35 seconds on hit. | Apply Hunger IV for 10.00 seconds on hit. | Apply Hunger IV for 17.80 seconds on hit. |
| Amethyst | Apply Hunger IV for 5.90 seconds on hit. | Apply Hunger IV for 10.00 seconds on hit. | Apply Hunger IV for 15.40 seconds on hit. |
| Jade | Apply Hunger V for 5.85 seconds on hit. | Apply Hunger V for 11.00 seconds on hit. | Apply Hunger V for 19.55 seconds on hit. |

### Weakness Hit (Morsium)

**Weapons:** Deathwisper Dagger, Hollow Claymore, Ghost Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Weakness IV for 2.65 seconds on hit. | Apply Weakness IV for 5.00 seconds on hit. | Apply Weakness IV for 8.90 seconds on hit. |
| Ruby | Apply Weakness IV for 2.65 seconds on hit. | Apply Weakness IV for 5.00 seconds on hit. | Apply Weakness IV for 8.90 seconds on hit. |
| Amber | Apply Weakness IV for 2.65 seconds on hit. | Apply Weakness IV for 5.00 seconds on hit. | Apply Weakness IV for 8.90 seconds on hit. |
| Amethyst | Apply Weakness IV for 2.95 seconds on hit. | Apply Weakness IV for 5.00 seconds on hit. | Apply Weakness IV for 7.70 seconds on hit. |
| Jade | Apply Weakness V for 3.20 seconds on hit. | Apply Weakness V for 6.00 seconds on hit. | Apply Weakness V for 10.65 seconds on hit. |

### Darkness Hit (Pulsite)

**Weapons:** Resonance Dagger, Shrieking Claymore, Echoing Axe

| Gemstone | Dagger tooltip | Claymore tooltip | Axe tooltip |
| --- | --- | --- | --- |
| None (base) | Apply Darkness VI for 3.75 seconds on hit. | Apply Darkness VI for 7.00 seconds on hit. | Apply Darkness VI for 12.45 seconds on hit. |
| Ruby | Apply Darkness VI for 3.75 seconds on hit. | Apply Darkness VI for 7.00 seconds on hit. | Apply Darkness VI for 12.45 seconds on hit. |
| Amber | Apply Darkness VI for 3.75 seconds on hit. | Apply Darkness VI for 7.00 seconds on hit. | Apply Darkness VI for 12.45 seconds on hit. |
| Amethyst | Apply Darkness VI for 4.10 seconds on hit. | Apply Darkness VI for 7.00 seconds on hit. | Apply Darkness VI for 10.75 seconds on hit. |
| Jade | Apply Darkness VII for 4.25 seconds on hit. | Apply Darkness VII for 8.00 seconds on hit. | Apply Darkness VII for 14.20 seconds on hit. |

Anger of Zeus waves now strike from the outermost occupied band toward the player. After the enemy strikes and chains finish, Lightning Rod on the casting weapon or a valid matching dual-wield partner adds a player-only lightning strike, granting its usual buffs.


Documentation refreshed from final in-game component snapshots. Numeric damage examples use the first eligible weapon listed in registry order; the actual tooltip computes values for the hovered weapon.

Cards use compact previews to fit the augmentation table. Hovering a card shows the complete reviewed description and all upgrade changes. Item descriptions retain the approved wording and colors.
