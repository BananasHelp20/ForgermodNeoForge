# Augmentation upgrade descriptions

Upgrade cards show the resulting values that change at the offered rank. Those values are green; unchanged words and values stay gray. The hover description also includes the ability's normal explanation. New ability cards retain their short learning summaries.

Examples from the current runtime values:

- Crucible Hook I to II: 1.5x weapon damage, 4 seconds of fire, stronger pull, and a 35-second cooldown.
- Static Field I to II: 5 damage per half-second. The unchanged target count and field unlock are not repeated as improvements.
- Ghostwalk III to Jumpscare Horror I: double opening-hit damage and the reduced cooldown. The unchanged invisibility duration is not highlighted.
- Bone Guard III to Necromancer I: new reinforcements and armor. The existing bogged remains gray within the roster.

Gold super-augment titles remain gold; their improved effects use green. Non-axe Windcharge does not claim the Skybreaker smash. Template previews qualify effects that only work on axes. Jade's extra second is included where it affects the resulting duration. Completed cards keep comparing the original offered ranks, and rejected cards mute the green highlights.

Some existing rank increases do not change any runtime effect, including early Double Jump ranks and Material Hit ranks. Their cards say **Effects unchanged at this level.** This UI change does not add bonuses to those ranks or alter weapon behavior.

`AugmentUpgradeDescriptions` contains the read-only comparisons. Update its properties alongside future ability balance changes. The in-game regression coverage exercises every offered rank across registered weapons and templates, verifies actual numeric examples and component colors, and exports distinct card components under `build/upgrade-preview-components.json` for layout checks. Default vanilla-font measurements found all 161 current distinct upgrade descriptions fit within the card, with body scales from 0.5 to 0.4.
