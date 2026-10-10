# Special weapon ability recommendations

**Proposals only; none are implemented or approved.** There are four distinct ideas for each of the 27 special weapon families: 108 ideas total. Each family includes its base, Ruby, Amber, Amethyst and Jade variants, covering all 135 registered special weapons. Gemstones retain their existing bonuses; they do not receive another separate ability pool here. Rusty and Carbon Steel equipment are outside this special-material list.

Each weapon gets two active and two passive proposals, matching the existing slot model. These are alternatives for your manual design, not automatic additions alongside the current dagger abilities. Material Hit, Dual Wield and axe slam remain separate. Damage, ranges, durations, cooldowns and I–IV progression need your balance decisions. Repeated damage cannot recursively trigger the same ability; blocks stop directed attacks unless explicitly described otherwise. Temporary effects do not permanently alter terrain or equipment.

## Ignisium

### Emberfang Dagger — `infernal_dagger`

1. **Wick Thread — Active.** Mark one struck enemy with a burning thread. Your next direct hit on a different enemy stretches the thread between them; enemies crossing that short segment take one fire hit before it snaps. Solid obstacles break the thread.
2. **Cinder Decoy — Active.** Leave a short-lived ember silhouette where you stand. Hostile AI investigating it ignites the silhouette on contact, revealing the approaching mob with a small flare; it deals no explosion damage and cannot distract players automatically.
3. **Quench Point — Passive.** Stabbing a burning enemy while both feet are in water extinguishes it and converts its remaining burn time into one immediate steam hit. That hit cannot ignite or retrigger Quench Point.
4. **Ash Ledger — Passive.** Extinguishing your own fire stores one ash token. A later successful dagger hit consumes it to prevent that target from reigniting you briefly; repeatedly touching fire cannot stack tokens.

### Infernal Claymore — `infernal_claymore`

1. **Furnace Gate — Active.** Place two visible furnace sparks on reachable ground. They form a short, stationary curtain that destroys ordinary hostile arrows crossing it, then expires; bodies and melee attacks pass through normally.
2. **Kiln Verdict — Active.** Begin a conspicuous windup that tracks how far the selected enemy moves. Landing the final close-range swing deals a bonus based on that movement, rewarding a prediction rather than a long-distance chase.
3. **Heat Debt — Passive.** Taking melee damage deposits a capped portion into the blade as heat. Your next fully charged attack spends the deposit on a brief ignition; stored heat decays and never reflects more damage than was taken.
4. **Cold Bellows — Passive.** Consecutive fully charged attacks without touching fire gradually cool the blade. The next shield block against your cooled strike consumes that state to increase shield recovery time, without disabling the shield permanently.

### Molten Axe — `infernal_axe`

1. **Crucible Hook — Active.** Throw a short molten hook into a reachable entity. It pulls that entity sideways toward the direction of your follow-up aim, allowing you to drag an opponent out from behind nearby cover without passing through walls.
2. **Slag Seal — Active.** Strike a grounded target and pin a visible slag weight to its feet. It can move normally, but its next jump breaks the weight and takes one small burst of damage; waiting out the seal avoids the burst.
3. **Tempered Teeth — Passive.** A fully charged hit against an armored target heats one temporary fracture counter. Repeated hits on that same target turn one later axe strike into extra armor durability damage, with no permanent armor-stat reduction.
4. **Lava Dividend — Passive.** Damaging a hostile enemy while standing beside natural lava earns one brief resistance to lava contact. It buys an escape window, not indefinite lava immunity, and cannot refresh from the same enemy continuously.

## Inanisium

### Riftfang Dagger — `dagger_of_the_void`

1. **Borrowed Door — Active.** Attach two small portals to visible, nearby air positions. Your next ordinary thrown projectile entering one exits the other with its remaining velocity; players, melee swings and beams cannot use them.
2. **Missing Beat — Active.** Mark one nearby enemy's current location. After a readable delay, it returns to that location if the destination is still safe, interrupting a charge without dealing damage or moving it into blocks.
3. **Absent Footprint — Passive.** A clean melee miss by a nearby enemy briefly muffles your footsteps and hides your nameplate at distance. It does not grant invisibility or cancel another attack already in progress.
4. **Edge of Nothing — Passive.** Fully charged hits against enemies standing near a real drop store one void edge. Consume it on a later grounded target to reduce that hit's knockback, helping hold that target in melee rather than knocking it away.

