package com.flansmod.common.guns.raytracing;

import net.minecraft.entity.Entity;

public class EntityHit extends BulletHit {
    public Entity entity;

    public EntityHit(Entity entity, float intersectTime) {
        super(intersectTime);
        this.entity = entity;
    }
}
