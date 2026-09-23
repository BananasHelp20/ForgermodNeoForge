package net.bananashelp20.forgermod.recipe;

import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModSpecialRecipes {
    public static final Item[][] FORGE_RECIPE_INPUTS = {
            //shard recipes
            {ModItems.ELECTRIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            //{ModItems.DEVELOPIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()}, //!PRESERVE
            {ModItems.IGNISIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},

            //claymore recipes
            {ModItems.ELECTRIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.DEVELOPIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.IGNISIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},

            //knife
            {ModItems.ELECTRIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.DEVELOPIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.IGNISIUM_INGOT.get(), ModItems.CARBON_STEEL_KNIFE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},

            //axe
            {ModItems.ELECTRIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.DEVELOPIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.IGNISIUM_INGOT.get(), ModItems.CARBON_STEEL_AXE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()}
    };

    public static final ItemStack[] FORGE_RECIPE_OUTPUTS = {
            //shard recipe outputs
            new ItemStack(ModItems.ELECTRIUM_INGOT.get()),
            new ItemStack(ModItems.INANISIUM_INGOT.get()),
            new ItemStack(ModItems.PULSITE_INGOT.get()),
            new ItemStack(ModItems.SOMNIUM_INGOT.get()),
            new ItemStack(ModItems.VULNUSIUM_INGOT.get()),
            new ItemStack(ModItems.MORSIUM_INGOT.get()),
            new ItemStack(ModItems.LUSH_INGOT.get()),
            new ItemStack(ModItems.TAIFUNITE_INGOT.get()),
            //new ItemStack(ModItems.DEVELOPIUM_INGOT.get()),
            new ItemStack(ModItems.IGNISIUM_INGOT.get()),

            //claymore recipe outputs
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE.get()),
            new ItemStack(ModItems.STUMPFL_BAT.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),

            //knife recipe outputs
            new ItemStack(ModItems.KNIFE_OF_THUNDER.get()),
            new ItemStack(ModItems.KNIFE_OF_THE_VOID.get()),
            new ItemStack(ModItems.SHRIEKING_KNIFE.get()),
            new ItemStack(ModItems.DREAMBOUND_KNIFE.get()),
            new ItemStack(ModItems.CURSEBLOOD_KNIFE.get()),
            new ItemStack(ModItems.HOLLOW_KNIFE.get()),
            new ItemStack(ModItems.OVERGROWN_KNIFE.get()),
            new ItemStack(ModItems.STORMING_KNIFE.get()),
            new ItemStack(ModItems.STUMPFL_BAT.get()),
            new ItemStack(ModItems.INFERNAL_KNIFE.get()),

            //axe recipe outputs
            new ItemStack(ModItems.AXE_OF_THUNDER.get()),
            new ItemStack(ModItems.AXE_OF_THE_VOID.get()),
            new ItemStack(ModItems.SHRIEKING_AXE.get()),
            new ItemStack(ModItems.DREAMBOUND_AXE.get()),
            new ItemStack(ModItems.CURSEBLOOD_AXE.get()),
            new ItemStack(ModItems.HOLLOW_AXE.get()),
            new ItemStack(ModItems.OVERGROWN_AXE.get()),
            new ItemStack(ModItems.STORMING_AXE.get()),
            new ItemStack(ModItems.STUMPFL_BAT.get()),
            new ItemStack(ModItems.INFERNAL_AXE.get())
    };


    public static final Item[][] INFUSION_TABLE_RECIPE_INPUTS = {
            {ModItems.RUBY_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            //knife
            {ModItems.RUBY_GEMSTONE.get(), ModItems.KNIFE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.KNIFE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.KNIFE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.KNIFE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.INFERNAL_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.INFERNAL_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.INFERNAL_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.INFERNAL_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.KNIFE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.KNIFE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.KNIFE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.KNIFE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.OVERGROWN_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.OVERGROWN_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.OVERGROWN_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.OVERGROWN_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.HOLLOW_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.HOLLOW_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.HOLLOW_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.HOLLOW_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.CURSEBLOOD_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CURSEBLOOD_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CURSEBLOOD_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CURSEBLOOD_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DREAMBOUND_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DREAMBOUND_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DREAMBOUND_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DREAMBOUND_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.SHRIEKING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.SHRIEKING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.SHRIEKING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.SHRIEKING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.STORMING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.STORMING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.STORMING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.STORMING_KNIFE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            //axe
            {ModItems.RUBY_GEMSTONE.get(), ModItems.AXE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.AXE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.AXE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.AXE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.INFERNAL_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.INFERNAL_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.INFERNAL_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.INFERNAL_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.AXE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.AXE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.AXE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.AXE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.OVERGROWN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.OVERGROWN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.OVERGROWN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.OVERGROWN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.HOLLOW_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.HOLLOW_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.HOLLOW_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.HOLLOW_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.CURSEBLOOD_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CURSEBLOOD_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CURSEBLOOD_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CURSEBLOOD_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DREAMBOUND_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DREAMBOUND_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DREAMBOUND_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DREAMBOUND_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.SHRIEKING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.SHRIEKING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.SHRIEKING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.SHRIEKING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.STORMING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.STORMING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.STORMING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.STORMING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
    };

    public static final ItemStack[] INFUSION_TABLE_RECIPE_OUTPUTS = {
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_RUBY.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_AMBER.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_JADE.get()),

            new ItemStack(ModItems.INFERNAL_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_RUBY.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_AMBER.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_JADE.get()),

            new ItemStack(ModItems.OVERGROWN_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.HOLLOW_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.SHRIEKING_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.STORMING_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE_JADE.get()),

            //knife
            new ItemStack(ModItems.KNIFE_OF_THUNDER_RUBY.get()),
            new ItemStack(ModItems.KNIFE_OF_THUNDER_AMBER.get()),
            new ItemStack(ModItems.KNIFE_OF_THUNDER_AMETHYST.get()),
            new ItemStack(ModItems.KNIFE_OF_THUNDER_JADE.get()),

            new ItemStack(ModItems.INFERNAL_KNIFE_RUBY.get()),
            new ItemStack(ModItems.INFERNAL_KNIFE_AMBER.get()),
            new ItemStack(ModItems.INFERNAL_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.INFERNAL_KNIFE_JADE.get()),

            new ItemStack(ModItems.KNIFE_OF_THE_VOID_RUBY.get()),
            new ItemStack(ModItems.KNIFE_OF_THE_VOID_AMBER.get()),
            new ItemStack(ModItems.KNIFE_OF_THE_VOID_AMETHYST.get()),
            new ItemStack(ModItems.KNIFE_OF_THE_VOID_JADE.get()),

            new ItemStack(ModItems.OVERGROWN_KNIFE_RUBY.get()),
            new ItemStack(ModItems.OVERGROWN_KNIFE_AMBER.get()),
            new ItemStack(ModItems.OVERGROWN_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.OVERGROWN_KNIFE_JADE.get()),

            new ItemStack(ModItems.HOLLOW_KNIFE_RUBY.get()),
            new ItemStack(ModItems.HOLLOW_KNIFE_AMBER.get()),
            new ItemStack(ModItems.HOLLOW_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.HOLLOW_KNIFE_JADE.get()),

            new ItemStack(ModItems.CURSEBLOOD_KNIFE_RUBY.get()),
            new ItemStack(ModItems.CURSEBLOOD_KNIFE_AMBER.get()),
            new ItemStack(ModItems.CURSEBLOOD_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.CURSEBLOOD_KNIFE_JADE.get()),

            new ItemStack(ModItems.DREAMBOUND_KNIFE_RUBY.get()),
            new ItemStack(ModItems.DREAMBOUND_KNIFE_AMBER.get()),
            new ItemStack(ModItems.DREAMBOUND_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.DREAMBOUND_KNIFE_JADE.get()),

            new ItemStack(ModItems.SHRIEKING_KNIFE_RUBY.get()),
            new ItemStack(ModItems.SHRIEKING_KNIFE_AMBER.get()),
            new ItemStack(ModItems.SHRIEKING_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.SHRIEKING_KNIFE_JADE.get()),

            new ItemStack(ModItems.STORMING_KNIFE_RUBY.get()),
            new ItemStack(ModItems.STORMING_KNIFE_AMBER.get()),
            new ItemStack(ModItems.STORMING_KNIFE_AMETHYST.get()),
            new ItemStack(ModItems.STORMING_KNIFE_JADE.get()),

            //axe
            new ItemStack(ModItems.AXE_OF_THUNDER_RUBY.get()),
            new ItemStack(ModItems.AXE_OF_THUNDER_AMBER.get()),
            new ItemStack(ModItems.AXE_OF_THUNDER_AMETHYST.get()),
            new ItemStack(ModItems.AXE_OF_THUNDER_JADE.get()),

            new ItemStack(ModItems.INFERNAL_AXE_RUBY.get()),
            new ItemStack(ModItems.INFERNAL_AXE_AMBER.get()),
            new ItemStack(ModItems.INFERNAL_AXE_AMETHYST.get()),
            new ItemStack(ModItems.INFERNAL_AXE_JADE.get()),

            new ItemStack(ModItems.AXE_OF_THE_VOID_RUBY.get()),
            new ItemStack(ModItems.AXE_OF_THE_VOID_AMBER.get()),
            new ItemStack(ModItems.AXE_OF_THE_VOID_AMETHYST.get()),
            new ItemStack(ModItems.AXE_OF_THE_VOID_JADE.get()),

            new ItemStack(ModItems.OVERGROWN_AXE_RUBY.get()),
            new ItemStack(ModItems.OVERGROWN_AXE_AMBER.get()),
            new ItemStack(ModItems.OVERGROWN_AXE_AMETHYST.get()),
            new ItemStack(ModItems.OVERGROWN_AXE_JADE.get()),

            new ItemStack(ModItems.HOLLOW_AXE_RUBY.get()),
            new ItemStack(ModItems.HOLLOW_AXE_AMBER.get()),
            new ItemStack(ModItems.HOLLOW_AXE_AMETHYST.get()),
            new ItemStack(ModItems.HOLLOW_AXE_JADE.get()),

            new ItemStack(ModItems.CURSEBLOOD_AXE_RUBY.get()),
            new ItemStack(ModItems.CURSEBLOOD_AXE_AMBER.get()),
            new ItemStack(ModItems.CURSEBLOOD_AXE_AMETHYST.get()),
            new ItemStack(ModItems.CURSEBLOOD_AXE_JADE.get()),

            new ItemStack(ModItems.DREAMBOUND_AXE_RUBY.get()),
            new ItemStack(ModItems.DREAMBOUND_AXE_AMBER.get()),
            new ItemStack(ModItems.DREAMBOUND_AXE_AMETHYST.get()),
            new ItemStack(ModItems.DREAMBOUND_AXE_JADE.get()),

            new ItemStack(ModItems.SHRIEKING_AXE_RUBY.get()),
            new ItemStack(ModItems.SHRIEKING_AXE_AMBER.get()),
            new ItemStack(ModItems.SHRIEKING_AXE_AMETHYST.get()),
            new ItemStack(ModItems.SHRIEKING_AXE_JADE.get()),

            new ItemStack(ModItems.STORMING_AXE_RUBY.get()),
            new ItemStack(ModItems.STORMING_AXE_AMBER.get()),
            new ItemStack(ModItems.STORMING_AXE_AMETHYST.get()),
            new ItemStack(ModItems.STORMING_AXE_JADE.get()),
    };
}
