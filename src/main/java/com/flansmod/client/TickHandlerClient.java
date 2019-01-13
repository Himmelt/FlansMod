package com.flansmod.client;

import com.flansmod.client.gui.GuiTeamScores;
import com.flansmod.client.render.RenderFlag;
import com.flansmod.client.render.RenderGun;
import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.driveables.mechas.EntityMecha;
import com.flansmod.common.guns.AttachmentType;
import com.flansmod.common.guns.EntityBullet;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.network.PacketTeamInfo;
import com.flansmod.common.teams.ItemTeamArmour;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.vector.Vector3i;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.flansmod.client.RenderHelper.drawModalRectWithCustomSizedTexture;
import static com.flansmod.client.RenderHelper.drawScaledCustomSizeModalRect;

public class TickHandlerClient {

    private final Minecraft theMc = Minecraft.getMinecraft();

    private static int lightOverrideRefreshRate = 5;
    private static ResourceLocation crossHair = null;
    private static ArrayList<Vector3i> blockLightOverrides = new ArrayList<>();
    private static RenderItem itemRenderer = new RenderItem();
    private static List<KillMessage> killMessages = new ArrayList<>();
    private static final ResourceLocation offHand = new ResourceLocation("flansmod", "gui/offHand.png");
    private static final ResourceLocation defaultCrossHair = new ResourceLocation("flansmod", "textures/crosshairs/defaultCrosshair.png");

