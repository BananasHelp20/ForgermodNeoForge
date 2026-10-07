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
            {ModItems.ELECTRIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.DEVELOPIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.IGNISIUM_INGOT.get(), ModItems.CARBON_STEEL_DAGGER.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},

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
            new ItemStack(ModItems.STATIC_DAGGER.get()),
            new ItemStack(ModItems.RIFTFANG_DAGGER.get()),
            new ItemStack(ModItems.WARDENS_NEEDLE.get()),
            new ItemStack(ModItems.NIGHTMARE_DAGGER.get()),
            new ItemStack(ModItems.ASSASSIN_DAGGER.get()),
            new ItemStack(ModItems.DEATHWISPER_DAGGER.get()),
            new ItemStack(ModItems.LEAFCUTTER_DAGGER.get()),
            new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),
            new ItemStack(ModItems.STUMPFL_BAT.get()),
            new ItemStack(ModItems.EMBERFANG_DAGGER.get()),

            //axe recipe outputs
            new ItemStack(ModItems.VOLTAGE_AXE.get()),
            new ItemStack(ModItems.NULLIFIED_AXE.get()),
            new ItemStack(ModItems.ECHOING_AXE.get()),
            new ItemStack(ModItems.DREAMWEAVER_AXE.get()),
            new ItemStack(ModItems.WOUNDMAKER_AXE.get()),
            new ItemStack(ModItems.GHOST_AXE.get()),
            new ItemStack(ModItems.VERDANT_AXE.get()),
            new ItemStack(ModItems.SKYBREAKER_AXE.get()),
            new ItemStack(ModItems.STUMPFL_BAT.get()),
            new ItemStack(ModItems.MOLTEN_AXE.get())
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
            {ModItems.RUBY_GEMSTONE.get(), ModItems.STATIC_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.STATIC_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.STATIC_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.STATIC_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.EMBERFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.EMBERFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.EMBERFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.EMBERFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.RIFTFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.RIFTFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.RIFTFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.RIFTFANG_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.LEAFCUTTER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.LEAFCUTTER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.LEAFCUTTER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.LEAFCUTTER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DEATHWISPER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DEATHWISPER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DEATHWISPER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DEATHWISPER_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.ASSASSIN_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.ASSASSIN_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.ASSASSIN_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.ASSASSIN_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.NIGHTMARE_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.NIGHTMARE_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.NIGHTMARE_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.NIGHTMARE_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.WARDENS_NEEDLE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.WARDENS_NEEDLE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.WARDENS_NEEDLE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.WARDENS_NEEDLE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DEAD_CALM_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DEAD_CALM_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DEAD_CALM_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DEAD_CALM_DAGGER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            //axe
            {ModItems.RUBY_GEMSTONE.get(), ModItems.VOLTAGE_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.VOLTAGE_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.VOLTAGE_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.VOLTAGE_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.MOLTEN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.MOLTEN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.MOLTEN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.MOLTEN_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.NULLIFIED_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.NULLIFIED_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.NULLIFIED_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.NULLIFIED_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.VERDANT_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.VERDANT_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.VERDANT_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.VERDANT_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.GHOST_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.GHOST_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.GHOST_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.GHOST_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.WOUNDMAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.WOUNDMAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.WOUNDMAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.WOUNDMAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DREAMWEAVER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DREAMWEAVER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DREAMWEAVER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DREAMWEAVER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.ECHOING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.ECHOING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.ECHOING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.ECHOING_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.SKYBREAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.SKYBREAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.SKYBREAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.SKYBREAKER_AXE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
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
            new ItemStack(ModItems.STATIC_DAGGER_RUBY.get()),
            new ItemStack(ModItems.STATIC_DAGGER_AMBER.get()),
            new ItemStack(ModItems.STATIC_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.STATIC_DAGGER_JADE.get()),

            new ItemStack(ModItems.EMBERFANG_DAGGER_RUBY.get()),
            new ItemStack(ModItems.EMBERFANG_DAGGER_AMBER.get()),
            new ItemStack(ModItems.EMBERFANG_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.EMBERFANG_DAGGER_JADE.get()),

            new ItemStack(ModItems.RIFTFANG_DAGGER_RUBY.get()),
            new ItemStack(ModItems.RIFTFANG_DAGGER_AMBER.get()),
            new ItemStack(ModItems.RIFTFANG_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.RIFTFANG_DAGGER_JADE.get()),

            new ItemStack(ModItems.LEAFCUTTER_DAGGER_RUBY.get()),
            new ItemStack(ModItems.LEAFCUTTER_DAGGER_AMBER.get()),
            new ItemStack(ModItems.LEAFCUTTER_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.LEAFCUTTER_DAGGER_JADE.get()),

            new ItemStack(ModItems.DEATHWISPER_DAGGER_RUBY.get()),
            new ItemStack(ModItems.DEATHWISPER_DAGGER_AMBER.get()),
            new ItemStack(ModItems.DEATHWISPER_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.DEATHWISPER_DAGGER_JADE.get()),

            new ItemStack(ModItems.ASSASSIN_DAGGER_RUBY.get()),
            new ItemStack(ModItems.ASSASSIN_DAGGER_AMBER.get()),
            new ItemStack(ModItems.ASSASSIN_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.ASSASSIN_DAGGER_JADE.get()),

            new ItemStack(ModItems.NIGHTMARE_DAGGER_RUBY.get()),
            new ItemStack(ModItems.NIGHTMARE_DAGGER_AMBER.get()),
            new ItemStack(ModItems.NIGHTMARE_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.NIGHTMARE_DAGGER_JADE.get()),

            new ItemStack(ModItems.WARDENS_NEEDLE_RUBY.get()),
            new ItemStack(ModItems.WARDENS_NEEDLE_AMBER.get()),
            new ItemStack(ModItems.WARDENS_NEEDLE_AMETHYST.get()),
            new ItemStack(ModItems.WARDENS_NEEDLE_JADE.get()),

            new ItemStack(ModItems.DEAD_CALM_DAGGER_RUBY.get()),
            new ItemStack(ModItems.DEAD_CALM_DAGGER_AMBER.get()),
            new ItemStack(ModItems.DEAD_CALM_DAGGER_AMETHYST.get()),
            new ItemStack(ModItems.DEAD_CALM_DAGGER_JADE.get()),

            //axe
            new ItemStack(ModItems.VOLTAGE_AXE_RUBY.get()),
            new ItemStack(ModItems.VOLTAGE_AXE_AMBER.get()),
            new ItemStack(ModItems.VOLTAGE_AXE_AMETHYST.get()),
            new ItemStack(ModItems.VOLTAGE_AXE_JADE.get()),

            new ItemStack(ModItems.MOLTEN_AXE_RUBY.get()),
            new ItemStack(ModItems.MOLTEN_AXE_AMBER.get()),
            new ItemStack(ModItems.MOLTEN_AXE_AMETHYST.get()),
            new ItemStack(ModItems.MOLTEN_AXE_JADE.get()),

            new ItemStack(ModItems.NULLIFIED_AXE_RUBY.get()),
            new ItemStack(ModItems.NULLIFIED_AXE_AMBER.get()),
            new ItemStack(ModItems.NULLIFIED_AXE_AMETHYST.get()),
            new ItemStack(ModItems.NULLIFIED_AXE_JADE.get()),

            new ItemStack(ModItems.VERDANT_AXE_RUBY.get()),
            new ItemStack(ModItems.VERDANT_AXE_AMBER.get()),
            new ItemStack(ModItems.VERDANT_AXE_AMETHYST.get()),
            new ItemStack(ModItems.VERDANT_AXE_JADE.get()),

            new ItemStack(ModItems.GHOST_AXE_RUBY.get()),
            new ItemStack(ModItems.GHOST_AXE_AMBER.get()),
            new ItemStack(ModItems.GHOST_AXE_AMETHYST.get()),
            new ItemStack(ModItems.GHOST_AXE_JADE.get()),

            new ItemStack(ModItems.WOUNDMAKER_AXE_RUBY.get()),
            new ItemStack(ModItems.WOUNDMAKER_AXE_AMBER.get()),
            new ItemStack(ModItems.WOUNDMAKER_AXE_AMETHYST.get()),
            new ItemStack(ModItems.WOUNDMAKER_AXE_JADE.get()),

            new ItemStack(ModItems.DREAMWEAVER_AXE_RUBY.get()),
            new ItemStack(ModItems.DREAMWEAVER_AXE_AMBER.get()),
            new ItemStack(ModItems.DREAMWEAVER_AXE_AMETHYST.get()),
            new ItemStack(ModItems.DREAMWEAVER_AXE_JADE.get()),

            new ItemStack(ModItems.ECHOING_AXE_RUBY.get()),
            new ItemStack(ModItems.ECHOING_AXE_AMBER.get()),
            new ItemStack(ModItems.ECHOING_AXE_AMETHYST.get()),
            new ItemStack(ModItems.ECHOING_AXE_JADE.get()),

            new ItemStack(ModItems.SKYBREAKER_AXE_RUBY.get()),
            new ItemStack(ModItems.SKYBREAKER_AXE_AMBER.get()),
            new ItemStack(ModItems.SKYBREAKER_AXE_AMETHYST.get()),
            new ItemStack(ModItems.SKYBREAKER_AXE_JADE.get()),
    };
}
