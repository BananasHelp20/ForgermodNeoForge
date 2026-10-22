package net.bananashelp20.forgermod.augmentation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** A single description path for weapons, templates, cards and their expanded hover text. */
public final class AbilityDescriptions {
    private static final Map<String, String> ENGLISH = loadEnglish();
    private AbilityDescriptions() {}

    private static Map<String, String> loadEnglish() {
        try (var input = AbilityDescriptions.class.getResourceAsStream("/assets/forgermod/lang/en_us.json")) {
            if (input == null) throw new IllegalStateException("Missing mod language resource");
            return new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>() {}.getType());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot load ability descriptions", exception);
        }
    }

    public static Component white(Object value) {
        return (value instanceof Component component ? component.copy() : Component.literal(String.valueOf(value)))
                .withStyle(ChatFormatting.WHITE);
    }

    public static Component text(String key, String fallback, Object... values) {
        Object[] styled = java.util.Arrays.stream(values).map(AbilityDescriptions::white).toArray();
        return Component.translatableWithFallback(key, fallback, styled).withStyle(ChatFormatting.GRAY);
    }

    public static Component description(ItemStack stack, String id, int rank, boolean compact) {
        Component reviewed = reviewed(stack,id,Math.max(1,rank));
        if (reviewed != null) return reviewed;
        String key = compact && ENGLISH.containsKey(id + ".card") ? id + ".card" : id;
        String original = ENGLISH.get(key);
        if (original == null) return Component.translatable(key).withStyle(ChatFormatting.GRAY);
        var resolved = RankedDescription.resolve(original, Math.max(1, rank));
        return text(key + ".current", resolved.format(), resolved.values().toArray());
    }

    private static Component reviewed(ItemStack stack,String id,int rank) {
        int jade = JadeAugments.isJade(stack) ? 1 : 0;
        return switch (id) {
            case AdditionalAbilities.BLOOD_RUSH -> text(id+".current", "On activation pays %s to gain %s for %s.",
                    "1 heart", "speed " + net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(2+jade), (10+jade)+" seconds");
            default -> null;
        };
    }
}