### Claymore of the Void — `claymore_of_the_void`

1. **Horizon Fold — Active.** Project a visible plane in front of you. The first enemy crossing it has its movement along the plane's normal reversed once, turning a rush into a retreat without teleportation.
2. **Vacant Throne — Active.** Designate a small clear patch of ground as forbidden space. Hostile mobs path around it while nearby; players may enter freely but become briefly outlined, making it a zoning and detection tool rather than a damage field.
3. **Distance Tax — Passive.** Mark the last enemy you hit. If it walks away from you, its next attack against you suffers a small flat damage penalty; approaching again clears the tax, and teleport distance contributes nothing.
4. **Unwritten Reach — Passive.** Holding a fully charged swing without attacking builds one visible extension of the blade. The next melee attack gains a modest reach increase and consumes the extension even on a miss; it cannot accumulate while swinging.

### Nullified Axe — `axe_of_the_void`

1. **Anchor Sever — Active.** Cut a target's physical attachment to a vehicle or leash where ordinary rules permit dismounting. It does not steal ownership, harm the vehicle or affect bosses that cannot be dismounted.
2. **Null Receipt — Active.** Arm a brief stance that records the next knockback applied to you. Your next successful axe strike transfers that stored impulse to its target instead of adding extra damage; holding the stance cannot negate incoming damage.
3. **Empty Socket — Passive.** Hitting an enemy that has just lost its temporary absorption hearts grants a single improved shield-breaking strike. Absorption created by your own weapon cannot feed this effect.
4. **Event Eraser — Passive.** A kill removes one hostile lingering projectile owned by that victim within a small radius. Priority goes to the nearest dangerous projectile; item drops and unrelated entities are untouched.

## Somnium

### Nightmare Dagger — `dreambound_dagger`

1. **False Awakening — Active.** Mark a target with two alternating visible silhouettes, one real and one echo. The echo briefly attracts mob attention; hitting the real target ends the deception with a small bonus, while players can identify it by its solid shadow.
2. **Sleepwalk Stitch — Active.** Record your last few safe footsteps and replay them as a moving spectral trail. Hostile mobs may follow the trail away from you; it never moves your character or duplicates your attacks.
3. **Dread Appetite — Passive.** Landing a hit while the target has no line of sight to any ally earns one dread stack. Its next successful attack on you consumes the stack to shorten one of its own beneficial effects.
4. **Unfinished Dream — Passive.** Leaving an enemy alive after a critical hit marks it until you hit someone else. Returning later with a normal charged hit converts the mark into a small absorption heart buffer, rewarding disengagement rather than killing.

### Dreambound Claymore — `dreambound_claymore`

1. **Curtain Call — Active.** Draw a visible curtain across a short corridor. Hostile AI loses target memory when passing through it once; players see a clear shimmer but receive no forced camera or movement changes.
2. **Night's Bargain — Active.** Temporarily reserve some of your health above one heart. Spend that reserve to add damage to one charged strike; unused reserve returns when the stance ends, while incoming damage can still kill you normally.
3. **Recurring Scene — Passive.** Returning to the location of your previous successful claymore hit grants a brief attack-readiness bonus. A visible ground sigil communicates the location and only one sigil can exist per wielder.
4. **Lucid Witness — Passive.** Seeing an enemy prepare a ranged shot while you hold the blade reveals that shooter briefly. It requires direct sight and rewards awareness without giving a general wall scan.

### Dreamweaver Axe — `dreambound_axe`

