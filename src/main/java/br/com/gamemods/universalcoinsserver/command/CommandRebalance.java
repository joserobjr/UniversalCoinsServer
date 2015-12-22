package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

public class CommandRebalance extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return StatCollector.translateToLocal("command.rebalance.name");
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return StatCollector.translateToLocal("command.rebalance.help");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] p_71515_2_)
    {
        EntityPlayer player = (EntityPlayer) sender;
        UniversalCoinsServerAPI.rebalance(player.inventory);
        player.inventoryContainer.detectAndSendChanges();
    }
}
