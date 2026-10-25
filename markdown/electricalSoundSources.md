# Electrical sound sources

The requested Minecraft Dungeons effects were extracted read-only from the user's installed original game at `D:/XboxGames/Minecraft Dungeons/Content/Dungeons/Content/Paks/pakchunk4-WindowsNoEditor.pak`. Original sound ownership: Mojang / Microsoft. The game installation is unchanged. Raw exports and extraction tools stay in ignored `build/` folders.

Reference pages: [Thundering](https://minecraft.wiki/w/Dungeons:Thundering), [Thundering Quiver](https://minecraft.wiki/w/Dungeons:Thundering_Quiver), [Lightning Rod](https://minecraft.wiki/w/Dungeons:Lightning_Rod).

## Alternating Current, Direct Current and Static Field

Three randomized 0.75-second mono Vorbis mixes replace the original synthesized zip. Each mix uses the corresponding `001`, `002` or `003` variants of:

- `D4_sfx_item_thunderingArrowWhoosh`: opening traveling whoosh, gain 0.42, first 0.34 seconds after onset.
- `sfx_env_thunderZap-00N_soundWave`: electrical body, gain 0.58, 0.52 seconds after onset, placed at 0.16 seconds.
- `D4_sfx_item_thunderingArrowImpact`: light arrival accent, gain 0.18, 0.34 seconds after onset, placed at 0.25 seconds.

Playback pitch is preserved. Leading silence is removed, cut tails are faded, and each finished mix is peak-normalized to 0.82 before Vorbis encoding. Output: `sounds/ability/electric_travel.ogg`, `_2.ogg`, `_3.ogg`. Decoding verifies mono, exact duration and no clipping. The positional source travels in five ticks and its tail stays at the destination. Playback has a finite 24-tick safety bound and stops on world change.

Extraction: checksum-verified [repak](https://github.com/trumank/repak) 0.2.3; only matching SoundWave exports were read. Embedded standard Ogg streams were recovered through their final Ogg page and decoded with libsndfile. No game binaries or packs are distributed with this project.

## Chain Lightning

A separate three-variant, 1-second mono mix emphasizes the charged hit and fast thunder body. Corresponding `001`/`002`/`003` variations are mixed at original pitch:

- Charged thundering-arrow whoosh: gain 0.33, 0.34 seconds from onset, starting immediately.
- Thunder zap: gain 0.35, 0.46 seconds from onset, starting at 0.12 seconds.
- Charged thundering-arrow impact: gain 0.62, 0.65 seconds from onset, starting at 0.25 seconds.
- Fast thunder strike (`sfx_env_thunderStrikeFast-00N_soundWave`): gain 0.15, 0.60 seconds from onset, starting at 0.27 seconds. This sample comes from `pakchunk70-WindowsNoEditor.pak`.

Same onset trimming, fades and 0.82 pre-encode peak normalization as the lighter mix. Output: `electric_chain.ogg`, `_2.ogg`, `_3.ogg`. Each successful chain hop requests this profile; circuit/alternating paths retain the lighter mix. Network version 2 explicitly carries the profile bit. Both use the same five-tick spatial movement and finite 24-tick safety bound. No chain timing or damage changes.
