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
    public static final DeferredHolder<SoundEvent,SoundEvent> ELECTRIC_TRAVEL=SOUNDS.register("electric_travel",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"electric_travel")));
    public static final DeferredHolder<SoundEvent,SoundEvent> ELECTRIC_CHAIN=SOUNDS.register("electric_chain",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"electric_chain")));
    public static final DeferredHolder<SoundEvent,SoundEvent> RINGING_STEEL_HIT=SOUNDS.register("ringing_steel_hit",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"ringing_steel_hit")));
    private ModSounds() {}
    public static final DeferredHolder<SoundEvent,SoundEvent> ROOTING_ROOTS_GROW=SOUNDS.register("rooting_roots_grow",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"rooting_roots_grow")));
    public static final DeferredHolder<SoundEvent,SoundEvent> ROOTING_ROOTS_HOLD=SOUNDS.register("rooting_roots_hold",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"rooting_roots_hold")));
    public static final DeferredHolder<SoundEvent,SoundEvent> ROOTING_ROOTS_RETREAT=SOUNDS.register("rooting_roots_retreat",
            ()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"rooting_roots_retreat")));
}
