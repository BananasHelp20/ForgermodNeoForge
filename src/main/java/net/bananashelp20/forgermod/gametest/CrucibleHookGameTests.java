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
