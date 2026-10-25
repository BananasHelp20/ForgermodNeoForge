# Rooting Roots sound

Rooting Roots uses three positional sound phases at each bound mob's roots:

- Growth: crunchy root fractures, earth breaking and low wooden strain as the roots emerge.
- Hold: quieter twisting creaks every 2.5 seconds, staggered between mobs.
- Retreat: reversed root and soil movement as the roots return underground, including cancellation.

Each phase has two randomly selected mixes with slight pitch variation. Growth and retreat follow the existing ten-tick animation; holding does not replay growth. Retraction stops all subsequent creaks. No new looping sound instances or ticking entities are created.

At most three cues of each phase can play in a 16-block cell per server tick. Separate areas retain independent budgets, so simultaneous players elsewhere do not silence one another.

## Sources and mix

All samples come from the project's Minecraft 1.21.1 vanilla assets: `block/mangrove_roots/break1–2`, `block/rooted_dirt/break1–2`, `dig/wood1–2`, and `block/azalea/break1–2`.

Mangrove roots are slowed to 72% playback speed, rooted dirt to 85%, wood to 58%, and azalea to 80%. Overlapping cracks, wood strain and quieter foliage produce the growth/hold mixes; reversed root and dirt layers emphasize withdrawal. Clips have faded edges and conservative normalization; all six encoded files are mono 44.1 kHz Vorbis and decode without clipping. Growth and hold clips last 0.65 seconds, retreat clips 0.55 seconds.

The earlier two generic activation sounds are replaced by this lifecycle audio. Damage, duration, targeting, cooldowns and root geometry are unchanged.
