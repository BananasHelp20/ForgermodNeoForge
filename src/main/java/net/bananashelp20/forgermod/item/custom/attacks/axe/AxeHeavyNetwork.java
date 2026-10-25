package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
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
    private record PendingSlam(ServerLevel level, Item axe, long dueTick, BlockPos center, float damage,
                               boolean flaming, ItemStack source) {
        private PendingSlam(ServerLevel level, Item axe, long dueTick, BlockPos center, float damage) {
            this(level, axe, dueTick, center, damage, false, ItemStack.EMPTY);
        }
    }

    private AxeHeavyNetwork() {}

    public record AxeAttackPayload(int attackKind) implements CustomPacketPayload {
        public static final Type<AxeAttackPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "axe_attack"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AxeAttackPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, AxeAttackPayload::attackKind, AxeAttackPayload::new);

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
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator() || net.bananashelp20.forgermod.item.custom.abilities.WeaponControlLocks.frozen(player)) return;
        if (payload.attackKind() < 0 || payload.attackKind() > 1) return;
        boolean slam = queueAttack(player);
        // Vanilla resets only the client ticker for a miss. Mirror that on the server,
        // including weak misses, after sampling charge; entity hits reset themselves.
        if (payload.attackKind() == 1) player.resetAttackStrengthTicker();
        if (slam) sendAnimation(player);
    }

    private static void sendAnimation(ServerPlayer player) {
        player.getPersistentData().putLong(ANIMATION_TAG,player.level().getGameTime());
        var viewers=new java.util.HashSet<>(player.serverLevel().getChunkSource().chunkMap.getPlayers(player.chunkPosition(),false));
        viewers.add(player);
        for(var viewer:viewers)
            if(net.neoforged.neoforge.network.registration.NetworkRegistry.hasChannel(viewer.connection,SlamAnimationPayload.TYPE.id()))
                PacketDistributor.sendToPlayer(viewer,new SlamAnimationPayload(player.getId()));
    }

    private static boolean queueAttack(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!AxeItems.canAttackPacket(stack) || !player.getOffhandItem().isEmpty()) {
            COMBOS.remove(player.getUUID());
            PENDING.remove(player.getUUID());
            return false;
        }

        UUID id = player.getUUID();
        long now = player.level().getGameTime();
        if (net.bananashelp20.forgermod.item.custom.abilities.FlamingStompAbility.consume(player, stack)) {
            COMBOS.remove(id);
            BlockPos center = findGroundAhead(player);
            if (center == null) center = BlockPos.containing(player.position().add(player.getLookAngle().multiply(2, 0, 2)));
            PENDING.put(id, new PendingSlam(player.serverLevel(), stack.getItem(), now + 3, center,
                    (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE), true, stack));
            return true;
        }
        // Sample before the following vanilla attack packet resets the attack ticker.
        if (!AxeItems.canVerticalStrike(stack) || player.getAttackStrengthScale(0) < 1.0F) return false;
        Combo combo = COMBOS.get(id);
        if (combo == null || combo.axe() != stack.getItem()
                || now - combo.firstTick() > COMBO_WINDOW_TICKS
                || now - combo.lastTick() > COMBO_WINDOW_TICKS) {
            COMBOS.put(id, new Combo(stack.getItem(), now, now, 1));
            return false;
        }
        // Air swings do not reset the server's vanilla ticker; enforce a full recharge
        // between accepted packets too, without weakening the following melee attack.
        if (now - combo.lastTick() < Math.ceil(player.getCurrentItemAttackStrengthDelay())) return false;
        if (combo.count() < 2) {
            COMBOS.put(id, new Combo(stack.getItem(), combo.count() == 0 ? now : combo.firstTick(), now, combo.count() + 1));
            return false;
        }

        // Keep the last accepted tick even after the third swing to reject duplicate packets.
        COMBOS.put(id, new Combo(stack.getItem(), now, now, 0));
        BlockPos center = findGroundAhead(player);
        if (center == null) return false;

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        PENDING.put(id, new PendingSlam(player.serverLevel(), stack.getItem(), now + 3, center, damage, false, stack));
        return true;
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

    /** An ability-supplied strike uses normal Vertical Strike geometry and passive interactions immediately. */
    public static boolean abilityVerticalStrike(ServerPlayer player,ItemStack source,Vec3 center) {
        if(!player.isAlive() || player.isSpectator() || source!=player.getMainHandItem()
                || !AxeItems.isAxe(source) || !player.getOffhandItem().isEmpty()
                || net.bananashelp20.forgermod.item.custom.abilities.WeaponControlLocks.frozen(player))return false;
        COMBOS.remove(player.getUUID());PENDING.remove(player.getUUID());
        sendAnimation(player);
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        landSlam(player,new PendingSlam(player.serverLevel(),source.getItem(),player.level().getGameTime(),
                BlockPos.containing(center),(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE),false,source));
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!AxeItems.canAttackPacket(player.getMainHandItem()) || !player.getOffhandItem().isEmpty()
                || !player.isAlive() || player.isSpectator() || net.bananashelp20.forgermod.item.custom.abilities.WeaponControlLocks.frozen(player)) {
            COMBOS.remove(player.getUUID());
            PENDING.remove(player.getUUID());
            return;
        }
        PendingSlam slam = PENDING.get(player.getUUID());
        if (slam == null || player.level().getGameTime() < slam.dueTick()) return;
        PENDING.remove(player.getUUID());
        if (player.level() != slam.level() || !player.getMainHandItem().is(slam.axe())
                || !slam.source().isEmpty() && player.getMainHandItem() != slam.source()) return;
        landSlam(player, slam);
    }

    /** Convert an accepted melee hit without striking its primary victim twice. */
    public static void echoVerticalStrike(ServerPlayer player,ItemStack source,LivingEntity primary,float bonus){
        if(source!=player.getMainHandItem() || !player.isAlive())return;
        COMBOS.remove(player.getUUID());PENDING.remove(player.getUUID());sendAnimation(player);player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        var slam=new PendingSlam(player.serverLevel(),source.getItem(),player.level().getGameTime(),primary.blockPosition(),(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE),false,source);
        net.bananashelp20.forgermod.item.custom.abilities.EchoSalvageEvents.vertical(player,()->landSlamBody(player,slam,primary,bonus));
    }
    private static void landSlam(ServerPlayer player, PendingSlam slam) {
        net.bananashelp20.forgermod.item.custom.abilities.EchoSalvageEvents.vertical(player,()->landSlamBody(player,slam,null,Float.NaN));
    }
    private static void landSlamBody(ServerPlayer player,PendingSlam slam,LivingEntity skipped,float suppliedBonus){
        float echoBonus=Float.isNaN(suppliedBonus)?net.bananashelp20.forgermod.item.custom.abilities.EchoSalvageEvents.bonus(player):suppliedBonus;
        boolean hitAnything=skipped!=null,attemptedHit=skipped!=null;
        double storedKnockback=net.bananashelp20.forgermod.item.custom.abilities.NullReceiptEvents.release(player);
        ServerLevel level = slam.level();
        BlockPos center = slam.center();
        int radius = slam.flaming() ? 2 : 1;
        AABB area = new AABB(center.getX() - radius, center.getY(), center.getZ() - radius,
                center.getX() + radius + 1, center.getY() + 2.5, center.getZ() + radius + 1);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            if(target==skipped)continue;
            if (target instanceof Player other && !player.canHarmPlayer(other)) continue;
            if (!player.hasLineOfSight(target)) continue;

            if (slam.flaming() && net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityTargets.enemy(player, target))
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 100));

            attemptedHit=true;
            int previousInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (target.hurt(player.damageSources().playerAttack(player), slam.damage()+echoBonus)) {
                hitAnything=true;
                if (slam.axe() instanceof SwordItemWithEffect weapon) {
                    weapon.applyMaterialEffect(target, player.getMainHandItem());
                    var runtime = net.bananashelp20.forgermod.augmentation.Augmentations.canonical(player.getMainHandItem());
                    if (runtime != null) runtime.onDaggerHit(target, player);
                }
                target.knockback(0.5+storedKnockback, center.getX() + 0.5 - target.getX(),
                        center.getZ() + 0.5 - target.getZ());
            } else {
                target.invulnerableTime = previousInvulnerableTime;
            }
        }

        boolean solidImpact=!level.getBlockState(center.below()).getCollisionShape(level,center.below()).isEmpty();
        net.bananashelp20.forgermod.item.custom.abilities.EchoSalvageEvents.onImpact(player,hitAnything,attemptedHit,solidImpact,slam.damage());

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.sendParticles(slam.flaming() ? ParticleTypes.FLAME : ParticleTypes.CLOUD, center.getX() + x + 0.5,
                        center.getY() + 0.1, center.getZ() + z + 0.5,
                        5, 0.25, 0.05, 0.25, 0.02);
            }
        }
        net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilitySounds.verticalStrikeImpact(level,center,SoundSource.PLAYERS);
        if(slam.flaming())level.playSound(null,center,SoundEvents.FIRECHARGE_USE,SoundSource.PLAYERS,.9F,.75F);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        COMBOS.remove(event.getEntity().getUUID());
        PENDING.remove(event.getEntity().getUUID());
    }
}
