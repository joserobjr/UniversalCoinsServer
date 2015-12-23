package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.datastore.DataStoreException;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.Arrays;
import java.util.List;

public class CommandReceivePackets extends CommandBase
{
    final String name;
    final List aliases;

    public CommandReceivePackets(String commandBalance)
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
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "Receives your packets";
    }

    @Override
    public List getCommandAliases()
    {
        return aliases;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayer player = (EntityPlayer) sender;
        try
        {
            int deliveries = UniversalCoinsServer.cardDb.getPendingDeliveries(player.getPersistentID());
            if(deliveries == 0)
            {
                sender.addChatMessage(new ChatComponentTranslation("no.packets.remaining"));
                return;
            }

            UniversalCoinsServer.cardDb.deliveryPackages(player);
        } catch (DataStoreException e)
        {
            e.printStackTrace();
        }
    }
}
