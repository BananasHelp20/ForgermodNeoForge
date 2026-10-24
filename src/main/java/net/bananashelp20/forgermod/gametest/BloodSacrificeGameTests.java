package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BloodSacrificeGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsSpendAbsorptionFirstDoubleDamageAndCancelOnSwitch(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);var cow=EntityType.COW.create(test.getLevel());int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof VulnusiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.BLOOD_SACRAFICE))==eligible,"Wrong Sacrifice pool");if(!eligible) continue;count++;
            player.setHealth(20);player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,600,1));player.setAbsorptionAmount(8);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.BLOOD_SACRAFICE),"Fresh weapon sacrifices");var stack=Augmentations.apply(fresh,AdditionalAbilities.BLOOD_SACRAFICE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BLOOD_SACRAFICE),"Sacrifice refused");test.assertTrue(player.getHealth()==14 && player.getAbsorptionAmount()==0,"Half combined pool must be paid absorption first");
            test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BLOOD_SACRAFICE),"Active stance charges twice");test.assertTrue(player.getHealth()==14 && player.getAbsorptionAmount()==0,"Reactivation spends health");
            mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(mob.getHealth()==18,"Melee damage not doubled");
            mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().indirectMagic(player,player),1);test.assertTrue(mob.getHealth()==18,"Weapon ability damage not doubled");
            cow.setHealth(10);cow.invulnerableTime=0;cow.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(cow.getHealth()==8,"Normal neutral-target weapon damage not doubled");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(BloodSacrificeEvents.active(player),"Switch retains stance");mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(mob.getHealth()==19,"Switch retains damage bonus");
        }} finally {BloodSacrificeEvents.cancel(player.getUUID());mob.discard();cow.discard();player.setHealth(20);player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="blood_sacrifice_health_gate")
    public static void strictFourHealthBoundaryCannotBeBypassedWithAbsorption(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,10000,1));int checked=0;
        try{for(var entry:ModItems.ITEMS.getEntries()){
            if(!(entry.get() instanceof VulnusiumWeapon))continue;checked++;
            for(int rank=1;rank<=4;rank++){
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.BLOOD_SACRAFICE,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                for(float health:new float[]{1,3.99F,4})for(float absorption:new float[]{0,8}){
                    player.setHealth(health);player.setAbsorptionAmount(absorption);player.setData(WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
                    AbilityTestPackets.use(player,0);
                    test.assertTrue(!BloodSacrificeEvents.active(player) && player.getHealth()==health && player.getAbsorptionAmount()==absorption && player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Low-health activation spends resources or bypasses strict four-HP gate");
                }
                player.setHealth(4.01F);player.setAbsorptionAmount(0);AbilityTestPackets.use(player,0);
                test.assertTrue(BloodSacrificeEvents.active(player) && Math.abs(player.getHealth()-2.005F)<.001,"Activation just above four HP is refused or spends wrong amount");
                BloodSacrificeEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));player.setData(WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
            }
        }test.assertTrue(checked==15,"Skipped sacrifice weapon variants");test.succeed();}
        finally{BloodSacrificeEvents.cancel(player.getUUID());WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.setHealth(20);player.server.getPlayerList().remove(player);}
    }

}
