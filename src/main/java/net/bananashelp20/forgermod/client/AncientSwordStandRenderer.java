package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.block.entity.custom.AncientSwordStandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AncientSwordStandRenderer implements BlockEntityRenderer<AncientSwordStandBlockEntity> {
    private static final ModelResourceLocation[] FRAMES = new ModelResourceLocation[AncientSwordStandBlockEntity.LAST_FRAME + 1];
    static {
        for (int i = 0; i < FRAMES.length; i++) {
            FRAMES[i] = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                    ForgerMod.MOD_ID, "block/ancient_sword_stand/opening_" + i));
        }
    }

    public AncientSwordStandRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public AABB getRenderBoundingBox(AncientSwordStandBlockEntity stand) {
        return new AABB(stand.getBlockPos()).inflate(0.5, 0, 0.5).expandTowards(0, 1, 0);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        for (ModelResourceLocation frame : FRAMES) event.register(frame);
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ANCIENT_SWORD_STAND_BE.get(), AncientSwordStandRenderer::new);
    }

    @Override
    public void render(AncientSwordStandBlockEntity stand, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int light, int overlay) {
        // Resting poses use the chunk mesh. Only opening stands draw here.
        if (stand.getLevel() == null || !stand.getBlockState().getValue(AncientSwordStandBlock.OPENING)) return;
        Minecraft client = Minecraft.getInstance();
        var model = client.getModelManager().getModel(FRAMES[stand.animationFrame(stand.getLevel().getGameTime(), partialTick)]);
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        float rotation = switch (stand.getBlockState().getValue(AncientSwordStandBlock.FACING)) {
            case EAST -> -90;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        };
        poses.mulPose(Axis.YP.rotationDegrees(rotation));
        poses.translate(-0.5, 0, -0.5);
        client.getBlockRenderer().getModelRenderer().renderModel(poses.last(), buffers.getBuffer(RenderType.cutout()),
                stand.getBlockState(), model, 1, 1, 1, light, overlay, ModelData.EMPTY, RenderType.cutout());
        poses.popPose();
    }
}