    public TickHandlerClient() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void eventHandler(MouseEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemGun) {
            if (((ItemGun) player.getCurrentEquippedItem().getItem()).type.oneHanded && Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode()) && Math.abs(event.dwheel) > 0) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void eventHandler(RenderGameOverlayEvent event) {
        if (event.type == ElementType.CROSSHAIRS) {
            if (FlansModClient.currentScope == null) {
                int width = event.resolution.getScaledWidth();
                int height = event.resolution.getScaledHeight();
                if (FlansModClient.gunType != null) {
                    drawTenCrossHair(width, height, FlansModClient.crossRadius, FlansModClient.gunType);
                    event.setCanceled(true);
                    return;
                }
            } else {
                event.setCanceled(true);
                return;
            }
        }

        ScaledResolution scaledresolution = new ScaledResolution(FlansModClient.minecraft, FlansModClient.minecraft.displayWidth, FlansModClient.minecraft.displayHeight);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();

        Tessellator tessellator = Tessellator.instance;

        if (!event.isCancelable() && event.type == ElementType.HELMET) {
            //Scopes and helmet overlays
            String overlayTexture = null;
            if (FlansModClient.currentScope != null && FlansModClient.currentScope.hasZoomOverlay() && FMLClientHandler.instance().getClient().currentScreen == null && FlansModClient.zoomProgress > 0.8F) {
                overlayTexture = FlansModClient.currentScope.getZoomOverlay();
            } else if (theMc.thePlayer != null) {
                ItemStack stack = theMc.thePlayer.inventory.armorInventory[3];
                if (stack != null && stack.getItem() instanceof ItemTeamArmour) {
                    overlayTexture = ((ItemTeamArmour) stack.getItem()).type.overlay;
                }
            }

            if (overlayTexture != null) {
                FlansModClient.minecraft.entityRenderer.setupOverlayRendering();
                GL11.glEnable(3042 /* GL_BLEND */);
                GL11.glDisable(2929 /* GL_DEPTH_TEST */);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(770, 771);
                GL11.glColor4f(1F, 1F, 1F, 1.0F);
                GL11.glDisable(3008 /* GL_ALPHA_TEST */);

                theMc.renderEngine.bindTexture(FlansModResourceHandler.getScope(overlayTexture));

                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(i / 2.0 - 2 * j, j, -90D, 0.0D, 1.0D);
                tessellator.addVertexWithUV(i / 2.0 + 2 * j, j, -90D, 1.0D, 1.0D);
                tessellator.addVertexWithUV(i / 2.0 + 2 * j, 0.0D, -90D, 1.0D, 0.0D);
                tessellator.addVertexWithUV(i / 2.0 - 2 * j, 0.0D, -90D, 0.0D, 0.0D);
                tessellator.draw();
                GL11.glDepthMask(true);
                GL11.glEnable(2929 /* GL_DEPTH_TEST */);
                GL11.glEnable(3008 /* GL_ALPHA_TEST */);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        if (!event.isCancelable() && event.type == ElementType.HOTBAR) {
            //Player ammo overlay
            if (theMc.thePlayer != null) {
                ItemStack stack = theMc.thePlayer.inventory.getCurrentItem();
                if (stack != null && stack.getItem() instanceof ItemGun) {
                    ItemGun gunItem = (ItemGun) stack.getItem();
                    GunType gunType = gunItem.type;
                    int x = 0;
                    for (int n = 0; n < gunType.numAmmoItemsInGun; n++) {
                        ItemStack bulletStack = ((ItemGun) stack.getItem()).getBulletItemStack(stack, n);
                        if (bulletStack != null && bulletStack.getItem() != null && bulletStack.getItemDamage() < bulletStack.getMaxDamage()) {
                            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                            drawSlotInventory(theMc.fontRenderer, bulletStack, i / 2 + 16 + x, j - 65);
                            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                            String s = (bulletStack.getMaxDamage() - bulletStack.getItemDamage()) + "/" + bulletStack.getMaxDamage();
                            if (bulletStack.getMaxDamage() == 1)
                                s = "";
                            theMc.fontRenderer.drawString(s, i / 2 + 32 + x, j - 59, 0x000000);
                            theMc.fontRenderer.drawString(s, i / 2 + 33 + x, j - 60, 0xffffff);
                            x += 16 + theMc.fontRenderer.getStringWidth(s);
                        }
                    }
                    //Render secondary gun
                    PlayerData data = PlayerHandler.getPlayerData(theMc.thePlayer, Side.CLIENT);
                    if (gunType.oneHanded && data.offHandGunSlot != 0) {
                        ItemStack offHandStack = theMc.thePlayer.inventory.getStackInSlot(data.offHandGunSlot - 1);
                        if (offHandStack != null && offHandStack.getItem() instanceof ItemGun) {
                            GunType offHandGunType = ((ItemGun) offHandStack.getItem()).type;
                            x = 0;
                            for (int n = 0; n < offHandGunType.numAmmoItemsInGun; n++) {
                                ItemStack bulletStack = ((ItemGun) offHandStack.getItem()).getBulletItemStack(offHandStack, n);
                                if (bulletStack != null && bulletStack.getItem() != null && bulletStack.getItemDamage() < bulletStack.getMaxDamage()) {
                                    //Find the string we are displaying next to the ammo item
                                    String s = (bulletStack.getMaxDamage() - bulletStack.getItemDamage()) + "/" + bulletStack.getMaxDamage();
                                    if (bulletStack.getMaxDamage() == 1)
                                        s = "";

                                    //Draw the slot and then move leftwards
                                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                                    drawSlotInventory(theMc.fontRenderer, bulletStack, i / 2 - 32 - x, j - 65);
                                    x += 16 + theMc.fontRenderer.getStringWidth(s);

                                    //Draw the string
                                    GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                                    theMc.fontRenderer.drawString(s, i / 2 - 16 - x, j - 59, 0x000000);
                                    theMc.fontRenderer.drawString(s, i / 2 - 17 - x, j - 60, 0xffffff);
                                }
                            }
                        }
                    }
                }
            }

            PacketTeamInfo teamInfo = FlansModClient.teamInfo;

            if (teamInfo != null && FlansModClient.minecraft.thePlayer != null && (PacketTeamInfo.numTeams > 0 || !PacketTeamInfo.sortedByTeam) && PacketTeamInfo.getPlayerScoreData(FlansModClient.minecraft.thePlayer.getCommandSenderName()) != null) {
                GL11.glEnable(3042 /* GL_BLEND */);
                GL11.glDisable(2929 /* GL_DEPTH_TEST */);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(770, 771);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(3008 /* GL_ALPHA_TEST */);

                theMc.renderEngine.bindTexture(GuiTeamScores.texture);

                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(i / 2 - 43, 27, -90D, 85D / 256D, 27D / 256D);
                tessellator.addVertexWithUV(i / 2 + 43, 27, -90D, 171D / 256D, 27D / 256D);
                tessellator.addVertexWithUV(i / 2 + 43, 0D, -90D, 171D / 256D, 0D / 256D);
                tessellator.addVertexWithUV(i / 2 - 43, 0D, -90D, 85D / 256D, 0D / 256D);
                tessellator.draw();

                //If we are in a two team gametype, draw the team scores at the top of the screen
                if (PacketTeamInfo.numTeams == 2 && PacketTeamInfo.sortedByTeam) {
                    //Draw team 1 colour bit
                    int colour = PacketTeamInfo.teamData[0].team.teamColour;
                    GL11.glColor4f(((colour >> 16) & 0xff) / 256F, ((colour >> 8) & 0xff) / 256F, (colour & 0xff) / 256F, 1.0F);
                    tessellator.startDrawingQuads();
                    tessellator.addVertexWithUV(i / 2 - 43, 27, -90D, 0D / 256D, 125D / 256D);
                    tessellator.addVertexWithUV(i / 2 - 19, 27, -90D, 24D / 256D, 125D / 256D);
                    tessellator.addVertexWithUV(i / 2 - 19, 0D, -90D, 24D / 256D, 98D / 256D);
                    tessellator.addVertexWithUV(i / 2 - 43, 0D, -90D, 0D / 256D, 98D / 256D);
                    tessellator.draw();
                    //Draw team 2 colour bit
                    colour = PacketTeamInfo.teamData[1].team.teamColour;
                    GL11.glColor4f(((colour >> 16) & 0xff) / 256F, ((colour >> 8) & 0xff) / 256F, (colour & 0xff) / 256F, 1.0F);
                    tessellator.startDrawingQuads();
                    tessellator.addVertexWithUV(i / 2 + 19, 27, -90D, 62D / 256D, 125D / 256D);
                    tessellator.addVertexWithUV(i / 2 + 43, 27, -90D, 86D / 256D, 125D / 256D);
                    tessellator.addVertexWithUV(i / 2 + 43, 0D, -90D, 86D / 256D, 98D / 256D);
                    tessellator.addVertexWithUV(i / 2 + 19, 0D, -90D, 62D / 256D, 98D / 256D);
                    tessellator.draw();

                    GL11.glDepthMask(true);
                    GL11.glEnable(2929 /* GL_DEPTH_TEST */);
                    GL11.glEnable(3008 /* GL_ALPHA_TEST */);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                    //Draw the team scores
                    theMc.fontRenderer.drawString(PacketTeamInfo.teamData[0].score + "", i / 2 - 35, 9, 0x000000);
                    theMc.fontRenderer.drawString(PacketTeamInfo.teamData[0].score + "", i / 2 - 36, 8, 0xffffff);
                    theMc.fontRenderer.drawString(PacketTeamInfo.teamData[1].score + "", i / 2 + 35 - theMc.fontRenderer.getStringWidth(PacketTeamInfo.teamData[1].score + ""), 9, 0x000000);
                    theMc.fontRenderer.drawString(PacketTeamInfo.teamData[1].score + "", i / 2 + 34 - theMc.fontRenderer.getStringWidth(PacketTeamInfo.teamData[1].score + ""), 8, 0xffffff);
                }

                theMc.fontRenderer.drawString(PacketTeamInfo.gametype + "", i / 2 + 48, 9, 0x000000);
                theMc.fontRenderer.drawString(PacketTeamInfo.gametype + "", i / 2 + 47, 8, 0xffffff);
                theMc.fontRenderer.drawString(PacketTeamInfo.map + "", i / 2 - 47 - theMc.fontRenderer.getStringWidth(PacketTeamInfo.map + ""), 9, 0x000000);
                theMc.fontRenderer.drawString(PacketTeamInfo.map + "", i / 2 - 48 - theMc.fontRenderer.getStringWidth(PacketTeamInfo.map + ""), 8, 0xffffff);

                int secondsLeft = PacketTeamInfo.timeLeft / 20;
                int minutesLeft = secondsLeft / 60;
                secondsLeft = secondsLeft % 60;
                String timeLeft = minutesLeft + ":" + (secondsLeft < 10 ? "0" + secondsLeft : secondsLeft);
                theMc.fontRenderer.drawString(timeLeft, i / 2 - theMc.fontRenderer.getStringWidth(timeLeft) / 2 - 1, 29, 0x000000);
                theMc.fontRenderer.drawString(timeLeft, i / 2 - theMc.fontRenderer.getStringWidth(timeLeft) / 2, 30, 0xffffff);


                GL11.glDepthMask(true);
                GL11.glEnable(2929 /* GL_DEPTH_TEST */);
                GL11.glEnable(3008 /* GL_ALPHA_TEST */);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                String playerUsername = FlansModClient.minecraft.thePlayer.getCommandSenderName();

                theMc.fontRenderer.drawString(PacketTeamInfo.getPlayerScoreData(playerUsername).score + "", i / 2 - 7, 1, 0x000000);
                theMc.fontRenderer.drawString(PacketTeamInfo.getPlayerScoreData(playerUsername).kills + "", i / 2 - 7, 9, 0x000000);
                theMc.fontRenderer.drawString(PacketTeamInfo.getPlayerScoreData(playerUsername).deaths + "", i / 2 - 7, 17, 0x000000);
            }
            for (KillMessage killMessage : killMessages) {
                theMc.fontRenderer.drawString("\u00a7" + killMessage.killerName + "     " + "\u00a7" + killMessage.killedName, i - theMc.fontRenderer.getStringWidth(killMessage.killerName + "     " + killMessage.killedName) - 6, j - 32 - killMessage.line * 16, 0xffffff);
            }

            //Draw icons indicated weapons used
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            for (KillMessage killMessage : killMessages) {
                drawSlotInventory(theMc.fontRenderer, new ItemStack(killMessage.weapon.item), i - theMc.fontRenderer.getStringWidth("     " + killMessage.killedName) - 12, j - 36 - killMessage.line * 16);
            }
            GL11.glDisable(3042 /*GL_BLEND*/);
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

            //Off-hand weapon graphics
            theMc.renderEngine.bindTexture(offHand);

            ItemStack currentStack = theMc.thePlayer.inventory.getCurrentItem();
            PlayerData data = PlayerHandler.getPlayerData(theMc.thePlayer, Side.CLIENT);

            if (currentStack != null && currentStack.getItem() instanceof ItemGun && ((ItemGun) currentStack.getItem()).type.oneHanded) {
                for (int n = 0; n < 9; n++) {
                    if (data.offHandGunSlot == n + 1) {
                        tessellator.startDrawingQuads();
                        tessellator.addVertexWithUV(i / 2 - 88 + 20 * n, j - 3, -90D, 16D / 64D, 16D / 32D);
                        tessellator.addVertexWithUV(i / 2 - 72 + 20 * n, j - 3, -90D, 32D / 64D, 16D / 32D);
                        tessellator.addVertexWithUV(i / 2 - 72 + 20 * n, j - 19, -90D, 32D / 64D, 0D / 32D);
                        tessellator.addVertexWithUV(i / 2 - 88 + 20 * n, j - 19, -90D, 16D / 64D, 0D / 32D);
                        tessellator.draw();
                    } else if (data.isValidOffHandWeapon(theMc.thePlayer, n + 1)) {
                        tessellator.startDrawingQuads();
                        tessellator.addVertexWithUV(i / 2 - 88 + 20 * n, j - 3, -90D, 0D / 64D, 16D / 32D);
                        tessellator.addVertexWithUV(i / 2 - 72 + 20 * n, j - 3, -90D, 16D / 64D, 16D / 32D);
                        tessellator.addVertexWithUV(i / 2 - 72 + 20 * n, j - 19, -90D, 16D / 64D, 0D / 32D);
                        tessellator.addVertexWithUV(i / 2 - 88 + 20 * n, j - 19, -90D, 0D / 64D, 0D / 32D);
                        tessellator.draw();
                    }
                }
            }

            //DEBUG vehicles
            if (FlansMod.DEBUG && theMc.thePlayer.ridingEntity instanceof EntitySeat) {
                EntityDriveable ent = ((EntitySeat) theMc.thePlayer.ridingEntity).driveable;
                theMc.fontRenderer.drawString("MotionX : " + ent.motionX, 2, 2, 0xffffff);
                theMc.fontRenderer.drawString("MotionY : " + ent.motionY, 2, 12, 0xffffff);
                theMc.fontRenderer.drawString("MotionZ : " + ent.motionZ, 2, 22, 0xffffff);
                theMc.fontRenderer.drawString("Throttle : " + ent.throttle, 2, 32, 0xffffff);
                theMc.fontRenderer.drawString("Break Blocks : " + TeamsManager.driveablesBreakBlocks, 2, 42, 0xffffff);
            }
        }
    }

    private void drawTenCrossHair(int screenWidth, int screenHeight, float radius, GunType gunType) {
        float centerX = screenWidth / 2.0F, centerY = screenHeight / 2.0F;
        float[] xx = new float[8], yy = new float[8];
        float[] points = new float[16];
        float length = gunType.getCrossLength();
        float thickness = gunType.getCrossThick();
        xx[0] = centerX - radius;
        xx[1] = xx[0] - length;
        xx[2] = centerX + radius;
        xx[3] = xx[2] + length;
        yy[0] = yy[1] = yy[2] = yy[3] = centerY;

        yy[4] = centerY - radius;
        yy[5] = yy[4] - length;
        yy[6] = centerY + radius;
        yy[7] = yy[6] + length;
        xx[4] = xx[5] = xx[6] = xx[7] = centerX;

//        xx[8] = centerX - thickness / 2;
//        xx[9] = centerX + thickness / 2;
//        yy[8] = yy[9] = centerY;
//        yy[10] = centerY - thickness / 2;
//        yy[11] = centerY + thickness / 2;
//        xx[10] = xx[11] = centerX;

        for (int i = 0; i < 8; i++) {
            points[i * 2] = xx[i];
            points[i * 2 + 1] = yy[i];
        }

        RenderHelper.drawFilledRectangle(centerX - thickness / 4, centerY - thickness / 4, centerX + thickness / 2, centerY + thickness / 2, gunType.getCrossColor(), false);
        RenderHelper.drawLines(points, gunType.getCrossThick(), gunType.getCrossColor(), false);
    }

    private boolean drawCrossHair(int screenWidth, int screenHeight, int offset, float scale) {
        boolean bind = bindTexture(theMc.getTextureManager(), crossHair);
        if (!bind) bind = bindTexture(theMc.getTextureManager(), defaultCrossHair);
        if (bind) drawScaledCustomSizeModalRect(screenWidth / 2D - 127 * scale, screenHeight / 2D - 127 * scale,
                256 * offset, 0,
                256, 256,
                256 * scale, 256 * scale,
                1024, 256);
        return bind;
    }

    private boolean drawCrossHair(int screenWidth, int screenHeight, int offset) {
        if (bindTexture(theMc.getTextureManager(), crossHair)) {
            drawModalRectWithCustomSizedTexture(screenWidth / 2D - 127, screenHeight / 2D - 127, 256 * offset, 0, 256, 256, 1024, 256);
            return true;
        } else if (bindTexture(theMc.getTextureManager(), defaultCrossHair)) {
            drawModalRectWithCustomSizedTexture(screenWidth / 2D - 127, screenHeight / 2D - 127, 256 * offset, 0, 256, 256, 1024, 256);
            return true;
        } else return false;
    }

    private static boolean bindTexture(TextureManager manager, ResourceLocation resource) {
        HashMap map = ObfuscationReflectionHelper.getPrivateValue(TextureManager.class, manager, "mapTextureObjects", "field_110585_a", "b");
        Object object = map.get(resource);
        boolean success = true;
        if (object == null) {
            object = new SimpleTexture(resource);
            success = manager.loadTexture(resource, (ITextureObject) object);
        }
        success = success && object != TextureUtil.missingTexture;
        if (success) GL11.glBindTexture(GL11.GL_TEXTURE_2D, ((ITextureObject) object).getGlTextureId());
        return success;
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        switch (event.phase) {
            case START:
                RenderGun.smoothing = event.renderTickTime;
                renderTickStart(Minecraft.getMinecraft(), event.renderTickTime);
                break;
            case END:
                renderTickEnd(Minecraft.getMinecraft());
                break;
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                clientTickStart(Minecraft.getMinecraft());
                break;
            case END:
                clientTickEnd(Minecraft.getMinecraft());
                break;
        }
    }

    /**
     * Handle flashlight block light override
     */
    private void clientTickStart(Minecraft mc) {
        if (FlansMod.ticker % lightOverrideRefreshRate == 0 && mc.theWorld != null) {
            //Check graphics setting and adjust refresh rate
            lightOverrideRefreshRate = mc.gameSettings.fancyGraphics ? 10 : 20;

            //Reset old light values
            for (Vector3i v : blockLightOverrides) {
                mc.theWorld.updateLightByType(EnumSkyBlock.Block, v.x, v.y, v.z);
            }
            //Clear the list
            blockLightOverrides.clear();

            //Find all flashlights
            for (Object obj : mc.theWorld.playerEntities) {
                EntityPlayer player = (EntityPlayer) obj;
                ItemStack currentHeldItem = player.getCurrentEquippedItem();
                if (currentHeldItem != null && currentHeldItem.getItem() instanceof ItemGun) {
                    GunType type = ((ItemGun) currentHeldItem.getItem()).type;
                    AttachmentType grip = type.getGrip(currentHeldItem);
                    if (grip != null && grip.flashlight) {
                        for (int i = 0; i < 2; i++) {
                            MovingObjectPosition ray = player.rayTrace(grip.flashlightRange / 2F * (i + 1), 1F);
                            if (ray != null) {
                                int x = ray.blockX;
                                int y = ray.blockY;
                                int z = ray.blockZ;
                                int side = ray.sideHit;
                                switch (side) {
                                    case 0:
                                        y--;
                                        break;
                                    case 1:
                                        y++;
                                        break;
                                    case 2:
                                        z--;
                                        break;
                                    case 3:
                                        z++;
                                        break;
                                    case 4:
                                        x--;
                                        break;
                                    case 5:
                                        x++;
                                        break;
                                }
                                blockLightOverrides.add(new Vector3i(x, y, z));
                                mc.theWorld.setLightValue(EnumSkyBlock.Block, x, y, z, 12);
                                mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y + 1, z);
                                mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y - 1, z);
                                mc.theWorld.updateLightByType(EnumSkyBlock.Block, x + 1, y, z);
                                mc.theWorld.updateLightByType(EnumSkyBlock.Block, x - 1, y, z);
                                mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y, z + 1);
                                mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y, z - 1);
                            }
                        }
                    }
                }
            }

