# Infusion table metal texture

Final asset: `src/main/resources/assets/forgermod/textures/block/infusion_table/forged_netherite.png` (64 x 64, opaque).

Generated with the built-in image-generation tool, using the existing vanilla-style netherite block PNG as a palette/material reference. The generated surface was exported at64 x64 using nearest-neighbour sampling. The larger surface contains newly drawn grain, joins and small dents. Each model face receives a proportional rectangular UV region, so long posts and thin trims retain the same texture aspect ratio. The new material is local to the infusion table.

Only `slender_hammer_handle` uses stripped spruce side/end textures. The cabinet, worktop, frame, anvil, calipers and hammer head resolve to metal materials. A regression check verifies this in the exported game model.

## Generation prompt

Create a production Minecraft pixel-art texture, an expanded 64x64-texel dark forged metal plate surface, for a high-value weapon gemsetting table. The attached vanilla netherite block texture is ONLY a palette and material reference. Invent new coherent detail across the larger surface; DO NOT stretch, tile, copy, or repeat the 16x16 reference. Flat orthographic texture only, no perspective/object/render, no text, no labels, no padding, completely opaque and edge-to-edge. Restrained charcoal / muted violet-brown netherite-like metallic colors, thin dark plate joins, small hand-forged grain, occasional subtle edge glints and tiny dents. Understated Minecraft vanilla texture style, chunky square pixels on an EXACT 64 by 64 logical grid (may output enlarged with every texel an identical-sized solid-color square), no blur/no gradients/no smooth noise/no lighting vignette. Avoid prominent center icons, rivets, bolts, or large square frames, because arbitrary narrow rectangular UV regions will be used for thin vertical posts and trim. Texture density and grain should be consistent across the full image, naturally drawn extensions with no repeated motifs. Keep the value range fairly dark while allowing individual forged layers and subtle metal grain to remain legible.
