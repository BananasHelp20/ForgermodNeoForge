package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DaggerReviewGameTests {
    @GameTest(template = "empty")
    public static void allDaggerMaterialPairings(GameTestHelper test) {
        List<Item> daggers = ModItems.ITEMS.getEntries().stream().map(entry -> (Item)entry.get())
                .filter(item -> DaggerItems.isDagger(new ItemStack(item))).toList();
        test.assertTrue(daggers.size() == 46, "Expected nine five-variant dagger families and the Rusty Dagger");
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        for (Item main : daggers) {
            for (Item off : daggers) {
                // Material classes are independent of the shared Tier used by the implementation.
                boolean expected = main.getClass() == off.getClass();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(main));
                player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(off));
                test.assertTrue(DaggerItems.hasMatchingDaggers(player) == expected,
                        "Incorrect material pairing: " + main + " / " + off);
            }
            for (Item other : new Item[]{ModItems.CARBON_STEEL_DAGGER.get(), ModItems.DAMASK_KNIFE.get(),
                    ModItems.CLAYMORE.get(), ModItems.NULLIFIED_AXE.get()}) {
                test.assertFalse(DaggerItems.areMatchingDaggers(new ItemStack(main), new ItemStack(other)),
                        "A knife or other weapon was accepted in the offhand");
                test.assertFalse(DaggerItems.areMatchingDaggers(new ItemStack(other), new ItemStack(main)),
                        "A knife or other weapon was accepted in the main hand");
            }
        }
        test.assertFalse(DaggerItems.areMatchingDaggers(ItemStack.EMPTY, ItemStack.EMPTY), "Empty hands dual wield");
        test.succeed();
    }

    private static String key(Component component) {
        return component.getContents() instanceof TranslatableContents text ? text.getKey() : "";
    }

    @GameTest(template = "empty")
    public static void abilityTooltipLayout(GameTestHelper test) throws ReflectiveOperationException {
        var append = SwordItemWithEffect.class.getDeclaredMethod("appendWeaponTooltip", ItemStack.class, List.class, String.class, String.class, boolean.class);
        append.setAccessible(true);
        int checked = 0;
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof SwordItemWithEffect weapon)) continue;
            checked++;
            ItemStack fresh = new ItemStack(weapon);
            List<Component> collapsed = new ArrayList<>();
            weapon.appendHoverText(fresh, Item.TooltipContext.of(test.getLevel()), collapsed, TooltipFlag.NORMAL);
            test.assertTrue(collapsed.stream().anyMatch(line -> key(line).equals("tooltips.forgermod.can_augment")), "Fresh item has no augmentation notice");
            test.assertFalse(collapsed.stream().anyMatch(line -> key(line).equals("tooltips.forgermod.ability.heading") || key(line).equals("tooltips.forgermod.passive.heading")), "Fresh item displays unlearned abilities");
            ItemStack learned = TestWeapons.ready(weapon);
            for (boolean expanded : new boolean[]{false, true}) {
                List<Component> tooltip = new ArrayList<>();
                append.invoke(weapon, learned, tooltip, key(collapsed.getFirst()), weapon.gemstoneName(), expanded);
                for (String heading : new String[]{"tooltips.forgermod.ability.heading", "tooltips.forgermod.passive.heading"}) {
                    var rows = tooltip.stream().filter(line -> key(line).equals(heading)).toList();
                    test.assertTrue(rows.size() == 1 && rows.getFirst().getStyle().getColor().getValue() == ChatFormatting.LIGHT_PURPLE.getColor(), "Heading is missing, repeated or not purple");
                }
                for (WeaponAbilitySlot slot : WeaponAbilitySlot.values()) {
                    String id = net.bananashelp20.forgermod.augmentation.Augmentations.activeId(learned, slot);
                    Component title = tooltip.stream().filter(line -> key(line).equals("tooltips.forgermod.ability.tooltip"))
                            .filter(line -> {
                                var rank = (TranslatableContents)((Component)((TranslatableContents)line.getContents()).getArgs()[0]).getContents();
                                return key((Component)rank.getArgs()[0]).equals(id + ".name");
                            }).findFirst().orElseThrow();
                    var args = ((TranslatableContents)title.getContents()).getArgs();
                    var binding = (KeybindContents)((Component)args[1]).getContents();
                    test.assertTrue(binding.getName().equals(slot == WeaponAbilitySlot.PRIMARY ? "key.forgermod.ability_primary" : "key.forgermod.ability_secondary"), "Configured key replaced by literal text");
                    test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals(id)) == expanded, "Active description does not follow Shift");
                }
                for (String id : net.bananashelp20.forgermod.augmentation.Augmentations.ids(learned, false))
                    test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals(id)) == expanded, "Passive description does not follow Shift");
                test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals("tooltips.forgermod.passive.material_effect")) == expanded, "Material description does not follow Shift");
                test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals("tooltips.forgermod.ability.shift_hint")) != expanded, "Shift hint state incorrect");
                for (Component line : tooltip) {
                    String id = key(line);
                    if ((id.startsWith("tooltips.forgermod.ability.") || id.startsWith("tooltips.forgermod.passive.")) && !id.endsWith("heading"))
                        test.assertTrue(line.getStyle().getColor().getValue() == ChatFormatting.GRAY.getColor(), "Ability text is not light gray");
                }
            }
        }
        test.assertTrue(checked == 135, "Skipped tooltip variants");
        test.succeed();
    }
}
