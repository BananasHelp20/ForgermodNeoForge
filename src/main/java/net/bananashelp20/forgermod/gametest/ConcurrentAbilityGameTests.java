package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ConcurrentAbilityGameTests {
    private static String key(ItemStack stack,String id){return BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+id;}
    private static List<Mob> guards(ServerPlayer player){return player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(16),
            mob->mob instanceof BoneGuardEvents.FriendlyGuard guard && guard.owner()==player && mob.isAlive());}
    @GameTest(template="riftfang_test",batch="concurrent_summons_and_weapons")
    public static void allSummonVariantsAllowAnotherSlotThenMagmaTossOnAnotherWeaponWithIndependentCooldowns(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(BlockPos.containing(test.getBounds().getCenter())));
        player.setXRot(-90);player.setYRot(0);
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,-1,z),Blocks.STONE.defaultBlockState());
            for(int y=0;y<=6;y++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        }
        List<Item> molten=List.of(ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),ModItems.MOLTEN_AXE_AMBER.get(),ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get());
        int checked=0;
        try{
            for(var entry:ModItems.ITEMS.getEntries()){
                if(!(entry.get() instanceof MorsiumWeapon weapon) || weapon.isDagger())continue;
                for(int rank:new int[]{1,4,7}){
                    int iteration=checked++;
                    var summon=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.BONE_GUARD,rank);
                    summon=Augmentations.setLevel(summon,AdditionalAbilities.GHOSTWALK,1);
                    if(rank>=4){var loadout=NecromancerLoadouts.get(player,summon);loadout.types.clear();loadout.types.add(0);loadout.publish(summon);}
                    player.getInventory().selected=0;player.setItemInHand(InteractionHand.MAIN_HAND,summon);player.doTick();
                    player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                    AbilityTestPackets.use(player,0);
                    var army=guards(player);army.forEach(mob->mob.setNoAi(true));
                    test.assertTrue(BoneGuardEvents.active(player) && !army.isEmpty(),"Summon command packet failed at rank "+rank);
                    var ids=army.stream().map(Mob::getUUID).toList();
                    long summonReady=player.level().getGameTime()+Augmentations.cooldown(summon,WeaponAbilitySlot.PRIMARY,weapon);
                    test.assertTrue(Objects.equals(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key(summon,AdditionalAbilities.BONE_GUARD)),summonReady),"Summon activation cooldown missing");
                    AbilityTestPackets.use(player,1);
                    test.assertTrue(GhostwalkEvents.active(player) && BoneGuardEvents.active(player),"Second slot cannot activate while summons are active");
                    var axe=Augmentations.apply(new ItemStack(molten.get(iteration%5)),AdditionalAbilities.MAGMA_TOSS);
                    axe=Augmentations.apply(axe,AdditionalAbilities.FLAME_BREATH);
                    player.getInventory().selected=1;player.setItemInHand(InteractionHand.MAIN_HAND,axe);player.doTick();
                    // Go through the packet path immediately, without requiring a prior session-update tick.
                    AbilityTestPackets.use(player,0);
                    test.assertTrue(MagmaTossEvents.active(player) && BoneGuardEvents.active(player),"Persistent summons block a new weapon's primary cast");
                    test.assertFalse(GhostwalkEvents.active(player),"Switch no longer cancels a weapon-bound invisibility ability");
                    var rock=player.level().getEntitiesOfClass(Snowball.class,player.getBoundingBox().inflate(5),entity->entity.getOwner()==player && entity.isAlive()).getFirst();
                    AbilityTestPackets.use(player,1);
                    test.assertTrue(MagmaTossEvents.active(player) && rock.isAlive() && BoneGuardEvents.active(player),"Second axe ability cancels the projectile/summons");
                    var cooldowns=player.getData(WeaponCooldownAttachments.COOLDOWNS);
                    test.assertTrue(cooldowns.containsKey(key(axe,AdditionalAbilities.FLAME_BREATH)) && !cooldowns.containsKey(key(axe,AdditionalAbilities.MAGMA_TOSS))
                            && Objects.equals(cooldowns.get(key(summon,AdditionalAbilities.BONE_GUARD)),summonReady),"Ability cooldowns share a global slot or restart each other");
                    if(iteration%2==0){
                        var point=player.position().add(0,5,0);
                        MagmaTossEvents.impact(new ProjectileImpactEvent(rock,new BlockHitResult(point,Direction.UP,BlockPos.containing(point),false)));
                        WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
                        test.assertFalse(MagmaTossEvents.active(player),"Magma impact did not finish");
                    }
                    player.getInventory().selected=0;player.setItemInHand(InteractionHand.MAIN_HAND,summon);
                    WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
                    test.assertTrue(!MagmaTossEvents.active(player) && !rock.isAlive() && BoneGuardEvents.active(player),"Switch leaves weapon projectile or cancels independent summons");
                    AbilityTestPackets.use(player,0);
                    test.assertTrue(guards(player).stream().map(Mob::getUUID).toList().equals(ids),"Cooldown was bypassed and replaced the active army");
                    test.assertTrue(Objects.equals(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key(summon,AdditionalAbilities.BONE_GUARD)),summonReady),"Returning to summon weapon restarts its cooldown");
                    BoneGuardEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
                }
            }
            test.assertTrue(checked==30,"Missing Bone Guard/Necromancer ranks or variants");test.succeed();
        }finally{
            BoneGuardEvents.cancel(player.getUUID());MagmaTossEvents.cancel(player.getUUID());GhostwalkEvents.cancel(player.getUUID());
            WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            player.server.getPlayerList().remove(player);
        }
    }
}
