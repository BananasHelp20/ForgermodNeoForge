package net.bananashelp20.forgermod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InfusionTableScreen extends AbstractContainerScreen<InfusionTableMenu> {
    public InfusionTableScreen(InfusionTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "textures/gui/infusion_table/infusion_table_gui.png");
    private ResourceLocation arrow_texture;

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        pGuiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(pGuiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            arrow_texture = ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, menu.getCorrectTexture());
            guiGraphics.blit(this.arrow_texture, x + 18, y + 20, 0 , 0, menu.getScaledArrowProgress(), 46, 141, 46);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
