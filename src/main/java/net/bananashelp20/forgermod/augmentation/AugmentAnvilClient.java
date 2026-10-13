package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public final class AugmentAnvilClient {
    private AugmentAnvilClient() {}

    @SubscribeEvent
    public static void key(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AnvilScreen screen)) return;
        int key = event.getKeyCode();
        if (key != GLFW.GLFW_KEY_LEFT && key != GLFW.GLFW_KEY_RIGHT) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var menu = screen.getMenu();
        var left = menu.getSlot(0).getItem();
        var right = menu.getSlot(1).getItem();
        var choices = AugmentAnvilEvents.selections(player, left, right);
        Augmentations.Replacement prompt = Augmentations.replacementPrompt(left, right, choices);
        if (prompt == null) return;
        String previous = choices.get(prompt.incoming());
        int index = prompt.choices().indexOf(previous);
        int direction = key == GLFW.GLFW_KEY_RIGHT ? 1 : -1;
        index = index < 0 ? (direction > 0 ? 0 : prompt.choices().size() - 1)
                : Math.floorMod(index + direction, prompt.choices().size());
        String replaced = prompt.choices().get(index);
        if (AugmentAnvilEvents.select(player, prompt.incoming(), replaced)) {
            PacketDistributor.sendToServer(new AugmentAnvilNetwork.ChooseReplacementPayload(
                    menu.containerId, prompt.incoming(), replaced));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AnvilScreen screen)) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var menu = screen.getMenu();
        var left = menu.getSlot(0).getItem();
        var right = menu.getSlot(1).getItem();
        var choices = AugmentAnvilEvents.selections(player, left, right);
        Augmentations.Replacement prompt = Augmentations.replacementPrompt(left, right, choices);
        if (prompt == null) return;
        int x = Math.min(screen.width - 155, screen.width / 2 + 94);
        int y = screen.height / 2 - 65;
        var graphics = event.getGuiGraphics();
        graphics.fill(x - 4, y - 5, x + 153, y + 34, 0xCC21152D);
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("screen.forgermod.augment_replace",
                        Augmentations.abilityName(left, prompt.incoming(), Augmentations.level(right, prompt.incoming()))),
                x, y, 0xE7C5FF);
        String chosen = choices.get(prompt.incoming());
        Component selected = chosen == null
                ? Component.translatable("screen.forgermod.augment_choose")
                : Augmentations.abilityName(left, chosen, Augmentations.level(left, chosen));
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("screen.forgermod.augment_arrows", selected), x, y + 16, 0xFFFFFF);
    }
}
