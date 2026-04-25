package com.lookeypl.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lookeypl.Home;
import com.lookeypl.HomeCatalogue;
import com.lookeypl.HomeCollection;
import com.lookeypl.LPPPHomesMod;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository;

class BlossomHome {
    public String name;
    public String world;
    public double x, y, z;
    public float yaw, pitch;
}

class BlossomPlayerHomes {
    public UUID uuid;
    public ArrayList<BlossomHome> homes;
    public int maxHomes;

    public BlossomPlayerHomes(UUID uuid) {
        this.uuid = uuid;
        homes = new ArrayList<>();
        maxHomes = 0;
    }
}

public class BlossomHomesImporterImpl implements ImporterImpl {
    public static final Logger LOGGER = LoggerFactory.getLogger(LPPPHomesMod.MOD_ID);

    protected static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .create();

    private File getBlossomHomesFile(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("BlossomHomes.json")
                .toFile();
    }

    private String determinePlayerName(ProfileResolver profileResolver, UUID uuid) {
        Optional<GameProfile> profile = profileResolver.fetchById(uuid);
        if (profile.isPresent()) {
            return profile.get().name();
        } else {
            LOGGER.warn("Could not determine profile name for ID %s, will be left empty.".formatted(uuid));
            return "";
        }
    }

    @Override
    public HomeCollection importHomes(MinecraftServer server) throws HomeImporterException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(getBlossomHomesFile(server)));) {
            BlossomPlayerHomes[] blossomHomes = GSON.fromJson(reader, BlossomPlayerHomes[].class);
            if (blossomHomes.length == 0) {
                throw new HomeImporterException("No homes to import.");
            }

            ProfileResolver profileResolver = server.services().profileResolver();

            HomeCollection collection = new HomeCollection();
            for (BlossomPlayerHomes playerHomes: blossomHomes) {
                String playerName = determinePlayerName(profileResolver, playerHomes.uuid);
                HomeCatalogue newCatalogue = collection.add(playerHomes.uuid, playerName);
                for (BlossomHome home: playerHomes.homes) {
                    newCatalogue.add(new Home(home.name, Identifier.parse(home.world), new Vec3(home.x, home.y, home.z), new Vec2(home.pitch, home.yaw)));
                }
            }

            return collection;
        } catch (IOException e) {
            HomeImporterException ex = new HomeImporterException("Failed to import Homes from BlossomHomes: %s".formatted(e.getMessage()));
            ex.addSuppressed(e);
            throw ex;
        }
    }

    @Override
    public ImporterSource getSourceType() {
        return ImporterSource.BLOSSOMHOMES;
    }
}
