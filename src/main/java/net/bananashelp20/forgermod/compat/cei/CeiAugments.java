package net.bananashelp20.forgermod.compat.cei;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Reflection stays confined to this optional bridge; no CEI class is linked by Forgermod. */
public final class CeiAugments {
    private static final String FORGER = "plus.dragons.createenchantmentindustry.common.processing.forger.";
    private CeiAugments() {}

    public static boolean canEnchant(ItemStack stack, Object behaviour) {
        return Augmentations.isTemplate(stack) && Augmentations.isSuperTemplate(stack) == machineSpecial(enchanter(behaviour))
                && !Augmentations.available(stack).isEmpty();
    }

    public static ItemStack enchant(ItemStack stack, Object behaviour) {
        var offers = Augmentations.available(stack);
        try {
            Object owner = enchanter(behaviour);
            var random = (net.minecraft.util.RandomSource) owner.getClass().getMethod("getRandom").invoke(owner);
            return offers.isEmpty() ? stack : Augmentations.apply(stack, offers.get(random.nextInt(offers.size())).id());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("CEI Enchanter random source changed", exception);
        }
    }

    private static Object enchanter(Object behaviour) {
        try {
            Field owner = behaviour.getClass().getDeclaredField("enchanter");
            owner.setAccessible(true);
            return owner.get(behaviour);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("CEI Enchanter layout changed", exception);
        }
    }

    public static ItemStack heldItem(Object behaviour) {
        try {
            Object enchanter = enchanter(behaviour);
            Field held = enchanter.getClass().getDeclaredField("heldItem");
            held.setAccessible(true);
            return (ItemStack) held.get(enchanter);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("CEI Enchanter layout changed", exception);
        }
    }

    public static boolean machineSpecial(Object machine) {
        try {
            Field special = machine.getClass().getDeclaredField("special");
            special.setAccessible(true);
            return special.getBoolean(machine);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("CEI machine mode changed", exception);
        }
    }

    public static Object forgerResult(Object inventory, ItemStack base, ItemStack addition) {
        if (!Augmentations.isTemplate(base) && !Augmentations.isTemplate(addition)) return null;
        try {
            Field owner = inventory.getClass().getDeclaredField("forger");
            owner.setAccessible(true);
            Object forger = owner.get(inventory);
            Object mode = forger.getClass().getMethod("getMode").invoke(forger);
            String name = ((Enum<?>) mode).name();
            ItemStack primary = ItemStack.EMPTY, secondary = ItemStack.EMPTY;
            boolean matching = (!Augmentations.isTemplate(base)
                    || Augmentations.isSuperTemplate(base) == machineSpecial(forger))
                    && (!Augmentations.isTemplate(addition)
                    || Augmentations.isSuperTemplate(addition) == machineSpecial(forger));
            if (matching && "APPLY".equals(name) && Augmentations.eligible(base) && Augmentations.isTemplate(addition)) {
                primary = Augmentations.merge(base, addition);
            } else if (matching && "EXTRACT".equals(name) && Augmentations.eligible(base) && Augmentations.isTemplate(addition)) {
                var extracted = Augmentations.extractFirst(base, addition);
                if (extracted != null) {
                    primary = extracted.weapon();
                    secondary = extracted.template();
                }
            } else if (matching && "MERGE".equals(name) && Augmentations.isTemplate(base) && base.is(addition.getItem())) {
                primary = Augmentations.merge(base, addition);
            }
            Class<?> resultClass = Class.forName(FORGER + "BlazeForgerInventory$Result");
            Class<?> modeClass = mode.getClass();
            if (primary.isEmpty() && secondary.isEmpty()) {
                Method invalid = resultClass.getMethod("invalid", modeClass, net.minecraft.network.chat.Component.class);
                return invalid.invoke(null, mode, net.minecraft.network.chat.Component.literal("No compatible augment transfer"));
            }
            Method ready = resultClass.getMethod("ready", modeClass, ItemStack.class, ItemStack.class,
                    int.class, boolean.class, boolean.class, boolean.class, int.class, int.class);
            return ready.invoke(null, mode, primary, secondary, 5, false, false, false, 0, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("CEI Forger layout changed", exception);
        }
    }
}
