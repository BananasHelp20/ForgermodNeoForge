package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.EchoAppetiteEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class EchoAppetiteGameTests {
    @GameTest(template="riftfang_test")
    public static void approvedRankedDescriptionsAndHardCapsMatchGameplay(GameTestHelper test){
        String id=AdditionalAbilities.ECHO_APPETITE;int count=0;
        for(var entry:ModItems.ITEMS.getEntries()){
            if(!(entry.get() instanceof PulsiteWeapon))continue;count++;
            var stack=new ItemStack(entry.get());
            for(int rank=1;rank<=2;rank++){
                stack=Augmentations.apply(stack,id);test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,id)==rank,"Cannot learn approved rank");
                for(boolean compact:new boolean[]{false,true}){
                    var text=AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    String reward=2*rank+" hunger bars";
                    test.assertTrue(text.getString().equals(compact?"Breaking sculk restores hunger. Warden kills restore "+reward+".":"Breaking Sculk restores hunger based on what sculk block you broke. Killing a Warden also restores "+reward+".") && white.toString().equals(reward),"Current description or white highlight disagrees with rank");
                }
                var changes=AugmentUpgradeDescriptions.changes(stack,id,rank);
                test.assertTrue(rank==2?changes.isEmpty():changes.size()==2 && changes.stream().anyMatch(c->c.property().equals("warden_hunger") && ((Number)c.after()).intValue()==4) && changes.stream().anyMatch(c->c.property().equals("sculk_hunger_multiplier") && ((Number)c.after()).intValue()==2),"Upgrade card omits doubled hunger rewards");
            }
            test.assertTrue(Augmentations.apply(stack,id).isEmpty() && !Augmentations.hasSuperForm(stack,id),"Echo Appetite exceeds II or gains a super form");
        }
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get(),ModItems.SHRIEKING_CLAYMORE.get())){
            var blank=new ItemStack(item);
            test.assertTrue(Augmentations.maxRank(blank,id)==2 && !Augmentations.setLevel(blank,id,2).isEmpty() && Augmentations.setLevel(blank,id,3).isEmpty(),"Weapon/template/command does not enforce rank II cap");
        }
        test.assertTrue(EchoAppetiteValues.wardenFood(0)==0 && EchoAppetiteValues.wardenFood(99)==8 && count==15,"Rank boundaries or variant coverage incorrect");
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allFifteenVariantsFeedOnOnlyActualSculkMining(GameTestHelper test) {
        var player=TestPlayers.survival(test);var pos=test.absolutePos(new BlockPos(8,5,8));int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.ECHO_APPETITE))==eligible,"Wrong appetite pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.getFoodData().setFoodLevel(10);fresh.mineBlock(test.getLevel(),Blocks.SCULK.defaultBlockState(),pos,player);test.assertTrue(player.getFoodData().getFoodLevel()==10,"Fresh weapon feeds");
            for(int rank=1;rank<=2;rank++){
            var stack=Augmentations.setLevel(fresh,AdditionalAbilities.ECHO_APPETITE,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var blocks=java.util.List.of(Blocks.SCULK,Blocks.SCULK_VEIN,Blocks.SCULK_SENSOR,Blocks.CALIBRATED_SCULK_SENSOR,Blocks.SCULK_CATALYST,Blocks.SCULK_SHRIEKER);
            int[] food={2,1,3,4,6,4};
            for(int i=0;i<blocks.size();i++){
                var sculk=blocks.get(i);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(2);int wear=stack.getDamageValue();
                stack.mineBlock(test.getLevel(),sculk.defaultBlockState(),pos,player);
                test.assertTrue(player.getFoodData().getFoodLevel()==Math.min(20,10+food[i]*rank) && player.getFoodData().getSaturationLevel()==2,"Wrong block/rank hunger or saturation: "+sculk+" rank "+rank);
                if(sculk.defaultBlockState().getDestroySpeed(test.getLevel(),pos)!=0)test.assertTrue(stack.getDamageValue()>wear,"Mining wear suppressed");
            }
            player.getFoodData().setFoodLevel(10);stack.mineBlock(test.getLevel(),Blocks.STONE.defaultBlockState(),pos,player);test.assertTrue(player.getFoodData().getFoodLevel()==10,"Stone grants hunger");
            player.getFoodData().setFoodLevel(20);stack.mineBlock(test.getLevel(),Blocks.SCULK.defaultBlockState(),pos,player);test.assertTrue(player.getFoodData().getFoodLevel()==20,"Food exceeds maximum");
            stack.setDamageValue(stack.getMaxDamage()-1);player.getFoodData().setFoodLevel(10);stack.mineBlock(test.getLevel(),Blocks.SCULK.defaultBlockState(),pos,player);test.assertTrue(stack.isEmpty() && player.getFoodData().getFoodLevel()==10+2*rank,"Last-durability sculk hit loses ranked benefit");
            }
        }}finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void serverDestroyBlockActuallyRestoresHunger(GameTestHelper test) {
        var player=TestPlayers.survival(test);var pos=test.absolutePos(new BlockPos(8,5,8));
        try {test.getLevel().setBlockAndUpdate(pos,Blocks.SCULK.defaultBlockState());player.getFoodData().setFoodLevel(10);
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),AdditionalAbilities.ECHO_APPETITE));
            test.assertTrue(player.gameMode.destroyBlock(pos),"Actual sculk break failed");test.assertTrue(player.getFoodData().getFoodLevel()==12 && test.getLevel().getBlockState(pos).isAir(),"Actual block destruction does not feed");
        }finally {player.server.getPlayerList().remove(player);}test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void everyPulsiteVariantFeedsOnCreditedWardenKillsOnly(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        var warden=EntityType.WARDEN.create(test.getLevel());
        var zombie=EntityType.ZOMBIE.create(test.getLevel());
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof PulsiteWeapon))continue;
            count++;
            var fresh=new ItemStack(entry.get());
            var credited=new LivingDeathEvent(warden,player.damageSources().playerAttack(player));
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(2);
            EchoAppetiteEvents.onWardenDeath(credited);
            test.assertTrue(player.getFoodData().getFoodLevel()==10,"Unlearned Warden kill feeds");
            var learned=Augmentations.apply(fresh,AdditionalAbilities.ECHO_APPETITE);
            player.setItemInHand(InteractionHand.MAIN_HAND,learned);
            credited.setCanceled(true);EchoAppetiteEvents.onWardenDeath(credited);
            test.assertTrue(player.getFoodData().getFoodLevel()==10,"Canceled Warden death feeds");
            credited.setCanceled(false);
            EchoAppetiteEvents.onWardenDeath(new LivingDeathEvent(zombie,player.damageSources().playerAttack(player)));
            EchoAppetiteEvents.onWardenDeath(new LivingDeathEvent(warden,player.damageSources().generic()));
            test.assertTrue(player.getFoodData().getFoodLevel()==10,"Wrong victim or uncredited death feeds");
            EchoAppetiteEvents.onWardenDeath(credited);
            test.assertTrue(player.getFoodData().getFoodLevel()==14 && player.getFoodData().getSaturationLevel()==2,
                    "Warden kill should restore two drumsticks without saturation");
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            EchoAppetiteEvents.onWardenDeath(credited);
            test.assertTrue(player.getFoodData().getFoodLevel()==14,"Switching to an unlearned weapon still feeds");
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.setLevel(fresh,AdditionalAbilities.ECHO_APPETITE,2));
            player.getFoodData().setFoodLevel(10);EchoAppetiteEvents.onWardenDeath(credited);
            test.assertTrue(player.getFoodData().getFoodLevel()==18 && player.getFoodData().getSaturationLevel()==2,"Level II does not double Warden reward without saturation");
        }} finally {warden.discard();zombie.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Pulsite variants");test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void realWardenDeathFeedsAndCapsAtFullHunger(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        var warden=EntityType.WARDEN.create(test.getLevel());
        warden.setNoAi(true);warden.setNoGravity(true);warden.setPos(player.getX()+2,player.getY(),player.getZ());
        test.getLevel().addFreshEntity(warden);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND,
                    Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),AdditionalAbilities.ECHO_APPETITE));
            player.getFoodData().setFoodLevel(18);player.getFoodData().setSaturation(2);
            warden.setHealth(1);warden.hurt(player.damageSources().playerAttack(player),5);
            test.assertFalse(warden.isAlive(),"Warden survived lethal player damage");
            test.assertTrue(player.getFoodData().getFoodLevel()==20 && player.getFoodData().getSaturationLevel()==2,
                    "Actual Warden death did not cap food without adding saturation");
        } finally {warden.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
