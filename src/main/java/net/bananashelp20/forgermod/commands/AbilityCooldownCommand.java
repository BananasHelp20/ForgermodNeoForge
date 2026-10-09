package net.bananashelp20.forgermod.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.commands.Commands;
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
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("disabled", BoolArgumentType.bool()).executes(context -> {
                            boolean disabled = BoolArgumentType.getBool(context, "disabled");
                            var targets = EntityArgument.getPlayers(context, "target");
                            for (var player : targets) {
                                player.setData(WeaponCooldownAttachments.DISABLED, disabled);
                                if (disabled) player.setData(WeaponCooldownAttachments.COOLDOWNS, Map.of());
                            }
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "Ability cooldowns " + (disabled ? "disabled" : "enabled")
                                            + " for " + targets.size() + " player(s)."), true);
                            return targets.size();
                        }))));
    }
}
