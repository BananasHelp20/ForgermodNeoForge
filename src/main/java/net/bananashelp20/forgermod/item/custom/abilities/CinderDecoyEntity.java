package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Uses vanilla armor-stand networking/rendering, but cannot be looted or saved as real equipment. */
public final class CinderDecoyEntity extends ArmorStand {
    private final UUID owner;
    public CinderDecoyEntity(ServerPlayer player) {
        super(player.level(),player.getX(),player.getY(),player.getZ()); owner=player.getUUID();
    }
    @Override public boolean hurt(DamageSource source,float amount) {
        if(!level().isClientSide && source.getEntity() instanceof Enemy) CinderDecoyEvents.detonate(owner);
        return false;
    }
    @Override public InteractionResult interactAt(Player player,Vec3 location,InteractionHand hand) { return InteractionResult.FAIL; }
    @Override public boolean shouldBeSaved() { return false; }
}
