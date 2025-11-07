package com.lookeypl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;


public class LPPPHomesMod implements ModInitializer {
    public static final String MOD_ID = "lppp-homes";

    public static final String HOME_COMMAND = "home";
    public static final String HOME_DEFAULT_COMMAND = "default";
    public static final String HOME_DELETE_COMMAND = "delete";
    public static final String HOME_HELP_COMMAND = "help";
    public static final String HOME_LIST_COMMAND = "list";
    public static final String HOME_RENAME_COMMAND = "rename";
    public static final String HOME_SET_COMMAND = "set";

    public static final String HOME_NAME_ARG = "name";
    public static final String HOME_OLD_NAME_ARG = "old_name";
    public static final String HOME_NEW_NAME_ARG = "new_name";

    public static final String HOME_DEFAULT_NAME = "main";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static HomeCollection homeCollection = new HomeCollection();


    // ===== PLAN OF ACTION ======
    //
    // The mode will be complete shall we complete the below functionalities
    // in the mod source code, as the needs of the mod shall then meet our needs
    // on the server.
    //
    // Requirements:
    //   - TPs player to home they provided
    //   + Supports multiple homes
    //   - Migrate homes from other mods (OP Only)
    //   - Load homes when user joins
    //   - Save homes when user leaves OR user changes their homes (sets, deletes, renames, defaults)
    //
    // Commands we want:
    //   - /home <name> - tps you to home of given name
    //     \_ CAN be called with <name> which will tp player to home of given name
    //     \_ CAN be called WITHOUT <name> to tp player to default home
    //   + /home set <name> - Sets a new home where player is located at the time of calling the command
    //     \_ <name> is required - fails without it
    //     \_ first set home is the default by default
    //   + /home delete <name> - Deletes an existing home
    //     \_ <name> is required unless there is one home on the list
    //     \_ confirmation required? maybe?
    //   + /home list - Lists homes available to the caller
    //     \_ OPs should be able to look up others homes
    //     \_ List should include - home name, coordinates
    //   + /home default <name> - Changes default home to <name>
    //     \_ <name> is required
    //   + /home rename <home> <newname> - renames a <home> to <newname>
    //   - /home help - prints help with available commands
    //     \_ result printed depends on who called it (ops will also see op commands listed)
    //
    // OP commands we want:
    //   - /home ??? - configure ???

    // Generic versions of commands

    public static int homeCommandInternal(CommandContext<CommandSourceStack> context, String homeName) {
        UUID playerUUID = context.getSource().getEntity().getUUID();
        String actualHomeName;
        if (homeName == "" && homeCollection.exists(playerUUID)) {
            actualHomeName = homeCollection.get(playerUUID).getDefault().getName();
        } else {
            actualHomeName = homeName;
        }

        // TODO actually TP the player :)

        context.getSource().sendSuccess(() -> Component.literal("TP to \"%s\"".formatted(actualHomeName)), false);
        return 0;
    }

