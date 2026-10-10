package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TooltipExamplesGameTests {
    private static String key(Component component) {
        return component.getContents() instanceof TranslatableContents text ? text.getKey() : "";
    }
    private static List<Component> tooltip(GameTestHelper test, ItemStack stack) {
        var result = new ArrayList<Component>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.of(test.getLevel()), result, TooltipFlag.NORMAL);
        return result;
    }
    private static boolean contains(List<Component> rows, String key) { return rows.stream().anyMatch(c -> key(c).equals(key)); }

    @GameTest(template = "empty")
    public static void freshPartialAmberAndJadeFollowWrittenExamples(GameTestHelper test) {
        ItemStack fresh = new ItemStack(ModItems.DEATHWISPER_DAGGER.get());
        var plain = tooltip(test, fresh);
        test.assertTrue(contains(plain, "tooltips.forgermod.can_augment") && contains(plain, "tooltips.forgermod.can_infuse"), "Fresh capability notices missing");
        test.assertFalse(contains(plain, "tooltips.forgermod.passive.heading") || contains(plain, "tooltips.forgermod.ability.shift_hint")
                || contains(plain, "tooltips.forgermod.gemstone"), "Fresh example contains ability/gemstone clutter");
        var freshJade = tooltip(test, new ItemStack(ModItems.DEATHWISPER_DAGGER_JADE.get()));
        test.assertTrue(freshJade.size() == 6 && key(freshJade.get(0)).equals("tooltips.forgermod.deathwisper_dagger.tooltip")
                && freshJade.get(1).getString().isEmpty() && key(freshJade.get(2)).equals("tooltips.forgermod.gemstone")
                && key(freshJade.get(3)).equals("tooltips.forgermod.gemstone_bonus.jade")
                && freshJade.get(4).getString().isEmpty() && key(freshJade.get(5)).equals("tooltips.forgermod.can_augment"),
                "Fresh Jade example does not follow lore, blank, gemstone, bonuses, blank, augmentation notice");
        Component jadeName = (Component)((TranslatableContents)freshJade.get(2).getContents()).getArgs()[0];
        test.assertTrue(key(jadeName).equals("gemstone.forgermod.jade")
                && jadeName.getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor()
                && freshJade.get(3).getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor(),
                "Fresh Jade name/bonuses are not green");
        test.assertFalse(contains(freshJade, "tooltips.forgermod.can_infuse") || contains(freshJade, "tooltips.forgermod.fire_resistant")
                || contains(freshJade, "tooltips.forgermod.passive.heading") || contains(freshJade, "tooltips.forgermod.ability.heading")
                || contains(freshJade, "tooltips.forgermod.ability.shift_hint"), "Fresh Jade example has extra item text");
        ItemStack partial = Augmentations.apply(fresh, "tooltips.forgermod.ability.storing_anger");
        var rows = tooltip(test, partial);
        test.assertTrue(contains(rows, "tooltips.forgermod.passive.dagger.name") && contains(rows, "tooltips.forgermod.passive.heading")
                && contains(rows, "tooltips.forgermod.ability.heading") && contains(rows, "tooltips.forgermod.can_infuse"), "Partial example missing sections");
        test.assertFalse(contains(rows, "tooltips.forgermod.can_augment") || contains(rows, "tooltips.forgermod.ability.storing_anger"), "Partial example contains extra notice/expanded description");
        for (var item : new Item[]{ModItems.DEATHWISPER_DAGGER_AMBER.get(), ModItems.DEATHWISPER_DAGGER_JADE.get()}) {
            ItemStack maxed = TestWeapons.ready(item);
            var initial = Augmentations.learned(maxed);
            for (String id : initial.keySet()) for (int rank = 1; rank < 4; rank++) maxed = Augmentations.apply(maxed, id);
            test.assertTrue(Augmentations.available(maxed).isEmpty(), "Morsium example not actually maxed");
            rows = tooltip(test, maxed);
            test.assertFalse(contains(rows, "tooltips.forgermod.can_augment") || contains(rows, "tooltips.forgermod.can_infuse"), "Maxed gem item has capability clutter");
            var gemLine = rows.stream().filter(c -> key(c).equals("tooltips.forgermod.gemstone")).findFirst().orElseThrow();
            Component gem = (Component)((TranslatableContents)gemLine.getContents()).getArgs()[0];
            boolean jade = item == ModItems.DEATHWISPER_DAGGER_JADE.get();
            test.assertTrue(key(gem).equals(jade ? "gemstone.forgermod.jade" : "gemstone.forgermod.amber")
                    && gem.getStyle().getColor().getValue() == (jade ? ChatFormatting.GREEN : ChatFormatting.GOLD).getColor(), "Gem name/color wrong");
            test.assertTrue(contains(rows, "tooltips.forgermod.fire_resistant") != jade, "Jade inherited Amber fire resistance text");
            test.assertTrue(contains(rows, "tooltips.forgermod.gemstone_bonus.jade") == jade, "Jade bonus text missing or shown on Amber");
            test.assertTrue(rows.stream().filter(c -> key(c).equals("augmentation.forgermod.rank")).count() == 3, "Material hit and two passives not displayed together");
            for (Component line : rows) if (key(line).equals("augmentation.forgermod.rank"))
                test.assertTrue(((TranslatableContents)line.getContents()).getArgs()[1].equals("IV"), "Maxed passive lacks IV");
            var hint = rows.getLast();
            test.assertTrue(key(hint).equals("tooltips.forgermod.ability.shift_hint"), "Shift hint is not last");
            Component shift = (Component)((TranslatableContents)hint.getContents()).getArgs()[0];
            test.assertTrue(shift.getStyle().getColor().getValue() == ChatFormatting.BLUE.getColor(), "Hold Shift is not blue");
        }
        test.succeed();
    }
}
