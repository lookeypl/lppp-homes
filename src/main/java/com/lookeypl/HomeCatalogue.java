package com.lookeypl;

import java.util.HashMap;
import java.util.Collection;


public class HomeCatalogue {
    private HashMap<String, Home> homes = new HashMap<>();
    private Home defaultHome = null;

    public void add(Home home) {
        if (homes.containsKey(home.getName())) {
            throw new IllegalArgumentException("Home %s already exists".formatted(home.getName()));
        }

        // TODO limit home count per player

        homes.put(home.getName(), home);

        // if this is the first Home we add, set it as default
        if (homes.size() == 1) {
            defaultHome = home;
            home.setDefault(true);
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
            defaultHome = null;
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

    public void load() {
        // ...
    }

    public void save() {
        // ...
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

        defaultHome.setDefault(false);
        defaultHome = homes.get(name);
        defaultHome.setDefault(true);
    }

    public Home getDefault() {
        return defaultHome;
    }

    public Collection<Home> list() {
        return homes.values();
    }
}
