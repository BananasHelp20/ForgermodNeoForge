package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Visible spectral equipment uses vanilla rendering; invisible body means no solid entity shadow. */
public final class DreamEchoEntity extends ArmorStand {
    public DreamEchoEntity(ServerPlayer owner,Vec3 position) { super(owner.level(),position.x,position.y,position.z); }
    @Override public boolean hurt(DamageSource source,float amount) { return false; }
    @Override public InteractionResult interactAt(Player player,Vec3 point,InteractionHand hand) { return InteractionResult.FAIL; }
    @Override public boolean shouldBeSaved() { return false; }
}
