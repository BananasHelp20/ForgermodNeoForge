# Infusion table: edited gemsetting bench

The in-game model now follows your edited JSON:30 authored objects, with the gemstone placeholder excluded from the29-cube static mesh. All edited geometry, object names and rotations are preserved. Only the hammer handle is wooden; all remaining structural/tool materials are dark metals with red accents.

Each occupied input slot is displayed immediately, independently of the other slots or recipe progress: the actual weapon lies flat on the small anvil, and one gemstone lies in the tray. Missing templates and blocked output do not hide these previews. Removing an input clears only its own display; completion removes the consumed weapon and keeps any spare gemstones visible.

During valid infusion, the gemstone rises from its tray to hover over the weapon, turns upright and rotates with a gentle bob. Gemstone-colored sparks circle it while enchantment particles gather above the anvil. Start/stop motion eases over one second; cancellation/completion stops particle emission and settles any remaining gemstone into its tray. This animation renders the real items, follows the exported display anchors and block facing, and creates no ticking item entities. Cosmetic packets carry only single-item display copies and an animation flag; client-local ticks animate them without sending progress packets every tick. Weapon components/augments and independent table recipe state are preserved.

Open `src/main/resources/assets/forgermod/models/block/infusion_table/infusion_table_concept.bbmodel` for the full editable project. Textures are embedded. The dynamic gemstone placeholder is in a hidden, non-exporting preview group.

`infusion_table_concept.json` remains the editable source. Run `python scripts/sync_infusion_model.py` to refresh the registered game model, eight facing/lit variants, display anchor positions and complete Blockbench project. The script applies the proportional UV and hammer-material rules. Crafting durations/costs and gemstone eligibility remain unchanged.

`infusion_table_concept.png` shows idle and active views. See `infusion_table_texture_notes.md` for the custom extended metal texture, generation method and prompt. Native regression tests and real Minecraft checks cover the display lifecycle, materials, different weapon types and facings.
