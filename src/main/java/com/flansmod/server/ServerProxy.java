package com.flansmod.server;

import com.flansmod.common.CommonProxy;
import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.eventhandlers.FMLEventHandler;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.utils.RGBA;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

import java.nio.charset.StandardCharsets;

import static com.flansmod.common.FlansMod.configFile;

public class ServerProxy extends CommonProxy {

    private final FMLEventChannel flanChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel("flansmod");

    public void load() {

    }

    public void forceReload() {

    }

    public void registerRenderers() {

    }

    public void doTutorialStuff(EntityPlayer player, EntityDriveable entityType) {

    }

    public void changeControlMode(EntityPlayer player) {

    }

    public Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new FMLEventHandler());
        FMLCommonHandler.instance().bus().register(this);
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    public void syncConfig() {
        FlansMod.DEBUG = configFile.getBoolean("debugMode", Configuration.CATEGORY_GENERAL, FlansMod.DEBUG, "Debug Mode.");

        FlansMod.recoilMark = configFile.getString("recoilMark", Configuration.CATEGORY_GENERAL, FlansMod.recoilMark, "Lore Recoil Keyword.");
        FlansMod.accuracyMark = configFile.getString("accuracyMark", Configuration.CATEGORY_GENERAL, FlansMod.accuracyMark, "Lore Accuracy Keyword.");
        FlansMod.shootDelayMark = configFile.getString("shootDelayMark", Configuration.CATEGORY_GENERAL, FlansMod.shootDelayMark, "Lore ShootDelay Keyword.");
        FlansMod.reloadTimeMark = configFile.getString("reloadTimeMark", Configuration.CATEGORY_GENERAL, FlansMod.reloadTimeMark, "Lore ReloadTime Keyword.");
        FlansMod.sneakingMark = configFile.getString("sneakingMark", Configuration.CATEGORY_GENERAL, FlansMod.sneakingMark, "Lore Sneaking Keyword.");
        FlansMod.sprintingMark = configFile.getString("sprintingMark", Configuration.CATEGORY_GENERAL, FlansMod.sprintingMark, "Lore Sprinting Keyword.");

        GunType.defCrossType = configFile.getString("defCrossType", "cross-hair", "default", "defCrossType");
        GunType.defCrossLength = configFile.getFloat("defCrossLength", "cross-hair", 10F, 0F, 100F, "defCrossLength 0-100");
        GunType.defCrossSneakRadius = configFile.getFloat("defCrossSneakRadius", "cross-hair", 5F, 0F, 100F, "defCrossSneakRadius 0-100");
        GunType.defCrossNormalRadius = configFile.getFloat("defCrossNormalRadius", "cross-hair", 8F, 0F, 100F, "defCrossNormalRadius 0-100");
        GunType.defCrossSprintingRadius = configFile.getFloat("defCrossSprintingRadius", "cross-hair", 11F, 0F, 100F, "defCrossSprintingRadius 0-100");
        GunType.defCrossFireRadius = configFile.getFloat("defCrossFireRadius", "cross-hair", 14F, 0F, 100F, "defCrossFireRadius 0-100");
        GunType.defCrossThick = configFile.getFloat("defCrossThick", "cross-hair", 1F, 0F, 100F, "defCrossThick 0-100");
        GunType.defCrossSpeed = configFile.getFloat("defCrossSpeed", "cross-hair", 0.5F, 0F, 100F, "defCrossSpeed 0-100");
        GunType.defCrossColor = RGBA.parseColor(configFile.getString("defCrossColor", "cross-hair", "rgba(255,255,255,255)", "defCrossColor rgba(red,green,blue,opacity)"), null);

        if (configFile.hasChanged()) configFile.save();
    }

    public void reload() {
        configFile.load();
        syncConfig();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(getData().getBytes(StandardCharsets.UTF_8));
            flanChannel.sendTo(new FMLProxyPacket(new PacketBuffer(buf), "flansmod"), (EntityPlayerMP) event.player);
        }
    }

    public void sendConfigToAll() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(getData().getBytes(StandardCharsets.UTF_8));
        flanChannel.sendToAll(new FMLProxyPacket(new PacketBuffer(buf), "flansmod"));
    }

    private String getData() {
        return FlansMod.recoilMark + "|"
                + FlansMod.accuracyMark + "|"
                + FlansMod.shootDelayMark + "|"
                + FlansMod.reloadTimeMark + "|"
                + FlansMod.sneakingMark + "|"
                + FlansMod.sprintingMark + "|"
                + GunType.defCrossType + "|"
                + GunType.defCrossLength + "|"
                + GunType.defCrossSneakRadius + "|"
                + GunType.defCrossNormalRadius + "|"
                + GunType.defCrossSprintingRadius + "|"
                + GunType.defCrossFireRadius + "|"
                + GunType.defCrossThick + "|"
                + GunType.defCrossSpeed + "|"
                + GunType.defCrossColor;
    }
}
