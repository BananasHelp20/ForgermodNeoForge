package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.AugmentTemplateItem;
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

/** Learned abilities; the material hit has its own rank, beside three active slots and weapon-specific passive limits. */
public final class Augmentations {
    public record Ability(String id, boolean active, Component name) {
        public Ability(String id, boolean active) { this(id, active, Component.translatable(id + ".name")); }
    }
    public static final String VERTICAL_STRIKE = "tooltips.forgermod.passive.axe";
    public static final String DUAL_WIELD = "tooltips.forgermod.passive.dagger";
    public static final String DOUBLE_JUMP = "tooltips.forgermod.passive.double_jump";
    public static final String MATERIAL_HIT = "augmentation.forgermod.material_hit";
    private static final String LEGACY_MATERIAL_HIT = "augmentation.forgermod.empowered_hit";
    private static final String DATA = "forgermod_augments";
    private static List<Ability> cachedTemplatePool;
    private Augmentations() {}

    public static boolean eligible(ItemStack stack) {
        return stack.getItem() instanceof SwordItemWithEffect weapon && weapon.hasMaterialEffect();
    }
    public static boolean augmentable(ItemStack stack) {
        return eligible(stack) || isTemplate(stack);
    }
    public static boolean isTemplate(ItemStack stack) {
        return stack.getItem() instanceof AugmentTemplateItem;
    }
    public static boolean isSuperTemplate(ItemStack stack) {
        return stack.getItem() instanceof AugmentTemplateItem template && template.isSuperTemplate();
    }
    public static int maxRank(ItemStack stack, String id) {
        return hasSuperForm(stack, id) ? 7 : RecommendedAbilities.maxRank(id) + (isSuperTemplate(stack) ? 1 : 0);
    }
    /** Stored ranks 4..7 are super ranks I..IV; the ID and occupied slot stay stable. */
    public static String superName(ItemStack stack, String id) {
        if (AdditionalAbilities.BONE_GUARD.equals(id)) return "tooltips.forgermod.ability.necromancer.name";
        if (AdditionalAbilities.GHOSTWALK.equals(id)) return "tooltips.forgermod.ability.jumpscare_horror.name";
        if (AdditionalAbilities.DIRECT_CURRENT.equals(id)) return "tooltips.forgermod.ability.static_field.name";
        if (AdditionalAbilities.WINDCHARGE.equals(id) && (isTemplate(stack) || canonical(stack)!=null && canonical(stack).isAxe())) return "tooltips.forgermod.ability.skybreaker.name";
        if (DOUBLE_JUMP.equals(id)) return "tooltips.forgermod.passive.windwalker.name";
        return null;
    }
    public static boolean hasSuperForm(ItemStack stack, String id) { return superName(stack,id) != null; }
    public static int displayRank(ItemStack stack, String id, int rank) {
        return hasSuperForm(stack,id) && rank >= 4 ? rank - 3 : rank;
    }
    private static int storedMax(ItemStack stack, String id) {
        return hasSuperForm(stack,id) ? 7 : RecommendedAbilities.maxRank(id) + 1;
    }
    public static SwordItemWithEffect canonical(ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return null;
        return weapon;
    }
    public static List<Ability> pool(ItemStack stack) {
        if (!eligible(stack)) return List.of();
        var canonical = canonical(stack);
        var abilities = new ArrayList<Ability>();
        for (var slot : WeaponAbilitySlot.nativeSlots()) {
            String key = canonical.abilityDescriptionKey(slot);
            if (key != null) abilities.add(new Ability(key, true));
        }
        for (String key : canonical.passiveDescriptionKeys()) abilities.add(new Ability(key, false));
        if (canonical.isDagger()) abilities.add(new Ability(DUAL_WIELD, false));
        if (canonical.isAxe()) abilities.add(new Ability(VERTICAL_STRIKE, false));
        abilities.addAll(RecommendedAbilities.pool(stack));
        abilities.add(new Ability(MATERIAL_HIT, false, canonical.materialAbilityName()));
        return List.copyOf(abilities);
    }
    /** The template can roll any registered weapon ability without appearing as a weapon itself. */
    public static List<Ability> catalog(ItemStack stack) {
        return isTemplate(stack) ? templatePool() : pool(stack);
    }
    private static List<Ability> templatePool() {
        if (cachedTemplatePool != null) return cachedTemplatePool;
        Map<String, Ability> all = new LinkedHashMap<>();
        for (var entry : ModItems.ITEMS.getEntries()) {
            ItemStack weapon = new ItemStack(entry.get());
            if (eligible(weapon)) for (Ability ability : pool(weapon)) all.putIfAbsent(ability.id(), ability);
        }
        cachedTemplatePool = List.copyOf(all.values());
        return cachedTemplatePool;
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
        for (var ability : catalog(stack)) valid.add(ability.id());
        for (Tag tag : saved) {
            CompoundTag row = (CompoundTag)tag;
            String id = migrateId(row.getString("id"));
            int level = row.getInt("level");
            if (valid.contains(id) && level >= 1 && level <= storedMax(stack,id)) result.put(id, level);
        }
        return result;
    }
    public static List<String> ids(ItemStack stack, boolean active) {
        if (!augmentable(stack)) return List.of();
        List<String> result = new ArrayList<>();
        Map<String, Boolean> types = new HashMap<>();
        for (var ability : catalog(stack)) types.put(ability.id(), ability.active());
        for (String id : learned(stack).keySet()) {
            if (!id.equals(MATERIAL_HIT) && Objects.equals(types.get(id), active) && !result.contains(id) && result.size() < limit(stack, active)) result.add(id);
        }
        if (active && data(stack).getBoolean("swapped") && result.size()>=2) Collections.swap(result,0,1);
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
        for (var slot : WeaponAbilitySlot.nativeSlots()) {
            String key = weapon.abilityDescriptionKey(slot);
            if (key != null) result.add(key);
        }
        result.addAll(weapon.passiveDescriptionKeys());
        return result;
    }
    public static int limit(ItemStack stack,boolean active) {return active?3:canonical(stack)!=null && canonical(stack).isDagger()?2:5;}
    public static boolean hasPassive(ItemStack stack, String id) { return ids(stack, false).contains(id); }
    public static boolean hasActive(ItemStack stack) { return !ids(stack, true).isEmpty(); }
    public static Component abilityName(ItemStack stack,String id,int rank) {
        if (MATERIAL_HIT.equals(id) && isTemplate(stack))
            return Component.translatable("augmentation.forgermod.material_hit.name");
        if(rank>=4 && hasSuperForm(stack,id)) return Component.translatable(superName(stack,id));
        return catalog(stack).stream().filter(a->a.id().equals(id)).map(Ability::name).findFirst().orElse(Component.translatable(id+".name"));
    }
    public static int count(ItemStack stack) { return Math.max(0, data(stack).getInt("count")); }
    public static int duration(ItemStack stack) { return (int)Math.min(Integer.MAX_VALUE, 100L + count(stack) * 20L); }
    public static List<Ability> available(ItemStack stack) {
        var result = new ArrayList<Ability>();
        var learned = learned(stack);
        int activeCount = ids(stack,true).size(), passiveCount = ids(stack,false).size();
        for (var ability : catalog(stack)) {
            int level = learned.getOrDefault(ability.id(),0);
            int count = ability.active()?activeCount:passiveCount;
            if (level > 0 && level < maxRank(stack, ability.id())
                    || level == 0 && (ability.id().equals(MATERIAL_HIT) || count < limit(stack,ability.active()))) result.add(ability);
        }
        return List.copyOf(result);
    }
    public static List<Ability> offers(ItemStack stack, RandomSource random) {
        var available = new ArrayList<>(available(stack));
        if (available.isEmpty()) return List.of();
        // A fresh dagger/axe always offers its weapon technique alongside a different choice.
        SwordItemWithEffect weapon = canonical(stack);
        String technique = weapon == null ? null : weapon.isDagger() ? DUAL_WIELD
                : weapon.isAxe() ? VERTICAL_STRIKE : null;
        Ability guaranteed = count(stack) == 0 && technique != null
                ? available.stream().filter(a -> a.id().equals(technique)).findFirst().orElse(null) : null;
        var first = guaranteed != null ? guaranteed : available.get(random.nextInt(available.size()));
        available.remove(first);
        // With only one legal upgrade remaining both buttons offer it; neither can exceed its rank cap.
        var second = available.isEmpty() ? first : available.get(random.nextInt(available.size()));
        return List.of(first, second);
    }
    public static ItemStack apply(ItemStack stack, String id) {
        if (available(stack).stream().noneMatch(a -> a.id().equals(id))) return ItemStack.EMPTY;
        ItemStack result = stack.copyWithCount(1);
        var learned = new LinkedHashMap<>(learned(stack));
        learned.put(id, level(stack, id) + 1);
        write(result, learned, count(stack) + 1);
        return result;
    }
    public record Extraction(ItemStack weapon, ItemStack template) {}

