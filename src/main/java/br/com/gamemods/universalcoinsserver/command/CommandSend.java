package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.api.ScanResult;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import scala.actors.threadpool.Arrays;

import java.util.List;

public class CommandSend extends CommandBase
{
    final String name;
    final List aliases;

    public CommandSend(String commandBalance)
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
        return StatCollector.translateToLocal("command.send.help");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if(args.length != 2)
        {
            commandSender.addChatMessage(new ChatComponentTranslation("command.send.error.incomplete")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            );
            return;
        }

        EntityPlayer sender = (EntityPlayer) commandSender;

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
            sender.addChatMessage(new ChatComponentTranslation("command.send.error.notfound").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            return;
        }

        int amount;
        try
        {
            amount = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            sender.addChatMessage(new ChatComponentTranslation("command.send.error.badentry").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            return;
        }

        if(amount <= 0)
        {
            sender.addChatMessage(new ChatComponentTranslation("command.send.error.badentry").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            return;
        }

        ScanResult scanResult = UniversalCoinsServerAPI.scanCoins(sender.inventory);
        if(scanResult.getCoins() < amount)
        {
            sender.addChatComponentMessage(new ChatComponentTranslation("command.send.error.insufficient").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            return;
        }

        UniversalCoinsServerAPI.takeCoinsReturningChange(scanResult, amount, sender, 3);
        UniversalCoinsServerAPI.giveCoins(receiver, amount, 3);

        sender.addChatComponentMessage(
                new ChatComponentText(Integer.toString(amount)+" ")
                        .appendSibling(new ChatComponentTranslation("command.send.result.sender"))
                        .appendText(" ")
                        .appendText(receiver.getCommandSenderName())
        );

        receiver.addChatComponentMessage(
                new ChatComponentText(Integer.toString(amount)+" ")
                .appendSibling(new ChatComponentTranslation("command.send.result.receiver"))
                .appendText(" "+sender.getCommandSenderName())
        );
    }
}