1. **Dream Nail — Active.** Place a harmless spectral nail in a visible grounded enemy's shadow. Its first attempt to sprint wakes the nail, exchanging its sprint burst for a brief wobble; normal walking avoids the trigger.
2. **Somnambulist's Map — Active.** For a short time, project the intended walking path of nearby hostile mobs as sparse footprints. It reveals their current route, not future attacks or targets hidden behind walls.
3. **Memory Splinter — Passive.** Hitting the same enemy with differently directed strikes builds a pattern. Completing front, side and rear hits exposes one short armor-gap damage opportunity, with a visible marker showing remaining directions.
4. **Quiet Room — Passive.** Remaining still after combat suppresses vibration events from your equipment actions briefly. Moving or attacking breaks the calm; it does not silence other players or disable sculk globally.

## Electrium

### Static Dagger — `dagger_of_thunder`

1. **Spark Switch — Active.** Tag two visible enemies with opposite charges. The next direct hit on either swaps their horizontal momentum once, letting a running enemy propel a stationary one without exchanging positions.
2. **Grounding Needle — Active.** Plant a temporary lightning pin on reachable ground. One nearby hostile electrical attack is drawn into it and dissipated, while ordinary arrows and melee damage remain unaffected.
3. **Capacitor Parry — Passive.** An attack that narrowly misses you fills one tiny capacitor. Your next hit spends it to briefly reveal the attacker's held-item cooldown readiness; repeated misses from the same attack cannot fill more cells.
4. **Contact Resistance — Passive.** Alternating direct hits between two enemies raises knockback resistance against those two only. Hitting a third enemy or leaving combat resets the circuit.

### Claymore of Thunder — `claymore_of_thunder`

1. **Rail Cut — Active.** Draw a short rail on open ground, then swing to send a travelling blade along it. It strikes the first enemy crossing the rail and ends at solid collision; its path is fixed before release.
2. **Faraday Crown — Active.** Suspend a small ring around a chosen ally. It absorbs one incoming debuff application and breaks, but cannot absorb direct damage or cleanse effects already active.
3. **Alternating Current — Passive.** Alternating critical and noncritical fully charged hits builds rhythm. Completing the pattern grants one strike that transfers a short beneficial movement effect from its target to you, without copying other buffs.
4. **Storm Barometer — Passive.** During thunderstorms, the blade predicts nearby natural lightning with a brief visible ground warning. Surviving close to that strike stores one small attack bonus; summoned lightning cannot generate it.

### Voltage Axe — `axe_of_thunder`

1. **Magnetic Census — Active.** Reveal nearby enemies carrying metal equipment and tug their loose arrows toward a selected ground point. Equipped gear stays attached, and the scan requires ordinary line of sight.
2. **Circuit Breaker — Active.** Arm one axe hit to interrupt the target's current shield or item-use action. It does not add cooldown to unrelated weapon abilities, remove enchantments or disable equipment after the interruption.
3. **Induction Heel — Passive.** Descending stairs or a slope in combat stores a small ground charge. Your next upward melee strike spends it to boost the target's vertical knockback, rewarding terrain use without another jump ability.
4. **Fault Tolerance — Passive.** If your axe strike is blocked, retain part of your attack readiness for the next attempt. It applies once per charged attack and cannot produce instant repeated hits.

## Taifunite

### Dead Calm Dagger — `storming_dagger`

1. **Crosswind Feint — Active.** Create a short gust beside your aim that bends nearby ordinary hostile arrows sideways. It changes their trajectory once instead of deleting or reflecting them toward the shooter.
2. **Pocket Silence — Active.** Catch one hostile sound event near you and release a harmless copy at a visible location. Sound-sensitive mobs investigate the copy; there is no damage blast or global audio suppression.
3. **Slipstream Etiquette — Passive.** Running immediately behind a moving allied entity gives a small movement benefit. You lose it when overtaking, stopping or breaking sight, so it supports formation travel rather than free speed.
4. **Weathercock — Passive.** A frontal melee hit briefly points an air ribbon toward the direction of the next nearby incoming projectile. The ribbon warns without blocking, targeting enemies or forcing the camera.

### Storming Claymore — `storming_claymore`