    /** Move the first learned augment to a blank template; no implicit weapon technique is removed. */
    public static Extraction extractFirst(ItemStack weapon, ItemStack blankTemplate) {
        if (!eligible(weapon) || !isTemplate(blankTemplate) || !learned(blankTemplate).isEmpty()) return null;
        var remaining = new LinkedHashMap<>(learned(weapon));
        if (remaining.isEmpty()) return null;
        var first = remaining.entrySet().stream()
                .filter(entry -> entry.getValue() <= maxRank(blankTemplate, entry.getKey())
                        && catalog(blankTemplate).stream().anyMatch(a -> a.id().equals(entry.getKey())))
                .findFirst().orElse(null);
        if (first == null) return null;
        ItemStack resultWeapon = weapon.copyWithCount(1);
        ItemStack resultTemplate = blankTemplate.copyWithCount(1);
        write(resultWeapon, remainingWithout(remaining, first.getKey()), count(weapon));
        write(resultTemplate, Map.of(first.getKey(), first.getValue()), 1);
        return new Extraction(resultWeapon, resultTemplate);
    }

    private static Map<String, Integer> remainingWithout(Map<String, Integer> levels, String id) {
        var result = new LinkedHashMap<>(levels);
        result.remove(id);
        return result;
    }
    private static void write(ItemStack result, Map<String, Integer> learned, int count) {
        CompoundTag all = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag augments = data(result).copy();
        ListTag levels = new ListTag();
        learned.forEach((key, level) -> {
            CompoundTag row = new CompoundTag(); row.putString("id", key); row.putInt("level", level); levels.add(row);
        });
        augments.put("levels", levels); augments.putInt("count", count); augments.putInt("version", 2);
        all.put(DATA, augments); result.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
    }
    public record Replacement(String incoming, List<String> choices) {}

