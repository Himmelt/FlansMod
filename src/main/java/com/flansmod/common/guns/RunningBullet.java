package com.flansmod.common.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.debug.EntityDebugDot;
import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.RotatedAxes;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntityPlane;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.driveables.EntityVehicle;
import com.flansmod.common.driveables.mechas.EntityMecha;
import com.flansmod.common.guns.raytracing.*;
import com.flansmod.common.network.PacketFlak;
import com.flansmod.common.teams.Team;
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
import net.minecraft.entity.Entity.EnumEntitySize;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class RunningBullet implements IEntityAdditionalSpawnData {
    public RunningBulletManager manager;
    private static int bulletLife = 600;
    public Entity owner;
    private int ticksInAir;
    public BulletType type;
    public InfoType firedFrom;
    public float damage;
    public boolean shotgun;
    public int pingOfShooter;
    public static Random bulletRandom = new Random();
    public Entity lockedOnTo;
    public float penetratingPower;
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public World worldObj;
    public final AxisAlignedBB boundingBox;
    private boolean firstUpdate;
    public EnumEntitySize myEntitySize;
    public float width;
    public float height;
    public int ticksExisted;
    public boolean isDead;
    private int fire;
    public float prevRotationYaw;
    public float prevRotationPitch;
    public float rotationYaw;
    public float rotationPitch;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double lastTickPosX;
    public double lastTickPosY;
    public double lastTickPosZ;
    public int chunkCoordX;
    public int chunkCoordY;
    public int chunkCoordZ;
    public float yOffset;
    public float ySize;
    protected Random rand;
    public int dimension;
    public boolean captureDrops;
    public ArrayList<EntityItem> capturedDrops;

    public RunningBullet(World world) {
        this();
        this.worldObj = world;
        this.manager = RunningBulletManager.getManager(world);
        this.manager.register(this);
        this.ticksInAir = 0;
        this.setSize(0.5F, 0.5F);
    }

    public RunningBullet() {
        this.shotgun = false;
        this.pingOfShooter = 0;
        this.captureDrops = false;
        this.capturedDrops = new ArrayList<>();
        this.boundingBox = AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        this.width = 0.6F;
        this.height = 1.8F;
        this.rand = new Random();
        this.firstUpdate = true;
        this.myEntitySize = EnumEntitySize.SIZE_2;
        this.setPosition(0.0D, 0.0D, 0.0D);
        this.entityInit();
    }

    protected void setSize(float width, float height) {
        float f2;
        if (width != this.width || height != this.height) {
            f2 = this.width;
            this.width = width;
            this.height = height;
            this.boundingBox.maxX = this.boundingBox.minX + (double) this.width;
            this.boundingBox.maxZ = this.boundingBox.minZ + (double) this.width;
            this.boundingBox.maxY = this.boundingBox.minY + (double) this.height;
            if (this.width > f2 && !this.firstUpdate && !this.worldObj.isRemote) {
            }
        }

        f2 = width % 2.0F;
        if ((double) f2 < 0.375D) {
            this.myEntitySize = EnumEntitySize.SIZE_1;
        } else if ((double) f2 < 0.75D) {
            this.myEntitySize = EnumEntitySize.SIZE_2;
        } else if ((double) f2 < 1.0D) {
            this.myEntitySize = EnumEntitySize.SIZE_3;
        } else if ((double) f2 < 1.375D) {
            this.myEntitySize = EnumEntitySize.SIZE_4;
        } else if ((double) f2 < 1.75D) {
            this.myEntitySize = EnumEntitySize.SIZE_5;
        } else {
            this.myEntitySize = EnumEntitySize.SIZE_6;
        }

    }

    private RunningBullet(World world, EntityLivingBase shooter, float gunDamage, BulletType bulletType, InfoType shotFrom) {
        this(world);
        this.owner = shooter;
        if (shooter instanceof EntityPlayerMP) {
            this.pingOfShooter = ((EntityPlayerMP) shooter).ping;
        }

        this.type = bulletType;
        this.firedFrom = shotFrom;
        this.damage = gunDamage;
        this.penetratingPower = this.type.penetratingPower;
    }

    public RunningBullet(World world, EntityLivingBase shooter, float spread, float gunDamage, BulletType type1, float speed, boolean shot, InfoType shotFrom) {
        this(world, Vec3.createVectorHelper(shooter.posX, shooter.posY + (double) shooter.getEyeHeight(), shooter.posZ), shooter.rotationYaw, shooter.rotationPitch, shooter, spread, gunDamage, type1, speed, shotFrom);
        this.shotgun = shot;
    }

    public RunningBullet(World world, Vec3 origin, float yaw, float pitch, EntityLivingBase shooter, float spread, float gunDamage, BulletType type1, InfoType shotFrom) {
        this(world, origin, yaw, pitch, shooter, spread, gunDamage, type1, 3.0F, shotFrom);
    }

    public RunningBullet(World world, Vec3 origin, float yaw, float pitch, EntityLivingBase shooter, float spread, float gunDamage, BulletType type1, float speed, InfoType shotFrom) {
        this(world, shooter, gunDamage, type1, shotFrom);
        this.setLocationAndAngles(origin.xCoord, origin.yCoord, origin.zCoord, yaw, pitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F));
        this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F));
        this.motionY = (double) (-MathHelper.sin(this.rotationPitch / 180.0F * 3.141593F));
        this.setArrowHeading(this.motionX, this.motionY, this.motionZ, spread / 2.0F, speed);
    }

    public RunningBullet(World world, Vector3f origin, Vector3f direction, EntityLivingBase shooter, float spread, float gunDamage, BulletType type1, float speed, InfoType shotFrom) {
        this(world, shooter, gunDamage, type1, shotFrom);
        this.damage = gunDamage;
        this.setPosition((double) origin.x, (double) origin.y, (double) origin.z);
        this.motionX = (double) direction.x;
        this.motionY = (double) direction.y;
        this.motionZ = (double) direction.z;
        this.setArrowHeading(this.motionX, this.motionY, this.motionZ, spread, speed);
    }

    public RunningBullet(World world, Vec3 origin, float yaw, float pitch, double motX, double motY, double motZ, EntityLivingBase shooter, float gunDamage, BulletType type1, InfoType shotFrom) {
        this(world, shooter, gunDamage, type1, shotFrom);
        this.setLocationAndAngles(origin.xCoord, origin.yCoord, origin.zCoord, yaw, pitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = motX;
        this.motionY = motY;
        this.motionZ = motZ;
    }

    public void setLocationAndAngles(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
        this.lastTickPosX = this.prevPosX = this.posX = p_70012_1_;
        this.lastTickPosY = this.prevPosY = this.posY = p_70012_3_ + (double) this.yOffset;
        this.lastTickPosZ = this.prevPosZ = this.posZ = p_70012_5_;
        this.rotationYaw = p_70012_7_;
        this.rotationPitch = p_70012_8_;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    protected void entityInit() {
    }

    public void setArrowHeading(double d, double d1, double d2, float spread, float speed) {
        float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
        d /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d *= (double) speed;
        d1 *= (double) speed;
        d2 *= (double) speed;
        d += this.rand.nextGaussian() * 0.005D * (double) spread * (double) speed;
        d1 += this.rand.nextGaussian() * 0.005D * (double) spread * (double) speed;
        d2 += this.rand.nextGaussian() * 0.005D * (double) spread * (double) speed;
        this.motionX = d;
        this.motionY = d1;
        this.motionZ = d2;
        float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(d, d2) * 180.0D / 3.1415927410125732D);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(d1, (double) f3) * 180.0D / 3.1415927410125732D);
        this.getLockOnTarget();
    }

    private void getLockOnTarget() {
        if (this.type.lockOnToPlanes || this.type.lockOnToVehicles || this.type.lockOnToMechas || this.type.lockOnToLivings || this.type.lockOnToPlayers) {
            Vector3f motionVec = new Vector3f(this.motionX, this.motionY, this.motionZ);
            Entity closestEntity = null;
            float closestAngle = this.type.maxLockOnAngle * 3.1415927F / 180.0F;
            Iterator i$ = this.worldObj.loadedEntityList.iterator();

            while (true) {
                Entity entity;
                do {
                    if (!i$.hasNext()) {
                        if (closestEntity != null) {
                            this.lockedOnTo = closestEntity;
                        }

                        return;
                    }

                    Object obj = i$.next();
                    entity = (Entity) obj;
                } while ((!this.type.lockOnToMechas || !(entity instanceof EntityMecha)) && (!this.type.lockOnToVehicles || !(entity instanceof EntityVehicle)) && (!this.type.lockOnToPlanes || !(entity instanceof EntityPlane)) && (!this.type.lockOnToPlayers || !(entity instanceof EntityPlayer)) && (!this.type.lockOnToLivings || !(entity instanceof EntityLivingBase)));

                Vector3f relPosVec = new Vector3f(entity.posX - this.posX, entity.posY - this.posY, entity.posZ - this.posZ);
                float angle = Math.abs(Vector3f.angle(motionVec, relPosVec));
                if (angle < closestAngle) {
                    closestEntity = entity;
                    closestAngle = angle;
                }
            }
        }
    }

    public void setVelocity(double d, double d1, double d2) {
    }

    public float getDistanceToEntity(Entity p_70032_1_) {
        float f = (float) (this.posX - p_70032_1_.posX);
        float f1 = (float) (this.posY - p_70032_1_.posY);
        float f2 = (float) (this.posZ - p_70032_1_.posZ);
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    public void onUpdate() {
        ++this.ticksExisted;
        ++this.ticksInAir;
        if (this.ticksInAir > this.type.fuse && this.type.fuse > 0 && !this.isDead) {
            this.setDead();
        }

        if (this.ticksExisted > bulletLife) {
            this.setDead();
        }

        if (!this.isDead) {
            ArrayList<BulletHit> hits = new ArrayList<>();
            Vector3f origin = new Vector3f(this.posX, this.posY, this.posZ);
            Vector3f motion = new Vector3f(this.motionX, this.motionY, this.motionZ);
            float speed = motion.length();

            float hitLambda;
            int snapshotToTry;
            float baseDamage;
            for (int i = 0; i < this.worldObj.loadedEntityList.size(); ++i) {
                Object obj = this.worldObj.loadedEntityList.get(i);
                if (obj instanceof EntityDriveable) {
                    EntityDriveable driveable = (EntityDriveable) obj;
                    if (!driveable.isDead() && !driveable.isPartOfThis(this.owner) && this.getDistanceToEntity(driveable) <= driveable.getDriveableType().bulletDetectionRadius + speed) {
                        ArrayList<BulletHit> driveableHits = driveable.attackFromBullet(origin, motion);
                        hits.addAll(driveableHits);
                    }
                } else if (obj instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) obj;
                    PlayerData data = PlayerHandler.getPlayerData(player);
                    boolean shouldDoNormalHitDetect = false;
                    if (data != null) {
                        if (player.isDead || data.team == Team.spectators || player == this.owner && this.ticksInAir < 20) {
                            continue;
                        }

                        snapshotToTry = this.pingOfShooter / 50;
                        if (snapshotToTry >= data.snapshots.length) {
                            snapshotToTry = data.snapshots.length - 1;
                        }

                        PlayerSnapshot snapshot = data.snapshots[snapshotToTry];
                        if (snapshot == null) {
                            snapshot = data.snapshots[0];
                        }

                        if (snapshot == null) {
                            shouldDoNormalHitDetect = true;
                        } else {
                            ArrayList<BulletHit> playerHits = snapshot.raytrace(origin, motion);
                            hits.addAll(playerHits);
                        }
                    }

                    if (data == null || shouldDoNormalHitDetect) {
                        MovingObjectPosition mop = player.boundingBox.calculateIntercept(origin.toVec3(), Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ));
                        if (mop != null) {
                            Vector3f hitPoint = new Vector3f(mop.hitVec.xCoord - this.posX, mop.hitVec.yCoord - this.posY, mop.hitVec.zCoord - this.posZ);
                            baseDamage = 1.0F;
                            if (motion.x != 0.0F) {
                                baseDamage = hitPoint.x / motion.x;
                            } else if (motion.y != 0.0F) {
                                baseDamage = hitPoint.y / motion.y;
                            } else if (motion.z != 0.0F) {
                                baseDamage = hitPoint.z / motion.z;
                            }

                            if (baseDamage < 0.0F) {
                                baseDamage = -baseDamage;
                            }

                            hits.add(new PlayerBulletHit(new PlayerHitbox(player, new RotatedAxes(), new Vector3f(), new Vector3f(), new Vector3f(), EnumHitboxType.BODY), baseDamage));
                        }
                    }
                } else {
                    Entity entity = (Entity) obj;
                    if (entity != this.owner && !entity.isDead && (entity instanceof EntityLivingBase || entity instanceof EntityAAGun || entity instanceof EntityGrenade)) {
                        MovingObjectPosition mop = entity.boundingBox.calculateIntercept(origin.toVec3(), Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ));
                        if (mop != null) {
                            Vector3f hitPoint = new Vector3f(mop.hitVec.xCoord - this.posX, mop.hitVec.yCoord - this.posY, mop.hitVec.zCoord - this.posZ);
                            hitLambda = 1.0F;
                            if (motion.x != 0.0F) {
                                hitLambda = hitPoint.x / motion.x;
                            } else if (motion.y != 0.0F) {
                                hitLambda = hitPoint.y / motion.y;
                            } else if (motion.z != 0.0F) {
                                hitLambda = hitPoint.z / motion.z;
                            }

                            if (hitLambda < 0.0F) {
                                hitLambda = -hitLambda;
                            }

                            hits.add(new EntityHit(entity, hitLambda));
                        }
                    }
                }
            }

            Vec3 posVec = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 nextPosVec = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition hit = this.worldObj.func_147447_a(posVec, nextPosVec, false, true, true);
            posVec = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            float lambda;
            if (hit != null) {
                Vec3 hitVec = posVec.subtract(hit.hitVec);
                lambda = 1.0F;
                if (this.motionX != 0.0D) {
                    lambda = (float) (hitVec.xCoord / this.motionX);
                } else if (this.motionY != 0.0D) {
                    lambda = (float) (hitVec.yCoord / this.motionY);
                } else if (this.motionZ != 0.0D) {
                    lambda = (float) (hitVec.zCoord / this.motionZ);
                }

                if (lambda < 0.0F) {
                    lambda = -lambda;
                }

                hits.add(new BlockHit(hit, lambda));
            }

            if (!hits.isEmpty()) {
                Collections.sort(hits);

                for (BulletHit bulletHit : hits) {
                    if (!(bulletHit instanceof DriveableHit)) {
                        if (bulletHit instanceof PlayerBulletHit) {
                            PlayerBulletHit playerHit = (PlayerBulletHit) bulletHit;
                            this.penetratingPower = playerHit.hitbox.hitByBullet(this, this.penetratingPower);
                            if (FlansMod.DEBUG) {
                                this.worldObj.spawnEntityInWorld(new EntityDebugDot(this.worldObj, new Vector3f(this.posX + this.motionX * (double) playerHit.intersectTime, this.posY + this.motionY * (double) playerHit.intersectTime, this.posZ + this.motionZ * (double) playerHit.intersectTime), 1000, 1.0F, 0.0F, 0.0F));
                            }
                        } else if (bulletHit instanceof EntityHit) {
                            EntityHit entityHit = (EntityHit) bulletHit;
                            DamageSource source = this.getBulletDamage(false);
                            baseDamage = this.damage * (float) this.type.damageVsLiving;
                            List<PotionEffect> effects = new ArrayList<>();

                            for (PotionEffect effect : this.type.hitEffects) {
                                effects.add(new PotionEffect(effect));
                            }

                            if (entityHit.entity.attackEntityFrom(source, baseDamage) && entityHit.entity instanceof EntityLivingBase) {
                                EntityLivingBase living = (EntityLivingBase) entityHit.entity;

                                for (PotionEffect effect : effects) {
                                    living.addPotionEffect(effect);
                                }

                                ++living.arrowHitTimer;
                                living.hurtResistantTime = living.maxHurtResistantTime / 2;
                            }

                            if (FlansMod.DEBUG) {
                                this.worldObj.spawnEntityInWorld(new EntityDebugDot(this.worldObj, new Vector3f(this.posX + this.motionX * (double) entityHit.intersectTime, this.posY + this.motionY * (double) entityHit.intersectTime, this.posZ + this.motionZ * (double) entityHit.intersectTime), 1000, 1.0F, 1.0F, 0.0F));
                            }
                        } else if (bulletHit instanceof BlockHit) {
                            BlockHit blockHit = (BlockHit) bulletHit;
                            MovingObjectPosition raytraceResult = blockHit.raytraceResult;
                            int xTile = raytraceResult.blockX;
                            int yTile = raytraceResult.blockY;
                            int zTile = raytraceResult.blockZ;
                            if (FlansMod.DEBUG) {
                                this.worldObj.spawnEntityInWorld(new EntityDebugDot(this.worldObj, new Vector3f(raytraceResult.hitVec.xCoord, raytraceResult.hitVec.yCoord, raytraceResult.hitVec.zCoord), 1000, 0.0F, 1.0F, 0.0F));
                            }

                            Block block = this.worldObj.getBlock(xTile, yTile, zTile);
                            Material mat = block.getMaterial();
                            int oreId = OreDictionary.getOreID(new ItemStack(block));
                            if (oreId != -1) {
                                String oreType = OreDictionary.getOreName(oreId);
                                if (oreType.equals("treeLeaves")) {
                                    continue;
                                }
                            }

                            if (this.type.breaksGlass && mat == Material.glass && TeamsManager.canBreakGlass) {
                                this.worldObj.setBlockToAir(xTile, yTile, zTile);
                                FlansMod.proxy.playBlockBreakSound(xTile, yTile, zTile, block);
                            }

                            this.setPosition(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
                            this.setDead();
                            break;
                        }
                    }

                    if (this.penetratingPower <= 0.0F || this.type.explodeOnImpact && this.ticksInAir > 1) {
                        this.setPosition(this.posX + this.motionX * (double) bulletHit.intersectTime, this.posY + this.motionY * (double) bulletHit.intersectTime, this.posZ + this.motionZ * (double) bulletHit.intersectTime);
                        this.setDead();
                        break;
                    }
                }
            }

            float drag = 0.99F;
            lambda = 0.02F;
            if (this.isInWater()) {
                for (snapshotToTry = 0; snapshotToTry < 4; ++snapshotToTry) {
                    float bubbleMotion = 0.25F;
                    this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double) bubbleMotion, this.posY - this.motionY * (double) bubbleMotion, this.posZ - this.motionZ * (double) bubbleMotion, this.motionX, this.motionY, this.motionZ);
                }

                drag = 0.8F;
            }

            this.motionX *= (double) drag;
            this.motionY *= (double) drag;
            this.motionZ *= (double) drag;
            this.motionY -= (double) (lambda * this.type.fallSpeed);
            if (this.lockedOnTo != null) {
                double dX = this.lockedOnTo.posX - this.posX;
                double dY = this.lockedOnTo.posY - this.posY;
                double dZ = this.lockedOnTo.posZ - this.posZ;
                double dXYZ = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
                Vector3f relPosVec = new Vector3f(this.lockedOnTo.posX - this.posX, this.lockedOnTo.posY - this.posY, this.lockedOnTo.posZ - this.posZ);
                float angle = Math.abs(Vector3f.angle(motion, relPosVec));
                double lockOnPull = (double) (angle / 2.0F * this.type.lockOnForce);
                this.motionX += lockOnPull * dX / dXYZ;
                this.motionY += lockOnPull * dY / dXYZ;
                this.motionZ += lockOnPull * dZ / dXYZ;
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.setPosition(this.posX, this.posY, this.posZ);
            hitLambda = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.1415927410125732D);

            for (this.rotationPitch = (float) (Math.atan2(this.motionY, (double) hitLambda) * 180.0D / 3.1415927410125732D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            }

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            if (this.type.trailParticles && this.worldObj.isRemote && this.ticksInAir > 1) {
                this.spawnParticles();
            }

            if (this.worldObj.isRemote) {
                this.extinguish();
            }
        }
    }

    public void extinguish() {
        this.fire = 0;
    }

    public void setPosition(double p_70107_1_, double p_70107_3_, double p_70107_5_) {
        this.posX = p_70107_1_;
        this.posY = p_70107_3_;
        this.posZ = p_70107_5_;
        float f = this.width / 2.0F;
        float f1 = this.height;
        this.boundingBox.setBounds(p_70107_1_ - (double) f, p_70107_3_ - (double) this.yOffset + (double) this.ySize, p_70107_5_ - (double) f, p_70107_1_ + (double) f, p_70107_3_ - (double) this.yOffset + (double) this.ySize + (double) f1, p_70107_5_ + (double) f);
    }

    public boolean isInWater() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        double dX = (this.posX - this.prevPosX) / 10.0D;
        double dY = (this.posY - this.prevPosY) / 10.0D;
        double dZ = (this.posZ - this.prevPosZ) / 10.0D;
        float spread = 0.1F;

        for (int i = 0; i < 10; ++i) {
            EntityFX particle = FlansModClient.getParticle(this.type.trailParticleType, this.worldObj, this.prevPosX + dX * (double) i + this.rand.nextGaussian() * (double) spread, this.prevPosY + dY * (double) i + this.rand.nextGaussian() * (double) spread, this.prevPosZ + dZ * (double) i + this.rand.nextGaussian() * (double) spread);
            if (particle != null && Minecraft.getMinecraft().gameSettings.fancyGraphics) {
                particle.renderDistanceWeight = 100.0D;
            }
        }
    }

    public DamageSource getBulletDamage(boolean headshot) {
        return this.owner instanceof EntityPlayer ? (new EntityDamageSourceGun(this.type.shortName, this.owner, (EntityPlayer) this.owner, this.firedFrom, headshot)).setProjectile() : (new EntityDamageSourceIndirect(this.type.shortName, this.owner, this.owner)).setProjectile();
    }

    private boolean isPartOfOwner(Entity entity) {
        if (this.owner == null) {
            return false;
        } else if (entity != this.owner && entity != this.owner.riddenByEntity && entity != this.owner.ridingEntity) {
            if (this.owner instanceof EntityPlayer) {
                if (PlayerHandler.getPlayerData((EntityPlayer) this.owner, this.worldObj.isRemote ? Side.CLIENT : Side.SERVER) == null) {
                    return false;
                }

                EntityMG mg = PlayerHandler.getPlayerData((EntityPlayer) this.owner, this.worldObj.isRemote ? Side.CLIENT : Side.SERVER).mountingGun;
                if (mg != null && mg == entity) {
                    return true;
                }
            }

            return this.owner.ridingEntity instanceof EntitySeat && (((EntitySeat) this.owner.ridingEntity).driveable == null || ((EntitySeat) this.owner.ridingEntity).driveable.isPartOfThis(entity));
        } else {
            return true;
        }
    }

    public void setDead() {
        if (!this.isDead) {
            if (!this.worldObj.isRemote) {
                String itemName;
                if (this.type.explosionRadius > 0.0F) {
                    itemName = this.worldObj.getWorldInfo().getWorldName();
                    if (this.owner instanceof EntityPlayer) {
                        new FlansModExplosion(this.worldObj, this, (EntityPlayer) this.owner, this.firedFrom, this.posX, this.posY, this.posZ, this.type.explosionRadius, TeamsManager.explosions);
                    } else {
                        this.worldObj.createExplosion((Entity) null, this.posX, this.posY, this.posZ, this.type.explosionRadius, TeamsManager.explosions);
                    }
                }

                if (this.type.fireRadius > 0.0F) {
                    for (float i = -this.type.fireRadius; i < this.type.fireRadius; ++i) {
                        for (float k = -this.type.fireRadius; k < this.type.fireRadius; ++k) {
                            for (int j = -1; j < 1; ++j) {
                                if (this.worldObj.getBlock((int) (this.posX + (double) i), (int) (this.posY + (double) j), (int) (this.posZ + (double) k)).getMaterial() == Material.air) {
                                    this.worldObj.setBlock((int) (this.posX + (double) i), (int) (this.posY + (double) j), (int) (this.posZ + (double) k), Blocks.fire);
                                }
                            }
                        }
                    }
                }

                if (this.type.flak > 0) {
                    FlansMod.getPacketHandler().sendToAllAround(new PacketFlak(this.posX, this.posY, this.posZ, this.type.flak, this.type.flakParticles), this.posX, this.posY, this.posZ, 200.0F, this.dimension);
                }

                if (this.type.dropItemOnHit != null) {
                    itemName = this.type.dropItemOnHit;
                    int damage = 0;
                    if (itemName.contains(".")) {
                        damage = Integer.parseInt(itemName.split("\\.")[1]);
                        itemName = itemName.split("\\.")[0];
                    }

                    ItemStack dropStack = InfoType.getRecipeElement(itemName, damage);
                    this.entityDropItem(dropStack, 1.0F);
                }

                this.manager.remove();
            }
        }
    }

    public EntityItem entityDropItem(ItemStack p_70099_1_, float p_70099_2_) {
        if (p_70099_1_.stackSize != 0 && p_70099_1_.getItem() != null) {
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY + (double) p_70099_2_, this.posZ, p_70099_1_);
            entityitem.delayBeforeCanPickup = 10;
            if (this.captureDrops) {
                this.capturedDrops.add(entityitem);
            } else {
                this.worldObj.spawnEntityInWorld(entityitem);
            }

            return entityitem;
        } else {
            return null;
        }
    }

    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("type", this.type.shortName);
        if (this.owner == null) {
            tag.setString("owner", "null");
        } else {
            tag.setString("owner", this.owner.getCommandSenderName());
        }

    }

    public void readEntityFromNBT(NBTTagCompound tag) {
        String typeString = tag.getString("type");
        String ownerName = tag.getString("owner");
        if (typeString != null) {
            this.type = BulletType.getBullet(typeString);
        }

        if (ownerName != null && !ownerName.equals("null")) {
            this.owner = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(ownerName);
        }

    }

    public float getShadowSize() {
        return this.type.hitBoxSize;
    }

    public int getBrightnessForRender(float par1) {
        if (this.type.hasLight) {
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

    public void writeSpawnData(ByteBuf data) {
        data.writeDouble(this.motionX);
        data.writeDouble(this.motionY);
        data.writeDouble(this.motionZ);
        data.writeInt(this.lockedOnTo == null ? -1 : this.lockedOnTo.getEntityId());
        ByteBufUtils.writeUTF8String(data, this.type.shortName);
        if (this.owner == null) {
            ByteBufUtils.writeUTF8String(data, "null");
        } else {
            ByteBufUtils.writeUTF8String(data, this.owner.getCommandSenderName());
        }

    }

    public void readSpawnData(ByteBuf data) {
        try {
            this.motionX = data.readDouble();
            this.motionY = data.readDouble();
            this.motionZ = data.readDouble();
            int lockedOnToID = data.readInt();
            if (lockedOnToID != -1) {
                this.lockedOnTo = this.worldObj.getEntityByID(lockedOnToID);
            }

            this.type = BulletType.getBullet(ByteBufUtils.readUTF8String(data));
            this.penetratingPower = this.type.penetratingPower;
            String name = ByteBufUtils.readUTF8String(data);
            Iterator i$ = this.worldObj.loadedEntityList.iterator();

            while (i$.hasNext()) {
                Object obj = i$.next();
                if (((Entity) obj).getCommandSenderName().equals(name)) {
                    this.owner = (EntityPlayer) obj;
                }
            }
        } catch (Exception var6) {
            FlansMod.log("Failed to read bullet owner from server.");
            this.setDead();
            var6.printStackTrace();
        }

    }

    public boolean isBurning() {
        return false;
    }
}
