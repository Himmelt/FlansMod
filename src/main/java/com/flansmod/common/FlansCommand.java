package com.flansmod.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class FlansCommand extends CommandBase {
    public String getCommandName() {
        return "flans";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/flans reload";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        if (args != null && args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                FlansMod.proxy.syncConfig();
                FlansMod.proxy.sendDataToAll();
            }
        }
    }
}
