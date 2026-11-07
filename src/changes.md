CODEX DON'T YOU DARE TOUCH THE TEXT INSIDE THE BRACKETS {
# BETA 0.2.0 (yeah ik, took a while)

## Fixes

- fixed Shardium ores always dropping the same shard instead of choosing a random material
- fixed Jade claymore bonuses affecting other gemstone variants of the same material
- fixed non-Amber gemstone claymores incorrectly surviving fire and lava like Amber variants
- fixed special claymores using attack attributes from the wrong material
- fixed Ancient Sword Stand collision shapes for all four facing directions
- fixed Infusion Table models not rotating with their facing direction
- fixed Infusion Tables sharing recipe selection and gemstone colors with each other

## Added

- added daggers and battle-axes for all special materials, including gemstone variants
- added more than 100 active and passive weapon augments, with material-specific abilities and upgrade levels. *Way to many to list them here, just try them out and have fun ;)*
- added the Augmentation Table
- added the Sapphire Gemstone
- added the Augment Template
- added the Super Augment Template
- added up to three active ability slots per special weapon, with primary, secondary and tertiary keybindings
- added up to two passive slots for special daggers and five for special claymores and axes
- with Shift+Alt+S, you can cycle the Keybindings in order through the equipped weapon's active abilities.
- You can dual wield Daggers. (if one of those daggers has the passive ability that allows this).
- added Vertical Strike: every third fully charged axe attack in a 10-second combo triggers an automatic jump and area slam
- added player animations for battleaxe holding, walking, alternating swings, casting and area attacks
- added super augments: Static Field, Skybreaker, Windwalker, Jumpscare Horror, Necromancer, Blazing Blast, Crucible Grappler, Intimidating Roar, Anger of Zeus and Warping Space
- added ability sounds and effects, including lightning arcs, animated roots, heavy impacts and fire and void particles
- added the Ancient Grave room to Ancient Cities, with ancient-city chest loot and a 10% chance per chest for an extra Sapphire Gemstone
- Ancient Sword Stands can be found in the Ancient Grave and now give one Rusty Claymore, play an opening animation and remain empty afterward
- added the Rusty Axe and Rusty Dagger
- added Meteoric Iron, Titanium and Tungsten, with new ores and processing recipes
- added Dolomite, Magnesium, raw metals, Titanium Sponge, Tungsten Powder, Reinforced Iron Billets and Damask Billets
- added pickaxes, axes, shovels and hoes for Scrap, Meteoric Iron, Reinforced Iron, Steel, Damask, Titanium, Tungsten and Carbon Steel
- added swords for Meteoric Iron, Titanium, Tungsten and Carbon Steel
- added optional Create: Enchantment Industry support for storing, applying, extracting and merging augments similar to enchantments
- added the /augment and /no-ability-cooldown commands
- added a mod configuration screen and Keybindings in the controls.

## Improvements

- redesigned the Infusion Table
- weapons and gemstones appear on the Infusion Table when inserted. infusing animates the gemstone above the weapon
- updated the Ancient Sword Stand model and its full and empty appearances
- enlarged claymore models outside the inventory to match their intended size
- Material-hit effects on special claymores are learned through augmentation instead of being applied automatically
- Jade infusion now requires a learned status-effect augment and adds one effect level and one second of effect duration
- special claymores now use legendary-yellow item names, matching the new special daggers and battle-axes
- renamed Gemstone Infusion Template to Gemstone Template
- rebalanced existing metal swords, knives and claymores, including attack speed, damage and durability
- reworked metal processing. Reinforced Iron now uses Meteoric Iron, while Damask uses a billet
- made Unrefined Steel cheaper: two Iron Ingots and one Charcoal, replacing Iron Ingots, Coal Blocks and Netherite Scrap
- made Unrefined Carbon Steel cheaper: one Steel Ingot and eight Charcoal, replacing Steel, Diamonds and Coal Blocks
- simplified the Reinforced Iron Sword recipe to two Reinforced Iron Ingots and a Stick
- updated Reinforced Iron Ingot, Reinforced Iron Block, Damask Sword, Damask Knife and Ruby Curseblood Claymore textures

## Removed

- removed Scrap Iron crafting and conversion recipes. existing Scrap Iron items remain available through commands
- removed direct Scrap-to-Damask and Scrap-Iron-to-Reinforced-Iron conversions in favor of the new processing recipes
- removed Shardium smelting and blasting recipes.
- removed the old recycling recipes for Damask weapons, Handles and Reinforced Iron Swords
- removed gemstone weapon variants and the developer item "Stumpfl's Bat" from the creative tabs
}

## Fixed
- centered the Augmentation Table item in inventory slots and adjusted its icon scale to fit
## Added
- added Axe Blades, Dagger Blades and Augmented Heavy Cores as weapon crafting components
- added an automatic Augmentation Table conversion from Heavy Core to Augmented Heavy Core, using one Sapphire Gemstone and one Gemstone Template
- added blade crafting recipes using Damask and Carbon Steel
- added battle axe crafting with an Augmented Heavy Core, two Axe Blades and two Advanced Handles
- added dagger crafting with a Dagger Blade, Carbon Steel Cross Guard and Advanced Handle
- Augmented Heavy Cores use the vanilla Heavy Core appearance with a constant blue enchantment glint
## Balancing (Buffed/Debuffed)
- gemstone infusion now consumes one Gemstone Template per completed infusion; forging continues to consume one Ancient Upgrade Template per craft
## Improvements
- normal Augment Template names now use epic purple
- prepared hinged Weapon Assembler arm animations, including hammer strikes and claw movement, with smooth start and return-to-rest transitions; crafting activation will be connected when assembly is implemented
- replaced the Weapon Assembler placeholder with the authored assembler model and texture, with four placement directions and simple collision shapes
- Super Augment Template names now use legendary yellow
- renamed Sharpened Blade to Claymore Blade, including its recipes; existing saved blades remain compatible
## Removed