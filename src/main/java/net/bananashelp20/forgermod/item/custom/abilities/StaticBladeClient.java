package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class StaticBladeClient {
    private StaticBladeClient() {}
    @SubscribeEvent
    public static void swing(InputEvent.InteractionKeyMappingTriggered event) {
        if(!event.isAttack() || event.isCanceled()) return;
        var minecraft=Minecraft.getInstance();
        if(minecraft.player!=null && minecraft.screen==null && Augmentations.ids(minecraft.player.getMainHandItem(),true).contains(AdditionalAbilities.STATIC_BLADE))
            PacketDistributor.sendToServer(new StaticBladeNetwork.SwingPayload());
    }
}
