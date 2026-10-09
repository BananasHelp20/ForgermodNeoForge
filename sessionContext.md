# Session context

## Project, repository, and standing instructions

- Workspace: `D:\Minecraft Modding internal\Mods\ForgermodNeoForge`; branch `main`; remote `origin` is `https://github.com/BananasHelp20/ForgermodNeoForge.git`.
- Mod ID `forgermod`, mod version `0.2.0`, Minecraft **1.21.1**, NeoForge **21.1.93**, Java/Gradle project. Follow `AGENTS.md` and verify APIs against NeoForge 1.21.1.
- The user wants every addition, deletion, buff, and debuff listed in `src/changes.txt`.
- Implement abilities one by one. After each: test, review, fix bugs, rerun tests, review again, commit, and push. If committing or pushing fails, give the user commands to run and continue as previously instructed.
- `ability-thoughts.txt` is the user's design input. Leave it untouched unless asked. The text after `[CODEX IGNOGE THE FOLLOWING TEXT]` is excluded from the approved list. The file was clean at the start of this handoff.
- The user has not finalized primary/secondary designs for special claymores and axes. Their current notes after the marker are excluded. Ask for designs when needed; do not invent them as approved behavior.

## Weapon framework and established behavior

- Configurable ability keys default to `1` for primary and `2` for secondary (`WeaponAbilityClient`). Tooltips show the live configured key with `Component.keybind`, under weapon lore and above gemstone infusion (`SwordItemWithEffect`). Ability activations are validated and executed on the server (`WeaponAbilityNetwork`).
- Primary and secondary cooldowns are independent for each registered weapon item, including gemstone variants. Both can run on the same equipped `ItemStack`. An ongoing ability starts its full cooldown when it finishes or when switching away from that exact stack cancels it. Instant abilities start cooldown on use. Deadlines persist through logout and respawn via `WeaponCooldownAttachments`.
- Ability packets include the selected hotbar slot and item ID so a delayed packet cannot trigger another weapon after switching. Active sessions track the stack identity and selected slot. Breakage, death, logout, or switching cancels ongoing states. Cooldown or failed activation displays an actionbar message.
- Matching dagger items, except knives, dual wield. An offhand swing occurs about 100 ms after the main swing even on an air attack. Successful special dagger hits apply their material effect. The delayed attack retains the original target and checks the server's recent main-hand hit, range, and line of sight.
- Axes have a 3×3 ground slam on the third attack if the previous two were within ten seconds, and disable offhand use. An occupied offhand moves into inventory or drops if full. The first-person slam animation is local; remote players see the ordinary swing plus particles.
- Special claymores retain material hit effects. Amber special weapons receive their attack attributes and fire resistance without leaking item properties into other variants.

## Implemented special dagger abilities

| Material | Primary | Secondary | Extra passive |
| --- | --- | --- | --- |
| Ignisium | Flaming Combo: hits add one second of burn during ten seconds | Pyromaniac: arm while burning; next hit ignites and heals 5 HP | Material effect |
| Inanisium | Stepping Through the Void: up to 10 blocks in view direction, 20 with an offhand Riftfang; seven-second cooldown | Sudden Presence: safe landing behind a nearby hostile mob within 20 blocks | Material effect |
| Somnium | Absolute Nightmare: next two hits inflict Darkness I and Weakness I for ten seconds | Lucid Dreaming: next enemy hit grants Speed II and halves fall damage for five seconds | Material effect |
| Electrium | Area Discharge: lightning damage to hostile mobs within five blocks | Charge Attack: 20 mob hits charge; 21st adds 20 lightning damage | Chain Lightning to up to two nearby hostiles |
| Taifunite | Eye of the Storm: whirl and levitate nearby hostiles for five seconds | Windy Dash: up to six blocks forward | One extra midair jump |
| Vulnusium | Deep Wound: next critical hit doubles its damage and slows for two seconds | Leech: next ten dagger hits drain up to 10% of target maximum health | Material effect |
| Overgrown / `LushWeapon` | Rooting Roots: hold and poison nearby hostiles for ten seconds | Poisoned Vein: next hostile melee kill leaves a five-second cloud applying 30 seconds of Poison I to anyone inside, including its creator | Material effect |
| Morsium | Strengthened Bones: next hostile melee kill grants ten seconds of invulnerability | Storing Anger: store outgoing damage for ten seconds, release on next dagger hit | Death March and Revenge |
| Pulsite | Sonically Charged Crit: next critical hit adds 15% of target maximum health as damage | Sonic Boom: 50-block block-clipped piercing blast, twice equipped attack damage; 45-second cooldown | Material effect |

- All special daggers also have the matching-offhand dual wield passive. Exact cooldowns and player-facing descriptions are in `src/main/resources/assets/forgermod/lang/en_us.json` and `src/changes.txt`.
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
