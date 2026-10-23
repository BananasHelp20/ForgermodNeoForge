# Ability review implementation (2026-10-22)

Work requested in the current conversation. Resume the interactive review at **43: Magma Toss** after implementation. The user authorizes implementation now, superseding the earlier review-only restriction. Untouched later entries get current-rank values highlighted white without mechanical changes.

## Implementation queue

- [x] Shared dynamic descriptions: gray prose, white current-rank values, no slash-separated rank previews. Use in weapon/template tooltips and augmentation cards/hover. Keep localization and configurable keys.
- [x] Area Discharge I-IV cooldown 180/130/80/30s; description exactly user-provided. Anger of Zeus super I-IV: 1/2/3/4 waves to 10/20/30/40 blocks, 500ms apart, targets selected/marked uniquely at activation; skip empty waves. Initial hits 2x weapon damage; 50% chance per strike of five-chain followups, within 10 blocks of previous target, 200ms per jump, .5x damage, no repeated targets. Cooldowns 300/260/220/180s. Hostile and aggroed neutral targets, Charge Attack interaction.
- [x] Blood Rush description: On activation pays 1 heart to gain speed II for 10 seconds
- [x] Blood Sacrafice: half combined current HP+absorption, absorption spent first (20+5 ->12.5+0). 15/20/25/30s (template V 35), constant90s cooldown, double weapon damage.
- [ ] Bone Guard / Necromancer: 10-minute lifetime, activation cooldown, resummon replaces group beside owner. Bone Guard dynamic roster description. Necromancer Shift+Alt editor, capacity4/6/8/10, no bosses/zombie horses; cost1 zombie/skeleton/zombie villager; cost2 husk/drowned/stray/bogged/zombified piglin/phantom; cost3 wither skeleton/skeleton horse WITH rider; cost4 zoglin. From superII persistent real-item armor/weapon inventory per selected slot; durability/enchantments, no drops, retained across death/spawn, return to player or feet on removal/change. Editing only when group inactive (no living guards). Exact user description retained, roster white.
- [x] Challenge Roar: dynamic range/Strength/duration, hard target lock for duration. Intimidating Roar super35/40/45/50 range,20/24/27/30s,60s cooldown. WeaknessI+SlownessI aura while Strength active; dynamic description approved.
- [x] Charge Attack: max+200% on charge hit15; discharge16 adds30 lightning damage and strikes wielder if Lightning Rod learned. Low end stays20 charge hits,+100%,20 lightning. White dynamic increments/count/damage, conditional max+LightningRod sentence.
- [x] Cinder Decoy: max health equals current ownerHP at creation, excludes absorption; accumulates damage; no touch/single-hit instant detonation;600s lifetime, expiry/death/recast explode; activation cooldown enables recast. All hostile attraction, white lifetime/radius/burn.
- [x] Crosswind: existing wording with current cooldown white.
- [ ] Crucible Hook: exact user description, white damage/burn. Crucible Grappler super: aimed block grappling25/30/35/40 range, acceleration and preserved release velocity, rightclick/switch release, autoarrival setting defaulttrue. Mob hit2.25/2.5/2.75/3x, burn7/8/9/10s, cooldown25s.
- [x] Death Stare: exact user conditional description, range sentence omitted at max.
- [x] Deep Wound capIII cooldown120/75/30; exact user description white amplifier/duration.
- [x] Delusion I-V: effects10/14/18/22/25s, cooldown120/100/80/60/40s; exact user description white duration.
- [ ] Direct Current: aim+key per unique anchor, consecutive edges only, marked enemies immune to own connection damage. White amount and damage. Static Field >2 anchors filled field, pulses existing1s damage1/1.25/1.5/1.75, .5s stun,20s starts at first connection, not refreshed on marks.
- [x] Displace: exact conditional description; belowmax any living entity in range, max any visible entity unlimited loaded range.
- [x] Downdraft Bell: exact user description, radius white.
- [x] Dream Feast I-V restores2/4/6/8/10 hunger AND saturation; cooldown120/100/75/50/30s, white1/2/3/4/5 hunger bars.
- [x] Echo Hearing exact user description white listen/range/glow, glow1s atI ->2s max (+1 Jade).
- [x] Ender Swing renamed Gravity Swing (retain ID): capIII radius2/3.5/5 lift10/15/20 gravity+50/100/150% until landing; amplify fall damage; cooldown90/75/60. White radius/height/gravity.
- [ ] Explosion unchanged base description; Blazing Blast super: same blast/resistance+4 ghast projectiles cardinal relative facing+8 blaze intermediate dirs, projectile damage100/115/130/145%, cooldown120/110/100/90s. Terrain manipulation setting shared Explosion/BlazingBlast defaultfalse including projectiles.
- [x] Explosive Hits exact user description white additionaldamage%/radius, existing mechanics.
- [x] Eye of Storm: enemies only, radius5 ->12 atV,duration5 ->20 atV including Levitation; trapped enemies follow moving storm.
- [x] False Awakening: decoy beside aimed enemy, repeat bonus hits while target distracted for duration, no one-hit consume. Exact user description whitepercent/duration.
- [x] Feather Shelter exact user description radius/duration white.
- [x] Flame Breath exact user description range/burn white.
- [x] Flameborn: directdamage.5x atI ->1.5x at existingmaxII; all entities ignited; existing FireResistance30s/range10/burn5(or7super). Exact user description white damage/range/burn.
- [x] Flaming Stomp exact user description white damage/burn.
- [x] Ghostwalk exact user description white duration, only successful enemy strike breaks invisibility, not air swing. Jumpscare same with white doubled damage.
- [x] Gravity Well: single aimed enemy pull then automatic vertical strike, exact user description white range.
- [x] Green Feast:1wheat,2HP heal, hunger/saturation2/4/6/8 (+10templateV), existing cooldowns. Exact user description white health/hungerbars.
- [x] Just a Dream capII cooldown60/30s; exact user description.
- [x] Live Wire exact user description white duration/hearts, mechanics retained.
- [x] Lucid Dreaming shared Speed/fallreduction duration30s,50% reduction; white Speedamplifier/percentage/duration.
- [x] Lullaby exact user description white range/duration.
- [ ] Verify, changelog, session handoff and resume Magma Toss showing highlights. Per AGENTS implement/test/review/commit/push each ability before proceeding.

## Notes

- User confirmed chains may extend beyond Anger of Zeus's initial 10-40-block wave radius; each jump is still limited to 10 blocks from the previous target. All previously reviewed abilities require their full agreed mechanics, not description-only changes. Unreviewed abilities receive formatting only.

- Actual checkout is E:/Schule/4-Klasse/Projects/Mods/ForgermodNeoForge. D: contains only .idea.
- Initial worktree clean, HEAD b137f58. Local code differs from public GitHub descriptions; use local sources.
- Merged origin/main (9481893) preserving all newer upstream work, resolved shared description integration; merge 56bc1f7 pushed successfully. All 312 required tests pass in build/merged-description-highlights-reviewed.log. Blood Sacrafice: build, 9,801 cost invariant cases and all 312 GameTests pass in build/blood-sacrifice-reviewed.log.
- Shared formatter build/lightweight tests pass; 279/279 required GameTests passed in build/current-descriptions-reviewed.log. Updated the legacy tooltip test to expect the new current-description translation keys; added a real component/style test for unreviewed Magma Toss.
