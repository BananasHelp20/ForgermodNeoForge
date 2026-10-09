# Session context

## Project, repository, and standing instructions

- Workspace: `D:\Minecraft Modding internal\Mods\ForgermodNeoForge`; branch `main`; remote `origin` is `https://github.com/BananasHelp20/ForgermodNeoForge.git`.
- Mod ID `forgermod`, mod version `0.2.0`, Minecraft **1.21.1**, NeoForge **21.1.93**, Java/Gradle project. Follow `AGENTS.md` and verify APIs against NeoForge 1.21.1.
- The user wants every addition, deletion, buff, and debuff listed in `src/changes.txt`.
- Implement abilities one by one. After each: test, review, fix bugs, rerun tests, review again, commit, and push. If committing or pushing fails, give the user commands to run and continue as previously instructed.
- `ability-thoughts.txt` is the user's design input. Leave it untouched unless asked. The text after `[CODEX IGNOGE THE FOLLOWING TEXT]` is excluded from the approved list. The file was clean at the start of this handoff.
- The user has not finalized primary/secondary designs for special claymores and axes. Their current notes after the marker are excluded. Ask for designs when needed; do not invent them as approved behavior.

## Weapon framework and established behavior

- Configurable ability keys default to `1` for primary and `2` for secondary (`WeaponAbilityClient`). Tooltips show named light-gray abilities and the live configured key with `Component.keybind`, under weapon lore and above gemstone infusion (`SwordItemWithEffect`); passive and active descriptions require holding Shift. Ability activations are validated and executed on the server (`WeaponAbilityNetwork`).
- Primary and secondary cooldowns are independent for each registered weapon item, including gemstone variants. Both can run on the same equipped `ItemStack`. An ongoing ability starts its full cooldown when it finishes or when switching away from that exact stack cancels it. Instant abilities start cooldown on use. Deadlines persist through logout and respawn via `WeaponCooldownAttachments`.
- Ability packets include the selected hotbar slot and item ID so a delayed packet cannot trigger another weapon after switching. Active sessions track the stack identity and selected slot. Breakage, death, logout, or switching cancels ongoing states. Cooldown or failed activation displays an actionbar message.
- Daggers of the same material, including different gemstone variants but excluding knives, dual wield. An offhand swing occurs about 100 ms after the main swing even on an air attack. Successful special dagger hits apply their material effect. The delayed attack retains the original target and checks the server's recent main-hand hit, range, and line of sight.
- Axes have a 3×3 ground slam on the third attack if the previous two were within ten seconds, and disable offhand use. An occupied offhand moves into inventory or drops if full. The first-person slam animation is local; remote players see the ordinary swing plus particles.
- Special claymores retain material hit effects. Amber special weapons receive their attack attributes and fire resistance without leaking item properties into other variants.

## Implemented special dagger abilities

| Material | Primary | Secondary | Extra passive |
| --- | --- | --- | --- |
| Ignisium | Flaming Combo: hits add one second of burn during ten seconds | Pyromaniac: arm while burning; next hit ignites and heals 5 HP | Material effect |
| Inanisium | Stepping Through the Void: up to 10 blocks in view direction, 20 with an offhand Riftfang; seven-second cooldown | Sudden Presence: safe landing behind a nearby hostile mob within 20 blocks | Material effect |
| Somnium | Absolute Nightmare: next two hits inflict Darkness I and Weakness I for ten seconds | Lucid Dreaming: next enemy dagger kill grants Speed II for 30 seconds and half fall damage for 15 seconds | Material effect |
| Electrium | Area Discharge: lightning damage to hostile mobs within five blocks | Charge Attack: 20 mob hits charge; 21st adds 20 lightning damage | Chain Lightning to up to two nearby hostiles |
| Taifunite | Eye of the Storm: whirl all entities except caster within 10 blocks for five seconds; living targets are levitated | Windy Dash: up to six blocks forward | One extra midair jump |
| Vulnusium | Deep Wound: next charged critical hit doubles damage and gives the target Slowness III for five seconds | Leech: next ten dagger hits deal 10% more normal damage and heal at most 10 HP in total | Material effect |
| Overgrown / `LushWeapon` | Rooting Roots: hold and poison hostile mobs within 10 blocks for ten seconds | Poisoned Vein: next hostile melee kill leaves a five-second cloud applying 30 seconds of Poison I to anyone inside, including its creator | Material effect |
| Morsium | Strengthened Bones: next hostile melee kill grants ten seconds of invulnerability | Storing Anger: store outgoing damage for ten seconds, release on next dagger hit | Death March and Revenge |
| Pulsite | Sonically Charged Crit: next critical hit adds 15% of target maximum health as damage | Sonic Boom: 50-block block-clipped piercing blast, twice equipped attack damage; 45-second cooldown | Material effect |

