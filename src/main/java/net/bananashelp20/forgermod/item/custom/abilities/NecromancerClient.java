package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.screen.custom.NecromancerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class NecromancerClient {
    private static boolean queued;
    private NecromancerClient(){}
    @SubscribeEvent public static void key(InputEvent.Key event){
        var minecraft=Minecraft.getInstance();if(minecraft.player==null || minecraft.screen!=null || !NecromancerMenu.learned(minecraft.player.getMainHandItem())){queued=false;return;}
        int modifiers=GLFW.GLFW_MOD_SHIFT|GLFW.GLFW_MOD_ALT;boolean combo=(event.getModifiers()&modifiers)==modifiers;
        if(event.getKey()==GLFW.GLFW_KEY_S && event.getAction()==GLFW.GLFW_PRESS && combo){queued=false;return;}
        boolean modifier=event.getKey()==GLFW.GLFW_KEY_LEFT_SHIFT || event.getKey()==GLFW.GLFW_KEY_RIGHT_SHIFT || event.getKey()==GLFW.GLFW_KEY_LEFT_ALT || event.getKey()==GLFW.GLFW_KEY_RIGHT_ALT;
        if(!modifier)return;if(event.getAction()==GLFW.GLFW_PRESS && combo)queued=true;
        // On release avoids stealing the existing Shift+Alt+S ability rotation chord.
        if(event.getAction()==GLFW.GLFW_RELEASE && queued){queued=false;PacketDistributor.sendToServer(new NecromancerNetwork.Open(minecraft.player.getInventory().selected,BuiltInRegistries.ITEM.getKey(minecraft.player.getMainHandItem().getItem()).toString()));}
    }
}
