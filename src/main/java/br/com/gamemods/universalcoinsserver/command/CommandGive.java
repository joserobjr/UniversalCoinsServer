package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import scala.actors.threadpool.Arrays;

import java.text.NumberFormat;
import java.util.List;

public class CommandGive extends CommandBase
{
    final String name;
    final List aliases;

    public CommandGive(String commandBalance)
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
        return StatCollector.translateToLocal("command.givecoins.help");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if(args.length != 2)
        {
            sender.addChatMessage(
                    new ChatComponentTranslation("command.givecoins.error.noname")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        EntityPlayer receiver = null;
        WorldServer[] ws = MinecraftServer.getServer().worldServers;
        for (WorldServer w : ws)
        {
            EntityPlayer player = w.getPlayerEntityByName(args[0]);
            if (w.playerEntities.contains(player))
                receiver = player;
        }

        if(receiver == null)
        {
            sender.addChatMessage(
                    new ChatComponentTranslation("command.givecoins.error.notfound")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        int coinsToSend;
        try
        {
            coinsToSend = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            sender.addChatMessage(
                    new ChatComponentTranslation("command.givecoins.error.badentry")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        if(coinsToSend <= 0)
        {
            sender.addChatMessage(
                    new ChatComponentTranslation("command.givecoins.error.badentry")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        int change = UniversalCoinsServerAPI.giveCoins(receiver, coinsToSend);
        if(change > 0)
            coinsToSend -= change;
        else if(change < 0)
            coinsToSend += -change;

        sender.addChatMessage(new ChatComponentText("Gave " + receiver.getCommandSenderName() + " " + coinsToSend + " ")
                        .appendSibling(new ChatComponentTranslation("item.itemCoin.name"))
        );

        receiver.addChatComponentMessage(new ChatComponentText(sender.getCommandSenderName() + " ")
                        .appendSibling(new ChatComponentTranslation("command.givecoins.result"))
                        .appendText(" " + NumberFormat.getIntegerInstance().format(coinsToSend)+" ")
                        .appendSibling(new ChatComponentTranslation("item.itemCoin.name"))
        );
    }
}
