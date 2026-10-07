package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class AxeHeavyNetwork {
    public static final String ANIMATION_TAG = "forgermod.axe_slam_start";
    private static final long COMBO_WINDOW_TICKS = 200;
    private static final Map<UUID, Combo> COMBOS = new HashMap<>();
    private static final Map<UUID, PendingSlam> PENDING = new HashMap<>();

    private record Combo(Item axe, long firstTick, long lastTick, int count) {}
    private record PendingSlam(ServerLevel level, Item axe, long dueTick, BlockPos center, float damage) {}

    private AxeHeavyNetwork() {}

    public record AxeAttackPayload(int unused) implements CustomPacketPayload {
        public static final Type<AxeAttackPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "axe_attack"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AxeAttackPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, AxeAttackPayload::unused, AxeAttackPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SlamAnimationPayload(int playerId) implements CustomPacketPayload {
        public static final Type<SlamAnimationPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "axe_slam_animation"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SlamAnimationPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, SlamAnimationPayload::playerId, SlamAnimationPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                AxeAttackPayload.TYPE, AxeAttackPayload.STREAM_CODEC, AxeHeavyNetwork::handleAttack);
        event.registrar("1").playToClient(
                SlamAnimationPayload.TYPE, SlamAnimationPayload.STREAM_CODEC, AxeHeavyNetwork::handleAnimation);
    }

    private static void handleAnimation(SlamAnimationPayload payload, IPayloadContext context) {
        if (context.player().level().getEntity(payload.playerId()) instanceof Player player) {
            player.getPersistentData().putLong(ANIMATION_TAG, player.level().getGameTime());
        }
    }

    private static void handleAttack(AxeAttackPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        ItemStack stack = player.getMainHandItem();
        if (!AxeItems.isAxe(stack)) return;

        UUID id = player.getUUID();
        long now = player.level().getGameTime();
        Combo combo = COMBOS.get(id);
        if (combo == null || combo.axe() != stack.getItem()
                || now - combo.firstTick() > COMBO_WINDOW_TICKS
                || now - combo.lastTick() > COMBO_WINDOW_TICKS) {
            COMBOS.put(id, new Combo(stack.getItem(), now, now, 1));
            return;
        }
        // Ignore duplicate packets and swings too close together to be separate attacks.
        if (now - combo.lastTick() < 3) return;
        if (combo.count() == 1) {
            COMBOS.put(id, new Combo(stack.getItem(), combo.firstTick(), now, 2));
            return;
        }

        COMBOS.remove(id);
        BlockPos center = findGroundAhead(player);
        if (center == null) return;

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        PENDING.put(id, new PendingSlam(player.serverLevel(), stack.getItem(), now + 3, center, damage));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SlamAnimationPayload(player.getId()));
    }

    private static BlockPos findGroundAhead(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        double horizontal = Math.hypot(look.x, look.z);
        if (horizontal < 0.01) return null;
        double x = player.getX() + look.x / horizontal * 2.0;
        double z = player.getZ() + look.z / horizontal * 2.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = player.blockPosition().getY() + 1; y >= player.blockPosition().getY() - 4; y--) {
            pos.set(x, y, z);
            BlockState state = player.level().getBlockState(pos);
            if (!state.getCollisionShape(player.level(), pos).isEmpty()) return pos.above();
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PendingSlam slam = PENDING.get(player.getUUID());
        if (slam == null || player.level().getGameTime() < slam.dueTick()) return;
        PENDING.remove(player.getUUID());
        if (player.level() != slam.level() || !player.getMainHandItem().is(slam.axe())) return;
        landSlam(player, slam);
    }

    private static void landSlam(ServerPlayer player, PendingSlam slam) {
        ServerLevel level = slam.level();
        BlockPos center = slam.center();
        AABB area = new AABB(center.getX() - 1, center.getY(), center.getZ() - 1,
                center.getX() + 2, center.getY() + 2.5, center.getZ() + 2);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            if (target instanceof Player other && !player.canHarmPlayer(other)) continue;
            if (!player.hasLineOfSight(target)) continue;

            int previousInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (target.hurt(player.damageSources().playerAttack(player), slam.damage())) {
                target.knockback(0.5, center.getX() + 0.5 - target.getX(),
                        center.getZ() + 0.5 - target.getZ());
            } else {
                target.invulnerableTime = previousInvulnerableTime;
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.sendParticles(ParticleTypes.CLOUD, center.getX() + x + 0.5,
                        center.getY() + 0.1, center.getZ() + z + 0.5,
                        5, 0.25, 0.05, 0.25, 0.02);
            }
        }
        level.playSound(null, center, SoundEvents.IRON_GOLEM_ATTACK,
                SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        COMBOS.remove(event.getEntity().getUUID());
        PENDING.remove(event.getEntity().getUUID());
    }
}
