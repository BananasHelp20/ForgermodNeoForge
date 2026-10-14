package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class PyromaniacGameTests {
    @GameTest(template="riftfang_test")
    public static void actualSuccessfulAttackTransfersFireAndFinishesCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var stack=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()),"tooltips.forgermod.ability.pyromaniac");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var victim=EntityType.COW.create(test.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(player.position().add(0,0,2));victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);test.getLevel().addFreshEntity(victim);
        try {
            for(int tick=0;tick<60;tick++)player.doTick();player.setHealth(10);player.setRemainingFireTicks(175);AbilityTestPackets.use(player,0);
            player.attack(victim);
            test.assertTrue(victim.getHealth()<1000 && victim.getRemainingFireTicks()==175 && player.getRemainingFireTicks()<=0 && player.getHealth()==15,"Actual melee pipeline fails to transfer fire/heal");
            WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":SECONDARY";
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==player.level().getGameTime()+1000,"Successful transfer fails to start existing cooldown");
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));victim.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsTransferRemainingFireOnceAndKeepHealing(GameTestHelper test) {
        var player=TestPlayers.survival(test);String id="tooltips.forgermod.ability.pyromaniac";
        try {
            for(var item:java.util.List.of(ModItems.EMBERFANG_DAGGER.get(),ModItems.EMBERFANG_DAGGER_RUBY.get(),ModItems.EMBERFANG_DAGGER_AMBER.get(),ModItems.EMBERFANG_DAGGER_AMETHYST.get(),ModItems.EMBERFANG_DAGGER_JADE.get())) {
                var stack=Augmentations.apply(new ItemStack(item),id);player.setItemInHand(InteractionHand.MAIN_HAND,stack);var weapon=Augmentations.canonical(stack);
                for(int ticks:new int[]{40,175,500}) {
                    var victim=EntityType.COW.create(test.getLevel());player.setHealth(10);player.setRemainingFireTicks(ticks);
                    test.assertTrue(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Burning player cannot arm Pyromaniac");
                    test.assertTrue(player.getRemainingFireTicks()==ticks,"Arming removes fire before the hit");
                    weapon.onDaggerHit(victim,player);
                    test.assertTrue(player.getRemainingFireTicks()<=0 && victim.getRemainingFireTicks()==ticks && player.getHealth()==15,"Hit does not transfer exact remaining fire and heal");
                    weapon.onDaggerHit(victim,player);test.assertTrue(player.getHealth()==15 && victim.getRemainingFireTicks()==ticks,"Activation triggers twice");
                    test.assertFalse(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Unburned player can arm transfer");
                }
                var victim=EntityType.COW.create(test.getLevel());victim.setRemainingFireTicks(300);player.setRemainingFireTicks(80);weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY);weapon.onDaggerHit(victim,player);test.assertTrue(victim.getRemainingFireTicks()==300,"Transfer shortens existing target fire");
                player.setHealth(19);player.setRemainingFireTicks(80);weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY);player.clearFire();victim.clearFire();weapon.onDaggerHit(victim,player);test.assertTrue(player.getHealth()==20 && victim.getRemainingFireTicks()<=0,"Expired fire invents burning or healing exceeds max");
            }
        } finally {net.bananashelp20.forgermod.item.custom.IgnisiumWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void transfersInfiniteBurnButNotIntoFireImmuneTarget(GameTestHelper test) {
        var player=TestPlayers.survival(test);var stack=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()),"tooltips.forgermod.ability.pyromaniac");player.setItemInHand(InteractionHand.MAIN_HAND,stack);var weapon=Augmentations.canonical(stack);
        try {
            var victim=EntityType.COW.create(test.getLevel());InfiniteBurnEvents.ignite(player);weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY);weapon.onDaggerHit(victim,player);
            test.assertTrue(!InfiniteBurnEvents.active(player) && player.getRemainingFireTicks()<=0 && InfiniteBurnEvents.active(victim),"Infinite burn is copied instead of transferred or loses permanence");
            var immune=EntityType.BLAZE.create(test.getLevel());InfiniteBurnEvents.ignite(player);weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY);weapon.onDaggerHit(immune,player);
            test.assertTrue(player.getRemainingFireTicks()<=0 && !InfiniteBurnEvents.active(immune) && immune.getRemainingFireTicks()<=0,"Immune target receives transferred fire");
        } finally {net.bananashelp20.forgermod.item.custom.IgnisiumWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
