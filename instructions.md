- I have already worked out a structure for the ancient Sword stand. it lies in: D:\Minecraft Modding internal\Mods\ForgermodNeoForge\run\saves\Structure\generated\minecraft\structures
and is named forgermod_ancient_grave_v2 as a nbt file. The structure should be named the Ancient Grave and spawn as a guarantied room of an ancient city. (up to one)
- when pressing shift+alt+s while holding an item that has 2 abilities (ignores offhanditem), the primary and secondary ability switch places, making the previous primary ability the secondary ability and the other one the primary abilty.

- make the forge, infusion table and augmentation table aswell as all crafting table, furnace and blast furnace recipes jei compatible.
- for passives, only one of the 2 daggers has to have them. (e.g. dual wield only has to be on the dagger in the main or offhand) (only works when the daggers are the same and one of them has dual wield).
- make gemstone ores rarer (same rarity as diamond, but only one ore block per vein). Implement a saphire ore, it has the same rarity as ancient debries and spawns in all dimensions. (stone, deepslate, endstone, obsidian, netherrack, blackstone varieties) For designing, use the lighter sapphire gemstone colors, and orient yourself by the other gemstone ore blocks. Make a blackstone variety for all other gemstone ores based on their other ore designs and blackstone.
- all weapons can now have a third ability (also include that as a tertiar ability in the keybindings, default: 3)
- make a fitting design for the augmentation table but keep the enchanting particles (abilies are simelar to enchantments)
- all weapons except for daggers can now have up to 5 passives
- the transition between the overlay overlaying the 2 slots and the following overlay takes way too long. (the overlay overlaying the 2 slots should end earlier so that the other overlay can start sooner) (augmentation table)
- when using a dead calm dagger in dual wield and both of them have double jump or windwalker, you are able to do an additional jump in the air. (if both of them have windwalker, you gain 2 additional jumps, if only one of them has windwalker, and the otherone has double jump, only one additional jump is added like both having double jump). When double jump hits max level, it changes it's name to Windwalker, and gains an aditional jump in the air.
- the notices "this item can be..." and "[hold shift] to..." should always be below the enchantments (seperated with a /n) if the item has enchantments. Enchantments should also have a "title" like Enchantments:/n[enchantments]. like with active abilites: Active Abilities:/n[abilities] and passives: Passives:/n[passives]
- find a new name for warden's needle based on it's texture and material. Find 5 fitting names and present them to me for me to choose
- [x] Axe animation import: preserve all six authored clips/keyframes from Downloads/axe_playerplaceholder_animations(1).bbmodel; extract embedded fallback mesh/texture from Axe - Converted(1).bbmodel for mod axes missing artwork, preserving existing textures.
- [x] Alternate axe_swing/axe_swing2 for ordinary axe attacks; render authored player bones and held axe in first/third person and sync other viewers, clean up on switch/death/logout; use idle/walk as applicable.
- [x] Use axe_jump_attack for all Vertical Strike variants (native third strike, Flaming Stomp, Gravity Well, Crucible Hook smash, Echo Salvage conversion and related axe slams); user explicitly requires unchanged animation and delayed hit timing to match authored impact (~1.25s).
- [x] Map axe_grtg_axe_slam to Gravity Swing (casting pose + long swirling field fits group gravity effect; Gravity Well is a Vertical Strike and uses jump clip); preserve authored six-second visual and align cast effect at~1.625s. Handle switch cancellation and independent ability cooldown/session behavior.
- [x] Test/review all animation resources, model fallback coverage, timings/cancellation/critical charge/combat regressions; run datagen/build and meaningful native/client validation; fix failures and rerun, then scoped commit/push preserving unrelated changes. Resume manual description review at78 Echo Appetite only after animation task complete.

- [x] Axe animation visual review: correct source arm-cube rest rotation/pivots, held-mesh scale/grip, first-person exposed shoulder caps and root/particle alignment; compare real-client poses against the source hierarchy without editing animation keyframes.

- [x] Axe camera-switch crash: read actual crash report, remove visual assertions from normal mod/runtime sources, hard-guard isolated test directory, reproduce three actual attacks across first/third/first person, restore generated normal launch files, verify artifact/classpath exclusion and build, review and scoped commit/push.

- [x] Import the upgraded axe_playerplaceholder_animations_2.bbmodel from Downloads without retiming authored keys; preserve smooth curves/new two-handed idle and updated swirl, use its new spinning animation for all Swing Attack variants, align damage timing, verify both camera views/build/GameTests, and restore normal launch settings.

- [x] Speed up ordinary axe swings and matching hit deadlines, restore natural walking/sprinting strides, fix shoulder/torso separation and audit all upgraded axe animation mappings/channels; verify native timing and both camera views.

- [x] Replace Vertical Strike fake forward/jump root motion with an automatic real jump, retain voluntary horizontal movement, hold the raised axe while airborne, and resolve all player vertical-strike variants only on landing; test cancellation, damage once, synchronization and in-game physics.

- [x] Ease normal axe swings back to a natural moderate speed with minimal hit delay; add the Vertical Strike raise/hold/landing animation to Skybreaker while preserving its launch, damage, fall protection and cleanup. Verify timings, all variants and real-client flight; commit only this task.
