package net.bananashelp20.forgermod.item.custom.abilities;

import com.mojang.serialization.Codec;
import net.bananashelp20.forgermod.ForgerMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

/** Player-persisted ability deadlines, copied to the respawned player on death. */
public final class WeaponCooldownAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ForgerMod.MOD_ID);
    public static final Supplier<AttachmentType<Map<String, Long>>> COOLDOWNS = TYPES.register(
            "weapon_cooldowns", () -> AttachmentType.builder(() -> Map.<String, Long>of())
                    .serialize(Codec.unboundedMap(Codec.STRING, Codec.LONG)).copyOnDeath().build());

    public static final Supplier<AttachmentType<Boolean>> DISABLED = TYPES.register(
            "ability_cooldowns_disabled", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL).copyOnDeath().build());

    private WeaponCooldownAttachments() {}
}