1. **Pressure Lock — Active.** Compress a visible volume of air around a grounded enemy. The target can move within it, but exiting releases one outward shove; remaining inside until it fades avoids the shove.
2. **Sailmaker — Active.** Give a selected ally a brief controllable glide after its next jump. It reduces descent but grants no upward launch, teleport or immunity to collisions; attacking ends the sail.
3. **Tailwind Finish — Passive.** A kill made while retreating grants a short backward-movement benefit. Forward sprinting receives no bonus, encouraging controlled withdrawal after finishing a threat.
4. **High Ground Forecast — Passive.** Holding the blade above an enemy's elevation shows a subtle marker of its next jump landing. The marker appears only once the jump starts and requires sight of the enemy.

### Skybreaker Axe — `storming_axe`

1. **Downdraft Bell — Active.** Strike the air to force nearby airborne enemies downward once. It cancels ascent without teleporting or adding artificial fall damage; grounded enemies are unaffected.
2. **Windward Wedge — Active.** Set a directional gust wall on open ground. Crossing against its direction slows horizontal momentum; crossing with it retains normal speed. It deals no damage and cannot lift entities.
3. **Vacuum Grip — Passive.** A fully charged axe hit briefly reduces only the target's ability to knock you back. Other enemies, explosions and self-inflicted impulses keep their normal effect.
4. **Sky Tithe — Passive.** Hitting flying enemies stores a capped feather token. Later spending it by crouching lets you cling briefly to a ladder or vine without descending, providing vertical positioning rather than fall protection.

## Vulnusium

### Assassin Dagger — `curseblood_dagger`

1. **Suture Theft — Active.** Mark one reachable enemy, then remove a single negative status from a chosen nearby ally by transferring its remaining duration to that mark. Boss immunities and invalid effects are respected; nothing is duplicated.
2. **Pulse Intercept — Active.** Predict a target's next direct heal within a short window. If it heals, cancel a capped portion and turn it into temporary absorption for you; passive regeneration ticks do not trigger it.
3. **Red Witness — Passive.** Recently injured allies leave a faint trail visible only to you. Following the trail to its end improves one subsequent support effect, without revealing unrelated players or their inventory.
4. **Clean Exit — Passive.** If your hit leaves an enemy just above death, moving out of melee reach before attacking again grants a brief cleanse of your own bleeding-like effect. It does not cleanse every debuff or heal health.

### Curseblood Claymore — `curseblood_claymore`

1. **Blood Escrow — Active.** Reserve a small amount of your own health in a visible floating seal. Breaking it with a successful claymore strike heals one selected nearby ally; expiry returns the reserve to you, while death destroys it.
2. **Anatomist's Verdict — Active.** Choose one visible target's current stance: running, guarding or airborne. A later strike while it remains in that stance applies a distinct short disruption; changing stance defeats the prediction.
3. **Scar Compass — Passive.** The most recent enemy to seriously injure you leaves a directional pulse while nearby. It conveys bearing, not exact location, and ends once you strike that enemy or combat expires.
4. **Borrowed Courage — Passive.** A fully charged hit while below a health threshold grants temporary protection from fear-like forced retreat. It provides no general damage resistance, healing or automatic extra damage.

### Woundmaker Axe — `curseblood_axe`

1. **Hemorrhage Clock — Active.** Start a visible countdown on a struck enemy. Damage it again precisely near the last beat to add one flat burst; early hits cancel the timer, making timing more important than attack spam.
2. **Triage Split — Active.** Strike a marked enemy to convert part of that hit's damage into healing divided among injured allies nearby. The damage tradeoff is real, healing is capped, and healthy allies receive nothing.
3. **Jagged Recovery — Passive.** After your own healing completes, the next successful axe hit reduces that target's knockback resistance briefly. The benefit cannot stack from regeneration ticks or trigger another heal.
4. **Open Ledger — Passive.** Every distinct enemy that damages you contributes one capped mark. Your next axe slam spends those marks to reveal the contributors through a brief visible outline, without adding area damage.

## Overgrown / Lush

### Leafcutter Dagger — `nature_dagger`

