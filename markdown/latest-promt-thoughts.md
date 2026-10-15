# Skybreaker implementation checkpoint

This is a concise design rationale and work-status summary, not private internal reasoning.

## Requested behavior

Launch upward, fall onto an enemy with mace-style damage and feedback, then produce a heavy landing impact that damages enemies within five blocks even if the axe swing missed.

## Findings and decisions

- The previous implementation had a two-block splash and consumed the flight on a successful falling hit, preventing a later ground smash.
- Preserve the existing 30-block super launch and super I-IV fall-bonus multipliers of 1.0/1.1/1.2/1.3.
- Put the direct-hit fall bonus in the weapon damage hook before critical-hit multiplication, matching the Minecraft 1.21.1 mace calculation.
- Reuse vanilla mace hit feedback instead of inventing a separate first-person animation. Its hit handler supplies sound, motion correction, smash particles and knockback.
- Keep landing armed after the first successful falling hit. Track descent independently because vanilla mace behavior resets the fall counter.
- Give landing its own one-shot five-block impact centered on the player. All eligible visible enemies in range receive weapon damage plus the fall bonus; this includes an enemy already struck directly if it remains in range.
- Use physical player-attack damage for normal armor handling, native heavy mace sound and smash level event 2013, plus an outward cloud burst and knockback.
- Retain variant-specific ending cooldowns and switching/lifecycle cancellation. Include a grounded-tick fallback when the normal fall event is shortened or suppressed.

## Changes already made

- Updated WindchargeEvents and the Taifunite weapon damage hook.
- Updated existing tests to use actual falling critical attacks, and added range, immunity, wall/neutral exclusion, landing fallback and outgoing feedback checks.
- Updated the game description, super-augment documentation and changes log.

## Verification and remaining work

- Initial full run failed because the new test packet recorder lacked its embedded network channel; corrected the fixture.
- The corrected full build passed all 261 required GameTests (build/skybreaker-mace-impact-corrected.log).
- Resumed review confirmed the direct strike uses Minecraft 1.21.1's mace hit handler for native sound, smash event, knockback and fall protection. A new regression check verifies that a direct falling hit still leads to a second damaging landing smash and zero landing fall damage, even after the mace resets the fall counter. The second full build passed all 261 required GameTests (`build/skybreaker-final-review.log`). Skybreaker review is complete.
- In-game appearance has not been previewed.
- The queued request in latestPromts.md for fall-damage immunity is complete. Skybreaker landing already suppressed fall damage. Ordinary Windcharge now protects one landing after launch on daggers, claymores and lower-rank axes; switching stacks, invalid flight conditions and timeout end that protection. Real fall-damage and all-variant event tests passed in two full 262-test NeoForge 21.1.93 runs (`build/windcharge-fall-protection.log` and `build/windcharge-fall-protection-rerun.log`).
