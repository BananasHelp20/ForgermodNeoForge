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

    /** Transient rendering state; synchronized by GhostwalkVisibility, never saved or copied on death. */
    public static final Supplier<AttachmentType<Boolean>> GHOSTWALK_HIDDEN = TYPES.register(
            "ghostwalk_hidden", () -> AttachmentType.builder(() -> false).build());

    /** Per-viewer outlines, never stored in world/player saves or copied to other players. */
    public static final Supplier<AttachmentType<Map<java.util.UUID, Long>>> ECHO_HIGHLIGHTS = TYPES.register(
            "echo_highlights", () -> AttachmentType.builder(() -> Map.<java.util.UUID, Long>of()).build());

    public static final Supplier<AttachmentType<CrucibleHookRope.Link>> CRUCIBLE_HOOK_ROPE = TYPES.register(
            "crucible_hook_rope", () -> AttachmentType.builder(() -> CrucibleHookRope.NONE).build());

    private WeaponCooldownAttachments() {}
}
