# Session context

## Project and working rules

- Project: Forger Mod in `D:\Schule\4-Klasse\Projects\Mods\ForgermodNeoForge`.
- Target **Minecraft 1.21.1 with NeoForge 21.1.x** for all APIs, events, networking, and examples. Do not substitute Forge or another Minecraft version.
- Record every addition, deletion, buff, and debuff as a list entry in `src/changes.txt` when making changes.
- The user currently authorizes direct code edits. An earlier request to explain edits without changing code was superseded by later requests to implement the features.
- Avoid changing names already defined in `ModItems.java`. The English translation file is `src/main/resources/assets/forgermod/lang/en_us.json`.
- `ability-thoughts.txt` is a user-authored design note. It was already staged and then modified in the working tree before this handoff. Treat its contents as design input; do not silently assume every idea is finalized.

## Requested weapon behavior and implementation

- Every special-material dagger, claymore, and axe is intended to have a primary and secondary active ability. Default keybindings are `1` and `2`, configurable through Minecraft controls. The user initially deferred final effects and cooldowns. The active ability framework is present, but no material class implements an active ability yet.
- `WeaponAbilityClient` registers the keys and sends a request to `WeaponAbilityNetwork`. `WeaponAbilitySlot` distinguishes primary and secondary. `SwordItemWithEffect` has `activateAbility`, `abilityCooldownTicks`, and `abilityDescriptionKey` hooks. The tooltip shows a defined ability below lore and above gemstone infusion, with `Component.keybind` so the configured key appears dynamically. Default keys share vanilla hotbar keys; the client consumes the hotbar click when a held weapon has an ability in that slot. This needs in-game verification.
- All daggers except knives can dual wield when the offhand dagger is the same item type. A second offhand swing happens about 100 ms after the main attack, including air swings. The client and server code is in `item/custom/attacks/dagger/`. Special-material dagger hits in either hand now apply their material effect. The rusty dagger has no material effect.
- Special-material claymores retain their material effect on hit. Material effect duration and amplifier are stored per weapon instance in `SwordItemWithEffect`; Jade modifies only its own instance, avoiding mutation of shared static values.
- All axes, including carbon steel and rusty axes, have a heavy attack. The third attack within a 200-tick (10-second) sequence schedules a ground slam 3 ticks later, 2 horizontal blocks ahead. It damages living entities in a 3x3 horizontal area, with knockback, particles, and sound. The local first-person hand has a vertical swing animation; remote players currently see the ordinary swing plus particles. Code is in `item/custom/attacks/axe/`. Axe normal hits consume durability.
- Holding any mod axe in the main hand disables the offhand. On the server tick, the offhand stack is copied into inventory; any remainder is dropped. Offhand item, block, and entity interactions are canceled while the axe is held. The implementation is `AxeOffhandRestriction.java`.
- Weapon names, lore, passive descriptions, and ability tooltip strings are in `en_us.json`. Generic and rusty axes have their passive and empty-offhand requirement in their lore; special axes receive it through `SwordItemWithEffect`.

## Ability design notes still pending

- `ability-thoughts.txt` contains proposed dagger abilities for Ignisium, Inanisium, Somnium, Electrium, Taifunite, and Vulnusium, with cooldown ideas and extra passives for some. Overgrown, Morsium, and Pulsite dagger entries are blank. There are no complete designs for all claymore and axe active abilities in this session.
- Before implementing active effects, reconcile the note with the user's final designs. One note mentions a right-click activation for an Inanisium dagger, while the current overall activation rule is the primary/secondary keybindings.

## Verification and known limits

- `./gradlew.bat classes --offline` passed after the weapon and offhand changes.
- `en_us.json` parsed as JSON, and `git diff --check` passed.
- No in-game client/server test has been performed. Check the keybinding collision with hotbar slots, offhand inventory movement and interaction cancellation, dual-wield timing, third attack timing and area, and first-person animation in game.
- The current axe animation is first person only; remote players do not get a custom vertical arm pose.
- `AxeHeavyNetwork` counts attack input packets, including misses/air swings. It uses a 3-tick minimum gap between counted swings and resets after the third attack or when the 10-second window expires.

## Current repository state at handoff

- Branch: `main`; remote: `origin` at `https://github.com/BananasHelp20/ForgermodNeoForge.git`.
- This session changed `src/changes.txt`, `en_us.json`, `ForgerMod.java`, nine special-material weapon classes, `SwordItemWithEffect.java`, `WeaponAbilityClient.java`, and `DualWieldNetwork.java`; it added the `item/custom/attacks/axe/` classes.
- The user requested this context file, then `git add`, commit, and push. `ability-thoughts.txt` was already staged and modified before that request; include its current version when staging all work.
