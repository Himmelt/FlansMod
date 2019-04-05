package com.flansmod.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

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
                FlansMod.proxy.reload();
                FlansMod.proxy.sendConfigToAll();
                sender.addChatMessage(new ChatComponentText("[FlansMod] config reloaded !"));
                return;
            }
        }
        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
    }
}
