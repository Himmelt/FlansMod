package com.flansmod.common.eventhandlers;

import com.flansmod.common.CommonProxy;
import com.flansmod.common.FlansMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;

import java.nio.charset.StandardCharsets;

public class FMLEventHandler {

    private final CommonProxy proxy;

    public FMLEventHandler(CommonProxy proxy) {
        this.proxy = proxy;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            if (player.mcServer.isDedicatedServer()) {
                ByteBuf buf = Unpooled.buffer();
                String content = FlansMod.recoilMark + "|" + FlansMod.accuracyMark + "|" + FlansMod.sneakingMark + "|" + FlansMod.sprintingMark;
                buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));
                proxy.sendTo(player, buf);
            }
        }
    }
}
