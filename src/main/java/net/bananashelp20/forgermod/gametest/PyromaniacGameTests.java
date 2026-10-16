package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class PyromaniacGameTests {
    private static final String ID=IgnisiumWeapon.PYROMANIAC;
    @GameTest(template="riftfang_test")
    public static void actualAttackPassivelyExtinguishesAndHealsTwoWithoutCooldown(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var stack=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()),ID);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var victim=EntityType.COW.create(test.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(player.position().add(0,0,2));victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);test.getLevel().addFreshEntity(victim);
        try {
            for(int tick=0;tick<60;tick++)player.doTick();player.setHealth(10);player.setRemainingFireTicks(175);player.attack(victim);
            test.assertTrue(victim.getHealth()<1000 && player.getRemainingFireTicks()<=0 && player.getHealth()==12,"Actual attack does not extinguish and heal exactly 2 HP without activation");
            test.assertTrue(Augmentations.hasPassive(stack,ID) && !Augmentations.ids(stack,true).contains(ID),"Pyromaniac occupies an active slot");
            for(var slot:WeaponAbilitySlot.values())test.assertFalse(Augmentations.canonical(stack).activateAbility(player,stack,slot),"Pyromaniac still arms an active");
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Passive creates a cooldown");
            player.setRemainingFireTicks(80);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(player.getHealth()==14 && player.getRemainingFireTicks()<=0,"Reigniting cannot trigger again immediately");
        } finally {victim.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsRequireBurningAndSuccessfulDamageAtEveryRank(GameTestHelper test) {
        var player=TestPlayers.survival(test);var victim=EntityType.COW.create(test.getLevel());victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);victim.setHealth(10000);test.getLevel().addFreshEntity(victim);
        try {
            for(var item:java.util.List.of(ModItems.EMBERFANG_DAGGER.get(),ModItems.EMBERFANG_DAGGER_RUBY.get(),ModItems.EMBERFANG_DAGGER_AMBER.get(),ModItems.EMBERFANG_DAGGER_AMETHYST.get(),ModItems.EMBERFANG_DAGGER_JADE.get())) {
                var stack=new ItemStack(item);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setHealth(10);player.setRemainingFireTicks(80);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
                test.assertTrue(player.getHealth()==10 && player.getRemainingFireTicks()==80,"Unaugmented dagger triggers Pyromaniac");
                for(int rank=1;rank<=4;rank++) {
                    stack=Augmentations.apply(stack,ID);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.clearFire();player.setHealth(10);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
                    test.assertTrue(player.getHealth()==10,"Non-burning hit heals");
                    player.setRemainingFireTicks(80);victim.setInvulnerable(true);victim.hurt(player.damageSources().playerAttack(player),2);victim.setInvulnerable(false);
                    test.assertTrue(player.getHealth()==10 && player.getRemainingFireTicks()==80,"Rejected hit triggers Pyromaniac");
                    victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
                    test.assertTrue(player.getHealth()==12 && player.getRemainingFireTicks()<=0,"Wrong healing or extinguishing at rank "+rank);
                    player.setHealth(19);InfiniteBurnEvents.ignite(player);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
                    test.assertTrue(player.getHealth()==20 && !InfiniteBurnEvents.active(player) && player.getRemainingFireTicks()<=0,"Infinite burning remains or healing exceeds maximum");
                }
            }
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));player.setHealth(10);player.setRemainingFireTicks(80);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(player.getHealth()==10 && player.getRemainingFireTicks()==80,"Passive remains after switching weapons");
        } finally {victim.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void matchingOffhandSharesPassiveWithoutStackingAndOldSavesKeepRanks(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        var main=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()),Augmentations.DUAL_WIELD);
        var off=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER_RUBY.get()),ID);
        var data=off.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();var augments=data.getCompound("forgermod_augments");
        augments.getList("levels",net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putInt("level",3);
        var order=new net.minecraft.nbt.ListTag();order.add(net.minecraft.nbt.StringTag.valueOf(ID));augments.put("active_order",order);
        off.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
        test.assertTrue(Augmentations.hasPassive(off,ID) && Augmentations.level(off,ID)==3 && Augmentations.ids(off,true).isEmpty(),"Saved active loses rank or occupies active slot");
        player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);
        var victim=EntityType.COW.create(test.getLevel());victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);test.getLevel().addFreshEntity(victim);
        try {
            player.setHealth(10);player.setRemainingFireTicks(80);victim.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(player.getHealth()==12 && player.getRemainingFireTicks()<=0,"Matching offhand passive fails");
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(main,ID));player.setRemainingFireTicks(80);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);test.assertTrue(player.getHealth()==14,"Two passive copies stack");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.EMBERFANG_DAGGER.get()));player.setRemainingFireTicks(80);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);test.assertTrue(player.getHealth()==14 && player.getRemainingFireTicks()==80,"Offhand shares without learned Dual Wield");
        } finally {victim.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
