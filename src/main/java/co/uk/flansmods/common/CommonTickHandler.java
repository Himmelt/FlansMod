package co.uk.flansmods.common;

import co.uk.flansmods.common.teams.TeamsManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.server.MinecraftServer;

import java.util.EnumSet;

public class CommonTickHandler implements ITickHandler {

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (type.equals(EnumSet.of(TickType.SERVER))) {
            sTick(FMLCommonHandler.instance().getMinecraftServerInstance());
        }
    }

    public void sTick(MinecraftServer minecraft) {
        /* Server */
        TeamsManager.getInstance().tick();
        FlansMod.playerHandler.tick();
        FlansMod.ticker++;
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() {
        return null;
    }

}
