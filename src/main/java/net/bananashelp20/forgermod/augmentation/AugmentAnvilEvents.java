package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class AugmentAnvilEvents {
    private record Selection(int menuId, ItemStack left, ItemStack right, Map<String, String> replacements) {}
    private static final Map<Player, Selection> SELECTIONS = Collections.synchronizedMap(new WeakHashMap<>());
    private AugmentAnvilEvents() {}

    public static Map<String, String> selections(Player player, ItemStack left, ItemStack right) {
        Selection selection = SELECTIONS.get(player);
        if (selection == null || selection.menuId() != player.containerMenu.containerId
                || !ItemStack.isSameItemSameComponents(selection.left(), left)
                || !ItemStack.isSameItemSameComponents(selection.right(), right)) {
            selection = new Selection(player.containerMenu.containerId, left.copy(), right.copy(), new HashMap<>());
            SELECTIONS.put(player, selection);
        }
        return selection.replacements();
    }

    public static boolean select(Player player, String incoming, String replaced) {
        if (!(player.containerMenu instanceof AnvilMenu menu)) return false;
        ItemStack left = menu.getSlot(0).getItem(), right = menu.getSlot(1).getItem();
        Map<String, String> choices = selections(player, left, right);
        Augmentations.Replacement pending = Augmentations.replacementPrompt(left, right, choices);
        if (pending == null || !pending.incoming().equals(incoming) || !pending.choices().contains(replaced)) return false;
        choices.put(incoming, replaced);
        menu.createResult();
        return true;
    }

    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        if (!Augmentations.eligible(event.getLeft()) || !Augmentations.augmentable(event.getRight())) return;
        Map<String, String> choices = selections(event.getPlayer(), event.getLeft(), event.getRight());
        if (Augmentations.pendingReplacement(event.getLeft(), event.getRight(), choices) != null) {
            // 21.1.93 returns early on cancellation without clearing a previous anvil result.
            if (event.getPlayer().containerMenu instanceof AnvilMenu menu) {
                menu.getSlot(2).set(ItemStack.EMPTY);
                menu.setMaximumCost(0);
            }
            event.setCanceled(true);
            return;
        }
        ItemStack result = Augmentations.merge(event.getLeft(), event.getRight(), choices);
        if (result.isEmpty()) return;
        ItemStack left = event.getLeft(), right = event.getRight();
        var previous = Augmentations.learned(left);
        int addedCost = 3 * (int)Augmentations.learned(result).entrySet().stream()
                .filter(entry -> entry.getValue() > previous.getOrDefault(entry.getKey(), 0)).count();
        if (left.is(right.getItem()) && left.isDamageableItem()) {
            int durability = left.getMaxDamage() - left.getDamageValue()
                    + right.getMaxDamage() - right.getDamageValue() + left.getMaxDamage() * 12 / 100;
            int newDamage = Math.max(0, left.getMaxDamage() - durability);
            if (newDamage < result.getDamageValue()) {
                result.setDamageValue(newDamage);
                addedCost += 2;
            }
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(left));
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(right).entrySet()) {
                var enchantment = entry.getKey();
                if (!left.supportsEnchantment(enchantment) && !event.getPlayer().getAbilities().instabuild) continue;
                boolean compatible = true;
                for (var existing : enchantments.keySet())
                    if (!existing.equals(enchantment) && !Enchantment.areCompatible(existing, enchantment)) compatible = false;
                if (!compatible) continue;
                int oldRank = enchantments.getLevel(enchantment), newRank = entry.getIntValue();
                int rank = Math.min(enchantment.value().getMaxLevel(), oldRank == newRank ? oldRank + 1 : Math.max(oldRank, newRank));
                if (rank > oldRank) {
                    enchantments.set(enchantment, rank);
                    addedCost += enchantment.value().getAnvilCost() * rank;
                }
            }
            EnchantmentHelper.setEnchantments(result, enchantments.toImmutable());
        }
        String name = event.getName();
        if (name != null) {
            if (name.isEmpty()) result.remove(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
            else result.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(name));
        }
        event.setOutput(result);
        event.setMaterialCost(1);
        int repairCost = Math.max(left.getOrDefault(DataComponents.REPAIR_COST, 0), right.getOrDefault(DataComponents.REPAIR_COST, 0));
        result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(repairCost));
        event.setCost(Math.max(1, event.getCost() + addedCost));
    }
}
