package net.bananashelp20.forgermod.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import java.util.Map;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class AbilityCooldownCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("no-ability-cooldown")
                // Keep the command discoverable; a hidden root reports "unknown command" without cheats.
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("commands.forgermod.cooldown.usage"), false);
                    return 0;
                })
                // A literal self target allows a clear permission error before vanilla's selector parser rejects @s.
                .then(Commands.literal("@s")
                        .then(Commands.argument("disabled", BoolArgumentType.bool()).executes(context -> {
                            if (!allowed(context.getSource())) return 0;
                            return apply(context.getSource(), BoolArgumentType.getBool(context, "disabled"),
                                    java.util.List.of(context.getSource().getPlayerOrException()));
                        })))
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("disabled", BoolArgumentType.bool()).executes(context -> {
                            if (!allowed(context.getSource())) return 0;
                            boolean disabled = BoolArgumentType.getBool(context, "disabled");
                            var targets = EntityArgument.getPlayers(context, "target");
                            return apply(context.getSource(), disabled, targets);
                        }))));
    }
    private static boolean allowed(CommandSourceStack source) {
        if (source.hasPermission(2)) return true;
        source.sendFailure(Component.translatable("commands.forgermod.cooldown.permission"));
        return false;
    }
    private static int apply(CommandSourceStack source, boolean disabled, java.util.Collection<net.minecraft.server.level.ServerPlayer> targets) {
        for (var player : targets) {
            player.setData(WeaponCooldownAttachments.DISABLED, disabled);
            if (disabled) player.setData(WeaponCooldownAttachments.COOLDOWNS, Map.of());
        }
        source.sendSuccess(() -> Component.literal("Ability cooldowns " + (disabled ? "disabled" : "enabled")
                + " for " + targets.size() + " player(s)."), true);
        return targets.size();
    }
}
