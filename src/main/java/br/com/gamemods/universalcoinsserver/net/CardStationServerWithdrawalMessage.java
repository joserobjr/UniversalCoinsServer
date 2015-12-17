package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.tile.TileCardStation;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CardStationServerWithdrawalMessage
        implements IMessage, IMessageHandler<CardStationServerWithdrawalMessage, IMessage>
{
    private int x, y, z, withdrawalAmount;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(withdrawalAmount);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.withdrawalAmount = buf.readInt();
    }

    @Override
    public IMessage onMessage(CardStationServerWithdrawalMessage message, MessageContext ctx)
    {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        World world = playerEntity.worldObj;

        TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
        if (tileEntity instanceof TileCardStation)
        {
            TileCardStation cardStation = ((TileCardStation) tileEntity);
            if(!playerEntity.equals(cardStation.opener))
                return null;

            cardStation.state.coinWithdrawalAmount = message.withdrawalAmount;
        }

        return null;
    }
}
