package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GhostwalkGameTests {
    @GameTest(template="riftfang_test")
    public static void superOpeningDelayedOffhandHitAlsoDoublesDamage(GameTestHelper test) throws Exception {
        var network=net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork.class;
        var contextType=net.neoforged.neoforge.network.handling.IPayloadContext.class;
        var method=network.getDeclaredMethod("handleAttack",net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork.OffhandAttackPayload.class,contextType);method.setAccessible(true);
        for(var item:new net.minecraft.world.item.Item[]{ModItems.DEATHWISPER_DAGGER.get(),ModItems.DEATHWISPER_DAGGER_RUBY.get(),ModItems.DEATHWISPER_DAGGER_AMBER.get(),ModItems.DEATHWISPER_DAGGER_AMETHYST.get(),ModItems.DEATHWISPER_DAGGER_JADE.get()}) {
            var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());var stack=Augmentations.apply(new ItemStack(item),Augmentations.DUAL_WIELD);
            for(int rank=0;rank<4;rank++)stack=Augmentations.apply(stack,AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(item==ModItems.DEATHWISPER_DAGGER_RUBY.get()?ModItems.DEATHWISPER_DAGGER.get():ModItems.DEATHWISPER_DAGGER_RUBY.get()));
            for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);player.fallDistance=0;
            var target=net.minecraft.world.entity.EntityType.COW.create(test.getLevel());target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setNoAi(true);target.setNoGravity(true);target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
            try {
                player.attack(target);test.assertTrue(GhostwalkEvents.activate(player,stack),"Cannot vanish between paired swings");float health=target.getHealth(),damage=(float)player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                var context=(net.neoforged.neoforge.network.handling.IPayloadContext)java.lang.reflect.Proxy.newProxyInstance(contextType.getClassLoader(),new Class<?>[]{contextType},(proxy,m,args)->{if(m.getName().equals("player"))return player;throw new UnsupportedOperationException(m.getName());});
                method.invoke(null,new net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork.OffhandAttackPayload(target.getId()),context);
                test.assertTrue(Math.abs(target.getHealth()-(health-damage*2))<.01 && !GhostwalkEvents.active(player) && !player.hasEffect(MobEffects.INVISIBILITY),"Invisible offhand hit fails to double and reveal");
            }finally {GhostwalkEvents.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));target.discard();player.server.getPlayerList().remove(player);}
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fifteenWeaponsTransformAndDoubleExactlyOneRealInvisibleHit(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());int count=0;
        var target=net.minecraft.world.entity.EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
        try {for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof MorsiumWeapon) {
            count++;var stack=new ItemStack(entry.get());
            for(int rank=1;rank<=7;rank++) {
                stack=Augmentations.apply(stack,AdditionalAbilities.GHOSTWALK);test.assertFalse(stack.isEmpty(),"Super form cannot upgrade");player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);
                target.setHealth(1000);target.invulnerableTime=0;target.removeAllEffects();
                test.assertTrue(GhostwalkEvents.activate(player,stack),"Ghost form will not activate");
                test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==200+40*Math.max(0,rank-4)+(net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack)?20:0),"Super duration progression wrong");
                float damage=(float)player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);player.attack(target);
                test.assertTrue(Math.abs(target.getHealth()-(1000-damage*(rank>=4?2:1)))<.01,"Invisible hit multiplier wrong at rank "+rank);
                test.assertFalse(player.hasEffect(MobEffects.INVISIBILITY) || GhostwalkEvents.active(player),"Invisible attack does not reveal player");
                for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);target.invulnerableTime=0;float health=target.getHealth();player.attack(target);test.assertTrue(Math.abs(target.getHealth()-(health-damage))<.01,"Visible second hit retains double damage");
                if(rank>=4)test.assertTrue(Augmentations.displayRank(stack,AdditionalAbilities.GHOSTWALK,rank)==rank-3 && Augmentations.abilityName(stack,AdditionalAbilities.GHOSTWALK,rank).getString().equals("Jumpscare Horror"),"Super name/rank wrong");
            }
            test.assertTrue(Augmentations.apply(stack,AdditionalAbilities.GHOSTWALK).isEmpty(),"Super IV accepts fifth level");
        }}finally {GhostwalkEvents.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));target.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Morsium variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsEndOnAirSwingAndPreserveExternalInvisibility(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.GHOSTWALK))==eligible,"Wrong Ghostwalk pool");if(!eligible) continue;count++;
            player.removeEffect(MobEffects.INVISIBILITY);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.GHOSTWALK),"Fresh weapon vanishes");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GHOSTWALK) && player.getEffect(MobEffects.INVISIBILITY).getDuration()==200+(net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack)?20:0),"Ghostwalk duration wrong");GhostwalkEvents.swing(player);test.assertFalse(GhostwalkEvents.active(player) || player.hasEffect(MobEffects.INVISIBILITY),"Air swing keeps invisibility");
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,100,0));GhostwalkEvents.activate(player,stack);GhostwalkEvents.tick(new PlayerTickEvent.Pre(player));GhostwalkEvents.swing(player);test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==99,"Prior invisibility lost or timer paused");player.removeEffect(MobEffects.INVISIBILITY);
            GhostwalkEvents.activate(player,stack);player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,80,0));player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(GhostwalkEvents.active(player),"Switch retains Ghostwalk");test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==80,"Newly applied potion lost on switch");player.removeEffect(MobEffects.INVISIBILITY);
            var current=player.getMainHandItem();player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,70,0));GhostwalkEvents.activate(player,current);GhostwalkEvents.expired(new net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired(player,player.getEffect(MobEffects.INVISIBILITY)));player.removeEffect(MobEffects.INVISIBILITY);test.assertFalse(GhostwalkEvents.active(player),"Natural potion expiry retains state");test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==70,"Natural expiry loses saved potion");player.removeEffect(MobEffects.INVISIBILITY);
            GhostwalkEvents.activate(player,current);player.removeEffect(MobEffects.INVISIBILITY);test.assertFalse(GhostwalkEvents.active(player) || player.hasEffect(MobEffects.INVISIBILITY),"Dispel resurrects invisibility");
        }} finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=230)
    public static void naturalExpiryEndsStateAndRestoresLongerPotion(GameTestHelper test) {
        var player=TestPlayers.survival(test);var stack=Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,600,0));GhostwalkEvents.activate(player,stack);
        test.runAfterDelay(201,()->{try {test.assertFalse(GhostwalkEvents.active(player),"Ghostwalk survives ten seconds");test.assertTrue(player.hasEffect(MobEffects.INVISIBILITY),"Longer potion lost on expiry");} finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}test.succeed();});
    }
}
