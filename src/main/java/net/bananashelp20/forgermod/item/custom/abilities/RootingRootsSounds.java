package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import java.util.WeakHashMap;

/** One spatial sound per binding phase, never per display segment. */
public final class RootingRootsSounds {
    public enum Phase { GROW, HOLD, RETREAT }
    private static final class Budget {
        long tick=Long.MIN_VALUE;
        final java.util.Map<net.minecraft.core.BlockPos,int[]> counts=new java.util.HashMap<>();
    }
    private static final WeakHashMap<ServerLevel,Budget> BUDGETS=new WeakHashMap<>();
    private RootingRootsSounds() {}
    public static void play(ServerLevel level,double x,double y,double z,Phase phase) {
        var budget=BUDGETS.computeIfAbsent(level,key->new Budget());
        long now=level.getGameTime();
        if(budget.tick!=now) {budget.tick=now;budget.counts.clear();}
        // Nearby simultaneous roots blend into a group cue rather than a deafening stack.
        var cell=new net.minecraft.core.BlockPos((int)Math.floor(x/16),(int)Math.floor(y/16),(int)Math.floor(z/16));
        var counts=budget.counts.computeIfAbsent(cell,key->new int[Phase.values().length]);
        if(counts[phase.ordinal()]++>=3) return;
        SoundEvent sound=switch(phase) {
            case GROW -> ModSounds.ROOTING_ROOTS_GROW.get();
            case HOLD -> ModSounds.ROOTING_ROOTS_HOLD.get();
            case RETREAT -> ModSounds.ROOTING_ROOTS_RETREAT.get();
        };
        float volume=phase==Phase.HOLD?.42F:.9F;
        level.playSound(null,x,y,z,sound,SoundSource.PLAYERS,volume,.96F+level.random.nextFloat()*.08F);
    }
}
