package com.lookeypl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LPPPHomesMod implements ModInitializer {
    public static final String MOD_ID = "lppp-homes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world! This is going to be LPPP Homes world soon (tm).");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("foo").executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal("Called /foo with no arguments"), false);
                return 1;
           })
        ));
    }
}
