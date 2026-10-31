package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class UnfinishedDreamGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenWeaponsHealOnlyForEnemiesThatNeverDamagedOwner(GameTestHelper test) {
        var player=TestPlayers.survival(test);int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.UNFINISHED_DREAM))==eligible,"Wrong Unfinished Dream assignment");
                if(!eligible) continue;checked++;String id=AdditionalAbilities.UNFINISHED_DREAM;var stack=fresh;
                for(int rank=1;rank<=3;rank++) {
                stack=Augmentations.apply(stack,id);test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,id)==rank,"Cannot upgrade Unfinished Dream");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var untouched=EntityType.ZOMBIE.create(test.getLevel());untouched.getAttribute(Attributes.ARMOR).setBaseValue(0);player.setHealth(10);
                untouched.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==10+2*rank,"Clean kill heals wrong rank amount");
                var attacker=EntityType.ZOMBIE.create(test.getLevel());attacker.getAttribute(Attributes.ARMOR).setBaseValue(0);player.invulnerableTime=0;player.setHealth(10);
                player.hurt(player.damageSources().mobAttack(attacker),1);float injured=player.getHealth();attacker.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==injured,"Enemy that injured owner still heals on kill");
                var absorbed=EntityType.ZOMBIE.create(test.getLevel());absorbed.getAttribute(Attributes.ARMOR).setBaseValue(0);player.invulnerableTime=0;player.setHealth(10);player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,100,0));player.setAbsorptionAmount(4);
                player.hurt(player.damageSources().mobAttack(absorbed),1);absorbed.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==10,"Absorbed hit treated as untouched enemy");player.removeEffect(net.minecraft.world.effect.MobEffects.ABSORPTION);player.setAbsorptionAmount(0);
                var unlearned=EntityType.ZOMBIE.create(test.getLevel());unlearned.getAttribute(Attributes.ARMOR).setBaseValue(0);player.setHealth(10);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                unlearned.hurt(player.damageSources().playerAttack(player),100);test.assertTrue(player.getHealth()==10,"Fresh gear heals on kill");
                var neutral=EntityType.COW.create(test.getLevel());player.setItemInHand(InteractionHand.MAIN_HAND,stack);neutral.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==10,"Neutral kill heals owner");
                for(boolean compact:new boolean[]{false,true}) {
                    var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(text.getString().equals("Killing an enemy before it damages you heals "+2*rank+" HP.") && white.toString().equals(2*rank+" HP"),"Wrong current text or highlights");
                }
                var changes=net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.changes(stack,id,rank);
                test.assertTrue(rank==3?changes.isEmpty():changes.size()==1 && changes.getFirst().property().equals("healing") && ((Number)changes.getFirst().after()).intValue()==2*(rank+1),"Wrong healing upgrade preview");
                var capped=EntityType.ZOMBIE.create(test.getLevel());capped.getAttribute(Attributes.ARMOR).setBaseValue(0);player.setHealth(19);capped.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==20,"Healing exceeds maximum health");capped.discard();untouched.discard();attacker.discard();absorbed.discard();unlearned.discard();neutral.discard();
                }
                test.assertTrue(Augmentations.apply(stack,id).isEmpty() && !Augmentations.hasSuperForm(stack,id),"Unfinished Dream exceeds III or becomes super");
            }
        } finally {player.server.getPlayerList().remove(player);}
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            var template=new ItemStack(item);String id=AdditionalAbilities.UNFINISHED_DREAM;
            test.assertTrue(Augmentations.maxRank(template,id)==3 && !Augmentations.setLevel(template,id,3).isEmpty() && Augmentations.setLevel(template,id,4).isEmpty(),"Template bypasses III cap");
        }
        test.assertTrue(checked==15,"Skipped Somnium family or gemstone");test.succeed();
    }
}
