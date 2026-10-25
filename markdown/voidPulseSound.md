# Void Pulse sound pair

Void Pulse uses Minecraft 1.21.1's `block/end_portal/eyeplace1` sample (the End Portal Frame fill sound), replacing its former Enderman teleport sound.

- Inward particles: a reversed version fitted to the existing ten-tick (0.5-second) charge.
- Outward pulse: the matching forward sound, with its portal resonance and decaying tail.

The forward sample is lowered slightly to 90% playback speed, kept for 0.95 seconds and faded at the edges. Its reversed waveform is fitted to the charge window. Both clips are normalized with headroom, encoded as mono 44.1 kHz Vorbis and decoded to verify no clipping. There is one fixed pair, so the charge and burst always share the same sound character.

Void Pulse has separate sound events for both phases. Gravity Well retains the original shared `void_pulse_charge` reversed teleport clip, and its audio is unchanged. Particle timing, push strength, range, cooldown and cancellation behavior are unchanged.
