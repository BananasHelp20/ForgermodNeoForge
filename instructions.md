- I have already worked out a structure for the ancient Sword stand. it lies in: D:\Minecraft Modding internal\Mods\ForgermodNeoForge\run\saves\Structure\generated\minecraft\structures
and is named forgermod_ancient_grave_v2 as a nbt file. The structure should be named the Ancient Grave and spawn as a guarantied room of an ancient city. (up to one)
- implement a command (/no-ability-cooldown [target] true) that disables all abilitie cooldowns for the target until you do "/no-ability-cooldown [target] false"
- the sonic charge hits all mobs in the player view direction. the "projectile" (area of the projectile, which pierces through all entities) has a 3x3 area (with the middle of the 3x3 area being in the face of the player).
The boom is only stopped by solid blocks the player cannot walk through. and only if the center block (of the 3x3) is blocked. (so shooting through a 1x1 hole is possible if the player is locking through it). It pierces ALL enemies on its way (all entities except for items), not just a few.
- the animatione of the dual wield attack looks like the offhand hits tiwice and the main hand doesn't
- The first infusion table slot (visually the one on the most left of the screen) should only accept gemstones as input (when shift clicking, or trying to place it in)
- The second infusion table slot should only accept the gemstone template as input (when shift clicking, or trying to place it in)
- The thrid infusion table slot should only accept Gemstone-infusion able items (when shift clicking, or trying to place it in)
- The Infusion table output slot should not accept input.
- The Forge output slot should not accept input
- The Template slot in the Forge should only accept upgrade templates (any upgrade template)
- The augmentation table UI should look simelar to the Forge and Infusion table UI (minecraft-like).
- when pressing shift+alt+s while holding an item that has 2 abilities (ignores offhanditem), the primary and secondary ability switch places, making the previous primary ability the secondary ability and the other one the primary abilty.
