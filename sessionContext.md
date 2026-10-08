# Session context

## Project and rules

- Workspace: `D:\Schule\4-Klasse\Projects\Mods\ForgermodNeoForge`.
- Target **Minecraft 1.21.1 and NeoForge 21.1.x** for all code and documentation.
- List every addition, deletion, buff, and debuff in `src/changes.txt`.
- The user authorizes implementation, testing, and one commit and push after each ability. Continue with the next ability after a successful push.
- `ability-thoughts.txt` is user-authored and has uncommitted changes. Do not stage or overwrite it without an explicit request. The text after `[CODEX IGNOGE THE FOLLOWING TEXT]` is excluded from the current ability list.

## Weapon framework

- `WeaponAbilityClient` binds primary and secondary abilities to configurable keys, default `1` and `2`; `WeaponAbilityNetwork` handles activations on the server.
- Ability tooltips show the current key with `Component.keybind` between weapon lore and gemstone infusion. Special passives also appear there.
- All daggers except knives can dual wield a matching offhand dagger, with its swing roughly 100 ms after the main swing, including air swings. Special daggers apply their material effect on successful hits in either hand.
- All axes have the third-hit 3×3 ground slam passive and forbid offhand use while held. Axe offhand items move to inventory or drop when full.
- Special claymores retain material hit effects. The user wants primary and secondary abilities for all special claymores and axes, but their final designs are not defined in the active part of `ability-thoughts.txt`.

## Implemented dagger abilities

- Ignisium: Flaming Combo and Pyromaniac.
- Inanisium: Stepping Through the Void and Sudden Presence.
- Somnium: Absolute Nightmare (next two hits apply Darkness I and Weakness I for ten seconds; twenty-second cooldown) and Lucid Dreaming.
- Electrium: Area Discharge, Charge Attack, and Chain Lightning passive.
- Taifunite: Eye of the Storm, Windy Dash, and Double Jump passive.
- Vulnusium: Deep Wound and Leech.
- Overgrown (`LushWeapon`): Rooting Roots and Poisoned Vein.
- Morsium: Strengthened Bones, Storing Anger, Death March passive, and Revenge passive.
- Pulsite: Sonically Charged Crit and Sonic Boom. The user was asked what “15% of the enemy's damage” means; pending clarification, the implemented critical hit adds 15% of the target's maximum health as damage. Revise if the user answers differently.
- All active abilities have cooldowns, including ones omitted from the design note. Exact behavior and cooldowns are in `en_us.json` tooltips and `src/changes.txt`.

## Verification and remaining limits

- The latest audit checked all nine special dagger classes, their state helpers, the common ability payload/key registration, dual-wield follow-up, and the NeoForge 21.1.93 attack call order. `gradlew.bat test build` passes after each fix.
- Sudden Presence now searches several safe positions behind nearby hostile mobs on uneven terrain and can try the next closest hostile if the nearest is blocked. Failed activations and cooldowns show an actionbar message.
- Dual-wield follow-up retains the target from the main swing and validates the server's recent main-hand hit, preventing a delayed hit from turning into an air swing after knockback or camera movement.
- Amber special weapons now receive their intended attributes. Every special weapon variant gets fresh `Item.Properties`, so Amber fire resistance does not leak into other variants.
- These changes were committed and pushed as `9f94948`, `5b79b11`, and `b1d0de9` on `main`. The only remaining local modification is the user's `ability-thoughts.txt`.
- `gradlew.bat test --offline` runs 24 lightweight ability test mains through Gradle and passes. JSON parsing and `git diff --check` pass.
- No live Minecraft client or dedicated-server playtest has been done. In-game effects, animations, networking, and reach should still be verified in game.
- Review found that logout cleared ability cooldowns; a persisted NeoForge player attachment with `copyOnDeath()` now keeps cooldown deadlines through relog and respawn.
- Sonic Boom charges now count dagger attacks, including air swings.
- The first-person heavy axe animation is local; remote players currently see an ordinary swing plus particles.

## Repository state

- Branch `main`, remote `origin`. All implemented dagger abilities and the latest audit fixes are pushed through `b1d0de9`.
- The next claymore and axe activated abilities await the user's final designs. Content after `[CODEX IGNOGE THE FOLLOWING TEXT]` in `ability-thoughts.txt` remains excluded.
