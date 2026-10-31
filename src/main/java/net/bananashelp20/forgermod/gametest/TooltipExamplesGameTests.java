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
    @GameTest(template = "empty")
    public static void allSpecialWeaponNamesAreLegendaryGold(GameTestHelper test) {
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof SwordItemWithEffect weapon) || !weapon.hasMaterialEffect())continue;
            checked++;
            var stack=new ItemStack(weapon);
            var plain=stack.copy();
            for(boolean renamed:new boolean[]{false,true}) {
                if(renamed)stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("My weapon"));
                for(boolean enchanted:new boolean[]{false,true}) {
                    if(enchanted)stack.enchant(test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),1);
                    var before=stack.copy();
                    test.assertTrue(stack.getHoverName().getStyle().getColor().getValue()==ChatFormatting.GOLD.getColor(),"Special weapon resolved name is not gold");
                    var title=stack.getTooltipLines(Item.TooltipContext.of(test.getLevel()),null,TooltipFlag.NORMAL).getFirst();
                    title.visit((style,text)->{
                        if(!text.isEmpty())test.assertTrue(style.getColor().getValue()==ChatFormatting.GOLD.getColor() && !style.isObfuscated(),"Rarity overrides the name color or obfuscates it");
                        return java.util.Optional.empty();
                    },net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(ItemStack.matches(before,stack),"Displaying the name modifies the stack");
                    if(renamed)test.assertTrue(stack.getHoverName().getString().equals("My weapon"),"Rename was lost");
                }
                stack=plain.copy();
            }
        }
        test.assertTrue(checked==135,"Missing special materials, weapon types or gemstone variants");
        for(var item:java.util.List.of(ModItems.CLAYMORE.get(),ModItems.RUSTY_CLAYMORE.get(),net.minecraft.world.item.Items.DIAMOND_SWORD))
            test.assertTrue(new ItemStack(item).getHoverName().getStyle().getColor()==null,"Ordinary item received special name formatting");
        test.succeed();
    }
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
        test.assertTrue(freshJade.size() == 7 && key(freshJade.get(0)).equals("tooltips.forgermod.deathwisper_dagger.tooltip")
                && freshJade.get(1).getString().isEmpty() && key(freshJade.get(2)).equals("tooltips.forgermod.gemstone")
                && key(freshJade.get(3)).equals("tooltips.forgermod.gemstone_bonus.jade")
                && key(freshJade.get(4)).equals("tooltips.forgermod.gemstone_bonus.jade.duration")
                && freshJade.get(5).getString().isEmpty() && key(freshJade.get(6)).equals("tooltips.forgermod.can_augment"),
                "Fresh Jade example does not follow lore, blank, gemstone, bonuses, blank, augmentation notice");
        Component jadeName = (Component)((TranslatableContents)freshJade.get(2).getContents()).getArgs()[0];
        test.assertTrue(key(jadeName).equals("gemstone.forgermod.jade")
                && jadeName.getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor()
                && freshJade.get(3).getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor()
                && freshJade.get(4).getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor(),
                "Fresh Jade name/bonuses are not green");
        test.assertFalse(contains(freshJade, "tooltips.forgermod.can_infuse") || contains(freshJade, "tooltips.forgermod.fire_resistant")
                || contains(freshJade, "tooltips.forgermod.passive.heading") || contains(freshJade, "tooltips.forgermod.ability.heading")
                || contains(freshJade, "tooltips.forgermod.ability.shift_hint"), "Fresh Jade example has extra item text");
        ItemStack partial = Augmentations.apply(fresh, "tooltips.forgermod.ability.storing_anger");
        var rows = tooltip(test, partial);
        test.assertTrue(contains(rows, "tooltips.forgermod.ability.heading") && contains(rows, "tooltips.forgermod.can_infuse"), "Partial example missing sections");
        test.assertFalse(contains(rows, "tooltips.forgermod.passive.heading"), "First active augmentation silently displays Dual Wield");
        test.assertFalse(contains(rows, "tooltips.forgermod.can_augment") || contains(rows, "tooltips.forgermod.ability.storing_anger"), "Partial example contains extra notice/expanded description");
        for (var item : new Item[]{ModItems.DEATHWISPER_DAGGER_AMBER.get(), ModItems.DEATHWISPER_DAGGER_JADE.get()}) {
            ItemStack maxed = TestWeapons.ready(item);
            var initial = Augmentations.learned(maxed);
            for (String id : initial.keySet()) for (int rank = 1; rank < 4; rank++) maxed = Augmentations.apply(maxed, id);
            while(!Augmentations.available(maxed).isEmpty()) maxed=Augmentations.apply(maxed,Augmentations.available(maxed).getFirst().id());
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
            for (Component line : rows) if (key(line).equals("augmentation.forgermod.rank")) {
                var args=((TranslatableContents)line.getContents()).getArgs();
                boolean fiveRanks=java.util.Set.of("tooltips.forgermod.passive.death_march.name", "tooltips.forgermod.passive.revenge.name").contains(key((Component)args[0]));
                test.assertTrue(args[1].equals(fiveRanks?"V":"IV"), "Maxed passive has wrong displayed rank");
            }
            var hint = rows.getLast();
            test.assertTrue(key(hint).equals("tooltips.forgermod.ability.shift_hint"), "Shift hint is not last");
            Component shift = (Component)((TranslatableContents)hint.getContents()).getArgs()[0];
            test.assertTrue(shift.getStyle().getColor().getValue() == ChatFormatting.BLUE.getColor(), "Hold Shift is not blue");
        }
        test.succeed();
    }
}
