package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.util.RandomSource;
import java.util.*;

/** Item-local learned abilities. Core material effects, dual wield and axe slam remain intrinsic. */
public final class Augmentations {
    public record Ability(String id, boolean active) {
        public Component name() { return Component.translatable(id + ".name"); }
    }
    public static final String EMPOWERED_HIT = "augmentation.forgermod.empowered_hit";
    public static final String GUARDED = "augmentation.forgermod.guarded";
    private static final Map<net.minecraft.world.item.Item, SwordItemWithEffect> CANONICAL = new java.util.concurrent.ConcurrentHashMap<>();
    private static final String DATA = "forgermod_augments";
    private Augmentations() {}

    public static boolean eligible(ItemStack stack) {
        return stack.getItem() instanceof SwordItemWithEffect weapon && weapon.hasMaterialEffect();
    }
    public static SwordItemWithEffect canonical(ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return null;
        return CANONICAL.computeIfAbsent(stack.getItem(), ignored -> {
            for (var entry : ModItems.ITEMS.getEntries()) {
                if (entry.get() instanceof SwordItemWithEffect candidate && candidate.getClass() == weapon.getClass()
                        && candidate.isDagger() && candidate.gemstoneName().equals(weapon.gemstoneName())) return candidate;
            }
            return weapon;
        });
    }
    public static List<Ability> pool(ItemStack stack) {
        if (!eligible(stack)) return List.of();
        var canonical = canonical(stack);
        var abilities = new ArrayList<Ability>();
        for (var slot : WeaponAbilitySlot.values()) {
            String key = canonical.abilityDescriptionKey(slot);
            if (key != null) abilities.add(new Ability(key, true));
        }
        for (String key : canonical.passiveDescriptionKeys()) abilities.add(new Ability(key, false));
        abilities.add(new Ability(EMPOWERED_HIT, false));
        abilities.add(new Ability(GUARDED, false));
        return List.copyOf(abilities);
    }
    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(DATA);
    }
    public static Map<String, Integer> learned(ItemStack stack) {
        Map<String, Integer> result = new LinkedHashMap<>();
        ListTag saved = data(stack).getList("levels", Tag.TAG_COMPOUND);
        if (saved.isEmpty()) return result;
        Set<String> valid = new HashSet<>();
        for (var ability : pool(stack)) valid.add(ability.id());
        for (Tag tag : saved) {
            CompoundTag row = (CompoundTag)tag;
            String id = row.getString("id");
            int level = row.getInt("level");
            if (valid.contains(id) && level >= 1 && level <= 4) result.put(id, level);
        }
        return result;
    }
    public static List<String> ids(ItemStack stack, boolean active) {
        if (!eligible(stack)) return List.of();
        var weapon = (SwordItemWithEffect)stack.getItem();
        List<String> result = new ArrayList<>();
        if (active) {
            for (var slot : WeaponAbilitySlot.values()) {
                String key = weapon.abilityDescriptionKey(slot);
                if (key != null) result.add(key);
            }
        } else result.addAll(weapon.passiveDescriptionKeys());
        Map<String, Boolean> types = new HashMap<>();
        for (var ability : pool(stack)) types.put(ability.id(), ability.active());
        for (String id : learned(stack).keySet()) {
            if (Objects.equals(types.get(id), active) && !result.contains(id) && result.size() < 2) result.add(id);
        }
        return List.copyOf(result);
    }
    public static int level(ItemStack stack, String id) {
        Integer value = learned(stack).get(id);
        if (value != null) return value;
        return idsWithoutLearned(stack).contains(id) ? 1 : 0;
    }
    private static List<String> idsWithoutLearned(ItemStack stack) {
        List<String> result = new ArrayList<>();
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return result;
        for (var slot : WeaponAbilitySlot.values()) {
            String key = weapon.abilityDescriptionKey(slot);
            if (key != null) result.add(key);
        }
        result.addAll(weapon.passiveDescriptionKeys());
        return result;
    }
    public static boolean hasPassive(ItemStack stack, String id) { return ids(stack, false).contains(id); }
    public static boolean hasActive(ItemStack stack) { return !ids(stack, true).isEmpty(); }
    public static int count(ItemStack stack) { return Math.max(0, data(stack).getInt("count")); }
    public static int duration(ItemStack stack) { return (int)Math.min(Integer.MAX_VALUE, 100L + count(stack) * 20L); }
    public static List<Ability> available(ItemStack stack) {
        var result = new ArrayList<Ability>();
        for (var ability : pool(stack)) {
            int level = level(stack, ability.id());
            if (level > 0 && level < 4 || level == 0 && ids(stack, ability.active()).size() < 2) result.add(ability);
        }
        return List.copyOf(result);
    }
    public static List<Ability> offers(ItemStack stack, RandomSource random) {
        var available = new ArrayList<>(available(stack));
        if (available.isEmpty()) return List.of();
        var first = available.remove(random.nextInt(available.size()));
        // With only one legal upgrade remaining both buttons offer that upgrade; neither can exceed IV.
        var second = available.isEmpty() ? first : available.get(random.nextInt(available.size()));
        return List.of(first, second);
    }
    public static ItemStack apply(ItemStack stack, String id) {
        if (available(stack).stream().noneMatch(a -> a.id().equals(id))) return ItemStack.EMPTY;
        ItemStack result = stack.copyWithCount(1);
        var learned = new LinkedHashMap<>(learned(stack));
        learned.put(id, level(stack, id) + 1);
        CompoundTag all = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag augments = new CompoundTag();
        ListTag levels = new ListTag();
        learned.forEach((key, level) -> {
            CompoundTag row = new CompoundTag(); row.putString("id", key); row.putInt("level", level); levels.add(row);
        });
        augments.put("levels", levels); augments.putInt("count", count(stack) + 1);
        all.put(DATA, augments); result.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
        return result;
    }
    public static String activeId(ItemStack stack, WeaponAbilitySlot slot) {
        var ids = ids(stack, true);
        return slot.ordinal() < ids.size() ? ids.get(slot.ordinal()) : null;
    }
    public static WeaponAbilitySlot sourceSlot(ItemStack stack, WeaponAbilitySlot slot) {
        String id = activeId(stack, slot);
        var weapon = canonical(stack);
        if (weapon != null && Objects.equals(id, weapon.abilityDescriptionKey(WeaponAbilitySlot.SECONDARY)))
            return WeaponAbilitySlot.SECONDARY;
        return WeaponAbilitySlot.PRIMARY;
    }
    public static int cooldown(ItemStack stack, WeaponAbilitySlot slot, SwordItemWithEffect weapon) {
        String id = activeId(stack, slot);
        int level = id == null ? 1 : Math.max(1, level(stack, id));
        return Math.max(0, Math.round(weapon.abilityCooldownTicks(sourceSlot(stack, slot)) * (1 - .15F * (level - 1))));
    }
}