1. **Graft Step — Active.** Put a temporary bud on a visible living enemy. Hitting a different enemy plants a second bud; allies standing between them receive one small cleanse when the link blooms, with no poison or damage cloud.
2. **Pollen Address — Active.** Dust one enemy with bright pollen. Nearby neutral mobs turn to face it, revealing its position through their attention without making them hostile or overriding pets' commands.
3. **Leaf Receipt — Passive.** Dodging from foliage into clear ground stores a single leaf charge. Your next direct hit consumes it to briefly hide your equipment glint from observers; your body remains visible.
4. **Patient Germination — Passive.** Hitting an untouched enemy plants a harmless seed. If you do not hit it again for a short interval, the seed matures into one extra food-saturation point for you; it neither heals nor damages the target.

### Overgrown Claymore — `overgrown_claymore`

1. **Canopy Covenant — Active.** Raise a temporary spectral canopy above a small allied group. It blocks one falling anvil or dripstone impact and vanishes; arrows and ordinary attacks are unaffected.
2. **Mycelium Relay — Active.** Link two visible allies through a temporary fungal strand. A capped portion of the next food benefit received by one is shared with the other, without copying potion effects or consuming extra items.
3. **Undergrowth Memory — Passive.** Crossing natural plants leaves a short trail only allies can see. An ally retracing it gets reduced slowing from vegetation, while blocks and their collision shapes remain unchanged.
4. **Harvest Restraint — Passive.** Sparing a badly wounded hostile enemy long enough for it to leave melee range earns one harvest token. Spending it on a later kill improves food restoration briefly; the same enemy cannot be farmed repeatedly.

### Verdant Axe — `nature_axe`

1. **Branch Jury — Active.** Grow three spectral branch markers around a target. Each different attack direction breaks one; completing the set pins its rotation briefly, without freezing position or forcing a player's camera.
2. **Seed Vault — Active.** Store a consumable seed item in the blade. Release it after your next axe kill to create a temporary safe landing patch that cushions one ally's fall, then disappears without replacing terrain.
3. **Bark Accounting — Passive.** Blocking sight of an enemy behind a tree grants one brief protection against that enemy's next projectile. It applies only after genuine loss of sight and does not stop other shooters.
4. **Coppice Rhythm — Passive.** Alternating a wood-cutting action with a successful combat hit stores one utility charge. Spend it to accelerate your next stripped-log interaction; it changes neither combat damage nor loot quantity.

## Morsium

### Deathwisper Dagger — `hollow_dagger`

1. **Last Confession — Active.** Mark a hostile mob so its next lethal hit leaves a short-lived whisper revealing which nearby enemy it was targeting. Players instead get a visible voluntary interaction; their private information is never exposed.
2. **Grave Mute — Active.** Suppress one selected enemy's next reinforcement summon during a short window, where that summon can be intercepted by the mod. Failed prediction spends the cooldown; existing summons remain alive.
3. **Pallbearer's Pace — Passive.** Carrying a recent ally-death token briefly removes terrain slowdown while moving toward the death location. It expires after arrival and gives no benefit while chasing enemies elsewhere.
4. **Bone Courtesy — Passive.** An undead enemy that misses a melee attack against you briefly hesitates before retargeting someone else. It may still attack you normally; players and bosses retain their usual control.

### Hollow Claymore — `hollow_claymore`

1. **Funeral Procession — Active.** Draw a slow spectral procession along a visible ground path. Hostile mobs crossing it have their aggression redirected toward you once, making a deliberate tanking tool rather than an extra damage source.
2. **Epitaph Seal — Active.** Attach a seal to a selected hostile mob. If it dies inside the short window, its dropped items remain in a visible protected cache briefly; the seal creates no extra loot and cannot hide player-owned drops.
3. **Mourning Weight — Passive.** When a nearby allied entity dies, your next fully charged hit gains increased downward knockback. Only one loss can be stored, and deliberately spawned expendable allies do not provide repeated charges.
4. **Unclaimed Name — Passive.** Hitting an enemy that has not damaged anyone recently briefly reduces its detection radius for other allies. Once it attacks, the effect ends; it cannot make an active fight invisible.

