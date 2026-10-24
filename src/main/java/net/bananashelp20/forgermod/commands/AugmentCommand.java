package net.bananashelp20.forgermod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class AugmentCommand {
    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
            id -> Component.translatable("commands.forgermod.augment.unknown", id.toString()));
    private static final DynamicCommandExceptionType AMBIGUOUS = new DynamicCommandExceptionType(
            id -> Component.translatable("commands.forgermod.augment.ambiguous", id.toString()));
    private static final DynamicCommandExceptionType NOT_LIVING = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.forgermod.augment.not_living", name));
    private static final DynamicCommandExceptionType NO_ITEM = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.forgermod.augment.no_item", name));
    private static final DynamicCommandExceptionType INCOMPATIBLE = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.forgermod.augment.incompatible", name));
    private static final Dynamic2CommandExceptionType LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType(
            (rank, maximum) -> Component.translatable("commands.forgermod.augment.level", rank, maximum));
    private static final SimpleCommandExceptionType NOTHING = new SimpleCommandExceptionType(
            Component.translatable("commands.forgermod.augment.failed"));

    private static final DynamicCommandExceptionType ALREADY = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.forgermod.augment.already", name));
    private static final DynamicCommandExceptionType MISSING = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.forgermod.augment.missing", name));
    private static final DynamicCommandExceptionType NO_SUPER = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.forgermod.augment.no_super", name));
    private enum Mode { NEW("new"), UPGRADE("upgrade"), SUPER("super");
        final String command; Mode(String command){this.command=command;}
    }
    private AugmentCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var root=Commands.literal("augment").requires(source -> source.hasPermission(2));
        for(var mode:Mode.values()) root.then(Commands.literal(mode.command)
                .then(Commands.argument("targets",EntityArgument.entities())
                        .then(Commands.argument("abilityName",StringArgumentType.greedyString())
                                .suggests((context,builder)->SharedSuggestionProvider.suggest(suggestions(context,mode),builder))
                                .executes(context->apply(context.getSource(),EntityArgument.getEntities(context,"targets"),
                                        StringArgumentType.getString(context,"abilityName"),mode)))));
        event.getDispatcher().register(root);
    }

    private static List<Augmentations.Ability> catalog() {
        return Augmentations.catalog(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));
    }

    private static String suffix(Augmentations.Ability ability) {
        String id = ability.id();
        if (Augmentations.DUAL_WIELD.equals(id)) return "dual_wield";
        if (Augmentations.VERTICAL_STRIKE.equals(id)) return "vertical_strike";
        return id.substring(id.lastIndexOf('.') + 1);
    }

    private static String category(Augmentations.Ability ability) {
        return Augmentations.MATERIAL_HIT.equals(ability.id()) ? "material" : ability.active() ? "ability" : "passive";
    }

    private static String superSuffix(Augmentations.Ability ability){
        String key=Augmentations.superName(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()),ability.id());
        if(key==null)return null;
        String withoutName=key.endsWith(".name")?key.substring(0,key.length()-5):key;
        return withoutName.substring(withoutName.lastIndexOf('.')+1);
    }
    private static List<String> suggestions(CommandContext<CommandSourceStack> context,Mode mode){
        Collection<? extends Entity> targets;
        try{targets=EntityArgument.getEntities(context,"targets");}catch(CommandSyntaxException ignored){return List.of();}
        var abilities=catalog();
        Map<String,Long> counts=abilities.stream().collect(Collectors.groupingBy(AugmentCommand::suffix,Collectors.counting()));
        var names=new java.util.TreeSet<String>();
        for(var ability:abilities){
            boolean available=targets.stream().anyMatch(target->{
                if(!(target instanceof LivingEntity living))return false;
                try{int rank=desiredRank(living.getMainHandItem(),ability,mode);return !Augmentations.setLevel(living.getMainHandItem(),ability.id(),rank).isEmpty();}
                catch(CommandSyntaxException ignored){return false;}
            });
            if(!available)continue;
            String suffix=mode==Mode.SUPER?superSuffix(ability):suffix(ability);
            if(suffix!=null)names.add(ForgerMod.MOD_ID+":"+(mode!=Mode.SUPER && counts.get(suffix)>1?category(ability)+"/":"")+suffix);
        }
        return List.copyOf(names);
    }
    private static String normalize(String name){return name.toLowerCase(java.util.Locale.ROOT).replace(' ','_').replace('-','_').replace("'","");}
    private static Augmentations.Ability resolve(String input,Mode mode)throws CommandSyntaxException{
        String path=input.trim();
        if(path.length()>=2 && path.startsWith("\"") && path.endsWith("\""))path=path.substring(1,path.length()-1);
        path=normalize(path);
        if(path.contains(":")){
            String namespace=path.substring(0,path.indexOf(':'));
            if(!namespace.equals(ForgerMod.MOD_ID) && !namespace.equals("minecraft"))throw UNKNOWN.create(input);
            path=path.substring(path.indexOf(':')+1);
        }
        final String name=path;
        var matches=catalog().stream().filter(ability->{
            String legacy=ability.id().substring(ability.id().lastIndexOf('.')+1);
            if(name.equals(category(ability)+"/"+suffix(ability)) || name.equals(suffix(ability)) || name.equals(legacy)
                    || name.equals(normalize(ability.name().getString())) || name.equals(ability.id()))return true;
            String superName=superSuffix(ability);
            return mode!=Mode.NEW && superName!=null && (name.equals(superName) || name.equals(category(ability)+"/"+superName));
        }).toList();
        if(matches.isEmpty())throw UNKNOWN.create(input);if(matches.size()>1)throw AMBIGUOUS.create(input);return matches.getFirst();
    }
    private static boolean superAlias(String input,Augmentations.Ability ability){
        String name=normalize(input.trim().replace("\"",""));int namespace=name.indexOf(':');if(namespace>=0)name=name.substring(namespace+1);
        String alias=superSuffix(ability);return alias!=null && (name.equals(alias) || name.equals(category(ability)+"/"+alias));
    }
    private static int desiredRank(ItemStack held,Augmentations.Ability ability,Mode mode)throws CommandSyntaxException{
        if(Augmentations.catalog(held).stream().noneMatch(a->a.id().equals(ability.id())))throw INCOMPATIBLE.create(held.getHoverName().getString());
        int current=Augmentations.level(held,ability.id());
        if(mode==Mode.NEW){if(current>0)throw ALREADY.create(ability.name());return 1;}
        if(mode==Mode.SUPER){
            if(!Augmentations.hasSuperForm(held,ability.id()))throw NO_SUPER.create(ability.name());
            if(Augmentations.isSuper(held,ability.id(),current))throw ALREADY.create(Augmentations.displayAbilityName(held,ability.id(),current));
            return Augmentations.superStart(ability.id());
        }
        if(current==0)throw MISSING.create(ability.name());
        if(current>=Augmentations.maxRank(held,ability.id()))throw LEVEL_TOO_HIGH.create(current+1,Augmentations.maxRank(held,ability.id()));
        return current+1;
    }

    private static int apply(CommandSourceStack source, Collection<? extends Entity> targets,
                             String name, Mode mode) throws CommandSyntaxException {
        Augmentations.Ability ability = resolve(name,mode);
        int changed = 0;
        Component singleName = ability.name();
        int displayRank = 1;
        for (Entity target : targets) {
            if (!(target instanceof LivingEntity living)) {
                if (targets.size() == 1) throw NOT_LIVING.create(target.getName().getString());
                continue;
            }
            ItemStack held = living.getMainHandItem();
            if (held.isEmpty()) {
                if (targets.size() == 1) throw NO_ITEM.create(living.getName().getString());
                continue;
            }
            if(mode==Mode.UPGRADE && superAlias(name,ability) && !Augmentations.hasSuperForm(held,ability.id())){
                if(targets.size()==1)throw NO_SUPER.create(ability.name());continue;
            }
            int rank;
            try{rank=desiredRank(held,ability,mode);}catch(CommandSyntaxException failure){if(targets.size()==1)throw failure;continue;}
            ItemStack augmented = Augmentations.setLevel(held, ability.id(), rank);
            if (augmented.isEmpty()) {
                if (targets.size() == 1) throw INCOMPATIBLE.create(held.getHoverName().getString());
                continue;
            }
            living.setItemInHand(InteractionHand.MAIN_HAND, augmented);
            if (living instanceof ServerPlayer player) player.inventoryMenu.broadcastChanges();
            changed++;
            singleName = Augmentations.displayAbilityName(augmented, ability.id(), rank);
            displayRank = Augmentations.displayRank(augmented, ability.id(), rank);
        }
        if (changed == 0) throw NOTHING.create();
        if (targets.size() == 1) {
            Component shownName = singleName;
            int shownRank = displayRank;
            Entity target = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable("commands.forgermod.augment.success.single",
                    shownName, shownRank, target.getDisplayName()), true);
        } else {
            int count = changed;
            source.sendSuccess(() -> Component.translatable("commands.forgermod.augment.updated.multiple",
                    ability.name(), count), true);
        }
        return changed;
    }
}