    public static int homeDeleteCommandInternal(CommandContext<CommandSourceStack> context, String homeName) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("Command source is not a player"));
            return 1;
        }

        try {
            UUID playerUUID = context.getSource().getEntity().getUUID();
            homeCollection.get(playerUUID).delete(homeName);

            // TODO save...
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to delete home \"%s\": %s".formatted(homeName, e.getMessage())));
            return 2;
        }

        context.getSource().sendSuccess(() -> Component.literal("Deleted home \"%s\"".formatted(homeName)), false);
        return 0;
    }

    public static int homeSetCommandInternal(CommandContext<CommandSourceStack> context, String homeName) {
        Vec3 pos = context.getSource().getPosition();
        Vec2 rot = context.getSource().getRotation();

        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("Command source is not a player"));
            return 1;
        }

        try {
            UUID playerUUID = context.getSource().getEntity().getUUID();

            if (!homeCollection.exists(playerUUID)) {
                homeCollection.add(playerUUID);
            }

            homeCollection.get(playerUUID).add(new Home(homeName, pos, rot));

            // TODO save...
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to add new home: %s".formatted(e.getMessage())));
            return 2;
        }

        context.getSource().sendSuccess(() -> Component.literal("Added new home \"%s\"".formatted(homeName)), false);
        return 0;
    }

    public static void sendMsg(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSystemMessage(Component.literal(message));
    }

    public static void sendMsg(CommandContext<CommandSourceStack> context, String message, ChatFormatting... formatting) {
        context.getSource().sendSystemMessage(
            Component.literal(message).withStyle(formatting)
        );
    }


    // Command callbacks

    public static int executeHomeCommand(CommandContext<CommandSourceStack> context) {
        return homeCommandInternal(context, "");
    }

    public static int executeHomeNamedCommand(CommandContext<CommandSourceStack> context) {
        String homeName = StringArgumentType.getString(context, HOME_NAME_ARG);
        return homeCommandInternal(context, homeName);
    }

    public static int executeHomeDefaultCommand(CommandContext<CommandSourceStack> context) {
        String homeName = StringArgumentType.getString(context, HOME_NAME_ARG);

        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("Command source is not a player"));
            return 1;
        }

        try {
            UUID playerUUID = context.getSource().getEntity().getUUID();
            homeCollection.get(playerUUID).setDefault(homeName);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to set home \"%s\" as default: %s".formatted(homeName, e.getMessage())));
            return 2;
        }

        context.getSource().sendSuccess(() -> Component.literal("Set home \"%s\" as default".formatted(homeName)), false);
        return 0;
    }

    public static int executeHomeDeleteCommand(CommandContext<CommandSourceStack> context) {
        return homeDeleteCommandInternal(context, "");
    }

    public static int executeHomeDeleteNamedCommand(CommandContext<CommandSourceStack> context) {
        String homeName = StringArgumentType.getString(context, HOME_NAME_ARG);
        return homeDeleteCommandInternal(context, homeName);
    }

    public static int executeHomeHelpCommand(CommandContext<CommandSourceStack> context) {
        // TODO
        context.getSource().sendSuccess(() -> Component.literal("Help message is not done yet ;("), false);
        return 0;
    }

    public static int executeHomeListCommand(CommandContext<CommandSourceStack> context) {
        UUID playerUUID = context.getSource().getEntity().getUUID();
        if (!homeCollection.exists(playerUUID)) {
            sendMsg(context, "You don't have a home :(");
            return 0;
        }

        Collection<Home> homes = homeCollection.get(playerUUID).list();

        if (homes.size() == 0) {
            sendMsg(context, "You don't have a home :(");
        } else {
            sendMsg(context, "You have %d homes:".formatted(homes.size()));
            for (Home h : homes) {
                String homeString = "  - %s (%.2f, %.2f, %.2f)".formatted(h.getName(), h.getPos().x, h.getPos().y, h.getPos().z);
                if (h.isDefault()) {
                    sendMsg(context, homeString, ChatFormatting.ITALIC, ChatFormatting.GRAY);
                } else {
                    sendMsg(context, homeString, ChatFormatting.GRAY);
                }
            }
        }

        return homes.size();
    }

    public static int executeHomeRenameCommand(CommandContext<CommandSourceStack> context) {
        String oldHomeName = StringArgumentType.getString(context, HOME_OLD_NAME_ARG);
        String newHomeName = StringArgumentType.getString(context, HOME_NEW_NAME_ARG);

        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("Command source is not a player"));
            return 1;
        }

        try {
            UUID playerUUID = context.getSource().getEntity().getUUID();
            homeCollection.get(playerUUID).rename(oldHomeName, newHomeName);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to rename home \"%s\" to \"%s\": %s".formatted(oldHomeName, newHomeName, e.getMessage())));
            return 2;
        }
        context.getSource().sendSuccess(() -> Component.literal("Renamed home \"%s\" to \"%s\"".formatted(oldHomeName, newHomeName)), false);
        return 0;
    }

    public static int executeHomeSetCommand(CommandContext<CommandSourceStack> context) {
        return homeSetCommandInternal(context, HOME_DEFAULT_NAME);
    }

    public static int executeHomeSetNamedCommand(CommandContext<CommandSourceStack> context) {
        String homeName = StringArgumentType.getString(context, HOME_NAME_ARG);
        return homeSetCommandInternal(context, homeName);
    }


    // Overrides

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world! This is going to be LPPP Homes world soon (tm).");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
                Commands.literal(HOME_COMMAND).executes(LPPPHomesMod::executeHomeCommand);

            LiteralArgumentBuilder<CommandSourceStack> defaultCommandBuilder =
                Commands.literal(HOME_DEFAULT_COMMAND)
                    .then(
                        Commands.argument(HOME_NAME_ARG, StringArgumentType.string()).executes(LPPPHomesMod::executeHomeDefaultCommand)
                    );

            LiteralArgumentBuilder<CommandSourceStack> deleteCommandBuilder =
                Commands.literal(HOME_DELETE_COMMAND)
                .executes(LPPPHomesMod::executeHomeDeleteCommand)
                .then(
                    Commands.argument(HOME_NAME_ARG, StringArgumentType.string()).executes(LPPPHomesMod::executeHomeDeleteNamedCommand)
                );

            LiteralArgumentBuilder<CommandSourceStack> helpCommandBuilder =
                Commands.literal(HOME_HELP_COMMAND).executes(LPPPHomesMod::executeHomeHelpCommand);

            LiteralArgumentBuilder<CommandSourceStack> listCommandBuilder =
                Commands.literal(HOME_LIST_COMMAND).executes(LPPPHomesMod::executeHomeListCommand);

            LiteralArgumentBuilder<CommandSourceStack> renameCommandBuilder =
                Commands.literal(HOME_RENAME_COMMAND)
                .then(
                    Commands.argument(HOME_OLD_NAME_ARG, StringArgumentType.string())
                    .then(
                        Commands.argument(HOME_NEW_NAME_ARG, StringArgumentType.string()).executes(LPPPHomesMod::executeHomeRenameCommand)
                    )
                );

            LiteralArgumentBuilder<CommandSourceStack> setCommandBuilder =
                Commands.literal(HOME_SET_COMMAND)
                .executes(LPPPHomesMod::executeHomeSetCommand)
                .then(
                    Commands.argument(HOME_NAME_ARG, StringArgumentType.string()).executes(LPPPHomesMod::executeHomeSetNamedCommand)
                );

            commandBuilder.then(defaultCommandBuilder);
            commandBuilder.then(deleteCommandBuilder);
            commandBuilder.then(helpCommandBuilder);
            commandBuilder.then(listCommandBuilder);
            commandBuilder.then(renameCommandBuilder);
            commandBuilder.then(setCommandBuilder);

            dispatcher.register(commandBuilder);
        });

        try {
            homeCollection.load();
        } catch (Exception e) {
            LOGGER.error("Failed to load Homes catalogue");
        }
    }
}