    public static Replacement pendingReplacement(ItemStack left, ItemStack right, Map<String, String> replacements) {
        return replacementPrompt(left, right, replacements, false);
    }

    /** Shows the next unresolved conflict, or the last choice so arrows can revise it. */
    public static Replacement replacementPrompt(ItemStack left, ItemStack right,
                                                 Map<String, String> replacements) {
        return replacementPrompt(left, right, replacements, true);
    }

    private static Replacement replacementPrompt(ItemStack left, ItemStack right,
                                                  Map<String, String> replacements, boolean includeResolved) {
        if (!(eligible(left) || isTemplate(left) && left.is(right.getItem())) || !augmentable(right)) return null;
        Map<String, Ability> compatible = new LinkedHashMap<>();
        for (Ability ability : catalog(left)) compatible.put(ability.id(), ability);
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>(learned(left));
        Replacement last = null;
        for (var entry : learned(right).entrySet()) {
            String id = entry.getKey();
            Ability ability = compatible.get(id);
            if (ability == null || merged.containsKey(id) || id.equals(MATERIAL_HIT)) continue;
            List<String> occupied = merged.keySet().stream().filter(key -> !key.equals(MATERIAL_HIT)
                    && compatible.containsKey(key) && compatible.get(key).active() == ability.active()).toList();
            if (occupied.size() >= limit(left, ability.active())) {
                last = new Replacement(id, occupied);
                String chosen = replacements.get(id);
                if (!occupied.contains(chosen)) return last;
                merged.remove(chosen);
            }
            merged.put(id, entry.getValue());
        }
        return includeResolved ? last : null;
    }

    /** Merges supported abilities. A full category requires an explicit replacement choice. */
    public static ItemStack merge(ItemStack left, ItemStack right) {
        return merge(left, right, Map.of());
    }
    public static ItemStack merge(ItemStack left, ItemStack right, Map<String, String> replacements) {
        if (!(eligible(left) || isTemplate(left) && left.is(right.getItem())) || !augmentable(right)) return ItemStack.EMPTY;
        if (pendingReplacement(left, right, replacements) != null) return ItemStack.EMPTY;
        Map<String, Ability> compatible = new LinkedHashMap<>();
        for (Ability ability : catalog(left)) compatible.put(ability.id(), ability);
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>(learned(left));
        boolean changed = false;
        for (var entry : learned(right).entrySet()) {
            String id = entry.getKey();
            Ability ability = compatible.get(id);
            if (ability == null) continue;
            int current = merged.getOrDefault(id, 0);
            int incoming = entry.getValue();
            int allowed = hasSuperForm(left,id) ? 7 : RecommendedAbilities.maxRank(id)
                    + (isSuperTemplate(right) || eligible(right) && incoming > RecommendedAbilities.maxRank(id) ? 1 : 0);
            int rank = current == incoming ? Math.min(allowed, current + 1) : Math.max(current, Math.min(incoming, allowed));
            if (rank <= current) continue;
            if (current == 0 && !id.equals(MATERIAL_HIT)) {
                long occupied = merged.keySet().stream().filter(key -> !key.equals(MATERIAL_HIT)
                        && compatible.containsKey(key) && compatible.get(key).active() == ability.active()).count();
                if (occupied >= limit(left, ability.active())) merged.remove(replacements.get(id));
            }
            merged.put(id, rank);
            changed = true;
        }
        if (!changed) return ItemStack.EMPTY;
        ItemStack result = left.copyWithCount(1);
        write(result, merged, Math.max(count(left), merged.size()));
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
        String id=activeId(stack,slot);
        if(RecommendedAbilities.active(id)) return RecommendedAbilities.cooldown(id,level(stack,id));
        return Math.max(0, weapon.abilityCooldownTicks(sourceSlot(stack, slot)));
    }
    public static String migrateId(String id) {
        return LEGACY_MATERIAL_HIT.equals(id) ? MATERIAL_HIT : id;
    }
}
