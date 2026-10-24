# Augment command

Requires operator permission level 2. Commands affect each target's main-hand item.

```text
/augment new <name-or-selector> <abilityName>
/augment upgrade <name-or-selector> <abilityName>
/augment super <name-or-selector> <abilityName>
```

- `new` learns a compatible, unapplied augment at level I.
- `upgrade` increases an already applied augment by one level, including super levels. It cannot exceed the item's augment cap.
- `super` applies the first super level directly or transforms an existing base augment. The weapon must support that super form. It never downgrades an existing super augment.

Examples:

```text
/augment new @s Thunderclap
/augment upgrade @s thunderclap
/augment super @s Static Field
/augment upgrade @s static_field
/augment super PlayerName forgermod:anger_of_zeus
```

Ability names are case-insensitive. Spaces, underscores, namespaced IDs and quoted names are supported. Super forms accept their own name or their underlying base augment's ID. For ambiguous base names, use `forgermod:ability/<name>` or `forgermod:passive/<name>`.

Autocomplete lists only abilities usable for the chosen operation and held items. Missing prerequisite augments are learned automatically at level I when the weapon has room for the complete dependency chain. Existing prerequisites retain their levels. Material compatibility and active/passive slot limits still apply; a failed operation leaves that target's item unchanged. Templates continue to store augments independently. Existing item names, enchantments and durability are preserved. Templates remain supported, and commands do not consume ingredients or increase the Augmentation Table use count.

Multiple-target selectors skip targets that cannot receive the requested change and report the number changed. If none can receive it, the command fails without changing their items.

For example, `/augment new @s flaming_stomp` also learns Vertical Strike if it is missing and both augments fit. This applies to upgrades that need a missing prerequisite too.
