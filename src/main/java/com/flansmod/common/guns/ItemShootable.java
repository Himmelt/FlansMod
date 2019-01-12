package com.flansmod.common.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.vector.Vector3f;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

public abstract class ItemShootable extends Item {
    public ShootableType type;

    public ItemShootable(ShootableType t) {
        type = t;
        maxStackSize = type.maxStackSize;
        setMaxDamage(type.roundsPerItem);
        try {
            GameRegistry.registerItem(this, type.shortName, FlansMod.MODID);
        } catch (Throwable e) {
            FlansMod.log(Level.WARN, e.getMessage());
        }
    }

    //Can be overriden to allow new types of bullets to be created, for planes
    public abstract EntityShootable getEntity(World worldObj, Vec3 origin, float yaw,
                                              float pitch, double motionX, double motionY, double motionZ,
                                              EntityLivingBase shooter, float gunDamage, int itemDamage, InfoType shotFrom);

    //Can be overriden to allow new types of bullets to be created, vector constructor
    public abstract EntityShootable getEntity(World worldObj, Vector3f origin, Vector3f direction,
                                              EntityLivingBase shooter, float spread, float damage, float speed, int itemDamage, InfoType shotFrom);

    //Can be overriden to allow new types of bullets to be created, AA/MG constructor
    public abstract EntityShootable getEntity(World worldObj, Vec3 origin, float yaw,
                                              float pitch, EntityLivingBase shooter, float spread, float damage,
                                              int itemDamage, InfoType shotFrom);

    //Can be overriden to allow new types of bullets to be created, Handheld constructor
    public abstract EntityShootable getEntity(World worldObj, EntityLivingBase player,
                                              float bulletSpread, float damage, float bulletSpeed, boolean b,
                                              int itemDamage, InfoType shotFrom);

    public abstract RunningBullet getBullet(World world, EntityLivingBase shooter, float bulletSpread, float gunDamage, float bulletSpeed, boolean b, int itemDamage, InfoType shotType);

}