- All special daggers also have the same-material offhand dual wield passive. Exact cooldowns and player-facing descriptions are in `src/main/resources/assets/forgermod/lang/en_us.json` and `src/changes.txt`.
- The Pulsite source note says “15% of the enemy's damage,” which is ambiguous. The implemented behavior uses 15% of the target's **maximum health**; change it if the user clarifies a different meaning.
- A clarification was requested on whether switching weapons should remove status effects already applied to enemies. No answer was received before the latest commit. Current behavior cancels remaining charges and ongoing logic, including Rooting Roots anchors and Poisoned Vein clouds, while damage and status already applied by a completed hit retain their normal duration. Lucid Dreaming's active Speed effect is removed on cancellation, restoring a previous Speed effect when applicable.

## Recent fixes and verification

- Sudden Presence now searches multiple safe spots behind nearby hostiles, including uneven ground, and can try the next closest hostile if the nearest has no safe landing space (`9f94948`).
- Dual wield follow-up retains the initial hit target through the 100 ms delay (`5b79b11`).
- Each special weapon variant gets fresh `Item.Properties`; Amber weapons receive the intended attributes and other variants do not inherit Amber fire resistance (`b1d0de9`).
- Cooldowns and switch cancellation were reworked for independent slots and weapon variants, with active-state cleanup across the nine special dagger materials (`8b97fa8`). Flaming Combo and Rooting Roots cannot be repeatedly restarted while active.
- `gradlew.bat test build` passes. It runs 24 lightweight ability test mains and compiles/packages the mod. JSON parsing and `git diff --check` also passed during the last review. No live client or dedicated-server playtest has been done, so in-game targeting, animations, networking, and effects remain to be verified.

## Handoff state

- The latest documentation-only update refreshed this file, added `AGENTS.md`, and recorded both in `src/changes.txt`. It did not change gameplay code; the latest gameplay commit is `8b97fa8`.
- Continue only with abilities whose behavior the user has specified. The next activated abilities for special claymores and axes still need approved designs.

## Riftfang teleport adjustment (2026-10-09)

- Stepping Through the Void now travels up to 10 blocks along the full normalized look vector, including upward, downward, and diagonal air travel. A second Riftfang Dagger in the offhand doubles this to 20 blocks, including different gemstone variants.
- Primary cooldown is 140 ticks (seven seconds); secondary cooldown remains 2400 ticks. Existing independent variant/slot cooldown handling is retained.
- Quarter-block path checks use the entire player bounding box and stop before obstacles, unloaded chunks, the world border, or build-height limits. No supporting floor is required. Blocked paths shorter than half a block fail without consuming cooldown.
- Fixed the initial test harness: NeoForge FakePlayer discards teleports, so these tests use a ServerPlayer with a real packet listener. After correction, `gradlew.bat test build runGameTestServer` passed all 24 lightweight tests and all 14 dedicated-server tests. Five new server tests cover view pitch, all 25 Riftfang variant pairings, unrelated offhands, walls/ceilings, blocked starts, height limits, and cooldown values. Final code review completed; client playtesting remains outstanding.
- This ability change was committed as `3bc6be0` and pushed to `origin/main`. Earlier axe/stand models, claymore scale, dagger tooltip/pairing, and attack-speed work remain uncommitted; the user's staged `src/review1.md` is preserved.

## Sonic Boom redesign (2026-10-09)

