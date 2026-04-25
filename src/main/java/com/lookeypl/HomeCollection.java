package com.lookeypl;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;


public class HomeCollection extends SavedData {
    public static final Codec<HomeCollection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(UUIDUtil.STRING_CODEC, HomeCatalogue.CODEC).xmap(
            HashMap<UUID, HomeCatalogue>::new, collection -> collection
        ).fieldOf("player_homes").forGetter(HomeCollection::getCataloguesMap)
    ).apply(instance, HomeCollection::new));

    private HashMap<UUID, HomeCatalogue> playerHomes = new HashMap<>();

    public HomeCollection() {
    }

    public HomeCollection(final HashMap<UUID, HomeCatalogue> homes) {
        playerHomes = homes;
    }

    public void add(UUID playerUUID, String playerName) {
        if (exists(playerUUID)) {
            throw new IllegalArgumentException("Player already exists in Collection; this should not happen, please fix. This is scary. Probably.");
        }

        playerHomes.put(playerUUID, new HomeCatalogue(playerUUID, playerName));
    }

    public void remove(UUID playerUUID) {
        if (!exists(playerUUID)) {
            throw new IllegalArgumentException("Player does not exist in home collection");
        }

        playerHomes.remove(playerUUID);
    }

    public HomeCatalogue get(UUID playerUUID) {
        if (!exists(playerUUID)) {
            throw new IllegalArgumentException("Player does not exist in home collection");
        }

        return playerHomes.get(playerUUID);
    }

    public Collection<HomeCatalogue> getCatalogues() {
        return playerHomes.values();
    }

    public boolean exists(UUID playerUUID) {
        return playerHomes.containsKey(playerUUID);
    }

    private HashMap<UUID, HomeCatalogue> getCataloguesMap() {
        return playerHomes;
    }
}
