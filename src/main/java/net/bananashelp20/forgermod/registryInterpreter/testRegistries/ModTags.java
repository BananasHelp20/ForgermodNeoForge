package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));
        }

        //!GENERATE MOD_TAGS

        public static final TagKey<Block> NEEDS_STEEL_TOOL = createTag("needs_steel_tool");
        public static final TagKey<Block> INCORRECT_FOR_STEEL_TOOL = createTag("incorrect_for_steel_tool");

        public static final TagKey<Block> NEEDS_MAT_TOOL = createTag("needs_mat_tool");
        public static final TagKey<Block> INCORRECT_FOR_MAT_TOOL = createTag("incorrect_for_mat_tool");

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));
        }
        //!GENERATE MOD_ITEM_TAGS
    }    }
}