            for (Object obj : mc.theWorld.loadedEntityList) {
                if (obj instanceof EntityBullet) {
                    EntityBullet bullet = (EntityBullet) obj;
                    if (!bullet.isDead && bullet.type.hasLight) {
                        int x = MathHelper.floor_double(bullet.posX);
                        int y = MathHelper.floor_double(bullet.posY);
                        int z = MathHelper.floor_double(bullet.posZ);
                        blockLightOverrides.add(new Vector3i(x, y, z));
                        mc.theWorld.setLightValue(EnumSkyBlock.Block, x, y, z, 15);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y + 1, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y - 1, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x + 1, y, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x - 1, y, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y, z + 1);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y, z - 1);
                    }
                } else if (obj instanceof EntityMecha) {
                    EntityMecha mecha = (EntityMecha) obj;
                    int x = MathHelper.floor_double(mecha.posX);
                    int y = MathHelper.floor_double(mecha.posY);
                    int z = MathHelper.floor_double(mecha.posZ);
                    if (mecha.lightLevel() > 0) {
                        blockLightOverrides.add(new Vector3i(x, y, z));
                        mc.theWorld.setLightValue(EnumSkyBlock.Block, x, y, z, Math.max(mc.theWorld.getBlockLightValue(x, y, z), mecha.lightLevel()));
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x + 1, y, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x - 1, y + 1, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y + 1, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y - 1, z);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y, z + 1);
                        mc.theWorld.updateLightByType(EnumSkyBlock.Block, x, y, z - 1);
                    }
                    if (mecha.forceDark()) {
                        for (int i = -3; i <= 3; i++) {
                            for (int j = -3; j <= 3; j++) {
                                for (int k = -3; k <= 3; k++) {
                                    int xd = i + x;
                                    int yd = j + y;
                                    int zd = k + z;
                                    blockLightOverrides.add(new Vector3i(xd, yd, zd));
                                    mc.theWorld.setLightValue(EnumSkyBlock.Sky, xd, yd, zd, Math.abs(i) + Math.abs(j) + Math.abs(k));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void clientTickEnd(Minecraft minecraft) { /* Client side only */
        for (int i = 0; i < killMessages.size(); i++) {
            killMessages.get(i).timer--;
            if (killMessages.get(i).timer == 0) {
                killMessages.remove(i);
            }
        }
        RenderFlag.angle += 2F;
        FlansModClient.tick();
    }

    private void renderTickStart(Minecraft mc, float smoothing) {
        // CAPTURE MOUSE INPUT!
        if (mc.currentScreen == null && FlansModClient.controlModeMouse) {
            MouseHelper mouse = mc.mouseHelper;
            Entity ridden = mc.thePlayer.ridingEntity;

            if (ridden instanceof EntityDriveable) {
                EntityDriveable entity = (EntityDriveable) ridden;
                entity.onMouseMoved(mouse.deltaX, mouse.deltaY);
            }
        }

        FlansModClient.renderTick(smoothing);
    }

    private void renderTickEnd(Minecraft mc) {
		/*
		ScaledResolution scaledresolution = new ScaledResolution(FlansModClient.minecraft, FlansModClient.minecraft.displayWidth, FlansModClient.minecraft.displayHeight);
		int i = scaledresolution.getScaledWidth();
		int j = scaledresolution.getScaledHeight();
		
		String overlayTexture = null;
		if (FlansModClient.currentScope != null && FlansModClient.currentScope.hasZoomOverlay() && FMLClientHandler.instance().getClient().currentScreen == null && FlansModClient.zoomProgress > 0.8F)
		{
			overlayTexture = FlansModClient.currentScope.getZoomOverlay();
		}
		else if(mc.thePlayer != null)
		{
			ItemStack stack = mc.thePlayer.inventory.armorInventory[3];
			if(stack != null && stack.getItem() instanceof ItemTeamArmour)
			{
				overlayTexture = ((ItemTeamArmour)stack.getItem()).type.overlay;
			}
		}
		
		if(overlayTexture != null)
		{
			FlansModClient.minecraft.entityRenderer.setupOverlayRendering();
			GL11.glEnable(3042);
			GL11.glDisable(2929);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(770, 771);
			GL11.glColor4f(mc.ingameGUI.prevVignetteBrightness, mc.ingameGUI.prevVignetteBrightness, mc.ingameGUI.prevVignetteBrightness, 1.0F);
			GL11.glDisable(3008);

			mc.renderEngine.bindTexture(FlansModResourceHandler.getScope(overlayTexture));

			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(i / 2 - 2 * j, j, -90D, 0.0D, 1.0D);
			tessellator.addVertexWithUV(i / 2 + 2 * j, j, -90D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(i / 2 + 2 * j, 0.0D, -90D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(i / 2 - 2 * j, 0.0D, -90D, 0.0D, 0.0D);
			tessellator.draw();
			GL11.glDepthMask(true);
			GL11.glEnable(2929);
			GL11.glEnable(3008);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		*/
    }

    private void drawSlotInventory(FontRenderer fontRenderer, ItemStack itemstack, int i, int j) {
        if (itemstack == null || itemstack.getItem() == null)
            return;
        itemRenderer.renderItemIntoGUI(fontRenderer, FlansModClient.minecraft.renderEngine, itemstack, i, j);
        itemRenderer.renderItemOverlayIntoGUI(fontRenderer, FlansModClient.minecraft.renderEngine, itemstack, i, j);
    }

    public static void addKillMessage(boolean headshot, InfoType infoType, String killer, String killed) {
        for (KillMessage killMessage : killMessages) {
            killMessage.line++;
            if (killMessage.line > 10)
                killMessage.timer = 0;
        }
        killMessages.add(new KillMessage(headshot, infoType, killer, killed));
    }

    private static class KillMessage {
        KillMessage(boolean head, InfoType infoType, String killer, String killed) {
            headshot = head;
            killerName = killer;
            killedName = killed;
            weapon = infoType;
            line = 0;
            timer = 200;
        }

        String killerName;
        String killedName;
        public InfoType weapon;
        int timer;
        public int line;
        boolean headshot;
    }
}
