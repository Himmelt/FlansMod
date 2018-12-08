package com.flansmod.client.handler;

import com.flansmod.common.FlansMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;

import java.nio.charset.StandardCharsets;

public class ClientFMLHandler {

    public static boolean isSneaking = false;
    private static boolean temp = false;
    private static long last = 0;

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer != null) {
            boolean sneak = mc.thePlayer.isSneaking();
            if (!sneak) {
                temp = false;
                isSneaking = false;
                last = System.currentTimeMillis();
                return;
            }
            if (!temp) {
                temp = true;
                last = System.currentTimeMillis();
                return;
            }
            if (isSneaking || System.currentTimeMillis() - last >= 2000) {
                isSneaking = true;
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        String content = event.packet.payload().toString(StandardCharsets.UTF_8);
        if (event.packet.channel().equals("flansmod")) {
            String[] ss = content.split("\\|");
            if (ss.length == 4) {
                FlansMod.recoilMark = ss[0];
                FlansMod.accuracyMark = ss[1];
                FlansMod.sneakingMark = ss[2];
                FlansMod.sprintingMark = ss[3];
            }
        }
    }

    @SubscribeEvent
    public void onLogout(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        FlansMod.syncConfig();
    }
}
