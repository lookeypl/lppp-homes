package com.lookeypl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import com.lookeypl.importer.HomeImporterException;
import com.lookeypl.importer.ImporterSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;


public class LPPPHomesMod implements ModInitializer {
    public static final String MOD_ID = "lppp-homes";

    public static final String HOME_COMMAND = "home";
    public static final String HOME_DEFAULT_COMMAND = "default";
    public static final String HOME_DELETE_COMMAND = "delete";
    public static final String HOME_DELETEPLAYER_COMMAND = "deleteplayer";
    public static final String HOME_HELP_COMMAND = "help";
    public static final String HOME_IMPORT_COMMAND = "import";
    public static final String HOME_LIST_COMMAND = "list";
    public static final String HOME_LIST_ALL_COMMAND = "listall";
    public static final String HOME_RENAME_COMMAND = "rename";
    public static final String HOME_SET_COMMAND = "set";

    public static final String HOME_IMPORT_BLOSSOMHOMES_COMMAND = "blossomhomes";
    public static final String HOME_IMPORT_OVERWRITE_COMMAND = "overwrite";

    public static final String HOME_NAME_ARG = "name";
    public static final String HOME_OLD_NAME_ARG = "old_name";
    public static final String HOME_NEW_NAME_ARG = "new_name";

    public static final String HOME_DEFAULT_NAME = "main";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static HomeCollection homeCollection;
    public static final SavedDataType<HomeCollection> HOME_COLLECTION_SAVED_DATA = new SavedDataType<HomeCollection>(
        Identifier.fromNamespaceAndPath(LPPPHomesMod.MOD_ID, "lppp_home_collection"), // The unique name for this saved data.
        HomeCollection::new, // If there's no 'HomeCollection' yet create one and refresh fields.
        HomeCollection.CODEC, // The codec used for serialization/deserialization.
        null // A data fixer, which is not needed here.
    );


    // Generic versions of commands

    private static String getActualHomeName(String homeName, UUID playerUUID) {
        if (homeName == "") {
            return homeCollection.get(playerUUID).getDefault().getName();
        } else {
            return homeName;
        }
    }

    private static ServerLevel getDestinationServerLevel(MinecraftServer server, Identifier destinationId) {
        for (ServerLevel level: server.getAllLevels()) {
            if (level.dimension().identifier().compareTo(destinationId) == 0) {
                return level;
            }
        }

        return null;
    }

