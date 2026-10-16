package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** A learned sculk affinity; short potion effects expire normally without removing other buffs. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class HomesickEvents {
    private HomesickEvents() {}

    private static ItemStack source(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof ElectriumWeapon)) return ItemStack.EMPTY;
        ItemStack best = Augmentations.hasPassive(main, AdditionalAbilities.HOMESICK) ? main : ItemStack.EMPTY;
        ItemStack off = player.getOffhandItem();
        if (DaggerItems.hasMatchingDaggers(player) && Augmentations.hasPassive(off, AdditionalAbilities.HOMESICK)
                && (best.isEmpty() || JadeAugments.isJade(off))) best = off;
        return best;
    }

    private static void refresh(ServerPlayer player, ItemStack stack, MobEffectInstance effect) {
        MobEffectInstance granted = JadeAugments.reinforce(stack, AdditionalAbilities.HOMESICK, effect);
        MobEffectInstance current = player.getEffect(granted.getEffect());
        // Avoid reapplying attributes every tick, and never replace longer or stronger external effects.
        if (current == null || current.getAmplifier() < granted.getAmplifier()
                || (current.getAmplifier() == granted.getAmplifier() && !current.isInfiniteDuration()
                    && current.getDuration() <= granted.getDuration() - 10)) player.addEffect(granted);
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || !player.onGround() || !player.level().getBlockState(player.getOnPos()).is(Blocks.SCULK)) return;
        ItemStack stack = source(player);
        if (stack.isEmpty()) return;
        refresh(player, stack, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, false, true));
        refresh(player, stack, new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false, true));
        if (player.tickCount % 10 != 0) return;
        double phase = player.tickCount * 0.08;
        for (int i = 0; i < 8; i++) {
            double angle = phase + i * Math.PI / 4;
            player.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,
                    player.getX() + Math.cos(angle) * 0.65,
                    player.getY() + 0.25 + 0.2 * Math.sin(angle * 2),
                    player.getZ() + Math.sin(angle) * 0.65, 0, 0, 1, 0, 0.035);
        }
        player.serverLevel().sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                player.getX(), player.getY() + 0.7, player.getZ(), 3, 0.35, 0.45, 0.35, 0.02);
    }
}
