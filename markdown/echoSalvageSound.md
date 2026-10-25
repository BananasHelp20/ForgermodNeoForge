# Echo Salvage discharge

Releasing stored Vertical Strikes adds a distinct sculk resonance over the normal heavy impact: a deep sonic thump, a sharp sculk crackle, and four overlapping, descending crystalline echoes. Two randomly selected mixes reduce repetition. A fuller bank slightly lowers playback pitch (1.08 for one stored strike through 0.92 for five).

The sound plays once when the bank is consumed, both for an actual Vertical Strike and when a successful melee hit converts into one. Reinforcements use the same cue. Storage misses, rejected hits, indirect damage and ordinary strikes with an empty bank do not play the discharge sound. Area attacks never play it once per victim. Damage, storage capacity, persistence and the existing heavy smash audio are unchanged.

## Sources and mix

The two 1.4-second mono 44.1 kHz Vorbis clips use vanilla Minecraft 1.21.1 samples: `mob/warden/sonic_boom1–2`, `block/sculk_sensor/sculk_clicking1–2`, and `block/amethyst/resonate1–2`.

The sonic layer runs at 72% speed, the sculk layer at 80%. Amethyst resonance repeats at 0, 0.21, 0.43 and 0.66 seconds, progressively lowered to 82%, 72%, 62% and 54% speed, with decreasing gains. The mix has faded edges, normalization headroom and decoded peaks below clipping. Echoes are baked into one finite clip; no extra scheduled sound events or ticking objects are needed.
