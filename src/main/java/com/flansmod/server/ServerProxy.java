package com.flansmod.server;

import com.flansmod.common.CommonProxy;
import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.eventhandlers.FMLEventHandler;
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
    }

    public void syncConfig() {
        FlansMod.DEBUG = configFile.getBoolean("debugMode", Configuration.CATEGORY_GENERAL, FlansMod.DEBUG, "Debug Mode.");

        FlansMod.recoilMark = configFile.getString("recoilMark", Configuration.CATEGORY_GENERAL, FlansMod.recoilMark, "Lore Recoil Keyword.");
        FlansMod.accuracyMark = configFile.getString("accuracyMark", Configuration.CATEGORY_GENERAL, FlansMod.accuracyMark, "Lore Accuracy Keyword.");
        FlansMod.shootDelayMark = configFile.getString("shootDelayMark", Configuration.CATEGORY_GENERAL, FlansMod.shootDelayMark, "Lore ShootDelay Keyword.");
        FlansMod.sneakingMark = configFile.getString("sneakingMark", Configuration.CATEGORY_GENERAL, FlansMod.sneakingMark, "Lore Sneaking Keyword.");
        FlansMod.sprintingMark = configFile.getString("sprintingMark", Configuration.CATEGORY_GENERAL, FlansMod.sprintingMark, "Lore Sprinting Keyword.");

        if (configFile.hasChanged()) configFile.save();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ByteBuf buf = Unpooled.buffer();
            String content = FlansMod.recoilMark + "|"
                    + FlansMod.accuracyMark + "|"
                    + FlansMod.shootDelayMark + "|"
                    + FlansMod.sneakingMark + "|"
                    + FlansMod.sprintingMark;
            buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));
            flanChannel.sendTo(new FMLProxyPacket(new PacketBuffer(buf), "flansmod"), (EntityPlayerMP) event.player);
        }
    }
}
