package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.item.custom.attacks.axe.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AxeAnimationGameTests {
    private static ServerPlayer player(GameTestHelper test,ItemStack stack){
        var p=TestPlayers.survival(test);p.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));p.setNoGravity(true);p.setOnGround(true);p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        for(int x=5;x<=11;x++)for(int z=5;z<=12;z++){test.setBlock(new BlockPos(x,4,z),Blocks.STONE);for(int y=5;y<=10;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);}
        for(int i=0;i<60;i++)p.doTick();
        // Mock players can be outside active chunks; drive the actual Post handlers on the
        // real server clock rather than executing or advancing queued impacts directly.
        test.onEachTick(()->{
            AxeAnimationImpacts.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            AxeHeavyNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        });return p;
    }
    private static net.minecraft.world.entity.monster.Zombie target(GameTestHelper test,ServerPlayer p){var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));mob.setPos(p.position().add(0,0,2));mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(1000);test.getLevel().addFreshEntity(mob);return mob;}
    private static void close(ServerPlayer p){AxeAnimationImpacts.cancel(p.getUUID());AxeHeavyNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));p.server.getPlayerList().remove(p);}
    @GameTest(template="riftfang_test")
    public static void authoredCurvesDurationsAndAllAxeModelsArePresent(GameTestHelper test) {
        test.assertTrue(AxeAnimationData.length(AxeAnimationData.Clip.SWING)==1 && AxeAnimationData.length(AxeAnimationData.Clip.SWING2)==.9375 && AxeAnimationData.length(AxeAnimationData.Clip.GRAVITY_CAST)==6,"Authored clip lengths changed");
        var jump=AxeAnimationData.sample(AxeAnimationData.Clip.VERTICAL,"player_model",.83333);
        test.assertTrue(jump.position().y()==15 && jump.position().z()==-16,"Authored jump keyframe changed");
        test.assertTrue(AxeAnimationData.sample(AxeAnimationData.Clip.GRAVITY_CAST,"tornado_emitter",6).rotation().y()==2880,"Emitter full turns were normalized away");
        test.assertTrue(AxeAnimationData.Clip.VERTICAL.impactTicks()==25 && AxeAnimationData.Clip.GRAVITY_CAST.impactTicks()==33,"Impact deadlines do not match source times");
        int count=0;
        for(var entry:ModItems.ITEMS.getEntries())if(AxeAnimationItems.isModAxe(new ItemStack(entry.get()))){
            count++;var resource="/assets/forgermod/models/item/"+entry.getId().getPath()+".json";
            test.assertTrue(AxeAnimationGameTests.class.getResource(resource)!=null,"Missing axe model: "+entry.getId());
            if(entry.get() instanceof AxeItem)test.assertFalse(AxeItems.isAxe(new ItemStack(entry.get())),"Ordinary tool gained special-weapon behavior");
        }
        test.assertTrue(count>=53,"Skipped registered tool or special axes");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="axe_authored_ordinary_timing",timeoutTicks=100)
    public static void normalSwingsAlternateAndHitOnlyAtTheirAuthoredImpacts(GameTestHelper test){
        var item=ModItems.ITEMS.getEntries().stream().map(e->e.get()).filter(i->i instanceof AxeItem).findFirst().orElseThrow();var p=player(test,new ItemStack(item));var mob=target(test,p);float damage=(float)p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        p.attack(mob);test.assertTrue(mob.getHealth()==1000,"Normal hit lands before wind-up");
        test.assertTrue(p.getPersistentData().getInt(AxeAnimationNetwork.CLIP)==AxeAnimationData.Clip.SWING.ordinal(),"First attack uses wrong clip");
        test.runAfterDelay(12,()->test.assertTrue(mob.getHealth()==1000,"First swing hits before .625s"));
        test.runAfterDelay(14,()->test.assertTrue(Math.abs(mob.getHealth()-(1000-damage))<.01,"First swing impact missing or weak"));
        test.runAfterDelay(22,()->{
            mob.setHealth(1000);mob.invulnerableTime=0;mob.setPos(p.position().add(0,0,2));for(int i=0;i<60;i++)p.doTick();p.attack(mob);
            test.assertTrue(mob.getHealth()==1000 && p.getPersistentData().getInt(AxeAnimationNetwork.CLIP)==AxeAnimationData.Clip.SWING2.ordinal(),"Second attack does not alternate or damages immediately");
        });
        test.runAfterDelay(33,()->test.assertTrue(mob.getHealth()==1000,"Second swing hits before .58333s"));
        test.runAfterDelay(35,()->{try{test.assertTrue(Math.abs(mob.getHealth()-(1000-damage))<.01,"Second swing impact missing");test.succeed();}finally{mob.discard();close(p);}});
    }
    @GameTest(template="riftfang_test",batch="axe_authored_vertical_timing",timeoutTicks=60)
    public static void suppliedVerticalStrikeWaitsForJumpImpactWithoutNeedingPassive(GameTestHelper test){
        var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),AdditionalAbilities.GRAVITY_WELL);var p=player(test,stack);var mob=target(test,p);float damage=(float)p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        test.assertTrue(AxeHeavyNetwork.abilityVerticalStrike(p,stack,mob.position()),"Supplied strike rejected");test.assertTrue(mob.getHealth()==1000,"Supplied strike hits immediately");
        test.runAfterDelay(24,()->test.assertTrue(mob.getHealth()==1000,"Vertical Strike hits before1.25 seconds"));
        test.runAfterDelay(26,()->{try{test.assertTrue(Math.abs(mob.getHealth()-(1000-damage))<.01,"Jump impact missing or ability-only axe canceled the wind-up");test.succeed();}finally{mob.discard();close(p);}});
    }
    @GameTest(template="riftfang_test",batch="axe_authored_cast_timing",timeoutTicks=65)
    public static void gravitySwingCastsAtSlamFrameAndKeepsActivationCooldown(GameTestHelper test) throws Exception {
        var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),RecommendedAbilities.ENDER_SWING);var p=player(test,stack);var mob=target(test,p);mob.setPos(p.position().add(0,0,1));double y=mob.getY();long activation=p.level().getGameTime();
        AbilityTestPackets.use(p,0);test.assertTrue(EnderSwingAbility.active(p) && mob.getHealth()==1000 && mob.getY()==y,"Gravity cast is instant or not active during wind-up");
        String key="forgermod:axe_of_the_void:"+RecommendedAbilities.ENDER_SWING;
        test.assertTrue(p.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==activation+1800,"Casting changes activation cooldown start");
        test.runAfterDelay(32,()->test.assertTrue(mob.getHealth()==1000 && mob.getY()==y,"Gravity spell fires before authored slam"));
        test.runAfterDelay(34,()->{try{test.assertTrue(mob.getHealth()<1000 && mob.getY()>y+8 && !EnderSwingAbility.active(p),"Gravity slam effect missing");test.assertTrue(p.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==activation+1800,"Cast completion restarts cooldown");test.succeed();}finally{GravitySwingEffects.clear(mob);mob.discard();close(p);}});
    }
    @GameTest(template="riftfang_test",batch="axe_authored_cancel_timing",timeoutTicks=60)
    public static void switchingStacksCancelsDelayedHitsBeforeDeadline(GameTestHelper test){
        var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),Augmentations.VERTICAL_STRIKE);var p=player(test,stack);var mob=target(test,p);
        AxeHeavyNetwork.abilityVerticalStrike(p,stack,mob.position());p.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
        AxeHeavyNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));test.assertFalse(AxeHeavyNetwork.hasPending(p),"Switch waits until impact before canceling");
        test.runAfterDelay(27,()->{try{test.assertTrue(mob.getHealth()==1000,"Canceled wind-up deals delayed damage");test.succeed();}finally{mob.discard();close(p);}});
    }
    @GameTest(template="riftfang_test",batch="axe_authored_critical_timing",timeoutTicks=65)
    public static void ordinaryWindupPreservesChargedCriticalAndCannotHitOutOfReach(GameTestHelper test){
        var item=ModItems.ITEMS.getEntries().stream().map(e->e.get()).filter(i->i instanceof AxeItem).findFirst().orElseThrow();
        var p=player(test,new ItemStack(item));var mob=target(test,p);float damage=(float)p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        p.setOnGround(false);p.fallDistance=1;p.setSprinting(false);p.attack(mob);
        // Landing during the wind-up must not erase the attack's captured critical/charge.
        p.fallDistance=0;p.setOnGround(true);
        test.runAfterDelay(14,()->test.assertTrue(Math.abs(mob.getHealth()-(1000-damage*1.5))<.01,"Delayed attack loses charged critical damage"));
        test.runAfterDelay(22,()->{
            mob.setHealth(1000);mob.invulnerableTime=0;for(int i=0;i<60;i++)p.doTick();p.attack(mob);
            mob.setPos(p.position().add(0,0,10));
        });
        test.runAfterDelay(36,()->{try{test.assertTrue(mob.getHealth()==1000,"Axe hits a target that left melee reach during the wind-up");test.succeed();}finally{mob.discard();close(p);}});
    }

}
