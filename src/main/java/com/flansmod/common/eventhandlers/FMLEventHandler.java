package com.flansmod.common.eventhandlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.UUID;

public class FMLEventHandler {

    private static final HashMap<UUID, Boolean> temps = new HashMap<>();
    private static final HashMap<UUID, Boolean> sneaking = new HashMap<>();
    private static final HashMap<UUID, Long> lasts = new HashMap<>();

    private static boolean getTemp(UUID uuid) {
        return temps.containsKey(uuid) ? temps.get(uuid) : false;
    }

    private static long getLast(UUID uuid) {
        return lasts.containsKey(uuid) ? lasts.get(uuid) : 0L;
    }

    public static boolean isSneaking(UUID uuid) {
        return sneaking.containsKey(uuid) ? sneaking.get(uuid) : false;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        UUID uuid = event.player.getUniqueID();
        boolean sneak = event.player.isSneaking();
        if (!sneak) {
            temps.put(uuid, false);
            sneaking.put(uuid, false);
            lasts.put(uuid, System.currentTimeMillis());
            return;
        }
        if (!getTemp(uuid)) {
            temps.put(uuid, true);
            lasts.put(uuid, System.currentTimeMillis());
            return;
        }
        if (isSneaking(uuid) || System.currentTimeMillis() - getLast(uuid) >= 2000) {
            sneaking.put(uuid, true);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            UUID uuid = player.getUniqueID();
            if (player.mcServer.isDedicatedServer()) {
                temps.remove(uuid);
                sneaking.remove(uuid);
                lasts.remove(uuid);
            }
        }
    }
}
