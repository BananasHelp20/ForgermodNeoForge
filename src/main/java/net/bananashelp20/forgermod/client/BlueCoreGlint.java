package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;

import java.io.IOException;

/** Vanilla foil animation with an item-local blue shader, including resource reloads. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class BlueCoreGlint extends RenderType {
    private static ShaderInstance shader;
    private static final ShaderStateShard BLUE_SHADER = new ShaderStateShard(() -> shader);
    private static final RenderType ITEM = glint("blue_core_glint", false, false);
    private static final RenderType TRANSLUCENT = glint("blue_core_glint_translucent", false, true);
    private static final RenderType ENTITY = glint("blue_core_entity_glint", true, true);
    private static final RenderType DIRECT = glint("blue_core_entity_glint_direct", true, false);

    private BlueCoreGlint(String name, VertexFormat format, VertexFormat.Mode mode, int size,
                          boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }

    private static RenderType glint(String name, boolean entity, boolean itemTarget) {
        var state = CompositeState.builder().setShaderState(BLUE_SHADER)
                .setTextureState(new TextureStateShard(entity ? ItemRenderer.ENCHANTED_GLINT_ENTITY
                        : ItemRenderer.ENCHANTED_GLINT_ITEM, true, false))
                .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                .setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY)
                .setTexturingState(entity ? ENTITY_GLINT_TEXTURING : GLINT_TEXTURING);
        if (itemTarget) state.setOutputState(ITEM_ENTITY_TARGET);
        return create(name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 1536,
                state.createCompositeState(false));
    }

    public static RenderType map(RenderType original) {
        if (original == RenderType.glint()) return ITEM;
        if (original == RenderType.glintTranslucent()) return TRANSLUCENT;
        if (original == RenderType.entityGlint()) return ENTITY;
        if (original == RenderType.entityGlintDirect()) return DIRECT;
        return original;
    }

    @SubscribeEvent
    public static void registerBuffers(RegisterRenderBuffersEvent event) {
        // Foil must stay buffered until after the base item has written its depth.
        event.registerRenderBuffer(ITEM);
        event.registerRenderBuffer(TRANSLUCENT);
        event.registerRenderBuffer(ENTITY);
        event.registerRenderBuffer(DIRECT);
    }

    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "blue_glint"),
                DefaultVertexFormat.POSITION_TEX), loaded -> shader = loaded);
    }
}
