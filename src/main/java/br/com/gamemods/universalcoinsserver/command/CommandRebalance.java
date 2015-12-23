package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

import java.util.Arrays;
import java.util.List;

public class CommandRebalance extends CommandBase
{
    final String name;
    final List aliases;

    public CommandRebalance(String commandBalance)
    {
        String[] split = commandBalance.split("\\s*,\\s*");
        this.name = split[0].trim();
        if(name.isEmpty()) throw new IllegalArgumentException();
        aliases = Arrays.asList(Arrays.copyOfRange(split, 1, split.length));
    }

    @Override
    public String getCommandName()
    {
        return name;
    }

    @Override
    public List getCommandAliases()
    {
        return aliases;
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