- Pulsite Dagger secondary Sonic Boom is now an instant straight 50-block ray from the player's eye along full pitch/yaw. It pierces every intersecting hostile mob and permitted enemy player, excluding allies and passive animals. Damage is twice the player's equipped ATTACK_DAMAGE attribute, including gemstone bonuses and active attribute effects.
- Uses Minecraft 1.21.1's Warden sonic-boom damage source, particle rings one block apart (long-distance packets to nearby viewers so the full trail remains visible beyond 32 blocks), sound, and resistance-aware knockback. Ray clipping uses actual block collision shapes; solid/glass walls stop damage and particles, gaps over bottom slabs pass, fluids do not block. Unloaded chunks truncate the ray without loading terrain.
- The 900-tick (45-second) secondary cooldown starts immediately even on a miss, with independent variant/slot cooldowns unchanged. The primary critical-hit charge is unaffected. Removed the six-hit state, reach modifier, air-swing networking, old armed message, and obsolete state test.
- Checked implementation against locally cached Minecraft/NeoForge 1.21.1/21.1.93 sources. Three `gradlew.bat test build runGameTestServer` runs passed; the final run includes 23 lightweight tests and all 20 server tests. Six new tests cover multiple pierced enemies, 50-block boundaries, off-axis/behind/passive exclusions, stone/glass, partial slab shapes/water, vertical/diagonal aim, all five gemstone damage variants, primary independence, immediate cooldown and miss/repeat handling, and variant cooldown separation. Final code review and whitespace checks passed. Client visual/audio playtesting remains outstanding.
- This redesign was committed as `14ce7e3` and pushed to `origin/main`, separately from the earlier uncommitted models, tooltip/pairing, and attack-speed work. The user's staged `src/review1.md` remains untouched.

## Poisoned Vein poison duration (2026-10-09)

- The user requested thirty seconds of poison for anybody inside the cloud. Each existing half-second pulse now applies 600 ticks of Poison I instead of 40, including the creator; removed the creator immunity and updated the tooltip. Normal Minecraft effect immunities are retained.
- The five-second cloud lifespan, three-block radius, trigger, primary ability, and 45-second secondary cooldown are unchanged. Remaining inside refreshes the duration; leaving, cloud expiry, and cancellation do not remove poison already applied.
- Two `gradlew.bat test build runGameTestServer` runs passed all 23 lightweight tests and all 22 server tests. Two new server tests exercise actual cloud pulses on the creator, another player, a passive animal, and a hostile mob; outside-radius exclusion, late entry, refresh, duration after leaving/canceling, and cloud expiry. Final code/whitespace review passed.
- This change is committed separately from earlier uncommitted model, tooltip/pairing, and attack-speed work. The user's staged `src/review1.md` is preserved.

## Material effect inheritance and speed scaling (2026-10-09)

- Every material axe now applies its claymore material effect on ordinary and successful slam hits. Dagger main/offhand hits share the same effect implementation. All nine materials and five gemstone variants are covered.
- Duration scales as the corresponding gemstone claymore duration times claymore attacks/sec divided by weapon attacks/sec, rounded to the nearest tick. Jade adds 20 ticks and one amplifier before scaling; Amethyst speed is included. Claymore durations and effect strengths are retained. Ordinary axes get approximately 1.78x duration and daggers 0.53x.
- Includes the previously authorized attack-speed correction as a prerequisite: positive final weapon rates, slow axes, medium claymores, fast daggers, +0.4 Amethyst, and validation rejecting nonpositive/nonfinite speeds. Ordinary swords and claymores retain their previous effective rates.
- Two test/build/GameTestServer passes: 23 lightweight checks and all 25 required server tests. New tests cover all 135 material weapons, 45 offhand daggers, 45 axe slams, actual equipped speed ratios and unchanged effect identity/strength. Existing weapon recharge tests cover every registered weapon. Final code and whitespace review passed.
- Earlier model work and tooltip/pairing changes remain uncommitted; staged src/review1.md is preserved. Pending user instructions: Rooting Roots range 10; Shift-only passive/active descriptions and passive names, all light gray; Deep Wound applies Slowness III for five seconds to the TARGET when the charged critical hit lands.

