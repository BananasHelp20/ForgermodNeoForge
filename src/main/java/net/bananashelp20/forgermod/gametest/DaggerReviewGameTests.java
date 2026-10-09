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
        var append = SwordItemWithEffect.class.getDeclaredMethod("appendWeaponTooltip", List.class, String.class, String.class, boolean.class);
        append.setAccessible(true);
        int abilities = 0;
        int passives = 0;
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof SwordItemWithEffect weapon)) continue;
            List<Component> collapsed = new ArrayList<>();
            weapon.appendHoverText(new ItemStack(weapon), Item.TooltipContext.of(test.getLevel()), collapsed, TooltipFlag.NORMAL);
            String gemKey = key(collapsed.getLast());
            String gem = gemKey.substring("tooltips.forgermod.".length(), gemKey.length() - ".tooltip_extra".length());
            for (boolean expanded : new boolean[]{false, true}) {
                List<Component> tooltip = expanded ? new ArrayList<>() : collapsed;
                if (expanded) append.invoke(weapon, tooltip, key(collapsed.getFirst()), gem, true);
                int headings = 0;
                for (Component line : tooltip) {
                    String lineKey = key(line);
                    boolean abilityText = lineKey.startsWith("tooltips.forgermod.ability.") || lineKey.startsWith("tooltips.forgermod.passive.");
                    if (!abilityText) continue;
                    test.assertTrue(line.getStyle().getColor() != null && ChatFormatting.GRAY.getColor().equals(line.getStyle().getColor().getValue()), "Ability/passive text is not light gray: " + lineKey);
                    if (lineKey.equals("tooltips.forgermod.ability.heading")) headings++;
                    if (lineKey.startsWith("tooltips.forgermod.passive.") && lineKey.endsWith(".name")) passives++;
                    if (!expanded && lineKey.startsWith("tooltips.forgermod.passive.")) {
                        test.assertTrue(lineKey.endsWith(".name") || lineKey.endsWith(".heading"), "Passive description visible without Shift");
                    }
                }
                int slots = 0;
                for (WeaponAbilitySlot slot : WeaponAbilitySlot.values()) {
                    String descriptionKey = weapon.abilityDescriptionKey(slot);
                    if (descriptionKey == null) continue;
                    slots++;
                    Component title = tooltip.stream().filter(line -> key(line).equals("tooltips.forgermod.ability.tooltip"))
                            .filter(line -> ((Component)((TranslatableContents)line.getContents()).getArgs()[0]).getContents() instanceof TranslatableContents name
                                    && name.getKey().equals(descriptionKey + ".name")).findFirst().orElseThrow();
                    var args = ((TranslatableContents)title.getContents()).getArgs();
                    var binding = (KeybindContents)((Component)args[1]).getContents();
                    test.assertTrue(binding.getName().equals(slot == WeaponAbilitySlot.PRIMARY ? "key.forgermod.ability_primary" : "key.forgermod.ability_secondary"), "Configured key replaced by literal text");
                    test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals(descriptionKey)) == expanded, "Active description does not follow Shift state");
                    abilities++;
                }
                test.assertTrue(headings == (slots == 0 ? 0 : 1), "Missing or duplicate ability heading");
                for (String passive : weapon.passiveDescriptionKeys()) {
                    test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals(passive + ".name")), "Missing passive name");
                    test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals(passive)) == expanded, "Material passive does not follow Shift state");
                }
                test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals("tooltips.forgermod.passive.material_effect")) == expanded, "On-hit description does not follow Shift state");
                test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals("tooltips.forgermod.ability.shift_hint")) != expanded, "Shift hint state is incorrect");
            }
        }
        for (Item item : new Item[]{ModItems.CARBON_STEEL_AXE.get(), ModItems.RUSTY_AXE.get(), ModItems.RUSTY_DAGGER.get()}) {
            List<Component> tooltip = new ArrayList<>();
            item.appendHoverText(new ItemStack(item), Item.TooltipContext.of(test.getLevel()), tooltip, TooltipFlag.NORMAL);
            String description = item == ModItems.RUSTY_DAGGER.get() ? "tooltips.forgermod.passive.dagger" : "tooltips.forgermod.passive.axe";
            test.assertTrue(tooltip.stream().anyMatch(line -> key(line).equals(description + ".name")), "Ordinary weapon has no passive name");
            test.assertFalse(tooltip.stream().anyMatch(line -> key(line).equals(description)), "Ordinary weapon description visible without Shift");
            List<Component> expanded = new ArrayList<>();
            net.bananashelp20.forgermod.item.custom.WeaponTooltips.ordinaryPassives(new ItemStack(item), expanded, true);
            test.assertTrue(expanded.stream().anyMatch(line -> key(line).equals(description)), "Ordinary passive description missing with Shift");
        }
        test.assertTrue(abilities == 180 && passives >= 450, "Tooltip checks skipped variants");
        test.succeed();
    }
}
