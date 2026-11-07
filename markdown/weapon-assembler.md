# Weapon Assembler and Ancient Crafting Template

The existing `forgermod:ancient_upgrade_template` registry ID is retained for saved items and existing recipes. Its display name is now **Ancient Crafting Template**. Its original sprite is unchanged.

- Insert a blank Ancient Crafting Template into the Augmentation Table. The existing ingredient pair is one Gemstone Upgrade Template and one Sapphire Gemstone, consumed only after selecting an offer.
- The table offers two different random choices from **Claymore**, **Battle Axe** and **Dagger**, using its existing selection workflow. Choosing one permanently specializes that one template; it cannot gain another specialization or additional levels.
- These choices are exclusive to the Ancient Crafting Template. Ordinary and Super Augmenting Templates cannot learn, extract or transfer them. Weapon ability pools are unchanged.
- Specialized templates receive blue foil rendering over the unchanged sprite. Ordinary augment-template foil keeps its existing color.
- The Ancient template multiplication recipe and its advancement were removed from generated resources and the recipe generator. The original recipe for creating a blank template remains.
- JEI shows the three possible specialization results, the blank subject and both consumed ingredients, plus the Augmentation Table catalyst and random-offer rule.

## Assembler stage currently available

The Weapon Assembler is registered in the Miscellaneous creative tab. Right-click to open its template slot. Only specialized Ancient Crafting Templates are accepted, one at a time. The selected specialization appears immediately. Removing it returns to the insertion prompt. The template persists through saving/loading and drops once when the block is broken. The block currently reuses vanilla smithing-table textures.

The user will specify the Claymore, Battle Axe and Dagger crafting layouts and their recipes later. Until then the screen explicitly states that its layout is undefined, and there are no ingredient slots, crafting outputs or assembler crafting recipes. No template is consumed merely by inserting it. **Successful weapon assembly must consume the specialized template** when the recipes are implemented, and those recipes must be visible in JEI with exact quantities and this consumption rule. Existing Forge recipes remain available in the meantime.

Redstone automation was cancelled by the user and is not implemented.
