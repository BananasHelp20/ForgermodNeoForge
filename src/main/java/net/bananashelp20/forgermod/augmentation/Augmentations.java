package net.bananashelp20.forgermod.augmentation;

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

/** Learned abilities; the material hit has its own rank, beside two active/two unique passive slots. */
public final class Augmentations {
    public record Ability(String id, boolean active, Component name) {
        public Ability(String id, boolean active) { this(id, active, Component.translatable(id + ".name")); }
    }
    public static final String MATERIAL_HIT = "augmentation.forgermod.material_hit";
    private static final String LEGACY_MATERIAL_HIT = "augmentation.forgermod.empowered_hit";
    private static final String DATA = "forgermod_augments";
    private Augmentations() {}

    public static boolean eligible(ItemStack stack) {
        return stack.getItem() instanceof SwordItemWithEffect weapon && weapon.hasMaterialEffect();
    }
    public static SwordItemWithEffect canonical(ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return null;
        return weapon;
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
        abilities.addAll(RecommendedAbilities.pool(stack));
        abilities.add(new Ability(MATERIAL_HIT, false, canonical.materialAbilityName()));
        return List.copyOf(abilities);
    }
    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(DATA);
    }
    public static Map<String, Integer> learned(ItemStack stack) {
        Map<String, Integer> result = new LinkedHashMap<>();
        ListTag saved = data(stack).getList("levels", Tag.TAG_COMPOUND);
        // Existing augmented stacks keep their former native abilities when migrating to learned-only gear.
        if (data(stack).getInt("version") < 2 && count(stack) > 0) {
            for (String id : nativeIds(stack)) result.put(id, 1);
            result.put(MATERIAL_HIT, 1);
        }
        if (saved.isEmpty()) return result;
        Set<String> valid = new HashSet<>();
        for (var ability : pool(stack)) valid.add(ability.id());
        for (Tag tag : saved) {
            CompoundTag row = (CompoundTag)tag;
            String id = migrateId(row.getString("id"));
            int level = row.getInt("level");
            if (valid.contains(id) && level >= 1 && level <= 4) result.put(id, level);
        }
        return result;
    }
    public static List<String> ids(ItemStack stack, boolean active) {
        if (!eligible(stack)) return List.of();
        List<String> result = new ArrayList<>();
        Map<String, Boolean> types = new HashMap<>();
        for (var ability : pool(stack)) types.put(ability.id(), ability.active());
        for (String id : learned(stack).keySet()) {
            if (!id.equals(MATERIAL_HIT) && Objects.equals(types.get(id), active) && !result.contains(id) && result.size() < 2) result.add(id);
        }
        if (active && data(stack).getBoolean("swapped")) Collections.reverse(result);
        return List.copyOf(result);
    }
    public static int level(ItemStack stack, String id) {
        Integer value = learned(stack).get(id);
        if (value != null) return value;
        return 0;
    }
    private static List<String> nativeIds(ItemStack stack) {
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
            if (level > 0 && level < 4 || level == 0 && (ability.id().equals(MATERIAL_HIT) || ids(stack, ability.active()).size() < 2)) result.add(ability);
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
        CompoundTag augments = data(stack).copy();
        ListTag levels = new ListTag();
        learned.forEach((key, level) -> {
            CompoundTag row = new CompoundTag(); row.putString("id", key); row.putInt("level", level); levels.add(row);
        });
        augments.put("levels", levels); augments.putInt("count", count(stack) + 1); augments.putInt("version", 2);
        all.put(DATA, augments); result.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
        return result;
    }
    public static String activeId(ItemStack stack, WeaponAbilitySlot slot) {
        var ids = ids(stack, true);
        return slot.ordinal() < ids.size() ? ids.get(slot.ordinal()) : null;
    }

    public static boolean swapActives(ItemStack stack) {
        if (ids(stack, true).size() != 2) return false;
        CompoundTag all = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag augments = data(stack).copy();
        // Persist legacy implicit abilities before changing format/version.
        ListTag levels = new ListTag();
        learned(stack).forEach((id, rank) -> {
            CompoundTag row = new CompoundTag(); row.putString("id", id); row.putInt("level", rank); levels.add(row);
        });
        augments.put("levels", levels); augments.putInt("version", 2);
        augments.putBoolean("swapped", !augments.getBoolean("swapped"));
        all.put(DATA, augments);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
        return true;
    }
    public static WeaponAbilitySlot sourceSlot(ItemStack stack, WeaponAbilitySlot slot) {
        String id = activeId(stack, slot);
        var weapon = canonical(stack);
        if (weapon != null && Objects.equals(id, weapon.abilityDescriptionKey(WeaponAbilitySlot.SECONDARY)))
            return WeaponAbilitySlot.SECONDARY;
        return WeaponAbilitySlot.PRIMARY;
    }
    public static int cooldown(ItemStack stack, WeaponAbilitySlot slot, SwordItemWithEffect weapon) {
        return Math.max(0, weapon.abilityCooldownTicks(sourceSlot(stack, slot)));
    }
    public static String migrateId(String id) {
        return LEGACY_MATERIAL_HIT.equals(id) ? MATERIAL_HIT : id;
    }
}
