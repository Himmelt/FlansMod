package co.uk.flansmods.client.model;

import co.uk.flansmods.client.FlansModClient;
import co.uk.flansmods.client.FlansModResourceHandler;
import co.uk.flansmods.common.guns.AttachmentType;
import co.uk.flansmods.common.guns.GunType;
import co.uk.flansmods.common.guns.ItemBullet;
import co.uk.flansmods.common.guns.ItemGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderGun implements IItemRenderer {
    private static TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

    public static float smoothing;

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        switch (type) {
            case ENTITY:
                if (!Minecraft.getMinecraft().gameSettings.fancyGraphics) return false;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:  /*case INVENTORY : */
                return item != null && item.getItem() instanceof ItemGun && ((ItemGun) item.getItem()).type.model != null;
        }
        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        //Avoid any broken cases by returning
        if (!(item.getItem() instanceof ItemGun))
            return;

        GunType gunType = ((ItemGun) item.getItem()).type;
        if (gunType == null)
            return;

        ModelGun model = gunType.model;
        if (model == null)
            return;

        //The model scale
        float f = 1F / 16F;

        GunAnimations animations = FlansModClient.gunAnimations.get(data[1]);
        if (animations == null) {
            animations = new GunAnimations();
            if (type != ItemRenderType.ENTITY)
                FlansModClient.gunAnimations.put((EntityLivingBase) data[1], animations);
        }

        GL11.glPushMatrix();
        {
            //Get the reload animation rotation
            float reloadRotate = 0F;

            //Setup transforms based on gun position
            switch (type) {
                case ENTITY: {
                    EntityItem entity = (EntityItem) data[1];
                    GL11.glRotatef(entity.age + (entity.age == 0 ? 0 : smoothing), 0F, 1F, 0F);
                    break;
                }
                case EQUIPPED: {
                    GL11.glRotatef(35F, 0F, 0F, 1F);
                    GL11.glRotatef(-5F, 0F, 1F, 0F);
                    GL11.glTranslatef(0.75F, -0.22F, -0.08F);
                    GL11.glScalef(1F, 1F, -1F);
                    break;
                }
                case EQUIPPED_FIRST_PERSON: {
                    float adsSwitch = FlansModClient.lastZoomProgress + (FlansModClient.zoomProgress - FlansModClient.lastZoomProgress) * smoothing;//0F;//((float)Math.sin((FlansMod.ticker) / 10F) + 1F) / 2F;
                    GL11.glRotatef(25F - 5F * adsSwitch, 0F, 0F, 1F);
                    GL11.glRotatef(-5F, 0F, 1F, 0F);
                    GL11.glTranslatef(0.15F, 0.2F + 0.175F * adsSwitch, -0.6F - 0.405F * adsSwitch);
                    GL11.glRotatef(4.5F * adsSwitch, 0F, 0F, 1F);
                    GL11.glTranslatef(0F, -0.03F * adsSwitch, 0F);

                    if (animations.reloading) {
                        //Calculate the amount of tilt required for the reloading animation
                        float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
                        reloadRotate = 1F;
                        if (effectiveReloadAnimationProgress < model.tiltGunTime)
                            reloadRotate = effectiveReloadAnimationProgress / model.tiltGunTime;
                        if (effectiveReloadAnimationProgress > model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
                            reloadRotate = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime + model.loadClipTime)) / model.untiltGunTime;

                        //Rotate the gun dependent on the animation type
                        switch (model.animationType) {
                            case BOTTOM_CLIP:
                            case PISTOL_CLIP:
                            case SHOTGUN:
                            case END_LOADED: {
                                GL11.glRotatef(60F * reloadRotate, 0F, 0F, 1F);
                                GL11.glRotatef(30F * reloadRotate, 1F, 0F, 0F);
                                GL11.glTranslatef(0.25F * reloadRotate, 0F, 0F);
                                break;
                            }
                            case RIFLE: {
                                GL11.glRotatef(30F * reloadRotate, 0F, 0F, 1F);
                                GL11.glRotatef(-30F * reloadRotate, 1F, 0F, 0F);
                                GL11.glTranslatef(0.5F * reloadRotate, 0F, -0.5F * reloadRotate);
                                break;
                            }

                        }
                    }
                    break;
                }
            }

            renderGun(item, gunType, f, model, animations, reloadRotate);
        }
        GL11.glPopMatrix();
    }

    /**
     * Gun render method, seperated from transforms so that mecha renderer may also call this
     */
    public void renderGun(ItemStack item, GunType type, float f, ModelGun model, GunAnimations animations, float reloadRotate) {
        //If we have no animation variables, use defaults
        if (animations == null)
            animations = GunAnimations.defaults;

        //Get all the attachments that we may need to render
        AttachmentType scopeAttachment = type.getScope(item);
        AttachmentType barrelAttachment = type.getBarrel(item);
        AttachmentType stockAttachment = type.getStock(item);
        AttachmentType gripAttachment = type.getGrip(item);

        ItemStack[] bulletStacks = new ItemStack[type.numAmmoItemsInGun];
        boolean empty = true;
        for (int i = 0; i < type.numAmmoItemsInGun; i++) {
            bulletStacks[i] = ((ItemGun) item.getItem()).getBulletItemStack(item, i);
            if (bulletStacks[i] != null && bulletStacks[i].getItem() instanceof ItemBullet && bulletStacks[i].getItemDamage() < bulletStacks[i].getMaxDamage())
                empty = false;
        }


        //Load texture
        renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));

        if (scopeAttachment != null)
            GL11.glTranslatef(0F, -scopeAttachment.model.renderOffset / 16F, 0F);

        //Render the gun and default attachment models
        GL11.glPushMatrix();
        {
            GL11.glScalef(type.modelScale, type.modelScale, type.modelScale);

            model.renderGun(f);
            if (scopeAttachment == null && !model.scopeIsOnSlide)
                model.renderDefaultScope(f);
            if (barrelAttachment == null)
                model.renderDefaultBarrel(f);
            if (stockAttachment == null)
                model.renderDefaultStock(f);
            if (gripAttachment == null)
                model.renderDefaultGrip(f);

            //Render various shoot / reload animated parts
            //Render the slide
            GL11.glPushMatrix();
            {
                GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
                model.renderSlide(f);
                if (scopeAttachment == null && model.scopeIsOnSlide)
                    model.renderDefaultScope(f);
            }
            GL11.glPopMatrix();

            //Render the pump-action handle
            GL11.glPushMatrix();
            {
                GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
                model.renderPump(f);
            }
            GL11.glPopMatrix();

            //Render the cocking handle

            //Render the clip
            GL11.glPushMatrix();
            {
                boolean shouldRender = true;
                //Check to see if the ammo should be rendered first
                switch (model.animationType) {
                    case END_LOADED: {
                        if (empty)
                            shouldRender = false;
                        break;
                    }
                }
                //If it should be rendered, do the transformations required
                if (shouldRender && animations.reloading && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                    //Calculate the amount of tilt required for the reloading animation
                    float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
                    float clipPosition = 0F;
                    if (effectiveReloadAnimationProgress > model.tiltGunTime && effectiveReloadAnimationProgress < model.tiltGunTime + model.unloadClipTime)
                        clipPosition = (effectiveReloadAnimationProgress - model.tiltGunTime) / model.unloadClipTime;
                    if (effectiveReloadAnimationProgress >= model.tiltGunTime + model.unloadClipTime && effectiveReloadAnimationProgress < model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
                        clipPosition = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime)) / model.loadClipTime;

                    //Rotate the gun dependent on the animation type
                    switch (model.animationType) {
                        case BOTTOM_CLIP: {
                            GL11.glRotatef(-180F * clipPosition, 0F, 0F, 1F);
                            GL11.glRotatef(60F * clipPosition, 1F, 0F, 0F);
                            GL11.glTranslatef(0.5F * clipPosition, 0F, 0F);
                            break;
                        }
                        case PISTOL_CLIP: {
                            GL11.glRotatef(-90F * clipPosition * clipPosition, 0F, 0F, 1F);
                            GL11.glTranslatef(0F, -1F * clipPosition, 0F);
                            break;
                        }
                        case P90: {
                            GL11.glRotatef(-15F * reloadRotate * reloadRotate, 0F, 0F, 1F);
                            GL11.glTranslatef(0F, 0.075F * reloadRotate, 0F);
                            GL11.glTranslatef(-2F * clipPosition, -0.3F * clipPosition, 0.5F * clipPosition);
                            break;
                        }
                        case RIFLE: {
                            float thing = clipPosition * model.numBulletsInReloadAnimation;
                            int bulletNum = MathHelper.floor_float(thing);
                            float bulletProgress = thing - bulletNum;

                            GL11.glRotatef(bulletProgress * 15F, 0F, 1F, 0F);
                            GL11.glRotatef(bulletProgress * 15F, 0F, 0F, 1F);
                            GL11.glTranslatef(bulletProgress * -1F, 0F, bulletProgress * 0.5F);

                            break;
                        }
                        case SHOTGUN: {
                            float thing = clipPosition * model.numBulletsInReloadAnimation;
                            int bulletNum = MathHelper.floor_float(thing);
                            float bulletProgress = thing - bulletNum;

                            GL11.glRotatef(bulletProgress * -30F, 0F, 0F, 1F);
                            GL11.glTranslatef(bulletProgress * -0.5F, bulletProgress * -1F, 0F);

                            break;
                        }
                        case END_LOADED: {
                            float bulletProgress = 1F;
                            if (effectiveReloadAnimationProgress > model.tiltGunTime)
                                bulletProgress = 1F - Math.min((effectiveReloadAnimationProgress - model.tiltGunTime) / (model.unloadClipTime + model.loadClipTime), 1);

                            GL11.glTranslatef(bulletProgress, 0F, 0F);

                            if (bulletProgress > 0.5F) {
                                GL11.glTranslatef(-3F * (bulletProgress - 0.5F), 0F, 0F);
                                GL11.glRotatef(-180F * (bulletProgress - 0.5F), 0F, 0F, 1F);
                            }
                            break;
                        }
                    }
                }

                if (shouldRender)
                    model.renderAmmo(f);
            }
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        //Render static attachments
        //Scope
        if (scopeAttachment != null) {
            GL11.glPushMatrix();
            {
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(scopeAttachment));
                GL11.glTranslatef(model.scopeAttachPoint.x * type.modelScale, model.scopeAttachPoint.y * type.modelScale, model.scopeAttachPoint.z * type.modelScale);
                if (model.scopeIsOnSlide)
                    GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
                GL11.glScalef(scopeAttachment.modelScale, scopeAttachment.modelScale, scopeAttachment.modelScale);
                ModelAttachment scopeModel = scopeAttachment.model;
                if (scopeModel != null)
                    scopeModel.renderAttachment(f);
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
            }
            GL11.glPopMatrix();
        }

        //Barrel
        if (barrelAttachment != null) {
            GL11.glPushMatrix();
            {
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(barrelAttachment));
                GL11.glTranslatef(model.barrelAttachPoint.x * type.modelScale, model.barrelAttachPoint.y * type.modelScale, model.barrelAttachPoint.z * type.modelScale);
                GL11.glScalef(barrelAttachment.modelScale, barrelAttachment.modelScale, barrelAttachment.modelScale);
                ModelAttachment barrelModel = barrelAttachment.model;
                if (barrelModel != null)
                    barrelModel.renderAttachment(f);
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
            }
            GL11.glPopMatrix();
        }

        //Scope
        if (stockAttachment != null) {
            GL11.glPushMatrix();
            {
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(stockAttachment));
                GL11.glTranslatef(model.stockAttachPoint.x * type.modelScale, model.stockAttachPoint.y * type.modelScale, model.stockAttachPoint.z * type.modelScale);
                GL11.glScalef(stockAttachment.modelScale, stockAttachment.modelScale, stockAttachment.modelScale);
                ModelAttachment stockModel = stockAttachment.model;
                if (stockModel != null)
                    stockModel.renderAttachment(f);
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
            }
            GL11.glPopMatrix();
        }

        //Grip
        if (gripAttachment != null) {
            GL11.glPushMatrix();
            {
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(gripAttachment));
                GL11.glTranslatef(model.gripAttachPoint.x * type.modelScale, model.gripAttachPoint.y * type.modelScale, model.gripAttachPoint.z * type.modelScale);
                GL11.glScalef(gripAttachment.modelScale, gripAttachment.modelScale, gripAttachment.modelScale);
                ModelAttachment gripModel = gripAttachment.model;
                if (gripModel != null)
                    gripModel.renderAttachment(f);
                renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
            }
            GL11.glPopMatrix();
        }
    }
}
