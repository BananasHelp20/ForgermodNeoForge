package net.bananashelp20.forgermod.client;

import net.bananashelp20.forgermod.sound.ElectricalTravel;
import net.bananashelp20.forgermod.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.*;

@OnlyIn(Dist.CLIENT)
public final class ElectricalTravelSound extends AbstractTickableSoundInstance {
    private final ElectricalTravel.Payload arc;
    private final ClientLevel level;
    private int age;
    private ElectricalTravelSound(ElectricalTravel.Payload arc,ClientLevel level){
        super(ModSounds.ELECTRIC_TRAVEL.get(),SoundSource.PLAYERS,RandomSource.create());this.arc=arc;this.level=level;volume=.65F;pitch=1;move(0);
    }
    private void move(int tick){var at=ElectricalTravel.position(arc.from(),arc.to(),tick);x=at.x;y=at.y;z=at.z;}
    public static void play(ElectricalTravel.Payload arc){
        var mc=Minecraft.getInstance();if(mc.level!=null && mc.level.dimension().location().equals(arc.dimension()))mc.getSoundManager().play(new ElectricalTravelSound(arc,mc.level));
    }
    @Override public void tick(){
        if(Minecraft.getInstance().level!=level || ++age>ElectricalTravel.TICKS+1){stop();return;}
        move(age);
    }
}
