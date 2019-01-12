package com.flansmod.common.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.debug.EntityDebugDot;
import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntityPlane;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.driveables.EntityVehicle;
import com.flansmod.common.driveables.mechas.EntityMecha;
import com.flansmod.common.guns.raytracing.*;
import com.flansmod.common.network.PacketFlak;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.vector.Vector3f;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EntityBullet extends EntityShootable implements IEntityAdditionalSpawnData {
    private static int bulletLife = 600; //Kill bullets after 30 seconds
    public Entity owner;
    private int ticksInAir;
    public BulletType type;
    /**
     * What type of weapon did this come from? For death messages
     */
    public InfoType firedFrom;
    /**
     * The amount of damage the gun imparted upon the bullet. Multiplied by the bullet damage to get total damage
     */
    public float damage;
    public boolean shotgun = false;
    /**
     * If this is non-zero, then the player raytrace code will look back in time to when the player thinks their bullet should have hit
     */
    public int pingOfShooter = 0;
    /**
     * Avoids the fact that using the entity random to calculate spread direction always results in the same direction
     */
    public static Random bulletRandom = new Random();
    /**
     * For homing missiles
     */
    public Entity lockedOnTo;

    public float penetratingPower;

    public EntityBullet(World world) {
        super(world);
        ticksInAir = 0;
        setSize(1.5F, 1.5F);
    }

    /**
     * Private partial constructor to avoid repeated code. All constructors go through this one
     */
    private EntityBullet(World world, EntityLivingBase shooter, float gunDamage, BulletType bulletType, InfoType shotFrom) {
        this(world);
        owner = shooter;
        if (shooter instanceof EntityPlayerMP) pingOfShooter = ((EntityPlayerMP) shooter).ping;
        type = bulletType;
        firedFrom = shotFrom;
        damage = gunDamage;
        if (type != null) {
            penetratingPower = type.penetratingPower;
            //setSize(type.hitBoxSize, type.hitBoxSize);
        }
    }

    /**
     * Method called by ItemGun for creating bullets from a hand held weapon
     */
    public EntityBullet(World world, EntityLivingBase shooter, float spread, float gunDamage, BulletType bulletType, float speed, boolean shot, InfoType shotFrom) {
        this(world, Vec3.createVectorHelper(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ), shooter.rotationYaw, shooter.rotationPitch, shooter, spread, gunDamage, bulletType, speed, shotFrom);
        shotgun = shot;
    }

    /**
     * Machinegun / AAGun bullet constructor
     */
    public EntityBullet(World world, Vec3 origin, float yaw, float pitch, EntityLivingBase shooter, float spread, float gunDamage, BulletType type1, InfoType shotFrom) {
        this(world, origin, yaw, pitch, shooter, spread, gunDamage, type1, 3.0F, shotFrom);
    }

    /**
     * More generalised bullet constructor
     */
    public EntityBullet(World world, Vec3 origin, float yaw, float pitch, EntityLivingBase shooter, float spread, float gunDamage, BulletType bulletType, float speed, InfoType shotType) {
        this(world, shooter, gunDamage, bulletType, shotType);
        setLocationAndAngles(origin.xCoord, origin.yCoord, origin.zCoord, yaw, pitch);
        setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        motionX = -MathHelper.sin((rotationYaw / 180F) * 3.14159265F) * MathHelper.cos((rotationPitch / 180F) * 3.14159265F);
        motionZ = MathHelper.cos((rotationYaw / 180F) * 3.14159265F) * MathHelper.cos((rotationPitch / 180F) * 3.14159265F);
        motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F);
        setArrowHeading(motionX, motionY, motionZ, spread / 2F, speed);
    }

    /**
     *
     */
    public EntityBullet(World world, Vector3f origin, Vector3f direction, EntityLivingBase shooter, float spread, float gunDamage, BulletType bulletType, float speed, InfoType shotType) {
        this(world, shooter, gunDamage, bulletType, shotType);
        damage = gunDamage;
        setPosition(origin.x, origin.y, origin.z);
        motionX = direction.x;
        motionY = direction.y;
        motionZ = direction.z;
        setArrowHeading(motionX, motionY, motionZ, spread, speed);
    }

    /**
     * Bomb constructor. Inherits the motion and rotation of the plane
     */
    public EntityBullet(World world, Vec3 origin, float yaw, float pitch, double motX, double motY, double motZ, EntityLivingBase shooter, float gunDamage, BulletType bulletType, InfoType shotType) {
        this(world, shooter, gunDamage, bulletType, shotType);
        setLocationAndAngles(origin.xCoord, origin.yCoord, origin.zCoord, yaw, pitch);
        setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        motionX = motX;
        motionY = motY;
        motionZ = motZ;
    }

    @Override
    protected void entityInit() {
    }

    public void setArrowHeading(double d, double d1, double d2, float spread, float speed) {
        float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
        d /= f2;
        d1 /= f2;
        d2 /= f2;
        d *= speed;
        d1 *= speed;
        d2 *= speed;
        d += rand.nextGaussian() * 0.005D * spread * speed;
        d1 += rand.nextGaussian() * 0.005D * spread * speed;
        d2 += rand.nextGaussian() * 0.005D * spread * speed;
        motionX = d;
        motionY = d1;
        motionZ = d2;
        float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
        prevRotationYaw = rotationYaw = (float) ((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
        prevRotationPitch = rotationPitch = (float) ((Math.atan2(d1, f3) * 180D) / 3.1415927410125732D);

        getLockOnTarget();
    }

    /**
     * Find the entity nearest to the missile's trajectory, anglewise
     */
    private void getLockOnTarget() {
        if (type.lockOnToPlanes || type.lockOnToVehicles || type.lockOnToMechas || type.lockOnToLivings || type.lockOnToPlayers) {
            Vector3f motionVec = new Vector3f(motionX, motionY, motionZ);
            Entity closestEntity = null;
            float closestAngle = type.maxLockOnAngle * 3.14159265F / 180F;

            for (Object obj : worldObj.loadedEntityList) {
                Entity entity = (Entity) obj;
                if ((type.lockOnToMechas && entity instanceof EntityMecha) || (type.lockOnToVehicles && entity instanceof EntityVehicle) || (type.lockOnToPlanes && entity instanceof EntityPlane) || (type.lockOnToPlayers && entity instanceof EntityPlayer) || (type.lockOnToLivings && entity instanceof EntityLivingBase)) {
                    Vector3f relPosVec = new Vector3f(entity.posX - posX, entity.posY - posY, entity.posZ - posZ);
                    float angle = Math.abs(Vector3f.angle(motionVec, relPosVec));
                    if (angle < closestAngle) {
                        closestEntity = entity;
                        closestAngle = angle;
                    }
                }
            }

            if (closestEntity != null)
                lockedOnTo = closestEntity;
        }
    }

    @Override
    public void setVelocity(double d, double d1, double d2) {
        motionX = d;
        motionY = d1;
        motionZ = d2;
        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt_double(d * d + d2 * d2);
            prevRotationYaw = rotationYaw = (float) ((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
            prevRotationPitch = rotationPitch = (float) ((Math.atan2(d1, f) * 180D) / 3.1415927410125732D);
            setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        //Check the fuse to see if the bullet should explode
        ticksInAir++;
        if (type == null || ticksInAir > type.fuse && type.fuse > 0 && !isDead) setDead();

        if (ticksExisted > bulletLife) setDead();

        if (isDead) return;

        //Create a list for all bullet hits
        ArrayList<BulletHit> hits = new ArrayList<>();
        Vector3f lastPos = new Vector3f(posX, posY, posZ);
        Vector3f nextPos = new Vector3f(posX + motionX, posY + motionY, posZ + motionZ);
        Vector3f motion = new Vector3f(motionX, motionY, motionZ);
        float speed = motion.length();

        double time = Math.max(Math.abs(motionX), Math.max(Math.abs(motionY), Math.abs(motionZ))) / Math.min(width, height);
        double unitX = motionX / time;
        double unitY = motionY / time;
        double unitZ = motionZ / time;

        //Iterate over all entities
        for (int i = 0; i < worldObj.loadedEntityList.size(); i++) {
            Entity entity = (Entity) worldObj.loadedEntityList.get(i);

            //Get driveables
            if (entity instanceof EntityDriveable) {
                EntityDriveable driveable = (EntityDriveable) entity;

                if (driveable.isDead() || driveable.isPartOfThis(owner)) continue;

                //If this bullet is within the driveable's detection range
                if (getDistanceToEntity(driveable) <= driveable.getDriveableType().bulletDetectionRadius + speed) {
                    //Raytrace the bullet
                    ArrayList<BulletHit> driveableHits = driveable.attackFromBullet(lastPos, motion);
                    hits.addAll(driveableHits);
                }
            } else if ((entity instanceof EntityLivingBase || entity instanceof EntityAAGun || entity instanceof EntityGrenade) && entity != this && entity != owner && !entity.isDead) {
                double cX = (entity.boundingBox.minX + entity.boundingBox.maxX) / 2;
                double cY = (entity.boundingBox.minY + entity.boundingBox.maxY) / 2;
                double cZ = (entity.boundingBox.minZ + entity.boundingBox.maxZ) / 2;
                double distance = lastPos.toVec3().distanceTo(Vec3.createVectorHelper(cX, cY, cZ));
                for (int j = 0; j <= time; j++) {
                    if (boundingBox.getOffsetBoundingBox(j * unitX, j * unitY, j * unitZ).intersectsWith(entity.boundingBox)) {
                        hits.add(new EntityHit(entity, (float) distance / speed));
                        break;
                    }
                }
            }
        }

        // rayTraceBlocks
        MovingObjectPosition hit = worldObj.func_147447_a(lastPos.toVec3(), nextPos.toVec3(), false, true, true);
        if (hit != null) {
            double distance = lastPos.toVec3().distanceTo(hit.hitVec);
            hits.add(new BlockHit(hit, (float) (distance / speed)));
        }

        //We hit something
        if (!hits.isEmpty()) {
            //Sort the hits according to the intercept position
            Collections.sort(hits);

            for (BulletHit bulletHit : hits) {
                if (bulletHit instanceof DriveableHit) {
                    DriveableHit driveableHit = (DriveableHit) bulletHit;
                    penetratingPower = driveableHit.driveable.bulletHit(this, driveableHit, penetratingPower);
                    if (FlansMod.DEBUG)
                        worldObj.spawnEntityInWorld(new EntityDebugDot(worldObj, new Vector3f(posX + motionX * driveableHit.intersectTime, posY + motionY * driveableHit.intersectTime, posZ + motionZ * driveableHit.intersectTime), 1000, 0F, 0F, 1F));

                } else if (bulletHit instanceof PlayerBulletHit) {
                    PlayerBulletHit playerHit = (PlayerBulletHit) bulletHit;
                    penetratingPower = playerHit.hitbox.hitByBullet(this, penetratingPower);
                    if (FlansMod.DEBUG)
                        worldObj.spawnEntityInWorld(new EntityDebugDot(worldObj, new Vector3f(posX + motionX * playerHit.intersectTime, posY + motionY * playerHit.intersectTime, posZ + motionZ * playerHit.intersectTime), 1000, 1F, 0F, 0F));
                } else if (bulletHit instanceof EntityHit) {
                    EntityHit entityHit = (EntityHit) bulletHit;
                    if (entityHit.entity.attackEntityFrom(getBulletDamage(false), damage * type.damageVsLiving) && entityHit.entity instanceof EntityLivingBase) {
                        EntityLivingBase living = (EntityLivingBase) entityHit.entity;
                        for (PotionEffect effect : type.hitEffects) {
                            living.addPotionEffect(new PotionEffect(effect));
                        }
                        //If the attack was allowed, we should remove their immortality cooldown so we can shoot them again. Without this, any rapid fire gun become useless
                        living.arrowHitTimer++;
                        living.hurtResistantTime = living.maxHurtResistantTime / 2;
                    }
                    if (type.setEntitiesOnFire)
                        entityHit.entity.setFire(20);
                    penetratingPower -= 1F;
                    if (FlansMod.DEBUG)
                        worldObj.spawnEntityInWorld(new EntityDebugDot(worldObj, new Vector3f(posX + motionX * entityHit.intersectTime, posY + motionY * entityHit.intersectTime, posZ + motionZ * entityHit.intersectTime), 1000, 1F, 1F, 0F));
                } else if (bulletHit instanceof BlockHit) {
                    BlockHit blockHit = (BlockHit) bulletHit;
                    MovingObjectPosition raytraceResult = blockHit.raytraceResult;
                    //If the hit wasn't an entity hit, then it must've been a block hit
                    int xTile = raytraceResult.blockX;
                    int yTile = raytraceResult.blockY;
                    int zTile = raytraceResult.blockZ;
                    if (FlansMod.DEBUG)
                        worldObj.spawnEntityInWorld(new EntityDebugDot(worldObj, new Vector3f(raytraceResult.hitVec.xCoord, raytraceResult.hitVec.yCoord, raytraceResult.hitVec.zCoord), 1000, 0F, 1F, 0F));

                    Block block = worldObj.getBlock(xTile, yTile, zTile);
                    if (block == Blocks.fence || block == Blocks.fence_gate || block == Blocks.nether_brick_fence) continue;

                    Material mat = block.getMaterial();
                    if (mat == Material.air || mat == Material.glass || mat == Material.grass || mat == Material.leaves) continue;

                    int oreId = OreDictionary.getOreID(new ItemStack(block));
                    if (oreId != -1) if (OreDictionary.getOreName(oreId).equals("treeLeaves")) continue;

                    //penetratingPower -= block.getBlockHardness(worldObj, zTile, zTile, zTile);
                    if (hit != null) setPosition(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
                    setDead();
                    break;
                }
                if (penetratingPower <= 0F || (type.explodeOnImpact && ticksInAir > 1)) {
                    setPosition(posX + motionX * bulletHit.intersectTime, posY + motionY * bulletHit.intersectTime, posZ + motionZ * bulletHit.intersectTime);
                    setDead();
                    break;
                }
            }
        }

        //Movement dampening variables
        float drag = 0.99F;
        float gravity = 0.02F;
        //If the bullet is in water, spawn particles and increase the drag
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                float bubbleMotion = 0.25F;
                worldObj.spawnParticle("bubble", posX - motionX * bubbleMotion, posY - motionY * bubbleMotion, posZ - motionZ * bubbleMotion, motionX, motionY, motionZ);
            }
            drag = 0.8F;
        }
        motionX *= drag;
        motionY *= drag;
        motionZ *= drag;
        motionY -= gravity * type.fallSpeed;

        //Apply homing action
        if (lockedOnTo != null) {
            double dX = lockedOnTo.posX - posX;
            double dY = lockedOnTo.posY - posY;
            double dZ = lockedOnTo.posZ - posZ;
            double dXYZ = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

            Vector3f relPosVec = new Vector3f(lockedOnTo.posX - posX, lockedOnTo.posY - posY, lockedOnTo.posZ - posZ);
            float angle = Math.abs(Vector3f.angle(motion, relPosVec));

            double lockOnPull = angle / 2F * type.lockOnForce;

            motionX += lockOnPull * dX / dXYZ;
            motionY += lockOnPull * dY / dXYZ;
            motionZ += lockOnPull * dZ / dXYZ;
        }


        //Apply motion
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        setPosition(posX, posY, posZ);

        //Recalculate the angles from the new motion
        float motionXZ = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) ((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        rotationPitch = (float) ((Math.atan2(motionY, motionXZ) * 180D) / 3.1415927410125732D);
        //Reset the range of the angles
        for (; rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) {
        }
        for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) {
        }
        for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) {
        }
        for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) {
        }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

        //Particles
        if (type.trailParticles && worldObj.isRemote && ticksInAir > 1) {
            spawnParticles();
        }

        //Temporary fire glitch fix
        if (worldObj.isRemote)
            extinguish();
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        double dX = (posX - prevPosX) / 10;
        double dY = (posY - prevPosY) / 10;
        double dZ = (posZ - prevPosZ) / 10;

        float spread = 0.1F;
        for (int i = 0; i < 10; i++) {
            EntityFX particle = FlansModClient.getParticle(type.trailParticleType, worldObj, prevPosX + dX * i + rand.nextGaussian() * spread, prevPosY + dY * i + rand.nextGaussian() * spread, prevPosZ + dZ * i + rand.nextGaussian() * spread);
            if (particle != null && Minecraft.getMinecraft().gameSettings.fancyGraphics)
                particle.renderDistanceWeight = 100D;
            //worldObj.spawnEntityInWorld(particle);
        }
    }

    public DamageSource getBulletDamage(boolean headshot) {
        if (owner instanceof EntityPlayer)
            return (new EntityDamageSourceGun(type.shortName, this, (EntityPlayer) owner, firedFrom, headshot)).setProjectile();
        else return (new EntityDamageSourceIndirect(type.shortName, this, owner)).setProjectile();
    }

    private boolean isPartOfOwner(Entity entity) {
        if (owner == null)
            return false;
        if (entity == owner || entity == owner.riddenByEntity || entity == owner.ridingEntity)
            return true;
        if (owner instanceof EntityPlayer) {
            if (PlayerHandler.getPlayerData((EntityPlayer) owner, worldObj.isRemote ? Side.CLIENT : Side.SERVER) == null)
                return false;
            EntityMG mg = PlayerHandler.getPlayerData((EntityPlayer) owner, worldObj.isRemote ? Side.CLIENT : Side.SERVER).mountingGun;
            if (mg != null && mg == entity) {
                return true;
            }
        }
        return owner.ridingEntity instanceof EntitySeat && (((EntitySeat) owner.ridingEntity).driveable == null || ((EntitySeat) owner.ridingEntity).driveable.isPartOfThis(entity));
    }

    @Override
    public void setDead() {
        if (isDead) return;
        super.setDead();
        if (worldObj.isRemote) return;
        if (type != null && type.explosionRadius > 0) {
            if (owner instanceof EntityPlayer)
                new FlansModExplosion(worldObj, this, (EntityPlayer) owner, firedFrom, posX, posY, posZ, type.explosionRadius, TeamsManager.explosions);
            else worldObj.createExplosion(this, posX, posY, posZ, type.explosionRadius, TeamsManager.explosions);
        }
        if (type != null && type.fireRadius > 0) {
            for (float i = -type.fireRadius; i < type.fireRadius; i++) {
                for (float k = -type.fireRadius; k < type.fireRadius; k++) {
                    for (int j = -1; j < 1; j++) {
                        if (worldObj.getBlock((int) (posX + i), (int) (posY + j), (int) (posZ + k)).getMaterial() == Material.air) {
                            worldObj.setBlock((int) (posX + i), (int) (posY + j), (int) (posZ + k), Blocks.fire);
                        }
                    }
                }
            }
        }
        //Send flak packet
        if (type != null && type.flak > 0) {
            FlansMod.getPacketHandler().sendToAllAround(new PacketFlak(posX, posY, posZ, type.flak, type.flakParticles), posX, posY, posZ, 200, dimension);
        }
        // Drop item on hitting if bullet requires it
        if (type != null && type.dropItemOnHit != null) {
            String itemName = type.dropItemOnHit;
            int damage = 0;
            if (itemName.contains(".")) {
                damage = Integer.parseInt(itemName.split("\\.")[1]);
                itemName = itemName.split("\\.")[0];
            }
            ItemStack dropStack = InfoType.getRecipeElement(itemName, damage);
            entityDropItem(dropStack, 1.0F);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        if (tag != null) {
            tag.setString("type", type.shortName);
            if (owner == null) tag.setString("owner", "null");
            else tag.setString("owner", owner.getCommandSenderName());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        if (tag != null) {
            String typeString = tag.getString("type");
            String ownerName = tag.getString("owner");
            if (typeString != null) type = BulletType.getBullet(typeString);
            if (ownerName != null && !ownerName.equals("null")) {
                owner = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(ownerName);
            }
        }
    }

    @Override
    public float getShadowSize() {
        return type.hitBoxSize;
    }

    public int getBrightnessForRender(float par1) {
        if (type.hasLight) {
            return 15728880;
        } else {
            int i = MathHelper.floor_double(this.posX);
            int j = MathHelper.floor_double(this.posZ);

            if (this.worldObj.blockExists(i, 0, j)) {
                double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D;
                int k = MathHelper.floor_double(this.posY - (double) this.yOffset + d0);
                return this.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
            } else {
                return 0;
            }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeDouble(motionX);
        data.writeDouble(motionY);
        data.writeDouble(motionZ);
        data.writeInt(lockedOnTo == null ? -1 : lockedOnTo.getEntityId());
        ByteBufUtils.writeUTF8String(data, type.shortName);
        if (owner == null)
            ByteBufUtils.writeUTF8String(data, "null");
        else
            ByteBufUtils.writeUTF8String(data, owner.getCommandSenderName());
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        try {
            motionX = data.readDouble();
            motionY = data.readDouble();
            motionZ = data.readDouble();
            int lockedOnToID = data.readInt();
            if (lockedOnToID != -1)
                lockedOnTo = worldObj.getEntityByID(lockedOnToID);
            type = BulletType.getBullet(ByteBufUtils.readUTF8String(data));
            penetratingPower = type.penetratingPower;
            String name = ByteBufUtils.readUTF8String(data);
            for (Object obj : worldObj.loadedEntityList) {
                if (((Entity) obj).getCommandSenderName().equals(name))
                    owner = (EntityPlayer) obj;
            }
        } catch (Exception e) {
            FlansMod.log("Failed to read bullet owner from server.");
            super.setDead();
            e.printStackTrace();
        }
    }

    @Override
    public boolean isBurning() {
        return false;
    }
}
