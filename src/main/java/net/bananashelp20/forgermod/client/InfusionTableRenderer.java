package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.custom.InfusionTableBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Independent slot previews and a client-side gemstone-setting animation. No item entities. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class InfusionTableRenderer implements BlockEntityRenderer<InfusionTableBlockEntity> {
    private record Point(double x,double y,double z){}
    private record Layout(Point gemstone,Point weapon){}
    private static final Layout DEFAULT_LAYOUT=new Layout(new Point(.75,15.625/16,13.875/16),new Point(.5,19.5/16,7.5/16));
    private final Layout layout;
    private final java.util.Map<InfusionTableBlockEntity,Long> particleTicks=new java.util.WeakHashMap<>();
    public InfusionTableRenderer(BlockEntityRendererProvider.Context context){
        Layout loaded=DEFAULT_LAYOUT;
        var location=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"rendering/infusion_table.json");
        try(var stream=Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow().open();
            var reader=new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)){
            var value=new com.google.gson.Gson().fromJson(reader,Layout.class);
            if(value!=null && valid(value.gemstone) && valid(value.weapon))loaded=value;
        }catch(java.io.IOException | RuntimeException exception){ForgerMod.LOGGER.warn("Using default infusion display anchors",exception);}
        layout=loaded;
    }
    private static boolean valid(Point point){return point!=null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.INFUSION_TABLE_BE.get(),InfusionTableRenderer::new);
    }
    @Override public int getViewDistance(){return 32;}
    @Override public AABB getRenderBoundingBox(InfusionTableBlockEntity table){return new AABB(table.getBlockPos()).inflate(.125,0,.125).expandTowards(0,1,0);}
    @Override public void render(InfusionTableBlockEntity table,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        if(table.getLevel()==null)return;
        var gem=table.displayGemstone();var weapon=table.displayWeapon();
        if(gem.isEmpty() && weapon.isEmpty())return;
        double lift=table.visualActivity(partial);lift=lift*lift*(3-2*lift);
        double time=table.animationTime(partial);
        double gx=layout.gemstone.x+(layout.weapon.x-layout.gemstone.x)*lift;
        double gy=layout.gemstone.y+.009+(layout.weapon.y+.34-layout.gemstone.y-.009)*lift+Math.sin(time*.12)*.025*lift;
        double gz=layout.gemstone.z+(layout.weapon.z-layout.gemstone.z)*lift;
        var items=Minecraft.getInstance().getItemRenderer();
        poses.pushPose();
        try {
            poses.translate(.5,0,.5);
            poses.mulPose(Axis.YP.rotationDegrees(180-table.getBlockState().getValue(InfusionTableBlock.FACING).toYRot()));
            poses.translate(-.5,0,-.5);
            if(!gem.isEmpty()){
            poses.pushPose();
            // The remaining tray in the user's edited model is centred at (12,15.625,13.875).
            poses.translate(gx,gy,gz);
            poses.mulPose(Axis.YP.rotationDegrees((float)((time*2%360)*lift)));
            poses.mulPose(Axis.XP.rotationDegrees((float)(90*(1-lift))));
            float gemScale=(float)(.13+.09*lift);poses.scale(gemScale,gemScale,gemScale);
            items.renderStatic(gem,ItemDisplayContext.FIXED,light,overlay,poses,buffers,table.getLevel(),table.getBlockPos().hashCode());
            poses.popPose();
            }
            if(!weapon.isEmpty()){
            poses.pushPose();
            boolean mesh=items.getModel(weapon,table.getLevel(),null,0).isGui3d();
            poses.translate(layout.weapon.x,layout.weapon.y+(mesh?.025:.017),layout.weapon.z);
            poses.mulPose(Axis.XP.rotationDegrees(90));
            if(!mesh)poses.mulPose(Axis.ZP.rotationDegrees(-45));
            float scale=mesh?.6F:.42F;poses.scale(scale,scale,scale);
            items.renderStatic(weapon,ItemDisplayContext.FIXED,light,overlay,poses,buffers,table.getLevel(),table.getBlockPos().hashCode());
            poses.popPose();
            }
        }finally{poses.popPose();}
        if(table.infusing() && !gem.isEmpty() && !weapon.isEmpty())sparks(table,gem,gx,gy,gz,time);
    }
    private void sparks(InfusionTableBlockEntity table,net.minecraft.world.item.ItemStack gem,double gx,double gy,double gz,double time){
        var level=table.getLevel();long tick=level.getGameTime();
        if(tick%3!=0 || java.util.Objects.equals(particleTicks.put(table,tick),tick))return;
        var color=gem.is(net.bananashelp20.forgermod.item.ModItems.RUBY_GEMSTONE.get())?new org.joml.Vector3f(.9F,.12F,.2F)
                :gem.is(net.bananashelp20.forgermod.item.ModItems.AMBER_GEMSTONE.get())?new org.joml.Vector3f(1,.6F,.12F)
                :gem.is(net.bananashelp20.forgermod.item.ModItems.JADE_GEMSTONE.get())?new org.joml.Vector3f(.2F,.85F,.45F):new org.joml.Vector3f(.7F,.3F,1);
        double yaw=Math.toRadians(180-table.getBlockState().getValue(InfusionTableBlock.FACING).toYRot());
        for(int i=0;i<3;i++){
            double angle=time*.12+i*Math.PI*2/3;
            double x=gx+Math.cos(angle)*.16,z=gz+Math.sin(angle)*.16;
            var point=world(table,x,gy-.04,z,yaw);
            level.addParticle(new net.minecraft.core.particles.DustParticleOptions(color,.65F),point[0],point[1],point[2],0,.008,0);
        }
        var point=world(table,layout.weapon.x,layout.weapon.y+.05,layout.weapon.z,yaw);
        level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,point[0],point[1],point[2],0,.22,0);
        if(tick%6==0)level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,point[0],point[1],point[2],0,.015,0);
    }
    private static double[] world(InfusionTableBlockEntity table,double x,double y,double z,double yaw){
        x-=.5;z-=.5;var pos=table.getBlockPos();
        return new double[]{pos.getX()+.5+x*Math.cos(yaw)+z*Math.sin(yaw),pos.getY()+y,pos.getZ()+.5-x*Math.sin(yaw)+z*Math.cos(yaw)};
    }
}
