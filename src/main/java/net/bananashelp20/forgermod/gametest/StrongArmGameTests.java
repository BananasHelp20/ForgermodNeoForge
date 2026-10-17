package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StrongArmGameTests {
    private static ServerPlayer player(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setYRot(0);player.setXRot(0);
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)for(int z=0;z<=3;z++) test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        return player;
    }
    private static Zombie target(GameTestHelper test,ServerPlayer player) {
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);zombie.setHealth(1000);zombie.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(zombie);return zombie;
    }
    private static ItemStack weapon(Item item,int rank) {return Augmentations.setLevel(new ItemStack(item),RecommendedAbilities.STRONG_ARM,rank);}
    private static void equip(Zombie zombie) {
        zombie.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));zombie.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));
    }
    private static int armor(Zombie zombie) {int count=0;for(var item:zombie.getArmorSlots())if(!item.isEmpty())count++;return count;}
    private static void strike(GameTestHelper test,ServerPlayer player,Zombie target,boolean critical,int charge,long seed) {
        player.resetAttackStrengthTicker();
        int ticks=charge==2?80:charge==1?Math.max(0,(int)Math.floor(player.getCurrentItemAttackStrengthDelay())-1):0;
        for(int i=0;i<ticks;i++)player.doTick();
        if(charge==1)test.assertTrue(player.getAttackStrengthScale(0)<1 && player.getAttackStrengthScale(.5F)>.9F,"Partial vanilla-critical fixture is not between 90% and 100% charge");
        target.invulnerableTime=0;target.setHealth(target.getMaxHealth());target.setPos(player.position().add(0,0,2));
        player.setOnGround(!critical);player.fallDistance=critical?1:0;player.setSprinting(false);player.setDeltaMovement(0,0,0);player.getRandom().setSeed(seed);
        player.attack(target);
    }
    @GameTest(template="riftfang_test")
    public static void failedCriticalRollStartsCooldownButArmorlessAttemptDoesNot(GameTestHelper test) throws Exception {
        var player=player(test);var zombie=target(test,player);var stack=weapon(ModItems.MOLTEN_AXE.get(),1);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.STRONG_ARM;
        try {
            AbilityTestPackets.use(player,0);
            test.assertFalse(StrongArmAbility.active(player) || player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Armorless activation arms Strong-arm or starts cooldown");
            zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));AbilityTestPackets.use(player,0);
            test.assertTrue(StrongArmAbility.active(player) && !player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Arming starts cooldown before its hit");
            strike(test,player,zombie,true,2,0);
            test.assertTrue(!zombie.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && !StrongArmAbility.active(player),"Failed critical roll breaks armor or remains armed");
            WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+1200,"Failed qualifying roll does not start its full cooldown");
            test.succeed();
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));zombie.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsBreakExactlyOneArmorOnMaxRankChargedCritical(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),ModItems.MOLTEN_AXE_AMBER.get(),ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            var player=player(test);var zombie=target(test,player);equip(zombie);var stack=weapon(item,4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            try {
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.STRONG_ARM),"Cannot arm Strong-arm");
                test.assertTrue(armor(zombie)==4 && zombie.getHealth()==1000,"Activation breaks armor or damages without a hit");
                strike(test,player,zombie,true,2,0);
                test.assertTrue(armor(zombie)==3 && zombie.getHealth()<1000 && !StrongArmAbility.active(player),"Actual charged critical does not consume one armor-breaking attempt");
                equip(zombie);zombie.setPos(player.position().add(0,0,2));StrongArmAbility.activate(player,stack);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1);
                strike(test,player,zombie,true,2,0);
                test.assertTrue(!zombie.isAlive() && armor(zombie)==3 && !StrongArmAbility.active(player),"Lethal charged critical does not break exactly one armor piece");
            } finally {StrongArmAbility.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);}
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void onlyFullyChargedSuccessfulCriticalHitsConsumeArmedAttempt(GameTestHelper test) {
        var player=player(test);var zombie=target(test,player);equip(zombie);var stack=weapon(ModItems.MOLTEN_AXE.get(),4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            StrongArmAbility.activate(player,stack);
            strike(test,player,zombie,false,2,0);
            test.assertTrue(StrongArmAbility.active(player) && armor(zombie)==4,"Fully charged ordinary hit consumes Strong-arm");
            strike(test,player,zombie,true,1,0);
            test.assertTrue(StrongArmAbility.active(player) && armor(zombie)==4,"Partially charged vanilla critical consumes Strong-arm");
            strike(test,player,zombie,true,0,0);
            test.assertTrue(StrongArmAbility.active(player) && armor(zombie)==4,"Uncharged attack consumes Strong-arm");
            zombie.setInvulnerable(true);strike(test,player,zombie,true,2,0);zombie.setInvulnerable(false);
            test.assertTrue(StrongArmAbility.active(player) && armor(zombie)==4,"Rejected critical hit consumes Strong-arm");
            zombie.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(StrongArmAbility.active(player) && armor(zombie)==4,"Unrelated same-tick damage borrows a rejected critical flag");
            for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})zombie.setItemSlot(slot,ItemStack.EMPTY);
            strike(test,player,zombie,true,2,0);test.assertTrue(StrongArmAbility.active(player),"Armorless landed critical consumes Strong-arm");equip(zombie);
            strike(test,player,zombie,true,2,0);
            test.assertFalse(StrongArmAbility.active(player),"Valid critical does not consume Strong-arm");
            equip(zombie);zombie.setPos(player.position().add(0,0,2));StrongArmAbility.activate(player,stack);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(StrongArmAbility.active(player),"Switching stacks leaves an armed attempt");
            test.succeed();
        } finally {StrongArmAbility.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void rankOneCriticalChanceHasSuccessAndFailure(GameTestHelper test) {
        var player=player(test);var zombie=target(test,player);var stack=weapon(ModItems.MOLTEN_AXE.get(),1);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        int successes=0,failures=0;
        try {
            for(int seed=0;seed<100;seed++) {
                long randomSeed=seed*7919L;zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));zombie.setPos(player.position().add(0,0,2));
                test.assertTrue(StrongArmAbility.activate(player,stack),"Valid armor attempt rejected");
                strike(test,player,zombie,true,2,randomSeed);
                boolean broken=zombie.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
                test.assertTrue(broken==(net.minecraft.util.RandomSource.create(randomSeed).nextFloat()<.3F),"Rank I critical chance differs from 30%");
                if(broken)successes++;else failures++;
            }
            test.assertTrue(successes>0 && failures>0 && StrongArmAbility.chance(2)==.55F && StrongArmAbility.chance(3)==.8F && StrongArmAbility.chance(4)==1,"Chance progression changed");
            test.succeed();
        } finally {StrongArmAbility.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);}
    }
}
