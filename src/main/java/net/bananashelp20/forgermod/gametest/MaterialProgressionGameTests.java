package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MaterialProgressionGameTests {
    private static double swordDamage(Player player, Item sword) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(sword));
        player.tick();
        return player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static boolean canHarvest(Item item, Block block) {
        Tool tool = new ItemStack(item).get(DataComponents.TOOL);
        return tool != null && tool.isCorrectForDrops(block.defaultBlockState());
    }

    @GameTest(template = "empty")
    public static void miningOrderAndOreRequirements(GameTestHelper test) {
        test.assertTrue(!canHarvest(Items.STONE_PICKAXE, ModBlocks.METEORIC_IRON_STONE_ORE.get()),
                "Stone unexpectedly harvests meteoric ore");
        test.assertTrue(canHarvest(Items.IRON_PICKAXE, ModBlocks.METEORIC_IRON_STONE_ORE.get()),
                "Iron cannot harvest meteoric ore");
        test.assertTrue(canHarvest(ModItems.TOOL_SETS.get("meteoric_iron").pickaxe().get(), Blocks.DIAMOND_ORE),
                "Meteoric iron cannot harvest diamond ore");
        test.assertTrue(!canHarvest(ModItems.TOOL_SETS.get("meteoric_iron").pickaxe().get(), Blocks.OBSIDIAN),
                "Meteoric iron skips diamond level");
        test.assertTrue(!canHarvest(ModItems.TOOL_SETS.get("steel").pickaxe().get(), Blocks.OBSIDIAN),
                "Steel skips diamond level");
        test.assertTrue(canHarvest(ModItems.TOOL_SETS.get("damask").pickaxe().get(), Blocks.OBSIDIAN),
                "Damask cannot harvest obsidian");
        test.assertTrue(!canHarvest(Items.DIAMOND_PICKAXE, ModBlocks.TUNGSTEN_STONE_ORE.get()),
                "Diamond skips titanium ore gate");
        test.assertTrue(!canHarvest(ModItems.TOOL_SETS.get("damask").pickaxe().get(), ModBlocks.TUNGSTEN_STONE_ORE.get()),
                "Damask skips titanium ore gate");
        test.assertTrue(canHarvest(ModItems.TOOL_SETS.get("titanium").pickaxe().get(), ModBlocks.TUNGSTEN_STONE_ORE.get()),
                "Titanium cannot harvest tungsten ore");
        test.assertTrue(!canHarvest(ModItems.TOOL_SETS.get("tungsten").pickaxe().get(), ModBlocks.INANISIUM_BLOCK.get()),
                "Tungsten skips carbon steel requirement");
        test.assertTrue(canHarvest(ModItems.TOOL_SETS.get("carbon_steel").pickaxe().get(), ModBlocks.INANISIUM_BLOCK.get()),
                "Carbon steel cannot harvest special material block");
        for (var set : ModItems.TOOL_SETS.values()) {
            test.assertTrue(AxeItems.canVerticalStrike(new ItemStack(set.axe().get())),
                    "Material axe lost its heavy strike: " + set.axe().getId());
        }
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void swordAndPickaxeStatsFollowProgression(GameTestHelper test) {
        String[] materials = {"scrap", "meteoric_iron", "reinforced_iron", "steel",
                "damask", "titanium", "tungsten", "carbon_steel", "inanisium"};
        Player player = test.makeMockPlayer(GameType.CREATIVE);
        double previousDamage = swordDamage(player, Items.IRON_SWORD);
        double previousDps = previousDamage * player.getAttributeValue(Attributes.ATTACK_SPEED);
        float previousSpeed = Tiers.IRON.getSpeed();
        int previousDurability = new ItemStack(Items.IRON_PICKAXE).getMaxDamage();
        for (String material : materials) {
            var set = ModItems.TOOL_SETS.get(material);
            double damage = swordDamage(player, set.sword().get());
            double dps = damage * player.getAttributeValue(Attributes.ATTACK_SPEED);
            float speed = ((net.minecraft.world.item.DiggerItem) set.pickaxe().get()).getTier().getSpeed();
            int durability = new ItemStack(set.pickaxe().get()).getMaxDamage();
            test.assertTrue(damage >= previousDamage, material + " sword loses damage over the prior step");
            test.assertTrue(dps > previousDps || "scrap".equals(material) && dps == previousDps,
                    material + " sword loses combat output over the prior step");
            test.assertTrue(speed >= previousSpeed, material + " pickaxe loses mining speed over the prior step");
            test.assertTrue(durability >= previousDurability, material + " pickaxe loses durability over the prior step");
            previousDamage = damage;
            previousDps = dps;
            previousSpeed = speed;
            previousDurability = durability;
            if ("steel".equals(material)) {
                test.assertTrue(damage < swordDamage(player, Items.DIAMOND_SWORD),
                        "Steel sword overtakes Diamond");
                test.assertTrue(speed < Tiers.DIAMOND.getSpeed(), "Steel pickaxe overtakes Diamond mining speed");
                test.assertTrue(durability < new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage(),
                        "Steel pickaxe overtakes Diamond durability");
                previousDamage = swordDamage(player, Items.DIAMOND_SWORD);
                previousDps = previousDamage * player.getAttributeValue(Attributes.ATTACK_SPEED);
                previousSpeed = Tiers.DIAMOND.getSpeed();
                previousDurability = new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage();
            }
        }
        test.succeed();
    }
}
