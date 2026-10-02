package com.yiran.minecraft.gtmqol.wireless.energy;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * {@code /gtmqol default_amperage [amperage]}: shows or sets the amperage the player's newly placed wireless
 * energy machines start with. Any player, for themselves only.
 */
public final class DefaultAmperageCommand {

    public static final String SHOW_KEY = "gtmqol.command.default_amperage.show";
    public static final String SET_KEY = "gtmqol.command.default_amperage.set";

    private DefaultAmperageCommand() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gtmqol")
                .then(Commands.literal("default_amperage")
                        .executes(DefaultAmperageCommand::show)
                        .then(Commands.argument("amperage",
                                IntegerArgumentType.integer(1, WirelessEnergyHatchPartMachine.MAX_AMPERAGE))
                                .executes(DefaultAmperageCommand::set))));
    }

    private static int show(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int amperage = WirelessEnergySavedData.get().getDefaultAmperage(player.getUUID());
        context.getSource().sendSuccess(() -> Component.translatable(SHOW_KEY,
                FormattingUtil.formatNumbers(amperage)), false);
        return amperage;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int amperage = IntegerArgumentType.getInteger(context, "amperage");
        WirelessEnergySavedData.get().setDefaultAmperage(player.getUUID(), amperage);
        context.getSource().sendSuccess(() -> Component.translatable(SET_KEY,
                FormattingUtil.formatNumbers(amperage)), false);
        return amperage;
    }
}
