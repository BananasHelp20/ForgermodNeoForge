package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.codec.StreamCodec;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class DoubleJumpNetwork {
    private static final DoubleJumpState STATE = new DoubleJumpState();
    private record Loadout(net.minecraft.world.item.ItemStack main,net.minecraft.world.item.ItemStack off,int slot,net.minecraft.world.level.Level level) {}
    private static final java.util.Map<java.util.UUID,Loadout> LOADOUTS=new java.util.HashMap<>();
    private DoubleJumpNetwork() {}
    private static int jumpRank(net.minecraft.world.item.ItemStack stack) {
        return Augmentations.hasPassive(stack,Augmentations.DOUBLE_JUMP)?Augmentations.level(stack,Augmentations.DOUBLE_JUMP):0;
    }
    private static int jumps(int rank) {return rank==0?0:rank>=4?2:1;}
    public static int airJumps(Player player) {
        int main=jumpRank(player.getMainHandItem());
        return jumps(main)+(net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player)?jumps(jumpRank(player.getOffhandItem())):0);
    }
    public static double lift(Player player) {
        int rank=jumpRank(player.getMainHandItem());
        if(net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player))rank=Math.max(rank,jumpRank(player.getOffhandItem()));
        return .55+.05*Math.clamp(rank-4,0,3);
    }
    private static boolean unchanged(ServerPlayer player) {
        var old=LOADOUTS.get(player.getUUID());return old!=null && old.main==player.getMainHandItem() && old.off==player.getOffhandItem() && old.slot==player.getInventory().selected && old.level==player.level();
    }
    private static void clear(java.util.UUID id) {STATE.clear(id);LOADOUTS.remove(id);}

    public record DoubleJumpPayload() implements CustomPacketPayload {
        public static final Type<DoubleJumpPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "double_jump"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleJumpPayload> STREAM_CODEC =
                StreamCodec.unit(new DoubleJumpPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                DoubleJumpPayload.TYPE, DoubleJumpPayload.STREAM_CODEC, DoubleJumpNetwork::handle);
        event.registrar("1").playToClient(
                DoubleJumpAcceptedPayload.TYPE, DoubleJumpAcceptedPayload.STREAM_CODEC, DoubleJumpNetwork::handleAccepted);
    }

    public record DoubleJumpAcceptedPayload(double lift) implements CustomPacketPayload {
        public DoubleJumpAcceptedPayload() { this(.55); }
        public static final Type<DoubleJumpAcceptedPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "double_jump_accepted"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleJumpAcceptedPayload> STREAM_CODEC =
                StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.DOUBLE,DoubleJumpAcceptedPayload::lift,DoubleJumpAcceptedPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void handleAccepted(DoubleJumpAcceptedPayload payload, IPayloadContext context) {
        boost(context.player(),payload.lift());
    }

    private static void boost(Player player,double lift) {
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, lift, velocity.z);
        player.hasImpulse = true;
        player.fallDistance = 0;
    }

    private static void handle(DoubleJumpPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.onGround()
                || player.isSpectator() || !player.isAlive() || WeaponControlLocks.frozen(player)
                || airJumps(player)==0 || !unchanged(player) || !STATE.consume(player.getUUID())) return;
        double lift=lift(player);
        boost(player,lift);
        // A full server motion packet overwrites the client's current sprint momentum.
        // Acknowledge only the lift, preserving the client's horizontal movement.
        PacketDistributor.sendToPlayer(player, new DoubleJumpAcceptedPayload(lift));
        player.serverLevel().sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY(), player.getZ(), 12, 0.35, 0.1, 0.35, 0.05);
    }

    @SubscribeEvent
    public static void onNormalJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && airJumps(player)>0) {
            STATE.normalJump(player.getUUID(),airJumps(player));
            LOADOUTS.put(player.getUUID(),new Loadout(player.getMainHandItem(),player.getOffhandItem(),player.getInventory().selected,player.level()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && (!player.isAlive() || player.onGround() && player.getDeltaMovement().y <= 0
                || airJumps(player)==0 || !unchanged(player))) {
            clear(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getEntity().getUUID());
    }
}
