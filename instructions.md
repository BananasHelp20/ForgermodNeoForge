make the forge, infusion table and augmentation table aswell as all crafting table, furnace and blast furnace recipes jei compatible at all times.
Check an item only after its implementation, relevant tests, and code review are complete. Existing instructions above remain intact; this checklist tracks the current work queue.

- [x] Rename Claymore Blade, add Axe Blade/Dagger Blade/Augmented Heavy Core, implement battle axe and dagger assembly recipes, and verify models, glint, recipes and save compatibility. Use the approved blade recipes; convert a vanilla Heavy Core automatically in the Augmentation Table without ability choices.
- [x] Make normal infusion consume one template per completed craft, and verify that forging also consumes its template without spending inputs on unfinished or blocked crafts.

- [x] Color the Super Augment Template item name legendary yellow.

- [x] Import Downloads/assembler.bbmodel and its embedded texture for the Weapon Assembler; verify the placed block, item model, facing and collision without changing crafting behavior.

- [x] Prepare hinged assembler-arm animations and synchronized start/stop hooks, with smooth return to rest. User confirmed crafting itself comes later; do not add recipes or animate merely from an inserted template.

- [x] Color the normal Augment Template item name epic purple.

- [x] Center the Augmentation Table item in GUI slots and verify it fits without changing the placed model.
