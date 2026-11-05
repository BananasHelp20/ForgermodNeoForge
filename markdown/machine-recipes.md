# Forge and Infusion Table recipes

Both machines now use Minecraft's recipe manager. Recipes live under `data/<namespace>/recipe/` in the mod or a datapack; they are no longer selected from Java arrays. The migration includes all 39 Forge and 108 Infusion recipes.

## Forge example

```json
{
  "type": "forgermod:forge",
  "ingredient1": { "item": "forgermod:electrium_shard" },
  "ingredient2": { "item": "forgermod:carbon_steel_ingot" },
  "template": { "item": "forgermod:ancient_upgrade_template" },
  "count1": 4,
  "count2": 1,
  "template_count": 1,
  "processing_time": 250,
  "copy_components": false,
  "requires_jade_compatible": false,
  "result": { "id": "forgermod:electrium_ingot", "count": 1 }
}
```

`ingredient1` is the left/additive slot, `ingredient2` is the base-item slot, and `template` is the template slot. Ingredients support normal item/tag ingredient definitions, for example `{ "tag": "minecraft:planks" }`.

## Infusion example

```json
{
  "type": "forgermod:infusion_table",
  "ingredient1": { "item": "forgermod:ruby_gemstone" },
  "ingredient2": { "item": "forgermod:claymore_of_thunder" },
  "template": { "item": "forgermod:gemstone_upgrade_template" },
  "count1": 1,
  "count2": 1,
  "template_count": 0,
  "processing_time": 400,
  "copy_components": true,
  "requires_jade_compatible": false,
  "result": { "id": "forgermod:claymore_of_thunder_ruby", "count": 1 }
}
```

## Fields and defaults

| Field | Meaning | Forge default | Infusion default |
| --- | --- | --- | --- |
| `ingredient1`, `ingredient2`, `template` | Required item/tag ingredients | Required | Required |
| `result` | Output item stack, including count and optional components | Required | Required |
| `count1`, `count2` | Consumed additive/base counts, 1–64 | 1 | 1 |
| `template_count` | Consumed template count, 0–64 | 1 | 0 |
| `processing_time` | Server ticks per craft, 1–32767; 20 ticks = 1 second | 250 | 400 |
| `copy_components` | Preserve the base item's component patch on the new output | false | true |
| `requires_jade_compatible` | Require a learned, Jade-compatible status-effect augment on the base weapon | false | false |

A template with `template_count: 0` must still be present and match its ingredient. It is retained, not free/optional. Built-in Jade recipes explicitly enable `requires_jade_compatible`.

When components are copied, explicit components in the recipe's result override copied components. This preserves learned augments, durability, names and other changed data while retaining the output item's own defaults. Output merging requires identical items **and components** and enough capacity; mismatches, full outputs and insufficient materials consume nothing.

Input slots, shift-click routing and the normal weapon's infusion hint use loaded recipes rather than fixed item lists. Recipes added through datapacks can therefore use other materials or templates without a Java change. Invalid outputs remain manually insert-restricted.

## Existing recipe costs

- Special ingots: four shards + one carbon-steel ingot + one Ancient Upgrade Template, 250 ticks.
- Special weapon conversion: one material ingot + one base weapon + one Ancient Upgrade Template, 250 ticks.
- Gemstone infusion: one gemstone + one base weapon, with a retained Gemstone Template, 400 ticks.

The old Forge code accepted one material ingot for weapon conversion but extracted up to four depending on the stack size. That inconsistent behavior is replaced by the explicit one-ingot cost. The disabled Developium-shard conversion was not introduced by this migration.

## Reloads and progress

Use `/reload` after changing datapack recipes. Replacing a recipe under the same ID changes machine behavior; removing it stops matching. If definitions overlap, the matching recipe with the lexicographically first full recipe ID is selected, giving deterministic behavior. Prefer nonoverlapping definitions.

Each machine tracks its own recipe definition and input items/components. Changing either resets progress, preventing partial work on one recipe from finishing a different recipe. Increasing/decreasing a stack count without changing its item/components preserves progress if it still meets the costs. Unchanged progress survives save/load; old saves without recipe identity safely restart their current craft rather than transferring unverifiable progress.

## JEI

Forge and Infusion Table have separate optional JEI categories. They read the same synced recipes as the machines and show ingredient counts, outputs, processing times, template consumption/retention and Jade requirements. Both machine blocks are category catalysts, with recipe-transfer mappings for their actual menu slots.

JEI is not bundled or required on dedicated servers. The plugin is isolated from common machine code. For a development client with the tested JEI dependency:

```powershell
.\gradlew.bat -PwithJei runClient
```

Tested against the JEI 19.21.0.247 API on Minecraft 1.21.1 / NeoForge 21.1.93. The build and all 485 GameTests pass both without JEI and with JEI installed. This includes all 147 built-in recipe JSON/network-codec round trips, custom tag ingredients, quantities, recipe replacement/removal, saved progress and component-safe output handling. Live visual inspection of the JEI pages/transfer button is still a manual follow-up.

API setup reference: [JEI's official Minecraft 1.21–1.21.1 developer guide](https://github.com/mezz/JustEnoughItems/wiki/Getting-Started-%5BMinecraft-1.21-and-1.21.1%5D).
