package com.flansmod.client.handler;

import com.flansmod.common.guns.ItemGun;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public class ClientBusHandler {
    @SubscribeEvent(receiveCanceled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        System.out.println("PlayerInteractEvent");
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && player.capabilities.isCreativeMode) {
            ItemStack stack = player.getHeldItem();
            if (stack != null && stack.getItem() instanceof ItemGun) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void on(BlockEvent.BreakEvent event) {
        System.out.println("BreakEvent");
    }
}
