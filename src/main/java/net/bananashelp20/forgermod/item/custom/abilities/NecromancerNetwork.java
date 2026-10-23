package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.screen.custom.NecromancerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NecromancerNetwork {
    private NecromancerNetwork(){}
    public record Open(int selected,String item) implements CustomPacketPayload {
        public static final Type<Open> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"necromancer_editor"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Open> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Open::selected,ByteBufCodecs.STRING_UTF8,Open::item,Open::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(Open.TYPE,Open.CODEC,NecromancerNetwork::open);}
    public static void open(Open request,IPayloadContext context){
        if(!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player) || request.selected()!=player.getInventory().selected)return;
        var weapon=player.getMainHandItem();if(!NecromancerMenu.learned(weapon) || !BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString().equals(request.item()))return;
        var loadout=NecromancerLoadouts.get(player,weapon);
        if(BoneGuardEvents.active(player) || BoneGuardEvents.busy(loadout.id)){player.displayClientMessage(Component.literal("Reinforcements must be dead before customizing."),true);return;}
        player.openMenu(new SimpleMenuProvider((id,inventory,ignored)->new NecromancerMenu(id,inventory,request.selected()),Component.literal("Necromancer")),buffer->buffer.writeVarInt(request.selected()));
    }
}
