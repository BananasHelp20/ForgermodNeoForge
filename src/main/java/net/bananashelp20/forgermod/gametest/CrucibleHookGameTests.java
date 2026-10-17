package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CrucibleHookGameTests {
    @GameTest(template="riftfang_test")
    public static void passiveTeamAlliesAndOwnedPetsReceiveTheWholeHookAndSmash(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setYRot(0);
        for(int x=-2;x<=2;x++)for(int y=0;y<=4;y++)for(int z=0;z<=12;z++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var team=player.getScoreboard().addPlayerTeam("hook_"+player.getUUID().toString().substring(0,8));player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);
        var stand=EntityType.ARMOR_STAND.create(test.getLevel());stand.setPos(player.position().add(0,0,4));stand.setNoGravity(true);test.getLevel().addFreshEntity(stand);
        player.getScoreboard().addPlayerToTeam(cow.getScoreboardName(),team);
        cow.setPos(player.position().add(8,0,0));wolf.setPos(player.position().add(8,0,2));test.getLevel().addFreshEntity(cow);test.getLevel().addFreshEntity(wolf);
        try {
            test.assertTrue(cow.isAlliedTo(player) && wolf.isAlliedTo(player),"Hook fixtures are not allies");
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof net.bananashelp20.forgermod.item.custom.IgnisiumWeapon weapon) || !weapon.isAxe())continue;
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),RecommendedAbilities.CRUCIBLE_HOOK,4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                for(var mob:java.util.List.of(cow,wolf)) {
                    mob.goalSelector.removeAllGoals(goal->true);mob.targetSelector.removeAllGoals(goal->true);mob.setNoGravity(true);
                    mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(200);mob.invulnerableTime=0;mob.setRemainingFireTicks(0);mob.setDeltaMovement(0,0,0);mob.setPos(player.position().add(0,0,10));
                    double drop=player.getEyeY()-mob.getBoundingBox().getCenter().y;player.setXRot((float)Math.toDegrees(Math.atan2(drop,10)));
                    var before=mob.position();int wear=stack.getDamageValue();
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CRUCIBLE_HOOK),"Cannot acquire passive/allied mob");
                    test.assertTrue(CrucibleHookEvents.active(player) && mob.position().equals(before) && mob.getDeltaMovement().z<0 && mob.getHealth()==200,"Allied hook cancels, teleports or damages during pull");
                    for(int tick=0;tick<30 && CrucibleHookEvents.active(player);tick++){mob.tick();CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));}
                    test.assertTrue(!CrucibleHookEvents.active(player) && Math.abs(mob.getHealth()-(200-player.getAttributeValue(Attributes.ATTACK_DAMAGE)*2))<.001
                        && mob.getRemainingFireTicks()==120 && stack.getDamageValue()==wear+1,"Allied mob does not receive normal smash damage, ignition and weapon wear");
                    mob.setPos(player.position().add(8,0,0));
                }
            }
            test.assertTrue(stand.getHealth()==stand.getMaxHealth() && stand.getDeltaMovement().lengthSqr()==0,"Nonmob armor stand is hooked or damaged");
            test.succeed();
        } finally {CrucibleHookEvents.cancel(player.getUUID());player.getScoreboard().removePlayerTeam(team);cow.discard();wolf.discard();stand.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsPullWithoutTeleportThenSmashForDoubleDamage(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),ModItems.MOLTEN_AXE_AMBER.get(),
                ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
            ItemStack stack=new ItemStack(item);
            for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.CRUCIBLE_HOOK);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick();
            var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoGravity(true);
            // Disable decisions while retaining ordinary mob travel/velocity physics.
            zombie.goalSelector.removeAllGoals(goal->true); zombie.targetSelector.removeAllGoals(goal->true);
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); zombie.setHealth(200); zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
            zombie.setPos(player.position().add(0,0,10)); test.getLevel().addFreshEntity(zombie);
            var before=zombie.position();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CRUCIBLE_HOOK),"Hook cannot acquire aimed hostile");
            test.assertTrue(zombie.position().equals(before) && zombie.getDeltaMovement().z<0,"Hook teleports instead of applying pull velocity");
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*2;
            for(int tick=0;tick<30 && CrucibleHookEvents.active(player);tick++) {
                zombie.tick(); CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            }
            test.assertFalse(CrucibleHookEvents.active(player),"Hook never reaches/strikes pulled target");
            test.assertTrue(Math.abs(zombie.getHealth()-(200-damage))<.001,"Rank IV smash does not deal double weapon damage: health="
                    +zombie.getHealth()+" expected="+(200-damage)+" armor="+zombie.getArmorValue());
            test.assertTrue(zombie.getRemainingFireTicks()==120 && stack.getDamageValue()==1,"Smash does not ignite target or wear weapon");
            zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void obstructionAndSwitchPreventHitAndStartCancellationCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.CRUCIBLE_HOOK);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true); zombie.setPos(player.position().add(0,0,10));
        test.getLevel().addFreshEntity(zombie); var wall=player.blockPosition().south(2).above();
        test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
        AbilityTestPackets.use(player,0); test.assertFalse(CrucibleHookEvents.active(player),"Hook targets an enemy through a wall");
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.CRUCIBLE_HOOK;
        test.assertFalse(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Failed hook starts cooldown");
        test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState()); AbilityTestPackets.use(player,0);
        test.assertTrue(CrucibleHookEvents.active(player),"Unobstructed packet activation failed");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
        test.assertFalse(CrucibleHookEvents.active(player),"Switch leaves hook running");
        test.assertTrue(zombie.getHealth()==20,"Canceled hook still smashes target");
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==player.level().getGameTime()+800,"Canceled hook lacks full cooldown");
        zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
