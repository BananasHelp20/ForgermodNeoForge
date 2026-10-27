package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.attacks.axe.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in visual fixture, run only in the isolated build/axe-client-run world. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AxeAnimationVisualChecks {
    private static final boolean ENABLED=Boolean.getBoolean("forgermod.axe.visualQA");
    private static int ticks,index=-1;
    private static boolean initialized;
    private static int verified=-1;
    private static final AxeAnimationData.Clip[] CLIPS={AxeAnimationData.Clip.IDLE,AxeAnimationData.Clip.SWING,AxeAnimationData.Clip.SWING,AxeAnimationData.Clip.SWING2,AxeAnimationData.Clip.VERTICAL,AxeAnimationData.Clip.VERTICAL,AxeAnimationData.Clip.GRAVITY_CAST,AxeAnimationData.Clip.GRAVITY_CAST,AxeAnimationData.Clip.GRAVITY_CAST,AxeAnimationData.Clip.IDLE,AxeAnimationData.Clip.SWING,AxeAnimationData.Clip.VERTICAL,AxeAnimationData.Clip.GRAVITY_CAST,AxeAnimationData.Clip.GRAVITY_CAST,AxeAnimationData.Clip.GRAVITY_CAST,AxeAnimationData.Clip.SPIN,AxeAnimationData.Clip.SPIN,AxeAnimationData.Clip.WALK,AxeAnimationData.Clip.SWING2};
    private static final double[] TIMES={0,.5,.625,.5,.83333,1.25,.5,1.625,3.25,0,.625,1.25,.5,1.625,3.25,.75,1.5,.25,.3};
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        // Launch arguments may survive an IDE devlaunch using generated argument files.
        // Never mutate a normal game directory, even when an old test flag is present.
        if(!ENABLED || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith(java.nio.file.Path.of("build","axe-client-run")))return;
        if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
        if(Boolean.getBoolean("forgermod.axe.cameraAttackQA")){cameraAttackTest(mc);return;}
        if(!initialized){
            initialized=true;mc.options.hideGui=false;mc.options.pauseOnLostFocus=false;
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(p==null)return;
                p.setGameMode(GameType.CREATIVE);
                for(var entity:p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(-16,60,-16,16,90,16)))entity.discard();
                for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,69,z),Blocks.STONE.defaultBlockState());
                p.teleportTo(.5,70,.5);p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Boolean.getBoolean("forgermod.axe.visualVoid")?ModItems.NULLIFIED_AXE.get():ModItems.MOLTEN_AXE.get()));p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            });return;
        }
        if(++ticks<100)return;
        int next=(ticks-100)/20;if(next>=CLIPS.length){if(ticks>100+CLIPS.length*20+30)mc.stop();return;}
        if(next!=index){index=next;mc.options.setCameraType(index>=9 && index<15?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(0);mc.player.setXRot(0);mc.player.yBodyRot=mc.player.yBodyRotO=0;}
        var tag=mc.player.getPersistentData();tag.putInt(AxeAnimationNetwork.CLIP,CLIPS[index].ordinal());tag.putLong(AxeAnimationNetwork.START,mc.level.getGameTime()-(long)(TIMES[index]*20));tag.putLong(AxeAnimationNetwork.SEQUENCE,10_000L+ticks);
        if(verified!=index){verifyRig(mc);verified=index;}
        if((ticks-100)%20==10)Screenshot.grab(mc.gameDirectory,String.format(java.util.Locale.ROOT,"axe-%02d-%s.png",index,CLIPS[index].name),mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Axe visual check: {}",message.getString()));
    }
    private static void verifyRig(Minecraft mc){
        var player=mc.player;var renderer=(net.minecraft.client.renderer.entity.player.PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(player);
        var model=renderer.getModel();model.setupAnim(player,0,0,player.tickCount,0,0);
        var sample=net.bananashelp20.forgermod.client.AxeAnimationClient.sample(player,0);
        var armPose=new com.mojang.blaze3d.vertex.PoseStack();model.rightArm.translateAndRotate(armPose);
        boolean slim=player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;
        var armCenter=armPose.last().pose().transformPosition(new org.joml.Vector3f(slim?-.5F/16:-1F/16,4F/16,0));
        var referenceArm=new org.joml.Matrix4f();reference(referenceArm,sample,"right_arm",4,22,1,0,0,0);
        referenceArm.translate(4,22,1).rotateX((float)Math.toRadians(37.5)).translate(-4,-22,-1);
        var sourceCenter=referenceArm.transformPosition(new org.joml.Vector3f(6,18,1));
        var expectedCenter=new org.joml.Vector3f(-sourceCenter.x/16,(24-sourceCenter.y)/16,(sourceCenter.z-1)/16);
        if(armCenter.distance(expectedCenter)>.0002)throw new IllegalStateException("Arm cube rest-pose mismatch at "+sample.clip());
        var rootPose=new com.mojang.blaze3d.vertex.PoseStack();net.bananashelp20.forgermod.client.AxeAnimationClient.root(player,0,rootPose);
        var rootCenter=rootPose.last().pose().transformPosition(new org.joml.Vector3f(-6F*.9375F/16,18F*.9375F/16,0));
        var referenceRoot=new org.joml.Matrix4f();reference(referenceRoot,sample,"player_model",0,17.33333F,1,0,0,0);
        var rootSource=referenceRoot.transformPosition(new org.joml.Vector3f(6,18,1));
        var expectedRoot=new org.joml.Vector3f(-rootSource.x*.9375F/16,rootSource.y*.9375F/16,-(rootSource.z-1)*.9375F/16);
        if(rootCenter.distance(expectedRoot)>.0002)throw new IllegalStateException("Player root mismatch at "+sample.clip());
        var actual=new com.mojang.blaze3d.vertex.PoseStack();model.rightArm.translateAndRotate(actual);
        net.bananashelp20.forgermod.client.AxeAnimationClient.heldTransform(player,player.getMainHandItem(),net.minecraft.world.entity.HumanoidArm.RIGHT,actual,0);
        // Independent source-space hierarchy: arm bone -> axe bone -> converted mesh.
        // No Minecraft cube pivots, arm-rest correction, or inventory origin are used here.
        var expected=new org.joml.Matrix4f();reference(expected,sample,"right_arm",4,22,1,0,0,0);
        reference(expected,sample,"axe",6,15.25F,-4,-90,37.5F,-90);
        double origin=Boolean.getBoolean("forgermod.axe.visualVoid")?21:10;
        for(float[] point:new float[][]{{0,0,0},{0,30.5F,0},{1,41,1}}){
            var input=new org.joml.Vector3f(point[0]/16,(float)((point[1]-origin)/16),point[2]/16);
            var got=actual.last().pose().transformPosition(input);
            var source=expected.transformPosition(new org.joml.Vector3f(6+point[0]*.65625F,1.94688F+point[1]*.65625F,-4.0375F+point[2]*.65625F));
            var wanted=new org.joml.Vector3f(-source.x/16,(24-source.y)/16,(source.z-1)/16);
            if(got.distance(wanted)>.0002)throw new IllegalStateException("Axe rig mismatch at "+sample.clip()+"/"+sample.seconds()+": "+got+" expected "+wanted);
        }
        ForgerMod.LOGGER.info("Source rig comparison passed: {} at {}",sample.clip(),sample.seconds());
    }
    private static void reference(org.joml.Matrix4f matrix,net.bananashelp20.forgermod.client.AxeAnimationClient.Sample sample,String bone,float x,float y,float z,float rx,float ry,float rz){
        var p=AxeAnimationData.sample(sample.clip(),bone,sample.seconds());
        matrix.translate((float)p.position().x(),(float)p.position().y(),(float)p.position().z()).translate(x,y,z)
                .rotateZ((float)Math.toRadians(rz+p.rotation().z())).rotateY((float)Math.toRadians(ry+p.rotation().y())).rotateX((float)Math.toRadians(rx+p.rotation().x())).translate(-x,-y,-z);
    }

    private static void cameraAttackTest(Minecraft mc){
        if(!initialized){
            initialized=true;mc.options.setCameraType(CameraType.FIRST_PERSON);mc.options.pauseOnLostFocus=false;
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.teleportTo(.5,70,.5);p.setYRot(0);p.setXRot(0);
                p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.MOLTEN_AXE.get()));p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                for(int x=-3;x<=3;x++)for(int z=-3;z<=5;z++){
                    p.serverLevel().setBlockAndUpdate(new BlockPos(x,69,z),Blocks.STONE.defaultBlockState());
                    for(int y=70;y<=74;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
                }
                var mob=net.minecraft.world.entity.EntityType.ZOMBIE.create(p.serverLevel());mob.setNoAi(true);mob.setNoGravity(true);
                mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
                mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.setPos(.5,70,2.5);p.serverLevel().addFreshEntity(mob);
            });return;
        }
        ticks++;
        if(ticks==100 || ticks==145 || ticks==190){
            mc.options.setCameraType(ticks==145?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);
            mc.player.setYRot(0);mc.player.setXRot(0);
            var target=mc.level.getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class,mc.player.getBoundingBox().inflate(4)).stream().filter(m->m.getMaxHealth()==1000).findFirst().orElseThrow();
            mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);
            ForgerMod.LOGGER.info("Camera attack regression: attack {} in {}",ticks,mc.options.getCameraType());
        }
        if(ticks==235)mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            var target=p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class,p.getBoundingBox().inflate(4)).stream().filter(m->m.getMaxHealth()==1000).findFirst().orElseThrow();
            if(target.getHealth()>970)throw new IllegalStateException("Camera-switch attacks failed to damage target: "+target.getHealth());
            ForgerMod.LOGGER.info("Camera attack regression passed: three attacks, remaining HP {}",target.getHealth());
            mc.execute(mc::stop);
        });
    }

}
