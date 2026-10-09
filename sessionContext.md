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
| Overgrown / `LushWeapon` | Rooting Roots: hold and poison nearby hostiles for ten seconds | Poisoned Vein: next hostile melee kill leaves a five-second poison cloud | Material effect |
| Morsium | Strengthened Bones: next hostile melee kill grants ten seconds of invulnerability | Storing Anger: store outgoing damage for ten seconds, release on next dagger hit | Death March and Revenge |
| Pulsite | Sonically Charged Crit: next critical hit adds 15% of target maximum health as damage | Sonic Boom: next six attacks have four extra blocks of reach | Material effect |

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
- This ability change is prepared as its own commit. Earlier axe/stand models, claymore scale, dagger tooltip/pairing, and attack-speed work remain uncommitted; the user's staged `src/review1.md` is preserved.
