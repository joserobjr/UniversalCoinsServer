package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.datastore.AccountAddress;
import br.com.gamemods.universalcoinsserver.datastore.PlayerData;
import br.com.gamemods.universalcoinsserver.tile.TileCardStation;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class TileCardStationMessage implements IMessage, IMessageHandler<TileCardStationMessage, IMessage>
{
    public TileCardStation owner;
    public int x, y, z, coinWithdrawalAmount, accountBalance, forcedMenuState, resetProgressBar, lockProgressBar;
    public boolean inUse, depositCoins, withdrawCoins, accountError;
    public AccountAddress primaryAccount;
    public AccountAddress customAccount;
    public AccountAddress cardAccount;
    public String playerName;
    public UUID playerUID;

    public void reset()
    {
        coinWithdrawalAmount = 0;
        accountBalance = 0;
        inUse = false;
        depositCoins = false;
        withdrawCoins = false;
        accountError = false;
        playerName = "";
        playerUID = null;
        accountBalance = 0;
        primaryAccount = null;
        customAccount = null;
        setOpener(owner.opener);
    }

    public void setOpener(EntityPlayer opener)
    {
        if(opener != null)
        {
            playerUID = opener.getUniqueID();
            playerName = opener.getCommandSenderName();
            inUse = true;
        }
        else
        {
            playerUID = null;
            playerName = "";
            inUse = false;
        }
    }

    public void setPlayerData(PlayerData playerData)
    {
        primaryAccount = playerData.getPrimaryAccount();
        customAccount = playerData.getAlternativeAccounts().isEmpty()?null:playerData.getAlternativeAccounts().iterator().next();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(coinWithdrawalAmount);
        buf.writeInt(accountBalance);
        buf.writeBoolean(inUse);
        buf.writeBoolean(depositCoins);
        buf.writeBoolean(withdrawCoins);
        buf.writeBoolean(accountError);
        ByteBufUtils.writeUTF8String(buf, playerName);
        ByteBufUtils.writeUTF8String(buf, playerUID == null?"":playerUID.toString());
        ByteBufUtils.writeUTF8String(buf, primaryAccount==null?"none":primaryAccount.getNumber().toString());
        ByteBufUtils.writeUTF8String(buf, cardAccount==null?"":cardAccount.getOwner().toString());
        ByteBufUtils.writeUTF8String(buf, customAccount==null?"none":customAccount.getName());
        ByteBufUtils.writeUTF8String(buf, customAccount==null?"none":customAccount.getNumber().toString());
        buf.writeInt(forcedMenuState);
        buf.writeInt(resetProgressBar);
        buf.writeInt(lockProgressBar);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TileCardStationMessage that = (TileCardStationMessage) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        if (resetProgressBar != that.resetProgressBar) return false;
        if (lockProgressBar != that.lockProgressBar) return false;
        if (coinWithdrawalAmount != that.coinWithdrawalAmount) return false;
        if (accountBalance != that.accountBalance) return false;
        if (forcedMenuState != that.forcedMenuState) return false;
        if (inUse != that.inUse) return false;
        if (depositCoins != that.depositCoins) return false;
        if (withdrawCoins != that.withdrawCoins) return false;
        if (accountError != that.accountError) return false;
        if (primaryAccount != null ? !primaryAccount.equals(that.primaryAccount) : that.primaryAccount != null)
            return false;
        if (customAccount != null ? !customAccount.equals(that.customAccount) : that.customAccount != null)
            return false;
        if (cardAccount != null ? !cardAccount.equals(that.cardAccount) : that.cardAccount != null) return false;
        if (playerName != null ? !playerName.equals(that.playerName) : that.playerName != null) return false;
        return !(playerUID != null ? !playerUID.equals(that.playerUID) : that.playerUID != null);

    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + resetProgressBar;
        result = 31 * result + lockProgressBar;
        result = 31 * result + coinWithdrawalAmount;
        result = 31 * result + accountBalance;
        result = 31 * result + forcedMenuState;
        result = 31 * result + (inUse ? 1 : 0);
        result = 31 * result + (depositCoins ? 1 : 0);
        result = 31 * result + (withdrawCoins ? 1 : 0);
        result = 31 * result + (accountError ? 1 : 0);
        result = 31 * result + (primaryAccount != null ? primaryAccount.hashCode() : 0);
        result = 31 * result + (customAccount != null ? customAccount.hashCode() : 0);
        result = 31 * result + (cardAccount != null ? cardAccount.hashCode() : 0);
        result = 31 * result + (playerName != null ? playerName.hashCode() : 0);
        result = 31 * result + (playerUID != null ? playerUID.hashCode() : 0);
        return result;
    }

    @Override
    public IMessage onMessage(TileCardStationMessage message, MessageContext ctx)
    {
        throw new UnsupportedOperationException();
    }

    public void force(int gui)
    {
        force(gui, 0, true);
    }

    public void force(int gui, int forcedProgress, boolean forceError)
    {
        forcedMenuState = gui;
        lockProgressBar = forcedProgress;
        accountError = forceError;
    }

    public void stopForcing()
    {
        forcedMenuState = -1;
        lockProgressBar  = -1;
        accountError = false;
    }
}