### Ghost Axe — `hollow_axe`

1. **Soul Rivet — Active.** Connect a target to a spectral rivet at its current position. The first knockback it receives is redirected around the rivet in an arc instead of straight away; collision still stops movement.
2. **Wake Toll — Active.** Ring a visible warning around a chosen corpse location. The next hostile mob entering pays a small stamina-like movement penalty; the toll triggers once and never resurrects entities.
3. **Grave Inventory — Passive.** An axe kill stores one harmless spectral outline of the defeated mob. Release it by inspecting the weapon to identify that mob type later; it has no AI, attacks, drops or collision.
4. **Cold Handle — Passive.** After touching soul fire and surviving, your next strike on an undead enemy briefly prevents it receiving healing from another source. It grants no self-healing and is consumed on contact.

## Pulsite

### Warden's Needle — `shrieking_dagger`

1. **Echo Pin — Active.** Tag a visible enemy with an acoustic pin. Its next loud action emits one directional pulse toward you, briefly outlining its route even if it has since moved out of sight; the pin then breaks.
2. **Whisper Exchange — Active.** Select two visible sound origins and exchange the next harmless sound they emit. This misdirects sound-sensitive mobs without swapping attacks, damage, entities or player speech.
3. **Offbeat Cut — Passive.** Time a fully charged hit between a target's footsteps to store one quiet-strike charge. The next hit emits no attack vibration, rewarding rhythm without increasing damage.
4. **Soft Landing Signal — Passive.** Landing near an enemy sends a low audible ping that reveals whether it is currently shield-blocking. It never reduces fall damage, bypasses walls or reveals all nearby entities.

### Shrieking Claymore — `shrieking_claymore`

1. **Resonance Lens — Active.** Establish a visible acoustic lens in clear air. One allied ordinary projectile crossing it narrows its spread and retains its existing damage; the lens cannot amplify beams or pierce walls.
2. **Counterchime — Active.** Arm a short response to one incoming explosion. It reduces that explosion's knockback for nearby allies, then vanishes, while all explosion damage and terrain rules remain normal.
3. **Room Tone — Passive.** Standing still briefly lets the blade estimate enclosed space from nearby blocks. Your next charged strike gains a small armor-gap bonus only in a tight room; the displayed tone changes with the enclosure.
4. **Broken Chorus — Passive.** Interrupting an enemy's ranged preparation stores a short note. A later hit on a different ranged enemy spends it to lengthen that enemy's preparation once, with no effect on melee attacks.

### Echoing Axe — `shrieking_axe`

1. **Fault Note — Active.** Aim at a visible block face to send a vibration along its connected solid surface. The first enemy touching that short path receives one shove away from the surface; the vibration cannot cross gaps or destroy blocks.
2. **Reverberation Cage — Active.** Set three small acoustic markers in open space. Enemies inside their triangle have their next footstep repeated as a harmless sound outside it, creating misleading pursuit information without immobilization.
3. **Tuning Scar — Passive.** Repeated fully charged hits at a consistent interval tune one target. Breaking your own rhythm spends the tune on a single extra shield-durability hit; it does nothing against an unshielded target.
4. **Echo Salvage — Passive.** A missed charged swing that strikes a solid wall stores its sound. Your next utility interaction releases a short nearby-block ping showing reachable interactable faces, with no ore scan or combat damage.

## Review notes

- Each proposal has its own trigger and payoff; material themes repeat, but the mechanics are not just renamed damage bursts. None reuse the existing dagger teleport, dash, double jump, poison cloud, chain lightning, damage ramp, life-steal or Sonic Boom mechanics.
- AI deception affects mobs only. Player-facing deception uses visible tells and never controls a player's camera or invents client-only immunity.
- Temporary markers and effects should be capped per wielder, cleaned up on death/logout/switching, and synchronized from the server. Use particles and compact state instead of persistent terrain edits or many ticking entities.
- You can approve, replace or reject these individually. Defining one does not approve its three upgrades, gemstone-specific behavior or another weapon's proposal.
