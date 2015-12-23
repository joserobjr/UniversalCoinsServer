package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.AccountAddress;
import br.com.gamemods.universalcoinsserver.datastore.AccountNotFoundException;
import br.com.gamemods.universalcoinsserver.datastore.DataStoreException;
import br.com.gamemods.universalcoinsserver.datastore.PlayerData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

public class CommandBalance extends CommandBase
{
    final String name;
    final List aliases;

    public CommandBalance(String commandBalance)
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
        return StatCollector.translateToLocal("command.balance.help");
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
        int playerCoins = UniversalCoinsServerAPI.scanCoins(player.inventory).getCoins();

        int primaryBalance = -1, secondaryBalance = -1;

        try
        {
            PlayerData playerData = UniversalCoinsServer.cardDb.getPlayerData(player.getPersistentID());
            if(playerData.hasPrimaryAccount())
                try
                {
                    primaryBalance = UniversalCoinsServer.cardDb.getAccountBalance(playerData.getPrimaryAccount());
                } catch (AccountNotFoundException e)
                {
                    e.printStackTrace();
                }

            if(playerData.hasCustomAccount())
            {
                boolean display = false;
                int balance = 0;
                for (AccountAddress address: playerData.getAlternativeAccounts())
                    try
                    {
                        balance += UniversalCoinsServer.cardDb.getAccountBalance(address);
                        display = true;
                    }
                    catch (AccountNotFoundException e)
                    {
                        e.printStackTrace();
                    }

                if(display)
                    secondaryBalance = balance;
            }
        }
        catch (DataStoreException e)
        {
            e.printStackTrace();
        }

        NumberFormat format = NumberFormat.getIntegerInstance();
        player.addChatComponentMessage(new ChatComponentTranslation("command.balance.result.inventory").appendText(format.format(playerCoins)));

        if(primaryBalance >= 0)
            player.addChatComponentMessage(new ChatComponentTranslation("command.balance.result.account").appendText(format.format(primaryBalance)));

        if(secondaryBalance >= 0)
            player.addChatComponentMessage(new ChatComponentTranslation("command.balance.result.customaccount").appendText(format.format(secondaryBalance)));

        if(playerCoins >= 1000 || primaryBalance >= 1000 || secondaryBalance >= 1000)
            player.addStat(UniversalCoinsServer.proxy.achievementThousand, 1);
        if(playerCoins >= 1000000 || primaryBalance >= 1000000 || secondaryBalance >= 1000000)
            player.addStat(UniversalCoinsServer.proxy.achievementMillion, 1);
        if(playerCoins >= 1000000000 || primaryBalance >= 1000000000 || secondaryBalance >= 1000000000)
            player.addStat(UniversalCoinsServer.proxy.achievementBillion, 1);
        if(playerCoins == Integer.MAX_VALUE || primaryBalance == Integer.MAX_VALUE || secondaryBalance == Integer.MAX_VALUE)
            player.addStat(UniversalCoinsServer.proxy.achievementMaxed, 1);
    }
}
