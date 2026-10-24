package net.bananashelp20.forgermod.item.custom.abilities;

import com.mojang.blaze3d.platform.InputConstants;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.screen.custom.NecromancerMenu;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.settings.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class NecromancerClient {
    private static final IKeyConflictContext CONTEXT=new IKeyConflictContext(){
        @Override public boolean isActive(){return KeyConflictContext.IN_GAME.isActive() && available(Minecraft.getInstance());}
        @Override public boolean conflicts(IKeyConflictContext other){return other==this || other==KeyConflictContext.IN_GAME;}
    };
    private static final KeyMapping CUSTOMIZE=new KeyMapping("key.forgermod.necromancer_customize",CONTEXT,
            KeyModifier.SHIFT,InputConstants.Type.MOUSE,GLFW.GLFW_MOUSE_BUTTON_RIGHT,"key.categories.forgermod");
    private NecromancerClient(){}
    private static boolean available(Minecraft minecraft){
        return minecraft.player!=null && minecraft.screen==null && NecromancerMenu.learned(minecraft.player.getMainHandItem());
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Pre event){
        var minecraft=Minecraft.getInstance();
        while(CUSTOMIZE.consumeClick()){
            if(!available(minecraft))continue;
            // Consume an overlapping vanilla use/attack binding for this customized mouse or keyboard chord.
            for(var binding:new KeyMapping[]{minecraft.options.keyUse,minecraft.options.keyAttack,minecraft.options.keyPickItem})
                if(binding.getKey().equals(CUSTOMIZE.getKey()))while(binding.consumeClick()){}
            PacketDistributor.sendToServer(new NecromancerNetwork.Open(minecraft.player.getInventory().selected,
                    BuiltInRegistries.ITEM.getKey(minecraft.player.getMainHandItem().getItem()).toString()));
        }
    }
    @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered event){
        if(available(Minecraft.getInstance()) && CUSTOMIZE.isActiveAndMatches(event.getKeyMapping().getKey())){
            // Also prevent held-button repeats from interacting with a block, entity or offhand item.
            event.setCanceled(true);event.setSwingHand(false);
        }
    }
    @EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        private Registration(){}
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent event){event.register(CUSTOMIZE);}
    }
}
