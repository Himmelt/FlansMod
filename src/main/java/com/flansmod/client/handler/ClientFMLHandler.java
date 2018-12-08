package com.flansmod.client.handler;

import com.flansmod.common.FlansMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

import java.nio.charset.StandardCharsets;

public class ClientFMLHandler {
    @SubscribeEvent
    public void onReceivePacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        String content = event.packet.payload().toString(StandardCharsets.UTF_8);
        if (event.packet.channel().equals("flansmod")) {
            String[] ss = content.split("\\|");
            if (ss.length == 5) {
                FlansMod.recoilMark = ss[0];
                FlansMod.accuracyMark = ss[1];
                FlansMod.shootDelayMark = ss[2];
                FlansMod.sneakingMark = ss[3];
                FlansMod.sprintingMark = ss[4];
            }
        }
    }

    @SubscribeEvent
    public void onLogout(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        FlansMod.syncConfig();
    }
}
