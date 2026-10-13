package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeHeavyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FlamingStompGameTests {
    private static Zombie zombie(GameTestHelper test, BlockPos pos) {
        Zombie zombie = EntityType.ZOMBIE.create(test.getLevel());
        zombie.setNoAi(true);
        zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        zombie.setHealth(100);
        zombie.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        test.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    @GameTest(template="riftfang_test")
    public static void nextAttackForcesFiveByFiveSlamAndStartsIndependentCooldown(GameTestHelper test) throws Exception {
        ServerPlayer player = TestPlayers.survival(test);
        player.setPos(test.getBounds().getCenter());
        player.setYRot(0);
        player.setNoGravity(true);
        BlockPos feet = player.blockPosition();
        for (int x=-5;x<=5;x++) for (int z=-5;z<=5;z++) {
            test.getLevel().setBlockAndUpdate(feet.offset(x,-1,z), Blocks.STONE.defaultBlockState());
            for (int y=0;y<=3;y++) test.getLevel().setBlockAndUpdate(feet.offset(x,y,z), Blocks.AIR.defaultBlockState());
        }
        ItemStack fresh = new ItemStack(ModItems.MOLTEN_AXE.get());
        test.assertFalse(Augmentations.ids(fresh,true).contains(RecommendedAbilities.FLAMING_STOMP),"Fresh axe owns Flaming Stomp");
        ItemStack stack = Augmentations.apply(fresh, RecommendedAbilities.FLAMING_STOMP);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        player.doTick();
        AbilityTestPackets.use(player,0);
        test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.FlamingStompAbility.active(player),"Stomp did not arm");

        var method=AxeHeavyNetwork.class.getDeclaredMethod("queueAttack",ServerPlayer.class);
        method.setAccessible(true);
        test.assertTrue((Boolean)method.invoke(null,player),"Attack did not schedule the forced slam");
        test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.FlamingStompAbility.active(player),"Attack did not consume stomp");
        var field=AxeHeavyNetwork.class.getDeclaredField("PENDING"); field.setAccessible(true);
        @SuppressWarnings("unchecked") Map<UUID,Object> pending=(Map<UUID,Object>)field.get(null);
        Object slam=pending.get(player.getUUID());
        test.assertTrue(slam!=null,"Attack without Vertical Strike did not schedule a slam");
        var centerMethod=slam.getClass().getDeclaredMethod("center"); centerMethod.setAccessible(true);
        BlockPos center=(BlockPos)centerMethod.invoke(slam);
        var edge=zombie(test,center.offset(2,0,2));
        var outside=zombie(test,center.offset(3,0,0));
        var land=AxeHeavyNetwork.class.getDeclaredMethod("landSlam",ServerPlayer.class,slam.getClass()); land.setAccessible(true);
        land.invoke(null,player,slam); pending.remove(player.getUUID());
        test.assertTrue(edge.getRemainingFireTicks()>0 && edge.getHealth()<100,"Five-by-five enemy was not burned and hit");
        test.assertTrue(outside.getRemainingFireTicks()<=0 && outside.getHealth()==100,"Stomp escaped its five-by-five area");
        net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.FLAMING_STOMP;
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+2400,
                "Two-minute cooldown did not start after the strike");
        edge.discard();outside.discard();test.getLevel().getServer().getPlayerList().remove(player);test.succeed();
    }
}
