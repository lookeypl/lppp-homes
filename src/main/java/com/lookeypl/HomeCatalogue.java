package com.lookeypl;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;


public class HomeCatalogue {
    public static final Codec<HomeCatalogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("owner_uuid").forGetter(HomeCatalogue::getOwnerUUID),
        Codec.STRING.fieldOf("owner_name").forGetter(HomeCatalogue::getOwnerName),
        Codec.STRING.fieldOf("default").forGetter(HomeCatalogue::getDefaultHomeName),
        Codec.unboundedMap(Codec.STRING, Home.CODEC).xmap(
            HashMap<String, Home>::new, home -> home
        ).fieldOf("homes").forGetter(HomeCatalogue::getAllHomes)
    ).apply(instance, HomeCatalogue::new));

    private UUID ownerUUID;
    private String ownerName;
    private String defaultHome = null;
    private HashMap<String, Home> homes = new HashMap<>();

    public HomeCatalogue(final UUID owner, final String ownerName) {
        this.ownerUUID = owner;
        this.ownerName = ownerName;
    }

    public HomeCatalogue(final UUID owner, final String ownerName, final String def, final HashMap<String, Home> h) {
        this.ownerUUID = owner;
        this.ownerName = ownerName;
        this.defaultHome = def;
        this.homes = h;
    }

    public void add(Home home) {
        if (homes.containsKey(home.getName())) {
            throw new IllegalArgumentException("Home %s already exists".formatted(home.getName()));
        }

        // TODO limit home count per player

        homes.put(home.getName(), home);

        // if this is the first Home we add, set it as default
        if (homes.size() == 1) {
            defaultHome = home.getName();
        }
    }

    public void delete(String name) {
        if (homes.size() == 0) {
            throw new IllegalArgumentException("You don't have a home to delete :(");
        }

        // TODO - what if we delete a default home but there are others on the list???

        if (name == "") {
            if (homes.size() > 1) {
                throw new IllegalArgumentException("Must provide home name to delete");
            }

            homes.clear();
        } else {
            if (!homes.containsKey(name)) {
                throw new IllegalArgumentException("Home %s does not exist".formatted(name));
            }

            homes.remove(name);
        }

        if (homes.size() == 0) {
            defaultHome = "";
        }
    }

    public void rename(String oldName, String newName) {
        if (!homes.containsKey(oldName)) {
            throw new IllegalArgumentException("Home %s does not exist".formatted(oldName));
        }

        if (homes.containsKey(newName)) {
            throw new IllegalArgumentException("New home name %s already taken".formatted(newName));
        }

        Home home = get(oldName);
        home.rename(newName);

        homes.remove(oldName);
        homes.put(newName, home);
    }

    public Home get(String name) {
        if (!homes.containsKey(name)) {
            throw new IllegalArgumentException("Home %s does not exist".formatted(name));
        }

        return homes.get(name);
    }

    public void setDefault(String name) {
        if (!homes.containsKey(name)) {
            throw new IllegalArgumentException("Home %s does not exist".formatted(name));
        }

        defaultHome = name;
    }

    public Home getDefault() {
        return get(getDefaultHomeName());
    }

    public Collection<Home> list() {
        return homes.values();
    }


    private UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getDefaultHomeName() {
        return defaultHome;
    }

    private HashMap<String, Home> getAllHomes() {
        return homes;
    }
}
