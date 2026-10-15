package net.bananashelp20.forgermod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    private AugmentCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("augment")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("augment", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(suggestions(), builder))
                                .executes(context -> apply(context.getSource(),
                                        EntityArgument.getEntities(context, "targets"),
                                        ResourceLocationArgument.getId(context, "augment"), 1))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(context -> apply(context.getSource(),
                                                EntityArgument.getEntities(context, "targets"),
                                                ResourceLocationArgument.getId(context, "augment"),
                                                IntegerArgumentType.getInteger(context, "level")))))));
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

    private static List<String> suggestions() {
        var abilities = catalog();
        Map<String, Long> counts = abilities.stream().collect(Collectors.groupingBy(AugmentCommand::suffix, Collectors.counting()));
        return abilities.stream().map(ability -> ForgerMod.MOD_ID + ":"
                + (counts.get(suffix(ability)) > 1 ? category(ability) + "/" : "") + suffix(ability))
                .distinct().sorted().toList();
    }

    private static Augmentations.Ability resolve(ResourceLocation name) throws CommandSyntaxException {
        // Unqualified names parse as minecraft IDs, as they do in vanilla /enchant.
        if (!ForgerMod.MOD_ID.equals(name.getNamespace()) && !"minecraft".equals(name.getNamespace())) throw UNKNOWN.create(name);
        String path = name.getPath();
        List<Augmentations.Ability> matches = catalog().stream().filter(ability -> {
            String scoped = category(ability) + "/" + suffix(ability);
            String legacy = ability.id().substring(ability.id().lastIndexOf('.') + 1);
            return path.equals(scoped) || path.equals(suffix(ability)) || path.equals(legacy);
        }).toList();
        if (matches.isEmpty()) throw UNKNOWN.create(name);
        if (matches.size() > 1) throw AMBIGUOUS.create(name);
        return matches.getFirst();
    }

    private static int apply(CommandSourceStack source, Collection<? extends Entity> targets,
                             ResourceLocation name, int rank) throws CommandSyntaxException {
        Augmentations.Ability ability = resolve(name);
        int changed = 0;
        Component singleName = ability.name();
        int displayRank = rank;
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
            if (rank > Augmentations.maxRank(held, ability.id())) {
                if (targets.size() == 1) throw LEVEL_TOO_HIGH.create(rank, Augmentations.maxRank(held, ability.id()));
                continue;
            }
            ItemStack augmented = Augmentations.setLevel(held, ability.id(), rank);
            if (augmented.isEmpty()) {
                if (targets.size() == 1) throw INCOMPATIBLE.create(held.getHoverName().getString());
                continue;
            }
            living.setItemInHand(InteractionHand.MAIN_HAND, augmented);
            if (living instanceof ServerPlayer player) player.inventoryMenu.broadcastChanges();
            changed++;
            singleName = Augmentations.abilityName(augmented, ability.id(), rank);
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
            source.sendSuccess(() -> Component.translatable("commands.forgermod.augment.success.multiple",
                    ability.name(), rank, count), true);
        }
        return changed;
    }
}
