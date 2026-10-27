package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.DeathsDueEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class DeathsDueGameTests {
    @GameTest(template="riftfang_test")
    public static void currentDescriptionsUpgradesAndCapsAgreeAtBothRanks(GameTestHelper test) {
        int count=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof MorsiumWeapon))continue;
            count++;var stack=new ItemStack(entry.get());
            for(int rank=1;rank<=2;rank++) {
                stack=Augmentations.apply(stack,AdditionalAbilities.DEATHS_DUE);test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,AdditionalAbilities.DEATHS_DUE)==rank,"Cannot reach requested XP rank");
                for(boolean compact:new boolean[]{false,true}) {
                    var text=AbilityDescriptions.description(stack,AdditionalAbilities.DEATHS_DUE,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    String percent=25*rank+"%";test.assertTrue(text.getString().equals("Enemy kills grant "+percent+" more xp.") && white.toString().equals(percent),"Wrong XP text/highlights");
                }
                var changes=AugmentUpgradeDescriptions.changes(stack,AdditionalAbilities.DEATHS_DUE,rank);
                test.assertTrue(rank==2?changes.isEmpty():changes.stream().anyMatch(c->c.property().equals("experience_bonus") && ((Number)c.after()).intValue()==50),"Wrong XP upgrade preview");
            }
            test.assertTrue(Augmentations.apply(stack,AdditionalAbilities.DEATHS_DUE).isEmpty(),"XP augment exceeds II");
        }
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get(),ModItems.HOLLOW_CLAYMORE.get())) {
            var blank=new ItemStack(item);test.assertTrue(Augmentations.maxRank(blank,AdditionalAbilities.DEATHS_DUE)==2,"Incorrect template/command cap");
            test.assertTrue(!Augmentations.setLevel(blank,AdditionalAbilities.DEATHS_DUE,2).isEmpty() && Augmentations.setLevel(blank,AdditionalAbilities.DEATHS_DUE,3).isEmpty(),"Command ranks exceed requested range");
        }
        test.assertTrue(count==15,"Skipped XP weapon variant");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="deaths_due_guard_rank")
    public static void reinforcementUsesLevelTwoBeforeApplyingItsXpPenalty(GameTestHelper test) {
        var owner=TestPlayers.survival(test);owner.setPos(test.getBounds().getCenter());
        var summon=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);owner.setItemInHand(InteractionHand.MAIN_HAND,summon);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        try {
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.BoneGuardEvents.activate(owner,summon),"Guard fixture cannot summon");
            var guard=owner.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,owner.getBoundingBox().inflate(6),m->m instanceof net.bananashelp20.forgermod.item.custom.abilities.BoneGuardEvents.FriendlyGuard g && g.owner()==owner).getFirst();
            guard.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,new ItemStack(ModItems.DEATHWISPER_DAGGER.get()));
            var off=Augmentations.setLevel(new ItemStack(ModItems.DEATHWISPER_DAGGER_RUBY.get()),AdditionalAbilities.DEATHS_DUE,2);off=Augmentations.apply(off,Augmentations.DUAL_WIELD);
            guard.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,off);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(victim,victim.damageSources().mobAttack(guard)));
            var event=new LivingExperienceDropEvent(victim,owner,8);DeathsDueEvents.experience(event);
            test.assertTrue(event.getDroppedExperience()==6,"RankII bonus missing, uses owner rank, or applies after reinforcement penalty");
        } finally {net.bananashelp20.forgermod.item.custom.abilities.BoneGuardEvents.cancel(owner.getUUID());owner.server.getPlayerList().remove(owner);}
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void allFifteenVariantsBoostOnlyEnemyExperience(GameTestHelper test) {
        var player=TestPlayers.survival(test);var zombie=EntityType.ZOMBIE.create(test.getLevel());var cow=EntityType.COW.create(test.getLevel());int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DEATHS_DUE))==eligible,"Wrong XP pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);var event=new LivingExperienceDropEvent(zombie,player,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Fresh weapon grants XP");
            for(int rank=1;rank<=2;rank++) {
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.setLevel(fresh,AdditionalAbilities.DEATHS_DUE,rank));
            event=new LivingExperienceDropEvent(zombie,player,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20+5*rank,"Wrong XP multiplier");
            event=new LivingExperienceDropEvent(zombie,player,5);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==5+rank,"Wrong whole-point rounding");
            event=new LivingExperienceDropEvent(cow,player,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Neutral kill boosted");
            event=new LivingExperienceDropEvent(zombie,player,20);event.setCanceled(true);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Canceled XP changed");
            event=new LivingExperienceDropEvent(zombie,null,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Uncredited XP boosted");
            event=new LivingExperienceDropEvent(zombie,player,Integer.MAX_VALUE);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==Integer.MAX_VALUE,"XP overflow");
            }
        }}finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void realKillDropsSixExperienceInsteadOfFive(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=net.minecraft.world.phys.Vec3.atBottomCenterOf(test.absolutePos(new net.minecraft.core.BlockPos(8,5,8)));player.setPos(point);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPos(point.add(0,0,2));test.getLevel().addFreshEntity(zombie);
        try {player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.DEATHS_DUE));
            zombie.hurt(player.damageSources().playerAttack(player),100);
            int value=player.level().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,zombie.getBoundingBox().inflate(2)).stream().mapToInt(orb -> orb.getValue()*orb.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getInt("Count")).sum();
            test.assertTrue(value==6,"Actual XP award not boosted: "+value);
        }finally {zombie.discard();player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void levelTwoRealKillDropsSevenExperienceInsteadOfFive(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=net.minecraft.world.phys.Vec3.atBottomCenterOf(test.absolutePos(new net.minecraft.core.BlockPos(8,5,8)));player.setPos(point);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPos(point.add(0,0,2));test.getLevel().addFreshEntity(zombie);
        try {player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.setLevel(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.DEATHS_DUE,2));
            zombie.hurt(player.damageSources().playerAttack(player),100);
            int value=player.level().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,zombie.getBoundingBox().inflate(2)).stream().mapToInt(orb -> orb.getValue()*orb.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getInt("Count")).sum();
            test.assertTrue(value==7,"Actual XP award not boosted: "+value);
        }finally {zombie.discard();player.server.getPlayerList().remove(player);}test.succeed();
    }
}
