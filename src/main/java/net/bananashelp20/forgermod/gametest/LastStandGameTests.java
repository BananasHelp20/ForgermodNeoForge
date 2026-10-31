package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.LastStandEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LastStandGameTests {
    @GameTest(template="riftfang_test",batch="last_stand_review_ranks")
    public static void allRanksUseCurrentCooldownTextCapsAndSurvivalDeadline(GameTestHelper test){
        String id=AdditionalAbilities.LAST_STAND;var player=TestPlayers.survival(test);int count=0;
        try{for(var entry:ModItems.ITEMS.getEntries()){
            if(!(entry.get() instanceof MorsiumWeapon))continue;count++;
            var stack=new ItemStack(entry.get());
            for(int rank=1;rank<=3;rank++){
                stack=Augmentations.apply(stack,id);test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,id)==rank,"Cannot learn requested Last Stand rank");
                String interval=rank==1?"2 minutes":rank==2?"1.5 minutes":"1 minute";
                for(boolean compact:new boolean[]{false,true}){
                    var text=AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(text.getString().equals("Survive a fatal hit with 1 HP once every "+interval+".") && white.toString().equals("1 HP"+interval),"Wrong rank description/highlights");
                }
                var changes=AugmentUpgradeDescriptions.changes(stack,id,rank);
                test.assertTrue(rank==3?changes.isEmpty():changes.size()==1 && changes.getFirst().property().equals("cooldown") && ((Number)changes.getFirst().after()).intValue()==(rank==1?90:60),"Upgrade preview disagrees with survival cooldown");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.removeAllEffects();player.setAbsorptionAmount(0);player.setHealth(20);player.invulnerableTime=0;player.getPersistentData().remove(LastStandEvents.COOLDOWN);
                player.hurt(player.damageSources().generic(),100);
                int expected=new int[]{2400,1800,1200}[rank-1];long deadline=player.server.overworld().getGameTime()+expected;
                test.assertTrue(player.isAlive() && player.getHealth()==1 && player.getPersistentData().getLong(LastStandEvents.COOLDOWN)==deadline,"Actual fatal hit does not set rank cooldown");
                player.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.setLevel(new ItemStack(entry.get()),id,3));
                var hit=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(hit);
                test.assertTrue(hit.getNewDamage()==100 && player.getPersistentData().getLong(LastStandEvents.COOLDOWN)==deadline,"Switching rank/stack bypasses existing cooldown");
            }
            test.assertTrue(Augmentations.apply(stack,id).isEmpty() && !Augmentations.hasSuperForm(stack,id),"Last Stand exceeds III or becomes super");
        }}finally{player.server.getPlayerList().remove(player);}
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get(),ModItems.HOLLOW_CLAYMORE.get())){
            var blank=new ItemStack(item);test.assertTrue(Augmentations.maxRank(blank,id)==3 && !Augmentations.setLevel(blank,id,3).isEmpty() && Augmentations.setLevel(blank,id,4).isEmpty(),"Template/weapon/command cap exceeds III");
        }
        test.assertTrue(count==15 && LastStandEvents.cooldownTicks(0)==2400 && LastStandEvents.cooldownTicks(99)==1200,"Missing variants or unsafe rank bounds");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsSurviveOneFatalHitAndSharePersistentCooldown(GameTestHelper test) {
        var player=TestPlayers.survival(test);var clone=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LAST_STAND))==eligible,"Wrong Last Stand pool");if(!eligible) continue;count++;
            player.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.getPersistentData().remove(LastStandEvents.COOLDOWN);
            var pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==100,"Fresh weapon prevents death");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.LAST_STAND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.removeEffect(MobEffects.ABSORPTION);player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,200,0));player.setAbsorptionAmount(4);
            pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),22));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==22 && !player.getPersistentData().contains(LastStandEvents.COOLDOWN),"Nonfatal absorbed hit spends Last Stand");
            player.invulnerableTime=0;player.hurt(player.damageSources().generic(),100);test.assertTrue(player.isAlive() && player.getHealth()==1 && player.getAbsorptionAmount()==0,"Fatal hit not reduced to one HP after absorption");
            long deadline=player.getPersistentData().getLong(LastStandEvents.COOLDOWN);test.assertTrue(deadline==player.server.overworld().getGameTime()+2400,"Cooldown not two minutes");
            player.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==100,"New stack bypasses cooldown");
            LastStandEvents.clone(new PlayerEvent.Clone(clone,player,true));test.assertTrue(clone.getPersistentData().getLong(LastStandEvents.COOLDOWN)==deadline,"Respawn drops cooldown");
            player.getPersistentData().remove(LastStandEvents.COOLDOWN);player.setHealth(1);pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),2));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==2,"One HP incorrectly protected");
            player.setHealth(20);player.getPersistentData().putLong(LastStandEvents.COOLDOWN,player.server.overworld().getGameTime());pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==19,"Expired cooldown not usable");
        }} finally {player.server.getPlayerList().remove(player);clone.server.getPlayerList().remove(clone);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
