package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AbilityDescriptions;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Optional;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class CurrentDescriptionGameTests {
    @GameTest(template="riftfang_test")
    public static void unreviewedMagmaTossResolvesOnlyCurrentRankAndKeepsWhiteValues(GameTestHelper test) {
        var stack = new ItemStack(ModItems.MOLTEN_AXE.get());
        var description = AbilityDescriptions.description(stack,AdditionalAbilities.MAGMA_TOSS,3,false);
        String text = description.getString();
        test.assertTrue(text.contains("3 blocks") && text.contains("5 seconds") && text.contains("25 seconds"), "Wrong current-rank values: " + text);
        test.assertFalse(text.contains("/") || text.contains("%s"), "Unresolved rank list or argument");
        var white = new StringBuilder();
        description.visit((style,part) -> {
            if (style.getColor()!=null && style.getColor().getValue()==ChatFormatting.WHITE.getColor()) white.append(part).append('|');
            return Optional.<Void>empty();
        },Style.EMPTY);
        test.assertTrue(white.toString().contains("3 blocks") && white.toString().contains("5 seconds"), "Rank values lost their white style: " + white);
        test.assertTrue(description.getStyle().getColor().getValue()==ChatFormatting.GRAY.getColor(), "Prose must remain gray");
        test.succeed();
    }
}
