package com.lookeypl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;


public class HomeCollection extends SavedData {
    public static final HomeCollectionCodec CODEC = new HomeCollectionCodec();

    private int randomNumber = 0;

    public int getRandomNumber() {
        return randomNumber;
    }

    public void incrementRandomNumber() {
        randomNumber++;
    }

    public HomeCollection() {
        this.randomNumber = 0;
    }

    public HomeCollection(int random) {
        this.randomNumber = random;
    }

    public static final Codec<HomeCollection> COUNT_CODEC = Codec.INT.xmap(
        HomeCollection::new,
        HomeCollection::getRandomNumber
    );



    private HashMap<UUID, HomeCatalogue> playerHomes = new HashMap<>();

    public void load() {

    }

    public void add(UUID playerUUID) {
        if (exists(playerUUID)) {
            throw new IllegalArgumentException("Player already exists in Collection; this should not happen, please fix. This is scary. Probably.");
        }

        playerHomes.put(playerUUID, new HomeCatalogue(playerUUID));
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

    public boolean exists(UUID playerUUID) {
        return playerHomes.containsKey(playerUUID);
    }

    public Collection<HomeCatalogue> getAllCatalogues() {
        return playerHomes.values();
    }
}
