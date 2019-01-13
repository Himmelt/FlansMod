package com.flansmod.client;

import com.flansmod.common.guns.GunType;
import com.flansmod.common.utils.RGBA;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class ClientFMLHandler {

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        GunType.defCrossType = "default";
        GunType.defCrossLength = 10F;
        GunType.defCrossSneakRadius = 5F;
        GunType.defCrossNormalRadius = 8F;
        GunType.defCrossSprintingRadius = 11F;
        GunType.defCrossFireRadius = 14F;
        GunType.defCrossThick = 1F;
        GunType.defCrossSpeed = 0.5F;
        GunType.defCrossColor = RGBA.WHITE;
    }
}
