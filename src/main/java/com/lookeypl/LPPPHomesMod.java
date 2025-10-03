package com.lookeypl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LPPPHomesMod implements ModInitializer {
    public static final String MOD_ID = "lppp-homes";

    public static final String HOME_COMMAND = "home";
    public static final String HOME_SET_COMMAND = "set";
    public static final String ECHO_MESSAGE_ARG = "message";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ===== PLAN OF ACTION ======
    //
    // The mode will be complete shall we complete the below functionalities
    // in the mod source code, as the needs of the mod shall then meet our needs
    // on the server.
    //
    // Requirements:
    //   - TPs player to home they provided
    //   - Supports multiple homes
    //   - Migrate homes from other mods (OP Only)
    //
    // Commands we want:
    //   - /home <name> - tps you to home of given name
    //     \_ CAN be called with <name> which will tp player to home of given name
    //     \_ CAN be called WITHOUT <name> to tp player to default home
    //   - /home set <name> - Sets a new home where player is located at the time of calling the command
    //     \_ <name> is required - fails without it
    //     \_ first set home is the default by default
    //   - /home del <name> - Deletes an existing home
    //     \_ <name> is required
    //     \_ confirmation required? maybe?
    //   - /home list - Lists homes available to the caller
    //     \_ OPs should be able to look up others homes
    //     \_ List should include - home name, coordinates
    //   - /home default <name> - Changes default home to <name>
    //     \_ <name> is required
    //   - /home rename <home> <newname> - renames a <home> to <newname>
    //   - /home help - prints help with available commands
    //     \_ result printed depends on who called it (ops will also see op commands listed)
    //
    // OP commands we want:
    //   - /home ??? - configure ???

    public static int executeHomeCommand(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Called /home with no arguments"), false);
        return 0;
    }

    public static int executeHomeSetCommand(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Called /home set with no arguments"), false);
        return 2;
    }

    public static int executeMessageEcho(CommandContext<ServerCommandSource> context) {
        String message = StringArgumentType.getString(context, ECHO_MESSAGE_ARG);
        context.getSource().sendFeedback(() -> Text.literal("Message: %s".formatted(message)), false);
        return 1;
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world! This is going to be LPPP Homes world soon (tm).");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> commandBuilder =
                CommandManager.literal(HOME_COMMAND).executes(LPPPHomesMod::executeHomeCommand);

            RequiredArgumentBuilder<ServerCommandSource, String> argBuilder =
                CommandManager.argument(ECHO_MESSAGE_ARG, StringArgumentType.string()).executes(LPPPHomesMod::executeMessageEcho);

            LiteralArgumentBuilder<ServerCommandSource> subCommandBuilder =
                CommandManager.literal(HOME_SET_COMMAND).executes(LPPPHomesMod::executeHomeSetCommand);

            subCommandBuilder.then(argBuilder);
            commandBuilder.then(subCommandBuilder);

            dispatcher.register(commandBuilder);
        });
    }
}
