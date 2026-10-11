package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ExpandedAbilitySlotsGameTests {
    @GameTest(template="riftfang_test")
    public static void all135WeaponsRespectThreeActivesAndWeaponSpecificPassiveLimits(GameTestHelper test) {
        int count=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());if(!Augmentations.eligible(fresh))continue;count++;
            for(boolean active:new boolean[]{true,false}) {
                var stack=fresh;var pool=Augmentations.pool(fresh).stream().filter(a->a.active()==active && !a.id().equals(Augmentations.MATERIAL_HIT)).toList();int expected=Math.min(pool.size(),Augmentations.limit(fresh,active));
                for(var ability:pool) {
                    var result=Augmentations.apply(stack,ability.id());
                    if(Augmentations.ids(stack,active).size()<expected){test.assertFalse(result.isEmpty(),"Legal new slot refused");stack=result;}
                    else test.assertTrue(result.isEmpty(),"Weapon exceeds slot limit");
                }
                test.assertTrue(Augmentations.ids(stack,active).size()==expected,"Slot count wrong");
                stack=Augmentations.apply(stack,Augmentations.MATERIAL_HIT);test.assertFalse(stack.isEmpty(),"Separate material slot refused");test.assertTrue(Augmentations.ids(stack,active).size()==expected,"Material consumes unique slot");
            }
        }
        test.assertTrue(count==135,"Skipped weapon variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void thirdSlotRunsAlongsideTwoOngoingAbilitiesAndKeepsIndependentCooldowns(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        for(int x=5;x<=11;x++)for(int y=5;y<=9;y++)for(int z=5;z<=11;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var stack=new ItemStack(ModItems.GHOST_AXE.get());for(var id:java.util.List.of(AdditionalAbilities.GHOSTWALK,AdditionalAbilities.BONE_GUARD,AdditionalAbilities.SKULL_THROW))stack=Augmentations.apply(stack,id);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            AbilityTestPackets.use(player,0);AbilityTestPackets.use(player,1);AbilityTestPackets.use(player,2);
            test.assertTrue(GhostwalkEvents.active(player) && BoneGuardEvents.active(player) && SkullThrowEvents.active(player),"Third slot cancels another ability or cannot activate");
            test.assertTrue(Augmentations.activeId(stack,WeaponAbilitySlot.TERTIARY).equals(AdditionalAbilities.SKULL_THROW),"Third slot mapping wrong");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            var cds=player.getData(WeaponCooldownAttachments.COOLDOWNS);long now=player.level().getGameTime();String item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            test.assertTrue(cds.get(item+":"+AdditionalAbilities.GHOSTWALK)==now+600 && cds.get(item+":"+AdditionalAbilities.BONE_GUARD)==now+1200 && cds.get(item+":"+AdditionalAbilities.SKULL_THROW)==now+1000,"Independent third-slot cooldown lost on switch");
            var swapped=Augmentations.apply(Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.GHOSTWALK),AdditionalAbilities.BONE_GUARD);Augmentations.swapActives(swapped);swapped=Augmentations.apply(swapped,AdditionalAbilities.SKULL_THROW);
            test.assertTrue(Augmentations.activeId(swapped,WeaponAbilitySlot.PRIMARY).equals(AdditionalAbilities.BONE_GUARD) && Augmentations.activeId(swapped,WeaponAbilitySlot.SECONDARY).equals(AdditionalAbilities.GHOSTWALK) && Augmentations.activeId(swapped,WeaponAbilitySlot.TERTIARY).equals(AdditionalAbilities.SKULL_THROW),"Learning third reverses old swapped order");
            test.assertFalse(Augmentations.swapActives(swapped),"Two-ability-only swap accepts three abilities");
        } finally {WeaponAbilityNetwork.onLogout(new PlayerEvent.PlayerLoggedOutEvent(player));GhostwalkEvents.cancel(player.getUUID());BoneGuardEvents.cancel(player.getUUID());SkullThrowEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
