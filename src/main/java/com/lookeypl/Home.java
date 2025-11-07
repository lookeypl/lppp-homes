package com.lookeypl;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public class Home {
    private String name;
    private Vec3 pos;
    private Vec2 rot;
    private boolean def;

    public Home(String name, Vec3 pos, Vec2 rot) {
        this.name = name;
        this.pos = pos;
        this.rot = rot;
    }

    public String getName() {
        return name;
    }

    public Vec3 getPos() {
        return pos;
    }

    public Vec2 getRot() {
        return rot;
    }

    public boolean isDefault() {
        return def;
    }

    public void setDefault(boolean def) {
        this.def = def;
    }

    public void rename(String newName) {
        this.name = newName;
    }
}