## Rooting Roots ten-block range (2026-10-09)

- Rooting Roots primary now selects hostile mobs within ten blocks instead of five. Duration remains 200 ticks and cooldown 1200 ticks.
- Two test/build/server-test passes: 23 lightweight checks and all 26 server tests. New test verifies mobs at 9.5 and exactly 10 blocks receive poison and anchoring, and mobs beyond 10 are excluded. Code review passed.
- Material-effect scaling and its attack-speed prerequisite were committed/pushed as 5252c72. Next: Shift-only descriptions/passive names and Deep Wound target Slowness III for five seconds on the charged critical hit.

## Named abilities and Shift-only descriptions (2026-10-09)

- Active and passive names remain visible; active names show the live configured key. Descriptions only show while holding Shift, with a light-gray hint when collapsed. All ability names/headings/descriptions are light gray. Screen shift input is read only on the client, guarded by FMLEnvironment; server tooltip checks safely use the collapsed view.
- Named passives: Dual Wield, Vertical Strike, Material Effect, Chain Lightning, Double Jump, Death March, Revenge. Material Effect describes actual scaled duration/amplifier including gems. Removed inline passive descriptions from ordinary axe/claymore lore. Lore and gemstone information remain visible.
- Includes the previously authorized review1.md same-material dagger pairing and split active name/description translation layout. Matching gemstones of a material pair in either hand; knives remain excluded. AGENTS.md reflects that approved behavior. src/review1.md remains unchanged and staged.
- Two test/build/GameTestServer passes: 23 lightweight tests and all 26 server tests. Tooltip tests cover collapsed/expanded views for 135 material weapons and ordinary axes/Rusty Dagger, dynamic keybinds, passive names, colors, and description visibility. Exhaustive dagger pairing tests pass. Final code/whitespace review passed; client visual Shift-hover testing remains outstanding.
- Rooting Roots range change was committed/pushed as b2f2703. Next: Deep Wound target Slowness III for five seconds on the charged critical hit. Earlier model/stand/claymore-scale changes remain uncommitted.

## Deep Wound target slowness upgrade (2026-10-09)

- Charged critical hits apply 100 ticks of Slowness III (amplifier 2) to the TARGET; the attacker never receives this slow. The user explicitly corrected the earlier attacker wording. Doubled critical damage, one-hit consumption, and existing cooldowns remain unchanged.
- Corrected test item IDs after an initial compilation failure. Two subsequent test/build/GameTestServer runs pass all 23 lightweight tests and all 28 required server tests. Tests cover all five variants, ordinary hits retaining the charge, critical damage multiplication, target-only duration/strength and one-time consumption. Final review passed.
- Shift tooltips and named passives were committed/pushed as 0ea5115. Pending tasks: Leech heals at most 10 HP total and hits deal 10 percent extra normal damage rather than percent max-health damage; Lucid Dreaming triggers after an enemy kill, grants Speed II 30 seconds and half fall damage 15 seconds with 15-second cooldown; preserve sprint momentum on Double Jump; Eye of the Storm affects all entities in its radius. Earlier model/stand changes remain uncommitted.

## Leech normal-damage bonus and healing cap (2026-10-09)

- Leech retains ten successful hits and its existing cooldown. Incoming ordinary dagger damage is multiplied by 1.1; removed the extra percent-maximum-health strike. Post-damage healing uses one eleventh of final hit damage (the bonus share), within a ten-HP total budget. Records actual healing restored, so overheal does not spend the budget.
- The damage event hooks cover main and matching offhand hits and killing hits, with no second damage instance or invulnerability reset. Budget resets on reactivation and clears with consumed charges/cancellation.
- Two test/build/GameTestServer runs passed 23 lightweight tests and all 29 required server tests. Tests verify identical damage bonus against different maximum-health targets, ten-HP cap even with large hits, ten-hit consumption, no bonus/healing when uncharged, and budget reset/nonfinite input. Final review passed.
- Deep Wound target slow was committed/pushed as 7fc8049. Still pending: Lucid Dreaming kill trigger/Speed II 30s/fall protection 15s; Double Jump sprint momentum; Eye of the Storm all entities (caster inclusion clarification pending).

