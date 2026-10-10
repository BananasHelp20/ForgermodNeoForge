# Codex-added abilities withdrawn for review

These were my design choices, not approved weapon designs from the user. They entered with augmentation commit `2ec39a8`; the later tooltip/learning work in `823942c` reused them. This document records their former behavior. They are withdrawn from gameplay, not recommendations already approved for implementation.

## Invented abilities

### Guarded

- Former ID: `augmentation.forgermod.guarded`.
- A generic learnable passive offered to every special dagger, claymore and axe, including gemstone variants.
- Reduced incoming damage while held in the main hand: 5% at I, 10% at II, 15% at III and 20% at IV.
- Occupied one of the two unique passive slots. Required no activation and had no cooldown.
- Removed its offers, translations and damage event handler. Existing saved Guarded entries are ignored and no longer occupy a slot or reduce damage.

### Empowered Hit

- Former ID: `augmentation.forgermod.empowered_hit`.
- Initially an extra generic passive extending the weapon's material effect by 25% of its base duration per rank. The later learned-only implementation made I unlock the material effect, II extend it by 25%, III by 50%, and IV by 75%.
- The extra duration was my invention. It is removed, including the tooltip duration multiplier.
- **Material Hit learning itself stays:** you explicitly requested fresh weapons without material abilities and showed Weakness Hit IV in the maxed Deathwisper example. Its rank remains separate from the two unique passive slots. All ranks currently apply the approved baseline effect; numeric rank bonuses await your design.
- Existing saved Empowered Hit ranks migrate to `augmentation.forgermod.material_hit`, retaining learning and rank but granting no invented duration bonus. Gemstone and weapon-type adjustments remain.

## Unapproved assignments also withdrawn

I reused each material's dagger ability pool for its claymore and axe. You requested shared material effects, not these shared active/unique-passive designs. These assignments are removed from offers and saved learned entries on claymores/axes:

| Material | Copied actives | Copied unique passives |
| --- | --- | --- |
| Ignisium | Flaming Combo; Pyromaniac | None |
| Inanisium | Stepping Through Void; Sudden Presence | None |
| Somnium | Absolute Nightmare; Lucid Dreaming | None |
| Electrium | Area Discharge; Charge Attack | Chain Lightning |
| Taifunite | Eye of the Storm; Windy Dash | Double Jump |
| Vulnusium | Deep Wound; Leech | None |
| Overgrown/Lush | Rooting Roots; Poisoned Vein | None |
| Morsium | Strengthened Bones; Storing Anger | Death March; Revenge |
| Pulsite | Sonically Charged Crit; Sonic Boom | None |

The original dagger abilities in this table are user-authored and remain learnable on their respective daggers. This withdrawal does not remove those approved dagger implementations.

## Invented upgrade rules also withdrawn

- All active abilities: each upgrade reduced cooldown by 15% of the base, giving I/II/III/IV cooldown multipliers of 100%/85%/70%/55%. Restored approved base cooldowns at every rank.
- Chain Lightning: each upgrade added 25% of its base chained damage, reaching 175% at IV. Restored the approved half-current-weapon-damage calculation, including the user-requested Charge Attack interaction.
- Death March and Revenge: each upgrade enlarged their existing damage bonus by 25% of the base bonus. Restored their original damage formulas.
- Double Jump: each upgrade added 0.05 upward velocity, from 0.55 at I to 0.70 at IV. Restored 0.55 lift, retaining the approved horizontal momentum preservation.

Ranks I–IV, ingredient costs, table cooking, key swapping and independent cooldown tracking remain. No replacement rank bonuses have been invented. Until you define upgrades, higher ranks record progression without changing combat strength.

## Saved-game handling

- No weapon, item component, gemstone, name, enchantment or durability is deleted. Historical augmentation counts remain, so previous spending and cooking progression are not rewritten.
- Removed ability entries become inert; saving a later augmentation or swap writes only valid learned abilities.
- Tables already cooking or awaiting a removed choice receive legal replacement offers without another payment. If no legal upgrade remains, cooking is cancelled, the weapon becomes removable, and the spent template/sapphire are refunded. An overflowing refund drops beside the table.
- Intrinsic Dual Wield and axe slam, approved material effects and prior explicitly requested dagger behavior remain.
- `ability-thoughts.txt` is unchanged. Its excluded section is not treated as an approved implementation list.

New proposals are in [abilityRecommendations.md](abilityRecommendations.md). None are implemented.
