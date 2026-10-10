package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.lang.reflect.Proxy;

/** Exercise the server packet handler rather than bypassing its learned/cooldown/session validation. */
final class AbilityTestPackets {
    private AbilityTestPackets() {}
    static void use(ServerPlayer player,int slot) throws Exception {
        var method=WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility",WeaponAbilityNetwork.UseAbilityPayload.class,IPayloadContext.class);
        method.setAccessible(true);
        var context=(IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),new Class<?>[]{IPayloadContext.class},
                (proxy,called,args)->called.getName().equals("player")?player:null);
        method.invoke(null,new WeaponAbilityNetwork.UseAbilityPayload(slot,player.getInventory().selected,
                BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()),context);
    }
}
