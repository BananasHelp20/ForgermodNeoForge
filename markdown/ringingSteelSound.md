# Ringing Steel hit sound

The old hit combined `ANVIL_HIT` with a bell. In Minecraft 1.21.1's actual sounds.json, `block.anvil.hit` resolves to stone-step samples, so its name did not provide a metal impact.

The replacement is a 1.35-second mono Vorbis sample: real vanilla `random/anvil_use.ogg` (gain 0.65, up to 1.25 seconds), an `anvil_land.ogg` attack transient (gain 0.28, 0.15 seconds), and a decaying inharmonic steel ring at 1370, 2170, 3880 and 5990 Hz with slight detuned beating. Endpoints are faded and the pre-encode peak normalized to 0.82; decoded duration and absence of clipping are checked. No bell or stone-hit samples are used in the hit mix.

Source assets come read-only from the local Minecraft 1.21.1 asset cache (index 17); original vanilla samples belong to Mojang/Microsoft. The added resonance is synthesized. Output is `sounds/ability/ringing_steel_hit.ogg`, played once at the victim when a successful armed melee hit consumes Ringing Steel. Existing spark feedback and fifteen durability damage remain.
