package net.bananashelp20.forgermod.item.custom.abilities;

import com.mojang.blaze3d.platform.InputConstants;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public final class WeaponAbilityClient {
    private static final String CATEGORY = "key.categories.forgermod";
    private static final KeyMapping PRIMARY = new KeyMapping(
            "key.forgermod.ability_primary", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    private static final KeyMapping SECONDARY = new KeyMapping(
            "key.forgermod.ability_secondary", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    private WeaponAbilityClient() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (PRIMARY.consumeClick()) sendAbility(WeaponAbilitySlot.PRIMARY);
        while (SECONDARY.consumeClick()) sendAbility(WeaponAbilitySlot.SECONDARY);
    }

    private static void sendAbility(WeaponAbilitySlot slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof SwordItemWithEffect)) return;
        PacketDistributor.sendToServer(new WeaponAbilityNetwork.UseAbilityPayload(slot.ordinal()));
    }

    @EventBusSubscriber(modid = ForgerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(PRIMARY);
            event.register(SECONDARY);
        }
    }
}
