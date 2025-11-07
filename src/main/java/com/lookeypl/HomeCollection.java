package com.lookeypl;

import java.util.HashMap;
import java.util.UUID;


public class HomeCollection {
    private HashMap<UUID, HomeCatalogue> playerHomes = new HashMap<>();

    public void load() {
        // ...
    }

    public void add(UUID playerUUID) {
        if (exists(playerUUID)) {
            throw new IllegalArgumentException("Player already exists in Collection; this should not happen, please fix. This is scary. Probably.");
        }

        playerHomes.put(playerUUID, new HomeCatalogue());
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
}
