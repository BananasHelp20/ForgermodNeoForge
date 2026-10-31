package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class DealtDamageGameTests {
    private static void ready(net.minecraft.server.level.ServerPlayer player){
        for(int i=0;i<60;i++)player.doTick();player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);
    }
    @GameTest(template="riftfang_test",batch="combined_hit_leech")
    public static void leechUsesEnchantmentGemstoneAndAbilityBonusesAfterMitigation(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        var target=EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.ARMOR).setBaseValue(6);target.setHealth(1000);test.getLevel().addFreshEntity(target);
        float[] combined={0};java.util.function.Consumer<LivingIncomingDamageEvent> observer=e->{if(e.getEntity()==target && e.getSource().getEntity()==player && PassiveActors.melee(e.getSource()))combined[0]=e.getAmount();};
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,observer);
        try{
            for(var item:new Item[]{ModItems.ASSASSIN_DAGGER.get(),ModItems.ASSASSIN_DAGGER_RUBY.get(),ModItems.ASSASSIN_DAGGER_AMBER.get(),ModItems.ASSASSIN_DAGGER_AMETHYST.get(),ModItems.ASSASSIN_DAGGER_JADE.get()})for(int rank=1;rank<=4;rank++){
                BloodSacrificeEvents.cancel(player.getUUID());
                var stack=Augmentations.setLevel(new ItemStack(item),"tooltips.forgermod.ability.leech",rank);stack=Augmentations.apply(stack,AdditionalAbilities.BLOOD_SACRAFICE);
                stack.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS),5);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);ready(player);player.setHealth(100);
                test.assertTrue(BloodSacrificeEvents.activate(player,stack),"Damage-bonus ability refused");player.setHealth(5);
                target.setPos(player.position().add(0,0,2));target.setHealth(1000);target.invulnerableTime=0;
                float full=(float)(2*(player.getAttributeValue(Attributes.ATTACK_DAMAGE)+3));player.attack(target);
                float dealt=1000-target.getHealth();
                test.assertTrue(Math.abs(combined[0]-full)<.01,"Enchantment/gemstone/ability contributions missing from combined hit");
                test.assertTrue(dealt>0 && dealt<combined[0] && Math.abs(player.getHealth()-(5+dealt*(.1F+.02F*(rank-1))))<.02,"Leech mismatch item="+item+" rank="+rank+" incoming="+combined[0]+" dealt="+dealt+" health="+player.getHealth()+" expected="+(5+dealt*(.1F+.02F*(rank-1))));
                float health=player.getHealth();target.invulnerableTime=0;target.hurt(player.damageSources().indirectMagic(player,player),10);
                test.assertTrue(player.getHealth()==health,"Separate magic ability damage joins the triggering hit");
                target.invulnerableTime=0;target.hurt(player.damageSources().onFire(),1);test.assertTrue(player.getHealth()==health,"Separate fire tick joins the triggering hit");
            }
        }finally{NeoForge.EVENT_BUS.unregister(observer);BloodSacrificeEvents.cancel(player.getUUID());target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="combined_hit_storing")
    public static void storingAngerBanksLateModifiersAndReleasesTheCombinedBankOnlyOnce(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var target=EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.setHealth(1000);target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
        var stack=Augmentations.apply(new ItemStack(ModItems.DEATHWISPER_DAGGER_RUBY.get()),"tooltips.forgermod.ability.storing_anger");
        stack.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS),5);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var field=MorsiumWeapon.class.getDeclaredField("ANGER");field.setAccessible(true);var anger=(StoringAngerState)field.get(null);
        float[] combined={0};java.util.function.Consumer<LivingIncomingDamageEvent> late=e->{if(e.getEntity()==target && e.getSource().getEntity()==player && PassiveActors.melee(e.getSource())){e.setAmount((e.getAmount()+7)*1.5F);combined[0]=e.getAmount();}};
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW,late);
        try{
            ready(player);long now=player.level().getGameTime();anger.arm(player.getUUID(),now);player.attack(target);
            test.assertTrue(target.getHealth()==1000 && combined[0]>player.getAttributeValue(Attributes.ATTACK_DAMAGE),"Charging hit dealt damage or late bonuses were absent");
            target.invulnerableTime=0;target.hurt(player.damageSources().indirectMagic(player,player),10);
            test.assertTrue(target.getHealth()==990,"Charging absorbs a separate spell instead of only melee hit damage");
            float bank=anger.releaseOnDaggerHit(player.getUUID(),now+200);
            test.assertTrue(Math.abs(bank-combined[0])<.01,"Stored damage misses late hit modifiers or includes separate spell");
            // Seed the same already-verified bank at an elapsed deadline; only the state clock
            // is arranged here, while both release hits run through real Player.attack/events.
            anger.arm(player.getUUID(),now-200);anger.storeIfCharging(player.getUUID(),now-199,bank);
            ready(player);target.setHealth(1000);target.invulnerableTime=0;target.setPos(player.position().add(0,0,2));player.attack(target);
            test.assertTrue(Math.abs(target.getHealth()-(1000-combined[0]-bank))<.01,"Release omits combined damage or multiplies the stored bank twice");
            ready(player);target.setHealth(1000);target.invulnerableTime=0;target.setPos(player.position().add(0,0,2));player.attack(target);
            test.assertTrue(Math.abs(target.getHealth()-(1000-combined[0]))<.01,"Stored hit damage releases repeatedly");
        }finally{NeoForge.EVENT_BUS.unregister(late);anger.clear(player.getUUID());target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
