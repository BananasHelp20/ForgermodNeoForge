package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AbilityRemovalGameTests {
    @GameTest(template="riftfang_test")
    public static void removedQuenchPointFreesPassiveSlotAndNoLongerExtinguishesOrBoostsHits(GameTestHelper test) {
        String removed="tooltips.forgermod.passive.quench_point";
        for(var entry:ModItems.ITEMS.getEntries())test.assertFalse(Augmentations.catalog(new ItemStack(entry.get())).stream().anyMatch(a->a.id().equals(removed)),"Retired passive still in catalog");
        var player=TestPlayers.survival(test);
        try {
            for(var item:java.util.List.of(ModItems.EMBERFANG_DAGGER.get(),ModItems.EMBERFANG_DAGGER_RUBY.get(),ModItems.EMBERFANG_DAGGER_AMBER.get(),ModItems.EMBERFANG_DAGGER_AMETHYST.get(),ModItems.EMBERFANG_DAGGER_JADE.get())) {
                var stack=Augmentations.apply(new ItemStack(item),Augmentations.DUAL_WIELD);
                var data=stack.get(DataComponents.CUSTOM_DATA).copyTag();var row=new CompoundTag();row.putString("id",removed);row.putInt("level",4);data.getCompound("forgermod_augments").getList("levels",Tag.TAG_COMPOUND).add(0,row);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
                test.assertTrue(Augmentations.level(stack,removed)==0 && Augmentations.ids(stack,false).size()==1 && Augmentations.apply(stack,removed).isEmpty(),"Saved removed passive still consumes a slot");
                var upgraded=Augmentations.apply(stack,AdditionalAbilities.CHARRED_EDGE);test.assertFalse(upgraded.isEmpty(),"Retired passive blocks new passive");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);var victim=EntityType.COW.create(test.getLevel());victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);victim.setHealth(100);victim.setRemainingFireTicks(80);
                victim.hurt(player.damageSources().playerAttack(player),2);victim.invulnerableTime=0;victim.hurt(player.damageSources().playerAttack(player),2);
                test.assertTrue(victim.getRemainingFireTicks()==80 && victim.getHealth()==96,"Retired passive still extinguishes or converts fire into damage");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void removedFlamingComboIsIgnoredAndPyromaniacIsPassive(GameTestHelper test) {
        String removed="tooltips.forgermod.ability.flaming_combo",pyro="tooltips.forgermod.ability.pyromaniac";
        for(var entry:ModItems.ITEMS.getEntries()) {
            var stack=new ItemStack(entry.get());test.assertFalse(Augmentations.catalog(stack).stream().anyMatch(a->a.id().equals(removed)),"Removed ability remains in catalog");
        }
        var stack=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()),pyro);
        var data=stack.get(DataComponents.CUSTOM_DATA).copyTag();var row=new CompoundTag();row.putString("id",removed);row.putInt("level",4);
        data.getCompound("forgermod_augments").getList("levels",Tag.TAG_COMPOUND).add(0,row);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        test.assertTrue(Augmentations.level(stack,removed)==0 && Augmentations.ids(stack,true).isEmpty() && Augmentations.apply(stack,removed).isEmpty(),"Saved retired augment consumes a slot or can be relearned");
        test.assertTrue(Augmentations.hasPassive(stack,pyro),"Pyromaniac is not a passive after removing Flaming Combo");
        test.succeed();
    }
}
