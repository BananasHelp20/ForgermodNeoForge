package net.bananashelp20.forgermod.sound;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,ForgerMod.MOD_ID);
    public static final DeferredHolder<SoundEvent,SoundEvent> VOID_PULSE_CHARGE=SOUNDS.register("void_pulse_charge",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"void_pulse_charge")));
    private ModSounds() {}
}