## Lucid Dreaming enemy-kill buffs (2026-10-09)

- Lucid Dreaming secondary consumes its armed charge only on an enemy dagger melee kill, including permitted enemy players. Nonlethal hits and passive animal kills do not trigger. Grants Speed II 600 ticks and half fall damage 300 ticks. Tooltips/actionbar reflect the kill trigger.
- Secondary cooldown remains 300 ticks. As required by the standing ongoing-ability policy, it starts when the 15-second protection window ends or is canceled. Speed lasts 30 seconds naturally; switching during ongoing protection cancels its ability buffs, retaining the existing prior-Speed restoration behavior. Repeated activation cannot restart the ongoing protection window.
- Two test/build/GameTestServer passes: 23 lightweight checks and all 30 server tests. State tests verify exact 300-tick protection expiry; server tests verify kill-only trigger, Speed II duration, half fall damage, cancellation, cooldown value, and no mid-effect restart. Final review passed.
- Leech was committed/pushed as fd6ba83. Pending: preserve sprint momentum on Double Jump; Eye of the Storm targets all entities (caster clarification requested).

## Double Jump sprint momentum preservation (2026-10-09)

- The server previously marked its double-jump velocity for a full owner motion packet, overwriting the client horizontal sprint velocity with potentially stale server values. Confirmed against Minecraft 1.21.1 ServerEntity motion synchronization.
- The server still validates/consumes the extra jump and applies lift, but sends a dedicated client acknowledgement. The client applies only vertical 0.55 lift to its current velocity, preserving horizontal x/z and sprint state; fall distance resets. No client-supplied velocity is trusted.
- Fixed initial test harness mock-login channel negotiation; two subsequent test/build/GameTestServer passes succeeded with 23 lightweight checks and all 31 server tests. The server test checks stale horizontal server motion, no full owner velocity overwrite, one-jump-only validation, accepted client handler preserving three sprint vectors, sprint flag, and fall reset. Final review passed; live client/multiplayer movement feel remains to be checked.
- Lucid Dreaming was committed/pushed as d93d091. Remaining task: Eye of the Storm affects all entities in its ten-block radius; requested caster-inclusion clarification received no response yet, so assume everyone except caster if needed. Earlier models remain uncommitted.

## Eye of the Storm all-entity targeting and completed gameplay handoff (2026-10-09)

- Eye of the Storm now selects all alive entities in its actual ten-block spherical radius, including players, passive mobs, hostile mobs, item drops, XP orbs, and projectiles. Every target is whirled and receives synchronized motion; living targets also receive levitation. Five-second duration and one-minute cooldown are unchanged.
- Caster exclusion is the chosen assumption: a clarification was requested, no reply arrived while other tasks proceeded, and the user was told we would affect everyone except the caster. Change this if the user clarifies otherwise.
- Targeting tests passed, then the expanded levitation test needed explicit mock-caster ticks because embedded test connections do not drive normal ServerPlayer ticks. After that harness correction, final test/build/GameTestServer verification passed: 23 lightweight checks and all 32 server tests. Coverage includes every entity category, live levitation on players/passive/hostile mobs, motion synchronization, range/caster exclusion and unchanged cooldown. Final code/whitespace review passed.
- All requested gameplay/tooltip tasks are implemented and pushed in separate reviewed commits: material effects and attack speeds 5252c72; Rooting Roots range b2f2703; named Shift-only light-gray descriptions and pairing 0ea5115; Deep Wound target Slowness III 7fc8049; Leech damage/heal cap fd6ba83; Lucid Dreaming kill buffs d93d091; Double Jump momentum 2c1974d. This commit completes Eye of the Storm.
- Earlier axe/stand model integration, stand behavior/shapes, claymore scale and exporters remain in the working tree and were tested previously, but remain uncommitted. The staged src/review1.md is preserved. Live client movement/Shift-hover/audio/visual playtesting remains outstanding.
