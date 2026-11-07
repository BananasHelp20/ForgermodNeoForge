package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;

/** Colors only the foil pass; the original template sprite is never tinted. */
@net.neoforged.fml.common.EventBusSubscriber(modid="forgermod",bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class AncientTemplateGlint extends RenderStateShard {
    private AncientTemplateGlint(){super("ancient_template_glint",()->{},()->{});}
    private static final float[][] PREVIOUS_COLOR={new float[]{1,1,1,1}};
    private static final TransparencyStateShard BLUE=new TransparencyStateShard("ancient_blue_glint",()->{
        PREVIOUS_COLOR[0]=RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor(.15F,.65F,1F,PREVIOUS_COLOR[0][3]);
        GLINT_TRANSPARENCY.setupRenderState();
    },()->{
        GLINT_TRANSPARENCY.clearRenderState();
        float[] color=PREVIOUS_COLOR[0];RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);
    });
    private static RenderType create(String name,ShaderStateShard shader,boolean entity,boolean translucent){
        var state=RenderType.CompositeState.builder().setShaderState(shader)
                .setTextureState(new TextureStateShard(entity?ItemRenderer.ENCHANTED_GLINT_ENTITY:ItemRenderer.ENCHANTED_GLINT_ITEM,true,false))
                .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
                .setTransparencyState(BLUE).setTexturingState(entity?ENTITY_GLINT_TEXTURING:GLINT_TEXTURING);
        if(translucent)state.setOutputState(ITEM_ENTITY_TARGET);
        return RenderType.create(name,DefaultVertexFormat.POSITION_TEX,VertexFormat.Mode.QUADS,1536,state.createCompositeState(false));
    }
    private static final RenderType ITEM=create("ancient_blue_glint",RENDERTYPE_GLINT_SHADER,false,false);
    private static final RenderType TRANSLUCENT=create("ancient_blue_glint_translucent",RENDERTYPE_GLINT_TRANSLUCENT_SHADER,false,true);
    private static final RenderType ENTITY=create("ancient_blue_entity_glint",RENDERTYPE_ENTITY_GLINT_SHADER,true,false);
    private static final RenderType DIRECT=create("ancient_blue_entity_glint_direct",RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER,true,false);
    @net.neoforged.bus.api.SubscribeEvent
    public static void registerBuffers(net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent event){
        for(var type:java.util.List.of(ITEM,TRANSLUCENT,ENTITY,DIRECT))event.registerRenderBuffer(type);
    }
    public static MultiBufferSource wrap(MultiBufferSource source){
        return type->source.getBuffer(type==RenderType.glint()?ITEM:type==RenderType.glintTranslucent()?TRANSLUCENT
                :type==RenderType.entityGlint()?ENTITY:type==RenderType.entityGlintDirect()?DIRECT:type);
    }
}
