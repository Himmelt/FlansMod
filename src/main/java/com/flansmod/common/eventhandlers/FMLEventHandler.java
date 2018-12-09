package com.flansmod.common.eventhandlers;

import com.flansmod.common.CommonProxy;
import com.flansmod.common.FlansMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

public class FMLEventHandler {

    private final CommonProxy proxy;
    private static final HashMap<UUID, Boolean> temps = new HashMap<>();
    private static final HashMap<UUID, Boolean> sneaking = new HashMap<>();
    private static final HashMap<UUID, Long> lasts = new HashMap<>();

    public FMLEventHandler(CommonProxy proxy) {
        this.proxy = proxy;
    }

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
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            if (player.mcServer.isDedicatedServer()) {
                ByteBuf buf = Unpooled.buffer();
                String content = FlansMod.recoilMark + "|"
                        + FlansMod.accuracyMark + "|"
                        + FlansMod.shootDelayMark + "|"
                        + FlansMod.sneakingMark + "|"
                        + FlansMod.sprintingMark;
                buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));
                proxy.sendTo(player, buf);
            }
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
