package co.uk.flansmods.common;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.EnumSet;

public class ServerTickHandler implements ITickHandler {

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        EntityPlayer player = (EntityPlayer) tickData[0];
        World world = player.worldObj;

        // do whatever here
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        EntityPlayer player = (EntityPlayer) tickData[0];
        World world = player.worldObj;

        // do whatever here
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public String getLabel() {
        return "FlansMod ServerTickhandler";
    }

}
