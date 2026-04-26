package com.lookeypl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;

public class HomeNameSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    public HomeNameSuggestionProvider() {
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
            throws CommandSyntaxException {
        if (!context.getSource().isPlayer()) {
            return builder.buildFuture();
        }

        HomeCollection collection = context.getSource().getServer().getDataStorage().get(LPPPHomesMod.HOME_COLLECTION_SAVED_DATA);
        UUID sourceUUID = context.getSource().getEntity().getUUID();

        if (collection.exists(sourceUUID, context.getSource().getTextName())) {
            HomeCatalogue catalogue = collection.get(sourceUUID);
            for (Home h: catalogue.list()) {
                builder.suggest(h.getName());
            }
        }

        return builder.buildFuture();
    }
}
