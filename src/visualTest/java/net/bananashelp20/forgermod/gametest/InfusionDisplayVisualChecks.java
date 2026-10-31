package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.InfusionTableBlock;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class InfusionDisplayVisualChecks {
    private static int ticks,waiting;
    private static final BlockPos[] POS={new BlockPos(0,70,0),new BlockPos(5,70,0),new BlockPos(5,70,5),new BlockPos(0,70,5)};
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("forgermod.infusion.visualQA") || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith(java.nio.file.Path.of("build","infusion-client-run")))return;
        if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null){if(waiting++>400)mc.stop();return;}
        if(ticks++==0){
            mc.options.hideGui=true;mc.options.pauseOnLostFocus=false;mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(GameType.CREATIVE);p.setNoGravity(true);
                p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                for(int x=-8;x<=10;x++)for(int z=-8;z<=10;z++){
                    p.serverLevel().setBlockAndUpdate(new BlockPos(x,69,z),Blocks.STONE_BRICKS.defaultBlockState());
                    for(int y=70;y<=75;y++){
                        var position=new BlockPos(x,y,z);
                        if(p.serverLevel().getBlockEntity(position) instanceof InfusionTableBlockEntity old)for(int slot=0;slot<4;slot++)old.itemHandler.setStackInSlot(slot,ItemStack.EMPTY);
                        p.serverLevel().setBlockAndUpdate(position,Blocks.AIR.defaultBlockState());
                    }
                }
                p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(-8,69,-8,11,76,11)).forEach(net.minecraft.world.entity.Entity::discard);
                p.serverLevel().setDayTime(6000);
                var facing=new Direction[]{Direction.NORTH,Direction.EAST,Direction.WEST,Direction.SOUTH};
                var gems=new Item[]{ModItems.RUBY_GEMSTONE.get(),ModItems.AMETHYST_GEMSTONE.get(),ModItems.JADE_GEMSTONE.get()};
                var weapons=new Item[]{ModItems.SHRIEKING_CLAYMORE.get(),ModItems.MOLTEN_AXE.get(),ModItems.WARDENS_NEEDLE.get()};
                for(int i=0;i<4;i++){
                    p.serverLevel().setBlockAndUpdate(POS[i],ModBlocks.INFUSION_TABLE.get().defaultBlockState().setValue(InfusionTableBlock.FACING,facing[i]));
                    if(i==3)continue;
                    var table=(InfusionTableBlockEntity)p.serverLevel().getBlockEntity(POS[i]);
                    if(i!=0)table.itemHandler.setStackInSlot(0,new ItemStack(gems[i],7));
                    if(i!=1)table.itemHandler.setStackInSlot(1,Augmentations.apply(new ItemStack(weapons[i]),Augmentations.MATERIAL_HIT));
                    
                }
                move(mc,0);
            });
        }
        mc.player.setYRot(31.76F);mc.player.setXRot(21F);
        if(ticks==90){
            for(int i=0;i<3;i++){
                var table=(InfusionTableBlockEntity)mc.level.getBlockEntity(POS[i]);
                if(table.displayWeapon().isEmpty()!=(i==1) || table.displayGemstone().isEmpty()!=(i==0) || table.infusing() || table.visualActivity(0)!=0)
                    throw new IllegalStateException("Independent previews failed before template: "+i);
            }
        }
        if(ticks==120)mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();
            ((InfusionTableBlockEntity)server.overworld().getBlockEntity(POS[0])).itemHandler.setStackInSlot(0,new ItemStack(ModItems.RUBY_GEMSTONE.get(),7));
            ((InfusionTableBlockEntity)server.overworld().getBlockEntity(POS[1])).itemHandler.setStackInSlot(1,new ItemStack(ModItems.MOLTEN_AXE.get()));
            for(int i=0;i<3;i++)((InfusionTableBlockEntity)server.overworld().getBlockEntity(POS[i])).itemHandler.setStackInSlot(2,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
        });
        if(ticks==175)for(int i=0;i<3;i++){
            var table=(InfusionTableBlockEntity)mc.level.getBlockEntity(POS[i]);
            if(!table.infusing() || table.visualActivity(0)<.99 || table.animationTime(0)<10)throw new IllegalStateException("Animation did not start: "+i);
        }
        if(ticks==100 || ticks==135 || ticks==200 || ticks==280 || ticks==360 || ticks==430)
            Screenshot.grab(mc.gameDirectory,"infusion-animated-"+ticks+".png",mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Infusion visual: {}",message.getString()));
        if(ticks==230 || ticks==310){int index=ticks==230?1:2;mc.getSingleplayerServer().execute(()->move(mc,index));}
        if(ticks==385)mc.getSingleplayerServer().execute(()->{
            for(int i=0;i<3;i++)((InfusionTableBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS[i])).itemHandler.extractItem(1,1,false);
        });
        if(ticks==420)for(int i=0;i<3;i++){
            var table=(InfusionTableBlockEntity)mc.level.getBlockEntity(POS[i]);
            if(table.displayGemstone().isEmpty() || !table.displayWeapon().isEmpty() || table.infusing() || table.visualActivity(0)!=0)
                throw new IllegalStateException("Cancelled animation or slot previews retained incorrectly: "+i);
        }
        if(ticks>450){ForgerMod.LOGGER.info("Infusion animation checks passed: independent inputs before recipe, all three weapon types, rising/rotating gems, cancellation/settle");mc.stop();}
    }
    private static void move(Minecraft mc,int index){
        var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var pos=POS[index];
        p.teleportTo(pos.getX()+1.8,70.45,pos.getZ()-1.6);
    }
}
