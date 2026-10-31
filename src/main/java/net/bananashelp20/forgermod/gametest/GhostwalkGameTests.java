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
    public static void rejectedAttacksKeepGhostwalkAndAbsorbedHitsRevealBothForms(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var target=net.minecraft.world.entity.EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION).setBaseValue(100);
        target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
        try {
            for(int rank:new int[]{1,4}) {
                var stack=Augmentations.setLevel(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.GHOSTWALK,rank);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();
                player.setOnGround(true);target.invulnerableTime=0;target.setInvulnerable(true);
                test.assertTrue(GhostwalkEvents.activate(player,stack),"Cannot activate form");
                player.attack(target);AxeAnimationTestDriver.finish(player);
                test.assertTrue(GhostwalkEvents.active(player) && GhostwalkVisibility.hidden(player),"Rejected hit reveals player");
                target.setInvulnerable(false);target.setAbsorptionAmount(100);
                for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);player.attack(target);AxeAnimationTestDriver.finish(player);
                test.assertTrue(target.getHealth()==10 && target.getAbsorptionAmount()<100,"Absorbed-hit fixture failed");
                test.assertFalse(GhostwalkEvents.active(player) || GhostwalkVisibility.hidden(player),"A successful absorbed hit does not reveal player");
            }
        } finally {GhostwalkEvents.cancel(player.getUUID());target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void equipmentVisibilityIsSynchronizedAndRestoredWithoutChangingGear(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        var stack=Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.GHOSTWALK);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(net.minecraft.world.item.Items.SHIELD));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,new ItemStack(net.minecraft.world.item.Items.DIAMOND_LEGGINGS));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,new ItemStack(net.minecraft.world.item.Items.DIAMOND_BOOTS));
        var gear=new java.util.EnumMap<net.minecraft.world.entity.EquipmentSlot,ItemStack>(net.minecraft.world.entity.EquipmentSlot.class);
        for(var slot:net.minecraft.world.entity.EquipmentSlot.values())gear.put(slot,player.getItemBySlot(slot).copy());
        try {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,600,0));
            test.assertFalse(GhostwalkVisibility.hidden(player),"Ordinary invisibility hides equipment");
            test.assertTrue(GhostwalkEvents.activate(player,stack) && GhostwalkVisibility.hidden(player),"Ghostwalk does not hide equipment");
            var packet=GhostwalkVisibility.snapshot(player);
            test.assertTrue(packet.hidden() && packet.entityId()==player.getId() && packet.playerId().equals(player.getUUID()),"Late-tracker snapshot lacks current owner/state");
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
            try {
                GhostwalkVisibility.Payload.CODEC.encode(buffer,packet);
                test.assertTrue(GhostwalkVisibility.Payload.CODEC.decode(buffer).equals(packet),"Visibility packet loses identity/state");
            } finally {buffer.release();}
            GhostwalkVisibility.apply(test.getLevel(),new GhostwalkVisibility.Payload(player.getId(),java.util.UUID.randomUUID(),false));
            test.assertTrue(GhostwalkVisibility.hidden(player),"Stale entity id changes a different player's visibility");
            GhostwalkEvents.cancel(player.getUUID());
            test.assertFalse(GhostwalkVisibility.hidden(player) || GhostwalkVisibility.snapshot(player).hidden(),"Attack does not restore equipment visibility");
            test.assertTrue(player.hasEffect(MobEffects.INVISIBILITY),"Restoring equipment loses prior invisibility");
            for(var slot:gear.keySet())test.assertTrue(ItemStack.matches(gear.get(slot),player.getItemBySlot(slot)),"Hiding equipment changes inventory/attributes");
            // The client packet path restores as well as hides; rendering reads only this transient state.
            GhostwalkVisibility.apply(test.getLevel(),packet);
            test.assertTrue(GhostwalkVisibility.hidden(player),"Visibility update does not apply");
            GhostwalkVisibility.apply(test.getLevel(),new GhostwalkVisibility.Payload(player.getId(),player.getUUID(),false));
            test.assertFalse(GhostwalkVisibility.hidden(player),"Reveal update does not apply");
            GhostwalkEvents.activate(player,stack);
            GhostwalkEvents.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            test.assertFalse(GhostwalkVisibility.hidden(player),"Logout leaves hidden equipment state");
        } finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.succeed();
    }
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
                player.attack(target);AxeAnimationTestDriver.finish(player);test.assertTrue(GhostwalkEvents.activate(player,stack),"Cannot vanish between paired swings");float health=target.getHealth(),damage=(float)player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                var context=(net.neoforged.neoforge.network.handling.IPayloadContext)java.lang.reflect.Proxy.newProxyInstance(contextType.getClassLoader(),new Class<?>[]{contextType},(proxy,m,args)->{if(m.getName().equals("player"))return player;throw new UnsupportedOperationException(m.getName());});
                for(var recipient:player.server.getPlayerList().getPlayers())
                    net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateCommonChannels(recipient.connection.getConnection(),net.minecraft.network.ConnectionProtocol.PLAY)
                            .add(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("forgermod","offhand_swing"));
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
                test.assertTrue(GhostwalkVisibility.hidden(player),"Ghost form leaves armor/held items visible");
                test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==200+40*Math.max(0,rank-4)+(net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack)?20:0),"Super duration progression wrong");
                float damage=(float)player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);player.attack(target);AxeAnimationTestDriver.finish(player);
                test.assertTrue(Math.abs(target.getHealth()-(1000-damage*(rank>=4?2:1)))<.01,"Invisible hit multiplier wrong at rank "+rank);
                test.assertFalse(player.hasEffect(MobEffects.INVISIBILITY) || GhostwalkEvents.active(player) || GhostwalkVisibility.hidden(player),"Invisible attack does not reveal player and equipment");
                for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);target.invulnerableTime=0;float health=target.getHealth();player.attack(target);AxeAnimationTestDriver.finish(player);test.assertTrue(Math.abs(target.getHealth()-(health-damage))<.01,"Visible second hit retains double damage");
                if(rank>=4)test.assertTrue(Augmentations.displayRank(stack,AdditionalAbilities.GHOSTWALK,rank)==rank-3 && Augmentations.abilityName(stack,AdditionalAbilities.GHOSTWALK,rank).getString().equals("Jumpscare Horror"),"Super name/rank wrong");
            }
            test.assertTrue(Augmentations.apply(stack,AdditionalAbilities.GHOSTWALK).isEmpty(),"Super IV accepts fifth level");
        }}finally {GhostwalkEvents.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));target.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Morsium variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsKeepInvisibilityOnAirSwingAndPreserveExternalInvisibility(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.GHOSTWALK))==eligible,"Wrong Ghostwalk pool");if(!eligible) continue;count++;
            player.removeEffect(MobEffects.INVISIBILITY);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.GHOSTWALK),"Fresh weapon vanishes");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GHOSTWALK) && player.getEffect(MobEffects.INVISIBILITY).getDuration()==200+(net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack)?20:0),"Ghostwalk duration wrong");GhostwalkEvents.swing(player);test.assertTrue(GhostwalkEvents.active(player) && player.hasEffect(MobEffects.INVISIBILITY),"Air swing reveals player");GhostwalkEvents.cancel(player.getUUID());
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,100,0));GhostwalkEvents.activate(player,stack);GhostwalkEvents.tick(new PlayerTickEvent.Pre(player));GhostwalkEvents.swing(player);test.assertTrue(GhostwalkEvents.active(player),"Air swing ends session with prior potion");GhostwalkEvents.cancel(player.getUUID());test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==99,"Prior invisibility lost or timer paused");player.removeEffect(MobEffects.INVISIBILITY);
            GhostwalkEvents.activate(player,stack);player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,80,0));player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(GhostwalkEvents.active(player) || GhostwalkVisibility.hidden(player),"Switch retains Ghostwalk or hidden equipment");test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==80,"Newly applied potion lost on switch");player.removeEffect(MobEffects.INVISIBILITY);
            var current=player.getMainHandItem();player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,70,0));GhostwalkEvents.activate(player,current);GhostwalkEvents.expired(new net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired(player,player.getEffect(MobEffects.INVISIBILITY)));player.removeEffect(MobEffects.INVISIBILITY);test.assertFalse(GhostwalkEvents.active(player) || GhostwalkVisibility.hidden(player),"Natural potion expiry retains hidden equipment");test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==70,"Natural expiry loses saved potion");player.removeEffect(MobEffects.INVISIBILITY);
            GhostwalkEvents.activate(player,current);player.removeEffect(MobEffects.INVISIBILITY);test.assertFalse(GhostwalkEvents.active(player) || player.hasEffect(MobEffects.INVISIBILITY) || GhostwalkVisibility.hidden(player),"Dispel resurrects invisibility or leaves equipment hidden");
        }} finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=230)
    public static void naturalExpiryEndsStateAndRestoresLongerPotion(GameTestHelper test) {
        var player=TestPlayers.survival(test);var stack=Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,600,0));GhostwalkEvents.activate(player,stack);
        test.runAfterDelay(201,()->{try {test.assertFalse(GhostwalkEvents.active(player) || GhostwalkVisibility.hidden(player),"Ghostwalk or hidden equipment survives ten seconds");test.assertTrue(player.hasEffect(MobEffects.INVISIBILITY),"Longer potion lost on expiry");} finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}test.succeed();});
    }
}