    public static int homeCommandInternal(CommandContext<CommandSourceStack> context, String homeName) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("Command source is not a player"));
            return 1;
        }

        try {
            Entity callerEntity = context.getSource().getEntity();
            UUID playerUUID = callerEntity.getUUID();
            String actualHomeName = getActualHomeName(homeName, playerUUID);
            Home home = homeCollection.get(playerUUID).get(actualHomeName);
            Vec2 homeRot = home.getRot();

            ServerLevel destinationLevel = getDestinationServerLevel(context.getSource().getServer(), home.getDimensionIdentifier());
            if (destinationLevel == null) {
                context.getSource().sendFailure(Component.literal("Unknown dimension \"%s\".".formatted(home.getDimensionIdentifier())));
                return 2;
            }

            TeleportTransition transition = new TeleportTransition(destinationLevel, home.getPos(), new Vec3(0, 0, 0), homeRot.y, homeRot.x, TeleportTransition.DO_NOTHING);
            callerEntity.teleport(transition);

            context.getSource().sendSuccess(() -> Component.literal("TP to \"%s\"".formatted(actualHomeName)), false);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to TP to home: %s".formatted(e.getMessage())));
            return 2;
        }

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
            homeCollection.setDirty();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to delete home \"%s\": %s".formatted(homeName, e.getMessage())));
            return 2;
        }

        context.getSource().sendSuccess(() -> Component.literal("Deleted home \"%s\"".formatted(homeName)), false);
        return 0;
    }

    public static int homeSetCommandInternal(CommandContext<CommandSourceStack> context, String homeName) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Command source is not a player"));
            return 1;
        }

        try {
            UUID playerUUID = source.getEntity().getUUID();

            if (!homeCollection.exists(playerUUID)) {
                homeCollection.add(playerUUID, source.getTextName());
            }

            homeCollection.get(playerUUID).add(new Home(homeName, source.getLevel().dimension().identifier(), source.getPosition(), source.getRotation()));
            homeCollection.setDirty();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to add new home: %s".formatted(e.getMessage())));
            return 2;
        }

        source.sendSuccess(() -> Component.literal("Added new home \"%s\"".formatted(homeName)), false);
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
            homeCollection.setDirty();
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

    public static int executeHomeDeletePlayerCommand(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, HOME_NAME_ARG);

        try {
            UUID toRemoveUUID = homeCollection.findUUID(playerName);
            if (toRemoveUUID == null) {
                context.getSource().sendFailure(Component.literal("Player \"%s\" not found.".formatted(playerName)));
                return 1;
            }

            homeCollection.remove(toRemoveUUID);
            homeCollection.setDirty();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to delete home \"%s\": %s".formatted(playerName, e.getMessage())));
            return 2;
        }

        context.getSource().sendSuccess(() -> Component.literal("Deleted player's \"%s\" homes.".formatted(playerName)), false);
        return 0;
    }

    public static int executeHomeHelpCommand(CommandContext<CommandSourceStack> context) {
        boolean isModerator = context.getSource().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        sendMsg(context, "---- LPPP Homes mod help page ----");
        sendMsg(context, "Available commands:");
        sendMsg(context, "  - /home <name> - TPs you to home of given name. Omit name to TP to default home.", ChatFormatting.GRAY);
        sendMsg(context, "  - /home set <name> - Sets a new home where player is now located.", ChatFormatting.GRAY);
        sendMsg(context, "  - /home delete <name> - Deletes an existing home.", ChatFormatting.GRAY);
        sendMsg(context, "  - /home default <name> - Changes default home to <name>", ChatFormatting.GRAY);
        sendMsg(context, "  - /home rename <home> <newname> - renames a <home> to <newname>", ChatFormatting.GRAY);
        sendMsg(context, "  - /home list - Lists available homes.", ChatFormatting.GRAY);
        if (isModerator) {
            sendMsg(context, "Available operator commands:");
            sendMsg(context, "  - /home listall - Lists all homes known to the mod.", ChatFormatting.GRAY);
            sendMsg(context, "  - /home deleteplayer - Removes player and their Homes from the collection.", ChatFormatting.GRAY);
            sendMsg(context, "  - /home import <provider> - Imports homes from a different mod. Available providers:", ChatFormatting.GRAY);
            sendMsg(context, "     -> blossomhomes", ChatFormatting.GRAY);
            sendMsg(context, "    If any Homes currently exist, import must be confirmed by \"/home import <provider> override\".", ChatFormatting.GRAY);
        }

        context.getSource().sendSuccess(() -> Component.literal("---------------------------------"), false);
        return 0;
    }

    private static int importHomes(CommandContext<CommandSourceStack> context, boolean overwrite) {
        if (!homeCollection.empty() && !overwrite) {
            sendMsg(context, "NOTE: There are existing homes that will be overwritten. Call \"/home import blossomhomes overwrite\" to confirm this is okay.");
            return 1;
        }

        try {
            MinecraftServer server = context.getSource().getServer();
            HomeCollection newCollection = HomeImporter.importHomes(server, ImporterSource.BLOSSOMHOMES);

            server.getDataStorage().set(HOME_COLLECTION_SAVED_DATA, newCollection);
            homeCollection = newCollection;
        } catch (HomeImporterException e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 2;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Caught Exception during import: %s".formatted(e.getMessage())));
            return 3;
        }

        context.getSource().sendSuccess(() -> Component.literal("Import from BlossomHomes successful."), false);
        return 0;
    }

    public static int executeHomeImportCommand(CommandContext<CommandSourceStack> context) {
        return importHomes(context, false);
    }

    public static int executeHomeImportOverwriteCommand(CommandContext<CommandSourceStack> context) {
        return importHomes(context, true);
    }

    private static void listHomes(CommandContext<CommandSourceStack> context, String username, Collection<Home> homes, String defaultHome) {
        if (homes.size() == 0) {
            sendMsg(context, "%s has no homes.".formatted(username));
        } else {
            if (homes.size() == 1) {
                sendMsg(context, "%s has 1 home:".formatted(username));
            } else {
                sendMsg(context, "%s has %d homes:".formatted(username, homes.size()));
            }
            for (Home h : homes) {
                String homeString = "  - %s (%.2f, %.2f, %.2f; %s)".formatted(h.getName(), h.getPos().x, h.getPos().y, h.getPos().z, h.getDimensionIdentifier());
                if (h.getName().contentEquals(defaultHome)) {
                    sendMsg(context, homeString, ChatFormatting.ITALIC, ChatFormatting.GRAY);
                } else {
                    sendMsg(context, homeString, ChatFormatting.GRAY);
                }
            }
        }
    }

    public static int executeHomeListCommand(CommandContext<CommandSourceStack> context) {
        UUID playerUUID = context.getSource().getEntity().getUUID();
        if (!homeCollection.exists(playerUUID)) {
            sendMsg(context, "You don't have a home :(");
            return 0;
        }

        Collection<Home> homes = homeCollection.get(playerUUID).list();
        String defaultHomeName = homeCollection.get(playerUUID).getDefaultHomeName();

        listHomes(context, context.getSource().getTextName(), homes, defaultHomeName);

        return homes.size();
    }

    public static int executeHomeListAllCommand(CommandContext<CommandSourceStack> context) {
        try {
            Collection<HomeCatalogue> catalogues = homeCollection.getCatalogues();
            for (HomeCatalogue c: catalogues) {
                listHomes(context, c.getOwnerName(), c.list(), c.getDefaultHomeName());
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to list all homes: %s".formatted(e.getMessage())));
            for (StackTraceElement stackTraceElement: e.getStackTrace()) {
                LOGGER.trace(stackTraceElement.toString());
            }
            return 1;
        }

        return 0;
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
            homeCollection.setDirty();
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
        LOGGER.info("Hello Fabric world! This is LPPP Homes mod reporting for duty.");

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

            RequiredArgumentBuilder<CommandSourceStack, String> nameArg =
                Commands.argument(HOME_NAME_ARG, StringArgumentType.string())
                .suggests(new HomeNameSuggestionProvider())
                .executes(LPPPHomesMod::executeHomeNamedCommand);


            // operator command list

            LiteralArgumentBuilder<CommandSourceStack> deletePlayerCommandBuilder =
                Commands.literal(HOME_DELETEPLAYER_COMMAND)
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(
                            Commands.argument(HOME_NAME_ARG, StringArgumentType.string()).executes(LPPPHomesMod::executeHomeDeletePlayerCommand)
                        );

            LiteralArgumentBuilder<CommandSourceStack> importCommandBuilder =
                Commands.literal(HOME_IMPORT_COMMAND)
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(
                            Commands.literal(HOME_IMPORT_BLOSSOMHOMES_COMMAND).executes(LPPPHomesMod::executeHomeImportCommand)
                                    .then(
                                        Commands.literal(HOME_IMPORT_OVERWRITE_COMMAND).executes(LPPPHomesMod::executeHomeImportOverwriteCommand)
                                    )
                        );

            LiteralArgumentBuilder<CommandSourceStack> listAllCommandBuilder =
                Commands.literal(HOME_LIST_ALL_COMMAND)
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(LPPPHomesMod::executeHomeListAllCommand);


            // Build the CLI

            commandBuilder.then(defaultCommandBuilder);
            commandBuilder.then(deleteCommandBuilder);
            commandBuilder.then(helpCommandBuilder);
            commandBuilder.then(listCommandBuilder);
            commandBuilder.then(renameCommandBuilder);
            commandBuilder.then(setCommandBuilder);
            commandBuilder.then(nameArg);

            commandBuilder.then(deletePlayerCommandBuilder);
            commandBuilder.then(importCommandBuilder);
            commandBuilder.then(listAllCommandBuilder);

            dispatcher.register(commandBuilder);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            // when server starts fetch the whole collection of Homes that is on the server saved with the server... server :)
            homeCollection = server.getDataStorage().computeIfAbsent(HOME_COLLECTION_SAVED_DATA);
        });
    }
}
