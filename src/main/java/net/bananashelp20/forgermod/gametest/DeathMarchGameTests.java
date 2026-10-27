package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DeathMarchGameTests {
    private static final String ID="tooltips.forgermod.passive.death_march";

    @GameTest(template="riftfang_test")
    public static void allVariantsUseFiveRankDamageDescriptionsAndPreviews(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        var target=EntityType.ZOMBIE.create(test.getLevel());target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof MorsiumWeapon weapon) || !weapon.isDagger())continue;
                count++;var stack=new ItemStack(entry.get());
                for(int rank=1;rank<=5;rank++) {
                    stack=Augmentations.apply(stack,ID);test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,ID)==rank,"Cannot upgrade Death March to "+rank);
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    for(float fraction:new float[]{1,.5F,.25F}) {
                        target.setHealth(1000*fraction);target.invulnerableTime=0;float before=target.getHealth();
                        test.assertTrue(target.hurt(player.damageSources().playerAttack(player),10),"Native hit rejected");
                        float expected=10*(1+(1-fraction)*(.5F*rank));
                        test.assertTrue(Math.abs(before-target.getHealth()-expected)<.001,"Wrong native damage at rank "+rank+" health="+fraction);
                    }
                    for(boolean compact:new boolean[]{false,true}) {
                        var text=AbilityDescriptions.description(stack,ID,rank,compact);var white=new StringBuilder();
                        text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                        String phrase=50*rank+"% more damage";
                        test.assertTrue(text.getString().equals("Deal more damage depending on the targets health. Up to "+phrase+".") && white.toString().equals(phrase),"Wrong current description/highlighting");
                    }
                    var changes=AugmentUpgradeDescriptions.changes(stack,ID,rank);int next=50*(rank+1);
                    test.assertTrue(rank==5?changes.isEmpty():changes.stream().anyMatch(c->c.property().equals("max_damage_bonus") && ((Number)c.after()).intValue()==next),"Wrong maximum bonus preview");
                }
                test.assertTrue(Augmentations.apply(stack,ID).isEmpty(),"Death March can exceed V");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(entry.get()));target.setHealth(500);target.invulnerableTime=0;target.hurt(player.damageSources().playerAttack(player),10);
                test.assertTrue(target.getHealth()==490,"Unlearned passive grants bonus");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);target.setHealth(500);target.invulnerableTime=0;target.hurt(player.damageSources().indirectMagic(player,player),10);
                test.assertTrue(target.getHealth()==490,"Secondary damage gains Death March");
            }
            for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get(),ModItems.DEATHWISPER_DAGGER.get())) {
                var blank=new ItemStack(item);test.assertTrue(Augmentations.maxRank(blank,ID)==5,"Wrong template/weapon cap");
                var max=Augmentations.setLevel(blank,ID,5);test.assertTrue(!max.isEmpty() && Augmentations.level(max,ID)==5,"Command cannot grant V");
                test.assertTrue(Augmentations.setLevel(blank,ID,6).isEmpty() && Augmentations.available(max).stream().noneMatch(a->a.id().equals(ID)),"Command/offers exceed V");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Deathwisper variants");test.succeed();
    }

    @GameTest(template="riftfang_test",batch="death_march_guard_rank")
    public static void reinforcementUsesLearnedRankFromItsPassiveWeapon(GameTestHelper test) {
        var owner=TestPlayers.survival(test);owner.setPos(test.getBounds().getCenter());
        var summon=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);owner.setItemInHand(InteractionHand.MAIN_HAND,summon);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        try {
            test.assertTrue(BoneGuardEvents.activate(owner,summon),"Reinforcement fixture cannot summon");
            var guard=owner.level().getEntitiesOfClass(Mob.class,owner.getBoundingBox().inflate(6),m->m instanceof BoneGuardEvents.FriendlyGuard g && g.owner()==owner).getFirst();
            var main=new ItemStack(ModItems.DEATHWISPER_DAGGER.get());var off=Augmentations.setLevel(new ItemStack(ModItems.DEATHWISPER_DAGGER_RUBY.get()),ID,5);
            off=Augmentations.apply(off,Augmentations.DUAL_WIELD);guard.setItemSlot(EquipmentSlot.MAINHAND,main);guard.setItemSlot(EquipmentSlot.OFFHAND,off);
            target.setHealth(500);target.invulnerableTime=0;target.hurt(guard.damageSources().mobAttack(guard),10);
            test.assertTrue(Math.abs(target.getHealth()-477.5F)<.001,"Reinforcement ignores offhand passive rank V");
        } finally {BoneGuardEvents.cancel(owner.getUUID());owner.server.getPlayerList().remove(owner);}
        test.succeed();
    }
}
