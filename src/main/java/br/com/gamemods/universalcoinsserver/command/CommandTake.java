package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.api.ScanResult;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;

public class CommandTake extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "takecoins";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "takecoins";
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

        int coinsToTake;
        try
        {
            coinsToTake = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            sender.addChatMessage(
                    new ChatComponentTranslation("command.givecoins.error.badentry")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        if(coinsToTake <= 0)
        {
            sender.addChatMessage(
                    new ChatComponentTranslation("command.givecoins.error.badentry")
                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        ScanResult scanResult = UniversalCoinsServerAPI.scanCoins(receiver.inventory);
        int change = UniversalCoinsServerAPI.takeCoinsReturningChange(scanResult, coinsToTake, receiver);
        sender.addChatMessage(new ChatComponentText("Took "+(coinsToTake)+" with change "+change+" (actually took "+(coinsToTake+change)+")"));
    }
}
