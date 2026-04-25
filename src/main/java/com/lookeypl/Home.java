package com.lookeypl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public class Home {
    public static final Codec<Home> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(Home::getName),
        Identifier.CODEC.fieldOf("dimension").forGetter(Home::getDimensionIdentifier),
        Vec3.CODEC.fieldOf("pos").forGetter(Home::getPos),
        Vec2.CODEC.fieldOf("rot").forGetter(Home::getRot)
    ).apply(instance, Home::new));

    private String name;
    private Identifier dimensionId;
    private Vec3 pos;
    private Vec2 rot;

    public Home(final String name, final Identifier dimension, final Vec3 pos, final Vec2 rot) {
        this.name = name;
        this.dimensionId = dimension;
        this.pos = pos;
        this.rot = rot;
    }

    public String getName() {
        return name;
    }

    public Identifier getDimensionIdentifier() {
        return dimensionId;
    }

    public Vec3 getPos() {
        return pos;
    }

    public Vec2 getRot() {
        return rot;
    }

    public void rename(String newName) {
        this.name = newName;
    }
}
